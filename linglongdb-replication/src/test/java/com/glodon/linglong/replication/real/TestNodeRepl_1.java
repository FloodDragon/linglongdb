package com.glodon.linglong.replication.real;

import com.glodon.linglong.base.exception.UnmodifiableReplicaException;
import com.glodon.linglong.engine.config.DatabaseConfig;
import com.glodon.linglong.engine.core.frame.Cursor;
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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.Assert.assertNotNull;

/**
 * Created by liuj-ai on 2021/2/4.
 */
public class TestNodeRepl_1 {

    private final static int index = 1;
    //数据库端口
    private final static int replPort = 10001;
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
            testR();
        } finally {
            //关闭数据库集群
            close();
        }
    }

    private static void testR() throws Exception {
        System.out.println("Node " + index + " 开始进行读测试...");
        Index idx = database.openIndex("test");
        for (int i = 0; i < 10000; i++) {
            //debugPrint(idx);
            Thread.sleep(30000L);
        }
        System.out.println("Node " + index + " 结束进行读测试...");
    }

    private static void debugPrint(Index index) {
        System.out.println("Node " + index + " 全文扫描 >>>>>>>    开始     >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
        try {
            Cursor namesCursor = index.newCursor(null);
            try {
                namesCursor.first();
                byte[] key;
                while ((key = namesCursor.key()) != null) {
                    byte[] value = namesCursor.value();
                    System.out.println("key string=" + new String(key));
                    if (value != null && value.length > 0) {
                        System.out.println("value string=" + new String(value));
                    }
                    namesCursor.next();
                }
            } finally {
                namesCursor.reset();
                namesCursor.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.println("Node " + index + " 全文扫描 >>>>>>>    结束     >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
        }
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

        if (index > 0) {
            replConfig.addSeed(new InetSocketAddress(TestNodeRepl_0.replPort));
            replConfig.localRole(replicaRole);
        }

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
