package com.linglong.engine.core.page;

import com.linglong.base.exception.DatabaseFullException;
import com.linglong.base.exception.WriteFailureException;
import com.linglong.base.io.PageArray;
import com.linglong.engine.event.EventListener;
import com.linglong.engine.event.EventType;
import com.linglong.engine.core.lock.CommitLock;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Stereo
 */
final class PageManager {
    /*

    Header structure is encoded as follows, in 140 bytes:

    +--------------------------------------------+
    | long: total page count                     |
    | PageQueue: regular free list (44 bytes)    |
    | PageQueue: recycle free list (44 bytes)    |
    | PageQueue: reserve list (44 bytes)         |
    +--------------------------------------------+

    */

    // Indexes of entries in header.
    static final int I_TOTAL_PAGE_COUNT = 0;
    static final int I_REGULAR_QUEUE = I_TOTAL_PAGE_COUNT + 8;
    static final int I_RECYCLE_QUEUE = I_REGULAR_QUEUE + PageQueue.HEADER_SIZE;
    static final int I_RESERVE_QUEUE = I_RECYCLE_QUEUE + PageQueue.HEADER_SIZE;

    private final PageArray mPageArray;

    // One remove lock for all queues.
    private final ReentrantLock mRemoveLock;
    private long mTotalPageCount;
    private long mPageLimit;
    private ThreadLocal<Long> mPageLimitOverride;

    private final PageQueue mRegularFreeList;
    private final PageQueue mRecycleFreeList;

    private volatile boolean mCompacting;
    private long mCompactionTargetPageCount = Long.MAX_VALUE;
    private PageQueue mReserveList;
    private long mReclaimUpperBound = Long.MIN_VALUE;

    static final int
            ALLOC_TRY_RESERVE = -1, // Create pages: no.  Compaction zone: yes
            ALLOC_NORMAL = 0,       // Create pages: yes. Compaction zone: no
            ALLOC_RESERVE = 1;      // Create pages: yes. Compaction zone: yes

    PageManager(PageArray array) throws IOException {
        this(null, false, array, DirectPageOps.p_null(), 0);
    }

    PageManager(EventListener debugListener, PageArray array, long header, int offset)
            throws IOException {
        this(debugListener, true, array, header, offset);
    }

    private PageManager(EventListener debugListener,
                        boolean restored, PageArray array, long header, int offset)
            throws IOException {
        if (array == null) {
            throw new IllegalArgumentException("PageArray is null");
        }

        mPageArray = array;

        mRemoveLock = new ReentrantLock(false);
        mRegularFreeList = PageQueue.newRegularFreeList(this);
        mRecycleFreeList = PageQueue.newRecycleFreeList(this);

        mPageLimit = -1; // no limit

        try {
            if (!restored) {
                mTotalPageCount = 4;
                mRegularFreeList.init(2);
                mRecycleFreeList.init(3);
            } else {
                mTotalPageCount = readTotalPageCount(header, offset + I_TOTAL_PAGE_COUNT);

                if (debugListener != null) {
                    debugListener.notify(EventType.DEBUG, "TOTAL_PAGE_COUNT: %1$d",
                            mTotalPageCount);
                }

                long actualPageCount = array.getPageCount();
                if (actualPageCount > mTotalPageCount) {
                    if (!array.isReadOnly()) {
                        array.setPageCount(mTotalPageCount);
                    }
                } else if (actualPageCount < mTotalPageCount) {
                }

                PageQueue reserve;
                fullLock();
                try {
                    mRegularFreeList.init(debugListener, header, offset + I_REGULAR_QUEUE);
                    mRecycleFreeList.init(debugListener, header, offset + I_RECYCLE_QUEUE);

                    if (PageQueue.exists(header, offset + I_RESERVE_QUEUE)) {
                        reserve = mRegularFreeList.newReserveFreeList();
                        try {
                            reserve.init(debugListener, header, offset + I_RESERVE_QUEUE);
                        } catch (Throwable e) {
                            reserve.delete();
                            throw e;
                        }
                    } else {
                        reserve = null;
                        if (debugListener != null) {
                            debugListener.notify(EventType.DEBUG, "Reserve free list is null");
                        }
                    }
                } finally {
                    fullUnlock();
                }

                if (reserve != null) {
                    try {
                        reserve.reclaim(mRemoveLock, mTotalPageCount - 1);
                    } finally {
                        reserve.delete();
                    }
                }
            }
        } catch (Throwable e) {
            delete();
            throw e;
        }
    }

    void delete() {
        if (mRegularFreeList != null) {
            mRegularFreeList.delete();
        }
        if (mRecycleFreeList != null) {
            mRecycleFreeList.delete();
        }
        PageQueue reserve = mReserveList;
        if (reserve != null) {
            reserve.delete();
            mReserveList = null;
        }
    }

    static long readTotalPageCount(long header, int offset) {
        return DirectPageOps.p_longGetLE(header, offset + I_TOTAL_PAGE_COUNT);
    }

    public PageArray pageArray() {
        return mPageArray;
    }

    public long allocPage() throws IOException {
        return allocPage(ALLOC_NORMAL);
    }

    public long allocPage(int mode) throws IOException {
        while (true) {
            long pageId;
            alloc:
            {
                pageId = mRecycleFreeList.tryUnappend();
                if (pageId != 0) {
                    break alloc;
                }

                final ReentrantLock lock = mRemoveLock;
                lock.lock();
                pageId = mRecycleFreeList.tryRemove(lock);
                if (pageId != 0) {
                    break alloc;
                }
                pageId = mRegularFreeList.tryRemove(lock);
                if (pageId != 0) {
                    break alloc;
                }

                if (mode >= ALLOC_NORMAL) {

                    PageQueue reserve = mReserveList;
                    if (reserve != null) {
                        if (mCompacting) {
                            mCompacting = false;
                        }
                        if (mReclaimUpperBound == Long.MIN_VALUE) {
                            pageId = reserve.tryRemove(lock);
                            if (pageId != 0) {
                                return pageId;
                            }
                        }
                    }

                    try {
                        pageId = increasePageCount();
                    } catch (Throwable e) {
                        lock.unlock();
                        throw e;
                    }
                }

                lock.unlock();
                return pageId;
            }

            if (mode == ALLOC_NORMAL && pageId >= mCompactionTargetPageCount && mCompacting) {
                mReserveList.append(pageId, true);
                continue;
            }

            return pageId;
        }
    }

    public void deletePage(long id, boolean force) throws IOException {
        if (id >= mCompactionTargetPageCount && mCompacting) {
            mReserveList.append(id, force);
        } else {
            mRegularFreeList.append(id, force);
        }
    }

    public void recyclePage(long id) throws IOException {
        if (id >= mCompactionTargetPageCount && mCompacting) {
            mReserveList.append(id, true);
        } else {
            mRecycleFreeList.append(id, true);
        }
    }

    public void allocAndRecyclePage() throws IOException {
        long pageId;
        mRemoveLock.lock();
        try {
            pageId = increasePageCount();
        } finally {
            mRemoveLock.unlock();
        }
        recyclePage(pageId);
    }

    private long increasePageCount() throws IOException, DatabaseFullException {
        long total = mTotalPageCount;

        long limit;
        {
            ThreadLocal<Long> override = mPageLimitOverride;
            Long limitObj;
            if (override == null || (limitObj = override.get()) == null) {
                limit = mPageLimit;
            } else {
                limit = limitObj;
            }

            long max = mPageArray.getPageCountLimit();
            if (max > 0 && (limit < 0 || limit > max)) {
                limit = max;
            }
        }

        if (limit >= 0 && total >= limit) {
            throw new DatabaseFullException
                    ("Capacity limit reached: " + (limit * mPageArray.pageSize()));
        }

        mTotalPageCount = total + 1;
        return total;
    }

    public void pageLimit(long limit) {
        mRemoveLock.lock();
        try {
            mPageLimit = limit;
        } finally {
            mRemoveLock.unlock();
        }
    }

    public void pageLimitOverride(long limit) {
        mRemoveLock.lock();
        try {
            if (limit == 0) {
                if (mPageLimitOverride != null) {
                    mPageLimitOverride.remove();
                }
            } else {
                if (mPageLimitOverride == null) {
                    mPageLimitOverride = new ThreadLocal<>();
                }
                mPageLimitOverride.set(limit);
            }
        } finally {
            mRemoveLock.unlock();
        }
    }

    public long pageLimit() {
        mRemoveLock.lock();
        try {
            return mPageLimit;
        } finally {
            mRemoveLock.unlock();
        }
    }

    public boolean compactionStart(long targetPageCount) throws IOException {
        if (mCompacting) {
            throw new IllegalStateException("Compaction in progress");
        }

        if (mReserveList != null) {
            throw new IllegalStateException();
        }

        if (targetPageCount < 2) {
            return false;
        }

        mRemoveLock.lock();
        try {
            if (targetPageCount >= mTotalPageCount
                    && targetPageCount >= mPageArray.getPageCount()) {
                return false;
            }
        } finally {
            mRemoveLock.unlock();
        }

        long initPageId = allocPage(ALLOC_TRY_RESERVE);
        if (initPageId == 0) {
            return false;
        }

        PageQueue reserve;
        try {
            reserve = mRegularFreeList.newReserveFreeList();
            reserve.init(initPageId);
        } catch (Throwable e) {
            try {
                recyclePage(initPageId);
            } catch (IOException e2) {
                // Ignore.
            }
            throw e;
        }

        mRemoveLock.lock();
        if (mReserveList != null) {
            mReserveList.delete();
        }
        mReserveList = reserve;
        mCompactionTargetPageCount = targetPageCount;
        mCompacting = true;
        mRemoveLock.unlock();

        return true;
    }

    public boolean compactionScanFreeList(CommitLock commitLock) throws IOException {
        return compactionScanFreeList(commitLock, mRecycleFreeList)
                && compactionScanFreeList(commitLock, mRegularFreeList);
    }

    private boolean compactionScanFreeList(CommitLock commitLock, PageQueue list)
            throws IOException {
        long target;
        mRemoveLock.lock();
        target = list.getRemoveScanTarget();
        mRemoveLock.unlock();

        CommitLock.Shared shared = commitLock.acquireShared();
        try {
            while (mCompacting) {
                mRemoveLock.lock();
                long pageId;
                if (list.isRemoveScanComplete(target)
                        || (pageId = list.tryRemove(mRemoveLock)) == 0) {
                    mRemoveLock.unlock();
                    return mCompacting;
                }
                if (pageId >= mCompactionTargetPageCount) {
                    mReserveList.append(pageId, true);
                } else {
                    mRecycleFreeList.append(pageId, true);
                }
                if (commitLock.hasQueuedThreads()) {
                    shared.release();
                    shared = commitLock.acquireShared();
                }
            }
        } finally {
            shared.release();
        }

        return false;
    }

    public boolean compactionVerify() throws IOException {
        if (!mCompacting) {
            return true;
        }
        long total;
        mRemoveLock.lock();
        total = mTotalPageCount;
        mRemoveLock.unlock();
        return mReserveList.verifyPageRange(mCompactionTargetPageCount, total);
    }

    public boolean compactionEnd(CommitLock commitLock) throws IOException {
        long upperBound = Long.MAX_VALUE;

        boolean ready = compactionVerify();

        commitLock.acquireExclusive();
        fullLock();

        if (ready && (ready = mCompacting && (mTotalPageCount > mCompactionTargetPageCount
                || mPageArray.getPageCount() > mTotalPageCount))) {
            mTotalPageCount = mCompactionTargetPageCount;
            upperBound = mTotalPageCount - 1;
        }

        mCompacting = false;
        mCompactionTargetPageCount = Long.MAX_VALUE;

        mReclaimUpperBound = upperBound;

        fullUnlock();
        commitLock.releaseExclusive();

        return ready;
    }

    public void compactionReclaim() throws IOException {
        mRemoveLock.lock();
        PageQueue reserve = mReserveList;
        long upperBound = mReclaimUpperBound;
        mReserveList = null;
        mReclaimUpperBound = Long.MIN_VALUE;
        mRemoveLock.unlock();

        if (reserve != null) {
            try {
                reserve.reclaim(mRemoveLock, upperBound);
            } finally {
                reserve.delete();
            }
        }
    }

    public boolean truncatePages() throws IOException {
        mRemoveLock.lock();
        try {
            if (mTotalPageCount < mPageArray.getPageCount()) {
                try {
                    mPageArray.setPageCount(mTotalPageCount);
                    return true;
                } catch (IllegalStateException e) {
                    return false;
                }
            }
        } finally {
            mRemoveLock.unlock();
        }
        return false;
    }

    public void commitStart(long header, int offset) throws IOException {
        fullLock();
        try {
            if (mPageLimit > 0) {
                if (mPageLimitOverride == null) {
                    mPageLimitOverride = new ThreadLocal<>();
                }
                mPageLimitOverride.set(-1L);
            }

            try {
                mRegularFreeList.preCommit();
                mRecycleFreeList.preCommit();
                if (mReserveList != null) {
                    mReserveList.preCommit();
                }
            } catch (DatabaseFullException e) {
                throw e;
            } catch (IOException e) {
                throw new WriteFailureException(e);
            } finally {
                if (mPageLimitOverride != null) {
                    mPageLimitOverride.remove();
                }
            }

            DirectPageOps.p_longPutLE(header, offset + I_TOTAL_PAGE_COUNT, mTotalPageCount);

            mRegularFreeList.commitStart(header, offset + I_REGULAR_QUEUE);
            mRecycleFreeList.commitStart(header, offset + I_RECYCLE_QUEUE);
            if (mReserveList != null) {
                mReserveList.commitStart(header, offset + I_RESERVE_QUEUE);
            }
        } finally {
            fullUnlock();
        }
    }

    public void commitEnd(long header, int offset) throws IOException {
        mRemoveLock.lock();
        try {
            mRegularFreeList.commitEnd(header, offset + I_REGULAR_QUEUE);
            mRecycleFreeList.commitEnd(header, offset + I_RECYCLE_QUEUE);
            if (mReserveList != null) {
                mReserveList.commitEnd(header, offset + I_RESERVE_QUEUE);
            }
        } finally {
            mRemoveLock.unlock();
        }
    }

    private void fullLock() {
        mRegularFreeList.appendLock().lock();
        mRecycleFreeList.appendLock().lock();
        if (mReserveList != null) {
            mReserveList.appendLock().lock();
        }
        mRemoveLock.lock();
    }

    private void fullUnlock() {
        mRemoveLock.unlock();
        if (mReserveList != null) {
            mReserveList.appendLock().unlock();
        }
        mRecycleFreeList.appendLock().unlock();
        mRegularFreeList.appendLock().unlock();
    }

    void addTo(PageDb.Stats stats) {
        fullLock();
        try {
            stats.totalPages += mTotalPageCount;
            mRegularFreeList.addTo(stats);
            mRecycleFreeList.addTo(stats);
            if (mReserveList != null) {
                mReserveList.addTo(stats);
            }
        } finally {
            fullUnlock();
        }
    }

    boolean isPageOutOfBounds(long id) {
        return id <= 1 || id >= mTotalPageCount;
    }
}
