package com.linglong.rpc.server.skeleton.liveliness.listener;


import com.linglong.rpc.common.remoting.Channel;

/**
 * @author Stereo on 2019/1/23.
 */
public interface ClientUnregisterListener {
    void unregistered(String clientId, Channel channel);
}
