package com.linglong.engine.core.frame;


import com.linglong.base.common.CauseCloseable;
import com.linglong.base.common.IOUtils;
import com.linglong.base.common.Utils;
import com.linglong.engine.config.DatabaseConfig;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.lock.CommitLock;
import com.linglong.engine.observer.CompactionObserver;
import com.linglong.engine.observer.VerificationObserver;
import com.linglong.engine.core.tx.Transaction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

/**
 * @author Stereo
 */
public interface Database extends CauseCloseable, Flushable {

    static Database open(DatabaseConfig config) throws IOException {
        return config.open(false, null);
    }

    static Database destroy(DatabaseConfig config) throws IOException {
        return config.open(true, null);
    }

    Index findIndex(byte[] name) throws IOException;

    default Index findIndex(String name) throws IOException {
        return findIndex(name.getBytes(StandardCharsets.UTF_8));
    }

    public abstract Index openIndex(byte[] name) throws IOException;

    default Index openIndex(String name) throws IOException {
        return openIndex(name.getBytes(StandardCharsets.UTF_8));
    }

    Index indexById(long id) throws IOException;

    default Index indexById(byte[] id) throws IOException {
        if (id.length != 8) {
            throw new IllegalArgumentException("Expected an 8 byte identifier: " + id.length);
        }
        return indexById(IOUtils.decodeLongBE(id, 0));
    }

    void renameIndex(Index index, byte[] newName) throws IOException;

    default void renameIndex(Index index, String newName) throws IOException {
        renameIndex(index, newName.getBytes(StandardCharsets.UTF_8));
    }

    Runnable deleteIndex(Index index) throws IOException;

    Index newTemporaryIndex() throws IOException;

    View indexRegistryByName() throws IOException;

    View indexRegistryById() throws IOException;

    default Transaction newTransaction() {
        return newTransaction(null);
    }

    Transaction newTransaction(DurabilityMode durabilityMode);

    Sorter newSorter(Executor executor) throws IOException;

    long preallocate(long bytes) throws IOException;

    default void capacityLimit(long bytes) {
        throw new UnsupportedOperationException();
    }

    default long capacityLimit() {
        return -1;
    }

    default void capacityLimitOverride(long bytes) {
        throw new UnsupportedOperationException();
    }

    Snapshot beginSnapshot() throws IOException;

    static Database restoreFromSnapshot(DatabaseConfig config, InputStream in)
            throws IOException {
        return config.open(false, in);
    }

    void createCachePrimer(OutputStream out) throws IOException;

    void applyCachePrimer(InputStream in) throws IOException;

    Stats stats();

    class Stats implements Cloneable, Serializable {
        private static final long serialVersionUID = 3L;

        public int pageSize;
        public long freePages;
        public long totalPages;
        public long cachedPages;
        public long dirtyPages;
        public int openIndexes;
        public long lockCount;
        public long cursorCount;
        public long txnCount;
        public long txnsCreated;

        public int pageSize() {
            return pageSize;
        }

        public long freePages() {
            return freePages;
        }

        public long totalPages() {
            return totalPages;
        }

        public long cachedPages() {
            return cachedPages;
        }

        public long dirtyPages() {
            return dirtyPages;
        }

        public int openIndexes() {
            return openIndexes;
        }

        public long lockCount() {
            return lockCount;
        }

        public long cursorCount() {
            return cursorCount;
        }

        public long transactionCount() {
            return txnCount;
        }

        public long transactionsCreated() {
            return txnsCreated;
        }

        @Override
        public Stats clone() {
            try {
                return (Stats) super.clone();
            } catch (CloneNotSupportedException e) {
                throw Utils.rethrow(e);
            }
        }

        @Override
        public int hashCode() {
            long hash = freePages;
            hash = hash * 31 + totalPages;
            hash = hash * 31 + txnsCreated;
            return (int) Utils.scramble(hash);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj != null && obj.getClass() == Stats.class) {
                Stats other = (Stats) obj;
                return pageSize == other.pageSize
                        && freePages == other.freePages
                        && totalPages == other.totalPages
                        && cachedPages == other.cachedPages
                        && dirtyPages == other.dirtyPages
                        && openIndexes == other.openIndexes
                        && lockCount == other.lockCount
                        && cursorCount == other.cursorCount
                        && txnCount == other.txnCount
                        && txnsCreated == other.txnsCreated;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Database.Stats {pageSize=" + pageSize
                    + ", freePages=" + freePages
                    + ", totalPages=" + totalPages
                    + ", cachedPages=" + cachedPages
                    + ", dirtyPages=" + dirtyPages
                    + ", openIndexes=" + openIndexes
                    + ", lockCount=" + lockCount
                    + ", cursorCount=" + cursorCount
                    + ", transactionCount=" + txnCount
                    + ", transactionsCreated=" + txnsCreated
                    + '}';
        }
    }

    @Override
    void flush() throws IOException;

    void sync() throws IOException;

    void checkpoint() throws IOException;

    void suspendCheckpoints();

    void resumeCheckpoints();

    boolean compactFile(CompactionObserver observer, double target)
            throws IOException;

    boolean verify(VerificationObserver observer) throws IOException;

    @Override
    default void close() throws IOException {
        close(null);
    }

    @Override
    void close(Throwable cause) throws IOException;

    boolean isClosed();

    void shutdown() throws IOException;

    CommitLock commitLock();
}
