package com.linglong.server.http;

import java.net.InetSocketAddress;

/**
 * Created by liuj-ai on 2019/8/7.
 */
public class ServerConfig {

    public static final String SYS_TMP_DIR = System.getProperty("java.io.tmpdir");

    private String host;
    private int port;
    private int maxThreads;
    private int minSpareThreads;
    private int maxConnections = -1;

    public ServerConfig(String host, int port, int maxThreads, int minSpareThreads, int maxConnections) {
        this.host = host;
        this.port = port;
        this.maxThreads = maxThreads;
        this.minSpareThreads = minSpareThreads;
        this.maxConnections = maxConnections;
    }

    public InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getMinSpareThreads() {
        return minSpareThreads;
    }

    public String getAddress() {
        return port <= 0 ? getHost() : getHost() + ":" + port;
    }
}