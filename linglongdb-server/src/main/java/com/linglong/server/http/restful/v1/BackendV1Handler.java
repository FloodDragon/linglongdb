package com.linglong.server.http.restful.v1;

import com.linglong.server.http.restful.api.RestActionHandler;
import com.linglong.server.http.restful.dto.Pong;
import com.linglong.server.http.restful.dto.QueryResult;
import com.linglong.server.http.restful.dto.State;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Stereo on 2021/3/12.
 */
public class BackendV1Handler implements RestActionHandler {

    @Override
    public State state(HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public Pong ping(HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public QueryResult query(HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public void write(HttpServletRequest request, HttpServletResponse response) {
    }
}
