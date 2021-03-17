package com.linglong.server.config;

import com.linglong.engine.config.DatabaseConfig;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.LocalDatabase;
import com.linglong.engine.core.frame.Database;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by liuj-ai on 2021/3/17.
 */
@Configuration
@EnableConfigurationProperties(LinglongdbProperties.class)
public class LinglongdbConfiguration {

    @Bean(destroyMethod = "close")
    public Database database(LinglongdbProperties linglongdbProperties) throws IOException {
        File file = new File(linglongdbProperties.getBaseDir());
        if (file.isFile()) {
            throw new IllegalArgumentException("linglongdb base dir must be directory: " + file.getAbsolutePath());
        }
        DurabilityMode durabilityMode = DurabilityMode.getDurabilityMode(linglongdbProperties.getDurabilityMode());
        if (durabilityMode == null) {
            throw new IllegalArgumentException("linglongdb durability mode error");
        }
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
        return Database.open(config);
    }
}
