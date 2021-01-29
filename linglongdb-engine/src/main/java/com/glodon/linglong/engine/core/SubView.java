package com.glodon.linglong.engine.core;

import com.glodon.linglong.base.exception.LockFailureException;
import com.glodon.linglong.base.exception.ViewConstraintException;
import com.glodon.linglong.engine.Ordering;
import com.glodon.linglong.engine.config.DurabilityMode;
import com.glodon.linglong.engine.core.lock.DeadlockException;
import com.glodon.linglong.engine.core.lock.LockResult;
import com.glodon.linglong.engine.core.tx.Transaction;

import java.io.IOException;
import java.util.Comparator;

/**
 * @author Stereo
 */
public abstract class SubView implements View {
    final View mSource;

    public SubView(View source) {
        mSource = source;
    }

    @Override
    public Ordering getOrdering() {
        return mSource.getOrdering();
    }

    @Override
    public Comparator<byte[]> getComparator() {
        return mSource.getComparator();
    }

    @Override
    public Transaction newTransaction(DurabilityMode durabilityMode) {
        return mSource.newTransaction(durabilityMode);
    }

    @Override
    public byte[] load(Transaction txn, byte[] key) throws IOException {
        return inRange(key) ? mSource.load(txn, key) : null;
    }

    @Override
    public boolean exists(Transaction txn, byte[] key) throws IOException {
        return inRange(key) ? mSource.exists(txn, key) : false;
    }

    @Override
    public void store(Transaction txn, byte[] key, byte[] value) throws IOException {
        if (inRange(key)) {
            mSource.store(txn, key, value);
        } else if (value != null) {
            throw fail();
        }
    }

    @Override
    public byte[] exchange(Transaction txn, byte[] key, byte[] value) throws IOException {
        if (inRange(key)) {
            return mSource.exchange(txn, key, value);
        }
        if (value == null) {
            return null;
        }
        throw fail();
    }

    @Override
    public boolean insert(Transaction txn, byte[] key, byte[] value) throws IOException {
        if (inRange(key)) {
            return mSource.insert(txn, key, value);
        }
        if (value == null) {
            return true;
        }
        throw fail();
    }

    @Override
    public boolean replace(Transaction txn, byte[] key, byte[] value) throws IOException {
        return inRange(key) ? mSource.replace(txn, key, value) : false;
    }

    @Override
    public boolean update(Transaction txn, byte[] key, byte[] value) throws IOException {
        if (inRange(key)) {
            return mSource.update(txn, key, value);
        }
        if (value != null) {
            throw fail();
        }
        return false;
    }

    @Override
    public boolean update(Transaction txn, byte[] key, byte[] oldValue, byte[] newValue)
            throws IOException {
        if (inRange(key)) {
            return mSource.update(txn, key, oldValue, newValue);
        }
        if (oldValue == null) {
            if (newValue == null) {
                return true;
            }
            throw fail();
        }
        return false;
    }

    @Override
    public boolean delete(Transaction txn, byte[] key) throws IOException {
        return inRange(key) ? mSource.delete(txn, key) : false;
    }

    @Override
    public boolean remove(Transaction txn, byte[] key, byte[] value) throws IOException {
        return inRange(key) ? mSource.remove(txn, key, value) : (value == null);
    }

    @Override
    public LockResult touch(Transaction txn, byte[] key) throws LockFailureException {
        return inRange(key) ? mSource.touch(txn, key) : LockResult.UNOWNED;
    }

    @Override
    public LockResult tryLockShared(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException, ViewConstraintException {
        if (inRange(key)) {
            return mSource.tryLockShared(txn, key, nanosTimeout);
        }
        throw fail();
    }

    @Override
    public final LockResult lockShared(Transaction txn, byte[] key)
            throws LockFailureException, ViewConstraintException {
        if (inRange(key)) {
            return mSource.lockShared(txn, key);
        }
        throw fail();
    }

    @Override
    public LockResult tryLockUpgradable(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException, ViewConstraintException {
        if (inRange(key)) {
            return mSource.tryLockUpgradable(txn, key, nanosTimeout);
        }
        throw fail();
    }

    @Override
    public final LockResult lockUpgradable(Transaction txn, byte[] key)
            throws LockFailureException, ViewConstraintException {
        if (inRange(key)) {
            return mSource.lockUpgradable(txn, key);
        }
        throw fail();
    }

    @Override
    public final LockResult tryLockExclusive(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException, ViewConstraintException {
        if (inRange(key)) {
            return mSource.tryLockExclusive(txn, key, nanosTimeout);
        }
        throw fail();
    }

    @Override
    public final LockResult lockExclusive(Transaction txn, byte[] key)
            throws LockFailureException, ViewConstraintException {
        if (inRange(key)) {
            return mSource.lockExclusive(txn, key);
        }
        throw fail();
    }

    @Override
    public final LockResult lockCheck(Transaction txn, byte[] key) throws ViewConstraintException {
        if (inRange(key)) {
            return mSource.lockCheck(txn, key);
        }
        throw fail();
    }

    @Override
    public boolean isUnmodifiable() {
        return mSource.isUnmodifiable();
    }

    @Override
    public boolean isModifyAtomic() {
        return mSource.isModifyAtomic();
    }

    abstract boolean inRange(byte[] key);

    static void prefixCheck(byte[] prefix, int trim) {
        if (prefix == null) {
            throw new NullPointerException("Prefix is null");
        }
        if (trim < 0 | trim > prefix.length) {
            if (trim < 0) {
                throw new IllegalArgumentException("Negative trim");
            }
            throw new IllegalArgumentException("Trim amount is longer than prefix");
        }
    }

    public static ViewConstraintException fail() {
        return new ViewConstraintException("Key is outside allowed range");
    }
}
