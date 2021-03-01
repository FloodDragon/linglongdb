package com.linglong.replication;

import com.linglong.base.common.Utils;
import com.linglong.base.exception.ConfirmationFailureException;
import com.linglong.base.exception.ConfirmationInterruptedException;
import com.linglong.base.exception.ConfirmationTimeoutException;
import com.linglong.base.exception.UnmodifiableReplicaException;
import com.linglong.base.io.CRC32C;
import com.linglong.engine.core.frame.Database;
import com.linglong.engine.core.frame.Snapshot;
import com.linglong.engine.event.EventListener;
import com.linglong.engine.event.EventType;
import com.linglong.engine.extend.ReplicationManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.Checksum;

/**
 * 流复制器
 *
 * @author Stereo
 */
final class DatabaseStreamReplicator implements DatabaseReplicator {
    private static final long ENCODING = 7944834171105125288L;
    private static final long RESTORE_EVENT_RATE_MILLIS = 5000;

    private final StreamReplicator mRepl;

    private StreamReplicator.Reader mStreamReader;
    private DbWriter mDbWriter;

    DatabaseStreamReplicator(StreamReplicator repl) {
        mRepl = repl;
    }

    @Override
    public long getLocalMemberId() {
        return mRepl.getLocalMemberId();
    }

    @Override
    public SocketAddress getLocalAddress() {
        return mRepl.getLocalAddress();
    }

    @Override
    public Role getLocalRole() {
        return mRepl.getLocalRole();
    }

    @Override
    public Socket connect(SocketAddress addr) throws IOException {
        return mRepl.connect(addr);
    }

    @Override
    public void socketAcceptor(Consumer<Socket> acceptor) {
        mRepl.socketAcceptor(acceptor);
    }

    @Override
    public long encoding() {
        return ENCODING;
    }

    @Override
    public InputStream restoreRequest(EventListener listener) throws IOException {
        Map<String, String> options = Collections.singletonMap("checksum", "CRC32C");
        SnapshotReceiver receiver = mRepl.restore(options);

        if (receiver == null) {
            return null;
        }

        InputStream in;

        try {
            in = receiver.inputStream();

            String checksumOption = receiver.options().get("checksum");

            if (checksumOption != null) {
                if (checksumOption.equals("CRC32C")) {
                    in = new CheckedInputStream(in, CRC32C.newInstance(), receiver.length());
                } else {
                    throw new IOException("Unknown checksum option: " + checksumOption);
                }
            }

            long length = receiver.length();

            if (listener != null && length >= 0 && (mRepl instanceof Controller)) {
                Scheduler scheduler = ((Controller) mRepl).scheduler();
                RestoreInputStream rin = new RestoreInputStream(in);
                in = rin;

                listener.notify(EventType.REPLICATION_RESTORE,
                        "Receiving snapshot: %1$,d bytes from %2$s",
                        length, receiver.senderAddress());

                scheduler.schedule(new ProgressTask(listener, scheduler, rin, length));
            }
        } catch (Throwable e) {
            Utils.closeQuietly(receiver);
            throw e;
        }

        return in;
    }

    private static final class ProgressTask extends Delayed {
        private final EventListener mListener;
        private final RestoreInputStream mRestore;
        private final long mLength;
        private final Scheduler mScheduler;

        private long mLastTimeMillis = Long.MIN_VALUE;
        private long mLastReceived;

        ProgressTask(EventListener listener, Scheduler scheduler,
                     RestoreInputStream in, long length) {
            super(0);
            mListener = listener;
            mScheduler = scheduler;
            mRestore = in;
            mLength = length;
        }

        @Override
        protected void doRun(long counter) {
            if (mRestore.isFinished()) {
                return;
            }

            long now = System.currentTimeMillis();

            long received = mRestore.received();
            double percent = 100.0 * (received / (double) mLength);
            long progess = received - mLastReceived;

            if (mLastTimeMillis != Long.MIN_VALUE) {
                double rate = (1000.0 * (progess / (double) (now - mLastTimeMillis)));
                String format = "Receiving snapshot: %1$1.3f%%";
                if (rate == 0) {
                    mListener.notify(EventType.REPLICATION_RESTORE, format, percent);
                } else {
                    format += "  rate: %2$,d bytes/s  remaining: ~%3$s";
                    long remainingSeconds = (long) ((mLength - received) / rate);
                    mListener.notify
                            (EventType.REPLICATION_RESTORE, format,
                                    percent, (long) rate, remainingDuration(remainingSeconds));
                }
            }

            mCounter = now + RESTORE_EVENT_RATE_MILLIS;

            mLastTimeMillis = now;
            mLastReceived = received;

            mScheduler.schedule(this);
        }

        private static String remainingDuration(long seconds) {
            if (seconds < (60 * 2)) {
                return seconds + "s";
            } else if (seconds < (60 * 60 * 2)) {
                return (seconds / 60) + "m";
            } else if (seconds < (60 * 60 * 24 * 2)) {
                return (seconds / (60 * 60)) + "h";
            } else {
                return (seconds / (60 * 60 * 24)) + "d";
            }
        }
    }

    @Override
    public void start(long position) throws IOException {
        if (mStreamReader != null) {
            throw new IllegalStateException();
        }

        mRepl.start();

        while (true) {
            StreamReplicator.Reader reader = mRepl.newReader(position, false);
            if (reader != null) {
                mStreamReader = reader;
                break;
            }
            StreamReplicator.Writer writer = mRepl.newWriter(position);
            if (writer != null) {
                mDbWriter = new DbWriter(writer);
                break;
            }
        }
    }

    @Override
    public void ready(ReplicationManager.Accessor accessor) throws IOException {
        mRepl.controlMessageAcceptor(message -> {
            try {
                accessor.control(message);
            } catch (UnmodifiableReplicaException e) {
                // Drop it.
            } catch (IOException e) {
                Utils.uncaught(e);
            }
        });

        Database db = accessor.database();

        mRepl.snapshotRequestAcceptor(sender -> {
            try {
                sendSnapshot(db, sender);
            } catch (IOException e) {
                Utils.closeQuietly(sender);
            }
        });

        mRepl.start();
        // TODO: 等待直到赶上？待定
    }

    private void sendSnapshot(Database db, SnapshotSender sender) throws IOException {
        Map<String, String> options = null;
        Checksum checksum = null;

        if ("CRC32C".equals(sender.options().get("checksum"))) {
            options = Collections.singletonMap("checksum", "CRC32C");
            checksum = CRC32C.newInstance();
        }

        try (Snapshot snapshot = db.beginSnapshot();
             OutputStream out = sender.begin(snapshot.length(), snapshot.position(), options)) {
            if (checksum == null) {
                snapshot.writeTo(out);
            } else {
                CheckedOutputStream cout = new CheckedOutputStream(out, checksum);
                snapshot.writeTo(cout);
                cout.writeChecksum();
            }
        }
    }

    @Override
    public long readPosition() {
        StreamReplicator.Reader reader = mStreamReader;
        if (reader != null) {
            return reader.index();
        } else {
            return mDbWriter.mWriter.termStartIndex();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        StreamReplicator.Reader reader = mStreamReader;

        if (reader == null) {
            return -1;
        }

        while (true) {
            int amt = reader.read(b, off, len);
            if (amt >= 0) {
                return amt;
            }

            StreamReplicator.Reader nextReader;
            while ((nextReader = mRepl.newReader(reader.index(), false)) == null) {
                StreamReplicator.Writer nextWriter = mRepl.newWriter(reader.index());
                if (nextWriter != null) {
                    mDbWriter = new DbWriter(nextWriter);
                    mStreamReader = null;
                    reader.close();
                    return -1;
                }
            }

            reader.close();
            mStreamReader = reader = nextReader;
        }
    }

    @Override
    public ReplicationManager.Writer writer() throws IOException {
        return mDbWriter;
    }

    @Override
    public void sync() throws IOException {
        mRepl.sync();
    }

    @Override
    public void syncConfirm(long position, long timeoutNanos) throws IOException {
        if (!mRepl.syncCommit(position, timeoutNanos)) {
            throw new ConfirmationTimeoutException(timeoutNanos);
        }
    }

    @Override
    public void checkpointed(long position) throws IOException {
        mRepl.compact(position);
    }

    @Override
    public void control(long position, byte[] message) throws IOException {
        mRepl.controlMessageReceived(position, message);
    }

    @Override
    public void close() throws IOException {
        mRepl.close();
    }

    void partitioned(boolean enable) {
        ((Controller) mRepl).partitioned(enable);
    }

    void toReplica(DbWriter expect, long index) {
        if (mDbWriter != expect) {
            throw new IllegalStateException("Mismatched writer: " + mDbWriter + " != " + expect);
        }

        mDbWriter.mWriter.close();
        mDbWriter = null;

        while ((mStreamReader = mRepl.newReader(index, false)) == null) {
            StreamReplicator.Writer nextWriter = mRepl.newWriter(index);
            if (nextWriter != null) {
                mDbWriter = new DbWriter(nextWriter);
                return;
            }
        }
    }

    private final class DbWriter implements ReplicationManager.Writer {
        final StreamReplicator.Writer mWriter;

        private boolean mEndConfirmed;

        DbWriter(StreamReplicator.Writer writer) {
            mWriter = writer;
        }

        @Override
        public long position() {
            return mWriter.index();
        }

        @Override
        public long confirmedPosition() {
            return mWriter.commitIndex();
        }

        @Override
        public boolean leaderNotify(Runnable callback) {
            mWriter.uponCommit(Long.MAX_VALUE, index -> new Thread(callback).start());
            return true;
        }

        @Override
        public boolean write(byte[] b, int off, int len, long commitPos) throws IOException {
            return mWriter.write(b, off, len, commitPos) >= len;
        }

        @Override
        public boolean confirm(long commitPos, long nanosTimeout) throws IOException {
            long pos;
            try {
                pos = mWriter.waitForCommit(commitPos, nanosTimeout);
            } catch (InterruptedIOException e) {
                throw new ConfirmationInterruptedException();
            }
            if (pos >= commitPos) {
                return true;
            }
            if (pos == -1) {
                return false;
            }
            if (pos == -2) {
                throw new ConfirmationTimeoutException(nanosTimeout);
            }
            throw new ConfirmationFailureException("Unexpected result: " + pos);
        }

        @Override
        public long confirmEnd(long nanosTimeout) throws ConfirmationFailureException {
            long pos;
            try {
                pos = mWriter.waitForEndCommit(nanosTimeout);
            } catch (InterruptedIOException e) {
                throw new ConfirmationInterruptedException();
            }
            if (pos >= 0) {
                synchronized (this) {
                    if (mEndConfirmed) {
                        // Don't call toReplica again.
                        return pos;
                    }
                    mEndConfirmed = true;
                }
                toReplica(this, pos);
                return pos;
            }
            if (pos == -1) {
                throw new ConfirmationFailureException("Closed");
            }
            if (pos == -2) {
                throw new ConfirmationTimeoutException(nanosTimeout);
            }
            throw new ConfirmationFailureException("Unexpected result: " + pos);
        }
    }
}
