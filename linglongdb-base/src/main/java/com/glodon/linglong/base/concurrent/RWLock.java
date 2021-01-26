package com.glodon.linglong.base.concurrent;


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
}
