package com.linglong.replication;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author Stereo
 */
public final class Peer implements Comparable<Peer> {
    private static final AtomicLongFieldUpdater<Peer> cGroupVersionUpdater =
            AtomicLongFieldUpdater.newUpdater(Peer.class, "mGroupVersion");

    private static final AtomicReferenceFieldUpdater<Peer, SnapshotScore> cSnapshotScoreUpdater =
            AtomicReferenceFieldUpdater.newUpdater(Peer.class, SnapshotScore.class, "mSnapshotScore");

    final long mMemberId;
    final SocketAddress mAddress;

    Role mRole;

    long mMatchIndex;

    long mSyncMatchIndex;

    volatile long mCompactIndex;

    volatile long mGroupVersion;

    private volatile SnapshotScore mSnapshotScore;

    Peer(long memberId) {
        mMemberId = memberId;
        mAddress = null;
    }

    Peer(long memberId, SocketAddress addr, Role role) {
        if (memberId == 0 || addr == null || role == null) {
            throw new IllegalArgumentException();
        }
        mMemberId = memberId;
        mAddress = addr;
        mRole = role;
    }

    long updateGroupVersion(final long groupVersion) {
        while (true) {
            long currentVersion = mGroupVersion;
            if (groupVersion <= currentVersion ||
                    cGroupVersionUpdater.compareAndSet(this, currentVersion, groupVersion)) {
                return currentVersion;
            }
        }
    }

    void resetSnapshotScore(SnapshotScore score) {
        mSnapshotScore = score;
    }

    SnapshotScore awaitSnapshotScore(Object requestedBy, long timeoutMillis) {
        SnapshotScore score = mSnapshotScore;

        if (score != null) {
            final SnapshotScore waitFor = score;

            try {
                if (!waitFor.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
                    score = null;
                }
            } catch (InterruptedException e) {
            }

            if (waitFor.mRequestedBy == requestedBy) {
                cSnapshotScoreUpdater.compareAndSet(this, waitFor, null);
            }
        }

        return score;
    }

    void snapshotScoreReply(int activeSessions, float weight) {
        SnapshotScore score = mSnapshotScore;
        if (score != null) {
            score.snapshotScoreReply(activeSessions, weight);
        }
    }

    public long getMemberId() {
        return mMemberId;
    }

    public SocketAddress getAddress() {
        return mAddress;
    }

    public Role getRole() {
        return mRole;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(mMemberId) + Objects.hashCode(mAddress) + Objects.hashCode(mRole);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Peer) {
            Peer other = (Peer) obj;
            return mMemberId == other.mMemberId && Objects.equals(mAddress, other.mAddress)
                    && Objects.equals(mRole, other.mRole);
        }
        return false;
    }

    @Override
    public int compareTo(Peer other) {
        return Long.compare(mMatchIndex, other.mMatchIndex);
    }

    @Override
    public String toString() {
        return "Peer: {memberId=" + Long.toUnsignedString(mMemberId)
                + ", address=" + mAddress + ", role=" + mRole + '}';
    }
}
