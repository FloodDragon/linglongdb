package com.glodon.linglong.engine.core.tx;

import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.concurrent.Latch;
import com.glodon.linglong.base.exception.UnmodifiableReplicaException;
import com.glodon.linglong.engine.config.DurabilityMode;
import com.glodon.linglong.engine.core.Database;

import java.io.Flushable;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * @author Stereo
 */
public final class TransactionContext extends Latch implements Flushable {
    private final static AtomicLongFieldUpdater<TransactionContext> cHighTxnIdUpdater =
            AtomicLongFieldUpdater.newUpdater(TransactionContext.class, "mHighTxnId");

    private final int mTxnStride;

    private long mInitialTxnId;
    private volatile long mHighTxnId;
    private UndoLog mTopUndoLog;
    private int mUndoLogCount;

    private final byte[] mRedoBuffer;
    private int mRedoPos;
    private int mRedoTerminatePos;
    private long mRedoFirstTxnId;
    private long mRedoLastTxnId;
    private RedoWriter mRedoWriter;
    private boolean mRedoWriterLatched;
    private long mRedoWriterPos;

    public TransactionContext(int txnStride, int redoBufferSize) {
        if (txnStride <= 0) {
            throw new IllegalArgumentException();
        }
        mTxnStride = txnStride;
        mRedoBuffer = new byte[redoBufferSize];
    }

    public synchronized void addStats(Database.Stats stats) {
        stats.txnCount += mUndoLogCount;
        stats.txnsCreated += mHighTxnId / mTxnStride;
    }

    public void resetTransactionId(long txnId) {
        if (txnId < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (this) {
            mInitialTxnId = txnId;
            mHighTxnId = txnId;
        }
    }

    long nextTransactionId() {
        long txnId = cHighTxnIdUpdater.addAndGet(this, mTxnStride);

        if (txnId <= 0) {
            synchronized (this) {
                if (mHighTxnId <= 0 && (txnId = mHighTxnId + mTxnStride) <= 0) {
                    txnId = mInitialTxnId % mTxnStride;
                }
                mHighTxnId = txnId;
            }
        }

        return txnId;
    }

    void acquireRedoLatch() {
        acquireExclusive();
    }

    public void releaseRedoLatch() throws IOException {
        try {
            if (mRedoWriterLatched) try {
                if (mRedoFirstTxnId == 0) {
                    int length = mRedoPos;
                    if (length != 0) {
                        try {
                            mRedoWriterPos = mRedoWriter.write
                                    (false, mRedoBuffer, 0, length, mRedoTerminatePos);
                        } catch (IOException e) {
                            throw Utils.rethrow(e, mRedoWriter.mCloseCause);
                        }
                        mRedoPos = 0;
                        mRedoTerminatePos = 0;
                    }
                }
            } finally {
                mRedoWriter.releaseExclusive();
                mRedoWriterLatched = false;
            }
        } finally {
            releaseExclusive();
        }
    }

    public void fullAcquireRedoLatch(RedoWriter redo) throws IOException {
        acquireExclusive();
        try {
            if (redo != mRedoWriter) {
                switchRedo(redo);
            }
            redo.acquireExclusive();
        } catch (Throwable e) {
            releaseRedoLatch();
            throw e;
        }

        mRedoWriterLatched = true;
    }

    long redoStoreAutoCommit(RedoWriter redo, long indexId, byte[] key, byte[] value,
                             DurabilityMode mode)
            throws IOException {
        Utils.keyCheck(key);
        mode = redo.opWriteCheck(mode);

        acquireRedoLatch();
        try {
            if (value == null) {
                redoWriteOp(redo, RedoOps.OP_DELETE, indexId);
                redoWriteUnsignedVarInt(key.length);
                redoWriteBytes(key, true);
            } else {
                redoWriteOp(redo, RedoOps.OP_STORE, indexId);
                redoWriteUnsignedVarInt(key.length);
                redoWriteBytes(key, false);
                redoWriteUnsignedVarInt(value.length);
                redoWriteBytes(value, true);
            }

            return redoNonTxnTerminateCommit(redo, mode);
        } finally {
            releaseRedoLatch();
        }
    }

    long redoStoreNoLockAutoCommit(RedoWriter redo, long indexId, byte[] key, byte[] value,
                                   DurabilityMode mode)
            throws IOException {
        Utils.keyCheck(key);
        mode = redo.opWriteCheck(mode);

        acquireRedoLatch();
        try {
            if (value == null) {
                redoWriteOp(redo, RedoOps.OP_DELETE_NO_LOCK, indexId);
                redoWriteUnsignedVarInt(key.length);
                redoWriteBytes(key, true);
            } else {
                redoWriteOp(redo, RedoOps.OP_STORE_NO_LOCK, indexId);
                redoWriteUnsignedVarInt(key.length);
                redoWriteBytes(key, false);
                redoWriteUnsignedVarInt(value.length);
                redoWriteBytes(value, true);
            }

            return redoNonTxnTerminateCommit(redo, mode);
        } finally {
            releaseRedoLatch();
        }
    }

    public long redoRenameIndexCommitFinal(RedoWriter redo, long txnId, long indexId,
                                           byte[] newName, DurabilityMode mode)
            throws IOException {
        mode = redo.opWriteCheck(mode);

        acquireRedoLatch();
        try {
            redoWriteTxnOp(redo, RedoOps.OP_RENAME_INDEX, txnId);
            redoWriteLongLE(indexId);
            redoWriteUnsignedVarInt(newName.length);
            redoWriteBytes(newName, true);
            redoWriteTerminator(redo);
            return redoFlushCommit(mode);
        } finally {
            releaseRedoLatch();
        }
    }

    public long redoDeleteIndexCommitFinal(RedoWriter redo, long txnId, long indexId,
                                           DurabilityMode mode)
            throws IOException {
        mode = redo.opWriteCheck(mode);

        acquireRedoLatch();
        try {
            redoWriteTxnOp(redo, RedoOps.OP_DELETE_INDEX, txnId);
            redoWriteLongLE(indexId);
            redoWriteTerminator(redo);
            return redoFlushCommit(mode);
        } finally {
            releaseRedoLatch();
        }
    }

    long redoPrepare(RedoWriter redo, long txnId, DurabilityMode mode) throws IOException {
        mode = redo.opWriteCheck(mode);

        acquireRedoLatch();
        try {
            redoWriteTxnOp(redo, RedoOps.OP_TXN_PREPARE, txnId);
            redoWriteTerminator(redo);
            return redoFlushCommit(mode);
        } finally {
            releaseRedoLatch();
        }
    }

    void redoEnter(RedoWriter redo, long txnId) throws IOException {
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            redoWriteTxnOp(redo, RedoOps.OP_TXN_ENTER, txnId);
            redoWriteTerminator(redo);
        } finally {
            releaseRedoLatch();
        }
    }

    void redoRollback(RedoWriter redo, long txnId) throws IOException {
        DurabilityMode mode = redo.opWriteCheck(DurabilityMode.NO_FLUSH);

        acquireRedoLatch();
        try {
            redoWriteTxnOp(redo, RedoOps.OP_TXN_ROLLBACK, txnId);
            redoWriteTerminator(redo);
            redoFlushCommit(mode);
        } finally {
            releaseRedoLatch();
        }
    }

    void redoRollbackFinal(RedoWriter redo, long txnId) throws IOException {
        DurabilityMode mode = redo.opWriteCheck(DurabilityMode.NO_FLUSH);

        acquireRedoLatch();
        try {
            redoWriteTxnOp(redo, RedoOps.OP_TXN_ROLLBACK_FINAL, txnId);
            redoWriteTerminator(redo);
            redoFlushCommit(mode);
        } finally {
            releaseRedoLatch();
        }
    }

    void redoCommit(RedoWriter redo, long txnId) throws IOException {
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            redoWriteTxnOp(redo, RedoOps.OP_TXN_COMMIT, txnId);
            redoWriteTerminator(redo);
        } finally {
            releaseRedoLatch();
        }
    }

    long redoCommitFinal(RedoWriter redo, long txnId, DurabilityMode mode)
            throws IOException {
        mode = redo.opWriteCheck(mode);

        acquireRedoLatch();
        try {
            redoWriteTxnOp(redo, RedoOps.OP_TXN_COMMIT_FINAL, txnId);
            redoWriteTerminator(redo);
            return redoFlushCommit(mode);
        } finally {
            releaseRedoLatch();
        }
    }

    void redoStore(RedoWriter redo, byte op, long txnId, long indexId,
                   byte[] key, byte[] value)
            throws IOException {
        Utils.keyCheck(key);
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            doRedoStore(redo, op, txnId, indexId, key, value);
        } finally {
            releaseRedoLatch();
        }
    }

    long redoStoreCommitFinal(RedoWriter redo, long txnId, long indexId,
                              byte[] key, byte[] value, DurabilityMode mode)
            throws IOException {
        Utils.keyCheck(key);
        mode = redo.opWriteCheck(mode);

        acquireRedoLatch();
        try {
            doRedoStore(redo, RedoOps.OP_TXN_STORE_COMMIT_FINAL, txnId, indexId, key, value);
            return redoFlushCommit(mode);
        } finally {
            releaseRedoLatch();
        }
    }

    private void doRedoStore(RedoWriter redo, byte op, long txnId, long indexId,
                             byte[] key, byte[] value)
            throws IOException {
        redoWriteTxnOp(redo, op, txnId);
        redoWriteLongLE(indexId);
        redoWriteUnsignedVarInt(key.length);
        redoWriteBytes(key, false);
        redoWriteUnsignedVarInt(value.length);
        redoWriteBytes(value, true);
        redoWriteTerminator(redo);
    }

    void redoDelete(RedoWriter redo, byte op, long txnId, long indexId, byte[] key)
            throws IOException {
        Utils.keyCheck(key);
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            doRedoDelete(redo, op, txnId, indexId, key);
        } finally {
            releaseRedoLatch();
        }
    }

    long redoDeleteCommitFinal(RedoWriter redo, long txnId, long indexId,
                               byte[] key, DurabilityMode mode)
            throws IOException {
        Utils.keyCheck(key);
        mode = redo.opWriteCheck(mode);

        acquireRedoLatch();
        try {
            doRedoDelete(redo, RedoOps.OP_TXN_DELETE_COMMIT_FINAL, txnId, indexId, key);
            return redoFlushCommit(mode);
        } finally {
            releaseRedoLatch();
        }
    }

    private void doRedoDelete(RedoWriter redo, byte op, long txnId, long indexId, byte[] key)
            throws IOException {
        redoWriteTxnOp(redo, op, txnId);
        redoWriteLongLE(indexId);
        redoWriteUnsignedVarInt(key.length);
        redoWriteBytes(key, true);
        redoWriteTerminator(redo);
    }

    void redoCursorRegister(RedoWriter redo, long cursorId, long indexId) throws IOException {
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            redoWriteTxnOp(redo, RedoOps.OP_CURSOR_REGISTER, cursorId);
            redoWriteLongLE(indexId);
            redoWriteTerminator(redo);
            redoFlush(false);
        } finally {
            releaseRedoLatch();
        }
    }

    void redoCursorUnregister(RedoWriter redo, long cursorId) throws IOException {
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            redoWriteTxnOp(redo, RedoOps.OP_CURSOR_UNREGISTER, cursorId);
            redoWriteTerminator(redo);
        } finally {
            releaseRedoLatch();
        }
    }

    void redoCursorStore(RedoWriter redo, long cursorId, long txnId, byte[] key, byte[] value)
            throws IOException {
        Utils.keyCheck(key);
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            redoWriteCursorOp(redo, RedoOps.OP_CURSOR_STORE, cursorId, txnId);
            redoWriteUnsignedVarInt(key.length);
            redoWriteBytes(key, false);
            redoWriteUnsignedVarInt(value.length);
            redoWriteBytes(value, true);
            redoWriteTerminator(redo);
        } finally {
            releaseRedoLatch();
        }
    }

    void redoCursorDelete(RedoWriter redo, long cursorId, long txnId, byte[] key)
            throws IOException {
        Utils.keyCheck(key);
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            redoWriteCursorOp(redo, RedoOps.OP_CURSOR_DELETE, cursorId, txnId);
            redoWriteUnsignedVarInt(key.length);
            redoWriteBytes(key, true);
            redoWriteTerminator(redo);
        } finally {
            releaseRedoLatch();
        }
    }

    void redoCursorFind(RedoWriter redo, long cursorId, long txnId, byte[] key)
            throws IOException {
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            redoWriteCursorOp(redo, RedoOps.OP_CURSOR_FIND, cursorId, txnId);
            redoWriteUnsignedVarInt(key.length);
            redoWriteBytes(key, true);
            redoWriteTerminator(redo);
        } finally {
            releaseRedoLatch();
        }
    }

    void redoCursorValueSetLength(RedoWriter redo, long cursorId, long txnId, long length)
            throws IOException {
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            redoWriteCursorOp(redo, RedoOps.OP_CURSOR_VALUE_SET_LENGTH, cursorId, txnId);
            redoWriteUnsignedVarLong(length);
            redoWriteTerminator(redo);
        } finally {
            releaseRedoLatch();
        }
    }

    void redoCursorValueWrite(RedoWriter redo, long cursorId, long txnId,
                              long pos, byte[] buf, int off, int len)
            throws IOException {
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            redoWriteCursorOp(redo, RedoOps.OP_CURSOR_VALUE_WRITE, cursorId, txnId);
            redoWriteUnsignedVarLong(pos);
            redoWriteUnsignedVarInt(len);
            redoWriteBytes(buf, off, len, true);
            redoWriteTerminator(redo);
        } finally {
            releaseRedoLatch();
        }
    }

    void redoCursorValueClear(RedoWriter redo, long cursorId, long txnId, long pos, long length)
            throws IOException {
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            redoWriteCursorOp(redo, RedoOps.OP_CURSOR_VALUE_CLEAR, cursorId, txnId);
            redoWriteUnsignedVarLong(pos);
            redoWriteUnsignedVarLong(length);
            redoWriteTerminator(redo);
        } finally {
            releaseRedoLatch();
        }
    }

    void redoCustom(RedoWriter redo, long txnId, byte[] message) throws IOException {
        if (message == null) {
            throw new NullPointerException("Message is null");
        }
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            redoWriteTxnOp(redo, RedoOps.OP_TXN_CUSTOM, txnId);
            redoWriteUnsignedVarInt(message.length);
            redoWriteBytes(message, true);
            redoWriteTerminator(redo);
        } finally {
            releaseRedoLatch();
        }
    }

    void redoCustomLock(RedoWriter redo, long txnId, byte[] message, long indexId, byte[] key)
            throws IOException {
        Utils.keyCheck(key);
        if (message == null) {
            throw new NullPointerException("Message is null");
        }
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            redoWriteTxnOp(redo, RedoOps.OP_TXN_CUSTOM_LOCK, txnId);
            redoWriteLongLE(indexId);
            redoWriteUnsignedVarInt(key.length);
            redoWriteBytes(key, false);
            redoWriteUnsignedVarInt(message.length);
            redoWriteBytes(message, true);
            redoWriteTerminator(redo);
        } finally {
            releaseRedoLatch();
        }
    }

    public void doRedoReset(RedoWriter redo) throws IOException {
        redo.opWriteCheck(null);
        redoWriteOp(redo, RedoOps.OP_RESET);
        redoNonTxnTerminateCommit(redo, DurabilityMode.NO_FLUSH);
        assert mRedoWriterLatched;
        redo.mLastTxnId = 0;
    }

    public void redoTimestamp(RedoWriter redo, byte op) throws IOException {
        acquireRedoLatch();
        try {
            doRedoTimestamp(redo, op, DurabilityMode.NO_FLUSH);
        } finally {
            releaseRedoLatch();
        }
    }

    public void doRedoTimestamp(RedoWriter redo, byte op, DurabilityMode mode) throws IOException {
        doRedoOp(redo, op, System.currentTimeMillis(), mode);
    }

    public void doRedoNopRandom(RedoWriter redo, DurabilityMode mode) throws IOException {
        doRedoOp(redo, RedoOps.OP_NOP_RANDOM, ThreadLocalRandom.current().nextLong(), mode);
    }

    private void doRedoOp(RedoWriter redo, byte op, long operand, DurabilityMode mode)
            throws IOException {
        redo.opWriteCheck(null);
        redoWriteOp(redo, op, operand);
        redoNonTxnTerminateCommit(redo, mode);
    }

    public long redoControl(RedoWriter redo, byte[] message) throws IOException {
        if (message == null) {
            throw new NullPointerException("Message is null");
        }
        redo.opWriteCheck(null);

        acquireRedoLatch();
        try {
            redoWriteOp(redo, RedoOps.OP_CONTROL);
            redoWriteUnsignedVarInt(message.length);
            redoWriteBytes(message, true);
            // Must use SYNC to obtain the log position.
            return redoNonTxnTerminateCommit(redo, DurabilityMode.SYNC);
        } finally {
            releaseRedoLatch();
        }
    }

    private long redoNonTxnTerminateCommit(RedoWriter redo, DurabilityMode mode)
            throws IOException {
        mRedoTerminatePos = mRedoPos;

        if (!redo.shouldWriteTerminators()) {
            return redoFlushCommit(mode);
        }

        if (mRedoFirstTxnId != 0) {
            redoWriteIntLE(Utils.nzHash(mRedoLastTxnId));
            return redoFlushCommit(mode);
        }

        int length = mRedoPos;
        int commitLen = mRedoTerminatePos;
        byte[] buffer = mRedoBuffer;
        redo = latchWriter();

        if (length > buffer.length - 4) {
            try {
                mRedoWriterPos = redo.write(false, buffer, 0, length, commitLen);
            } catch (IOException e) {
                throw Utils.rethrow(e, redo.mCloseCause);
            }
            mRedoPos = 0;
            mRedoTerminatePos = 0;
            length = 0;
            commitLen = 0;
        }

        Utils.encodeIntLE(buffer, length, Utils.nzHash(redo.mLastTxnId));
        length += 4;

        boolean flush = mode == DurabilityMode.SYNC || mode == DurabilityMode.NO_SYNC;

        try {
            mRedoWriterPos = redo.write(flush, buffer, 0, length, commitLen);
        } catch (IOException e) {
            throw Utils.rethrow(e, redo.mCloseCause);
        }

        mRedoPos = 0;
        mRedoTerminatePos = 0;

        return mode == DurabilityMode.SYNC ? mRedoWriterPos : 0;
    }

    private void redoWriteTerminator(RedoWriter redo) throws IOException {
        mRedoTerminatePos = mRedoPos;
        if (redo.shouldWriteTerminators()) {
            redoWriteIntLE(Utils.nzHash(mRedoLastTxnId));
        }
    }

    private void redoWriteIntLE(int v) throws IOException {
        byte[] buffer = mRedoBuffer;
        int pos = mRedoPos;
        if (pos > buffer.length - 4) {
            redoFlush(false);
            pos = 0;
        }
        Utils.encodeIntLE(buffer, pos, v);
        mRedoPos = pos + 4;
    }

    private void redoWriteLongLE(long v) throws IOException {
        byte[] buffer = mRedoBuffer;
        int pos = mRedoPos;
        if (pos > buffer.length - 8) {
            redoFlush(false);
            pos = 0;
        }
        Utils.encodeLongLE(buffer, pos, v);
        mRedoPos = pos + 8;
    }

    private void redoWriteUnsignedVarInt(int v) throws IOException {
        byte[] buffer = mRedoBuffer;
        int pos = mRedoPos;
        if (pos > buffer.length - 5) {
            redoFlush(false);
            pos = 0;
        }
        mRedoPos = Utils.encodeUnsignedVarInt(buffer, pos, v);
    }

    private void redoWriteUnsignedVarLong(long v) throws IOException {
        byte[] buffer = mRedoBuffer;
        int pos = mRedoPos;
        if (pos > buffer.length - 9) {
            redoFlush(false);
            pos = 0;
        }
        mRedoPos = Utils.encodeUnsignedVarLong(buffer, pos, v);
    }

    private void redoWriteBytes(byte[] bytes, boolean term) throws IOException {
        redoWriteBytes(bytes, 0, bytes.length, term);
    }

    private void redoWriteBytes(byte[] bytes, int offset, int length, boolean term)
            throws IOException {
        if (length == 0) {
            return;
        }

        byte[] buffer = mRedoBuffer;
        int avail = buffer.length - mRedoPos;

        if (avail >= length) {
            if (mRedoPos == 0 && avail == length) {
                RedoWriter redo = latchWriter();
                mRedoWriterPos = write(redo, bytes, offset, length, term);
            } else {
                System.arraycopy(bytes, offset, buffer, mRedoPos, length);
                mRedoPos += length;
            }
        } else {
            System.arraycopy(bytes, offset, buffer, mRedoPos, avail);
            mRedoPos = buffer.length;

            redoFlush(false);

            offset += avail;
            length -= avail;

            if (length >= buffer.length) {
                mRedoWriterPos = write(mRedoWriter, bytes, offset, length, term);
            } else {
                System.arraycopy(bytes, offset, buffer, 0, length);
                mRedoPos = length;
            }
        }
    }

    private static long write(RedoWriter redo, byte[] bytes, int offset, int length, boolean term)
            throws IOException {
        try {
            return redo.write(false, bytes, offset, length, term ? length : 0);
        } catch (IOException e) {
            throw Utils.rethrow(e, redo.mCloseCause);
        }
    }

    private void redoWriteOp(RedoWriter redo, byte op) throws IOException {
        mRedoPos = doRedoWriteOp(redo, op, 1); // 1 for op
    }

    private void redoWriteOp(RedoWriter redo, byte op, long operand) throws IOException {
        int pos = doRedoWriteOp(redo, op, 1 + 8); // 1 for op, 8 for operand
        Utils.encodeLongLE(mRedoBuffer, pos, operand);
        mRedoPos = pos + 8;
    }

    private int doRedoWriteOp(RedoWriter redo, byte op, int len) throws IOException {
        if (redo != mRedoWriter) {
            switchRedo(redo);
        }

        byte[] buffer = mRedoBuffer;
        int pos = mRedoPos;

        if (pos > buffer.length - len) {
            redoFlush(false);
            pos = 0;
        }

        buffer[pos] = op;
        return pos + 1;
    }

    private void redoWriteTxnOp(RedoWriter redo, byte op, long txnId) throws IOException {
        if (redo != mRedoWriter) {
            switchRedo(redo);
        }

        byte[] buffer = mRedoBuffer;
        int pos = mRedoPos;

        prepare:
        {
            if (pos > buffer.length - (1 + 9)) { // 1 for op, up to 9 for txn delta
                redoFlush(false);
                pos = 0;
            } else if (pos != 0) {
                mRedoPos = Utils.encodeSignedVarLong(buffer, pos + 1, txnId - mRedoLastTxnId);
                break prepare;
            }
            mRedoFirstTxnId = txnId;
            mRedoPos = 1 + 9; // 1 for op, and reserve 9 for txn delta
        }

        buffer[pos] = op;
        mRedoLastTxnId = txnId;
    }

    private void redoWriteCursorOp(RedoWriter redo, byte op, long cursorId, long txnId)
            throws IOException {
        if (redo != mRedoWriter) {
            switchRedo(redo);
        }

        byte[] buffer = mRedoBuffer;
        int pos = mRedoPos;

        prepare:
        {
            if (pos > buffer.length - ((1 + 9) << 1)) { // 2 ops and 2 deltas (max length)
                redoFlush(false);
                pos = 0;
            } else if (pos != 0) {
                buffer[pos] = op;
                pos = Utils.encodeSignedVarLong(buffer, pos + 1, cursorId - mRedoLastTxnId);
                break prepare;
            }
            buffer[0] = op;
            pos = 1 + 9;  // 1 for op, and reserve 9 for txn delta (cursorId actually)
            mRedoFirstTxnId = cursorId;
        }

        mRedoPos = Utils.encodeSignedVarLong(buffer, pos, txnId - cursorId);
        mRedoLastTxnId = txnId;
    }

    @Override
    public void flush() throws IOException {
        acquireRedoLatch();
        try {
            doFlush();
        } finally {
            releaseRedoLatch();
        }
    }

    void doFlush() throws IOException {
        redoFlush(false);
    }

    private void switchRedo(RedoWriter redo) throws IOException {
        try {
            redoFlush(false);
        } catch (UnmodifiableReplicaException e) {
            mRedoPos = 0;
            mRedoTerminatePos = 0;
            mRedoFirstTxnId = 0;
        } finally {
            if (mRedoWriterLatched) {
                mRedoWriter.releaseExclusive();
                mRedoWriterLatched = false;
            }
        }

        mRedoWriter = redo;
    }

    private long redoFlushCommit(DurabilityMode mode) throws IOException {
        if (mode == DurabilityMode.SYNC) {
            redoFlush(true);
            return mRedoWriterPos;
        } else {
            redoFlush(mode == DurabilityMode.NO_SYNC); // ignore flush for NO_FLUSH, etc.
            return 0;
        }
    }

    private void redoFlush(boolean full) throws IOException {
        int length = mRedoPos;
        if (length == 0) {
            return;
        }

        int commitLen = mRedoTerminatePos;
        byte[] buffer = mRedoBuffer;
        int offset = 0;
        RedoWriter redo = latchWriter();

        final long redoWriterLastTxnId = redo.mLastTxnId;

        if (mRedoFirstTxnId != 0) {
            long delta = Utils.convertSignedVarLong(mRedoFirstTxnId - redoWriterLastTxnId);
            int varLen = Utils.calcUnsignedVarLongLength(delta);
            offset = (1 + 9) - varLen;
            Utils.encodeUnsignedVarLong(buffer, offset, delta);
            buffer[--offset] = buffer[0];
            length -= offset;
            commitLen -= offset;
            redo.mLastTxnId = mRedoLastTxnId;
        }

        try {
            try {
                mRedoWriterPos = redo.write(full, buffer, offset, length, commitLen);
            } catch (IOException e) {
                throw Utils.rethrow(e, redo.mCloseCause);
            }
        } catch (Throwable e) {
            redo.mLastTxnId = redoWriterLastTxnId;
            throw e;
        }

        mRedoPos = 0;
        mRedoTerminatePos = 0;
        mRedoFirstTxnId = 0;
    }

    private RedoWriter latchWriter() {
        RedoWriter redo = mRedoWriter;
        if (!mRedoWriterLatched) {
            redo.acquireExclusive();
            mRedoWriterLatched = true;
        }
        return redo;
    }

    synchronized void register(UndoLog undo) {
        UndoLog top = mTopUndoLog;
        if (top != null) {
            undo.mPrev = top;
            top.mNext = undo;
        }
        mTopUndoLog = undo;
        mUndoLogCount++;
    }

    synchronized void unregister(UndoLog log) {
        UndoLog prev = log.mPrev;
        UndoLog next = log.mNext;
        if (prev != null) {
            prev.mNext = next;
            log.mPrev = null;
        }
        if (next != null) {
            next.mPrev = prev;
            log.mNext = null;
        } else if (log == mTopUndoLog) {
            mTopUndoLog = prev;
        }
        mUndoLogCount--;
    }

    public long higherTransactionId(long txnId) {
        return Math.max(mHighTxnId, txnId);
    }

    public boolean hasUndoLogs() {
        return mTopUndoLog != null;
    }

    public byte[] writeToMaster(UndoLog master, byte[] workspace) throws IOException {
        for (UndoLog log = mTopUndoLog; log != null; log = log.mPrev) {
            workspace = log.writeToMaster(master, workspace);
        }
        return workspace;
    }

    public synchronized void deleteUndoLogs() {
        for (UndoLog log = mTopUndoLog; log != null; log = log.mPrev) {
            log.delete();
        }
        mTopUndoLog = null;
    }
}
