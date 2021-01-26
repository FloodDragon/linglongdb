package com.glodon.linglong.base.lock;

import com.glodon.linglong.base.core.DatabaseAccess;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Stereo
 */
abstract class LockOwner implements DatabaseAccess {
    private final int mHash;

    Lock mWaitingFor;

    LockOwner() {
        mHash = ThreadLocalRandom.current().nextInt();
    }

    @Override
    public final int hashCode() {
        return mHash;
    }

    public abstract void attach(Object obj);

    public abstract Object attachment();
}
