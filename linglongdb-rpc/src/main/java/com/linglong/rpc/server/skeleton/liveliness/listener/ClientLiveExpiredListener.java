package com.linglong.rpc.server.skeleton.liveliness.listener;


import com.linglong.rpc.common.remoting.Channel;

/**
 * Created by liuj-ai on 2019/1/23.
 */
public interface ClientLiveExpiredListener {
    void expired(String clientId, Channel channel);
}
