package com.linglong.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author Stereo on 2021/3/12.
 */
@Validated
@ConfigurationProperties("linglongdb.http")
public class HttpServerProperties {

    private String serverHost;
    private int serverPort;
    private int maxThreads;
    private int minSpareThreads;
    private int maxConnections;
    private String handlerVersion;

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

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getMinSpareThreads() {
        return minSpareThreads;
    }

    public void setMinSpareThreads(int minSpareThreads) {
        this.minSpareThreads = minSpareThreads;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public String getHandlerVersion() {
        return handlerVersion;
    }

    public void setHandlerVersion(String handlerVersion) {
        this.handlerVersion = handlerVersion;
    }
}
