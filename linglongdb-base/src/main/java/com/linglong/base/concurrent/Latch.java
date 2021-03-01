package com.linglong.base.concurrent;

import com.linglong.base.common.IOUtils;
import com.linglong.base.common.UnsafeAccess;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * 非重入式读写锁，专为公平起见而设计。
 * 实作不跟踪线程所有权或检查非法使用情况。
 * 通常情况优于ReentrantLock和内置的Java同步。
 * 不公平情况等待线程不会无限期的饥饿。
 * 此版本借鉴于LMAX公司高并发设计并且简化而来。
 *
 * @author Stereo
 */
@SuppressWarnings("restriction")
public class Latch {
    public static final int UNLATCHED = 0, EXCLUSIVE = 0x80000000, SHARED = 1;

    public static final int SPIN_LIMIT = Runtime.getRuntime().availableProcessors() > 1 ? 1 << 10 : 1;

    // TODO: Switch to VarHandle when available and utilize specialized operations. 
    static final sun.misc.Unsafe UNSAFE = UnsafeAccess.obtain();

    static final long STATE_OFFSET, FIRST_OFFSET, LAST_OFFSET;
    static final long WAITER_OFFSET;

    static {
        try {
            // Reduce the risk of "lost unpark" due to classloading.
            // https://bugs.openjdk.java.net/browse/JDK-8074773
            Class<?> clazz = LockSupport.class;

            clazz = Latch.class;
            STATE_OFFSET = UNSAFE.objectFieldOffset(clazz.getDeclaredField("mLatchState"));
            FIRST_OFFSET = UNSAFE.objectFieldOffset(clazz.getDeclaredField("mLatchFirst"));
            LAST_OFFSET = UNSAFE.objectFieldOffset(clazz.getDeclaredField("mLatchLast"));

            clazz = WaitNode.class;
            WAITER_OFFSET = UNSAFE.objectFieldOffset(clazz.getDeclaredField("mWaiter"));
        } catch (Throwable e) {
            throw IOUtils.rethrow(e);
        }
    }

    /*
      unlatched:           0               latch is available
      shared:              1..0x7fffffff   latch is held shared
      exclusive:  0x80000000               latch is held exclusively
      illegal:    0x80000001..0xffffffff   illegal exclusive state
     */
    volatile int mLatchState;

    private transient volatile WaitNode mLatchFirst;
    private transient volatile WaitNode mLatchLast;

    public Latch() {
    }

    /**
     * initialState = UNLATCHED, EXCLUSIVE, or SHARED
     */
    public Latch(int initialState) {
        UNSAFE.putInt(this, STATE_OFFSET, initialState);
    }

    boolean isHeldExclusive() {
        return mLatchState == EXCLUSIVE;
    }

    public boolean tryAcquireExclusive() {
        return doTryAcquireExclusive();
    }

    private boolean doTryAcquireExclusive() {
        return mLatchState == 0 && UNSAFE.compareAndSwapInt(this, STATE_OFFSET, 0, EXCLUSIVE);
    }

    public boolean tryAcquireExclusiveNanos(long nanosTimeout) throws InterruptedException {
        return doTryAcquireExclusiveNanos(nanosTimeout);
    }

    private boolean doTryAcquireExclusiveNanos(long nanosTimeout) throws InterruptedException {
        if (doTryAcquireExclusive()) {
            return true;
        }

        if (nanosTimeout == 0) {
            return false;
        }

        boolean result;
        try {
            result = acquire(new Timed(nanosTimeout));
        } catch (Throwable e) {
            // 可能 OutOfMemoryError.
            if (nanosTimeout < 0) {
                while (!doTryAcquireExclusive()) ;
                return true;
            }
            return false;
        }

        return checkTimedResult(result, nanosTimeout);
    }

    public void acquireExclusive() {
        if (!doTryAcquireExclusive()) {
            doAcquireExclusive();
        }
    }

    private void doAcquireExclusive() {
        try {
            acquire(new WaitNode());
        } catch (Throwable e) {
            while (!doTryAcquireExclusive()) ;
        }
    }

    public void acquireExclusiveInterruptibly() throws InterruptedException {
        doTryAcquireExclusiveNanos(-1);
    }

    public final void downgrade() {
        mLatchState = 1;

        while (true) {
            final WaitNode first = first();
            if (first == null) {
                return;
            }

            WaitNode node = first;
            while (true) {
                Thread waiter = node.mWaiter;
                if (waiter != null) {
                    if (node instanceof Shared) {
                        UNSAFE.getAndAddInt(this, STATE_OFFSET, 1);
                        if (UNSAFE.compareAndSwapObject(node, WAITER_OFFSET, waiter, null)) {
                            LockSupport.unpark(waiter);
                        } else {
                            UNSAFE.getAndAddInt(this, STATE_OFFSET, -1);
                        }
                    } else {
                        if (node != first) {
                            mLatchFirst = node;
                        }
                        return;
                    }
                }

                WaitNode next = node.get();

                if (next == null) {
                    if (UNSAFE.compareAndSwapObject(this, LAST_OFFSET, node, null)) {
                        UNSAFE.compareAndSwapObject(this, FIRST_OFFSET, first, null);
                        return;
                    }
                    break;
                }

                node = next;
            }
        }
    }

    public final void releaseExclusive() {
        int trials = 0;
        while (true) {
            WaitNode last = mLatchLast;

            if (last == null) {
                mLatchState = 0;

                last = mLatchLast;
                if (last == null || !UNSAFE.compareAndSwapInt(this, STATE_OFFSET, 0, EXCLUSIVE)) {
                    return;
                }
            }

            WaitNode first = mLatchFirst;

            unpark:
            if (first != null) {
                Thread waiter = first.mWaiter;

                if (waiter != null) {
                    if (first instanceof Shared) {
                        // TODO: 是否可以合并操作
                        downgrade();
                        doReleaseShared();
                        return;
                    }

                    if (!first.mFair) {
                        mLatchState = 0;
                        LockSupport.unpark(waiter);
                        return;
                    }
                }

                {
                    WaitNode next = first.get();
                    if (next != null) {
                        mLatchFirst = next;
                    } else {
                        if (last != first ||
                                !UNSAFE.compareAndSwapObject(this, LAST_OFFSET, last, null)) {
                            break unpark;
                        }
                        UNSAFE.compareAndSwapObject(this, FIRST_OFFSET, last, null);
                    }
                }

                if (waiter != null &&
                        UNSAFE.compareAndSwapObject(first, WAITER_OFFSET, waiter, null)) {
                    LockSupport.unpark(waiter);
                    return;
                }
            }

            trials = spin(trials);
        }
    }

    public final void release(boolean exclusive) {
        if (exclusive) {
            releaseExclusive();
        } else {
            releaseShared();
        }
    }

    public final void releaseEither() {
        // TODO: mLatchState 待优化去掉volatile读
        if (mLatchState == EXCLUSIVE) {
            releaseExclusive();
        } else {
            releaseShared();
        }
    }

    public boolean tryAcquireShared() {
        return doTryAcquireShared();
    }

    private boolean doTryAcquireShared() {
        WaitNode first = mLatchFirst;
        if (first != null && !(first instanceof Shared)) {
            return false;
        }
        int state = mLatchState;
        return state >= 0 && UNSAFE.compareAndSwapInt(this, STATE_OFFSET, state, state + 1);
    }

    public boolean tryAcquireSharedNanos(long nanosTimeout) throws InterruptedException {
        return doTryAcquireSharedNanos(nanosTimeout);
    }

    private final boolean doTryAcquireSharedNanos(long nanosTimeout) throws InterruptedException {
        WaitNode first = mLatchFirst;
        if (first == null || first instanceof Shared) {
            int trials = 0;
            int state;
            while ((state = mLatchState) >= 0) {
                if (UNSAFE.compareAndSwapInt(this, STATE_OFFSET, state, state + 1)) {
                    return true;
                }
                trials = spin(trials);
            }
        }

        if (nanosTimeout == 0) {
            return false;
        }

        boolean result;
        try {
            result = acquire(new TimedShared(nanosTimeout));
        } catch (Throwable e) {
            if (nanosTimeout < 0) {
                while (!doTryAcquireShared()) ;
                return true;
            }
            return false;
        }

        return checkTimedResult(result, nanosTimeout);
    }

    private static boolean checkTimedResult(boolean result, long nanosTimeout)
            throws InterruptedException {
        if (!result && (Thread.interrupted() || nanosTimeout < 0)) {
            InterruptedException e;
            try {
                e = new InterruptedException();
            } catch (Throwable e2) {
                if (nanosTimeout < 0) {
                    throw e2;
                }
                return false;
            }
            throw e;
        }

        return result;
    }

    public boolean acquireSharedUncontended() {
        WaitNode first = mLatchFirst;
        if (first == null || first instanceof Shared) {
            int state = mLatchState;
            if (state >= 0) {
                return UNSAFE.compareAndSwapInt(this, STATE_OFFSET, state, state + 1);
            }
        }

        try {
            acquire(new Shared());
        } catch (Throwable e) {
            while (!doTryAcquireShared()) ;
        }

        return true;
    }

    public int acquireSharedUncontendedNanos(long nanosTimeout) throws InterruptedException {
        WaitNode first = mLatchFirst;
        if (first == null || first instanceof Shared) {
            int state = mLatchState;
            if (state >= 0) {
                return UNSAFE.compareAndSwapInt(this, STATE_OFFSET, state, state + 1) ? 1 : -1;
            }
        }

        boolean result;
        try {
            result = acquire(new TimedShared(nanosTimeout));
        } catch (Throwable e) {
            if (nanosTimeout < 0) {
                while (!doTryAcquireShared()) ;
                return 1;
            }
            return 0;
        }

        return checkTimedResult(result, nanosTimeout) ? 1 : 0;
    }

    public void acquireShared() {
        if (!tryAcquireSharedSpin()) {
            try {
                acquire(new Shared());
            } catch (Throwable e) {
                while (!doTryAcquireShared()) ;
            }
        }
    }

    private boolean tryAcquireSharedSpin() {
        WaitNode first = mLatchFirst;
        if (first == null || first instanceof Shared) {
            int trials = 0;
            int state;
            while ((state = mLatchState) >= 0) {
                if (UNSAFE.compareAndSwapInt(this, STATE_OFFSET, state, state + 1)) {
                    return true;
                }
                trials = spin(trials);
            }
        }
        return false;
    }

    public void acquireSharedInterruptibly() throws InterruptedException {
        doTryAcquireSharedNanos(-1);
    }

    public boolean tryUpgrade() {
        return doTryUpgrade();
    }

    private boolean doTryUpgrade() {
        while (true) {
            int state = mLatchState;
            if ((state & ~EXCLUSIVE) != 1) {
                return false;
            }
            if (UNSAFE.compareAndSwapInt(this, STATE_OFFSET, state, EXCLUSIVE)) {
                return true;
            }
        }
    }

    public void releaseShared() {
        doReleaseShared();
    }

    private void doReleaseShared() {
        int trials = 0;
        while (true) {
            int state = mLatchState;

            WaitNode last = mLatchLast;
            if (last == null) {
                if (UNSAFE.compareAndSwapInt(this, STATE_OFFSET, state, --state)) {
                    if (state == 0) {
                        last = mLatchLast;
                        if (last != null &&
                                UNSAFE.compareAndSwapInt(this, STATE_OFFSET, 0, EXCLUSIVE)) {
                            releaseExclusive();
                        }
                    }
                    return;
                }
            } else if (state == 1) {
                if (UNSAFE.compareAndSwapInt(this, STATE_OFFSET, 1, EXCLUSIVE) || doTryUpgrade()) {
                    releaseExclusive();
                    return;
                }
            } else if (UNSAFE.compareAndSwapInt(this, STATE_OFFSET, state, --state)) {
                return;
            }

            trials = spin(trials);
        }
    }

    private boolean acquire(final WaitNode node) {
        node.mWaiter = Thread.currentThread();
        WaitNode prev = enqueue(node);
        int acquireResult = node.tryAcquire(this);

        if (acquireResult < 0) {
            int denied = 0;
            while (true) {
                boolean parkAbort = node.park(this);

                acquireResult = node.tryAcquire(this);

                if (acquireResult >= 0) {
                    break;
                }

                if (parkAbort) {
                    if (!UNSAFE.compareAndSwapObject
                            (node, WAITER_OFFSET, Thread.currentThread(), null)) {
                        return true;
                    }

                    if (prev != null) {
                        remove(node, prev);
                    }

                    return false;
                }

                if (denied++ == 0) {
                    node.mFair = true;
                }
            }
        }

        if (acquireResult != 0) {
            return true;
        }

        if (mLatchFirst != node) {
            remove(node, prev);
            return true;
        }

        while (true) {
            WaitNode next = node.get();
            if (next != null) {
                mLatchFirst = next;
                return true;
            } else {
                WaitNode last = mLatchLast;
                if (last == node && UNSAFE.compareAndSwapObject(this, LAST_OFFSET, last, null)) {
                    UNSAFE.compareAndSwapObject(this, FIRST_OFFSET, last, null);
                    return true;
                }
            }
        }
    }

    private WaitNode enqueue(final WaitNode node) {
        WaitNode prev = (WaitNode) UNSAFE.getAndSetObject(this, LAST_OFFSET, node);

        if (prev == null) {
            mLatchFirst = node;
        } else {
            prev.set(node);
            WaitNode pp = prev.mPrev;
            if (pp != null) {
                pp.lazySet(node);
            }
        }

        return prev;
    }

    private void remove(final WaitNode node, final WaitNode prev) {
        WaitNode next = node.get();

        if (next == null) {
            node.mPrev = prev;
            next = node.get();
            if (next == null) {
                return;
            }
        }

        while (next.mWaiter == null) {
            WaitNode nextNext = next.get();
            if (nextNext == null) {
                break;
            }
            next = nextNext;
        }

        prev.lazySet(next);
    }

    private WaitNode first() {
        int trials = 0;
        while (true) {
            WaitNode last = mLatchLast;
            if (last == null) {
                return null;
            }
            WaitNode first = mLatchFirst;
            if (first != null) {
                return first;
            }
            trials = spin(trials);
        }
    }

    public final boolean hasQueuedThreads() {
        return mLatchLast != null;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        appendMiniString(b, this);
        b.append(" {state=");

        int state = mLatchState;
        if (state == 0) {
            b.append("unlatched");
        } else if (state == EXCLUSIVE) {
            b.append("exclusive");
        } else if (state >= 0) {
            b.append("shared:").append(state);
        } else {
            b.append("illegal:").append(state);
        }

        WaitNode last = mLatchLast;

        if (last != null) {
            b.append(", ");
            WaitNode first = mLatchFirst;
            if (first == last) {
                b.append("firstQueued=").append(last);
            } else if (first == null) {
                b.append("lastQueued=").append(last);
            } else {
                b.append("firstQueued=").append(first)
                        .append(", lastQueued=").append(last);
            }
        }

        return b.append('}').toString();
    }

    static void appendMiniString(StringBuilder b, Object obj) {
        if (obj == null) {
            b.append("null");
            return;
        }
        b.append(obj.getClass().getName()).append('@').append(Integer.toHexString(obj.hashCode()));
    }

    static int spin(int trials) {
        trials++;
        if (trials >= SPIN_LIMIT) {
            Thread.yield();
            trials = 0;
        }
        return trials;
    }

    @SuppressWarnings("serial")
    static class WaitNode extends AtomicReference<WaitNode> {
        volatile Thread mWaiter;
        volatile boolean mFair;

        volatile WaitNode mPrev;

        boolean park(Latch latch) {
            LockSupport.park(latch);
            return false;
        }

        int tryAcquire(Latch latch) {
            int trials = 0;
            while (true) {
                for (int i = 0; i < SPIN_LIMIT; i++) {
                    boolean acquired = latch.doTryAcquireExclusive();
                    Object waiter = mWaiter;
                    if (waiter == null) {
                        return 1;
                    }
                    if (!acquired) {
                        continue;
                    }
                    if (!mFair) {
                        UNSAFE.putOrderedObject(this, WAITER_OFFSET, null);
                    } else if (!UNSAFE.compareAndSwapObject(this, WAITER_OFFSET, waiter, null)) {
                        return 1;
                    }
                    return 0;
                }
                if (++trials >= SPIN_LIMIT >> 1) {
                    return -1;
                }
                Thread.yield();
            }
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            appendMiniString(b, this);
            b.append(" {waiter=").append(mWaiter);
            b.append(", fair=").append(mFair);
            b.append(", next=");
            appendMiniString(b, get());
            b.append(", prev=");
            appendMiniString(b, mPrev);
            return b.append('}').toString();
        }
    }

    @SuppressWarnings("serial")
    static class Timed extends WaitNode {
        private long mNanosTimeout;
        private long mEndNanos;

        Timed(long nanosTimeout) {
            mNanosTimeout = nanosTimeout;
            if (nanosTimeout >= 0) {
                mEndNanos = System.nanoTime() + nanosTimeout;
            }
        }

        @Override
        final boolean park(Latch latch) {
            if (mNanosTimeout < 0) {
                LockSupport.park(latch);
                return Thread.currentThread().isInterrupted();
            } else {
                LockSupport.parkNanos(latch, mNanosTimeout);
                if (Thread.currentThread().isInterrupted()) {
                    return true;
                }
                return (mNanosTimeout = mEndNanos - System.nanoTime()) <= 0;
            }
        }
    }

    @SuppressWarnings("serial")
    static class Shared extends WaitNode {
        @Override
        final int tryAcquire(Latch latch) {
            WaitNode first = latch.mLatchFirst;
            if (first != null && !(first instanceof Shared)) {
                return mWaiter == null ? 1 : -1;
            }

            int trials = 0;
            while (true) {
                if (mWaiter == null) {
                    return 1;
                }

                int state = latch.mLatchState;
                if (state < 0) {
                    return state;
                }

                if (UNSAFE.compareAndSwapInt(latch, STATE_OFFSET, state, state + 1)) {
                    Object waiter = mWaiter;
                    if (waiter == null ||
                            !UNSAFE.compareAndSwapObject(this, WAITER_OFFSET, waiter, null)) {
                        if (!UNSAFE.compareAndSwapInt(latch, STATE_OFFSET, state + 1, state)) {
                            UNSAFE.getAndAddInt(latch, STATE_OFFSET, -1);
                        }
                        return 1;
                    }

                    return state;
                }

                trials = spin(trials);
            }
        }
    }

    @SuppressWarnings("serial")
    static class TimedShared extends Shared {
        private long mNanosTimeout;
        private long mEndNanos;

        TimedShared(long nanosTimeout) {
            mNanosTimeout = nanosTimeout;
            if (nanosTimeout >= 0) {
                mEndNanos = System.nanoTime() + nanosTimeout;
            }
        }

        @Override
        final boolean park(Latch latch) {
            if (mNanosTimeout < 0) {
                LockSupport.park(latch);
                return Thread.currentThread().isInterrupted();
            } else {
                LockSupport.parkNanos(latch, mNanosTimeout);
                if (Thread.currentThread().isInterrupted()) {
                    return true;
                }
                return (mNanosTimeout = mEndNanos - System.nanoTime()) <= 0;
            }
        }
    }
}
