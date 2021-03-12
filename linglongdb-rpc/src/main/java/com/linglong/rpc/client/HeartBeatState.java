package com.linglong.rpc.client;

/**
 * @author Stereo
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
