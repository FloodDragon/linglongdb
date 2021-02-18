package com.glodon.linglong.server.http;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Created by liuj-ai on 2019/8/7.
 */
public interface HttpHandler {
    void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;
}