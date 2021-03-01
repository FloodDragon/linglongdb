package com.linglong.rpc.server.skeleton.service;


import com.linglong.rpc.common.event.Event;
import com.linglong.rpc.common.event.EventHandler;
import com.linglong.rpc.server.event.enums.ServiceEnum;

/**
 * Created by liuj-ai on 2019/12/10.
 */
public interface ServiceEventHandler<RequestEvent, ResponseEvent> extends EventHandler<Event<ServiceEnum>> {

    //public void handleHeartbeat(HeartbeatEvent heartbeat) throws Exception;

    void handleRequest(RequestEvent request) throws Exception;

    void replyResponse(ResponseEvent response) throws Exception;

    IServiceInvoker getServiceInvoker();
}
