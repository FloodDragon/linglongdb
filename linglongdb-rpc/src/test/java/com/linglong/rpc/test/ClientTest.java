package com.linglong.rpc.test;

import com.linglong.rpc.client.Client;
import com.linglong.rpc.client.ClientProxy;
import com.linglong.rpc.client.DataStreamListener;
import com.linglong.rpc.client.FailoverHandler;
import com.linglong.rpc.test.protocol.Test;
import com.linglong.rpc.test.protocol.TestService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        ClientProxy clientProxy = new ClientProxy();
        clientProxy.setFailoverHandler(new FailoverHandler() {
            @Override
            public void failover(Client client) {
                System.out.println("服务器异常进行转移...");
            }
        });
        //开启
        clientProxy.start();
        //创建代理
        TestService testService = clientProxy.create(TestService.class, new DataStreamListener() {
            @Override
            public void streaming(Object data) {
                System.out.println("---------------------> 数据流  " + data);
            }
        });
        //创建参数
        final Test test = new Test();
        test.setId("id");
        test.getMap().put("test", 11L);
        test.getList().add("test");
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 1; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            //访问远程服务
                            Test rt = testService.test(test);
                            System.out.println("---------------------> code = " + rt.getCode() + " msg = " + rt.getMsg());
                            Thread.sleep(1000L);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            try {
                                Thread.sleep(1000L);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            });
        }
    }
}
