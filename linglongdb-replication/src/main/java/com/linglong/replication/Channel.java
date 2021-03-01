package com.linglong.replication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

/**
 * 复制通道
 *
 * @author Stereo
 */
interface Channel {
    default Peer peer() {
        return null;
    }

    default int waitForConnection(int timeoutMillis) throws InterruptedIOException {
        return timeoutMillis;
    }

    void unknown(Channel from, int op);

    boolean nop(Channel from);

    boolean requestVote(Channel from, long term, long candidateId,
                        long highestTerm, long highestIndex);

    boolean requestVoteReply(Channel from, long term);

    boolean queryTerms(Channel from, long startIndex, long endIndex);

    boolean queryTermsReply(Channel from, long prevTerm, long term, long startIndex);

    boolean queryData(Channel from, long startIndex, long endIndex);

    boolean queryDataReply(Channel from, long currentTerm,
                           long prevTerm, long term, long index, byte[] data);

    boolean writeData(Channel from, long prevTerm, long term, long index,
                      long highestIndex, long commitIndex, byte[] data);

    boolean writeDataReply(Channel from, long term, long highestIndex);

    boolean syncCommit(Channel from, long prevTerm, long term, long index);

    boolean syncCommitReply(Channel from, long groupVersion, long term, long index);

    boolean compact(Channel from, long index);

    boolean snapshotScore(Channel from);

    boolean snapshotScoreReply(Channel from, int activeSessions, float weight);

    boolean updateRole(Channel from, long groupVersion, long memberId, Role role);

    boolean updateRoleReply(Channel from, long groupVersion, long memberId, byte result);

    boolean groupVersion(Channel from, long groupVersion);

    boolean groupVersionReply(Channel from, long groupVersion);

    boolean groupFile(Channel from, long groupVersion) throws IOException;

    OutputStream groupFileReply(Channel from, InputStream in) throws IOException;
}
