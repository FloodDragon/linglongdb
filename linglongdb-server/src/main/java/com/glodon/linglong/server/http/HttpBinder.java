package com.glodon.linglong.server.http;


/**
 * Created by liuj-ai on 2019/8/7.
 */
public interface HttpBinder {

    HttpServer bind(ServerConfig serverConfig, HttpHandler handler);

}