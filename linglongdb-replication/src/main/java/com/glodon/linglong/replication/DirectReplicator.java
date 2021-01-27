package com.glodon.linglong.replication;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * @author Stereo
 */
public interface DirectReplicator extends Replicator {
    void start() throws IOException;

    SnapshotReceiver restore(Map<String, String> options) throws IOException;

    SnapshotReceiver requestSnapshot(Map<String, String> options) throws IOException;

    void snapshotRequestAcceptor(Consumer<SnapshotSender> acceptor);

    Reader newReader(long index, boolean follow);

    Writer newWriter();

    Writer newWriter(long index);

    boolean syncCommit(long index, long nanosTimeout) throws IOException;

    void compact(long index) throws IOException;

    interface Accessor extends Closeable {

        long term();

        long termStartIndex();

        long termEndIndex();

        long index();

        @Override
        void close();
    }

    interface Reader extends Accessor {
    }

    interface Writer extends Accessor {
        long commitIndex();

        long waitForCommit(long index, long nanosTimeout) throws InterruptedIOException;

        default long waitForEndCommit(long nanosTimeout) throws InterruptedIOException {
            long endNanos = nanosTimeout > 0 ? (System.nanoTime() + nanosTimeout) : 0;

            long endIndex = termEndIndex();

            while (true) {
                long index = waitForCommit(endIndex, nanosTimeout);
                if (index == -2) {
                    return -2;
                }
                endIndex = termEndIndex();
                if (endIndex == Long.MAX_VALUE) {
                    return -1;
                }
                if (index == endIndex) {
                    return index;
                }
                if (nanosTimeout > 0) {
                    nanosTimeout = Math.max(0, endNanos - System.nanoTime());
                }
            }
        }

        void uponCommit(long index, LongConsumer task);

        default void uponEndCommit(LongConsumer task) {
            uponCommit(termEndIndex(), index -> {
                long endIndex = termEndIndex();
                if (endIndex == Long.MAX_VALUE) {
                    task.accept(-1);
                } else if (index == endIndex) {
                    task.accept(index);
                } else {
                    uponEndCommit(task);
                }
            });
        }
    }
}
