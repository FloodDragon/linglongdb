package com.glodon.linglong.replication;

import com.glodon.linglong.base.exception.UnmodifiableReplicaException;
import com.glodon.linglong.engine.config.DatabaseConfig;
import com.glodon.linglong.engine.core.frame.Database;
import com.glodon.linglong.engine.core.frame.Index;
import com.glodon.linglong.engine.event.EventListener;
import com.glodon.linglong.engine.event.EventType;
import com.glodon.linglong.engine.event.ReplicationEventListener;
import com.glodon.linglong.engine.extend.RecoveryHandler;
import com.glodon.linglong.replication.confg.ReplicatorConfig;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.Assert.assertNotNull;

/**
 * 测试数据库集群流复制
 * <p>
 * Created by liuj-ai on 2021/2/3.
 */
public class TestRepl {

    //数据库端口
    private static int[] replPorts;
    //数据库目录
    private static File[] replBaseFiles;
    //数据库集群
    private static Database[] databases;
    //数据库配置
    private static DatabaseConfig[] dbConfigs;
    //服务网络
    private static ServerSocket[] serverSockets;
    //数据库集群复制配置
    private static ReplicatorConfig[] replConfigs;
    //数据集群复制器
    private static DatabaseReplicator[] replicators;

    public static void main(String[] args) throws Exception {
        try {
            //启动数据库集群3个成员
            start(3, Role.NORMAL, null);
            teetRW();
            Thread.sleep(10000L);
        } finally {
            //关闭数据库集群
            close();
        }
    }

    private static void teetRW() throws Exception {
        System.out.println("开始进行读写测试...");
        for (int j = 1; j <= 10000; j++) {
            byte[] key = ("hello-world-" + j).getBytes();
            byte[] value = ("ling-long-" + j).getBytes();
            Index ix0 = databases[0].openIndex("test");
            //写入数据
            ix0.store(null, key, value);
            Thread.sleep(1000L);
            for (int i = 0; i < databases.length; i++) {
                Index ix = databases[i].openIndex("test");
                byte[] actual = ix.load(null, key);
                System.out.println("数据库实例: " + i + " Key: " + new String(key) + "  Value: " + new String(actual));
            }
            System.out.println("已进行第" + j + "次集群读写测试");
        }
        System.out.println("结束进行读写测试...");
    }

    private static Database[] start(int members, Role replicaRole, Supplier<RecoveryHandler> handlerSupplier) throws Exception {
        if (members < 1) {
            throw new IllegalArgumentException();
        }
        //服务网络
        serverSockets = new ServerSocket[members];
        for (int i = 0; i < members; i++) {
            serverSockets[i] = new ServerSocket(0);
        }
        replBaseFiles = new File[members];
        replPorts = new int[members];
        replConfigs = new ReplicatorConfig[members];
        replicators = new DatabaseReplicator[members];
        dbConfigs = new DatabaseConfig[members];
        databases = new Database[members];
        for (int i = 0; i < members; i++) {
            replBaseFiles[i] = TestUtils.newTempBaseFile(TestRepl.class);
            replPorts[i] = serverSockets[i].getLocalPort();
            replConfigs[i] = new ReplicatorConfig().groupToken(1).localSocket(serverSockets[i]).baseFile(replBaseFiles[i]);
            final int index = i;
            replConfigs[i].eventListener(new ReplicationEventListener(new EventListener() {
                @Override
                public void notify(EventType type, String message, Object... args) {
                    System.out.println("数据库实例 " + index + " type=" + type.toString() + " message=" + message + " args=" + Arrays.toString(args));
                }
            }));
            if (i > 0) {
                replConfigs[i].addSeed(serverSockets[0].getLocalSocketAddress());
                replConfigs[i].localRole(replicaRole);

            }
            replicators[i] = DatabaseReplicator.open(replConfigs[i]);

            dbConfigs[i] = new DatabaseConfig()
                    .baseFile(replBaseFiles[i])
                    .replicate(replicators[i])
                    .lockTimeout(5, TimeUnit.SECONDS)
                    .directPageAccess(false);

            if (handlerSupplier != null) {
                dbConfigs[i].recoveryHandler(handlerSupplier.get());
            }

            Database db = Database.open(dbConfigs[i]);
            databases[i] = db;

            readyCheck:
            {
                for (int trial = 0; trial < 100; trial++) {
                    Thread.sleep(100);
                    if (i == 0) {
                        try {
                            db.openIndex("first");
                            db.checkpoint();
                            break readyCheck;
                        } catch (UnmodifiableReplicaException e) {
                        }
                    } else {
                        assertNotNull(db.openIndex("first"));
                        break readyCheck;
                    }
                }

                throw new AssertionError(i == 0 ? "No leader" : "Not joined");
            }
        }
        System.out.println("数据库集群创建成功....");
        return databases;
    }

    private static void close() throws IOException {
        if (databases != null) {
            for (Database db : databases) {
                if (db != null) {
                    db.close();
                }
            }
        }
        TestUtils.deleteTempFiles(TestRepl.class);
        System.out.println("数据库集群销毁成功....");
    }
}
