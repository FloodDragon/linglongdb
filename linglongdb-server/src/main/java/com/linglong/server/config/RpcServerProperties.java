package com.linglong.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Created by liuj-ai on 2021/3/22.
 */
@Validated
@ConfigurationProperties("linglongdb.rpc")
public class RpcServerProperties {

    private String serverHost;
    private int serverPort;
    private boolean useEpoll;
    private int payload;
    private int sendTimeout;
    private int readTimeout;
    private int connectTimeout;
    private int threadConcurrency;
    private int sendBufferSize;
    private int receiveBufferSize;
    private int heartBeatExpireInterval;

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public boolean isUseEpoll() {
        return useEpoll;
    }

    public void setUseEpoll(boolean useEpoll) {
        this.useEpoll = useEpoll;
    }

    public int getPayload() {
        return payload;
    }

    public void setPayload(int payload) {
        this.payload = payload;
    }

    public int getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getThreadConcurrency() {
        return threadConcurrency;
    }

    public void setThreadConcurrency(int threadConcurrency) {
        this.threadConcurrency = threadConcurrency;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public int getHeartBeatExpireInterval() {
        return heartBeatExpireInterval;
    }

    public void setHeartBeatExpireInterval(int heartBeatExpireInterval) {
        this.heartBeatExpireInterval = heartBeatExpireInterval;
    }
}
