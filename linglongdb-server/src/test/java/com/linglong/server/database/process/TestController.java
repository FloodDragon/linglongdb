package com.linglong.server.database.process;

import com.linglong.protocol.IndexProtocol;
import com.linglong.protocol.KeyValueProtocol;
import com.linglong.protocol.TransactionProtocol;
import com.linglong.protocol.message.*;
import com.linglong.rpc.client.ClientProxy;
import com.linglong.rpc.client.DataStream;
import com.linglong.rpc.client.DataStreamExecutor;
import com.linglong.rpc.client.DataStreamHandler;
import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.common.utils.UUID;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public class TestController {

    public static void main(String[] args) throws InterruptedException {
        //客户端开启
        ClientProxy clientProxy = new ClientProxy(new Config("0.0.0.0", 7002));
        clientProxy.start();

        //数据库创建协议
        IndexProtocol indexProtocol = clientProxy.create(IndexProtocol.class);
        KeyValueProtocol keyValueProtocol = clientProxy.create(KeyValueProtocol.class);
        TransactionProtocol transactionProtocol = clientProxy.create(TransactionProtocol.class);
        DataStream<KeyValueProtocol> dataStream = clientProxy.createDataStream(KeyValueProtocol.class);

        //执行数据库操作
        ExecutorService service = Executors.newFixedThreadPool(5);
        for (int k = 0; k < 2; k++) {
            final String indexName = "index-test-" + k;
            service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int j = 0; j < 10000; j++) {
                            Response txnResponse = transactionProtocol.openTxn();
                            for (int i = 0; i < 10000; i++) {
                                try {
                                    long num = ThreadLocalRandom.current().nextLong();
                                    KeyValueRequest insertKeyValue = new KeyValueRequest();
                                    insertKeyValue.setId(new UUID(indexName).toString());
                                    insertKeyValue.setIndexName(indexName);
                                    insertKeyValue.setXid(txnResponse.getXid());
                                    insertKeyValue.setKey(DatabaseProcessorTest.toBytes(num));
                                    insertKeyValue.setValue(String.valueOf(num).getBytes());
                                    KeyValueResponse keyValueResponse = keyValueProtocol.insert(insertKeyValue);
                                    System.out.println("数据库测试 步骤0 写入" + i + (keyValueResponse.isSuccessful() ? "成功" : "失败"));
                                    //Thread.sleep(1000L);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    System.out.println("数据库测试 步骤0 写入" + i + "失败");
                                }
                            }
                            System.out.println("数据库测试 步骤1 已写入完成.");

                            KeyLowHighRequest keyLowHighRequest = new KeyLowHighRequest();
                            keyLowHighRequest.setId(new UUID(indexName).toString());
                            keyLowHighRequest.setIndexName(indexName);
                            keyLowHighRequest.setXid(txnResponse.getXid());
                            CountResponse countResponse = indexProtocol.count(keyLowHighRequest);
                            System.out.println("数据库测试 步骤2 获取索引数据长度大小(" + countResponse.getCount() + ")");

                            dataStream.call(new DataStreamExecutor<KeyValueProtocol>() {
                                @Override
                                public void execute(KeyValueProtocol keyValueProtocol) {
                                    IndexRequest indexRequest = new IndexRequest();
                                    indexRequest.setId(new UUID(indexName).toString());
                                    indexRequest.setIndexName(indexName);
                                    indexRequest.setXid(txnResponse.getXid());
                                    IndexScanResponse indexScanResponse = keyValueProtocol.scan(indexRequest);
                                    System.out.println("数据库测试 步骤3 扫描数据长度(" + indexScanResponse.getScanTotal() + ")");
                                }
                            }, new DataStreamHandler() {
                                @Override
                                public void handle(Object data) {
                                    if (data instanceof IndexScanItemResponse) {
                                        IndexScanItemResponse response = (IndexScanItemResponse) data;
                                        System.out.println("数据库测试 步骤3 扫描数据(key = " + DatabaseProcessorTest.toLong(response.getKey()) + "  value = " + new String(response.getValue()) + ")");
                                    }
                                }
                            });

                            IndexRequest indexRequest = new IndexRequest();
                            indexRequest.setId(new UUID(indexName).toString());
                            indexRequest.setIndexName(indexName);
                            IndexDeleteResponse deleteResponse = indexProtocol.delete(indexRequest);
                            System.out.println("数据库测试 步骤4 删除索引" + (deleteResponse.isDeleted() ? "成功" : "失败"));

                            countResponse = indexProtocol.count(keyLowHighRequest);
                            System.out.println("数据库测试 步骤5 获取索引数据长度大小(" + countResponse.getCount() + ")");

                            TxnRequest txnRequest = new TxnRequest();
                            txnRequest.setId(new UUID(indexName).toString());
                            txnRequest.setTxnId(txnResponse.getXid());
                            TxnCommitResponse txnCommitResponse = transactionProtocol.commitTxn(txnRequest);

                            System.out.println("数据库测试 步骤6 提交操作" + (txnCommitResponse.isCommited() ? "成功" : "失败"));
                            System.out.println("数据库测试 步骤7 第(" + j + "次)测试");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        service.shutdown();
        while (!service.awaitTermination(3000L, TimeUnit.MILLISECONDS)) ;
        //客户端关闭
        clientProxy.stop();
    }
}
