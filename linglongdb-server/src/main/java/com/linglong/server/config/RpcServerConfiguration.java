package com.linglong.server.config;

import com.linglong.protocol.IndexProtocol;
import com.linglong.protocol.TableProtocol;
import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.common.service.IService;
import com.linglong.rpc.server.RpcServiceServer;
import com.linglong.server.database.process.DatabaseProcessor;
import com.linglong.server.database.controller.TableControllerImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.linglong.server.database.controller.IndexControllerImpl;

import java.net.InetSocketAddress;

/**
 * Created by liuj-ai on 2021/3/22.
 */
@Configuration
@EnableConfigurationProperties(RpcServerProperties.class)
public class RpcServerConfiguration {

    @Bean
    public IService[] services(DatabaseProcessor databaseProcessor) {
        IndexProtocol indexProtocol = new IndexControllerImpl(databaseProcessor);
        TableProtocol tableProtocol = new TableControllerImpl(databaseProcessor);
        //TODO More
        return new IService[]{tableProtocol, indexProtocol};
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RpcServiceServer rpcServiceServer(RpcServerProperties rpcServerProperties, IService[] services) {
        Config rpcConfig = new Config(new InetSocketAddress(rpcServerProperties.getServerHost(), rpcServerProperties.getServerPort()));
        rpcConfig.setUseEpoll(rpcServerProperties.isUseEpoll());
        rpcConfig.setPayload(rpcServerProperties.getPayload());
        rpcConfig.setSendTimeout(rpcServerProperties.getSendTimeout());
        rpcConfig.setReadTimeout(rpcServerProperties.getReadTimeout());
        rpcConfig.setConnectTimeout(rpcServerProperties.getConnectTimeout());
        rpcConfig.setBusinessPoolQueueSize(rpcServerProperties.getThreadConcurrency());
        rpcConfig.setSendBufferSize(rpcServerProperties.getSendBufferSize());
        rpcConfig.setReceiveBufferSize(rpcServerProperties.getReceiveBufferSize());
        rpcConfig.setHeartBeatExpireInterval(rpcServerProperties.getHeartBeatExpireInterval());
        RpcServiceServer rpcServiceServer = new RpcServiceServer(rpcConfig);
        if (services != null && services.length > 0) {
            for (IService service : services) {
                rpcServiceServer.getRpcRegistry().registerService(service);
            }
        }
        return rpcServiceServer;
    }
}
