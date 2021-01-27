package com.glodon.linglong.replication;

import java.io.IOException;

/**
 * @author Stereo
 */
interface LogReader extends StreamReplicator.Reader {
    long prevTerm();

    int tryRead(byte[] buf, int offset, int length) throws IOException;

    int tryReadAny(byte[] buf, int offset, int length) throws IOException;

    void release();
}
