package com.glodon.linglong.engine.core.page;


import com.glodon.linglong.base.io.PageArray;
import com.glodon.linglong.engine.core.LocalDatabase;
import com.glodon.linglong.engine.core.Snapshot;
import com.glodon.linglong.engine.core.temp.TempFileManager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import static java.lang.System.arraycopy;

/**
 * @author Stereo
 */
public final class SnapshotPageArray extends PageArray {
    private final PageArray mSource;
    private final PageArray mRawSource;
    private final PageCache mCache;

    private volatile Object mSnapshots;

    public SnapshotPageArray(PageArray source, PageArray rawSource, PageCache cache) {
        super(source.pageSize());
        mSource = source;
        mRawSource = rawSource;
        mCache = cache;
    }

    @Override
    public boolean isDirectIO() {
        return mSource.isDirectIO();
    }

    @Override
    public boolean isReadOnly() {
        return mSource.isReadOnly();
    }

    @Override
    public boolean isEmpty() throws IOException {
        return mSource.isEmpty();
    }

    @Override
    public long getPageCount() throws IOException {
        return mSource.getPageCount();
    }

    @Override
    public void setPageCount(long count) throws IOException {
        synchronized (this) {
            if (mSnapshots == null) {
                mSource.setPageCount(count);
                return;
            }
        }
        throw new IllegalStateException();
    }

    @Override
    public long getPageCountLimit() throws IOException {
        return mSource.getPageCountLimit();
    }

    @Override
    public void readPage(long index, byte[] dst, int offset, int length) throws IOException {
        PageCache cache = mCache;
        if (cache == null || !cache.remove(index, dst, offset, length)) {
            mSource.readPage(index, dst, offset, length);
        }
    }

    @Override
    public void readPage(long index, long dstPtr, int offset, int length) throws IOException {
        PageCache cache = mCache;
        if (cache == null || !cache.remove(index, dstPtr, offset, length)) {
            mSource.readPage(index, dstPtr, offset, length);
        }
    }

    @Override
    public void writePage(long index, byte[] src, int offset) throws IOException {
        preWritePage(index);
        cachePage(index, src, offset);
        mSource.writePage(index, src, offset);
    }

    @Override
    public void writePage(long index, long srcPtr, int offset) throws IOException {
        preWritePage(index);
        cachePage(index, srcPtr, offset);
        mSource.writePage(index, srcPtr, offset);
    }

    @Override
    public byte[] evictPage(long index, byte[] buf) throws IOException {
        preWritePage(index);
        cachePage(index, buf, 0);
        return mSource.evictPage(index, buf);
    }

    @Override
    public long evictPage(long index, long bufPtr) throws IOException {
        preWritePage(index);
        cachePage(index, bufPtr, 0);
        return mSource.evictPage(index, bufPtr);
    }

    private void preWritePage(long index) throws IOException {
        if (index < 0) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }

        Object obj = mSnapshots;
        if (obj != null) {
            if (obj instanceof SnapshotImpl) {
                ((SnapshotImpl) obj).capture(index);
            } else for (SnapshotImpl snapshot : (SnapshotImpl[]) obj) {
                snapshot.capture(index);
            }
        }
    }

    @Override
    public void cachePage(long index, byte[] src, int offset) {
        PageCache cache = mCache;
        if (cache != null) {
            cache.add(index, src, offset, true);
        }
    }

    @Override
    public void cachePage(long index, long srcPtr, int offset) {
        PageCache cache = mCache;
        if (cache != null) {
            cache.add(index, srcPtr, offset, true);
        }
    }

    @Override
    public void uncachePage(long index) {
        PageCache cache = mCache;
        if (cache != null) {
            cache.remove(index, PageOps.p_null(), 0, 0);
        }
    }

    @Override
    public long directPagePointer(long index) throws IOException {
        if (mCache != null) {
            throw new IllegalStateException();
        }
        return mSource.directPagePointer(index);
    }

    public long dirtyPage(long index) throws IOException {
        preCopyPage(index);
        return mSource.dirtyPage(index);
    }

    @Override
    public long copyPage(long srcIndex, long dstIndex) throws IOException {
        preCopyPage(dstIndex);
        return mSource.copyPage(srcIndex, dstIndex);
    }

    @Override
    public long copyPageFromPointer(long srcPointer, long dstIndex) throws IOException {
        preCopyPage(dstIndex);
        return mSource.copyPageFromPointer(srcPointer, dstIndex);
    }

    private void preCopyPage(long dstIndex) throws IOException {
        if (mCache != null) {
            throw new IllegalStateException();
        }

        if (dstIndex < 0) {
            throw new IndexOutOfBoundsException(String.valueOf(dstIndex));
        }

        Object obj = mSnapshots;
        if (obj != null) {
            if (obj instanceof SnapshotImpl) {
                ((SnapshotImpl) obj).capture(dstIndex);
            } else for (SnapshotImpl snapshot : (SnapshotImpl[]) obj) {
                snapshot.capture(dstIndex);
            }
        }
    }

    @Override
    public void sync(boolean metadata) throws IOException {
        mSource.sync(metadata);
    }

    @Override
    public void close(Throwable cause) throws IOException {
        if (mCache != null) {
            mCache.close();
        }
        mSource.close(cause);
    }

    /**
     * Supports writing a snapshot of the array, while still permitting
     * concurrent access. Snapshot data is not a valid array file. It must be
     * processed specially by the restoreFromSnapshot method.
     *
     * @param pageCount total number of pages to include in snapshot
     * @param redoPos   redo log position for the snapshot
     */
    Snapshot beginSnapshot(LocalDatabase db, long pageCount, long redoPos) throws IOException {
        pageCount = Math.min(pageCount, getPageCount());

        LocalDatabase nodeCache = db;

        // Snapshot does not decrypt pages.
        PageArray rawSource = mRawSource;
        if (rawSource != mSource) {
            // Cache contents are not encrypted, and so it cannot be used.
            nodeCache = null;
        }

        TempFileManager tfm = db.mTempFileManager;

        SnapshotImpl snapshot = new SnapshotImpl(tfm, pageCount, redoPos, nodeCache, rawSource);

        synchronized (this) {
            Object obj = mSnapshots;
            if (obj == null) {
                mSnapshots = snapshot;
            } else if (obj instanceof SnapshotImpl[]) {
                SnapshotImpl[] snapshots = (SnapshotImpl[]) obj;
                SnapshotImpl[] newSnapshots = new SnapshotImpl[snapshots.length + 1];
                arraycopy(snapshots, 0, newSnapshots, 0, snapshots.length);
                newSnapshots[newSnapshots.length - 1] = snapshot;
                mSnapshots = newSnapshots;
            } else {
                mSnapshots = new SnapshotImpl[]{(SnapshotImpl) obj, snapshot};
            }
        }

        return snapshot;
    }

    synchronized void unregister(SnapshotImpl snapshot) {
        Object obj = mSnapshots;
        if (obj == snapshot) {
            mSnapshots = null;
            return;
        }
        if (!(obj instanceof SnapshotImpl[])) {
            return;
        }

        SnapshotImpl[] snapshots = (SnapshotImpl[]) obj;

        if (snapshots.length == 2) {
            if (snapshots[0] == snapshot) {
                mSnapshots = snapshots[1];
            } else if (snapshots[1] == snapshot) {
                mSnapshots = snapshots[0];
            }
            return;
        }

        int pos;
        find:
        {
            for (pos = 0; pos < snapshots.length; pos++) {
                if (snapshots[pos] == snapshot) {
                    break find;
                }
            }
            return;
        }

        SnapshotImpl[] newSnapshots = new SnapshotImpl[snapshots.length - 1];
        arraycopy(snapshots, 0, newSnapshots, 0, pos);
        arraycopy(snapshots, pos + 1, newSnapshots, pos, newSnapshots.length - pos);
        mSnapshots = newSnapshots;
    }

    // This should be declared in the SnapshotImpl class, but the Java compiler prohibits this
    // for no good reason. This also requires that the field be declared as package-private.
    static final AtomicLongFieldUpdater<SnapshotImpl> mProgressUpdater =
            AtomicLongFieldUpdater.newUpdater(SnapshotImpl.class, "mProgress");

    class SnapshotImpl implements CauseCloseable, Snapshot {
        private final LocalDatabase mNodeCache;
        private final PageArray mRawPageArray;

        private final TempFileManager mTempFileManager;
        private final long mSnapshotPageCount;
        private final long mSnapshotRedoPosition;

        private final Tree mPageCopyIndex;
        private final File mTempFile;

        private final Latch mSnapshotLatch;

        private final Latch[] mCaptureLatches;
        private final byte[][] mCaptureBufferArrays;
        private final /*P*/ byte[][] mCaptureBuffers;

        // The highest page written by the writeTo method.
        volatile long mProgress;

        private volatile Throwable mAbortCause;

        /**
         * @param nodeCache optional
         */
        SnapshotImpl(TempFileManager tfm, long pageCount, long redoPos,
                     LocalDatabase nodeCache, PageArray rawPageArray)
                throws IOException {
            mNodeCache = nodeCache;
            mRawPageArray = rawPageArray;

            mTempFileManager = tfm;
            mSnapshotPageCount = pageCount;
            mSnapshotRedoPosition = redoPos;

            final int pageSize = pageSize();

            mSnapshotLatch = new Latch();

            final int slots = Runtime.getRuntime().availableProcessors() * 4;
            mCaptureLatches = new Latch[slots];
            mCaptureBufferArrays = new byte[slots][];
            /*P*/ // [
            mCaptureBuffers = new byte[slots][];
            /*P*/ // |
            /*P*/ // mCaptureBuffers = new long[slots];
            /*P*/ // ]

            for (int i = 0; i < slots; i++) {
                mCaptureLatches[i] = new Latch();
                mCaptureBufferArrays[i] = new byte[pageSize];
                // Allocates if page is not an array. The copy is not actually required.
                mCaptureBuffers[i] = PageOps.p_transfer(mCaptureBufferArrays[i], isDirectIO());
            }

            DatabaseConfig config = new DatabaseConfig()
                    .pageSize(pageSize).minCacheSize(pageSize * Math.max(100, slots * 16));
            mPageCopyIndex = LocalDatabase.openTemp(tfm, config);
            mTempFile = config.mBaseFile;

            // -2: Not yet started. -1: Started, but nothing written yet.
            mProgress = -2;
        }

        @Override
        public long length() {
            return mSnapshotPageCount * pageSize();
        }

        @Override
        public long position() {
            return mSnapshotRedoPosition;
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            mSnapshotLatch.acquireExclusive();
            try {
                long progress = mProgress;
                if (progress == Long.MAX_VALUE) {
                    throw aborted(mAbortCause);
                }
                if (progress > -2) {
                    throw new IllegalStateException("Snapshot already started");
                }
                mProgress = -1;
            } finally {
                mSnapshotLatch.releaseExclusive();
            }

            final byte[] pageBufferArray = new byte[pageSize()];
            // Allocates if page is not an array. The copy is not actually required.
            final /*P*/ byte[] pageBuffer = PageOps.p_transfer(pageBufferArray, isDirectIO());

            final LocalDatabase cache = mNodeCache;
            final long count = mSnapshotPageCount;

            Transaction txn = mPageCopyIndex.mDatabase.newTransaction();
            try {
                // Disable writes to the undo log and fragmented value trash.
                txn.lockMode(LockMode.UNSAFE);

                Cursor c = mPageCopyIndex.newCursor(txn);
                try {
                    for (long index = 0; index < count; index++) {
                        byte[] key = new byte[8];
                        encodeLongBE(key, 0, index);
                        txn.lockExclusive(mPageCopyIndex.getId(), key);

                        c.findNearby(key);
                        byte[] value = c.value();

                        if (value != null) {
                            // Advance progress before releasing the lock.
                            advanceProgress(index);
                            c.commit(null);
                        } else {
                            read:
                            {
                                Node node;
                                if (cache != null && (node = cache.nodeMapGet(index)) != null) {
                                    if (node.tryAcquireShared()) try {
                                        if (node.mId == index
                                                && node.mCachedState == Node.CACHED_CLEAN) {
                                            PageOps.p_copy(node.mPage, 0, pageBuffer, 0, pageSize());
                                            break read;
                                        }
                                    } finally {
                                        node.releaseShared();
                                    }
                                }

                                mRawPageArray.readPage(index, pageBuffer);
                            }

                            // Advance progress after copying the captured value and before
                            // releasing the lock.
                            advanceProgress(index);
                            txn.commit();

                            value = PageOps.p_copyIfNotArray(pageBuffer, pageBufferArray);
                        }

                        out.write(value);
                    }
                } catch (Throwable e) {
                    if (mProgress == Long.MAX_VALUE) {
                        throw aborted(mAbortCause);
                    }
                    throw e;
                } finally {
                    c.reset();
                    PageOps.p_delete(pageBuffer);
                    close();
                }
            } finally {
                txn.reset();
            }
        }

        private void advanceProgress(long index) {
            if (!mProgressUpdater.compareAndSet(this, index - 1, index)) {
                // If closed, the caller's exception handler must detect this.
                throw new IllegalStateException();
            }
        }

        void capture(final long index) {
            if (index >= mSnapshotPageCount || index <= mProgress) {
                return;
            }

            Cursor c = mPageCopyIndex.newCursor(Transaction.BOGUS);
            try {
                c.autoload(false);
                byte[] key = new byte[8];
                encodeLongBE(key, 0, index);
                c.find(key);

                if (c.value() != null) {
                    // Already captured.
                    return;
                }

                // Lock and check again.

                Transaction txn = mPageCopyIndex.mDatabase.newTransaction();
                try {
                    c.link(txn);
                    c.load();

                    if (c.value() != null || index <= mProgress) {
                        // Already captured or writer has advanced ahead.
                        txn.reset();
                        return;
                    }

                    int slot = ThreadLocalRandom.current().nextInt(mCaptureLatches.length);

                    Latch latch = mCaptureLatches[slot];
                    latch.acquireExclusive();
                    try {
                        byte[] bufferArray = mCaptureBufferArrays[slot];
                        if (bufferArray != null) {
                            /*P*/
                            byte[] buffer = mCaptureBuffers[slot];
                            mRawPageArray.readPage(index, buffer);
                            c.commit(PageOps.p_copyIfNotArray(buffer, bufferArray));
                        }
                    } finally {
                        latch.releaseExclusive();
                    }
                } catch (Throwable e) {
                    txn.reset();
                    throw e;
                }
            } catch (Throwable e) {
                abort(e);
            } finally {
                c.reset();
            }
        }

        @Override
        public void close() throws IOException {
            close(null);
        }

        @Override
        public void close(Throwable cause) throws IOException {
            if (mProgress == Long.MAX_VALUE) {
                return;
            }

            mSnapshotLatch.acquireExclusive();
            try {
                if (mProgress == Long.MAX_VALUE) {
                    return;
                }

                mProgress = Long.MAX_VALUE;
                mAbortCause = cause;

                for (int i = 0; i < mCaptureLatches.length; i++) {
                    Latch latch = mCaptureLatches[i];
                    latch.acquireExclusive();
                    try {
                        mCaptureBufferArrays[i] = null;
                        PageOps.p_delete(mCaptureBuffers[i]);
                    } finally {
                        latch.releaseExclusive();
                    }
                }
            } finally {
                mSnapshotLatch.releaseExclusive();
            }

            unregister(this);
            closeQuietly(mPageCopyIndex.mDatabase);
            mTempFileManager.deleteTempFile(mTempFile);
        }

        private void abort(Throwable e) {
            try {
                close(e);
            } catch (IOException e2) {
                // Ignore.
            }
        }

        private IOException aborted(Throwable cause) {
            return new IOException("Snapshot closed", cause);
        }
    }
}
