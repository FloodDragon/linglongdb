package com.linglong.server.config;

import com.linglong.engine.event.ReplicationEventListener;
import com.linglong.server.database.process.DatabaseProcessor;
import com.linglong.server.logger.ReplicationLoggerListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author Stereo on 2021/3/17.
 */
@Configuration
@EnableConfigurationProperties(LinglongdbProperties.class)
public class LinglongdbConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ReplicationEventListener replicationLoggerListener(LinglongdbProperties linglongdbProperties) {
        return new ReplicationEventListener(new ReplicationLoggerListener(linglongdbProperties));
    }

    @Bean
    public DatabaseProcessor databaseManager(
            LinglongdbProperties linglongdbProperties,
            RpcServerProperties rpcServerProperties,
            ReplicationEventListener replicationEventListener) throws IOException {
        return new DatabaseProcessor(linglongdbProperties, rpcServerProperties, replicationEventListener);
    }
}
