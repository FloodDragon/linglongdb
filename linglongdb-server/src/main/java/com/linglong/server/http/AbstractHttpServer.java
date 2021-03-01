package com.linglong.server.http;


import java.net.InetSocketAddress;

/**
 * Created by liuj-ai on 2019/8/7.
 */
public abstract class AbstractHttpServer implements HttpServer {

    private final ServerConfig config;

    private final HttpHandler handler;

    private volatile boolean closed;

    public AbstractHttpServer(ServerConfig config, HttpHandler handler) {
        if (config == null) {
            throw new IllegalArgumentException("config == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.config = config;
        this.handler = handler;
    }

    @Override
    public HttpHandler getHttpHandler() {
        return handler;
    }

    @Override
    public ServerConfig getConfig() {
        return config;
    }

    @Override
    public boolean isBound() {
        return true;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return config.toInetSocketAddress();
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public void close(int timeout) {
        close();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

}
