package com.linglong.server.http.restful.api;

import com.linglong.server.http.restful.dto.Pong;
import com.linglong.server.http.restful.dto.QueryResult;
import com.linglong.server.http.restful.dto.State;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Stereo on 2019/7/31.
 */
public class RestActionApiImpl extends AbstractRestActionApi {

    public RestActionApiImpl(RestActionHandler restActionHandler) {
        super(restActionHandler);
    }

    @Override
    public State state(HttpServletRequest request, HttpServletResponse response) {
        return restActionHandler.state(request, response);
    }

    @Override
    public Pong ping(HttpServletRequest request, HttpServletResponse response) {
        return restActionHandler.ping(request, response);
    }

    @Override
    public QueryResult query(HttpServletRequest request, HttpServletResponse response) {
        //TODO Filter
        return restActionHandler.query(request, response);
    }

    @Override
    public void write(HttpServletRequest request, HttpServletResponse response) {
        //TODO Filter
        restActionHandler.write(request, response);
    }
}
