package com.glodon.linglong.engine.core.lock;

import com.glodon.linglong.base.concurrent.Latch;

import java.io.InterruptedIOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Stereo
 */
public final class CommitLock implements Lock {
    private final LongAdder mSharedAcquire = new LongAdder();
    private final LongAdder mSharedRelease = new LongAdder();

    private final Latch mFullLatch = new Latch();

    private volatile Thread mExclusiveThread;

    public static final class Shared extends WeakReference<CommitLock> {
        int count;

        Shared(CommitLock lock) {
            super(lock);
        }

        public void release() {
            CommitLock lock = get();
            if (lock != null) {
                lock.releaseShared();
            }
            count--;
        }

        public void addCountTo(LongAdder adder) {
            if (count > 0) {
                adder.add(count);
            }
        }
    }

    private final ThreadLocal<Shared> mShared = ThreadLocal.withInitial(() -> new Shared(this));

    @Override
    public boolean tryLock() {
        return tryAcquireShared() != null;
    }

    public Shared tryAcquireShared() {
        mSharedAcquire.increment();
        Shared shared = mShared.get();
        if (mExclusiveThread != null && shared.count == 0) {
            releaseShared();
            return null;
        } else {
            shared.count++;
            return shared;
        }
    }

    @Override
    public void lock() {
        acquireShared();
    }

    public Shared acquireShared() {
        mSharedAcquire.increment();
        Shared shared = mShared.get();
        if (mExclusiveThread != null && shared.count == 0) {
            releaseShared();
            mFullLatch.acquireShared();
            try {
                mSharedAcquire.increment();
            } finally {
                mFullLatch.releaseShared();
            }
        }
        shared.count++;
        return shared;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        acquireSharedInterruptibly();
    }

    Shared acquireSharedInterruptibly() throws InterruptedException {
        mSharedAcquire.increment();
        Shared shared = mShared.get();
        if (mExclusiveThread != null && shared.count == 0) {
            releaseShared();
            mFullLatch.acquireSharedInterruptibly();
            try {
                mSharedAcquire.increment();
            } finally {
                mFullLatch.releaseShared();
            }
        }
        shared.count++;
        return shared;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return tryAcquireShared(time, unit) != null;
    }

    Shared tryAcquireShared(long time, TimeUnit unit) throws InterruptedException {
        mSharedAcquire.increment();
        Shared shared = mShared.get();
        if (mExclusiveThread != null && shared.count == 0) {
            releaseShared();
            if (time < 0) {
                mFullLatch.acquireShared();
            } else if (time == 0 || !mFullLatch.tryAcquireSharedNanos(unit.toNanos(time))) {
                return null;
            }
            try {
                mSharedAcquire.increment();
            } finally {
                mFullLatch.releaseShared();
            }
        }
        shared.count++;
        return shared;
    }

    @Override
    public void unlock() {
        releaseShared();
        mShared.get().count--;
    }

    void releaseShared() {
        mSharedRelease.increment();
        Thread t = mExclusiveThread;
        if (t != null && !hasSharedLockers()) {
            LockSupport.unpark(t);
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

    public void acquireExclusive() throws InterruptedIOException {
        long nanosTimeout = 1000; // 1 microsecond
        while (!finishAcquireExclusive(nanosTimeout)) {
            nanosTimeout <<= 1;
        }
    }

    private boolean finishAcquireExclusive(long nanosTimeout) throws InterruptedIOException {
        try {
            mFullLatch.acquireExclusiveInterruptibly();
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }

        mExclusiveThread = Thread.currentThread();

        try {
            final Shared shared = mShared.get();

            shared.addCountTo(mSharedRelease);

            try {
                if (hasSharedLockers()) {

                    long nanosEnd = nanosTimeout <= 0 ? 0 : System.nanoTime() + nanosTimeout;

                    while (true) {
                        if (nanosTimeout < 0) {
                            LockSupport.park(this);
                        } else {
                            LockSupport.parkNanos(this, nanosTimeout);
                        }

                        if (Thread.interrupted()) {
                            throw new InterruptedIOException();
                        }

                        if (!hasSharedLockers()) {
                            break;
                        }

                        if (nanosTimeout >= 0 &&
                                (nanosTimeout == 0
                                        || (nanosTimeout = nanosEnd - System.nanoTime()) <= 0)) {
                            mExclusiveThread = null;
                            mFullLatch.releaseExclusive();
                            return false;
                        }
                    }
                }
            } finally {
                shared.addCountTo(mSharedAcquire);
            }

            shared.count++;
            return true;
        } catch (Throwable e) {
            mExclusiveThread = null;
            mFullLatch.releaseExclusive();
            throw e;
        }
    }

    public void releaseExclusive() {
        mExclusiveThread = null;
        mFullLatch.releaseExclusive();
        mShared.get().count--;
    }

    public boolean hasQueuedThreads() {
        return mFullLatch.hasQueuedThreads();
    }

    private boolean hasSharedLockers() {
        return mSharedRelease.sum() != mSharedAcquire.sum();
    }
}
