package com.glodon.linglong.replication.real;

import com.glodon.linglong.base.exception.UnmodifiableReplicaException;
import com.glodon.linglong.engine.config.DatabaseConfig;
import com.glodon.linglong.engine.core.frame.Database;
import com.glodon.linglong.engine.core.frame.Index;
import com.glodon.linglong.engine.event.EventListener;
import com.glodon.linglong.engine.event.EventType;
import com.glodon.linglong.engine.event.ReplicationEventListener;
import com.glodon.linglong.engine.extend.RecoveryHandler;
import com.glodon.linglong.replication.DatabaseReplicator;
import com.glodon.linglong.replication.Role;
import com.glodon.linglong.replication.confg.ReplicatorConfig;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.Assert.assertNotNull;

/**
 * Created by liuj-ai on 2021/2/4.
 */
public class TestNodeRepl_0 {

    private final static int index = 0;
    //数据库端口
    public final static int replPort = 10000;
    //数据库目录
    private final static File replBaseFile = new File("C:\\Users\\liuj-ai\\Desktop\\数据库开发\\node-" + index + "\\data");
    //数据库集群
    private static Database database;
    //数据库配置
    private static DatabaseConfig dbConfig;
    //服务网络
    private static ServerSocket serverSocket;
    //数据库集群复制配置
    private static ReplicatorConfig replConfig;
    //数据集群复制器
    private static DatabaseReplicator replicator;

    public static void main(String[] args) throws Exception {
        try {
            //启动数据库集群3个成员
            start(Role.NORMAL, null);
            Thread.sleep(10000L);
            teetRW();
        } finally {
            //关闭数据库集群
            close();
        }
    }

    private static void teetRW() throws Exception {
        System.out.println("Node " + index + " 开始进行写测试...");
        for (int j = 1; j <= 10000; j++) {
            try {
                byte[] key = ("hello-world-" + j).getBytes();
                byte[] value = ("ling-long-" + j).getBytes();
                Index idx = database.openIndex("test");
                idx.store(null, key, value);
            } catch (Exception ex) {
                System.out.println("Node " + index + " 数据不能写入");
            } finally {
                System.out.println("Node " + index + " 已进行第" + j + "次集群写测试");
                Thread.sleep(5000L);
            }
        }
        System.out.println("Node " + index + " 结束进行写测试...");
    }

    private static Database start(Role replicaRole, Supplier<RecoveryHandler> handlerSupplier) throws Exception {
        serverSocket = new ServerSocket(replPort);
        replConfig = new ReplicatorConfig().groupToken(1).localSocket(serverSocket).baseFile(replBaseFile);
        replConfig.eventListener(new ReplicationEventListener(new EventListener() {
            @Override
            public void notify(EventType type, String message, Object... args) {
                System.out.println("Node " + index + " type=" + type.toString() + " message=" + message + " args=" + Arrays.toString(args));
            }
        }));

        //if (i > 0) {
        //    replConfigs[i].addSeed(serverSockets[0].getLocalSocketAddress());
        //    replConfigs[i].localRole(replicaRole);
        //}

        replicator = DatabaseReplicator.open(replConfig);
        dbConfig = new DatabaseConfig()
                .baseFile(replBaseFile)
                .replicate(replicator)
                .lockTimeout(5, TimeUnit.SECONDS)
                .directPageAccess(false);

        if (handlerSupplier != null) {
            dbConfig.recoveryHandler(handlerSupplier.get());
        }

        Database db = Database.open(dbConfig);
        database = db;

        readyCheck:
        {
            for (int trial = 0; trial < 100; trial++) {
                Thread.sleep(100);
                if (index == 0) {
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

            throw new AssertionError(index == 0 ? "No leader" : "Not joined");
        }
        System.out.println("Node " + index + " 创建成功....");
        return database;
    }

    private static void close() throws IOException {
        if (database != null) {
            database.close();
        }
        System.out.println("Node " + index + " 销毁成功....");
    }
}
