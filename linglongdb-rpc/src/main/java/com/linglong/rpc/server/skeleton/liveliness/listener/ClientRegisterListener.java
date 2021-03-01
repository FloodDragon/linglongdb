package com.linglong.rpc.server.skeleton.liveliness.listener;


import com.linglong.rpc.common.remoting.Channel;

/**
 * Created by liuj-ai on 2019/1/23.
 */
public interface ClientRegisterListener {
    void registered(String clientId, Channel channel);
}
