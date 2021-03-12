package com.linglong.server.http.restful.api;

import com.linglong.server.http.HttpHandler;

/**
 * @author Stereo on 2019/7/31.
 */
public interface RestActionApi extends HttpHandler, RestActionHandler {
    String REST_API_ENCODING = "UTF-8";
    String REST_API_CONTENT_TYPE = "application/json;charset=UTF-8";

    enum ProxyMapping {
        STATE("/state", "state", "GET"),
        PING("/ping", "ping", "GET"),
        QUERY_GET("/query", "query", "GET"),
        QUERY_POST("/query", "query", "POST"),
        WRITE_POST("/write", "write", "POST");

        String path;
        String httpMethod;
        String method;

        ProxyMapping(String path, String method, String httpMethod) {
            this.path = path;
            this.method = method;
            this.httpMethod = httpMethod;
        }

        public static ProxyMapping match(String path, String httpMethod) {
            ProxyMapping[] proxyMappings = ProxyMapping.values();
            for (ProxyMapping proxyMapping : proxyMappings) {
                if (proxyMapping.path.equals(path) && proxyMapping.httpMethod.equals(httpMethod)) {
                    return proxyMapping;
                }
            }
            return null;
        }
    }
}
