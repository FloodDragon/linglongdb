package com.glodon.linglong.engine.core;

import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.exception.LockFailureException;
import com.glodon.linglong.base.exception.ViewConstraintException;
import com.glodon.linglong.base.common.Ordering;
import com.glodon.linglong.engine.config.DurabilityMode;
import com.glodon.linglong.engine.core.frame.Cursor;
import com.glodon.linglong.engine.core.frame.Scanner;
import com.glodon.linglong.engine.core.frame.Transformer;
import com.glodon.linglong.engine.core.frame.ValueAccessor;
import com.glodon.linglong.engine.core.lock.DeadlockException;
import com.glodon.linglong.engine.core.lock.LockMode;
import com.glodon.linglong.engine.core.lock.LockResult;
import com.glodon.linglong.engine.core.tx.Transaction;
import com.glodon.linglong.engine.core.updater.CursorAutoCommitUpdater;
import com.glodon.linglong.engine.core.updater.CursorNonRepeatableUpdater;
import com.glodon.linglong.engine.core.updater.Updater;
import com.glodon.linglong.engine.core.view.ViewUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 数据库视图
 *
 * @author Stereo
 */
public interface View {

    Ordering getOrdering();


    default Comparator<byte[]> getComparator() {
        return null;
    }


    Cursor newCursor(Transaction txn);


    default Scanner newScanner(Transaction txn) throws IOException {
        Cursor c = newCursor(txn);
        c.first();
        return new CursorScanner(c);
    }


    default Updater newUpdater(Transaction txn) throws IOException {
        if (txn == null) {
            txn = newTransaction(null);
            Cursor c = newCursor(txn);
            try {
                return new CursorAutoCommitUpdater(c);
            } catch (Throwable e) {
                try {
                    txn.exit();
                } catch (Throwable e2) {
                    Utils.suppress(e, e2);
                }
                throw e;
            }
        } else {
            Cursor c = newCursor(txn);
            switch (txn.lockMode()) {
                default:
                    return new CursorSimpleUpdater(c);
                case REPEATABLE_READ:
                    return new CursorUpgradableUpdater(c);
                case READ_COMMITTED:
                case READ_UNCOMMITTED:
                    txn.enter();
                    txn.lockMode(LockMode.UPGRADABLE_READ);
                    return new CursorNonRepeatableUpdater(c);
            }
        }
    }

    default Cursor newAccessor(Transaction txn, byte[] key) throws IOException {
        Cursor c = newCursor(txn);
        try {
            c.autoload(false);
            c.find(key);
            return c;
        } catch (Throwable e) {
            Utils.closeQuietly(c);
            throw e;
        }
    }

    default Transaction newTransaction(DurabilityMode durabilityMode) {
        throw new UnsupportedOperationException();
    }

    default long count(byte[] lowKey, byte[] highKey) throws IOException {
        return ViewUtils.count(this, false, lowKey, highKey);
    }

    default byte[] load(Transaction txn, byte[] key) throws IOException {
        Cursor c = newCursor(txn);
        try {
            c.find(key);
            return c.value();
        } finally {
            c.reset();
        }
    }

    default boolean exists(Transaction txn, byte[] key) throws IOException {
        return load(txn, key) != null;
    }

    default void store(Transaction txn, byte[] key, byte[] value) throws IOException {
        txn = ViewUtils.enterScope(this, txn);
        Cursor c = newCursor(txn);
        try {
            c.autoload(false);
            c.find(key);
            if (c.key() == null) {
                if (value == null) {
                    return;
                }
                throw new ViewConstraintException();
            }
            c.commit(value);
        } finally {
            txn.exit();
            c.reset();
        }
    }

    default byte[] exchange(Transaction txn, byte[] key, byte[] value) throws IOException {
        txn = ViewUtils.enterScope(this, txn);
        Cursor c = newCursor(txn);
        try {
            c.find(key);
            if (c.key() == null) {
                if (value == null) {
                    return null;
                }
                throw new ViewConstraintException();
            }
            byte[] old = c.value();
            c.commit(value);
            return old;
        } finally {
            txn.exit();
            c.reset();
        }
    }

    default boolean insert(Transaction txn, byte[] key, byte[] value) throws IOException {
        return update(txn, key, null, value);
    }

    default boolean replace(Transaction txn, byte[] key, byte[] value) throws IOException {
        txn = ViewUtils.enterScope(this, txn);
        Cursor c = newCursor(txn);
        try {
            c.autoload(false);
            c.find(key);
            if (c.key() == null) {
                throw new ViewConstraintException();
            }
            // NOTE: Not atomic with BOGUS transaction.
            if (c.value() == null) {
                return false;
            }
            c.commit(value);
            return true;
        } finally {
            txn.exit();
            c.reset();
        }
    }

    default boolean update(Transaction txn, byte[] key, byte[] value) throws IOException {
        txn = ViewUtils.enterScope(this, txn);
        Cursor c = newCursor(txn);
        try {
            c.find(key);
            if (c.key() == null) {
                throw new ViewConstraintException();
            }
            // NOTE: Not atomic with BOGUS transaction.
            if (Arrays.equals(c.value(), value)) {
                return false;
            }
            c.commit(value);
            return true;
        } finally {
            txn.exit();
            c.reset();
        }
    }

    default boolean update(Transaction txn, byte[] key, byte[] oldValue, byte[] newValue)
            throws IOException {
        txn = ViewUtils.enterScope(this, txn);
        Cursor c = newCursor(txn);
        try {
            c.autoload(oldValue != null);
            c.find(key);
            if (c.key() == null) {
                throw new ViewConstraintException();
            }
            // NOTE: Not atomic with BOGUS transaction.
            if (!Arrays.equals(c.value(), oldValue)) {
                return false;
            }
            if (oldValue != null || newValue != null) {
                c.commit(newValue);
            }
            return true;
        } finally {
            txn.exit();
            c.reset();
        }
    }

    default boolean delete(Transaction txn, byte[] key) throws IOException {
        return replace(txn, key, null);
    }

    default boolean remove(Transaction txn, byte[] key, byte[] value) throws IOException {
        return update(txn, key, value, null);
    }

    default LockResult touch(Transaction txn, byte[] key) throws LockFailureException {
        try {
            LockMode mode;
            if (txn == null) {
                exists(null, key);
            } else if ((mode = txn.lockMode()) == LockMode.READ_COMMITTED) {
                LockResult result = lockShared(txn, key);
                if (result == LockResult.ACQUIRED) {
                    txn.unlock();
                }
            } else if (!mode.isNoReadLock()) {
                if (mode == LockMode.UPGRADABLE_READ) {
                    return lockUpgradable(txn, key);
                } else {
                    return lockShared(txn, key);
                }
            }
        } catch (IOException e) {
        }
        return LockResult.UNOWNED;
    }


    default LockResult tryLockShared(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException, ViewConstraintException {
        return ViewUtils.tryLock(txn, key, nanosTimeout, this::lockShared);
    }

    LockResult lockShared(Transaction txn, byte[] key)
            throws LockFailureException, ViewConstraintException;

    default LockResult tryLockUpgradable(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException, ViewConstraintException {
        return ViewUtils.tryLock(txn, key, nanosTimeout, this::lockUpgradable);
    }

    LockResult lockUpgradable(Transaction txn, byte[] key)
            throws LockFailureException, ViewConstraintException;

    default LockResult tryLockExclusive(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException, ViewConstraintException {
        return ViewUtils.tryLock(txn, key, nanosTimeout, this::lockExclusive);
    }

    LockResult lockExclusive(Transaction txn, byte[] key)
            throws LockFailureException, ViewConstraintException;

    LockResult lockCheck(Transaction txn, byte[] key) throws ViewConstraintException;

    default View viewGe(byte[] key) {
        Ordering ordering = getOrdering();
        if (ordering == Ordering.ASCENDING) {
            return BoundedView.viewGe(this, key);
        } else if (ordering == Ordering.DESCENDING) {
            return BoundedView.viewGe(viewReverse(), key).viewReverse();
        } else {
            throw new UnsupportedOperationException("Unsupported ordering: " + ordering);
        }
    }

    /**
     * Returns a sub-view, backed by this one, whose keys are greater than the
     * given key. Ownership of the key instance is transferred, and so it must
     * not be modified after calling this method.
     * <p>
     * <p>The returned view will throw a {@link ViewConstraintException} on an attempt to
     * insert a key outside its range.
     *
     * @throws UnsupportedOperationException if view is unordered
     * @throws NullPointerException          if key is null
     */
    public default View viewGt(byte[] key) {
        Ordering ordering = getOrdering();
        if (ordering == Ordering.ASCENDING) {
            return BoundedView.viewGt(this, key);
        } else if (ordering == Ordering.DESCENDING) {
            return BoundedView.viewGt(viewReverse(), key).viewReverse();
        } else {
            throw new UnsupportedOperationException("Unsupported ordering: " + ordering);
        }
    }

    /**
     * Returns a sub-view, backed by this one, whose keys are less than or
     * equal to the given key. Ownership of the key instance is transferred,
     * and so it must not be modified after calling this method.
     * <p>
     * <p>The returned view will throw a {@link ViewConstraintException} on an attempt to
     * insert a key outside its range.
     *
     * @throws UnsupportedOperationException if view is unordered
     * @throws NullPointerException          if key is null
     */
    public default View viewLe(byte[] key) {
        Ordering ordering = getOrdering();
        if (ordering == Ordering.ASCENDING) {
            return BoundedView.viewLe(this, key);
        } else if (ordering == Ordering.DESCENDING) {
            return BoundedView.viewLe(viewReverse(), key).viewReverse();
        } else {
            throw new UnsupportedOperationException("Unsupported ordering: " + ordering);
        }
    }

    /**
     * Returns a sub-view, backed by this one, whose keys are less than the
     * given key. Ownership of the key instance is transferred, and so it must
     * not be modified after calling this method.
     * <p>
     * <p>The returned view will throw a {@link ViewConstraintException} on an attempt to
     * insert a key outside its range.
     *
     * @throws UnsupportedOperationException if view is unordered
     * @throws NullPointerException          if key is null
     */
    public default View viewLt(byte[] key) {
        Ordering ordering = getOrdering();
        if (ordering == Ordering.ASCENDING) {
            return BoundedView.viewLt(this, key);
        } else if (ordering == Ordering.DESCENDING) {
            return BoundedView.viewLt(viewReverse(), key).viewReverse();
        } else {
            throw new UnsupportedOperationException("Unsupported ordering: " + ordering);
        }
    }

    /**
     * Returns a sub-view, backed by this one, whose keys start with the given prefix.
     * Ownership of the prefix instance is transferred, and so it must not be modified after
     * calling this method.
     * <p>
     * <p>The returned view will throw a {@link ViewConstraintException} on an attempt to
     * insert a key outside its range.
     *
     * @param trim amount of prefix length to trim from all keys in the view
     * @throws UnsupportedOperationException if view is unordered
     * @throws NullPointerException          if prefix is null
     * @throws IllegalArgumentException      if trim is longer than prefix
     */
    public default View viewPrefix(byte[] prefix, int trim) {
        Ordering ordering = getOrdering();
        if (ordering == Ordering.ASCENDING) {
            return BoundedView.viewPrefix(this, prefix, trim);
        } else if (ordering == Ordering.DESCENDING) {
            return BoundedView.viewPrefix(viewReverse(), prefix, trim).viewReverse();
        } else {
            throw new UnsupportedOperationException("Unsupported ordering: " + ordering);
        }
    }

    /**
     * Returns a sub-view, backed by this one, whose entries have been filtered out and
     * transformed.
     * <p>
     * <p>The returned view will throw a {@link ViewConstraintException} on an attempt to
     * insert an entry not supported by the transformer.
     *
     * @throws NullPointerException if transformer is null
     */
    public default View viewTransformed(Transformer transformer) {
        return TransformedView.apply(this, transformer);
    }

    /**
     * Returns a view which represents the <a
     * href="https://en.wikipedia.org/wiki/Union_(set_theory)"><cite>set union</cite></a> of
     * this view and a second one. A union eliminates duplicate keys, by relying on a combiner
     * to decide how to deal with them. If the combiner chooses to {@link Combiner#discard
     * discard} duplicate keys, then the returned view represents the <a
     * href="https://en.wikipedia.org/wiki/Symmetric_difference"><cite>symmetric set
     * difference</cite></a> instead.
     * <p>
     * <p>Storing entries in the union is permitted, if the combiner supports {@link
     * Combiner#separate separation}. The separator must supply at least one non-null value, or
     * else a {@link ViewConstraintException} will be thrown.
     *
     * @param combiner combines common entries together; pass null to always choose the {@link
     *                 Combiner#first first}
     * @param second   required second view in the union
     * @throws NullPointerException     if second view is null
     * @throws IllegalArgumentException if the views don't define a consistent ordering, as
     *                                  specified by their comparators
     */
    public default View viewUnion(Combiner combiner, View second) {
        if (combiner == null) {
            combiner = Combiner.first();
        }
        return new UnionView(combiner, this, second);
    }

    /**
     * Returns a view which represents the <a
     * href="https://en.wikipedia.org/wiki/Intersection_(set_theory)"><cite>set
     * intersection</cite></a> of this view and a second one. An intersection eliminates
     * duplicate keys, by relying on a combiner to decide how to deal with them.
     * <p>
     * <p>Storing entries in the intersection is permitted, if the combiner supports {@link
     * Combiner#separate separation}. The separator must supply two non-null values, or else a
     * {@link ViewConstraintException} will be thrown.
     *
     * @param combiner combines common entries together; pass null to always choose the {@link
     *                 Combiner#first first}
     * @param second   required second view in the intersection
     * @throws NullPointerException     if second view is null
     * @throws IllegalArgumentException if the views don't define a consistent ordering, as
     *                                  specified by their comparators
     */
    public default View viewIntersection(Combiner combiner, View second) {
        if (combiner == null) {
            combiner = Combiner.first();
        }
        return new IntersectionView(combiner, this, second);
    }

    /**
     * Returns a view which represents the <a
     * href="https://en.wikipedia.org/wiki/Complement_(set_theory)#Relative_complement"><cite>set
     * difference</cite></a> of this view and a second one. A difference eliminates duplicate
     * keys, by relying on a combiner to decide how to deal with them.
     * <p>
     * <p>Storing entries in the difference is permitted, if the combiner supports {@link
     * Combiner#separate separation}.  The separator must supply a non-null first value, or
     * else a {@link ViewConstraintException} will be thrown.
     *
     * @param combiner combines common entries together; pass null to always {@link
     *                 Combiner#discard discard} them
     * @param second   required second view in the difference
     * @throws NullPointerException     if second view is null
     * @throws IllegalArgumentException if the views don't define a consistent ordering, as
     *                                  specified by their comparators
     */
    public default View viewDifference(Combiner combiner, View second) {
        if (combiner == null) {
            combiner = Combiner.discard();
        }
        return new DifferenceView(combiner, this, second);
    }


     default View viewKeys() {
        return new KeyOnlyView(this);
    }


     default View viewReverse() {
        return new ReverseView(this);
    }


     default View viewUnmodifiable() {
        return UnmodifiableView.apply(this);
    }


     boolean isUnmodifiable();

    default boolean isModifyAtomic() {
        return false;
    }
}
