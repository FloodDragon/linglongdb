package com.linglong.server.database.process;

import com.linglong.protocol.IndexProtocol;
import com.linglong.protocol.KeyValueProtocol;
import com.linglong.protocol.TransactionProtocol;
import com.linglong.protocol.message.*;
import com.linglong.rpc.client.ClientProxy;
import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.common.utils.UUID;

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

        //执行数据库操作
        final String indexName = "index-test";
        Response txnResponse = transactionProtocol.openTxn();
        for (int i = 0; i < 10; i++) {
            try {
                KeyValueRequest insertKeyValue = new KeyValueRequest();
                insertKeyValue.setId(new UUID(indexName).toString());
                insertKeyValue.setIndexName(indexName);
                insertKeyValue.setXid(txnResponse.getXid());
                insertKeyValue.setKey(DatabaseProcessorTest.toBytes(i));
                insertKeyValue.setValue(String.valueOf(i).getBytes());
                KeyValueResponse keyValueResponse = keyValueProtocol.insert(insertKeyValue);
                System.out.println("数据库测试 步骤0 写入" + i + (keyValueResponse.isSuccessful() ? "成功" : "失败"));
                //Thread.sleep(1000L);
            } catch (Exception ex) {
                System.out.println("数据库测试 步骤0 写入" + i + "失败");
            }
        }
        System.out.println("数据库测试 步骤1 已写入完成.");

//        KeyLowHighRequest keyLowHighRequest = new KeyLowHighRequest();
//        keyLowHighRequest.setId(new UUID(indexName).toString());
//        keyLowHighRequest.setIndexName(indexName);
//        keyLowHighRequest.setXid(txnResponse.getXid());
//        CountResponse countResponse = indexProtocol.count(keyLowHighRequest);
//        System.out.println("数据库测试 步骤2 获取索引数据长度大小(" + countResponse.getCount() + ")");

//        IndexRequest indexRequest = new IndexRequest();
//        indexRequest.setId(new UUID(indexName).toString());
//        indexRequest.setIndexName(indexName);
//        IndexDeleteResponse deleteResponse = indexProtocol.delete(indexRequest);
//        System.out.println("数据库测试 步骤3 删除索引" + (deleteResponse.isDeleted() ? "成功" : "失败"));

//        countResponse = indexProtocol.count(keyLowHighRequest);
//        System.out.println("数据库测试 步骤4 获取索引数据长度大小(" + countResponse.getCount() + ")");

        TxnRequest txnRequest = new TxnRequest();
        txnRequest.setId(new UUID(indexName).toString());
        txnRequest.setTxnId(txnResponse.getXid());
        TxnCommitResponse txnCommitResponse = transactionProtocol.commitTxn(txnRequest);

        System.out.println("数据库测试 步骤5 提交操作" + (txnCommitResponse.isCommited() ? "成功" : "失败"));

        //客户端关闭
        clientProxy.stop();
    }
}
