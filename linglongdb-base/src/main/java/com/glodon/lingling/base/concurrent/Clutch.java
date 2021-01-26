package com.glodon.lingling.base.concurrent;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;

/**
 * clutch是一种专门的锁，它可以支持高度并发的共享请求，前提是exclusive请求很少发生。
 * 当由于高争用而拒绝太多的共享请求时，离合器将切换到一个特殊的争用模式。
 * 稍后，当一个独家离合器是获得，模式切换回非竞争模式。
 * 这种设计允许离合器是自适应的，依靠独家离合器作为信号，访问模式已经改变。
 * <p>
 * 注意:共享访问不应该被任何线程无限期地持有。
 * 如果另一个线程试图切换到争用模式，它首先需要获得独占访问才能进行切换。
 * 如果线程继续尝试，即使可以授予共享访问权，线程也会阻塞。
 * 这种行为也适用于降级。
 * 在降级锁存器完全释放之前，其他线程不能切换到竞争模式。
 *
 * @author Stereo
 */
public abstract class Clutch extends Latch {
    // 继承的锁存器方法用于非竞争模式，以及切换到它。
    //mContendedSlot >=0 竞争模式
    private volatile int mContendedSlot = -1;

    public Clutch() {
    }

    /**
     * @param initialState=UNLATCHED/EXCLUSIVE/SHARED
     */
    public Clutch(int initialState) {
        super(initialState);
    }

    public final boolean isContended() {
        return mContendedSlot >= 0;
    }

    @Override
    public final boolean tryAcquireExclusive() {
        if (!super.tryAcquireExclusive()) {
            return false;
        }
        int slot = mContendedSlot;
        if (slot >= 0) {
            if (!getPack().tryUnregisterExclusive(slot, this)) {
                super.releaseExclusive();
                return false;
            }
            mContendedSlot = -1;
        }
        return true;
    }

    @Override
    public final boolean tryAcquireExclusiveNanos(long nanosTimeout) throws InterruptedException {
        if (nanosTimeout < 0) {
            acquireExclusiveInterruptibly();
            return true;
        }

        long start = System.nanoTime();
        if (!super.tryAcquireExclusiveNanos(nanosTimeout)) {
            return false;
        }

        nanosTimeout -= (System.nanoTime() - start);
        if (nanosTimeout < 0) {
            nanosTimeout = 0;
        }

        int slot = mContendedSlot;
        if (slot >= 0) {
            if (!getPack().tryUnregisterExclusiveNanos(slot, this, nanosTimeout)) {
                super.releaseExclusive();
                return false;
            }
            mContendedSlot = -1;
        }

        return true;
    }

    @Override
    public final void acquireExclusive() {
        super.acquireExclusive();
        int slot = mContendedSlot;
        if (slot >= 0) {
            getPack().unregisterExclusive(slot);
            mContendedSlot = -1;
        }
    }

    @Override
    public final void acquireExclusiveInterruptibly() throws InterruptedException {
        super.acquireExclusiveInterruptibly();
        int slot = mContendedSlot;
        if (slot >= 0) {
            getPack().tryUnregisterExclusiveNanos(slot, this, -1);
            mContendedSlot = -1;
        }
    }

    public final void downgrade(boolean contended) {
        if (contended) {
            Pack pack = getPack();
            int slot = pack.tryRegister(this);
            if (slot >= 0) {
                mContendedSlot = slot;
                if (!pack.tryAcquireShared(slot, this)) {
                    throw new AssertionError();
                }
                super.releaseExclusive();
                return;
            }
        }

        super.downgrade();
    }

    public final void releaseExclusive(boolean contended) {
        if (contended) {
            mContendedSlot = getPack().tryRegister(this);
        }
        super.releaseExclusive();
    }

    @Override
    public final boolean tryAcquireShared() {
        int slot = mContendedSlot;
        if (slot < 0 || !getPack().tryAcquireShared(slot, this)) {
            if (!super.tryAcquireShared()) {
                return false;
            }
            uncontendedMode();
        }
        return true;
    }

    @Override
    public final boolean tryAcquireSharedNanos(long nanosTimeout) throws InterruptedException {
        if (nanosTimeout < 0) {
            acquireSharedInterruptibly();
            return true;
        }

        doAcquire:
        {
            int slot = mContendedSlot;
            if (slot >= 0) {
                if (getPack().tryAcquireShared(slot, this)) {
                    return true;
                }
            } else {
                long start = System.nanoTime();
                int result = acquireSharedUncontendedNanos(nanosTimeout);
                if (result > 0) {
                    break doAcquire;
                } else if (result == 0) {
                    return false;
                }
                nanosTimeout -= (System.nanoTime() - start);
                if (nanosTimeout < 0) {
                    nanosTimeout = 0;
                }
                if (shouldSwitchToContendedMode()) {
                    if (super.tryAcquireShared()) {
                        break doAcquire;
                    }
                    if (!super.tryAcquireExclusiveNanos(nanosTimeout)) {
                        return false;
                    }
                    contendedMode();
                    return true;
                }
            }

            if (!super.tryAcquireSharedNanos(nanosTimeout)) {
                return false;
            }
        }

        uncontendedMode();
        return true;
    }

    @Override
    public final boolean acquireSharedUncontended() {
        int slot = mContendedSlot;
        if (slot < 0 || !getPack().tryAcquireShared(slot, this)) {
            if (!super.acquireSharedUncontended()) {
                return false;
            }
            uncontendedMode();
        }
        return true;
    }

    @Override
    public final int acquireSharedUncontendedNanos(long nanosTimeout) throws InterruptedException {
        int slot = mContendedSlot;
        if (slot < 0 || !getPack().tryAcquireShared(slot, this)) {
            int result = super.acquireSharedUncontendedNanos(nanosTimeout);
            if (result <= 0) {
                return result;
            }
            uncontendedMode();
        }
        return 1;
    }

    @Override
    public final void acquireShared() {
        doAcquire:
        {
            int slot = mContendedSlot;
            if (slot >= 0) {
                if (getPack().tryAcquireShared(slot, this)) {
                    return;
                }
            } else {
                if (super.acquireSharedUncontended()) {
                    break doAcquire;
                }
                if (shouldSwitchToContendedMode()) {
                    if (super.tryAcquireShared()) {
                        break doAcquire;
                    }
                    super.acquireExclusive();
                    contendedMode();
                    return;
                }
            }

            super.acquireShared();
        }

        uncontendedMode();
    }

    @Override
    public final void acquireSharedInterruptibly() throws InterruptedException {
        doAcquire:
        {
            int slot = mContendedSlot;
            if (slot >= 0) {
                if (getPack().tryAcquireShared(slot, this)) {
                    return;
                }
            } else {
                if (super.acquireSharedUncontendedNanos(-1) > 0) {
                    break doAcquire;
                }
                if (shouldSwitchToContendedMode()) {
                    if (super.tryAcquireShared()) {
                        break doAcquire;
                    }
                    super.acquireExclusiveInterruptibly();
                    contendedMode();
                    return;
                }
            }

            super.acquireSharedInterruptibly();
        }

        uncontendedMode();
    }

    private static boolean shouldSwitchToContendedMode() {
        return (ThreadLocalRandom.current().nextInt() & 0x0ff) == 0;
    }

    private void contendedMode() {
        Pack pack = getPack();
        int slot = mContendedSlot;
        if (slot < 0) {
            slot = pack.tryRegister(this);
            if (slot < 0) {
                // No slots are available.
                super.downgrade();
                return;
            }
            mContendedSlot = slot;
        }

        if (!pack.tryAcquireShared(slot, this)) {
            throw new AssertionError();
        }

        super.releaseExclusive();
    }

    private void uncontendedMode() {
        int slot = mContendedSlot;
        if (slot >= 0) {
            if (!getPack().tryAcquireShared(slot, this)) {
                throw new AssertionError();
            }
            super.releaseShared();
        }
    }

    @Override
    public final boolean tryUpgrade() {
        return mContendedSlot < 0 && super.tryUpgrade();
    }

    @Override
    public final void releaseShared() {
        // TODO: can be non-volatile read
        int slot = mContendedSlot;
        if (slot < 0) {
            super.releaseShared();
        } else {
            getPack().releaseShared(slot);
        }
    }

    @Override
    public String toString() {
        if (mContendedSlot < 0) {
            return super.toString();
        }
        StringBuilder b = new StringBuilder();
        appendMiniString(b, this);
        return b.append(" {state=").append("contended").append('}').toString();
    }

    protected abstract Pack getPack();

    /**
     * 支持竞争共享对象。内存开销(以字节为单位)与{@code(插槽数)*(内核数)}成正比。插槽的数量应该至少为16，以减少缓存线争用。为了方便起见，这个类还扩展了Latch类，但是这里没有使用Latch特性。
     */
    @SuppressWarnings("restriction")
    public static class Pack extends Latch {
        private static final int OBJECT_ARRAY_BASE;
        private static final int OBJECT_ARRAY_SHIFT;
        private static final int INT_ARRAY_BASE;
        private static final int INT_ARRAY_SHIFT;

        static {
            OBJECT_ARRAY_BASE = UNSAFE.arrayBaseOffset(Object[].class);
            OBJECT_ARRAY_SHIFT = computeShift(UNSAFE.arrayIndexScale(Object[].class));
            INT_ARRAY_BASE = UNSAFE.arrayBaseOffset(int[].class);
            INT_ARRAY_SHIFT = computeShift(UNSAFE.arrayIndexScale(int[].class));
        }

        private static int computeShift(int scale) {
            if ((scale & (scale - 1)) != 0) {
                throw new Error("data type scale not a power of two");
            }
            return 31 - Integer.numberOfLeadingZeros(scale);
        }

        private final int mCores;
        private final Object[] mSlots;
        private final int[] mCounters;
        private final int[] mThreadStripes;

        public Pack(int numSlots) {
            this(numSlots, Runtime.getRuntime().availableProcessors());
        }

        public Pack(int numSlots, int cores) {
            if (cores < 1) {
                throw new IllegalArgumentException();
            }
            mCores = cores;
            mSlots = new Object[numSlots];
            mCounters = new int[numSlots * cores];
            mThreadStripes = new int[cores * 4];
        }

        final int tryRegister(Clutch clutch) {
            Object[] slots = mSlots;
            int slot = ThreadLocalRandom.current().nextInt(slots.length);

            Object existing = get(slots, slot);
            if (existing == null) {
                if (compareAndSet(slots, slot, null, clutch)) {
                    return slot;
                }
                existing = get(slots, slot);
            }

            if (existing instanceof Clutch) {
                Clutch existingClutch = (Clutch) existing;
                if (existingClutch.tryAcquireExclusive()) {
                    existingClutch.releaseExclusive();
                    if (compareAndSet(slots, slot, null, clutch)) {
                        return slot;
                    }
                }
            }

            return -1;
        }

        final boolean tryUnregisterExclusive(int slot, Clutch clutch) {
            if (isZero(slot)) {
                set(mSlots, slot, this);
                if (isZero(slot)) {
                    set(mSlots, slot, null);
                    return true;
                }
                set(mSlots, slot, clutch);
            }
            return false;
        }

        final boolean tryUnregisterExclusiveNanos(int slot, Clutch clutch, long nanosTimeout)
                throws InterruptedException {
            set(mSlots, slot, Thread.currentThread());

            if (nanosTimeout < 0) {
                while (!isZero(slot)) {
                    LockSupport.park(this);
                    if (Thread.interrupted()) {
                        set(mSlots, slot, clutch);
                        throw new InterruptedException();
                    }
                }
            } else if (!isZero(slot)) {
                long start = System.nanoTime();
                while (true) {
                    LockSupport.parkNanos(this, nanosTimeout);
                    if (Thread.interrupted()) {
                        set(mSlots, slot, clutch);
                        throw new InterruptedException();
                    }
                    if (isZero(slot)) {
                        break;
                    }
                    long now = System.nanoTime();
                    nanosTimeout -= (now - start);
                    if (nanosTimeout <= 0) {
                        set(mSlots, slot, clutch);
                        return false;
                    }
                    start = now;
                }
            }

            set(mSlots, slot, null);
            return true;
        }

        final void unregisterExclusive(int slot) {
            set(mSlots, slot, Thread.currentThread());
            while (!isZero(slot)) {
                LockSupport.park(this);
                Thread.interrupted();
            }
            set(mSlots, slot, null);
        }

        final boolean tryAcquireShared(int slot, Clutch clutch) {
            int stripe = add(slot, +1);
            if (get(mSlots, slot) == clutch) {
                return true;
            }
            releaseShared(slot, stripe);
            return false;
        }

        final void releaseShared(int slot) {
            releaseShared(slot, threadStripe());
        }

        private void releaseShared(int slot, int stripe) {
            add(slot, -1, stripe);
            Object entry = get(mSlots, slot);
            if (entry instanceof Thread && isZero(slot)) {
                LockSupport.unpark((Thread) entry);
            }
        }

        private int add(int slot, int delta) {
            int stripe = threadStripe();
            long offset = intArrayByteOffset(slot + stripe);
            int[] counters = mCounters;
            int cv = UNSAFE.getIntVolatile(counters, offset);

            if (!UNSAFE.compareAndSwapInt(counters, offset, cv, cv + delta)) {
                stripe = ThreadLocalRandom.current().nextInt(mCores) * mSlots.length;
                int id = xorshift((int) Thread.currentThread().getId());
                mThreadStripes[id & (mThreadStripes.length - 1)] = stripe;
                add(slot, delta, stripe);
            }

            return stripe;
        }

        private void add(int slot, int delta, int stripe) {
            UNSAFE.getAndAddInt(mCounters, intArrayByteOffset(slot + stripe), delta);
        }

        private int threadStripe() {
            int id = xorshift((int) Thread.currentThread().getId());
            return mThreadStripes[id & (mThreadStripes.length - 1)];
        }

        private static int xorshift(int v) {
            v ^= v << 13;
            v ^= v >>> 17;
            v ^= v << 5;
            return v;
        }

        private boolean isZero(int slot) {
            int[] counters = mCounters;
            int stride = mSlots.length;
            long sum = 0;
            for (int i = 0; i < mCores; i++, slot += stride) {
                sum += UNSAFE.getIntVolatile(counters, intArrayByteOffset(slot));
            }
            return sum == 0;
        }

        private static Object get(Object[] array, int i) {
            return UNSAFE.getObjectVolatile(array, objectArrayByteOffset(i));
        }

        private static void set(Object[] array, int i, Object value) {
            UNSAFE.putObjectVolatile(array, objectArrayByteOffset(i), value);
        }

        private static boolean compareAndSet(Object[] array, int i, Object expect, Object update) {
            return UNSAFE.compareAndSwapObject(array, objectArrayByteOffset(i), expect, update);
        }

        private static long objectArrayByteOffset(int i) {
            return ((long) i << OBJECT_ARRAY_SHIFT) + OBJECT_ARRAY_BASE;
        }

        private static long intArrayByteOffset(int i) {
            return ((long) i << INT_ARRAY_SHIFT) + INT_ARRAY_BASE;
        }
    }
}
