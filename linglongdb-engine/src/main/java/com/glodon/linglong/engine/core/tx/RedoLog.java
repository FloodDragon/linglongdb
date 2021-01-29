package com.glodon.linglong.engine.core.tx;

import com.glodon.linglong.base.common.Crypto;
import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.exception.DatabaseException;
import com.glodon.linglong.base.exception.WriteFailureException;
import com.glodon.linglong.base.io.FileFactory;
import com.glodon.linglong.base.io.FileIO;
import com.glodon.linglong.engine.config.DatabaseConfig;
import com.glodon.linglong.engine.config.DurabilityMode;
import com.glodon.linglong.engine.core.LocalDatabase;
import com.glodon.linglong.engine.event.EventListener;
import com.glodon.linglong.engine.event.EventType;

import java.io.*;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.security.GeneralSecurityException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Stereo
 */
public final class RedoLog extends RedoWriter {
    private static final long MAGIC_NUMBER = 431399725605778814L;
    private static final int ENCODING_VERSION = 20130106;

    private final Crypto mCrypto;
    private final File mBaseFile;
    private final FileFactory mFileFactory;

    private final boolean mReplayMode;

    private final byte[] mBuffer;
    private int mBufferPos;

    private boolean mAlwaysFlush;

    private long mLogId;
    private long mPosition;
    private OutputStream mOut;
    private volatile FileChannel mChannel;

    private int mTermRndSeed;

    private long mNextLogId;
    private long mNextPosition;
    private OutputStream mNextOut;
    private FileChannel mNextChannel;
    private int mNextTermRndSeed;

    private volatile OutputStream mOldOut;
    private volatile FileChannel mOldChannel;

    private long mDeleteLogId;

    public RedoLog(DatabaseConfig config, long logId, long redoPos) throws IOException {
        this(config.getCrypto(), config.getBaseFile(), config.getFileFactory(), logId, redoPos, null);
    }

    public RedoLog(DatabaseConfig config, RedoLog replayed, TransactionContext context)
            throws IOException {
        this(config.getCrypto(), config.getBaseFile(), config.getFileFactory(),
                replayed.mLogId, replayed.mPosition, context);
    }

    public RedoLog(Crypto crypto, File baseFile, FileFactory factory,
                   long logId, long redoPos, TransactionContext context)
            throws IOException {
        mCrypto = crypto;
        mBaseFile = baseFile;
        mFileFactory = factory;
        mReplayMode = context == null;

        mBuffer = new byte[8192];

        acquireExclusive();
        mLogId = logId;
        mPosition = redoPos;
        releaseExclusive();

        if (context != null) {
            openNextFile(logId);
            applyNextFile(context);
            mDeleteLogId = logId;
        }
    }

    public Set<File> replay(RedoVisitor visitor, EventListener listener, EventType type, String message)
            throws IOException {
        if (!mReplayMode || mBaseFile == null) {
            throw new IllegalStateException();
        }

        acquireExclusive();
        try {
            Set<File> files = new LinkedHashSet<>(2);

            while (true) {
                File file = fileFor(mBaseFile, mLogId);

                InputStream in;
                try {
                    in = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    break;
                }

                boolean finished;
                try {
                    if (mCrypto != null) {
                        try {
                            in = mCrypto.newDecryptingStream(mLogId, in);
                        } catch (IOException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new DatabaseException(e);
                        }
                    }

                    if (listener != null) {
                        listener.notify(type, message, mLogId);
                    }

                    files.add(file);

                    DataIn din = new DataIn.Stream(mPosition, in);
                    finished = replay(din, visitor, listener);
                    mPosition = din.getPos();
                } finally {
                    Utils.closeQuietly(in);
                }

                mLogId++;

                if (!finished) {
                    Utils.deleteNumberedFiles(mBaseFile, LocalDatabase.REDO_FILE_SUFFIX, mLogId);
                    break;
                }
            }

            return files;
        } catch (IOException e) {
            throw Utils.rethrow(e, mCloseCause);
        } finally {
            releaseExclusive();
        }
    }

    public static void deleteOldFile(File baseFile, long logId) {
        fileFor(baseFile, logId).delete();
    }

    private void openNextFile(long logId) throws IOException {
        byte[] header = new byte[8 + 4 + 8 + 4];

        final File file = fileFor(mBaseFile, logId);
        if (file.exists() && file.length() > header.length) {
            throw new FileNotFoundException("Log file already exists: " + file.getPath());
        }

        if (mFileFactory != null) {
            mFileFactory.createFile(file);
        }

        FileOutputStream fout = null;
        OutputStream nextOut;
        FileChannel nextChannel;

        int nextTermRndSeed = 0;

        try {
            fout = new FileOutputStream(file);
            nextChannel = fout.getChannel();

            if (mCrypto == null) {
                nextOut = fout;
            } else {
                try {
                    nextOut = mCrypto.newEncryptingStream(logId, fout);
                } catch (GeneralSecurityException e) {
                    throw new DatabaseException(e);
                }
            }

            int offset = 0;
            Utils.encodeLongLE(header, offset, MAGIC_NUMBER);
            offset += 8;
            Utils.encodeIntLE(header, offset, ENCODING_VERSION);
            offset += 4;
            Utils.encodeLongLE(header, offset, logId);
            offset += 8;
            Utils.encodeIntLE(header, offset, nextTermRndSeed);
            offset += 4;
            if (offset != header.length) {
                throw new AssertionError();
            }

            nextOut.write(header);

            FileIO.dirSync(file);
        } catch (IOException e) {
            Utils.closeQuietly(fout);
            file.delete();
            throw new WriteFailureException(e);
        }

        mNextLogId = logId;
        mNextOut = nextOut;
        mNextChannel = nextChannel;
        mNextTermRndSeed = nextTermRndSeed;
    }

    private void applyNextFile(TransactionContext... contexts) throws IOException {
        final OutputStream oldOut;
        final FileChannel oldChannel;

        TransactionContext context = contexts[0];
        for (int i = contexts.length; --i >= 1; ) {
            contexts[i].flush();
        }

        context.fullAcquireRedoLatch(this);
        try {
            oldOut = mOut;
            oldChannel = mChannel;

            if (oldOut != null) {
                context.doRedoTimestamp(this, RedoOps.OP_END_FILE, DurabilityMode.NO_FLUSH);
                context.doFlush();
                doFlush();
            }

            mNextPosition = mPosition;

            mOut = mNextOut;
            mChannel = mNextChannel;
            mTermRndSeed = mNextTermRndSeed;
            mLogId = mNextLogId;

            mNextOut = null;
            mNextChannel = null;

            mLastTxnId = 0;

            context.doRedoTimestamp(this, RedoOps.OP_TIMESTAMP, DurabilityMode.NO_FLUSH);
            context.doRedoReset(this);

            context.doFlush();
        } finally {
            context.releaseRedoLatch();
        }

        Utils.closeQuietly(mOldOut);

        mOldOut = oldOut;
        mOldChannel = oldChannel;
    }

    private static File fileFor(File base, long logId) {
        return base == null ? null : new File
                (base.getPath() + LocalDatabase.REDO_FILE_SUFFIX + logId);
    }

    @Override
    public void commitSync(TransactionContext context, long commitPos) throws IOException {
        txnCommitSync((LocalTransaction) null, commitPos);
    }

    @Override
    public void txnCommitSync(LocalTransaction txn, long commitPos) throws IOException {
        try {
            force(false);
        } catch (IOException e) {
            throw Utils.rethrow(e, mCloseCause);
        }
    }

    @Override
    public void txnCommitPending(PendingTxn pending) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public final long encoding() {
        return 0;
    }

    @Override
    public final RedoWriter txnRedoWriter() {
        return this;
    }

    @Override
    public boolean shouldCheckpoint(long size) {
        try {
            FileChannel channel = mChannel;
            return channel != null && channel.size() >= size;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void checkpointPrepare() throws IOException {
        if (mReplayMode) {
            throw new IllegalStateException();
        }

        acquireShared();
        final long logId = mLogId;
        releaseShared();

        openNextFile(logId + 1);
    }

    @Override
    public void checkpointSwitch(TransactionContext[] contexts) throws IOException {
        applyNextFile(contexts);
    }

    @Override
    public long checkpointNumber() {
        return mNextLogId;
    }

    @Override
    public long checkpointPosition() {
        return mNextPosition;
    }

    @Override
    public long checkpointTransactionId() {
        return 0;
    }

    @Override
    public void checkpointAborted() {
        if (mNextOut != null) {
            Utils.closeQuietly(mNextOut);
            mNextOut = null;
        }
    }

    @Override
    public void checkpointStarted() throws IOException {
        /*
        FileChannel oldChannel = mOldChannel;

        if (oldChannel != null) {
            // Make sure any exception thrown by this call is not caught here,
            // because a checkpoint cannot complete successfully if the redo
            // log has not been durably written.
            oldChannel.force(true);
            mOldChannel = null;
        }

        Utils.closeQuietly(mOldOut);
        */
    }

    @Override
    public void checkpointFlushed() throws IOException {
        // Nothing to do.
    }

    @Override
    public void checkpointFinished() throws IOException {
        mOldChannel = null;
        Utils.closeQuietly(mOldOut);
        long id = mDeleteLogId;
        for (; id < mNextLogId; id++) {
            deleteOldFile(mBaseFile, id);
        }
        mDeleteLogId = id;
    }

    @Override
    public DurabilityMode opWriteCheck(DurabilityMode mode) throws IOException {
        return mode;
    }

    @Override
    public boolean shouldWriteTerminators() {
        return true;
    }

    @Override
    public long write(boolean flush, byte[] bytes, int offset, final int length, int commitLen)
            throws IOException {
        try {
            byte[] buf = mBuffer;
            int avail = buf.length - mBufferPos;

            if (avail >= length) {
                if (mBufferPos == 0 && avail == length) {
                    mOut.write(bytes, offset, length);
                } else {
                    System.arraycopy(bytes, offset, buf, mBufferPos, length);
                    mBufferPos += length;
                    if (mBufferPos == buf.length || flush || mAlwaysFlush) {
                        mOut.write(buf, 0, mBufferPos);
                        mBufferPos = 0;
                    }
                }
            } else {
                // Fill remainder of buffer and flush it.
                System.arraycopy(bytes, offset, buf, mBufferPos, avail);
                mBufferPos = buf.length;
                mOut.write(buf, 0, mBufferPos);
                offset += avail;
                int rem = length - avail;
                if (rem >= buf.length || flush || mAlwaysFlush) {
                    mBufferPos = 0;
                    mOut.write(bytes, offset, rem);
                } else {
                    System.arraycopy(bytes, offset, buf, 0, rem);
                    mBufferPos = rem;
                }
            }

            return mPosition += length;
        } catch (IOException e) {
            throw new WriteFailureException(e);
        }
    }

    @Override
    public void alwaysFlush(boolean enable) throws IOException {
        acquireExclusive();
        try {
            mAlwaysFlush = enable;
            if (enable) {
                doFlush();
            }
        } finally {
            releaseExclusive();
        }
    }

    @Override
    public void flush() throws IOException {
        acquireExclusive();
        try {
            doFlush();
        } finally {
            releaseExclusive();
        }
    }

    private void doFlush() throws IOException {
        try {
            if (mBufferPos > 0) {
                mOut.write(mBuffer, 0, mBufferPos);
                mBufferPos = 0;
            }
        } catch (IOException e) {
            throw new WriteFailureException(e);
        }
    }

    @Override
    public void force(boolean metadata) throws IOException {
        FileChannel oldChannel = mOldChannel;
        if (oldChannel != null) {
            try {
                oldChannel.force(true);
            } catch (ClosedChannelException e) {
                // Ignore.
            }
            mOldChannel = null;
        }

        FileChannel channel = mChannel;
        if (channel != null) {
            try {
                channel.force(metadata);
            } catch (ClosedChannelException e) {
                // Ignore.
            }
        }
    }

    @Override
    public void close() throws IOException {
        Utils.closeQuietly(mOldOut);

        FileChannel channel = mChannel;
        if (channel != null) {
            try {
                channel.close();
            } catch (ClosedChannelException e) {
                // Ignore.
            }
        }

        Utils.closeQuietly(mOut);
    }

    public int nextTermRnd() {
        return mTermRndSeed = Utils.nextRandom(mTermRndSeed);
    }

    private boolean replay(DataIn in, RedoVisitor visitor, EventListener listener)
            throws IOException {
        try {
            long magic = in.readLongLE();
            if (magic != MAGIC_NUMBER) {
                if (magic == 0) {
                    // Assume file was flushed improperly and discard it.
                    return false;
                }
                throw new DatabaseException("Incorrect magic number in redo log file");
            }
        } catch (EOFException e) {
            return false;
        }

        int version = in.readIntLE();
        if (version != ENCODING_VERSION) {
            throw new DatabaseException("Unsupported redo log encoding version: " + version);
        }

        long id = in.readLongLE();
        if (id != mLogId) {
            throw new DatabaseException
                    ("Expected redo log identifier of " + mLogId + ", but actual is: " + id);
        }

        mTermRndSeed = in.readIntLE();

        try {
            return new RedoLogDecoder(this, in, listener).run(visitor);
        } catch (EOFException e) {
            if (listener != null) {
                listener.notify(EventType.RECOVERY_REDO_LOG_CORRUPTION, "Unexpected end of file");
            }
            return false;
        }
    }
}
