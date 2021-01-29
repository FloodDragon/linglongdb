package com.glodon.linglong.engine.core.page;

import com.glodon.linglong.base.common.Crypto;
import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.concurrent.Latch;
import com.glodon.linglong.base.exception.CorruptDatabaseException;
import com.glodon.linglong.base.exception.DatabaseException;
import com.glodon.linglong.base.exception.IncompleteRestoreException;
import com.glodon.linglong.base.io.*;
import com.glodon.linglong.engine.core.LocalDatabase;
import com.glodon.linglong.engine.core.Node;
import com.glodon.linglong.engine.core.Snapshot;
import com.glodon.linglong.engine.core.lock.CommitLock;
import com.glodon.linglong.engine.event.EventListener;
import com.glodon.linglong.engine.event.EventType;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.EnumSet;
import java.util.function.LongConsumer;

import static com.glodon.linglong.base.common.IOUtils.*;
import static java.lang.System.arraycopy;

/**
 * @author Stereo
 */
public final class DurablePageDb extends PageDb {
    /*

    Header format for first and second pages in file, which is always 512 bytes:

    +------------------------------------------+
    | long: magic number                       |
    | int:  page size                          |
    | int:  commit number                      |
    | int:  checksum                           |
    | page manager header (140 bytes)          |
    +------------------------------------------+
    | reserved (96 bytes)                      |
    +------------------------------------------+
    | extra data (256 bytes)                   |
    +------------------------------------------+

    */

    private static final long MAGIC_NUMBER = 6529720411368701212L;
    private static final long INCOMPLETE_RESTORE = 1295383629602457917L;

    private static final int I_MAGIC_NUMBER = 0;
    private static final int I_PAGE_SIZE = I_MAGIC_NUMBER + 8;
    private static final int I_COMMIT_NUMBER = I_PAGE_SIZE + 4;
    private static final int I_CHECKSUM = I_COMMIT_NUMBER + 4;
    private static final int I_MANAGER_HEADER = I_CHECKSUM + 4;
    private static final int I_EXTRA_DATA = 256;

    private static final int MINIMUM_PAGE_SIZE = 512;

    private final Crypto mCrypto;
    private final SnapshotPageArray mPageArray;
    private final PageManager mPageManager;

    private final Latch mHeaderLatch;
    private int mCommitNumber;

    public static DurablePageDb open(EventListener debugListener,
                                     boolean explicitPageSize, int pageSize,
                                     File[] files, FileFactory factory, EnumSet<OpenOption> options,
                                     PageCache cache, Crypto crypto, boolean destroy)
            throws IOException {
        while (true) {
            try {
                return new DurablePageDb
                        (debugListener,
                                openPageArray(pageSize, files, factory, options),
                                cache, crypto, destroy);
            } catch (WrongPageSize e) {
                if (explicitPageSize) {
                    throw e.rethrow();
                }
                pageSize = e.mActual;
                explicitPageSize = true;
            }
        }
    }

    public static DurablePageDb open(EventListener debugListener,
                                     PageArray rawArray, PageCache cache, Crypto crypto, boolean destroy)
            throws IOException {
        try {
            return new DurablePageDb(debugListener, rawArray, cache, crypto, destroy);
        } catch (WrongPageSize e) {
            throw e.rethrow();
        }
    }

    private static PageArray openPageArray(int pageSize, File[] files, FileFactory factory,
                                           EnumSet<OpenOption> options)
            throws IOException {
        checkPageSize(pageSize);

        if (!options.contains(OpenOption.CREATE)) {
            for (File file : files) {
                if (!file.exists()) {
                    throw new DatabaseException("File does not exist: " + file);
                }
            }
        }

        if (files.length == 0) {
            throw new IllegalArgumentException("No files provided");
        }

        if (files.length == 1) {
            return new FilePageArray(pageSize, files[0], factory, options);
        }

        PageArray[] arrays = new PageArray[files.length];

        try {
            for (int i = 0; i < files.length; i++) {
                arrays[i] = new FilePageArray(pageSize, files[i], factory, options);
            }

            return new StripedPageArray(arrays);
        } catch (Throwable e) {
            for (PageArray pa : arrays) {
                closeQuietly(pa);
            }
            throw e;
        }
    }

    private static void checkPageSize(int pageSize) {
        if (pageSize < MINIMUM_PAGE_SIZE) {
            throw new IllegalArgumentException
                    ("Page size is too small: " + pageSize + " < " + MINIMUM_PAGE_SIZE);
        }
    }

    private DurablePageDb(final EventListener debugListener,
                          final PageArray rawArray, final PageCache cache,
                          final Crypto crypto, final boolean destroy)
            throws IOException, WrongPageSize {
        mCrypto = crypto;

        PageArray array = crypto == null ? rawArray : new CryptoPageArray(rawArray, crypto);

        mPageArray = new SnapshotPageArray(array, rawArray, cache);
        mHeaderLatch = new Latch();

        try {
            int pageSize = mPageArray.pageSize();
            checkPageSize(pageSize);

            if (destroy || mPageArray.isEmpty()) {
                mPageManager = new PageManager(mPageArray);
                mCommitNumber = -1;

                long header = DirectPageOps.p_calloc(pageSize, isDirectIO());
                try {
                    mCommitLock.acquireExclusive();
                    try {
                        commit(false, header, null);
                        commit(false, header, null);
                    } finally {
                        mCommitLock.releaseExclusive();
                    }
                } finally {
                    DirectPageOps.p_delete(header);
                }

                mPageArray.setPageCount(2);
            } else {

                mPageArray.sync(false);

                long header0 = DirectPageOps.p_null();
                long header1 = DirectPageOps.p_null();

                try {
                    final long header;
                    final int commitNumber;
                    findHeader:
                    {
                        int pageSize0;
                        int commitNumber0, commitNumber1;
                        CorruptDatabaseException ex0;

                        try {
                            header0 = readHeader(0);
                            commitNumber0 = DirectPageOps.p_intGetLE(header0, I_COMMIT_NUMBER);
                            pageSize0 = DirectPageOps.p_intGetLE(header0, I_PAGE_SIZE);
                            ex0 = null;
                        } catch (IncompleteRestoreException e) {
                            throw e;
                        } catch (CorruptDatabaseException e) {
                            header0 = DirectPageOps.p_null();
                            commitNumber0 = -1;
                            pageSize0 = pageSize;
                            ex0 = e;
                        }

                        if (pageSize0 != pageSize) {
                            throw new WrongPageSize(pageSize, pageSize0);
                        }

                        try {
                            header1 = readHeader(1);
                            commitNumber1 = DirectPageOps.p_intGetLE(header1, I_COMMIT_NUMBER);
                        } catch (IncompleteRestoreException e) {
                            throw e;
                        } catch (CorruptDatabaseException e) {
                            if (ex0 != null) {
                                throw ex0;
                            }
                            header = header0;
                            commitNumber = commitNumber0;
                            break findHeader;
                        }

                        int pageSize1 = DirectPageOps.p_intGetLE(header1, I_PAGE_SIZE);
                        if (pageSize0 != pageSize1) {
                            throw new CorruptDatabaseException
                                    ("Mismatched page sizes: " + pageSize0 + " != " + pageSize1);
                        }

                        if (header0 == DirectPageOps.p_null()) {
                            header = header1;
                            commitNumber = commitNumber1;
                        } else {
                            int diff = commitNumber1 - commitNumber0;
                            if (diff > 0) {
                                header = header1;
                                commitNumber = commitNumber1;
                            } else if (diff < 0) {
                                header = header0;
                                commitNumber = commitNumber0;
                            } else {
                                throw new CorruptDatabaseException
                                        ("Both headers have same commit number: " + commitNumber0);
                            }
                        }
                    }

                    mHeaderLatch.acquireExclusive();
                    mCommitNumber = commitNumber;
                    mHeaderLatch.releaseExclusive();

                    if (debugListener != null) {
                        debugListener.notify(EventType.DEBUG, "PAGE_SIZE: %1$d", pageSize);
                        debugListener.notify(EventType.DEBUG, "COMMIT_NUMBER: %1$d", commitNumber);
                    }

                    mPageManager = new PageManager
                            (debugListener, mPageArray, header, I_MANAGER_HEADER);
                } finally {
                    DirectPageOps.p_delete(header0);
                    DirectPageOps.p_delete(header1);
                }
            }
        } catch (WrongPageSize e) {
            delete();
            closeQuietly(this);
            throw e;
        } catch (Throwable e) {
            delete();
            throw closeOnFailure(e);
        }
    }

    @Override
    public void delete() {
        if (mPageManager != null) {
            mPageManager.delete();
        }
    }

    @Override
    public boolean isDurable() {
        return true;
    }

    @Override
    public boolean isDirectIO() {
        return mPageArray.isDirectIO();
    }

    @Override
    public int allocMode() {
        return 0;
    }

    @Override
    public Node allocLatchedNode(LocalDatabase db, int mode) throws IOException {
        long nodeId = allocPage();
        try {
            Node node = db.allocLatchedNode(nodeId, mode);
            node.setId(nodeId);
            return node;
        } catch (Throwable e) {
            try {
                recyclePage(nodeId);
            } catch (Throwable e2) {
                Utils.suppress(e, e2);
            }
            throw e;
        }
    }

    @Override
    public int pageSize() {
        return mPageArray.pageSize();
    }

    @Override
    public long pageCount() throws IOException {
        return mPageArray.getPageCount();
    }

    @Override
    public void pageLimit(long limit) {
        mPageManager.pageLimit(limit);
    }

    @Override
    public long pageLimit() {
        return mPageManager.pageLimit();
    }

    @Override
    public void pageLimitOverride(long bytes) {
        mPageManager.pageLimitOverride(bytes);
    }

    @Override
    public Stats stats() {
        Stats stats = new Stats();
        mPageManager.addTo(stats);
        return stats;
    }

    @Override
    public void readPage(long id, long page) throws IOException {
        try {
            mPageArray.readPage(id, page, 0, pageSize());
        } catch (Throwable e) {
            throw closeOnFailure(e);
        }
    }

    @Override
    public long allocPage() throws IOException {
        CommitLock.Shared shared = mCommitLock.acquireShared();
        try {
            return mPageManager.allocPage();
        } catch (DatabaseException e) {
            if (e.isRecoverable()) {
                throw e;
            }
            throw closeOnFailure(e);
        } catch (Throwable e) {
            throw closeOnFailure(e);
        } finally {
            shared.release();
        }
    }

    @Override
    public void writePage(long id, long page) throws IOException {
        checkId(id);
        mPageArray.writePage(id, page, 0);
    }

    @Override
    public long evictPage(long id, long page) throws IOException {
        checkId(id);
        return mPageArray.evictPage(id, page);
    }

    @Override
    public void cachePage(long id, long page) throws IOException {
        mPageArray.cachePage(id, page);
    }

    @Override
    public void uncachePage(long id) throws IOException {
        mPageArray.uncachePage(id);
    }

    @Override
    public void deletePage(long id, boolean force) throws IOException {
        checkId(id);
        CommitLock.Shared shared = mCommitLock.acquireShared();
        try {
            mPageManager.deletePage(id, force);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw closeOnFailure(e);
        } finally {
            shared.release();
        }
        mPageArray.uncachePage(id);
    }

    @Override
    public void recyclePage(long id) throws IOException {
        checkId(id);
        CommitLock.Shared shared = mCommitLock.acquireShared();
        try {
            try {
                mPageManager.recyclePage(id);
            } catch (IOException e) {
                mPageManager.deletePage(id, true);
            }
        } catch (Throwable e) {
            throw closeOnFailure(e);
        } finally {
            shared.release();
        }
    }

    @Override
    public long allocatePages(long pageCount) throws IOException {
        if (pageCount <= 0) {
            return 0;
        }

        Stats stats = new Stats();
        mPageManager.addTo(stats);
        pageCount -= stats.freePages;

        if (pageCount <= 0) {
            return 0;
        }

        for (int i = 0; i < pageCount; i++) {
            CommitLock.Shared shared = mCommitLock.acquireShared();
            try {
                mPageManager.allocAndRecyclePage();
            } catch (Throwable e) {
                throw closeOnFailure(e);
            } finally {
                shared.release();
            }
        }

        return pageCount;
    }

    @Override
    public long directPagePointer(long id) throws IOException {
        return mPageArray.directPagePointer(id);
    }

    @Override
    public long dirtyPage(long id) throws IOException {
        return mPageArray.dirtyPage(id);
    }

    @Override
    public long copyPage(long srcId, long dstId) throws IOException {
        return mPageArray.copyPage(srcId, dstId);
    }

    @Override
    public void scanFreeList(LongConsumer dst) throws IOException {
        CommitLock.Shared shared = mCommitLock.acquireShared();
        try {
            scanFreeList(I_MANAGER_HEADER + PageManager.I_REGULAR_QUEUE, dst);
            scanFreeList(I_MANAGER_HEADER + PageManager.I_RECYCLE_QUEUE, dst);
        } finally {
            shared.release();
        }
    }

    private void scanFreeList(int headerOffset, LongConsumer dst) throws IOException {
        PageQueueScanner.scan(mPageArray, mCommitNumber & 1, headerOffset, dst);
    }

    @Override
    public boolean compactionStart(long targetPageCount) throws IOException {
        mCommitLock.acquireExclusive();
        try {
            return mPageManager.compactionStart(targetPageCount);
        } catch (Throwable e) {
            throw closeOnFailure(e);
        } finally {
            mCommitLock.releaseExclusive();
        }
    }

    @Override
    public boolean compactionScanFreeList() throws IOException {
        try {
            return mPageManager.compactionScanFreeList(mCommitLock);
        } catch (Throwable e) {
            throw closeOnFailure(e);
        }
    }

    @Override
    public boolean compactionVerify() throws IOException {
        return mPageManager.compactionVerify();
    }

    @Override
    public boolean compactionEnd() throws IOException {
        try {
            return mPageManager.compactionEnd(mCommitLock);
        } catch (Throwable e) {
            throw closeOnFailure(e);
        }
    }

    @Override
    public void compactionReclaim() throws IOException {
        try {
            mPageManager.compactionReclaim();
        } catch (Throwable e) {
            throw closeOnFailure(e);
        }
    }

    @Override
    public boolean truncatePages() throws IOException {
        return mPageManager.truncatePages();
    }

    @Override
    public int extraCommitDataOffset() {
        return I_EXTRA_DATA;
    }

    @Override
    public void commit(boolean resume, long header, final CommitCallback callback)
            throws IOException {
        CommitLock.Shared shared = mCommitLock.acquireShared();

        try {
            mHeaderLatch.acquireShared();
            final int commitNumber = mCommitNumber + 1;
            mHeaderLatch.releaseShared();

            try {
                if (!resume) {
                    mPageManager.commitStart(header, I_MANAGER_HEADER);
                }
                if (callback != null) {
                    callback.prepare(resume, header);
                }
            } catch (DatabaseException e) {
                if (e.isRecoverable()) {
                    throw e;
                } else {
                    throw closeOnFailure(e);
                }
            }

            try {
                commitHeader(header, commitNumber);
                mPageManager.commitEnd(header, I_MANAGER_HEADER);
            } catch (Throwable e) {
                throw closeOnFailure(e);
            }
        } finally {
            shared.release();
        }
    }

    @Override
    public void readExtraCommitData(byte[] extra) throws IOException {
        try {
            mHeaderLatch.acquireShared();
            try {
                readPartial(mCommitNumber & 1, I_EXTRA_DATA, extra, 0, extra.length);
            } finally {
                mHeaderLatch.releaseShared();
            }
        } catch (Throwable e) {
            throw closeOnFailure(e);
        }
    }

    @Override
    public void close() throws IOException {
        close(null);
    }

    @Override
    public void close(Throwable cause) throws IOException {
        if (mPageArray != null) {
            mPageArray.close(cause);
        }
    }

    public OutputStream encrypt(OutputStream out) throws IOException {
        if (mCrypto != null) {
            try {
                out = mCrypto.newEncryptingStream(0, out);
            } catch (GeneralSecurityException e) {
                throw new DatabaseException(e);
            }
        }
        return out;
    }

    public InputStream decrypt(InputStream in) throws IOException {
        if (mCrypto != null) {
            try {
                in = mCrypto.newDecryptingStream(0, in);
            } catch (GeneralSecurityException e) {
                throw new DatabaseException(e);
            }
        }
        return in;
    }

    public Snapshot beginSnapshot(LocalDatabase db) throws IOException {
        mHeaderLatch.acquireShared();
        try {
            long pageCount, redoPos;
            long header = DirectPageOps.p_alloc(MINIMUM_PAGE_SIZE, isDirectIO());
            try {
                mPageArray.readPage(mCommitNumber & 1, header, 0, MINIMUM_PAGE_SIZE);
                pageCount = PageManager.readTotalPageCount(header, I_MANAGER_HEADER);
                redoPos = LocalDatabase.readRedoPosition(header, I_EXTRA_DATA);
            } finally {
                DirectPageOps.p_delete(header);
            }
            return mPageArray.beginSnapshot(db, pageCount, redoPos);
        } finally {
            mHeaderLatch.releaseShared();
        }
    }

    public static PageDb restoreFromSnapshot(int pageSize, File[] files, FileFactory factory,
                                             EnumSet<OpenOption> options,
                                             PageCache cache, Crypto crypto, InputStream in)
            throws IOException {
        try {
            if (options.contains(OpenOption.READ_ONLY)) {
                throw new DatabaseException("Cannot restore into a read-only file");
            }

            byte[] buffer;

            if (crypto != null) {
                buffer = new byte[pageSize];
                readFully(in, buffer, 0, buffer.length);
                try {
                    crypto.decryptPage(0, pageSize, buffer, 0);
                } catch (GeneralSecurityException e) {
                    throw new DatabaseException(e);
                }
            } else {
                buffer = new byte[MINIMUM_PAGE_SIZE];
                readFully(in, buffer, 0, buffer.length);
            }

            checkMagicNumber(decodeLongLE(buffer, I_MAGIC_NUMBER));
            pageSize = decodeIntLE(buffer, I_PAGE_SIZE);
            PageArray pa = openPageArray(pageSize, files, factory, options);

            if (!pa.isEmpty()) {
                closeQuietly(pa);
                throw new DatabaseException("Cannot restore into a non-empty file");
            }

            if (pageSize != buffer.length) {
                if (crypto != null) {
                    closeQuietly(pa);
                    throw new CorruptDatabaseException
                            ("Mismatched page size: " + pageSize + " != " + buffer.length);
                } else {
                    byte[] newBuffer = new byte[pageSize];
                    arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                    readFully(in, newBuffer, buffer.length, pageSize - buffer.length);
                    buffer = newBuffer;
                }
            }

            return restoreFromSnapshot(cache, crypto, in, buffer, pa);
        } finally {
            closeQuietly(in);
        }
    }

    public static PageDb restoreFromSnapshot(PageArray pa, PageCache cache, Crypto crypto, InputStream in)
            throws IOException {
        try {
            if (!pa.isEmpty()) {
                closeQuietly(pa);
                throw new DatabaseException("Cannot restore into a non-empty file");
            }

            byte[] buffer = new byte[pa.pageSize()];
            readFully(in, buffer, 0, buffer.length);

            try {
                crypto.decryptPage(0, buffer.length, buffer, 0);
            } catch (GeneralSecurityException e) {
                throw new DatabaseException(e);
            }

            checkMagicNumber(decodeLongLE(buffer, I_MAGIC_NUMBER));
            int pageSize = decodeIntLE(buffer, I_PAGE_SIZE);

            if (pageSize != buffer.length) {
                closeQuietly(pa);
                throw new CorruptDatabaseException
                        ("Mismatched page size: " + pageSize + " != " + buffer.length);
            }

            return restoreFromSnapshot(cache, crypto, in, buffer, pa);
        } finally {
            closeQuietly(in);
        }
    }

    private static PageDb restoreFromSnapshot(PageCache cache, Crypto crypto, InputStream in,
                                              byte[] buffer, PageArray pa)
            throws IOException {
        encodeLongLE(buffer, I_MAGIC_NUMBER, INCOMPLETE_RESTORE);

        if (crypto != null) {
            try {
                crypto.encryptPage(0, pa.pageSize(), buffer, 0);
            } catch (GeneralSecurityException e) {
                closeQuietly(pa);
                throw new DatabaseException(e);
            }
        }

        long bufferPage = DirectPageOps.p_transfer(buffer, pa.isDirectIO());

        try {
            pa.writePage(0, bufferPage);
            pa.sync(false);

            long index = 1;
            while (true) {
                int amt = in.read(buffer);
                if (amt < 0) {
                    break;
                }
                readFully(in, buffer, amt, buffer.length - amt);
                pa.writePage(index, DirectPageOps.p_transferTo(buffer, bufferPage));
                index++;
            }

            pa.sync(false);
            pa.readPage(0, bufferPage);

            try {
                if (crypto != null) {
                    crypto.decryptPage(0, pa.pageSize(), bufferPage, 0);
                }

                DirectPageOps.p_longPutLE(bufferPage, I_MAGIC_NUMBER, MAGIC_NUMBER);

                if (crypto != null) {
                    crypto.encryptPage(0, pa.pageSize(), bufferPage, 0);
                }
            } catch (GeneralSecurityException e) {
                throw new DatabaseException(e);
            }

            pa.writePage(0, bufferPage);

            pa.sync(true);
        } catch (Throwable e) {
            closeQuietly(pa);
            throw e;
        } finally {
            DirectPageOps.p_delete(bufferPage);
        }

        try {
            return new DurablePageDb(null, pa, cache, crypto, false);
        } catch (WrongPageSize e) {
            throw e.rethrow();
        }
    }

    private IOException closeOnFailure(Throwable e) throws IOException {
        throw Utils.closeOnFailure(this, e);
    }

    private static void checkId(long id) {
        if (id <= 1) {
            throw new IllegalArgumentException("Illegal page id: " + id);
        }
    }

    private void commitHeader(final long header, final int commitNumber)
            throws IOException {
        final PageArray array = mPageArray;

        DirectPageOps.p_longPutLE(header, I_MAGIC_NUMBER, MAGIC_NUMBER);
        DirectPageOps.p_intPutLE(header, I_PAGE_SIZE, array.pageSize());
        DirectPageOps.p_intPutLE(header, I_COMMIT_NUMBER, commitNumber);

        setHeaderChecksum(header);

        int dupCount = pageSize() / MINIMUM_PAGE_SIZE;
        for (int i = 1; i < dupCount; i++) {
            DirectPageOps.p_copy(header, 0, header, i * MINIMUM_PAGE_SIZE, MINIMUM_PAGE_SIZE);
        }

        array.sync(true);

        mHeaderLatch.acquireExclusive();
        try {
            array.writePage(commitNumber & 1, header);
            mCommitNumber = commitNumber;
        } finally {
            mHeaderLatch.releaseExclusive();
        }

        array.syncPage(commitNumber & 1);
    }

    private static int setHeaderChecksum(long header) {
        DirectPageOps.p_intPutLE(header, I_CHECKSUM, 0);
        int checksum = DirectPageOps.p_crc32(header, 0, MINIMUM_PAGE_SIZE);
        DirectPageOps.p_intPutLE(header, I_CHECKSUM, checksum);
        return checksum;
    }

    private long readHeader(int id) throws IOException {
        long header = DirectPageOps.p_alloc(MINIMUM_PAGE_SIZE, isDirectIO());

        try {
            try {
                mPageArray.readPage(id, header, 0, MINIMUM_PAGE_SIZE);
            } catch (EOFException e) {
                throw new CorruptDatabaseException("File is smaller than expected");
            }

            checkMagicNumber(DirectPageOps.p_longGetLE(header, I_MAGIC_NUMBER));

            int checksum = DirectPageOps.p_intGetLE(header, I_CHECKSUM);

            int newChecksum = setHeaderChecksum(header);
            if (newChecksum != checksum) {
                throw new CorruptDatabaseException
                        ("Header checksum mismatch: " + newChecksum + " != " + checksum);
            }

            return header;
        } catch (Throwable e) {
            DirectPageOps.p_delete(header);
            throw e;
        }
    }

    private void readPartial(long index, int start, byte[] buf, int offset, int length)
            throws IOException {
        long page;
        int readLen;

        if (isDirectIO()) {
            readLen = pageSize();
            page = DirectPageOps.p_alloc(readLen, true);
        } else {
            readLen = start + length;
            page = DirectPageOps.p_alloc(readLen, false);
        }

        try {
            mPageArray.readPage(index, page, 0, readLen);
            DirectPageOps.p_copyToArray(page, start, buf, offset, length);
        } finally {
            DirectPageOps.p_delete(page);
        }
    }

    private static void checkMagicNumber(long magic) throws CorruptDatabaseException {
        if (magic != MAGIC_NUMBER) {
            if (magic == INCOMPLETE_RESTORE) {
                throw new IncompleteRestoreException();
            }
            throw new CorruptDatabaseException("Wrong magic number: " + magic);
        }
    }
}
