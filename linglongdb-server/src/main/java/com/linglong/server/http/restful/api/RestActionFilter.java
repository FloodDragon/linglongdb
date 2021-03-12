package com.linglong.server.http.restful.api;

/**
 * @author Stereo on 2019/8/1.
 */
public interface RestActionFilter {

    void doFilter(RestAction op, RestActionFilterChain filterChain);
}
