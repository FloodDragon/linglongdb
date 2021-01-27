package com.glodon.linglong.base.concurrent;

import com.glodon.linglong.base.common.IOUtils;
import com.glodon.linglong.base.common.UnsafeAccess;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 简单任务工作者，最多具有一个后台线程，并且预期只有一个线程入队任务。
 * 此类对于排队任务不是线程安全的，因此调用者必须提供自己的互斥功能以防止并发排队。
 *
 * @author Stereo
 */
@SuppressWarnings("restriction")
public class Worker {
    /**
     * @param maxSize       maximum amount of tasks which can be enqueued
     * @param keepAliveTime maximum idle time before worker thread exits
     * @param unit          keepAliveTime time unit
     * @param threadFactory null for default
     */
    public static Worker make(int maxSize, long keepAliveTime, TimeUnit unit,
                              ThreadFactory threadFactory) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException();
        }

        if (threadFactory == null) {
            threadFactory = Executors.defaultThreadFactory();
        }

        return new Worker(maxSize, keepAliveTime, unit, threadFactory);
    }

    private static final sun.misc.Unsafe UNSAFE = UnsafeAccess.obtain();

    static final long SIZE_OFFSET, FIRST_OFFSET, LAST_OFFSET, STATE_OFFSET, THREAD_OFFSET;

    static {
        try {
            // Reduce the risk of "lost unpark" due to classloading.
            // https://bugs.openjdk.java.net/browse/JDK-8074773
            Class<?> clazz = LockSupport.class;

            SIZE_OFFSET = UNSAFE.objectFieldOffset(Worker.class.getDeclaredField("mSize"));
            FIRST_OFFSET = UNSAFE.objectFieldOffset(Worker.class.getDeclaredField("mFirst"));
            LAST_OFFSET = UNSAFE.objectFieldOffset(Worker.class.getDeclaredField("mLast"));
            STATE_OFFSET = UNSAFE.objectFieldOffset(Worker.class.getDeclaredField("mThreadState"));
            THREAD_OFFSET = UNSAFE.objectFieldOffset(Worker.class.getDeclaredField("mThread"));
        } catch (Throwable e) {
            throw IOUtils.rethrow(e);
        }
    }

    private final ThreadFactory mThreadFactory;
    private final int mMaxSize;
    private final long mKeepAliveNanos;

    private volatile int mSize;
    private volatile Task mFirst;
    private volatile Task mLast;

    private static final int
            THREAD_NONE = 0,    // no worker thread
            THREAD_RUNNING = 1, // worker thread is running
            THREAD_BLOCKED = 2, // worker thread is running and an enqueue/join thread is blocked
            THREAD_IDLE = 3;    // worker thread is idle

    private volatile int mThreadState;
    private volatile Thread mThread;

    private Thread mWaiter;

    private Worker(int maxSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
        mThreadFactory = threadFactory;

        mMaxSize = maxSize;

        if (keepAliveTime > 0) {
            mKeepAliveNanos = unit.toNanos(keepAliveTime);
        } else {
            mKeepAliveNanos = keepAliveTime;
        }
    }

    public boolean tryEnqueue(Task task) {
        if (task == null) {
            throw new NullPointerException();
        }

        int size = mSize;
        if (size >= mMaxSize) {
            return false;
        }

        if (!UNSAFE.compareAndSwapInt(this, SIZE_OFFSET, size, size + 1)) {
            UNSAFE.getAndAddInt(this, SIZE_OFFSET, 1);
        }

        Task prev = (Task) UNSAFE.getAndSetObject(this, LAST_OFFSET, task);
        if (prev == null) {
            mFirst = task;
        } else {
            prev.mNext = task;
        }

        while (true) {
            int state = mThreadState;

            if (state == THREAD_RUNNING) {
                return true;
            }

            if (state == THREAD_NONE) {
                mThreadState = THREAD_RUNNING;
                Thread t;
                try {
                    t = mThreadFactory.newThread(this::runTasks);
                    t.start();
                } catch (Throwable e) {
                    UNSAFE.getAndAddInt(this, SIZE_OFFSET, -1);
                    mThreadState = THREAD_NONE;
                    throw e;
                }
                mThread = t;
                return true;
            }

            // assert state == THREAD_IDLE

            if (UNSAFE.compareAndSwapInt(this, STATE_OFFSET, state, THREAD_RUNNING)) {
                LockSupport.unpark(mThread);
                return true;
            }
        }
    }

    public void enqueue(Task task) {
        while (!tryEnqueue(task)) {
            // Keep trying before parking.
            for (int i = 1; i < Latch.SPIN_LIMIT; i++) {
                if (tryEnqueue(task)) {
                    return;
                }
            }
            Thread.yield();
            if (tryEnqueue(task)) {
                return;
            }
            mWaiter = Thread.currentThread();
            if (UNSAFE.compareAndSwapInt(this, STATE_OFFSET, THREAD_RUNNING, THREAD_BLOCKED)) {
                LockSupport.park(this);
            }
            mWaiter = null;
        }
    }

    public void join(boolean interrupt) {
        while (mSize > 0) {
            for (int i = 1; i < Latch.SPIN_LIMIT; i++) {
                if (mSize <= 0) {
                    break;
                }
            }
            Thread.yield();
            if (mSize <= 0) {
                break;
            }
            mWaiter = Thread.currentThread();
            if (UNSAFE.compareAndSwapInt(this, STATE_OFFSET, THREAD_RUNNING, THREAD_BLOCKED)) {
                LockSupport.park(this);
            }
            mWaiter = null;
        }

        if (interrupt) {
            Thread t = mThread;
            if (t != null) {
                t.interrupt();
            }
        }
    }

    public static abstract class Task {
        volatile Task mNext;

        public abstract void run() throws Throwable;
    }

    private void runTasks() {
        int size = 0;

        outer:
        while (true) {
            if (size > 0 || (size = mSize) > 0) {
                Task task;
                while ((task = mFirst) == null) ;

                Task next;
                while (true) {
                    next = task.mNext;
                    if (next != null) {
                        mFirst = next;
                        break;
                    } else {
                        if (task == mLast &&
                                UNSAFE.compareAndSwapObject(this, LAST_OFFSET, task, null)) {
                            UNSAFE.compareAndSwapObject(this, FIRST_OFFSET, task, null);
                            break;
                        }
                    }
                }

                try {
                    task.run();
                } catch (Throwable e) {
                    IOUtils.uncaught(e);
                }

                size = UNSAFE.getAndAddInt(this, SIZE_OFFSET, -1) - 1;

                if (mThreadState == THREAD_BLOCKED) {
                    mThreadState = THREAD_RUNNING;
                    LockSupport.unpark(mWaiter);
                }

                continue;
            }

            for (int i = 0; i < Latch.SPIN_LIMIT; i++) {
                if ((size = mSize) > 0) {
                    continue outer;
                }
                if (mThreadState == THREAD_BLOCKED) {
                    mThreadState = THREAD_RUNNING;
                    LockSupport.unpark(mWaiter);
                }
            }

            Thread.yield();

            if ((size = mSize) > 0) {
                continue;
            }

            if (!UNSAFE.compareAndSwapInt(this, STATE_OFFSET, THREAD_RUNNING, THREAD_IDLE)) {
                continue;
            }

            long parkNanos = mKeepAliveNanos;
            long endNanos = parkNanos < 0 ? 0 : (System.nanoTime() + parkNanos);

            while ((size = mSize) <= 0) {
                if (parkNanos < 0) {
                    LockSupport.park(this);
                } else {
                    LockSupport.parkNanos(this, parkNanos);
                    parkNanos = Math.max(0, endNanos - System.nanoTime());
                }

                boolean interrupted = Thread.interrupted();

                if ((size = mSize) > 0) {
                    break;
                }

                if (parkNanos == 0 || interrupted) {
                    if (!UNSAFE.compareAndSwapInt(this, STATE_OFFSET, THREAD_IDLE, THREAD_NONE)) {
                        continue outer;
                    }
                    UNSAFE.compareAndSwapObject(this, THREAD_OFFSET, Thread.currentThread(), null);
                    return;
                }

                if (mThreadState != THREAD_IDLE) {
                    continue outer;
                }
            }

            UNSAFE.compareAndSwapInt(this, STATE_OFFSET, THREAD_IDLE, THREAD_RUNNING);
        }
    }
}
