package com.linglong.engine.core;

import com.linglong.base.common.Utils;
import com.linglong.engine.core.frame.Chain;
import com.linglong.engine.core.tx.Transaction;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Stereo
 */
abstract class TreeSeparator extends LongAdder {
    protected final LocalDatabase mDatabase;
    protected final Tree[] mSources;
    protected final Executor mExecutor;

    private final int mWorkerCount;
    private final Worker[] mWorkerHashtable;

    private Worker mFirstWorker;

    private volatile Throwable mException;

    static final AtomicReferenceFieldUpdater<TreeSeparator, Throwable> cExceptionUpdater =
            AtomicReferenceFieldUpdater.newUpdater(TreeSeparator.class, Throwable.class, "mException");

    static final AtomicIntegerFieldUpdater<Worker> cSpawnCountUpdater =
            AtomicIntegerFieldUpdater.newUpdater(Worker.class, "mSpawnCount");

    TreeSeparator(LocalDatabase db, Tree[] sources, Executor executor, int workerCount) {
        if (db == null || sources.length <= 0 || workerCount <= 0) {
            throw new IllegalArgumentException();
        }
        if (executor == null) {
            workerCount = 1;
        }
        mDatabase = db;
        mSources = sources;
        mExecutor = executor;
        mWorkerCount = workerCount;
        mWorkerHashtable = new Worker[Utils.roundUpPower2(workerCount)];
    }

    public void start() {
        startWorker(null, mWorkerCount - 1, null, null);
    }

    public void stop() {
        Worker[] hashtable = mWorkerHashtable;

        synchronized (hashtable) {
            for (int slot = 0; slot < hashtable.length; slot++) {
                for (Worker w = hashtable[slot]; w != null; ) {
                    while (true) {
                        int spawnCount = w.mSpawnCount;
                        if (cSpawnCountUpdater.compareAndSet
                                (w, spawnCount, spawnCount | (1 << 31))) {
                            break;
                        }
                    }

                    w = w.mHashtableNext;
                }
            }
        }
    }

    public Throwable exceptionCheck() {
        return mException;
    }

    protected void failed(Throwable cause) {
        cExceptionUpdater.compareAndSet(this, null, cause);
        stop();
    }

    protected abstract void finished(Chain<Tree> firstRange);

    private void startWorker(Worker from, int spawnCount, byte[] lowKey, byte[] highKey) {
        Worker worker = new Worker(spawnCount, lowKey, highKey, mSources.length);

        Worker[] hashtable = mWorkerHashtable;
        int slot = worker.mHash & (hashtable.length - 1);

        synchronized (hashtable) {
            if (from != null && from.mSpawnCount < 0) {
                worker.mSpawnCount = spawnCount | (1 << 31);
            }
            worker.mHashtableNext = hashtable[slot];
            hashtable[slot] = worker;

            if (from == null) {
                mFirstWorker = worker;
            } else {
                worker.mPrev = from;
                Worker next = from.mNext;
                from.mNext = worker;
                if (next != null) {
                    worker.mNext = next;
                    next.mPrev = worker;
                }
            }
        }

        if (mExecutor == null) {
            worker.run();
        } else {
            mExecutor.execute(worker);
        }
    }

    private TreeCursor openSourceCursor(int sourceSlot, byte[] lowKey) throws IOException {
        TreeCursor scursor = mSources[sourceSlot].newCursor(Transaction.BOGUS);
        scursor.mKeyOnly = true;
        if (lowKey == null) {
            scursor.first();
        } else {
            scursor.findGe(lowKey);
        }
        return scursor;
    }

    private byte[] selectSplitKey(byte[] lowKey, byte[] highKey) throws IOException {
        Tree source = mSources[ThreadLocalRandom.current().nextInt(mSources.length)];

        TreeCursor scursor = source.newCursor(Transaction.BOGUS);
        try {
            scursor.mKeyOnly = true;
            scursor.random(lowKey, highKey);
            return scursor.key();
        } finally {
            scursor.reset();
        }
    }

    private void workerFinished(Worker worker) {
        Worker first;
        Worker[] hashtable = mWorkerHashtable;
        int slot = worker.mHash & (hashtable.length - 1);

        synchronized (hashtable) {
            for (Worker w = hashtable[slot], prev = null; ; ) {
                Worker next = w.mHashtableNext;
                if (w == worker) {
                    if (prev == null) {
                        hashtable[slot] = next;
                    } else {
                        prev.mHashtableNext = next;
                    }
                    break;
                } else {
                    prev = w;
                    w = next;
                }
            }

            int addCount = 1 + (worker.mSpawnCount & ~(1 << 31));

            if (addCount < mWorkerCount) {
                int randomSlot = ThreadLocalRandom.current().nextInt(hashtable.length);
                while (true) {
                    Worker w = hashtable[randomSlot];
                    if (w != null) {
                        cSpawnCountUpdater.getAndAdd(w, addCount);
                        return;
                    }
                    randomSlot++;
                    if (randomSlot >= hashtable.length) {
                        randomSlot = 0;
                    }
                }
            }

            first = mFirstWorker;
            mFirstWorker = null;
        }

        finished(first);
    }

    private final class Worker implements Runnable, Chain<Tree> {
        final int mHash;
        final byte[] mLowKey;
        byte[] mHighKey;
        final Selector[] mQueue;
        volatile int mSpawnCount;
        Worker mHashtableNext;
        private Tree mTarget;

        Worker mNext;
        Worker mPrev;

        Worker(int spawnCount, byte[] lowKey, byte[] highKey, int numSources) {
            mHash = ThreadLocalRandom.current().nextInt();
            mLowKey = lowKey;
            mHighKey = highKey;
            mQueue = new Selector[numSources];
            mSpawnCount = spawnCount;
        }

        @Override
        public void run() {
            try {
                doRun();
            } catch (Throwable e) {
                for (Selector s : mQueue) {
                    if (s != null) {
                        s.mSource.reset();
                    }
                }
                failed(e);
            }

            workerFinished(this);
        }

        @Override
        public Tree element() {
            return mTarget;
        }

        @Override
        public Worker next() {
            return mNext;
        }

        private void doRun() throws Exception {
            final Selector[] queue = mQueue;

            int queueSize = 0;
            for (int slot = 0; slot < queue.length; slot++) {
                TreeCursor scursor = openSourceCursor(slot, mLowKey);
                if (scursor.key() != null) {
                    queue[queueSize++] = new Selector(slot, scursor);
                }
            }

            if (queueSize == 0) {
                return;
            }

            for (int i = queueSize >>> 1; --i >= 0; ) {
                siftDown(queue, queueSize, i, queue[i]);
            }

            TreeCursor tcursor = null;
            byte[] highKey = mHighKey;
            byte count = 0;

            while (true) {
                Selector selector = queue[0];
                TreeCursor scursor = selector.mSource;

                transfer:
                {
                    if (highKey != null && Utils.compareUnsigned(scursor.key(), highKey) >= 0) {
                        scursor.reset();
                    } else {
                        if (selector.mSkip) {
                            scursor.store(null);
                            scursor.next();
                            selector.mSkip = false;
                        } else {
                            if (tcursor == null) {
                                mTarget = mDatabase.newTemporaryIndex();
                                tcursor = mTarget.newCursor(Transaction.BOGUS);
                                tcursor.mKeyOnly = true;
                                tcursor.firstLeaf();
                            }
                            tcursor.appendTransfer(scursor);
                            if (++count == 0) {
                                add(256);
                            }
                        }
                        if (scursor.key() != null) {
                            break transfer;
                        }
                    }

                    if (--queueSize == 0) {
                        break;
                    }

                    selector = queue[queueSize];
                    queue[queueSize] = null;
                }

                siftDown(queue, queueSize, 0, selector);

                int spawnCount = mSpawnCount;
                if (spawnCount != 0) {
                    if (spawnCount < 0) {
                        mHighKey = scursor.key();
                        for (int i = 0; i < queueSize; i++) {
                            queue[i].mSource.reset();
                        }
                        break;
                    }

                    byte[] splitKey = selectSplitKey(queue[0].mSource.key(), highKey);
                    trySplit:
                    if (splitKey != null) {
                        for (int i = 0; i < queueSize; i++) {
                            if (Arrays.equals(splitKey, queue[i].mSource.key())) {
                                break trySplit;
                            }
                        }

                        startWorker(this, 0, splitKey, highKey);
                        mHighKey = highKey = splitKey;
                        cSpawnCountUpdater.decrementAndGet(this);
                    }
                }
            }

            if (tcursor != null) {
                tcursor.reset();
            }

            add(count & 0xffL);
        }
    }

    private static final class Selector {
        final int mSourceSlot;
        final TreeCursor mSource;

        boolean mSkip;

        Selector(int slot, TreeCursor source) {
            mSourceSlot = slot;
            mSource = source;
        }

        int compareTo(Selector other) {
            int compare = Utils.compareUnsigned(this.mSource.key(), other.mSource.key());

            if (compare == 0) {
                // Favor the later source when duplicates are found.
                if (this.mSourceSlot < other.mSourceSlot) {
                    this.mSkip = true;
                    compare = -1;
                } else {
                    other.mSkip = true;
                    compare = 1;
                }
            }

            return compare;
        }
    }

    static void siftDown(Selector[] selectors, int size, int pos, Selector element) {
        int half = size >>> 1;
        while (pos < half) {
            int childPos = (pos << 1) + 1;
            Selector child = selectors[childPos];
            int rightPos = childPos + 1;
            if (rightPos < size && child.compareTo(selectors[rightPos]) > 0) {
                childPos = rightPos;
                child = selectors[childPos];
            }
            if (element.compareTo(child) <= 0) {
                break;
            }
            selectors[pos] = child;
            pos = childPos;
        }
        selectors[pos] = element;
    }
}
