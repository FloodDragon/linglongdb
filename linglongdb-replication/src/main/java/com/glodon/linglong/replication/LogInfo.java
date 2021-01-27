package com.glodon.linglong.replication;

/**
 * @author Stereo
 */
public class LogInfo {
    long mTerm, mHighestIndex, mCommitIndex;

    @Override
    public String toString() {
        return "LogInfo: {term=" + mTerm + ", highestIndex=" +
                mHighestIndex + ", commitIndex=" + mCommitIndex + '}';
    }
}
