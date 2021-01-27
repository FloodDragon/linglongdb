package com.glodon.linglong.replication;

/**
 * @author Stereo
 */
@FunctionalInterface
interface IndexRange {
    void range(long startIndex, long endIndex);
}
