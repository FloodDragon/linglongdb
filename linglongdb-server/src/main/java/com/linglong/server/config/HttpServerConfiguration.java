package com.linglong.server.config;

import com.linglong.server.http.restful.api.RestActionHandler;
import com.linglong.server.http.HttpServer;
import com.linglong.server.http.ServerConfig;
import com.linglong.server.http.restful.api.RestActionApi;
import com.linglong.server.http.restful.api.RestActionApiImpl;
import com.linglong.server.http.restful.v1.BackendV1Handler;
import com.linglong.server.http.tomcat.TomcatHttpBinder;
import com.linglong.server.utils.Daemon;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author Stereo on 2021/2/18.
 */
@Configuration
@EnableConfigurationProperties(HttpServerProperties.class)
public class HttpServerConfiguration {


    @Bean
    @ConditionalOnExpression("#{'v1'.equals(environment['linglongdb.http.handlerVersion'])}")
    public RestActionHandler backendHandler() {
        RestActionHandler backendHandler = new BackendV1Handler();
       //TODO
        return backendHandler;
    }

    @Bean
    public RestActionApi restActionApi(RestActionHandler backendHandler) {
        return new RestActionApiImpl(backendHandler);
    }

    @Bean(destroyMethod = "close")
    public HttpServer httpServer(RestActionApi restActionApi, HttpServerProperties httpServerProperties) {
        TomcatHttpBinder tomcatHttpBinder = new TomcatHttpBinder();
        return tomcatHttpBinder.bind(new ServerConfig(
                        httpServerProperties.getServerHost(),
                        httpServerProperties.getServerPort(),
                        httpServerProperties.getMaxThreads(),
                        httpServerProperties.getMinSpareThreads(),
                        httpServerProperties.getMaxConnections()),
                restActionApi);
    }

}
