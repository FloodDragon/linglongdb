package com.glodon.linglong.base.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 用于运行任务。 此类不是线程安全的排队任务，因此调用方必须提供自己的互斥功能，以防止并发排队。
 *
 * @author Stereo
 */
public abstract class WorkerGroup {
    /**
     * @param workerCount   number of workers
     * @param maxSize       maximum amount of tasks which can be enqueued per worker
     * @param keepAliveTime maximum idle time before worker threads exit
     * @param unit          keepAliveTime time unit per worker
     * @param threadFactory null for default
     */
    public static WorkerGroup make(int workerCount, int maxSize,
                                   long keepAliveTime, TimeUnit unit,
                                   ThreadFactory threadFactory) {
        if (workerCount < 1) {
            throw new IllegalArgumentException();
        }

        if (threadFactory == null) {
            threadFactory = Executors.defaultThreadFactory();
        }

        if (workerCount == 1) {
            return new One(Worker.make(maxSize, keepAliveTime, unit, threadFactory));
        }

        return new Many(workerCount, maxSize, keepAliveTime, unit, threadFactory);
    }

    public abstract Worker tryEnqueue(Worker.Task task);

    public abstract Worker enqueue(Worker.Task task);

    public abstract void join(boolean interrupt);

    private static final class One extends WorkerGroup {
        private final Worker mWorker;

        One(Worker worker) {
            mWorker = worker;
        }

        @Override
        public Worker tryEnqueue(Worker.Task task) {
            mWorker.tryEnqueue(task);
            return mWorker;
        }

        @Override
        public Worker enqueue(Worker.Task task) {
            mWorker.enqueue(task);
            return mWorker;
        }

        @Override
        public void join(boolean interrupt) {
            mWorker.join(interrupt);
        }
    }

    private static final class Many extends WorkerGroup {
        private final Worker[] mWorkers;
        private int mLastSelected;

        Many(int workerCount, int maxSize,
             long keepAliveTime, TimeUnit unit,
             ThreadFactory threadFactory) {
            Worker[] workers = new Worker[workerCount];

            for (int i = 0; i < workers.length; i++) {
                workers[i] = Worker.make(maxSize, keepAliveTime, unit, threadFactory);
            }

            mWorkers = workers;
        }

        @Override
        public Worker tryEnqueue(Worker.Task task) {
            int slot = Math.max(0, mLastSelected - 1);

            for (int i = 0; i < mWorkers.length; i++) {
                Worker w = mWorkers[slot];
                if (w.tryEnqueue(task)) {
                    mLastSelected = slot;
                    return w;
                }
                slot++;
                if (slot >= mWorkers.length) {
                    slot = 0;
                }
            }

            return null;
        }

        @Override
        public Worker enqueue(Worker.Task task) {
            Worker w = tryEnqueue(task);
            if (w == null) {
                w = mWorkers[mLastSelected = ThreadLocalRandom.current().nextInt(mWorkers.length)];
                w.enqueue(task);
            }
            return w;
        }

        @Override
        public void join(boolean interrupt) {
            for (Worker w : mWorkers) {
                w.join(interrupt);
            }
        }
    }
}
