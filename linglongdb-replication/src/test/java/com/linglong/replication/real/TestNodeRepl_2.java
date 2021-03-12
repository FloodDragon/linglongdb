package com.linglong.replication.real;

import com.linglong.base.exception.UnmodifiableReplicaException;
import com.linglong.engine.config.DatabaseConfig;
import com.linglong.engine.core.frame.Cursor;
import com.linglong.engine.core.frame.Database;
import com.linglong.engine.core.frame.Index;
import com.linglong.engine.core.tx.Transaction;
import com.linglong.engine.event.EventListener;
import com.linglong.engine.event.EventType;
import com.linglong.engine.event.ReplicationEventListener;
import com.linglong.engine.extend.RecoveryHandler;
import com.linglong.replication.DatabaseReplicator;
import com.linglong.replication.Role;
import com.linglong.replication.confg.ReplicatorConfig;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.Assert.assertNotNull;

/**
 * @author Stereo on 2021/2/4.
 */
public class TestNodeRepl_2 {

    private final static int index = 2;
    //数据库端口
    private final static int replPort = 10002;
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
            testRW();
        } finally {
            //关闭数据库集群
            close();
        }
    }

    private static void testRW() throws Exception {
        for (int j = 1; j <= 10000; j++) {
            try {
                System.out.println("Node " + index + " 开始进行写测试...");
                int code = ThreadLocalRandom.current().nextInt();
                byte[] key = ("Node " + index + " 写入key " + code).getBytes();
                byte[] value = ("Node " + index + " 写入value " + code).getBytes();
                //Transaction tx = database.newTransaction();
                Index idx = database.openIndex("test");
                idx.store(null, key, value);
                //tx.commit();
                System.out.println("Node " + index + " 数据写入成功 key=" + new String(key) + " value=" + new String(value));
            } catch (Exception ex) {
                System.out.println("Node " + index + " 不是主节点,数据不能写入,转读取");
                //ex.printStackTrace();
                testR();
            } finally {
                System.out.println("Node " + index + " 结束进行写测试...");
                Thread.sleep(3000L);
            }
            //testR();
        }
    }

    private static void testR() throws Exception {
        System.out.println("Node " + index + " 开始进行读测试...");
        Index idx = database.openIndex("test");
        debugPrint(idx);
        System.out.println("Node " + index + " 结束进行读测试...");
        Thread.sleep(3000L);
    }

    private static void debugPrint(Index index) {
        try {
            Cursor namesCursor = index.newCursor(null);
            try {
                namesCursor.first();
                byte[] key;
                while ((key = namesCursor.key()) != null) {
                    byte[] value = namesCursor.value();
                    System.out.println("key = " + new String(key));
                    if (value != null && value.length > 0) {
                        System.out.println("value = " + new String(value));
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
