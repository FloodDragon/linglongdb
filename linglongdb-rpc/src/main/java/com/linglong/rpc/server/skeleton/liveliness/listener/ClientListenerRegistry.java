package com.linglong.rpc.server.skeleton.liveliness.listener;

/**
 * Created by liuj-ai on 2020/4/3.
 */
public interface ClientListenerRegistry {

    void registerClientRegisterListener(ClientRegisterListener clientRegisterListener);

    void registerClientUnregisterListener(ClientUnregisterListener clientUnregisterListener);

    void registerClientLiveExpiredListener(ClientLiveExpiredListener clientLiveExpiredListener);

    void registerClientHeartbeatBodyListener(ClientHeartbeatBodyListener clientHeartbeatBodyListener);
}
