package com.glodon.linglong.engine.core;

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * @author Stereo
 */
public interface Sorter {
    /**
     * Add an entry into the sorter. If multiple entries are added with matching keys, only the
     * last one added is kept. After a sorter is fully finished or reset, no entries exist in
     * the sorter, and new entries can be added for another sort.
     *
     * @throws IllegalStateException if sort is finishing in another thread
     * @throws InterruptedIOException if reset by another thread
     */
    public void add(byte[] key, byte[] value) throws IOException;

    /**
     * Finish sorting the entries, and return a temporary index with the results.
     *
     * @throws IllegalStateException if sort is finishing in another thread
     * @throws InterruptedIOException if reset by another thread
     */
    public Index finish() throws IOException;

    /**
     * Returns a single-use Scanner over the sorted results, which deletes temporary resources
     * as it goes. Invoking this method causes the sort to be asynchronously finished, and the
     * Scanner might block waiting for entries to become available. Closing the Scanner before
     * the sort is finished interrupts it.
     *
     * @throws IllegalStateException if sort is finishing in another thread
     * @throws InterruptedIOException if reset by another thread
     */
    public Scanner finishScan() throws IOException;

    /**
     * Same as {@link #finishScan finishScan}, but in reverse order.
     *
     * @throws IllegalStateException if sort is finishing in another thread
     * @throws InterruptedIOException if reset by another thread
     */
    public Scanner finishScanReverse() throws IOException;

    /**
     * Returns an approximate count of entries which have finished, which is only updated while
     * sort results are being finished.
     */
    public long progress();

    /**
     * Discards all the entries and frees up space in the database. Can be called to interrupt
     * any sort which is in progress.
     */
    public void reset() throws IOException;
}
