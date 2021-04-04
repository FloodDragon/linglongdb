package com.linglong.server.database.process;

import com.linglong.engine.event.EventListener;
import com.linglong.engine.event.EventType;
import com.linglong.engine.event.ReplicationEventListener;
import com.linglong.server.config.LinglongdbProperties;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

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
        linglongdbProperties.setBaseDir("D:\\workspace\\linglongdb\\");
        linglongdbProperties.setPageSize(4096);
        linglongdbProperties.setMinCacheSize(100000000L);
        linglongdbProperties.setMaxCacheSize(100000000L);
        linglongdbProperties.setLockTimeout(10 * 1000L);
        linglongdbProperties.setCheckpointRate(1000);
        linglongdbProperties.setDurabilityMode("SYNC");
        linglongdbProperties.setCheckpointSizeThreshold(1048576);
        linglongdbProperties.setCheckpointDelayThreshold(60000);
        linglongdbProperties.setMaxCheckpointThreads(8);
        linglongdbProperties.setReplicaEnabled(false);
        DatabaseProcessor processor = new DatabaseProcessor(linglongdbProperties, replicationEventListener);
        processor.afterPropertiesSet();
        final String indexName = "test";
        for (int j = 0; j < 1000; j++) {
            _Txn txn = processor.new OpenTxn().process(null);
            for (int i = 0; i < 5; i++) {
                processor.new KeyValueStore().process(processor.newOptions().txn(txn.txnId).indexName(indexName).key(toBytes(i)).value(String.valueOf(i).getBytes()));
                System.out.println("数据库测试 步骤0 写入" + i);
                Thread.sleep(100L);
            }
            System.out.println("数据库测试 步骤1 已写入完成.");

            processor.new IndexKeyValueScan().process(processor.newOptions().txn(txn.txnId).indexName(indexName).scanFunc(new Function<Map.Entry<byte[], byte[]>, Boolean>() {
                @Override
                public Boolean apply(Map.Entry<byte[], byte[]> entry) {
                    System.out.println("entry: " + toLong(entry.getKey()) + " " + new String(entry.getValue()));
                    return true;
                }
            }));
            System.out.println("数据库测试 步骤2 已扫描完成.");
            processor.new IndexDelete().process(new _IndexName().indexName(indexName));
            System.out.println("数据库测试 步骤3 已删除索引(" + indexName + ")完成");
            _Options options = processor.newOptions().indexName(indexName);
            processor.new IndexCount().process(options);
            System.out.println("数据库测试 步骤4 索引数据长度大小(" + options.count + ")");
            processor.new TxnCommitOrRollback().process(txn.commit());
        }
        processor.destroy();
    }

    public static byte[] toBytes(long num) {
        byte buf[] = new byte[8];
        buf[0] = (byte) (num >>> 56);
        buf[1] = (byte) (num >>> 48);
        buf[2] = (byte) (num >>> 40);
        buf[3] = (byte) (num >>> 32);
        buf[4] = (byte) (num >>> 24);
        buf[5] = (byte) (num >>> 16);
        buf[6] = (byte) (num >>> 8);
        buf[7] = (byte) (num >>> 0);
        return buf;
    }

    public static long toLong(byte[] bytes) {
        return (((long) bytes[0] << 56) +
                ((long) (bytes[1] & 255) << 48) +
                ((long) (bytes[2] & 255) << 40) +
                ((long) (bytes[3] & 255) << 32) +
                ((long) (bytes[4] & 255) << 24) +
                ((bytes[5] & 255) << 16) +
                ((bytes[6] & 255) << 8) +
                ((bytes[7] & 255) << 0));
    }
}
