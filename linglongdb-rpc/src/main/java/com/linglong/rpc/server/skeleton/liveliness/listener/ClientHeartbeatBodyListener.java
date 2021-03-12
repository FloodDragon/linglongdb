package com.linglong.rpc.server.skeleton.liveliness.listener;

/**
 * @author Stereo on 2019/1/23.
 */
public interface ClientHeartbeatBodyListener {
    void process(String clientId, String body);
}
