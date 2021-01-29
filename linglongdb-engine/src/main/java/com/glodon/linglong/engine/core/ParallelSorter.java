package com.glodon.linglong.engine.core;

import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.engine.core.frame.Scanner;
import com.glodon.linglong.engine.core.frame.Sorter;
import com.glodon.linglong.engine.core.lock.CommitLock;
import com.glodon.linglong.engine.core.page.DirectPageOps;
import com.glodon.linglong.engine.core.tx.Transaction;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Stereo
 */
public final class ParallelSorter implements Sorter, Node.Supplier {
    private static final int MIN_SORTTreeS = 8;
    private static final int MAX_SORTTreeS = 64;

    private static final int LEVEL_MIN_SIZE = 8;
    private static final int L0_MAX_SIZE = 256;
    private static final int L1_MAX_SIZE = 1024;

    private static final int MERGE_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private static final int S_READY = 0, S_FINISHING = 1, S_EXCEPTION = 2, S_RESET = 3;

    private final LocalDatabase mDatabase;
    private final Executor mExecutor;

    private Tree[] mSortTrees;
    private int mSortTreesSize;

    private Tree[] mSortTreePool;
    private int mSortTreePoolSize;

    private Merger mLastMerger;
    private int mMergerCount;

    private volatile List<Level> mSortTreeLevels;

    private LongAdder mFinishCounter;
    private long mFinishCount;

    private int mState;
    private Throwable mException;

    public ParallelSorter(LocalDatabase db, Executor executor) {
        mDatabase = db;
        mExecutor = executor;
    }

    private static final class Level {
        final int mLevelNum;
        Tree[] mTrees;
        int mSize;
        TreeMerger mMerger;
        boolean mStopped;

        Level(int levelNum) {
            mLevelNum = levelNum;
            mTrees = new Tree[LEVEL_MIN_SIZE];
        }

        synchronized void stop() {
            mStopped = true;
            if (mMerger != null) {
                mMerger.stop();
            }
        }

        void waitUntilFinished() throws InterruptedIOException {
            try {
                while (mMerger != null) {
                    wait();
                }
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            }
        }

        synchronized Tree waitForFirstTree() throws InterruptedIOException {
            waitUntilFinished();
            return mTrees[0];
        }

        synchronized void finished(TreeMerger merger) {
            if (merger == mMerger) {
                mMerger = null;
                notifyAll();
            }
        }
    }

    @Override
    public synchronized void add(byte[] key, byte[] value) throws IOException {
        CommitLock.Shared shared = mDatabase.commitLock().acquireShared();
        try {
            Node node;
            if (mSortTreesSize == 0) {
                Tree sortTree = allocSortTree();
                (mSortTrees = new Tree[MIN_SORTTreeS])[0] = sortTree;
                mSortTreesSize = 1;
                node = sortTree.mRoot;
            } else {
                Tree sortTree = mSortTrees[mSortTreesSize - 1];
                node = latchRootDirty(sortTree);
            }

            try {
                node = Node.appendToSortLeaf(node, mDatabase, key, value, this);
            } finally {
                node.releaseExclusive();
            }
        } catch (Throwable e) {
            exception(e);
            throw e;
        } finally {
            shared.release();
        }

        if (mSortTreesSize >= MAX_SORTTreeS) {
            mergeSortTrees();
        }
    }

    @Override
    public Tree finish() throws IOException {
        try {
            Tree tree = doFinish(null);
            finishComplete();
            return tree;
        } catch (Throwable e) {
            try {
                reset();
            } catch (Exception e2) {
                e.addSuppressed(e2);
            }
            throw e;
        }
    }

    @Override
    public Scanner finishScan() throws IOException {
        return finishScan(new SortScanner(mDatabase));
    }

    @Override
    public Scanner finishScanReverse() throws IOException {
        return finishScan(new SortReverseScanner(mDatabase));
    }

    private Scanner finishScan(SortScanner scanner) throws IOException {
        try {
            Tree tree = doFinish(scanner);
            if (tree != null) {
                finishComplete();
                scanner.ready(tree);
            }
            return scanner;
        } catch (Throwable e) {
            try {
                reset();
            } catch (Exception e2) {
                e.addSuppressed(e2);
            }
            throw e;
        }
    }

    /**
     * @param scanner pass null to always wait to finish
     */
    private Tree doFinish(SortScanner scanner) throws IOException {
        Level finishLevel;

        synchronized (this) {
            checkState();

            mState = S_FINISHING;
            mFinishCount = 0;

            try {
                while (mMergerCount != 0) {
                    wait();
                }
                if (mLastMerger != null) {
                    throw new AssertionError();
                }
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            }

            Level[] levels = stopTreeMergers();
            int numLevelTrees = 0;

            if (levels != null) {
                for (Level level : levels) {
                    synchronized (level) {
                        numLevelTrees += level.mSize;
                    }
                }
            }

            final Tree[] sortTrees = mSortTrees;
            final int size = mSortTreesSize;
            mSortTrees = null;
            mSortTreesSize = 0;

            Tree[] allTrees;

            if (size == 0) {
                if (numLevelTrees == 0) {
                    return mDatabase.newTemporaryIndex();
                }
                if (numLevelTrees == 1) {
                    return levels[0].mTrees[0];
                }
                allTrees = new Tree[numLevelTrees];
            } else {
                Tree tree;
                if (size == 1) {
                    tree = sortTrees[0];
                    CommitLock.Shared shared = mDatabase.commitLock().acquireShared();
                    Node node;
                    try {
                        node = latchRootDirty(tree);
                    } finally {
                        shared.release();
                    }
                    node.sortLeaf();
                    node.releaseExclusive();
                } else {
                    tree = mDatabase.newTemporaryIndex();
                    doMergeSortTrees(null, sortTrees, size, tree);
                }

                if (numLevelTrees == 0) {
                    return tree;
                }

                allTrees = new Tree[numLevelTrees + 1];
                allTrees[numLevelTrees] = tree;
            }

            for (int i = levels.length, pos = 0; --i >= 0; ) {
                Level level = levels[i];
                System.arraycopy(level.mTrees, 0, allTrees, pos, level.mSize);
                pos += level.mSize;
            }

            for (int i = mSortTreeLevels.size(); --i >= 1; ) {
                mSortTreeLevels.remove(i);
            }

            finishLevel = levels[0];
            levels = null;
            finishLevel.mSize = 0;
            TreeMerger merger = newTreeMerger(allTrees, finishLevel, finishLevel);
            finishLevel.mMerger = merger;
            merger.start();

            mFinishCounter = merger;
        }

        if (scanner == null) {
            return finishLevel.waitForFirstTree();
        }

        scanner.notReady(new SortScanner.Supplier() {
            @Override
            public Tree get() throws IOException {
                try {
                    Tree tree = finishLevel.waitForFirstTree();
                    finishComplete();
                    return tree;
                } catch (Throwable e) {
                    try {
                        reset();
                    } catch (Exception e2) {
                        e.addSuppressed(e2);
                    }
                    throw e;
                }
            }

            @Override
            public void close() throws IOException {
                reset();
            }
        });

        return null;
    }

    private Level[] stopTreeMergers() throws InterruptedIOException {
        List<Level> list = mSortTreeLevels;

        if (list == null) {
            return null;
        }

        while (true) {
            Level[] levels;
            synchronized (list) {
                levels = list.toArray(new Level[list.size()]);
            }

            for (Level level : levels) {
                level.stop();
            }

            for (Level level : levels) {
                synchronized (level) {
                    level.waitUntilFinished();
                }
            }

            synchronized (list) {
                if (list.size() <= levels.length) {
                    return levels;
                }
            }
        }
    }

    private synchronized void finishComplete() throws IOException {
        mSortTreeLevels = null;

        if (mFinishCounter != null) {
            mFinishCount = mFinishCounter.sum();
            mFinishCounter = null;
        }

        if (mSortTreePoolSize > 0) {
            do {
                Tree tree = mSortTreePool[--mSortTreePoolSize];
                mSortTreePool[mSortTreePoolSize] = null;
                mDatabase.quickDeleteTemporaryTree(tree);
            } while (mSortTreePoolSize > 0);
        }

        if (mState == S_EXCEPTION) {
            checkState();
        }

        mState = S_READY;
        mException = null;
    }

    @Override
    public synchronized long progress() {
        return mFinishCounter != null ? mFinishCounter.sum() : mFinishCount;
    }

    @Override
    public void reset() throws IOException {
        List<Tree> toDrop = null;

        synchronized (this) {
            mState = S_RESET;
            mFinishCounter = null;
            mFinishCount = 0;

            try {
                while (mMergerCount != 0) {
                    wait();
                }
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            }

            Level[] levels = stopTreeMergers();

            if (levels != null) {
                for (Level level : levels) {
                    Tree[] trees;
                    int size;
                    synchronized (level) {
                        trees = level.mTrees;
                        size = level.mSize;
                        level.mSize = 0;
                    }
                    if (size != 0) {
                        if (toDrop == null) {
                            toDrop = new ArrayList<>();
                        }
                        for (int i = 0; i < size; i++) {
                            toDrop.add(trees[i]);
                        }
                    }
                }

                mSortTreeLevels = null;
            }

            Tree[] sortTrees = mSortTrees;
            int size = mSortTreesSize;
            mSortTrees = null;
            mSortTreesSize = 0;

            if (size != 0) {
                if (toDrop == null) {
                    toDrop = new ArrayList<>();
                }
                for (int i = 0; i < size; i++) {
                    toDrop.add(sortTrees[i]);
                }
            }
        }

        if (toDrop != null) for (Tree tree : toDrop) {
            tree.drop(false).run();
        }

        finishComplete();
    }

    @Override
    public Node newNode() throws IOException {
        if (mSortTreesSize >= mSortTrees.length) {
            mSortTrees = Arrays.copyOf(mSortTrees, MAX_SORTTreeS);
        }
        Tree sortTree = allocSortTree();
        mSortTrees[mSortTreesSize++] = sortTree;
        return sortTree.mRoot;
    }

    private Tree allocSortTree() throws IOException {
        checkState();

        Tree tree;
        Node root;

        int size = mSortTreePoolSize;
        if (size > 0) {
            tree = mSortTreePool[--size];
            mSortTreePoolSize = size;
            root = latchRootDirty(tree);
        } else {
            tree = mDatabase.newTemporaryTree(true);
            root = tree.mRoot;
        }

        root.asSortLeaf();
        return tree;
    }

    private Node latchRootDirty(Tree tree) throws IOException {
        Node root = tree.mRoot;
        root.acquireExclusive();
        try {
            mDatabase.markDirty(tree, root);
            return root;
        } catch (Throwable e) {
            root.releaseExclusive();
            throw e;
        }
    }

    private void mergeSortTrees() throws IOException {
        final Tree dest = mDatabase.newTemporaryIndex();

        final Tree[] sortTrees = mSortTrees;
        final int size = mSortTreesSize;
        mSortTrees = new Tree[MAX_SORTTreeS];
        mSortTreesSize = 0;

        if (mSortTreeLevels == null) {
            mSortTreeLevels = new ArrayList<>();
        }

        Merger merger = new Merger(mLastMerger, sortTrees, size, dest);
        mLastMerger = merger;

        try {
            while (mMergerCount >= MERGE_THREAD_COUNT) {
                wait();
            }
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }

        mMergerCount++;
        mExecutor.execute(merger);
    }

    private final class Merger implements Runnable {
        private Tree[] mSortTrees;
        private int mSize;
        private Tree mDest;

        Merger mPrev;

        Merger mNext;

        Merger(Merger prev, Tree[] sortTrees, int size, Tree dest) {
            mPrev = prev;
            mSortTrees = sortTrees;
            mSize = size;
            mDest = dest;
        }

        @Override
        public void run() {
            try {
                doMergeSortTrees(this, mSortTrees, mSize, mDest);
            } catch (Throwable e) {
                exception(e);
            }
        }
    }

    private void doMergeSortTrees(Merger merger, Tree[] sortTrees, int size, Tree dest)
            throws IOException {
        Throwable ex = null;

        final TreeCursor appender = dest.newCursor(Transaction.BOGUS);
        try {
            appender.firstLeaf();

            final CommitLock commitLock = mDatabase.commitLock();
            CommitLock.Shared shared = commitLock.acquireShared();
            try {
                mDatabase.checkClosed();

                for (int i = 0; i < size; i++) {
                    Node node = latchRootDirty(sortTrees[i]);
                    node.sortLeaf();
                    node.garbage(i << 1);
                }

                for (int i = size >>> 1; --i >= 0; ) {
                    siftDown(sortTrees, size, i, sortTrees[i]);
                }

                if (commitLock.hasQueuedThreads()) {
                    shared.release();
                    shared = commitLock.acquireShared();
                    for (int i = 0; i < size; i++) {
                        Tree sortTree = sortTrees[i];
                        mDatabase.markDirty(sortTree, sortTree.mRoot);
                    }
                }

                int len = size;

                while (true) {
                    Tree sortTree = sortTrees[0];
                    Node node = sortTree.mRoot;

                    int order = node.garbage();
                    if ((order & 1) == 0) {
                        appender.appendTransfer(node);
                    } else {
                        node.deleteFirstSortLeafEntry();
                        node.garbage(order & ~1);
                    }

                    if (!node.hasKeys()) {
                        len--;
                        if (len == 0) {
                            break;
                        }
                        Tree last = sortTrees[len];
                        sortTrees[len] = sortTree;
                        sortTree = last;
                    }

                    siftDown(sortTrees, len, 0, sortTree);
                }
            } finally {
                shared.release();
            }
        } catch (Throwable e) {
            ex = e;
        } finally {
            appender.reset();
        }

        for (int i = 0; i < size; i++) {
            sortTrees[i].mRoot.releaseExclusive();
        }

        synchronized (this) {
            if (mSortTreePool == null || mSortTreePoolSize == 0) {
                mSortTreePool = sortTrees;
                mSortTreePoolSize = size;
            } else {
                int totalSize = mSortTreePoolSize + size;
                if (totalSize > mSortTreePool.length) {
                    mSortTreePool = Arrays.copyOf(mSortTreePool, totalSize);
                }
                System.arraycopy(sortTrees, 0, mSortTreePool, mSortTreePoolSize, size);
                mSortTreePoolSize = totalSize;
            }

            if (merger != null) {
                merger.mSortTrees = null;
                merger.mSize = 0;

                Merger prev = merger.mPrev;
                if (prev != null && prev.mDest != null) {
                    prev.mNext = merger;
                    return;
                }

                while (true) {
                    addToLevel(selectLevel(0), L0_MAX_SIZE, merger.mDest);
                    merger.mDest = null;
                    merger.mPrev = null;
                    mMergerCount--;

                    Merger next = merger.mNext;
                    if (next == null) {
                        if (mLastMerger == merger) {
                            if (mMergerCount != 0) {
                                throw new AssertionError();
                            }
                            mLastMerger = null;
                        }
                        break;
                    } else {
                        merger = next;
                    }
                }

                notifyAll();
            }
        }

        if (ex != null) {
            Utils.rethrow(ex);
        }
    }

    private static void siftDown(Tree[] sortTrees, int size, int pos, Tree element)
            throws IOException {
        int half = size >>> 1;
        while (pos < half) {
            int childPos = (pos << 1) + 1;
            Tree child = sortTrees[childPos];
            int rightPos = childPos + 1;
            if (rightPos < size && compareSortTrees(child, sortTrees[rightPos]) > 0) {
                childPos = rightPos;
                child = sortTrees[childPos];
            }
            if (compareSortTrees(element, child) <= 0) {
                break;
            }
            sortTrees[pos] = child;
            pos = childPos;
        }
        sortTrees[pos] = element;
    }

    private static int compareSortTrees(Tree leftTree, Tree rightTree) throws IOException {
        Node left = leftTree.mRoot;
        Node right = rightTree.mRoot;

        int compare = Node.compareKeys
                (left, DirectPageOps.p_ushortGetLE(left.mPage, left.searchVecStart()),
                        right, DirectPageOps.p_ushortGetLE(right.mPage, right.searchVecStart()));

        if (compare == 0) {

            int leftOrder = left.garbage();
            int rightOrder = right.garbage();

            if (leftOrder < rightOrder) {
                left.garbage(leftOrder | 1);
                compare = -1;
            } else {
                right.garbage(rightOrder | 1);
                compare = 1;
            }
        }

        return compare;
    }

    private Level selectLevel(int levelNum) {
        return selectLevel(levelNum, mSortTreeLevels);
    }

    private static Level selectLevel(int levelNum, List<Level> levels) {
        synchronized (levels) {
            if (levelNum < levels.size()) {
                return levels.get(levelNum);
            }
            Level level = new Level(levelNum);
            levels.add(level);
            return level;
        }
    }

    private void addToLevel(Level level, int maxSize, Tree tree) {
        TreeMerger merger;

        try {
            synchronized (level) {
                Tree[] trees = level.mTrees;
                int size = level.mSize;

                if (size >= trees.length) {
                    level.mTrees = trees = Arrays.copyOfRange(trees, 0, trees.length << 1);
                }

                trees[size++] = tree;

                if (size < maxSize || level.mStopped) {
                    level.mSize = size;
                    return;
                }

                Level nextLevel = selectLevel(level.mLevelNum + 1);
                level.mSize = 0;
                trees = trees.clone();

                merger = newTreeMerger(trees, level, nextLevel);

                level.waitUntilFinished();

                level.mMerger = merger;
            }
        } catch (Throwable e) {
            exception(e);
            return;
        }

        try {
            merger.start();
        } catch (Throwable e) {
            level.finished(merger);
            throw e;
        }
    }

    private TreeMerger newTreeMerger(Tree[] trees, Level level, Level nextLevel) {
        return new TreeMerger(mDatabase, trees, mExecutor, MERGE_THREAD_COUNT) {
            @Override
            protected void merged(Tree tree) {
                addToLevel(nextLevel, L1_MAX_SIZE, tree);
            }

            @Override
            protected void remainder(Tree tree) {
                if (tree != null) {
                    addToLevel(nextLevel, L1_MAX_SIZE, tree);
                } else {
                    Throwable ex = exceptionCheck();
                    if (ex != null) {
                        exception(ex);
                    }
                    level.finished(this);
                }
            }
        };
    }

    private void checkState() throws InterruptedIOException {
        if (mState != S_READY) {
            switch (mState) {
                case S_FINISHING:
                    throw new IllegalStateException("Finish in progress");
                case S_EXCEPTION:
                    Throwable e = mException;
                    if (e != null) {
                        Utils.addLocalTrace(e);
                        throw Utils.rethrow(e);
                    }
            }
            throw new InterruptedIOException("Sorter is reset");
        }
    }

    private synchronized void exception(Throwable e) {
        if (mException == null) {
            mException = e;
        }
        mState = S_EXCEPTION;
    }
}
