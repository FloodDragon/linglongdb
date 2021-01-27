package com.glodon.linglong.engine.lock;

/**
 * @author Stereo
 */
public enum LockMode {

    UPGRADABLE_READ(LockManager.TYPE_UPGRADABLE, false),

    REPEATABLE_READ(LockManager.TYPE_SHARED, false),

    READ_COMMITTED(0, false),

    READ_UNCOMMITTED(0, true),

    UNSAFE(0, true);

    final int repeatable;

    final boolean noReadLock;

    LockMode(int repeatable, boolean noReadLock) {
        this.repeatable = repeatable;
        this.noReadLock = noReadLock;
    }

    public boolean isRepeatable() {
        return repeatable != 0;
    }
}
