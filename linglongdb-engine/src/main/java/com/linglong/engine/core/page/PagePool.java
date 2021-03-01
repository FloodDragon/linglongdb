package com.linglong.engine.core.page;


import com.linglong.base.concurrent.Latch;
import com.linglong.base.concurrent.LatchCondition;

/**
 * @author Stereo
 */
public final class PagePool extends Latch {
    private final transient LatchCondition mQueue;
    private final long[] mPool;
    private int mPos;

    public PagePool(int pageSize, int poolSize, boolean aligned) {
        mQueue = new LatchCondition();
        long[] pool = DirectPageOps.p_allocArray(poolSize);
        for (int i = 0; i < poolSize; i++) {
            pool[i] = DirectPageOps.p_calloc(pageSize, aligned);
        }
        mPool = pool;
        mPos = poolSize;
    }

    public long remove() {
        acquireExclusive();
        try {
            int pos;
            while ((pos = mPos) == 0) {
                mQueue.await(this, -1, 0);
            }
            return mPool[mPos = pos - 1];
        } finally {
            releaseExclusive();
        }
    }

    public void add(long page) {
        acquireExclusive();
        try {
            int pos = mPos;
            mPool[pos] = page;
            // Adjust pos after assignment to prevent harm if an array bounds exception was thrown.
            mPos = pos + 1;
            mQueue.signal();
        } finally {
            releaseExclusive();
        }
    }

    public void delete() {
        acquireExclusive();
        try {
            for (int i = 0; i < mPos; i++) {
                long page = mPool[i];
                mPool[i] = DirectPageOps.p_null();
                DirectPageOps.p_delete(page);
            }
        } finally {
            releaseExclusive();
        }
    }
}
