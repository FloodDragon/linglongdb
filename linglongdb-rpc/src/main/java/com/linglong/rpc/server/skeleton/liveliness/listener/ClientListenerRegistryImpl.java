package com.linglong.rpc.server.skeleton.liveliness.listener;

/**
 * Created by liuj-ai on 2020/4/3.
 */
public class ClientListenerRegistryImpl implements ClientListenerRegistry {
    private volatile ClientRegisterListener clientRegisterListener;//客户端注册心跳监听
    private volatile ClientUnregisterListener clientUnregisterListener;//客户端注销心跳监听
    private volatile ClientLiveExpiredListener clientLiveExpiredListener;//心跳租约超期监听
    private volatile ClientHeartbeatBodyListener clientHeartbeatBodyListener;//心跳携带的body监控

    public ClientRegisterListener getClientRegisterListener() {
        return clientRegisterListener;
    }

    public void registerClientRegisterListener(ClientRegisterListener clientRegisterListener) {
        this.clientRegisterListener = clientRegisterListener;
    }

    public ClientUnregisterListener getClientUnregisterListener() {
        return clientUnregisterListener;
    }

    public void registerClientUnregisterListener(ClientUnregisterListener clientUnregisterListener) {
        this.clientUnregisterListener = clientUnregisterListener;
    }

    public ClientLiveExpiredListener getClientLiveExpiredListener() {
        return clientLiveExpiredListener;
    }

    public void registerClientLiveExpiredListener(ClientLiveExpiredListener clientLiveExpiredListener) {
        this.clientLiveExpiredListener = clientLiveExpiredListener;
    }

    public ClientHeartbeatBodyListener getClientHeartbeatBodyListener() {
        return clientHeartbeatBodyListener;
    }

    public void registerClientHeartbeatBodyListener(ClientHeartbeatBodyListener clientHeartbeatBodyListener) {
        this.clientHeartbeatBodyListener = clientHeartbeatBodyListener;
    }
}
