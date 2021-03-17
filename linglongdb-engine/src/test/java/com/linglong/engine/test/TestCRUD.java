package com.linglong.engine.test;

import com.linglong.engine.config.DatabaseConfig;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.frame.Database;
import com.linglong.engine.core.frame.Index;
import com.linglong.engine.core.tx.Transaction;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Stereo on 2021/2/2.
 */
public final class TestCRUD {

    private static final String tableName = "data";
    private static final String basePath = "C:\\Users\\liuj-ai\\Desktop\\数据库开发\\linglongdb-test\\";
    private static volatile long lastCount = 0;
    private static final long testTotal = 20000000L;
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
        long start = System.currentTimeMillis();
        System.out.println("数据库性能测试开始......");
        DatabaseConfig config = new DatabaseConfig()
                .baseFilePath(basePath + tableName)
                .minCacheSize(100_000_000)
                .durabilityMode(DurabilityMode.NO_FLUSH);
        //打开数据库
        Database db = Database.open(config);
        for (int i = 0; i < 5; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    String indexName = "linglongTest-" + index;
                    //开始事务
                    Transaction txn = null;
                    //构建索引
                    Index userIx = db.openIndex(indexName);
                    String msg = "Hi 大家好,我是玲珑数据库.";
                    byte[] key;
                    for (int j = 1; j <= testTotal; j++) {
                        if (txn == null) {
                            txn = db.newTransaction();
                        }
                        userIx.insert(txn, key = String.valueOf(indexName + "-" + j).getBytes(), msg.getBytes());
                        if (key != null) {
                            byte[] data = userIx.load(txn, key);
                            //System.out.println("indexName=" + indexName + " data=" + new String(data));
                        }
                        counter.incrementAndGet();

                        if (j % 10000 == 0) {
                            //事务提交 & 回滚
                            txn.commit();//txn.reset();
                            txn = null;
                        }
                    }
                    if (txn != null) {
                        txn.commit();
                    }
                    long count = userIx.count(null, null);
                    System.out.println(indexName + " 索引测试结束 总写入数量为 " + count);
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
        System.out.println("数据库性能测试10s后即将关闭,总耗时" + ((System.currentTimeMillis() - start) / 1000) + "s ......");
        Thread.sleep(1000 * 10L);
        scheduledExecutorService.shutdown();
    }
}
