package com.linglong.replication;

/**
 * @author Stereo
 */
public interface LKey<T extends LKey> extends Comparable<LKey<T>> {
    long key();

    @Override
    default int compareTo(LKey<T> other) {
        return Long.compare(key(), other.key());
    }

    class Finder<T extends LKey> implements LKey<T> {
        private final long mKey;

        Finder(long key) {
            mKey = key;
        }

        @Override
        public long key() {
            return mKey;
        }
    }
}
