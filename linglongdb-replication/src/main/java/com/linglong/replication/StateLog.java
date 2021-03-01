package com.linglong.replication;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Stereo
 */
interface StateLog extends Closeable {

    default LogInfo captureHighest() {
        LogInfo info = new LogInfo();
        captureHighest(info);
        return info;
    }

    TermLog captureHighest(LogInfo info);

    void commit(long commitIndex);

    long incrementCurrentTerm(int amount, long candidateId) throws IOException;

    long checkCurrentTerm(long term) throws IOException;

    boolean checkCandidate(long candidateId) throws IOException;

    void compact(long index) throws IOException;

    void truncateAll(long prevTerm, long term, long index) throws IOException;

    boolean defineTerm(long prevTerm, long term, long index) throws IOException;

    TermLog termLogAt(long index);

    void queryTerms(long startIndex, long endIndex, TermQuery results);

    long checkForMissingData(long contigIndex, IndexRange results);

    LogWriter openWriter(long prevTerm, long term, long index) throws IOException;

    LogReader openReader(long index);

    void sync() throws IOException;

    long syncCommit(long prevTerm, long term, long index) throws IOException;

    boolean isDurable(long index);

    boolean commitDurable(long index) throws IOException;
}
