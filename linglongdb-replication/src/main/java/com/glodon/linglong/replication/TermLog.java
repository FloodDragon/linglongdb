package com.glodon.linglong.replication;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * @author Stereo
 */
public interface TermLog extends LKey<TermLog>, Closeable {
    @Override
    default long key() {
        return startIndex();
    }

    long prevTerm();

    long term();

    long startIndex();

    long prevTermAt(long index);

    boolean compact(long startIndex) throws IOException;

    long potentialCommitIndex();

    default boolean hasCommit() {
        return hasCommit(startIndex());
    }

    default boolean hasCommit(long index) {
        return potentialCommitIndex() > index;
    }

    long endIndex();

    void captureHighest(LogInfo info);

    void commit(long commitIndex);

    long waitForCommit(long index, long nanosTimeout) throws InterruptedIOException;

    void uponCommit(Delayed task);

    void finishTerm(long endIndex);

    long checkForMissingData(long contigIndex, IndexRange results);

    LogWriter openWriter(long index);

    LogReader openReader(long index);

    void sync() throws IOException;
}
