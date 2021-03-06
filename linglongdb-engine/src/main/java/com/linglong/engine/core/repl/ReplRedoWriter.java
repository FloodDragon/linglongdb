package com.linglong.engine.core.repl;

import com.linglong.base.common.Utils;
import com.linglong.base.concurrent.Latch;
import com.linglong.base.exception.DatabaseException;
import com.linglong.base.exception.UnmodifiableReplicaException;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.LocalDatabase;
import com.linglong.engine.core.tx.LocalTransaction;
import com.linglong.engine.core.tx.PendingTxn;
import com.linglong.engine.core.tx.RedoWriter;
import com.linglong.engine.core.tx.TransactionContext;
import com.linglong.engine.extend.ReplicationManager;

import java.io.IOException;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Stereo
 */
public class ReplRedoWriter extends RedoWriter {
    final ReplRedoEngine mEngine;

    final ReplicationManager.Writer mReplWriter;

    long mLastCommitPos;
    long mLastCommitTxnId;

    private volatile PendingTxnWaiter mPendingWaiter;

    private final Latch mBufferLatch;
    private Thread mProducer;
    private Thread mConsumer;
    private boolean mConsumerParked;
    private byte[] mBuffer;
    private int mBufferHead;
    private int mBufferTail = -1;
    private long mWritePos;

    public ReplRedoWriter(ReplRedoEngine engine, ReplicationManager.Writer writer) {
        mEngine = engine;
        mReplWriter = writer;
        mBufferLatch = writer == null ? null : new Latch();
    }

    void start() {
        mBufferLatch.acquireExclusive();
        try {
            if (mEngine.mDatabase.isClosed()) {
                return;
            }

            mWritePos = mReplWriter.position();
            mBuffer = new byte[65536];

            mConsumer = new Thread(this::consume);
            mConsumer.setName("WriteConsumer-" + mConsumer.getId());
            mConsumer.setDaemon(true);
            mConsumer.start();
        } finally {
            mBufferLatch.releaseExclusive();
        }
    }

    @Override
    public final void commitSync(TransactionContext context, long commitPos) throws IOException {
        ReplicationManager.Writer writer = mReplWriter;
        if (writer == null) {
            throw mEngine.unmodifiable();
        }
        if (!writer.confirm(commitPos)) {
            throw nowUnmodifiable();
        }
    }

    @Override
    public final void txnCommitSync(LocalTransaction txn, long commitPos) throws IOException {
        ReplicationManager.Writer writer = mReplWriter;
        if (writer == null) {
            throw mEngine.unmodifiable();
        }
        if (!writer.confirm(commitPos)) {
            throw nowUnmodifiable();
        }
    }

    @Override
    public final void txnCommitPending(PendingTxn pending) throws IOException {
        PendingTxnWaiter waiter = mPendingWaiter;
        int action;
        if (waiter == null || (action = waiter.add(pending)) == PendingTxnWaiter.EXITED) {
            acquireExclusive();
            try {
                waiter = mPendingWaiter;
                if (waiter == null || (action = waiter.add(pending)) == PendingTxnWaiter.EXITED) {
                    waiter = new PendingTxnWaiter(this);
                    mPendingWaiter = waiter;
                    action = waiter.add(pending);
                    if (action == PendingTxnWaiter.PENDING) {
                        waiter.setName("PendingTxnWaiter-" + waiter.getId());
                        waiter.setDaemon(true);
                        waiter.start();
                    }
                }
            } finally {
                releaseExclusive();
            }
        }

        if (action != PendingTxnWaiter.PENDING) {
            LocalDatabase db = mEngine.mDatabase;
            if (action == PendingTxnWaiter.DO_COMMIT) {
                pending.commit(db);
            } else if (action == PendingTxnWaiter.DO_ROLLBACK) {
                pending.rollback(db);
            }
        }
    }

    protected final void flipped(long commitPos) {
        closeConsumerThread();

        PendingTxnWaiter waiter;
        acquireExclusive();
        try {
            waiter = mPendingWaiter;
            if (waiter == null) {
                waiter = new PendingTxnWaiter(this);
                mPendingWaiter = waiter;
            }
            waiter.flipped(commitPos);
        } finally {
            releaseExclusive();
        }

        waiter.finishAll();
    }

    final boolean confirm(PendingTxn pending) {
        ReplicationManager.Writer writer = mReplWriter;
        if (writer == null) {
            return false;
        }

        long commitPos = pending.getCommitPos();

        try {
            if (writer.confirm(commitPos)) {
                return true;
            }
        } catch (IOException e) {
        }

        mEngine.mController.switchToReplica(mReplWriter);

        return false;
    }

    @Override
    public final long encoding() {
        return mEngine.mManager.encoding();
    }

    @Override
    public RedoWriter txnRedoWriter() {
        return this;
    }

    @Override
    public boolean shouldCheckpoint(long sizeThreshold) {
        return false;
    }

    @Override
    public void checkpointPrepare() throws IOException {
        throw fail();
    }

    @Override
    public void checkpointSwitch(TransactionContext[] contexts) throws IOException {
        throw fail();
    }

    @Override
    public long checkpointNumber() {
        throw fail();
    }

    @Override
    public long checkpointPosition() {
        throw fail();
    }

    @Override
    public long checkpointTransactionId() {
        throw fail();
    }

    @Override
    public void checkpointAborted() {
    }

    @Override
    public void checkpointStarted() throws IOException {
        throw fail();
    }

    @Override
    public void checkpointFlushed() throws IOException {
        throw fail();
    }

    @Override
    public void checkpointFinished() throws IOException {
        throw fail();
    }

    @Override
    public DurabilityMode opWriteCheck(DurabilityMode mode) throws IOException {
        return DurabilityMode.SYNC;
    }

    @Override
    public boolean shouldWriteTerminators() {
        return false;
    }


    /**
     * 数据写入redo复制缓冲区
     *
     * @param flush
     * @param bytes
     * @param offset
     * @param length
     * @param commitLen
     * @return
     * @throws IOException
     */
    @Override
    public final long write(boolean flush, byte[] bytes, int offset, int length, int commitLen)
            throws IOException {
        if (mReplWriter == null) {
            throw mEngine.unmodifiable();
        }

        mBufferLatch.acquireExclusive();
        try {
            byte[] buffer = mBuffer;
            if (buffer == null) {
                throw nowUnmodifiable();
            }

            if (commitLen > 0) {
                mLastCommitPos = mWritePos + commitLen;
                mLastCommitTxnId = mLastTxnId;
            }

            while (true) {
                if (mBufferHead == mBufferTail) {
                    mProducer = Thread.currentThread();
                    try {
                        Thread consumer = mConsumer;
                        do {
                            boolean parked = mConsumerParked;
                            if (parked) {
                                mConsumerParked = false;
                            }
                            mBufferLatch.releaseExclusive();
                            if (parked) {
                                LockSupport.unpark(consumer);
                            }
                            LockSupport.park(mBufferLatch);
                            mBufferLatch.acquireExclusive();
                            buffer = mBuffer;
                            if (buffer == null) {
                                throw nowUnmodifiable();
                            }
                        } while (mBufferHead == mBufferTail);
                    } finally {
                        mProducer = null;
                    }
                }

                int amt;
                //assert mBufferHead != mBufferTail;
                if (mBufferHead < mBufferTail) {
                    amt = buffer.length - mBufferTail;
                } else if (mBufferTail >= 0) {
                    amt = mBufferHead - mBufferTail;
                } else {
                    if (length != 0) {
                        mBufferHead = 0;
                        mBufferTail = 0;
                    }
                    amt = buffer.length;
                }

                if (length <= amt) {
                    try {
                        System.arraycopy(bytes, offset, buffer, mBufferTail, length);
                    } catch (Throwable e) {
                        if (mBufferHead == mBufferTail) {
                            mBufferTail = -1;
                        }
                        throw e;
                    }

                    mWritePos += length;

                    if ((mBufferTail += length) >= buffer.length) {
                        mBufferTail = 0;
                    }

                    // TODO: 如果消费者停车，尝试立即写入。
                    if (mConsumerParked) {
                        mConsumerParked = false;
                        LockSupport.unpark(mConsumer);
                    }

                    return mWritePos;
                }

                try {
                    System.arraycopy(bytes, offset, buffer, mBufferTail, amt);
                } catch (Throwable e) {
                    if (mBufferHead == mBufferTail) {
                        mBufferTail = -1;
                    }
                    throw e;
                }

                mWritePos += amt;
                length -= amt;
                offset += amt;

                if ((mBufferTail += amt) >= buffer.length) {
                    mBufferTail = 0;
                }
            }
        } finally {
            mBufferLatch.releaseExclusive();
        }
    }

    @Override
    public final void alwaysFlush(boolean enable) {
    }

    @Override
    public final void flush() {
    }

    @Override
    public void force(boolean metadata) throws IOException {
        mEngine.mManager.sync();
    }

    @Override
    public void close() throws IOException {
        mEngine.mManager.close();

        if (mBufferLatch == null) {
            return;
        }

        closeConsumerThread();
    }

    private void closeConsumerThread() {
        mBufferLatch.acquireExclusive();
        Thread consumer = mConsumer;
        mConsumer = null;
        mConsumerParked = false;
        mBufferLatch.releaseExclusive();

        if (consumer != null) {
            LockSupport.unpark(consumer);
            try {
                consumer.join();
            } catch (InterruptedException e) {
                // Ignore.
            }
        }
    }

    private UnsupportedOperationException fail() {
        return new UnsupportedOperationException();
    }

    private UnmodifiableReplicaException nowUnmodifiable() throws DatabaseException {
        return mEngine.mController.nowUnmodifiable(mReplWriter);
    }

    /**
     * 从缓冲区消费执行复制
     */
    private void consume() {
        mBufferLatch.acquireExclusive();

        final byte[] buffer = mBuffer;

        while (mConsumer != null) {
            int head = mBufferHead;
            int tail = mBufferTail;
            long commitPos = mLastCommitPos;

            try {
                if (head == tail) {
                    if (!mReplWriter.write(buffer, head, buffer.length - head, commitPos)) {
                        break;
                    }

                    if (head > 0) {
                        mBufferHead = 0;
                        if (!mReplWriter.write(buffer, 0, tail, commitPos)) {
                            break;
                        }
                    }

                    mBufferTail = -1;
                } else if (tail >= 0) {
                    mBufferLatch.releaseExclusive();
                    try {
                        if (head < tail) {
                            if (!mReplWriter.write(buffer, head, tail - head, commitPos)) {
                                break;
                            }
                            head = tail;
                        } else {
                            int len = buffer.length - head;
                            if (!mReplWriter.write(buffer, head, len, commitPos)) {
                                break;
                            }
                            head = 0;
                        }
                    } finally {
                        mBufferLatch.acquireExclusive();
                    }

                    if (head != mBufferTail) {
                        mBufferHead = head;
                        continue;
                    }

                    mBufferTail = -1;
                }
            } catch (Throwable e) {
                if (!(e instanceof IOException)) {
                    Utils.uncaught(e);
                }
                mBufferLatch.releaseExclusive();
                Thread.yield();
                mBufferLatch.acquireExclusive();
                continue;
            }

            mConsumerParked = true;
            Thread producer = mProducer;
            mBufferLatch.releaseExclusive();
            LockSupport.unpark(producer);
            LockSupport.park(mBufferLatch);
            mBufferLatch.acquireExclusive();
        }

        mConsumer = null;
        mBuffer = null;
        LockSupport.unpark(mProducer);
        mBufferLatch.releaseExclusive();

        mEngine.mController.switchToReplica(mReplWriter);
    }
}
