package com.glodon.linglong.server.http.tomcat;

import com.glodon.linglong.server.http.*;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 嵌入式tomcat http
 * <p>
 * Created by liuj-ai on 2019/8/7.
 */
public class TomcatHttpServer extends AbstractHttpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TomcatHttpServer.class);
    private final Tomcat tomcat;
    private ServerConfig serverConfig;

    public TomcatHttpServer(ServerConfig config, HttpHandler handler) {
        super(config, handler);
        this.serverConfig = config;
        DispatcherServlet.addHttpHandler(serverConfig.getPort(), handler);
        String baseDir = new File(ServerConfig.SYS_TMP_DIR).getAbsolutePath();
        tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir);
        tomcat.setPort(config.getPort());
        tomcat.getConnector().setProperty(
                "maxThreads", String.valueOf(serverConfig.getMaxThreads()));

        /*tomcat.getConnector().setProperty("minSpareThreads", String.valueOf(serverConfig.getMinSpareThreads()));*/

        tomcat.getConnector().setProperty(
                "maxConnections", String.valueOf(serverConfig.getMaxConnections()));

        tomcat.getConnector().setProperty("URIEncoding", "UTF-8");
        tomcat.getConnector().setProperty("connectionTimeout", "180000");

        tomcat.getConnector().setProperty("maxKeepAliveRequests", "-1");
        tomcat.getConnector().setProtocol("org.apache.coyote.http11.Http11NioProtocol");

        Context context = tomcat.addContext("/", baseDir);
        Tomcat.addServlet(context, "dispatcher", new DispatcherServlet());
        context.addServletMapping("/*", "dispatcher");
        ServletManager.getInstance().addServletContext(serverConfig.getPort(), context.getServletContext());

        System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new IllegalStateException("Failed to start tomcat server at " + serverConfig.getAddress(), e);
        }
    }

    @Override
    public void close() {
        super.close();
        ServletManager.getInstance().removeServletContext(serverConfig.getPort());
        try {
            tomcat.stop();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
