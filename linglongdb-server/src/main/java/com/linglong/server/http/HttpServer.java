package com.linglong.server.http;

import java.net.InetSocketAddress;

/**
 * @author Stereo on 2019/8/7.
 */
public interface HttpServer {

    HttpHandler getHttpHandler();

    ServerConfig getConfig();

    InetSocketAddress getLocalAddress();

    void close();

    void close(int timeout);

    boolean isBound();

    boolean isClosed();
}