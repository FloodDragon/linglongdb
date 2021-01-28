package com.glodon.linglong.engine.core.page;

import com.glodon.linglong.base.common.CauseCloseable;
import com.glodon.linglong.engine.core.LocalDatabase;
import com.glodon.linglong.engine.core.Node;
import com.glodon.linglong.engine.core.lock.CommitLock;

import java.io.IOException;
import java.util.function.LongConsumer;

/**
 * @author Stereo
 */
public abstract class PageDb implements CauseCloseable {
    final CommitLock mCommitLock;

    public PageDb() {
        mCommitLock = new CommitLock();
    }

    public abstract void delete();

    public abstract boolean isDurable();

    public abstract boolean isDirectIO();

    public abstract int allocMode();

    public abstract Node allocLatchedNode(LocalDatabase db, int mode) throws IOException;

    public abstract int pageSize();

    public abstract long pageCount() throws IOException;

    public abstract void pageLimit(long limit);

    public abstract long pageLimit();

    public abstract void pageLimitOverride(long limit);

    public abstract Stats stats();

    public static final class Stats {
        public long totalPages;
        public long freePages;

        public String toString() {
            return "PageDb.Stats {totalPages=" + totalPages + ", freePages=" + freePages + '}';
        }
    }

    public abstract void readPage(long id, long page) throws IOException;

    public abstract long allocPage() throws IOException;

    public abstract void writePage(long id, long page) throws IOException;

    public abstract long evictPage(long id, long page) throws IOException;

    public abstract void cachePage(long id, long page) throws IOException;

    public abstract void uncachePage(long id) throws IOException;

    public abstract void deletePage(long id, boolean force) throws IOException;

    public abstract void recyclePage(long id) throws IOException;

    public abstract long allocatePages(long pageCount) throws IOException;

    public long directPagePointer(long id) throws IOException {
        throw new UnsupportedOperationException();
    }

    public long dirtyPage(long id) throws IOException {
        throw new UnsupportedOperationException();
    }

    public long copyPage(long srcId, long dstId) throws IOException {
        throw new UnsupportedOperationException();
    }

    public CommitLock commitLock() {
        return mCommitLock;
    }

    public abstract void scanFreeList(LongConsumer dst) throws IOException;

    public abstract boolean compactionStart(long targetPageCount) throws IOException;

    public abstract boolean compactionScanFreeList() throws IOException;

    public abstract boolean compactionVerify() throws IOException;

    public abstract boolean compactionEnd() throws IOException;

    public abstract void compactionReclaim() throws IOException;

    public abstract boolean truncatePages() throws IOException;

    public abstract int extraCommitDataOffset();

    public abstract void commit(boolean resume, long header, CommitCallback callback)
            throws IOException;

    @FunctionalInterface
    public interface CommitCallback {

        void prepare(boolean resume, long header) throws IOException;
    }

    public abstract void readExtraCommitData(byte[] extra) throws IOException;
}
