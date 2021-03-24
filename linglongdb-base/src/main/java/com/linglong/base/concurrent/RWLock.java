package com.linglong.base.concurrent;


import java.util.concurrent.locks.LockSupport;

/**
 * 可扩展的非可重入读写锁。
 *
 * @author Stereo
 */
public final class RWLock extends Clutch {
    private final Pack mPack;

    public RWLock() {
        mPack = new Pack(16);
    }

    @Override
    protected Pack getPack() {
        return mPack;
    }

    public void upgrade() {
        while (!this.tryUpgrade()) {
            LockSupport.parkNanos(this, 1000000L);
        }
    }
}
