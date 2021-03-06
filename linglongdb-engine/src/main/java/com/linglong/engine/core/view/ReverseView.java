package com.linglong.engine.core.view;

import com.linglong.base.common.Ordering;
import com.linglong.base.exception.LockFailureException;
import com.linglong.base.exception.ViewConstraintException;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.frame.Cursor;
import com.linglong.engine.core.frame.Transformer;
import com.linglong.engine.core.frame.View;
import com.linglong.engine.core.lock.DeadlockException;
import com.linglong.engine.core.lock.LockResult;
import com.linglong.engine.core.tx.Transaction;
import com.linglong.engine.core.frame.ViewUtils;

import java.io.IOException;
import java.util.Comparator;

/**
 * @author Stereo
 */
public final class ReverseView implements View {
    private final View mSource;

    public ReverseView(View source) {
        mSource = source;
    }

    @Override
    public Ordering getOrdering() {
        return mSource.getOrdering().reverse();
    }

    @Override
    public Comparator<byte[]> getComparator() {
        return mSource.getComparator().reversed();
    }

    @Override
    public Cursor newCursor(Transaction txn) {
        return new ReverseCursor(mSource.newCursor(txn));
    }

    @Override
    public Transaction newTransaction(DurabilityMode durabilityMode) {
        return mSource.newTransaction(durabilityMode);
    }

    @Override
    public long count(byte[] lowKey, byte[] highKey) throws IOException {
        return mSource.count(appendZero(highKey), appendZero((lowKey)));
    }

    @Override
    public byte[] load(Transaction txn, byte[] key) throws IOException {
        return mSource.load(txn, key);
    }

    @Override
    public boolean exists(Transaction txn, byte[] key) throws IOException {
        return mSource.exists(txn, key);
    }

    @Override
    public void store(Transaction txn, byte[] key, byte[] value) throws IOException {
        mSource.store(txn, key, value);
    }

    @Override
    public byte[] exchange(Transaction txn, byte[] key, byte[] value) throws IOException {
        return mSource.exchange(txn, key, value);
    }

    @Override
    public boolean insert(Transaction txn, byte[] key, byte[] value) throws IOException {
        return mSource.insert(txn, key, value);
    }

    @Override
    public boolean replace(Transaction txn, byte[] key, byte[] value) throws IOException {
        return mSource.replace(txn, key, value);
    }

    @Override
    public boolean update(Transaction txn, byte[] key, byte[] value) throws IOException {
        return mSource.update(txn, key, value);
    }

    @Override
    public boolean update(Transaction txn, byte[] key, byte[] oldValue, byte[] newValue)
            throws IOException {
        return mSource.update(txn, key, oldValue, newValue);
    }

    @Override
    public boolean delete(Transaction txn, byte[] key) throws IOException {
        return mSource.delete(txn, key);
    }

    @Override
    public boolean remove(Transaction txn, byte[] key, byte[] value) throws IOException {
        return mSource.remove(txn, key, value);
    }

    @Override
    public LockResult touch(Transaction txn, byte[] key) throws LockFailureException {
        return mSource.touch(txn, key);
    }

    @Override
    public LockResult tryLockShared(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException, ViewConstraintException {
        return mSource.tryLockShared(txn, key, nanosTimeout);
    }

    @Override
    public LockResult lockShared(Transaction txn, byte[] key)
            throws LockFailureException, ViewConstraintException {
        return mSource.lockShared(txn, key);
    }

    @Override
    public LockResult tryLockUpgradable(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException, ViewConstraintException {
        return mSource.tryLockUpgradable(txn, key, nanosTimeout);
    }

    @Override
    public LockResult lockUpgradable(Transaction txn, byte[] key)
            throws LockFailureException, ViewConstraintException {
        return mSource.lockUpgradable(txn, key);
    }

    @Override
    public LockResult tryLockExclusive(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException, ViewConstraintException {
        return mSource.tryLockExclusive(txn, key, nanosTimeout);
    }

    @Override
    public LockResult lockExclusive(Transaction txn, byte[] key)
            throws LockFailureException, ViewConstraintException {
        return mSource.lockExclusive(txn, key);
    }

    @Override
    public final LockResult lockCheck(Transaction txn, byte[] key) throws ViewConstraintException {
        return mSource.lockCheck(txn, key);
    }

    @Override
    public View viewGe(byte[] key) {
        return new ReverseView(mSource.viewLe(key));
    }

    @Override
    public View viewGt(byte[] key) {
        return new ReverseView(mSource.viewLt(key));
    }

    @Override
    public View viewLe(byte[] key) {
        return new ReverseView(mSource.viewGe(key));
    }

    @Override
    public View viewLt(byte[] key) {
        return new ReverseView(mSource.viewGt(key));
    }

    @Override
    public View viewPrefix(byte[] prefix, int trim) {
        return new ReverseView(mSource.viewPrefix(prefix, trim));
    }

    @Override
    public View viewTransformed(Transformer transformer) {
        return new ReverseView(mSource.viewTransformed(transformer));
    }

    @Override
    public View viewKeys() {
        View sourceKeys = mSource.viewKeys();
        return sourceKeys == mSource ? this : new ReverseView(sourceKeys);
    }

    @Override
    public View viewReverse() {
        return mSource;
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

    static byte[] appendZero(byte[] key) {
        return key == null ? null : ViewUtils.appendZero(key);
    }
}
