package com.glodon.linglong.replication;

/**
 * @author Stereo
 */
public enum Role {
    NORMAL((byte) 1),

    STANDBY((byte) 2),

    OBSERVER((byte) 3);

    byte mCode;

    private Role(byte code) {
        mCode = code;
    }

    static Role decode(byte code) {
        switch (code) {
            case 1:
                return NORMAL;
            case 2:
                return STANDBY;
            case 3:
                return OBSERVER;
            default:
                throw new IllegalArgumentException();
        }
    }
}
