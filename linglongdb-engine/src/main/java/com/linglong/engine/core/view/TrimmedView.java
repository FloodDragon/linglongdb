package com.linglong.engine.core.view;

import com.linglong.base.common.Ordering;
import com.linglong.base.common.Utils;
import com.linglong.base.exception.LockFailureException;
import com.linglong.base.exception.ViewConstraintException;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.frame.Cursor;
import com.linglong.engine.core.frame.Transformer;
import com.linglong.engine.core.frame.View;
import com.linglong.engine.core.lock.DeadlockException;
import com.linglong.engine.core.lock.LockResult;
import com.linglong.engine.core.tx.Transaction;

import java.io.IOException;
import java.util.Comparator;

/**
 * @author Stereo
 */
public final class TrimmedView implements View {
    private final View mSource;
    private final byte[] mPrefix;
    final int mTrim;

    TrimmedView(View source, byte[] prefix, int trim) {
        mSource = source;
        mPrefix = prefix;
        mTrim = trim;
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
    public Cursor newCursor(Transaction txn) {
        return new TrimmedCursor(this, mSource.newCursor(txn));
    }

    @Override
    public long count(byte[] lowKey, byte[] highKey) throws IOException {
        return mSource.count(lowKey, highKey);
    }

    @Override
    public Transaction newTransaction(DurabilityMode durabilityMode) {
        return mSource.newTransaction(durabilityMode);
    }

    @Override
    public byte[] load(Transaction txn, byte[] key) throws IOException {
        return mSource.load(txn, applyPrefix(key));
    }

    @Override
    public boolean exists(Transaction txn, byte[] key) throws IOException {
        return mSource.exists(txn, applyPrefix(key));
    }

    @Override
    public void store(Transaction txn, byte[] key, byte[] value) throws IOException {
        mSource.store(txn, applyPrefix(key), value);
    }

    @Override
    public byte[] exchange(Transaction txn, byte[] key, byte[] value) throws IOException {
        return mSource.exchange(txn, applyPrefix(key), value);
    }

    @Override
    public boolean insert(Transaction txn, byte[] key, byte[] value) throws IOException {
        return mSource.insert(txn, applyPrefix(key), value);
    }

    @Override
    public boolean replace(Transaction txn, byte[] key, byte[] value) throws IOException {
        return mSource.replace(txn, applyPrefix(key), value);
    }

    @Override
    public boolean update(Transaction txn, byte[] key, byte[] value) throws IOException {
        return mSource.update(txn, applyPrefix(key), value);
    }

    @Override
    public boolean update(Transaction txn, byte[] key, byte[] oldValue, byte[] newValue)
            throws IOException {
        return mSource.update(txn, applyPrefix(key), oldValue, newValue);
    }

    @Override
    public boolean delete(Transaction txn, byte[] key) throws IOException {
        return mSource.delete(txn, applyPrefix(key));
    }

    @Override
    public boolean remove(Transaction txn, byte[] key, byte[] value) throws IOException {
        return mSource.remove(txn, applyPrefix(key), value);
    }

    @Override
    public LockResult touch(Transaction txn, byte[] key) throws LockFailureException {
        return mSource.touch(txn, applyPrefix(key));
    }

    @Override
    public LockResult tryLockShared(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException, ViewConstraintException {
        return mSource.tryLockShared(txn, applyPrefix(key), nanosTimeout);
    }

    @Override
    public final LockResult lockShared(Transaction txn, byte[] key)
            throws LockFailureException, ViewConstraintException {
        return mSource.lockShared(txn, applyPrefix(key));
    }

    @Override
    public LockResult tryLockUpgradable(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException, ViewConstraintException {
        return mSource.tryLockUpgradable(txn, applyPrefix(key), nanosTimeout);
    }

    @Override
    public final LockResult lockUpgradable(Transaction txn, byte[] key)
            throws LockFailureException, ViewConstraintException {
        return mSource.lockUpgradable(txn, applyPrefix(key));
    }

    @Override
    public LockResult tryLockExclusive(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException, ViewConstraintException {
        return mSource.tryLockExclusive(txn, applyPrefix(key), nanosTimeout);
    }

    @Override
    public final LockResult lockExclusive(Transaction txn, byte[] key)
            throws LockFailureException, ViewConstraintException {
        return mSource.lockExclusive(txn, applyPrefix(key));
    }

    @Override
    public final LockResult lockCheck(Transaction txn, byte[] key) throws ViewConstraintException {
        return mSource.lockCheck(txn, applyPrefix(key));
    }

    @Override
    public View viewGe(byte[] key) {
        return new TrimmedView(mSource.viewGe(applyPrefix(key)), mPrefix, mTrim);
    }

    @Override
    public View viewGt(byte[] key) {
        return new TrimmedView(mSource.viewGt(applyPrefix(key)), mPrefix, mTrim);
    }

    @Override
    public View viewLe(byte[] key) {
        return new TrimmedView(mSource.viewLe(applyPrefix(key)), mPrefix, mTrim);
    }

    @Override
    public View viewLt(byte[] key) {
        return new TrimmedView(mSource.viewLt(applyPrefix(key)), mPrefix, mTrim);
    }

    @Override
    public View viewPrefix(byte[] prefix, int trim) {
        SubView.prefixCheck(prefix, trim);
        return mSource.viewPrefix(applyPrefix(prefix), mTrim + trim);
    }

    @Override
    public View viewTransformed(Transformer transformer) {
        return TransformedView.apply(this, transformer);
    }

    @Override
    public View viewKeys() {
        View sourceKeys = mSource.viewKeys();
        return sourceKeys == mSource ? this : new TrimmedView(sourceKeys, mPrefix, mTrim);
    }

    @Override
    public View viewReverse() {
        return new TrimmedView(mSource.viewReverse(), mPrefix, mTrim);
    }

    @Override
    public View viewUnmodifiable() {
        return UnmodifiableView.apply(this);
    }

    @Override
    public boolean isUnmodifiable() {
        return mSource.isUnmodifiable();
    }

    @Override
    public boolean isModifyAtomic() {
        return mSource.isModifyAtomic();
    }

    byte[] applyPrefix(byte[] key) {
        return applyPrefix(key, 0, key.length);
    }

    byte[] applyPrefix(byte[] key, int offset, int length) {
        Utils.keyCheck(key);
        byte[] prefix = mPrefix;
        byte[] full = new byte[prefix.length + length];
        System.arraycopy(prefix, 0, full, 0, prefix.length);
        System.arraycopy(key, offset, full, prefix.length, length);
        return full;
    }
}
