package com.glodon.linglong.server.http.tomcat;


import com.glodon.linglong.server.http.HttpBinder;
import com.glodon.linglong.server.http.HttpHandler;
import com.glodon.linglong.server.http.HttpServer;
import com.glodon.linglong.server.http.ServerConfig;

/**
 * 嵌入式tomcat http 绑定
 * <p>
 * Created by liuj-ai on 2019/8/7.
 */
public class TomcatHttpBinder implements HttpBinder {

    @Override
    public HttpServer bind(ServerConfig serverConfig, HttpHandler handler) {
        return new TomcatHttpServer(serverConfig, handler);
    }
}
