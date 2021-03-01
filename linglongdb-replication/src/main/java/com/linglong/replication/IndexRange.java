package com.linglong.replication;

/**
 * @author Stereo
 */
@FunctionalInterface
interface IndexRange {
    void range(long startIndex, long endIndex);
}
