package com.linglong.engine.core.repl;

import com.linglong.base.exception.ConfirmationFailureException;
import com.linglong.base.exception.DatabaseException;
import com.linglong.base.exception.UnmodifiableReplicaException;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.tx.RedoOps;
import com.linglong.engine.core.tx.RedoWriter;
import com.linglong.engine.core.tx.TransactionContext;
import com.linglong.engine.extend.ReplicationManager;

import java.io.IOException;

/**
 * @author Stereo
 */
public final class ReplRedoController extends ReplRedoWriter {
    final ReplicationManager mManager;

    public ReplicationManager getManager() {
        return mManager;
    }

    private volatile ReplRedoWriter mTxnRedoWriter;
    private volatile boolean mSwitchingToReplica;

    private ReplRedoWriter mCheckpointRedoWriter;
    private long mCheckpointPos;
    private long mCheckpointTxnId;

    private long mCheckpointNum;

    public ReplRedoController(ReplRedoEngine engine) {
        super(engine, null);
        mManager = engine.mManager;
        mTxnRedoWriter = this;
    }

    void initCheckpointNumber(long num) {
        acquireExclusive();
        mCheckpointNum = num;
        releaseExclusive();
    }

    public void ready(long initialTxnId, ReplicationManager.Accessor accessor) throws IOException {
        mEngine.startReceiving(mManager.readPosition(), initialTxnId);
        mManager.ready(accessor);
    }

    @Override
    public RedoWriter txnRedoWriter() {
        return mTxnRedoWriter;
    }

    @Override
    public boolean shouldCheckpoint(long sizeThreshold) {
        acquireShared();
        try {
            ReplicationManager.Writer writer = mTxnRedoWriter.mReplWriter;
            long pos = writer == null ? mEngine.decodePosition() : writer.position();
            return (pos - checkpointPosition()) >= sizeThreshold;
        } finally {
            releaseShared();
        }
    }

    @Override
    public void checkpointPrepare() throws IOException {
        mEngine.suspend();
    }

    @Override
    public void checkpointSwitch(TransactionContext[] contexts) throws IOException {
        mCheckpointNum++;

        ReplRedoWriter redo = mTxnRedoWriter;
        mCheckpointRedoWriter = redo;

        if (mCheckpointPos <= 0 && mCheckpointTxnId == 0) {
            ReplicationManager.Writer writer = redo.mReplWriter;
            if (writer == null) {
                mCheckpointPos = mEngine.suspendedDecodePosition();
                mCheckpointTxnId = mEngine.suspendedDecodeTransactionId();
            } else {
                redo.acquireShared();
                mCheckpointPos = redo.mLastCommitPos;
                mCheckpointTxnId = redo.mLastCommitTxnId;
                redo.releaseShared();
            }
        }
    }

    @Override
    public long checkpointNumber() {
        return mCheckpointNum;
    }

    @Override
    public long checkpointPosition() {
        return mCheckpointPos & ~(1L << 63);
    }

    @Override
    public long checkpointTransactionId() {
        return mCheckpointTxnId;
    }

    @Override
    public void checkpointAborted() {
        mEngine.resume();
        mCheckpointRedoWriter = null;
    }

    @Override
    public void checkpointStarted() throws IOException {
        mEngine.resume();
    }

    @Override
    public void checkpointFlushed() throws IOException {
        ReplRedoWriter redo = mCheckpointRedoWriter;
        ReplicationManager.Writer writer = redo.mReplWriter;

        if (writer != null && !writer.confirm(mCheckpointPos)) {
            long endPos = writer.confirmEnd();
            if (endPos < mCheckpointPos) {
                mCheckpointPos = endPos;
                mCheckpointTxnId = 0;
            }
            mCheckpointRedoWriter = this;

            throw nowUnmodifiable(writer);
        }
        mManager.syncConfirm(mCheckpointPos);
    }

    @Override
    public void checkpointFinished() throws IOException {
        mManager.checkpointed(mCheckpointPos);
        mCheckpointRedoWriter = null;
        mCheckpointPos |= 1L << 63;
        mCheckpointTxnId = 0;
    }

    @Override
    public DurabilityMode opWriteCheck(DurabilityMode mode) throws IOException {
        throw mEngine.unmodifiable();
    }

    @Override
    public long adjustTransactionId(long txnId) {
        return -txnId;
    }

    @Override
    public void force(boolean metadata) throws IOException {
        if (metadata) {
            try {
                long pos;
                ReplicationManager.Writer writer = mTxnRedoWriter.mReplWriter;
                if (writer == null) {
                    pos = mEngine.decodePosition();
                } else {
                    pos = writer.confirmedPosition();
                }
                mEngine.mManager.syncConfirm(pos);
                return;
            } catch (IOException e) {
            }
        }

        mEngine.mManager.sync();
    }

    /**
     * 晋升为Leader后会将mTxnRedoWriter变更为ReplRedoWriter对象进行读写
     *
     * @return
     * @throws UnmodifiableReplicaException
     * @throws IOException
     */
    ReplRedoWriter leaderNotify() throws UnmodifiableReplicaException, IOException {
        acquireExclusive();
        try {
            if (mTxnRedoWriter.mReplWriter != null) {
                return null;
            }

            ReplicationManager.Writer writer = mManager.writer();

            if (writer == null) {
                mEngine.fail(new IllegalStateException("No writer for the leader"));
                return null;
            }

            ReplRedoWriter redo = new ReplRedoWriter(mEngine, writer);
            redo.start();
            TransactionContext context = mEngine.mDatabase.anyTransactionContext();

            context.fullAcquireRedoLatch(redo);
            try {
                mTxnRedoWriter = redo;

                if (!writer.leaderNotify(() -> switchToReplica(writer))) {
                    throw nowUnmodifiable(writer);
                }

                context.doRedoReset(redo);

                context.doRedoTimestamp(redo, RedoOps.OP_TIMESTAMP, DurabilityMode.NO_FLUSH);

                context.doRedoNopRandom(redo, DurabilityMode.NO_SYNC);

                return redo;
            } finally {
                context.releaseRedoLatch();
            }
        } finally {
            releaseExclusive();
        }
    }

    UnmodifiableReplicaException nowUnmodifiable(ReplicationManager.Writer expect)
            throws DatabaseException {
        switchToReplica(expect);
        return mEngine.unmodifiable();
    }

    void switchToReplica(ReplicationManager.Writer expect) {
        if (shouldSwitchToReplica(expect) != null) {
            new Thread(() -> doSwitchToReplica(expect)).start();
        }
    }

    private void doSwitchToReplica(ReplicationManager.Writer expect) {
        ReplRedoWriter redo;

        acquireExclusive();
        try {
            redo = shouldSwitchToReplica(expect);
            if (redo == null) {
                return;
            }
            mSwitchingToReplica = true;
        } finally {
            releaseExclusive();
        }

        long pos;
        try {
            pos = redo.mReplWriter.confirmEnd();
        } catch (ConfirmationFailureException e) {
            mEngine.fail(e);
            return;
        }

        redo.flipped(pos);

        mEngine.startReceiving(pos, 0);

        acquireExclusive();
        mTxnRedoWriter = this;
        mSwitchingToReplica = false;
        releaseExclusive();
    }

    private ReplRedoWriter shouldSwitchToReplica(ReplicationManager.Writer expect) {
        if (mSwitchingToReplica) {
            return null;
        }

        if (mEngine.mDatabase.isClosed()) {
            return null;
        }

        ReplRedoWriter redo = mTxnRedoWriter;
        ReplicationManager.Writer writer = redo.mReplWriter;

        if (writer == null || writer != expect) {
            return null;
        }

        return redo;
    }
}
