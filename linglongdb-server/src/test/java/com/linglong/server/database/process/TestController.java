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
import com.linglong.base.utils.UUID;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public class TestController {

    public static void main(String[] args) throws InterruptedException {
        if (args == null || args.length == 0) {
            System.out.println("请传入数据库服务器地址.");
            return;
        }
        //客户端开启
        ClientProxy clientProxy = new ClientProxy(new Config(args[0], 7002));
        clientProxy.start();

        //数据库创建协议
        IndexProtocol indexProtocol = clientProxy.create(IndexProtocol.class);
        KeyValueProtocol keyValueProtocol = clientProxy.create(KeyValueProtocol.class);
        TransactionProtocol transactionProtocol = clientProxy.create(TransactionProtocol.class);
        DataStream<KeyValueProtocol> dataStream = clientProxy.createDataStream(KeyValueProtocol.class);

        //执行数据库操作
        ExecutorService service = Executors.newFixedThreadPool(5);
        for (int k = 0; k < 1; k++) {
            final String indexName = "index-test-" + k;
            service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int j = 0; j < 1; j++) {
                            //打开事务
                            Response txnResponse = transactionProtocol.openTxn();
                            //写入数据
                            final long size = 10000;
                            for (int i = 0; i < size; i++) {
                                try {
                                    KeyValueRequest insertKeyValue = new KeyValueRequest();
                                    insertKeyValue.setId(new UUID(indexName).toString());
                                    insertKeyValue.setIndexName(indexName);
                                    insertKeyValue.setXid(txnResponse.getXid());
                                    insertKeyValue.setKey(DatabaseProcessorTest.toBytes(i));
                                    insertKeyValue.setValue(String.valueOf(i).getBytes());
                                    KeyValueResponse keyValueResponse = keyValueProtocol.insert(insertKeyValue);
                                    System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤0 写入" + i + (keyValueResponse.isSuccessful() ? "成功" : "失败"));
                                    //Thread.sleep(1000L);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤0 写入" + i + "失败");
                                }
                            }
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤1 已写入完成.");

                            //获取索引数据长度
                            KeyLowHighRequest keyLowHighRequest = new KeyLowHighRequest();
                            keyLowHighRequest.setId(new UUID(indexName).toString());
                            keyLowHighRequest.setIndexName(indexName);
                            keyLowHighRequest.setXid(txnResponse.getXid());
                            CountResponse countResponse = indexProtocol.count(keyLowHighRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤2 获取索引数据长度大小(" + countResponse.getCount() + ")");

                            //全索引扫描数据
                            dataStream.call(new DataStreamExecutor<KeyValueProtocol>() {
                                @Override
                                public void execute(KeyValueProtocol keyValueProtocol) {
                                    IndexRequest indexRequest = new IndexRequest();
                                    indexRequest.setId(new UUID(indexName).toString());
                                    indexRequest.setIndexName(indexName);
                                    indexRequest.setXid(txnResponse.getXid());
                                    IndexScanResponse indexScanResponse = keyValueProtocol.scan(indexRequest);
                                    System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤3 扫描数据长度(" + indexScanResponse.getScanTotal() + ")");
                                }
                            }, new DataStreamHandler() {
                                @Override
                                public void handle(Object data) {
                                    if (data instanceof IndexScanItemResponse) {
                                        IndexScanItemResponse response = (IndexScanItemResponse) data;
                                        System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤3 扫描数据(key = " + DatabaseProcessorTest.toLong(response.getKey()) + "  value = " + new String(response.getValue()) + ")");
                                    }
                                }
                            });

                            byte[] key = DatabaseProcessorTest.toBytes((size - 1));
                            //检查在最后一个Key是否存
                            KeyValueRequest keyValueRequest = new KeyValueRequest();
                            keyValueRequest.setId(new UUID(indexName).toString());
                            keyValueRequest.setIndexName(indexName);
                            keyValueRequest.setXid(txnResponse.getXid());
                            keyValueRequest.setKey(key);
                            ExistsResponse existsResponse = keyValueProtocol.exists(keyValueRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤4 判断Key=" + (size - 1) + "是否存在, 结果: " + (existsResponse.isExists() ? "存在" : "不存在"));

                            //索引替换Value
                            final byte[] value = "Hi 我是玲珑数据库.".getBytes();
                            keyValueRequest = new KeyValueRequest();
                            keyValueRequest.setId(new UUID(indexName).toString());
                            keyValueRequest.setIndexName(indexName);
                            keyValueRequest.setXid(txnResponse.getXid());
                            keyValueRequest.setKey(key);
                            keyValueRequest.setValue(value);
                            KeyValueResponse keyValueResponse = keyValueProtocol.replace(keyValueRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤5 替换Value数据" + (keyValueResponse.isSuccessful() ? "替换成功" : "替换失败"));

                            //获取替换后的Value
                            keyValueRequest = new KeyValueRequest();
                            keyValueRequest.setId(new UUID(indexName).toString());
                            keyValueRequest.setIndexName(indexName);
                            keyValueRequest.setXid(txnResponse.getXid());
                            keyValueRequest.setKey(DatabaseProcessorTest.toBytes((size - 1)));
                            keyValueResponse = keyValueProtocol.load(keyValueRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤6 获取替换Value后数据 " + (keyValueResponse.isSuccessful() ? new String(keyValueResponse.getValue()) : "获取失败"));

                            //索引更新Value
                            final byte[] newValue = "Hi 我是玲珑数据库, 我正在努力研发中...".getBytes();
                            keyValueRequest = new KeyValueRequest();
                            keyValueRequest.setId(new UUID(indexName).toString());
                            keyValueRequest.setIndexName(indexName);
                            keyValueRequest.setXid(txnResponse.getXid());
                            keyValueRequest.setKey(key);
                            keyValueRequest.setValue(newValue);
                            keyValueRequest.setOldValue(value);
                            keyValueResponse = keyValueProtocol.update(keyValueRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤7 更新Value数据" + (keyValueResponse.isSuccessful() ? "更新成功" : "更新失败"));

                            //获取更新后的KeyValue
                            keyValueRequest = new KeyValueRequest();
                            keyValueRequest.setId(new UUID(indexName).toString());
                            keyValueRequest.setIndexName(indexName);
                            keyValueRequest.setXid(txnResponse.getXid());
                            keyValueRequest.setKey(key);
                            keyValueResponse = keyValueProtocol.load(keyValueRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤8 获取更新Value后数据 " + (keyValueResponse.isSuccessful() ? new String(keyValueResponse.getValue()) : "获取失败"));

                            //进行交换KeyValue
                            final byte[] newValue1 = "Hi 我是玲珑数据库, 我正在努力研发中...加油...奥利给...".getBytes();
                            keyValueRequest = new KeyValueRequest();
                            keyValueRequest.setId(new UUID(indexName).toString());
                            keyValueRequest.setIndexName(indexName);
                            keyValueRequest.setXid(txnResponse.getXid());
                            keyValueRequest.setKey(key);
                            keyValueRequest.setValue(newValue1);
                            keyValueResponse = keyValueProtocol.exchange(keyValueRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤9 交换Value数据 " + (keyValueResponse.isSuccessful() ? new String(keyValueResponse.getValue()) : "交换失败"));

                            //加载交换后的KeyValue
                            keyValueRequest = new KeyValueRequest();
                            keyValueRequest.setId(new UUID(indexName).toString());
                            keyValueRequest.setIndexName(indexName);
                            keyValueRequest.setXid(txnResponse.getXid());
                            keyValueRequest.setKey(key);
                            keyValueResponse = keyValueProtocol.load(keyValueRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤10 获取交换Value后数据 " + (keyValueResponse.isSuccessful() ? new String(keyValueResponse.getValue()) : "获取失败"));

                            //删除KeyValue
                            keyValueRequest = new KeyValueRequest();
                            keyValueRequest.setId(new UUID(indexName).toString());
                            keyValueRequest.setIndexName(indexName);
                            keyValueRequest.setXid(txnResponse.getXid());
                            keyValueRequest.setKey(key);
                            keyValueResponse = keyValueProtocol.delete(keyValueRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤11 删除Value后数据 " + (keyValueResponse.isSuccessful() ? "删除成功" : "删除失败"));

                            //获取删除后的KeyValue
                            keyValueRequest = new KeyValueRequest();
                            keyValueRequest.setId(new UUID(indexName).toString());
                            keyValueRequest.setIndexName(indexName);
                            keyValueRequest.setXid(txnResponse.getXid());
                            keyValueRequest.setKey(key);
                            keyValueResponse = keyValueProtocol.load(keyValueRequest);
                            existsResponse = keyValueProtocol.exists(keyValueRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤12 " + (existsResponse.isExists() ? "存在" : "不存在") + ", 获取删除后数据 " + (keyValueResponse.isSuccessful() ? (keyValueResponse.getValue() != null ? new String(keyValueResponse.getValue()) : "NULL") : "获取失败"));

                            //更新索引名称
                            String newIndexName = "new-" + indexName;
                            IndexRenameRequest indexRenameRequest = new IndexRenameRequest();
                            indexRenameRequest.setId(new UUID(newIndexName).toString());
                            indexRenameRequest.setIndexName(indexName);
                            indexRenameRequest.setNewName(newIndexName);
                            IndexRenameResponse indexRenameResponse = indexProtocol.rename(indexRenameRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤13 更新索引名 " + (indexRenameResponse.isRenamed() ? "更新成功" : "更新失败") + " 新名称 => " + indexRenameResponse.getNewName());

                            //删除索引
                            IndexRequest indexRequest = new IndexRequest();
                            indexRequest.setId(new UUID(newIndexName).toString());
                            indexRequest.setIndexName(newIndexName);
                            IndexDeleteResponse deleteResponse = indexProtocol.delete(indexRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤14 删除索引" + (deleteResponse.isDeleted() ? "成功" : "失败"));

                            //获取旧索引数据长度
                            countResponse = indexProtocol.count(keyLowHighRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤15 获取旧索引数据长度大小(" + countResponse.getCount() + ")");

                            //获取新索引数据长度
                            keyLowHighRequest.setIndexName(newIndexName);
                            countResponse = indexProtocol.count(keyLowHighRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤16 获取新索引数据长度大小(" + countResponse.getCount() + ")");

                            //提交事务
                            TxnRequest txnRequest = new TxnRequest();
                            txnRequest.setId(new UUID(indexName).toString());
                            txnRequest.setTxnId(txnResponse.getXid());
                            TxnCommitResponse txnCommitResponse = transactionProtocol.commitTxn(txnRequest);
                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤17 提交操作" + (txnCommitResponse.isCommited() ? "成功" : "失败"));

                            System.out.println(Thread.currentThread().getName() + " 数据库测试 步骤18 第" + j + "次测试完成");
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
