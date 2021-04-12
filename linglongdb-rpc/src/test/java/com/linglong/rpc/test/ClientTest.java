package com.linglong.rpc.test;

import com.linglong.rpc.client.*;
import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.test.protocol.Test;
import com.linglong.rpc.test.protocol.TestService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        //测试RPC访问
        //test_1();
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
                                Thread.sleep(1000000000L);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            });
        }
    }

    private final static void test_2() {
        Config config = new Config();
        //config.setReadTimeout(1000);
        ClientProxy clientProxy = new ClientProxy(config);
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
        for (int i = 0; i < 1; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            dataStream.call((testService) -> {
                                //执行调用接口
                                Test rt = testService.test(test);
                                System.out.println("数据流测试1结束 ---------------------> code = " + rt.getCode() + " msg = " + rt.getMsg());
                                rt = testService.test(test);
                                System.out.println("数据流测试2结束 ---------------------> code = " + rt.getCode() + " msg = " + rt.getMsg());

                            }, (data) -> System.out.println("===============>  数据流 " + data));
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            System.out.println("===============>  测试数据流线程执行结束  <===============");
                            try {
                                Thread.sleep(100000000L);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            });
        }
    }
}
