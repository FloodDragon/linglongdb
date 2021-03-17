package com.linglong.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author Stereo on 2021/3/17.
 */
@Validated
@ConfigurationProperties("linglongdb")
public class LinglongdbProperties {

    private String baseDir;
    private int pageSize;
    private long lockTimeout;
    private long minCacheSize;
    private long maxCacheSize;
    private long checkpointRate;
    private String durabilityMode;
    private long checkpointSizeThreshold;
    private long checkpointDelayThreshold;
    private int maxCheckpointThreads;

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getLockTimeout() {
        return lockTimeout;
    }

    public void setLockTimeout(long lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    public long getCheckpointRate() {
        return checkpointRate;
    }

    public void setCheckpointRate(long checkpointRate) {
        this.checkpointRate = checkpointRate;
    }

    public String getDurabilityMode() {
        return durabilityMode;
    }

    public void setDurabilityMode(String durabilityMode) {
        this.durabilityMode = durabilityMode;
    }

    public long getCheckpointSizeThreshold() {
        return checkpointSizeThreshold;
    }

    public void setCheckpointSizeThreshold(long checkpointSizeThreshold) {
        this.checkpointSizeThreshold = checkpointSizeThreshold;
    }

    public int getMaxCheckpointThreads() {
        return maxCheckpointThreads;
    }

    public void setMaxCheckpointThreads(int maxCheckpointThreads) {
        this.maxCheckpointThreads = maxCheckpointThreads;
    }

    public long getMinCacheSize() {
        return minCacheSize;
    }

    public void setMinCacheSize(long minCacheSize) {
        this.minCacheSize = minCacheSize;
    }

    public long getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(long maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public long getCheckpointDelayThreshold() {
        return checkpointDelayThreshold;
    }

    public void setCheckpointDelayThreshold(long checkpointDelayThreshold) {
        this.checkpointDelayThreshold = checkpointDelayThreshold;
    }
}
