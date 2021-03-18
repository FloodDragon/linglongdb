package com.linglong.replication;

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

    public static Role getRole(String role) {
        Role[] roles = Role.values();
        for (Role r : roles) {
            if (r.name().equals(role)) {
                return r;
            }
        }
        return null;
    }
}
