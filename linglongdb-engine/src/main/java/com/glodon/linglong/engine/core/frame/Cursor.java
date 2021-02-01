package com.glodon.linglong.engine.core.frame;

import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.common.Ordering;
import com.glodon.linglong.engine.core.lock.LockResult;
import com.glodon.linglong.engine.core.tx.Transaction;
import com.glodon.linglong.engine.core.frame.ViewUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Comparator;

/**
 * @author Stereo
 */
public interface Cursor extends ValueAccessor, Closeable {

    byte[] NOT_LOADED = new byte[0];

    Ordering getOrdering();

    default Comparator<byte[]> getComparator() {
        return null;
    }

    Transaction link(Transaction txn);

    Transaction link();

    byte[] key();

    byte[] value();

    boolean autoload(boolean mode);

    boolean autoload();

    default int compareKeyTo(byte[] rkey) {
        byte[] lkey = key();
        return Utils.compareUnsigned(lkey, 0, lkey.length, rkey, 0, rkey.length);
    }

    default int compareKeyTo(byte[] rkey, int offset, int length) {
        byte[] lkey = key();
        return Utils.compareUnsigned(lkey, 0, lkey.length, rkey, offset, length);
    }

    default boolean register() throws IOException {
        return false;
    }

    default void unregister() {
    }

    LockResult first() throws IOException;

    LockResult last() throws IOException;

    LockResult skip(long amount) throws IOException;

    default LockResult skip(long amount, byte[] limitKey, boolean inclusive)
            throws IOException {
        return ViewUtils.skip(this, amount, limitKey, inclusive);
    }

    LockResult next() throws IOException;

    default LockResult nextLe(byte[] limitKey) throws IOException {
        return ViewUtils.nextCmp(this, limitKey, 1);
    }

    default LockResult nextLt(byte[] limitKey) throws IOException {
        return ViewUtils.nextCmp(this, limitKey, 0);
    }

    LockResult previous() throws IOException;

    default LockResult previousGe(byte[] limitKey) throws IOException {
        return ViewUtils.previousCmp(this, limitKey, -1);
    }

    default LockResult previousGt(byte[] limitKey) throws IOException {
        return ViewUtils.previousCmp(this, limitKey, 0);
    }

    LockResult find(byte[] key) throws IOException;

    default LockResult findGe(byte[] key) throws IOException {
        LockResult result = find(key);
        if (value() == null) {
            if (result == LockResult.ACQUIRED) {
                link().unlock();
            }
            result = next();
        }
        return result;
    }

    default LockResult findGt(byte[] key) throws IOException {
        ViewUtils.findNoLock(this, key);
        return next();
    }

    default LockResult findLe(byte[] key) throws IOException {
        LockResult result = find(key);
        if (value() == null) {
            if (result == LockResult.ACQUIRED) {
                link().unlock();
            }
            result = previous();
        }
        return result;
    }

    default LockResult findLt(byte[] key) throws IOException {
        ViewUtils.findNoLock(this, key);
        return previous();
    }

    default LockResult findNearby(byte[] key) throws IOException {
        return find(key);
    }

    default LockResult findNearbyGe(byte[] key) throws IOException {
        LockResult result = findNearby(key);
        if (value() == null) {
            if (result == LockResult.ACQUIRED) {
                link().unlock();
            }
            result = next();
        }
        return result;
    }

    default LockResult findNearbyGt(byte[] key) throws IOException {
        ViewUtils.findNearbyNoLock(this, key);
        return next();
    }

    default LockResult findNearbyLe(byte[] key) throws IOException {
        LockResult result = findNearby(key);
        if (value() == null) {
            if (result == LockResult.ACQUIRED) {
                link().unlock();
            }
            result = previous();
        }
        return result;
    }

    default LockResult findNearbyLt(byte[] key) throws IOException {
        ViewUtils.findNearbyNoLock(this, key);
        return previous();
    }

    LockResult random(byte[] lowKey, byte[] highKey) throws IOException;

    default LockResult lock() throws IOException {
        return load();
    }

    LockResult load() throws IOException;

    void store(byte[] value) throws IOException;

    default void commit(byte[] value) throws IOException {
        ViewUtils.commit(this, value);
    }

    Cursor copy();

    void reset();

    @Override
    void close();
}
