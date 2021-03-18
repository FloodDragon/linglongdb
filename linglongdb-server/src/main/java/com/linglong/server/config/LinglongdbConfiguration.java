package com.linglong.server.config;

import com.linglong.engine.config.DatabaseConfig;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.LocalDatabase;
import com.linglong.engine.core.frame.Database;
import com.linglong.replication.DatabaseReplicator;
import com.linglong.replication.Role;
import com.linglong.replication.confg.ReplicatorConfig;
import com.linglong.server.utils.MixAll;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author Stereo on 2021/3/17.
 */
@Configuration
@EnableConfigurationProperties(LinglongdbProperties.class)
public class LinglongdbConfiguration {

    private final static String LINGLONGDB_DATA = "data";

    @Bean(destroyMethod = "close")
    public Database database(LinglongdbProperties linglongdbProperties) throws IOException {
        File file = new File(linglongdbProperties.getBaseDir());
        if (file.isFile()) {
            throw new IllegalArgumentException("linglongdb base dir must be directory: " + file.getAbsolutePath());
        } else {
            file = new File(file, LINGLONGDB_DATA);
        }
        DurabilityMode durabilityMode = DurabilityMode.getDurabilityMode(linglongdbProperties.getDurabilityMode());
        if (durabilityMode == null) {
            throw new IllegalArgumentException("linglongdb durability mode error");
        }
        if (!MixAll.checkPowerOfTwo(linglongdbProperties.getPageSize())) {
            throw new IllegalArgumentException("linglongdb page size must be greater than zero and power of 2");
        }
        if (linglongdbProperties.getLockTimeout() <= 0) {
            throw new IllegalArgumentException("linglongdb lock timeout must be greater than zero");
        }
        if (!MixAll.checkPowerOfTwo(linglongdbProperties.getCheckpointSizeThreshold())) {
            throw new IllegalArgumentException("linglongdb checkpoint size threshold must be greater than zero and power of 2");
        }
        if (linglongdbProperties.getCheckpointDelayThreshold() <= 0) {
            throw new IllegalArgumentException("linglongdb checkpoint delay threshold must be greater than zero");
        }
        if (linglongdbProperties.getMaxCheckpointThreads() <= 0) {
            throw new IllegalArgumentException("linglongdb checkpoint max threads must be greater than zero");
        }
        if (linglongdbProperties.getMinCacheSize() <= 0) {
            throw new IllegalArgumentException("linglongdb min cache size must be greater than zero");
        }
        if (linglongdbProperties.getMaxCacheSize() <= 0) {
            throw new IllegalArgumentException("linglongdb max cache size must be greater than zero");
        }
        Role role = Role.getRole(linglongdbProperties.getReplicaRole());
        if (role == null) {
            throw new IllegalArgumentException("linglongdb replica role error");
        }

        //创建数据库配置
        DatabaseConfig config = new DatabaseConfig()
                .baseFile(file)
                .pageSize(linglongdbProperties.getPageSize())
                .lockTimeout(linglongdbProperties.getLockTimeout(), TimeUnit.MILLISECONDS)
                .checkpointRate(linglongdbProperties.getCheckpointRate(), TimeUnit.MILLISECONDS)
                .checkpointSizeThreshold(linglongdbProperties.getCheckpointSizeThreshold())
                .checkpointDelayThreshold(linglongdbProperties.getCheckpointDelayThreshold(), TimeUnit.MILLISECONDS)
                .maxCheckpointThreads(linglongdbProperties.getMaxCheckpointThreads())
                .minCacheSize(linglongdbProperties.getMinCacheSize())
                .maxCacheSize(linglongdbProperties.getMaxCacheSize())
                .durabilityMode(durabilityMode);
        if (linglongdbProperties.isReplicaEnabled()) {
            //打开数据库复制
            ReplicatorConfig replicatorConfig = new ReplicatorConfig()
                    .groupToken(linglongdbProperties.getReplicaGroupToken())
                    .localPort(linglongdbProperties.getReplicaPort())
                    .localRole(role)
                    .baseFile(file);

            //设置集群复制发现地址
            if (!CollectionUtils.isEmpty(linglongdbProperties.getReplicaSeedAddresses())) {
                for (String replicaSeedAddress : linglongdbProperties.getReplicaSeedAddresses()) {
                    replicatorConfig.addSeed(replicaSeedAddress);
                }
            }
            //开启集群复制器
            DatabaseReplicator databaseReplicator = DatabaseReplicator.open(replicatorConfig);
            config.replicate(databaseReplicator);
        }
        return Database.open(config);
    }
}
