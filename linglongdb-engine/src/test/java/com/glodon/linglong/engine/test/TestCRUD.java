package com.glodon.linglong.engine.test;

import com.glodon.linglong.engine.config.DatabaseConfig;
import com.glodon.linglong.engine.config.DurabilityMode;
import com.glodon.linglong.engine.core.frame.Database;
import com.glodon.linglong.engine.core.frame.Index;
import com.glodon.linglong.engine.core.tx.Transaction;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by liuj-ai on 2021/2/2.
 */
public final class TestCRUD {

    private static volatile long lastCount = 0;
    private static final long testTotal = 1000000L;
    private static final AtomicLong counter = new AtomicLong(0);
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    static {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (lastCount == 0) {
                lastCount = counter.get();
            } else {
                long count = counter.get();
                System.out.println("当前已写入数据总量 " + count + " ,每秒读写 " + (count - lastCount) + " 次/s");
                lastCount = count;
            }
        }, 0, 1000l, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("数据库性能测试开始......");
        DatabaseConfig config = new DatabaseConfig()
                .baseFilePath("C:\\Users\\liuj-ai\\Desktop\\数据库开发\\linglongdb\\data")
                .minCacheSize(100_000_000)
                .durabilityMode(DurabilityMode.SYNC);
        //打开数据库
        Database db = Database.open(config);
        for (int i = 0; i < 5; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    String indexName = "linglongTest-" + index;
                    //开始事务
                    Transaction txn = db.newTransaction();
                    //构建索引
                    Index userIx = db.openIndex(indexName);
                    String msg = "Hi 大家好,我是玲珑数据库";
                    byte[] key;
                    for (int j = 1; j <= testTotal; j++) {
                        userIx.insert(txn, key = String.valueOf(indexName + "-" + j).getBytes(), msg.getBytes());
                        if (key != null) {
                            byte[] data = userIx.load(txn, key);
                            //System.out.println("indexName=" + indexName + " data=" + new String(data));
                        }
                        counter.incrementAndGet();
                    }
                    //事务提交 & 回滚
                    txn.commit();//txn.reset();
                    //索引关闭
                    userIx.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
        executorService.shutdown();
        while (!executorService.awaitTermination(1000L, TimeUnit.MILLISECONDS)) ;
        //关闭数据库
        db.close();
        System.out.println("数据库性能测试10s后即将关闭......");
        Thread.sleep(1000 * 10L);
        scheduledExecutorService.shutdown();
    }
}
