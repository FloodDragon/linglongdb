package com.linglong.engine.core.page;

import com.linglong.base.exception.DatabaseException;
import com.linglong.base.exception.DatabaseFullException;
import com.linglong.engine.core.LocalDatabase;
import com.linglong.engine.core.Node;
import com.linglong.engine.core.NodeContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongConsumer;

/**
 * @author Stereo
 */
public final class NonPageDb extends PageDb {
    private final int mPageSize;
    private final PageCache mCache;

    private final AtomicLong mAllocId;
    private final LongAdder mFreePageCount;

    public NonPageDb(int pageSize, PageCache cache) {
        mPageSize = pageSize;
        mCache = cache;
        // Next assigned id is 2, the first legal identifier.
        mAllocId = new AtomicLong(1);
        mFreePageCount = new LongAdder();
    }

    @Override
    public void delete() {
    }

    @Override
    public boolean isDurable() {
        return false;
    }

    @Override
    public boolean isDirectIO() {
        return false;
    }

    @Override
    public int allocMode() {
        return NodeContext.MODE_NO_EVICT;
    }

    @Override
    public Node allocLatchedNode(LocalDatabase db, int mode) throws IOException {
        Node node = db.allocLatchedNode(ThreadLocalRandom.current().nextLong(), mode);
        long nodeId = node.getId();
        if (nodeId < 0) {
            nodeId = -nodeId;
            mFreePageCount.decrement();
        } else {
            nodeId = allocPage();
        }
        node.setId(nodeId);
        return node;
    }

    @Override
    public int pageSize() {
        return mPageSize;
    }

    @Override
    public long pageCount() {
        return 0;
    }

    @Override
    public void pageLimit(long limit) {
    }

    @Override
    public long pageLimit() {
        return -1;
    }

    @Override
    public void pageLimitOverride(long limit) {
    }

    @Override
    public Stats stats() {
        Stats stats = new Stats();
        stats.freePages = Math.max(0, mFreePageCount.sum());
        stats.totalPages = Math.max(stats.freePages, mAllocId.get());
        return stats;
    }

    @Override
    public void readPage(long id, long page) throws IOException {
        PageCache cache = mCache;
        if (cache == null || !cache.remove(id, page, 0, pageSize())) {
            fail(false);
        }
    }

    @Override
    public long allocPage() throws IOException {
        long id = mAllocId.incrementAndGet();
        if (id > 0x0000_ffff_ffff_ffffL) {
            mAllocId.decrementAndGet();
            throw new DatabaseFullException();
        }
        return id;
    }

    @Override
    public void writePage(long id, long page) throws IOException {
        PageCache cache = mCache;
        if (cache == null || !cache.add(id, page, 0, false)) {
            fail(true);
        }
    }

    @Override
    public long evictPage(long id, long page) throws IOException {
        writePage(id, page);
        return page;
    }

    @Override
    public void cachePage(long id, long page) throws IOException {
        PageCache cache = mCache;
        if (cache != null && !cache.add(id, page, 0, false)) {
            fail(false);
        }
    }

    @Override
    public void uncachePage(long id) throws IOException {
        PageCache cache = mCache;
        if (cache != null) {
            cache.remove(id, DirectPageOps.p_null(), 0, 0);
        }
    }

    @Override
    public void deletePage(long id, boolean force) throws IOException {
        uncachePage(id);
        mFreePageCount.increment();
    }

    @Override
    public void recyclePage(long id) throws IOException {
        deletePage(id, true);
    }

    @Override
    public long allocatePages(long pageCount) throws IOException {
        return 0;
    }

    @Override
    public void scanFreeList(LongConsumer dst) throws IOException {
        return;
    }

    @Override
    public boolean compactionStart(long targetPageCount) throws IOException {
        return false;
    }

    @Override
    public boolean compactionScanFreeList() throws IOException {
        return false;
    }

    @Override
    public boolean compactionVerify() throws IOException {
        return false;
    }

    @Override
    public boolean compactionEnd() throws IOException {
        return false;
    }

    @Override
    public void compactionReclaim() throws IOException {
    }

    @Override
    public boolean truncatePages() throws IOException {
        return false;
    }

    @Override
    public int extraCommitDataOffset() {
        return 0;
    }

    @Override
    public void commit(boolean resume, long header, CommitCallback callback)
            throws IOException {
        throw new DatabaseException("Cannot commit to a non-durable database");
    }

    @Override
    public void readExtraCommitData(byte[] extra) throws IOException {
        Arrays.fill(extra, (byte) 0);
    }

    @Override
    public void close() {
        if (mCache != null) {
            mCache.close();
        }
    }

    @Override
    public void close(Throwable cause) {
        close();
    }

    private static void fail(boolean forWrite) throws DatabaseException {
        if (forWrite) {
            throw new DatabaseFullException();
        } else {
            throw new DatabaseException("Cannot read from a non-durable database");
        }
    }
}
