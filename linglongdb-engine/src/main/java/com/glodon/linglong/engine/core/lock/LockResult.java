package com.glodon.linglong.engine.core.lock;

/**
 * @author Stereo
 */
public enum LockResult {
    ILLEGAL(0),

    INTERRUPTED(0),

    //TIMED_OUT_LATCH(1),

    TIMED_OUT_LOCK(1),

    //DEADLOCK(0),

    ACQUIRED(2),

    UPGRADED(2),

    OWNED_SHARED(3),

    OWNED_UPGRADABLE(3),

    OWNED_EXCLUSIVE(3),

    UNOWNED(0);

    // 1: timed out, 2: acquired, 3: owned
    private final int mType;

    LockResult(int type) {
        mType = type;
    }

    public boolean isTimedOut() {
        return mType == 1;
    }

    public boolean isHeld() {
        return mType >= 2;
    }

    public boolean alreadyOwned() {
        return mType == 3;
    }

    public boolean isAcquired() {
        return mType == 2;
    }

    LockResult commonOwned(LockResult other) {
        if (this == UNOWNED) {
            return this;
        } else if (this == OWNED_SHARED) {
            return other == UNOWNED ? other : this;
        } else if (this == OWNED_UPGRADABLE) {
            return (other == UNOWNED || other == OWNED_SHARED) ? other : this;
        } else if (this == OWNED_EXCLUSIVE) {
            return other;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
