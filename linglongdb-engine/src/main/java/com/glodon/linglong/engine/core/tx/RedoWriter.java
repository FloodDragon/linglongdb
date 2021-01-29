package com.glodon.linglong.engine.core.tx;

import com.glodon.linglong.base.concurrent.Latch;
import com.glodon.linglong.engine.config.DurabilityMode;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * @author Stereo
 */
public abstract class RedoWriter extends Latch implements Closeable, Flushable {
    protected long mLastTxnId;

    protected volatile Throwable mCloseCause;

    public RedoWriter() {
    }

    public final void closeCause(Throwable cause) {
        if (cause != null) {
            acquireExclusive();
            if (mCloseCause == null) {
                mCloseCause = cause;
            }
            releaseExclusive();
        }
    }

    public abstract void commitSync(TransactionContext context, long commitPos) throws IOException;

    public abstract void txnCommitSync(LocalTransaction txn, long commitPos) throws IOException;

    public abstract void txnCommitPending(PendingTxn pending) throws IOException;

    public abstract long encoding();

    public abstract RedoWriter txnRedoWriter();

    public abstract boolean shouldCheckpoint(long sizeThreshold);

    public abstract void checkpointPrepare() throws IOException;

    public abstract void checkpointSwitch(TransactionContext[] contexts) throws IOException;

    public abstract long checkpointNumber() throws IOException;

    public abstract long checkpointPosition() throws IOException;

    public abstract long checkpointTransactionId() throws IOException;

    public abstract void checkpointAborted();

    public abstract void checkpointStarted() throws IOException;

    public abstract void checkpointFlushed() throws IOException;

    public abstract void checkpointFinished() throws IOException;

    public long adjustTransactionId(long txnId) {
        return txnId;
    }

    public abstract DurabilityMode opWriteCheck(DurabilityMode mode) throws IOException;

    public abstract boolean shouldWriteTerminators();

    public abstract long write(boolean flush, byte[] bytes, int offset, int length, int commitLen)
            throws IOException;

    public abstract void alwaysFlush(boolean enable) throws IOException;

    public abstract void force(boolean metadata) throws IOException;
}
