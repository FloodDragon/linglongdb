package com.glodon.linglong.engine.core;

import com.glodon.linglong.base.common.EntryConsumer;

import java.io.IOException;
import java.util.Comparator;

/**
 * Scans through all entries in a view and passes them to a consumer. Scanner implementations
 * which perform pre-fetching can be more efficient than a {@link Cursor cursor}. Any exception
 * thrown by a scan action automatically closes the Scanner.
 *
 * <p>Scanner instances can only be safely used by one thread at a time, and they must be
 * closed when no longer needed. Instances can be exchanged by threads, as long as a
 * happens-before relationship is established. Without proper exclusion, multiple threads
 * interacting with a Scanner instance may cause database corruption.
 *
 * @author Stereo
 * @see View#newScanner View.newScanner
 * @see Updater
 */
public interface Scanner extends AutoCloseable {
    /**
     * Returns a comparator for the ordering of this scanner, or null if unordered.
     */
    Comparator<byte[]> getComparator();

    /**
     * Returns an uncopied reference to the current key, or null if closed. Array contents
     * must not be modified.
     */
    byte[] key();

    /**
     * Returns an uncopied reference to the current value, which might be null or {@link
     * Cursor#NOT_LOADED}. Array contents can be safely modified.
     */
    byte[] value();

    /**
     * Step to the next entry.
     *
     * @return false if no more entries remain and scanner has been closed
     */
    boolean step() throws IOException;

    /**
     * Steps over the requested amount of entries.
     *
     * @param amount amount of entries to step over
     * @return false if no more entries remain and scanner has been closed
     * @throws IllegalArgumentException if amount is negative
     */
    default boolean step(long amount) throws IOException {
        if (amount > 0) while (true) {
            boolean result = step();
            if (!result || --amount <= 0) {
                return result;
            }
        }
        if (amount == 0) {
            return key() != null;
        }
        throw ViewUtils.fail(this, new IllegalArgumentException());
    }

    /**
     * Calls the given action for each remaining entry, and then closes the scanner.
     */
    default void scanAll(EntryConsumer action) throws IOException {
        while (true) {
            byte[] key = key();
            if (key == null) {
                return;
            }
            try {
                action.accept(key, value());
            } catch (Throwable e) {
                throw ViewUtils.fail(this, e);
            }
            step();
        }
    }

    @Override
    public void close() throws IOException;
}
