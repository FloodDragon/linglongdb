package com.linglong.replication;

import com.linglong.base.common.Utils;

/**
 *
 * @author Stereo
 */
abstract class Delayed implements Comparable<Delayed>, Runnable {
    protected long mCounter;

    Delayed(long counter) {
        mCounter = counter;
    }

    @Override
    public int compareTo(Delayed other) {
        return Long.signum(mCounter - other.mCounter);
    }

    public final void run() {
        run(mCounter);
    }

    public final void run(long counter) {
        try {
            doRun(counter);
        } catch (Throwable e) {
            Utils.uncaught(e);
        }
    }

    protected abstract void doRun(long counter) throws Throwable;

    static final class Runner extends Delayed {
        private final Runnable mTask;

        Runner(long counter, Runnable task) {
            super(counter);
            mTask = task;
        }

        @Override
        protected void doRun(long counter) throws Throwable {
            mTask.run();
        }
    }
}
