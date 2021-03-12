package com.linglong.server.http.restful.api;

import com.linglong.server.http.restful.dto.Pong;
import com.linglong.server.http.restful.dto.QueryResult;
import com.linglong.server.http.restful.dto.State;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Stereo on 2021/3/12.
 */
public interface RestActionHandler {

    State state(HttpServletRequest request, HttpServletResponse response);

    Pong ping(HttpServletRequest request, HttpServletResponse response);

    QueryResult query(HttpServletRequest request, HttpServletResponse response);

    void write(HttpServletRequest request, HttpServletResponse response);
}
