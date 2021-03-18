package com.linglong.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

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
    private boolean replicaEnabled;
    private int replicaPort;
    private String replicaRole;
    private long replicaGroupToken;
    private List<String> replicaSeedAddresses;

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

    public boolean isReplicaEnabled() {
        return replicaEnabled;
    }

    public void setReplicaEnabled(boolean replicaEnabled) {
        this.replicaEnabled = replicaEnabled;
    }

    public int getReplicaPort() {
        return replicaPort;
    }

    public void setReplicaPort(int replicaPort) {
        this.replicaPort = replicaPort;
    }

    public String getReplicaRole() {
        return replicaRole;
    }

    public void setReplicaRole(String replicaRole) {
        this.replicaRole = replicaRole;
    }

    public long getReplicaGroupToken() {
        return replicaGroupToken;
    }

    public void setReplicaGroupToken(long replicaGroupToken) {
        this.replicaGroupToken = replicaGroupToken;
    }

    public List<String> getReplicaSeedAddresses() {
        return replicaSeedAddresses;
    }

    public void setReplicaSeedAddresses(List<String> replicaSeedAddresses) {
        this.replicaSeedAddresses = replicaSeedAddresses;
    }
}
