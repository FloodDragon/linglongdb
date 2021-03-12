package com.linglong.server.http.restful.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Stereo on 2021/3/12.
 */
public abstract class RestAction {
    protected String u;
    protected String p;
    protected String shardingFactor;
    protected HttpServletRequest request;
    protected HttpServletResponse response;

    public RestAction() {
    }

    public String user() {
        return this.u;
    }

    public String password() {
        return this.p;
    }

    public String shardingFactor() {
        return this.shardingFactor;
    }

    public HttpServletRequest request() {
        return request;
    }

    public HttpServletResponse response() {
        return response;
    }
}
