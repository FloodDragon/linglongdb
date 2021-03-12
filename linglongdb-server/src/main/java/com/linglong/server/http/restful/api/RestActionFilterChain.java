package com.linglong.server.http.restful.api;


/**
 * @author Stereo on 2019/8/1.
 */
public interface RestActionFilterChain {

    void doFilter(RestAction op);

    void addFilter(RestActionFilter backendFilter);
}
