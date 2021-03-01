package com.linglong.replication;

import java.util.concurrent.CountDownLatch;

/**
 * @author Stereo
 */
final class SnapshotScore extends CountDownLatch implements Comparable<SnapshotScore> {
    final Object mRequestedBy;
    final Channel mChannel;

    int mActiveSessions;
    float mWeight;

    SnapshotScore(Object requestedBy, Channel channel) {
        super(1);
        mRequestedBy = requestedBy;
        mChannel = channel;
    }

    void snapshotScoreReply(int activeSessions, float weight) {
        mActiveSessions = activeSessions;
        mWeight = weight;
        countDown();
    }

    @Override
    public int compareTo(SnapshotScore other) {
        int cmp = Integer.compare(mActiveSessions, other.mActiveSessions);
        if (cmp == 0) {
            cmp = Float.compare(mWeight, other.mWeight);
        }
        return cmp;
    }
}
