package com.linglong.server.http;


/**
 * @author Stereo on 2019/8/7.
 */
public interface HttpBinder {

    HttpServer bind(ServerConfig serverConfig, HttpHandler handler);

}