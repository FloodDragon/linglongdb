package com.linglong.replication;

import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author Stereo
 */
final class Scheduler {
    private final ExecutorService mExecutor;
    private final PriorityQueue<Delayed> mDelayed;

    private boolean mRunning;

    Scheduler() {
        this(Executors.newCachedThreadPool());
    }

    Scheduler(ExecutorService executor) {
        if (executor == null) {
            throw new IllegalArgumentException();
        }
        mExecutor = executor;
        mDelayed = new PriorityQueue<>();
    }

    public void shutdown() {
        mExecutor.shutdown();
        synchronized (this) {
            mDelayed.clear();
            notify();
        }
    }

    public boolean isShutdown() {
        return mExecutor.isShutdown();
    }

    public boolean execute(Runnable task) {
        try {
            mExecutor.execute(task);
        } catch (RejectedExecutionException e) {
            if (isShutdown()) {
                return false;
            }
            schedule(task, 1);
        }
        return true;
    }

    public boolean schedule(Runnable task, long delayMillis) {
        return schedule(new Delayed.Runner(System.currentTimeMillis() + delayMillis, task));
    }

    public synchronized boolean schedule(Delayed delayed) {
        mDelayed.add(delayed);

        if (!mRunning) {
            if (!doExecute(this::runDelayedTasks)) {
                return false;
            }
            mRunning = true;
        } else if (mDelayed.peek() == delayed) {
            notify();
        }

        return true;
    }

    private boolean doExecute(Runnable task) {
        while (true) {
            try {
                mExecutor.execute(task);
                return true;
            } catch (RejectedExecutionException e) {
                if (isShutdown()) {
                    return false;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }

    private void runDelayedTasks() {
        while (true) {
            Delayed delayed;
            synchronized (this) {
                while (true) {
                    delayed = mDelayed.peek();
                    if (delayed == null) {
                        mRunning = false;
                        return;
                    }
                    long delay = delayed.mCounter - System.currentTimeMillis();
                    if (delay <= 0) {
                        break;
                    }
                    try {
                        wait(delay);
                    } catch (InterruptedException e) {
                        if (isShutdown()) {
                            mRunning = false;
                            return;
                        }
                    }
                }

                if (delayed != mDelayed.remove()) {
                    mRunning = false;
                    throw new AssertionError();
                }
            }

            try {
                doExecute(delayed);
            } catch (Throwable e) {
                synchronized (this) {
                    mRunning = false;
                }
                throw e;
            }
        }
    }
}
