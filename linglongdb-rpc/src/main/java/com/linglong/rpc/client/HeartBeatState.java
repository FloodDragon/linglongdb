package com.linglong.rpc.client;

/**
 * Created by liuj-ai on 2019/11/23.
 */
public interface HeartBeatState {

    enum State {
        BORN,
        CONNECTED,
        LOST,
        HEALTHY,
        RECOVERY,
        CEASE;
    }

    State getState();
}
