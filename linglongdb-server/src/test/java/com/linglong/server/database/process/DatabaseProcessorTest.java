package com.linglong.server.database.process;

import com.linglong.engine.event.EventListener;
import com.linglong.engine.event.EventType;
import com.linglong.engine.event.ReplicationEventListener;
import com.linglong.server.config.LinglongdbProperties;

/**
 * Created by liuj-ai on 2021/3/23.
 */
public class DatabaseProcessorTest {
    /**
     * 局部测试
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        ReplicationEventListener replicationEventListener = new ReplicationEventListener(new EventListener() {
            @Override
            public void notify(EventType type, String message, Object... args) {
                System.out.println("测试数据 DatabaseProcessor type=" + type + "  mesg=" + message);
            }
        });

        LinglongdbProperties linglongdbProperties = new LinglongdbProperties();
        linglongdbProperties.setBaseDir("C:\\Users\\liuj-ai\\Desktop\\数据库开发\\node-0");
        linglongdbProperties.setPageSize(4096);
        linglongdbProperties.setMinCacheSize(100000000L);
        linglongdbProperties.setMaxCacheSize(100000000L);
        linglongdbProperties.setLockTimeout(1000);
        linglongdbProperties.setCheckpointRate(1000);
        linglongdbProperties.setDurabilityMode("SYNC");
        linglongdbProperties.setCheckpointSizeThreshold(1048576);
        linglongdbProperties.setCheckpointDelayThreshold(60000);
        linglongdbProperties.setMaxCheckpointThreads(8);
        linglongdbProperties.setReplicaEnabled(false);
        DatabaseProcessor processor = new DatabaseProcessor(linglongdbProperties, replicationEventListener);
        processor.afterPropertiesSet();
        final String indexName = "test";
        DatabaseProcessor._Txn txn = processor.new OpenTxn().process(null);
        for (int i = 0; i < 10; i++) {
            processor.new KeyValueInsert().process(processor.newOptions().txn(txn.txnId).indexName(indexName).key(String.valueOf(i).getBytes()).value(String.valueOf(i).getBytes()));
            System.out.println("数据库测试 步骤1 已写入 " + String.valueOf(i));
            processor.new IndexDelete().process(indexName);
            System.out.println("数据库测试 步骤2 删除索引(" + indexName + ")完成");
            DatabaseProcessor._Options options = processor.newOptions().indexName(indexName);
            processor.new IndexCount().process(options);
            System.out.println("数据库测试 步骤3 索引数据大小(" + options.count + ")");
            Thread.sleep(1000L);
        }
        processor.new TxnCommitOrRollback().process(txn.commit());
        processor.destroy();
    }
}
