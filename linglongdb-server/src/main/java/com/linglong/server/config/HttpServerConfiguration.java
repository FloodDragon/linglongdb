package com.linglong.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


/**
 * @author Stereo on 2021/2/18.
 */
@Configuration
public class HttpServerConfiguration {

    @Value("${linglongdb.http.serverHost}")
    private String serverHost;
    @Value("${linglongdb.http.serverPort}")
    private int serverPort = 8080;
    @Value("${linglongdb.http.maxThreads}")
    private int maxThreads = 200;
    @Value("${linglongdb.http.minSpareThreads}")
    private int minSpareThreads = 200;
    @Value("${linglongdb.http.maxConnections}")
    private int maxConnections = -1;
    @Value("${linglongdb.http.version}")
    private String version = "v1";
}
