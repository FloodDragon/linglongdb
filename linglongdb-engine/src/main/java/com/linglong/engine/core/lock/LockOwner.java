package com.linglong.engine.core.lock;

import com.linglong.engine.core.frame.DatabaseAccess;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Stereo
 */
public abstract class LockOwner implements DatabaseAccess {
    private final int mHash;

    Lock mWaitingFor;

    public LockOwner() {
        mHash = ThreadLocalRandom.current().nextInt();
    }

    @Override
    public final int hashCode() {
        return mHash;
    }

    public abstract void attach(Object obj);

    public abstract Object attachment();
}
