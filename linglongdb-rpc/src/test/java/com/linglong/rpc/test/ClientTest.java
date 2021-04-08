package com.linglong.rpc.test;

import com.linglong.rpc.client.Client;
import com.linglong.rpc.client.ClientProxy;
import com.linglong.rpc.client.FailoverHandler;
import com.linglong.rpc.client.ds.DataStream;
import com.linglong.rpc.client.ds.DataStreamExecutor;
import com.linglong.rpc.client.ds.DataStreamHandler;
import com.linglong.rpc.test.protocol.Test;
import com.linglong.rpc.test.protocol.TestService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        //测试RPC访问
        test_1();
        //测试RPC数据流访问
        test_2();
    }


    private final static void test_1() {
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
        TestService testService = clientProxy.create(TestService.class);
        //创建参数
        final Test test = new Test();
        test.setId("id");
        test.getMap().put("test", 11L);
        test.getList().add("test");
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
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

    private final static void test_2() {
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
        DataStream<TestService> dataStream = clientProxy.createDataStream(TestService.class);
        //创建参数
        final Test test = new Test();
        test.setId("id");
        test.getMap().put("test", 11L);
        test.getList().add("test");
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        dataStream.call(new DataStreamExecutor<TestService>() {
                            @Override
                            public void execute(TestService testService) {
                                //执行调用接口
                                Test rt = testService.test(test);
                                System.out.println("数据流测试1结束 ---------------------> code = " + rt.getCode() + " msg = " + rt.getMsg());
                                rt = testService.test(test);
                                System.out.println("数据流测试2结束 ---------------------> code = " + rt.getCode() + " msg = " + rt.getMsg());
                            }
                        }, new DataStreamHandler() {
                            @Override
                            public void handle(Object data) {
                                //数据流
                                System.out.println("===============>  数据流 " + data);
                            }
                        });
                        System.out.println("===============>  数据流测试执行结束  <===============");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            });
        }
    }
}
