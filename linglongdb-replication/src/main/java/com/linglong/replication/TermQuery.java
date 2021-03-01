package com.linglong.replication;

/**
 * @author Stereo
 */
@FunctionalInterface
public interface TermQuery {
    void term(long prevTerm, long term, long startIndex);
}
