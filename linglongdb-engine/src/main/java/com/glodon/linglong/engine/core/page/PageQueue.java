package com.glodon.linglong.engine.core.page;

import com.glodon.linglong.base.common.IntegerRef;
import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.exception.CorruptDatabaseException;
import com.glodon.linglong.base.exception.WriteFailureException;
import com.glodon.linglong.base.io.PageArray;
import com.glodon.linglong.engine.core.IdHeap;
import com.glodon.linglong.engine.event.EventListener;
import com.glodon.linglong.engine.event.EventType;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Stereo
 */
public final class PageQueue implements IntegerRef {

    static final int I_REMOVE_PAGE_COUNT = 0;
    static final int I_REMOVE_NODE_COUNT = I_REMOVE_PAGE_COUNT + 8;
    static final int I_REMOVE_HEAD_ID = I_REMOVE_NODE_COUNT + 8;
    static final int I_REMOVE_HEAD_OFFSET = I_REMOVE_HEAD_ID + 8;
    static final int I_REMOVE_HEAD_FIRST_PAGE_ID = I_REMOVE_HEAD_OFFSET + 4;
    static final int I_APPEND_HEAD_ID = I_REMOVE_HEAD_FIRST_PAGE_ID + 8;
    static final int HEADER_SIZE = I_APPEND_HEAD_ID + 8;

    static final int I_NEXT_NODE_ID = 0;
    static final int I_FIRST_PAGE_ID = I_NEXT_NODE_ID + 8;
    static final int I_NODE_START = I_FIRST_PAGE_ID + 8;

    private final PageManager mManager;
    private final int mPageSize;
    private final int mAllocMode;
    private final boolean mAggressive;

    private long mRemovePageCount;
    private long mRemoveNodeCount;
    private final long mRemoveHead;
    private long mRemoveHeadId;
    private int mRemoveHeadOffset;
    private long mRemoveHeadFirstPageId;
    private long mRemoveStoppedId;
    private long mRemovedNodeCounter;
    private long mReserveReclaimUpperBound;

    private volatile long mAppendHeadId;

    private final ReentrantLock mAppendLock;
    private final IdHeap mAppendHeap;
    private final long mAppendTail;
    private volatile long mAppendTailId;
    private long mAppendPageCount;
    private long mAppendNodeCount;
    private boolean mDrainInProgress;

    public static boolean exists(long header, int offset) {
        return DirectPageOps.p_longGetLE(header, offset + I_REMOVE_HEAD_ID) != 0;
    }

    public static PageQueue newRegularFreeList(PageManager manager) {
        return new PageQueue(manager, PageManager.ALLOC_NORMAL, false, null);
    }

    public static PageQueue newRecycleFreeList(PageManager manager) {
        return new PageQueue(manager, PageManager.ALLOC_NORMAL, true, null);
    }

    private PageQueue(PageManager manager, int allocMode, boolean aggressive,
                      ReentrantLock appendLock) {
        PageArray array = manager.pageArray();

        mManager = manager;
        mPageSize = array.pageSize();
        mAllocMode = allocMode;
        mAggressive = aggressive;

        mRemoveHead = DirectPageOps.p_calloc(mPageSize, array.isDirectIO());

        if (appendLock == null) {
            mAppendLock = new ReentrantLock(false);
        } else {
            mAppendLock = appendLock;
        }

        mAppendHeap = new IdHeap(mPageSize - I_NODE_START);
        mAppendTail = DirectPageOps.p_calloc(mPageSize, array.isDirectIO());
    }

    public void delete() {
        DirectPageOps.p_delete(mRemoveHead);
        DirectPageOps.p_delete(mAppendTail);
    }

    public PageQueue newReserveFreeList() {
        if (mAggressive) {
            throw new IllegalStateException();
        }
        return new PageQueue(mManager, PageManager.ALLOC_RESERVE, false, mAppendLock);
    }

    void init(long headNodeId) {
        mAppendLock.lock();
        try {
            mRemoveStoppedId = mAppendHeadId = mAppendTailId = headNodeId;
        } finally {
            mAppendLock.unlock();
        }
    }

    void init(EventListener debugListener, long header, int offset) throws IOException {
        mRemovePageCount = DirectPageOps.p_longGetLE(header, offset + I_REMOVE_PAGE_COUNT);
        mRemoveNodeCount = DirectPageOps.p_longGetLE(header, offset + I_REMOVE_NODE_COUNT);

        mRemoveHeadId = DirectPageOps.p_longGetLE(header, offset + I_REMOVE_HEAD_ID);
        mRemoveHeadOffset = DirectPageOps.p_intGetLE(header, offset + I_REMOVE_HEAD_OFFSET);
        mRemoveHeadFirstPageId = DirectPageOps.p_longGetLE(header, offset + I_REMOVE_HEAD_FIRST_PAGE_ID);

        mAppendHeadId = mAppendTailId = DirectPageOps.p_longGetLE(header, offset + I_APPEND_HEAD_ID);

        if (debugListener != null) {
            String type;
            if (mAllocMode == PageManager.ALLOC_NORMAL) {
                type = mAggressive ? "Recycle" : "Regular";
            } else {
                type = "Reserve";
            }

            debugListener.notify(EventType.DEBUG, "%1$s free list REMOVE_PAGE_COUNT: %2$d",
                    type, mRemovePageCount);
            debugListener.notify(EventType.DEBUG, "%1$s free list REMOVE_NODE_COUNT: %2$d",
                    type, mRemoveNodeCount);
            debugListener.notify(EventType.DEBUG, "%1$s free list REMOVE_HEAD_ID: %2$d",
                    type, mRemoveHeadId);
            debugListener.notify(EventType.DEBUG, "%1$s free list REMOVE_HEAD_OFFSET: %2$d",
                    type, mRemoveHeadOffset);
            debugListener.notify(EventType.DEBUG, "%1$s free list REMOVE_HEAD_FIRST_PAGE_ID: %2$d",
                    type, mRemoveHeadFirstPageId);
        }

        if (mRemoveHeadId == 0) {
            mRemoveStoppedId = mAppendHeadId;
        } else {
            mManager.pageArray().readPage(mRemoveHeadId, mRemoveHead);
            if (mRemoveHeadFirstPageId == 0) {
                mRemoveHeadFirstPageId = DirectPageOps.p_longGetBE(mRemoveHead, I_FIRST_PAGE_ID);
            }
        }
    }

    public void reclaim(ReentrantLock removeLock, long upperBound) throws IOException {
        if (mAllocMode != PageManager.ALLOC_RESERVE) {
            throw new IllegalStateException();
        }

        removeLock.lock();
        mReserveReclaimUpperBound = upperBound;

        while (true) {
            long pageId = tryRemove(removeLock);
            if (pageId == 0) {
                removeLock.unlock();
                break;
            }
            if (pageId <= upperBound) {
                mManager.deletePage(pageId, true);
            }
            removeLock.lock();
        }

        long pageId = mRemoveStoppedId;
        if (pageId != 0 && pageId <= upperBound) {
            mManager.deletePage(pageId, true);
        }
    }

    public long getRemoveScanTarget() {
        return mRemovedNodeCounter + mRemoveNodeCount;
    }

    public boolean isRemoveScanComplete(long target) {
        return (mRemovedNodeCounter - target) >= 0;
    }

    public long tryRemove(ReentrantLock lock) throws IOException {
        if (mRemoveHeadId == 0) {
            if (!mAggressive || mRemoveStoppedId == mAppendTailId) {
                return 0;
            }
            loadRemoveNode(mRemoveStoppedId);
            mRemoveStoppedId = 0;
        }

        long pageId;
        long oldHeadId;

        try {
            pageId = mRemoveHeadFirstPageId;

            if (mAllocMode != PageManager.ALLOC_RESERVE && mManager.isPageOutOfBounds(pageId)) {
                throw new CorruptDatabaseException
                        ("Invalid page id in free list: " + pageId + "; list node: " + mRemoveHeadId);
            }

            mRemovePageCount--;

            final long head = mRemoveHead;
            if (mRemoveHeadOffset < pageSize(head)) {
                long delta = DirectPageOps.p_ulongGetVar(head, this);
                if (delta > 0) {
                    mRemoveHeadFirstPageId = pageId + delta;
                    return pageId;
                }
            }

            oldHeadId = mRemoveHeadId;

            if (mAllocMode == PageManager.ALLOC_RESERVE && oldHeadId > mReserveReclaimUpperBound) {
                oldHeadId = 0;
            }

            long nextId = DirectPageOps.p_longGetBE(head, I_NEXT_NODE_ID);

            if (nextId == (mAggressive ? mAppendTailId : mAppendHeadId)) {
                mRemoveHeadId = 0;
                mRemoveHeadOffset = 0;
                mRemoveHeadFirstPageId = 0;
                mRemoveStoppedId = nextId;
            } else {
                loadRemoveNode(nextId);
            }

            mRemoveNodeCount--;
            mRemovedNodeCounter++;
        } finally {
            lock.unlock();
        }

        if (oldHeadId != 0) {
            mManager.deletePage(oldHeadId, true);
        }

        return pageId;
    }

    private void loadRemoveNode(long id) throws IOException {
        if (mAllocMode != PageManager.ALLOC_RESERVE && mManager.isPageOutOfBounds(id)) {
            throw new CorruptDatabaseException("Invalid node id in free list: " + id);
        }
        long head = mRemoveHead;
        mManager.pageArray().readPage(id, head);
        mRemoveHeadId = id;
        mRemoveHeadOffset = I_NODE_START;
        mRemoveHeadFirstPageId = DirectPageOps.p_longGetBE(head, I_FIRST_PAGE_ID);
    }

    public void append(long id, boolean force) throws IOException {
        if (id <= 1) {
            throw new IllegalArgumentException("Page id: " + id);
        }

        final IdHeap appendHeap = mAppendHeap;

        mAppendLock.lock();
        try {
            appendHeap.add(id);
            mAppendPageCount++;
            if (!mDrainInProgress && appendHeap.shouldDrain()) {
                try {
                    drainAppendHeap(appendHeap);
                } catch (IOException e) {
                    if (!force) {
                        // Undo.
                        appendHeap.remove(id);
                        mAppendPageCount--;
                        throw e;
                    }
                }
            }
        } finally {
            mAppendLock.unlock();
        }
    }

    public long tryUnappend() {
        mAppendLock.lock();
        try {
            final IdHeap appendHeap = mAppendHeap;
            if (mDrainInProgress && appendHeap.size() <= 1) {
                return 0;
            }
            long id = appendHeap.tryRemove();
            if (id != 0) {
                mAppendPageCount--;
            }
            return id;
        } finally {
            mAppendLock.unlock();
        }
    }

    private void drainAppendHeap(IdHeap appendHeap) throws IOException {
        if (mDrainInProgress) {
            throw new AssertionError();
        }

        mDrainInProgress = true;
        try {
            long newTailId = mManager.allocPage(mAllocMode);
            long firstPageId = appendHeap.remove();

            long tailBuf = mAppendTail;
            DirectPageOps.p_longPutBE(tailBuf, I_NEXT_NODE_ID, newTailId);
            DirectPageOps.p_longPutBE(tailBuf, I_FIRST_PAGE_ID, firstPageId);

            int end = appendHeap.drain(firstPageId,
                    tailBuf,
                    I_NODE_START,
                    pageSize(tailBuf) - I_NODE_START);

            DirectPageOps.p_clear(tailBuf, end, pageSize(tailBuf));

            try {
                mManager.pageArray().writePage(mAppendTailId, tailBuf);
            } catch (IOException e) {
                // Undo.
                appendHeap.undrain(firstPageId, tailBuf, I_NODE_START, end);
                throw new WriteFailureException(e);
            }

            mAppendNodeCount++;
            mAppendTailId = newTailId;
        } finally {
            mDrainInProgress = false;
        }
    }

    public ReentrantLock appendLock() {
        return mAppendLock;
    }

    public void preCommit() throws IOException {
        final IdHeap appendHeap = mAppendHeap;
        while (appendHeap.size() > 0) {
            drainAppendHeap(appendHeap);
        }
    }

    public void commitStart(long header, int offset) {
        DirectPageOps.p_longPutLE(header, offset + I_REMOVE_PAGE_COUNT, mRemovePageCount + mAppendPageCount);
        DirectPageOps.p_longPutLE(header, offset + I_REMOVE_NODE_COUNT, mRemoveNodeCount + mAppendNodeCount);

        if (mRemoveHeadId == 0 && mAppendPageCount > 0) {
            long headId = mAppendHeadId;
            if (headId != mRemoveStoppedId) {
                if (mRemoveStoppedId == mAppendTailId) {
                    headId = 0;
                } else {
                    headId = mRemoveStoppedId;
                }
            }

            DirectPageOps.p_longPutLE(header, offset + I_REMOVE_HEAD_ID, headId);
            DirectPageOps.p_intPutLE(header, offset + I_REMOVE_HEAD_OFFSET, I_NODE_START);
            DirectPageOps.p_longPutLE(header, offset + I_REMOVE_HEAD_FIRST_PAGE_ID, 0);
        } else {
            DirectPageOps.p_longPutLE(header, offset + I_REMOVE_HEAD_ID, mRemoveHeadId);
            DirectPageOps.p_intPutLE(header, offset + I_REMOVE_HEAD_OFFSET, mRemoveHeadOffset);
            DirectPageOps.p_longPutLE(header, offset + I_REMOVE_HEAD_FIRST_PAGE_ID, mRemoveHeadFirstPageId);
        }

        DirectPageOps.p_longPutLE(header, offset + I_APPEND_HEAD_ID, mAppendTailId);

        mRemovePageCount += mAppendPageCount;
        mRemoveNodeCount += mAppendNodeCount;

        mAppendPageCount = 0;
        mAppendNodeCount = 0;
    }

    public void commitEnd(long header, int offset) throws IOException {
        long newAppendHeadId = DirectPageOps.p_longGetLE(header, offset + I_APPEND_HEAD_ID);

        if (mRemoveHeadId == 0
                && mRemoveStoppedId != newAppendHeadId && mRemoveStoppedId != mAppendTailId) {
            loadRemoveNode(mRemoveStoppedId);
            mRemoveStoppedId = 0;
        }

        mAppendHeadId = newAppendHeadId;
    }

    public void addTo(PageDb.Stats stats) {
        stats.freePages +=
                mRemovePageCount + mAppendPageCount +
                        mRemoveNodeCount + mAppendNodeCount;
    }

    public boolean verifyPageRange(long startId, long endId) throws IOException {
        long expectedHash = 0;
        for (long i = startId; i < endId; i++) {
            expectedHash += Utils.scramble(i);
        }

        long hash = 0;
        long count = 0;

        long nodeId = mRemoveHeadId;

        if (nodeId != 0) {
            PageArray pa = mManager.pageArray();
            long node = DirectPageOps.p_clone(mRemoveHead, pageSize(mRemoveHead), pa.isDirectIO());
            try {
                long pageId = mRemoveHeadFirstPageId;
                IntegerRef.Value nodeOffsetRef = new IntegerRef.Value();
                nodeOffsetRef.set(mRemoveHeadOffset);

                while (true) {
                    if (pageId < startId || pageId >= endId) {
                        return false;
                    }

                    hash += Utils.scramble(pageId);
                    count++;

                    if (nodeOffsetRef.get() < pageSize(node)) {
                        long delta = DirectPageOps.p_ulongGetVar(node, nodeOffsetRef);
                        if (delta > 0) {
                            pageId += delta;
                            continue;
                        }
                    }

                    if (nodeId >= startId && nodeId < endId) {
                        hash += Utils.scramble(nodeId);
                        count++;
                    }

                    nodeId = DirectPageOps.p_longGetBE(node, I_NEXT_NODE_ID);
                    if (nodeId == mAppendTailId) {
                        break;
                    }

                    pa.readPage(nodeId, node);
                    pageId = DirectPageOps.p_longGetBE(node, I_FIRST_PAGE_ID);
                    nodeOffsetRef.set(I_NODE_START);
                }
            } finally {
                DirectPageOps.p_delete(node);
            }
        }

        return hash == expectedHash && count == (endId - startId);
    }

    private int pageSize(long page) {
        return mPageSize;
    }

    @Override
    public int get() {
        return mRemoveHeadOffset;
    }

    @Override
    public void set(int offset) {
        mRemoveHeadOffset = offset;
    }
}
