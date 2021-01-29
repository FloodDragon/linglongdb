package com.glodon.linglong.engine.core;

import com.glodon.linglong.base.concurrent.Clutch;
import com.glodon.linglong.base.exception.DatabaseException;
import com.glodon.linglong.engine.core.page.DirectPageOps;
import com.glodon.linglong.engine.core.page.PageDb;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Stereo
 */
public final class NodeContext extends Clutch.Pack implements Checkpointer.DirtySet {
    public static final int MODE_UNEVICTABLE = 1;

    public static final int MODE_NO_EVICT = 2;

    private static final int PACK_SLOTS = 64;

    final LocalDatabase mDatabase;
    private final int mPageSize;
    private final long mUsedRate;

    private int mMaxSize;
    private int mSize;
    private Node mMostRecentlyUsed;
    private Node mLeastRecentlyUsed;

    private Node mFirstDirty;
    private Node mLastDirty;
    private long mDirtyCount;
    private Node mFlushNext;

    NodeContext(LocalDatabase db, long usedRate, int maxSize) {
        super(PACK_SLOTS);
        if (maxSize <= 0) {
            throw new IllegalArgumentException();
        }
        mDatabase = db;
        mPageSize = db.pageSize();
        mUsedRate = usedRate;
        acquireExclusive();
        mMaxSize = maxSize;
        releaseExclusive();
    }

    int pageSize() {
        return mPageSize;
    }

    void initialize(Object arena, int min) throws DatabaseException, OutOfMemoryError {
        while (--min >= 0) {
            acquireExclusive();
            if (mSize >= mMaxSize) {
                releaseExclusive();
                break;
            }
            doAllocLatchedNode(arena, 0).releaseExclusive();
        }
    }

    int nodeCount() {
        acquireShared();
        int size = mSize;
        releaseShared();
        return size;
    }

    Node tryAllocLatchedNode(int trial, int mode) throws IOException {
        acquireExclusive();

        int limit = mSize;
        do {
            Node node = mLeastRecentlyUsed;
            Node moreUsed;
            if (node == null || (moreUsed = node.mMoreUsed) == null) {
                if (mSize < mMaxSize) {
                    return doAllocLatchedNode(null, mode);
                } else if (node == null) {
                    break;
                }
            } else {
                moreUsed.mLessUsed = null;
                mLeastRecentlyUsed = moreUsed;
                node.mMoreUsed = null;
                (node.mLessUsed = mMostRecentlyUsed).mMoreUsed = node;
                mMostRecentlyUsed = node;
            }

            if (!node.tryAcquireExclusive()) {
                continue;
            }

            if (trial == 1) {
                if (node.mCachedState != Node.CACHED_CLEAN) {
                    if (mSize < mMaxSize) {
                        node.releaseExclusive();
                        return doAllocLatchedNode(null, mode);
                    } else if ((mode & MODE_NO_EVICT) != 0) {
                        node.releaseExclusive();
                        break;
                    }
                }

                releaseExclusive();

                if (node.evict(mDatabase)) {
                    if ((mode & MODE_UNEVICTABLE) != 0) {
                        node.mContext.makeUnevictable(node);
                    }
                    return node;
                }

                acquireExclusive();
            } else if ((mode & MODE_NO_EVICT) != 0) {
                if (node.mCachedState != Node.CACHED_CLEAN) {
                    node.releaseExclusive();
                    break;
                }
            } else {
                try {
                    if (node.evict(mDatabase)) {
                        if ((mode & MODE_UNEVICTABLE) != 0) {
                            NodeContext context = node.mContext;
                            if (context == this) {
                                doMakeUnevictable(node);
                            } else {
                                releaseExclusive();
                                context.makeUnevictable(node);
                                return node;
                            }
                        }
                        releaseExclusive();
                        return node;
                    }
                } catch (Throwable e) {
                    releaseExclusive();
                    throw e;
                }
            }
        } while (--limit > 0);

        releaseExclusive();

        return null;
    }

    private Node doAllocLatchedNode(Object arena, int mode) throws DatabaseException {
        try {
            mDatabase.checkClosed();

            long page;
            // page = p_calloc(arena, mPageSize, mDatabase.mPageDb.isDirectIO());
            page = mDatabase.mFullyMapped ? DirectPageOps.p_nonTreePage()
                   : DirectPageOps.p_calloc(arena, mPageSize, mDatabase.mPageDb.isDirectIO());

            Node node = new Node(this, page);
            node.acquireExclusive();
            mSize++;

            if ((mode & MODE_UNEVICTABLE) == 0) {
                Node most = mMostRecentlyUsed;
                node.mLessUsed = most;
                if (most == null) {
                    mLeastRecentlyUsed = node;
                } else {
                    most.mMoreUsed = node;
                }
                mMostRecentlyUsed = node;
            }

            return node;
        } finally {
            releaseExclusive();
        }
    }

    void used(final Node node, final ThreadLocalRandom rnd) {
        if ((rnd.nextLong() & mUsedRate) == 0 && tryAcquireExclusive()) {
            doUsed(node);
        }
    }

    private void doUsed(final Node node) {
        Node moreUsed = node.mMoreUsed;
        if (moreUsed != null) {
            Node lessUsed = node.mLessUsed;
            moreUsed.mLessUsed = lessUsed;
            if (lessUsed == null) {
                mLeastRecentlyUsed = moreUsed;
            } else {
                lessUsed.mMoreUsed = moreUsed;
            }
            node.mMoreUsed = null;
            (node.mLessUsed = mMostRecentlyUsed).mMoreUsed = node;
            mMostRecentlyUsed = node;
        }
        releaseExclusive();
    }

    void unused(final Node node) {
        try {
            acquireExclusive();
        } catch (Throwable e) {
            node.releaseExclusive();
            throw e;
        }

        try {
            Node lessUsed = node.mLessUsed;
            if (lessUsed != null) {
                Node moreUsed = node.mMoreUsed;
                lessUsed.mMoreUsed = moreUsed;
                if (moreUsed == null) {
                    mMostRecentlyUsed = lessUsed;
                } else {
                    moreUsed.mLessUsed = lessUsed;
                }
                node.mLessUsed = null;
                (node.mMoreUsed = mLeastRecentlyUsed).mLessUsed = node;
                mLeastRecentlyUsed = node;
            } else if (mMaxSize != 0) {
                doMakeEvictableNow(node);
            }
        } finally {
            node.releaseExclusive();
            releaseExclusive();
        }
    }

    void makeEvictable(final Node node) {
        acquireExclusive();
        try {
            if (mMaxSize != 0 && node.mMoreUsed == null) {
                Node most = mMostRecentlyUsed;
                if (node != most) {
                    node.mLessUsed = most;
                    if (most == null) {
                        mLeastRecentlyUsed = node;
                    } else {
                        most.mMoreUsed = node;
                    }
                    mMostRecentlyUsed = node;
                }
            }
        } finally {
            releaseExclusive();
        }
    }

    void makeEvictableNow(final Node node) {
        acquireExclusive();
        try {
            if (mMaxSize != 0 && node.mLessUsed == null) {
                doMakeEvictableNow(node);
            }
        } finally {
            releaseExclusive();
        }
    }

    private void doMakeEvictableNow(final Node node) {
        Node least = mLeastRecentlyUsed;
        if (node != least) {
            node.mMoreUsed = least;
            if (least == null) {
                mMostRecentlyUsed = node;
            } else {
                least.mLessUsed = node;
            }
            mLeastRecentlyUsed = node;
        }
    }

    void makeUnevictable(final Node node) {
        acquireExclusive();
        try {
            if (mMaxSize != 0) {
                doMakeUnevictable(node);
            }
        } finally {
            releaseExclusive();
        }
    }

    private void doMakeUnevictable(final Node node) {
        final Node lessUsed = node.mLessUsed;
        final Node moreUsed = node.mMoreUsed;

        if (lessUsed != null) {
            node.mLessUsed = null;
            if (moreUsed != null) {
                node.mMoreUsed = null;
                lessUsed.mMoreUsed = moreUsed;
                moreUsed.mLessUsed = lessUsed;
            } else if (node == mMostRecentlyUsed) {
                mMostRecentlyUsed = lessUsed;
                lessUsed.mMoreUsed = null;
            }
        } else if (node == mLeastRecentlyUsed) {
            mLeastRecentlyUsed = moreUsed;
            if (moreUsed != null) {
                node.mMoreUsed = null;
                moreUsed.mLessUsed = null;
            } else {
                mMostRecentlyUsed = null;
            }
        }
    }

    synchronized void addDirty(Node node, byte cachedState) {
        node.mCachedState = cachedState;

        final Node next = node.mNextDirty;
        final Node prev = node.mPrevDirty;
        if (next != null) {
            if ((next.mPrevDirty = prev) == null) {
                mFirstDirty = next;
            } else {
                prev.mNextDirty = next;
            }
            node.mNextDirty = null;
            (node.mPrevDirty = mLastDirty).mNextDirty = node;
        } else if (prev == null) {
            Node last = mLastDirty;
            if (last == node) {
                return;
            }
            mDirtyCount++;
            if (last == null) {
                mFirstDirty = node;
            } else {
                node.mPrevDirty = last;
                last.mNextDirty = node;
            }
        }

        mLastDirty = node;

        if (mFlushNext == node) {
            mFlushNext = next;
        }
    }

    synchronized void swapIfDirty(Node oldNode, Node newNode) {
        Node next = oldNode.mNextDirty;
        if (next != null) {
            newNode.mNextDirty = next;
            next.mPrevDirty = newNode;
            oldNode.mNextDirty = null;
        }
        Node prev = oldNode.mPrevDirty;
        if (prev != null) {
            newNode.mPrevDirty = prev;
            prev.mNextDirty = newNode;
            oldNode.mPrevDirty = null;
        }
        if (oldNode == mFirstDirty) {
            mFirstDirty = newNode;
        }
        if (oldNode == mLastDirty) {
            mLastDirty = newNode;
        }
        if (oldNode == mFlushNext) {
            mFlushNext = newNode;
        }
    }

    @Override
    public void flushDirty(final int dirtyState) throws IOException {
        final PageDb pageDb = mDatabase.mPageDb;

        synchronized (this) {
            mFlushNext = mFirstDirty;
        }

        while (true) {
            Node node;
            int state;

            synchronized (this) {
                node = mFlushNext;
                if (node == null) {
                    return;
                }

                state = node.mCachedState;

                if (state == (dirtyState ^ 1)) {
                    mFlushNext = null;
                    return;
                }

                mFlushNext = node.mNextDirty;

                Node next = node.mNextDirty;
                Node prev = node.mPrevDirty;
                if (next != null) {
                    next.mPrevDirty = prev;
                    node.mNextDirty = null;
                } else if (mLastDirty == node) {
                    mLastDirty = prev;
                }
                if (prev != null) {
                    prev.mNextDirty = next;
                    node.mPrevDirty = null;
                } else if (mFirstDirty == node) {
                    mFirstDirty = next;
                }

                mDirtyCount--;
            }

            if (state == Node.CACHED_CLEAN) {
                continue;
            }

            node.acquireExclusive();
            state = node.mCachedState;
            if (state != dirtyState) {
                node.releaseExclusive();
                continue;
            }

            node.downgrade();
            try {
                node.write(pageDb);
                node.mCachedState = Node.CACHED_CLEAN;
            } finally {
                node.releaseShared();
            }
        }
    }

    synchronized long dirtyCount() {
        return mDirtyCount;
    }

    void delete() {
        acquireExclusive();
        try {
            mMaxSize = 0;

            Node node = mLeastRecentlyUsed;
            mLeastRecentlyUsed = null;
            mMostRecentlyUsed = null;

            while (node != null) {
                Node next = node.mMoreUsed;
                node.mLessUsed = null;
                node.mMoreUsed = null;

                node.delete(mDatabase);

                node = next;
            }
        } finally {
            releaseExclusive();
        }

        synchronized (this) {
            Node node = mFirstDirty;
            mFlushNext = null;
            mFirstDirty = null;
            mLastDirty = null;
            while (node != null) {
                node.delete(mDatabase);
                Node next = node.mNextDirty;
                node.mPrevDirty = null;
                node.mNextDirty = null;
                node = next;
            }
        }
    }
}
