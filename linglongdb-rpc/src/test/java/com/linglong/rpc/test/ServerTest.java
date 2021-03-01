package com.linglong.rpc.test;


import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.server.RpcServiceServer;
import com.linglong.rpc.test.protocol.TestService;

/**
 * Created by liuj-ai on 2020/4/27.
 */
public class ServerTest {

    public static void main(String[] args) {
        Config config = new Config();
        TestService testService = new TestServiceImpl(TestService.class);
        RpcServiceServer rpcServiceServer = new RpcServiceServer(config);
        rpcServiceServer.getRpcRegistry().registerService(testService);
        rpcServiceServer.start();
    }
}
