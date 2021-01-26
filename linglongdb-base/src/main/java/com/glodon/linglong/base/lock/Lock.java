package com.glodon.linglong.base.lock;

import com.glodon.linglong.base.concurrent.Latch;
import com.glodon.linglong.base.concurrent.LatchCondition;
import com.glodon.linglong.base.core.Database;

import java.util.Arrays;

/**
 * 部分可重入的共享/可升级/独占锁，且公平获得方法。
 * 锁归LockOwners而非线程所有。
 * 实施依赖互斥的锁，但条件变量逻辑用于在LockOwners之间转移所有权。
 *
 * @author Stereo
 */
public final class Lock {
    long mIndexId;
    byte[] mKey;
    int mHashCode;

    Lock mLockManagerNext;

    int mLockCount;

    LockOwner mOwner;

    private Object mSharedLockOwnersObj;

    LatchCondition mQueueU;

    LatchCondition mQueueSX;

    boolean isAvailable(LockOwner locker) {
        return mLockCount >= 0 || mOwner == locker;
    }

    LockResult check(LockOwner locker) {
        int count = mLockCount;
        return mOwner == locker
                ? (count == ~0 ? LockResult.OWNED_EXCLUSIVE : LockResult.OWNED_UPGRADABLE)
                : ((count != 0 && isSharedLockOwner(locker)) ? LockResult.OWNED_SHARED : LockResult.UNOWNED);
    }

    LockResult tryLockShared(Latch latch, Locker locker, long nanosTimeout) {
        if (mOwner == locker) {
            return mLockCount == ~0 ? LockResult.OWNED_EXCLUSIVE : LockResult.OWNED_UPGRADABLE;
        }

        LatchCondition queueSX = mQueueSX;
        if (queueSX != null) {
            if (mLockCount != 0 && isSharedLockOwner(locker)) {
                return LockResult.OWNED_SHARED;
            }
            if (nanosTimeout == 0) {
                locker.mWaitingFor = this;
                return LockResult.TIMED_OUT_LOCK;
            }
        } else {
            LockResult r = tryLockShared(locker);
            if (r != null) {
                return r;
            }
            if (nanosTimeout == 0) {
                locker.mWaitingFor = this;
                return LockResult.TIMED_OUT_LOCK;
            }
            mQueueSX = queueSX = new LatchCondition();
        }

        locker.mWaitingFor = this;
        long nanosEnd = nanosTimeout < 0 ? 0 : (System.nanoTime() + nanosTimeout);

        while (true) {
            int w = queueSX.awaitShared(latch, nanosTimeout, nanosEnd);
            queueSX = mQueueSX;

            if (queueSX == null) {
                locker.mWaitingFor = null;
                return LockResult.INTERRUPTED;
            }

            if (!queueSX.signalNextShared()) {
                mQueueSX = null;
            }

            if (w < 1) {
                if (w == 0) {
                    return LockResult.TIMED_OUT_LOCK;
                } else {
                    locker.mWaitingFor = null;
                    return LockResult.INTERRUPTED;
                }
            }

            if (mOwner == locker) {
                locker.mWaitingFor = null;
                return mLockCount == ~0 ? LockResult.OWNED_EXCLUSIVE : LockResult.OWNED_UPGRADABLE;
            }

            LockResult r = tryLockShared(locker);
            if (r != null) {
                locker.mWaitingFor = null;
                return r;
            }

            if (nanosTimeout >= 0 && (nanosTimeout = nanosEnd - System.nanoTime()) <= 0) {
                return LockResult.TIMED_OUT_LOCK;
            }
        }
    }

    LockResult tryLockUpgradable(Latch latch, Locker locker, long nanosTimeout) {
        if (mOwner == locker) {
            return mLockCount == ~0 ? LockResult.OWNED_EXCLUSIVE : LockResult.OWNED_UPGRADABLE;
        }

        int count = mLockCount;
        if (count != 0 && isSharedLockOwner(locker)) {
            if (!locker.canAttemptUpgrade(count)) {
                return LockResult.ILLEGAL;
            }
            if (count > 0) {
                mLockCount = (count - 1) | 0x80000000;
                mOwner = locker;
                return LockResult.OWNED_UPGRADABLE;
            }
        }

        LatchCondition queueU = mQueueU;
        if (queueU != null) {
            if (nanosTimeout == 0) {
                locker.mWaitingFor = this;
                return LockResult.TIMED_OUT_LOCK;
            }
        } else {
            if (count >= 0) {
                mLockCount = count | 0x80000000;
                mOwner = locker;
                return LockResult.ACQUIRED;
            }
            if (nanosTimeout == 0) {
                locker.mWaitingFor = this;
                return LockResult.TIMED_OUT_LOCK;
            }
            mQueueU = queueU = new LatchCondition();
        }

        locker.mWaitingFor = this;
        long nanosEnd = nanosTimeout < 0 ? 0 : (System.nanoTime() + nanosTimeout);

        while (true) {
            int w = queueU.await(latch, nanosTimeout, nanosEnd);
            queueU = mQueueU;

            if (queueU == null) {
                locker.mWaitingFor = null;
                return LockResult.INTERRUPTED;
            }

            if (queueU.isEmpty()) {
                mQueueU = null;
            }

            if (w < 1) {
                if (w == 0) {
                    return LockResult.TIMED_OUT_LOCK;
                } else {
                    locker.mWaitingFor = null;
                    return LockResult.INTERRUPTED;
                }
            }

            if (mOwner == locker) {
                locker.mWaitingFor = null;
                return mLockCount == ~0 ? LockResult.OWNED_EXCLUSIVE : LockResult.OWNED_UPGRADABLE;
            }

            count = mLockCount;
            if (count != 0 && isSharedLockOwner(locker)) {
                if (!locker.canAttemptUpgrade(count)) {
                    if (queueU != null) {
                        queueU.signal();
                    }
                    locker.mWaitingFor = null;
                    return LockResult.ILLEGAL;
                }
                if (count > 0) {
                    mLockCount = (count - 1) | 0x80000000;
                    mOwner = locker;
                    return LockResult.OWNED_UPGRADABLE;
                }
            }

            if (count >= 0) {
                mLockCount = count | 0x80000000;
                mOwner = locker;
                locker.mWaitingFor = null;
                return LockResult.ACQUIRED;
            }

            if (nanosTimeout >= 0 && (nanosTimeout = nanosEnd - System.nanoTime()) <= 0) {
                return LockResult.TIMED_OUT_LOCK;
            }
        }
    }

    LockResult tryLockExclusive(Latch latch, Locker locker, long nanosTimeout) {
        final LockResult ur = tryLockUpgradable(latch, locker, nanosTimeout);
        if (!ur.isHeld() || ur == LockResult.OWNED_EXCLUSIVE) {
            return ur;
        }

        LatchCondition queueSX = mQueueSX;
        if (queueSX != null) {
            if (nanosTimeout == 0) {
                if (ur == LockResult.ACQUIRED) {
                    unlockUpgradable();
                }
                locker.mWaitingFor = this;
                return LockResult.TIMED_OUT_LOCK;
            }
        } else {
            if (mLockCount == 0x80000000) {
                mLockCount = ~0;
                return ur == LockResult.OWNED_UPGRADABLE ? LockResult.UPGRADED : LockResult.ACQUIRED;
            }
            if (nanosTimeout == 0) {
                if (ur == LockResult.ACQUIRED) {
                    unlockUpgradable();
                }
                locker.mWaitingFor = this;
                return LockResult.TIMED_OUT_LOCK;
            }
            mQueueSX = queueSX = new LatchCondition();
        }

        locker.mWaitingFor = this;
        long nanosEnd = nanosTimeout < 0 ? 0 : (System.nanoTime() + nanosTimeout);

        while (true) {
            int w = queueSX.await(latch, nanosTimeout, nanosEnd);
            queueSX = mQueueSX;

            if (queueSX == null) {
                locker.mWaitingFor = null;
                return LockResult.INTERRUPTED;
            }

            if (queueSX.isEmpty()) {
                mQueueSX = null;
            }

            if (w < 1) {
                if (ur == LockResult.ACQUIRED) {
                    unlockUpgradable();
                }
                if (w == 0) {
                    return LockResult.TIMED_OUT_LOCK;
                } else {
                    locker.mWaitingFor = null;
                    return LockResult.INTERRUPTED;
                }
            }

            acquired:
            {
                int count = mLockCount;
                if (count == 0x80000000) {
                    mLockCount = ~0;
                } else if (count != ~0) {
                    break acquired;
                }
                locker.mWaitingFor = null;
                return ur == LockResult.OWNED_UPGRADABLE ? LockResult.UPGRADED : LockResult.ACQUIRED;
            }

            if (nanosTimeout >= 0 && (nanosTimeout = nanosEnd - System.nanoTime()) <= 0) {
                return LockResult.TIMED_OUT_LOCK;
            }
        }
    }

    private void unlockUpgradable() {
        mOwner = null;
        LatchCondition queueU = mQueueU;
        if (queueU != null) {
            queueU.signal();
        }
        mLockCount &= 0x7fffffff;
    }

    void unlock(LockOwner locker, LockManager.LockHT ht) {
        if (mOwner == locker) {
            deleteGhost(ht);

            mOwner = null;
            LatchCondition queueU = mQueueU;
            int count = mLockCount;

            if (count != ~0) {
                if ((mLockCount = count & 0x7fffffff) == 0 && queueU == null && mQueueSX == null) {
                    ht.remove(this);
                } else if (queueU != null) {
                    if (queueU.signalRelease(ht)) {
                        return;
                    }
                }
            } else {
                mLockCount = 0;
                LatchCondition queueSX = mQueueSX;
                if (queueSX == null) {
                    if (queueU == null) {
                        ht.remove(this);
                    } else {
                        if (queueU.signalRelease(ht)) {
                            return;
                        }
                    }
                } else {
                    if (queueU != null) {
                        queueU.signal();
                    }
                    if (queueSX.signalRelease(ht)) {
                        return;
                    }
                }
            }
        } else {
            int count = mLockCount;

            unlock:
            {
                if ((count & 0x7fffffff) != 0) {
                    Object sharedObj = mSharedLockOwnersObj;
                    if (sharedObj == locker) {
                        mSharedLockOwnersObj = null;
                        break unlock;
                    } else if (sharedObj instanceof LockOwnerHTEntry[]) {
                        LockOwnerHTEntry[] entries = (LockOwnerHTEntry[]) sharedObj;
                        if (lockerHTremove(entries, locker)) {
                            if (count == 2) {
                                mSharedLockOwnersObj = lockerHTgetOne(entries);
                            }
                            break unlock;
                        }
                    }
                }

                if (isClosed(locker)) {
                    ht.releaseExclusive();
                    return;
                }

                throw new IllegalStateException("Lock not held");
            }

            mLockCount = --count;

            LatchCondition queueSX = mQueueSX;
            if (count == 0x80000000) {
                if (queueSX != null) {
                    if (queueSX.signalRelease(ht)) {
                        return;
                    }
                }
            } else if (count == 0 && queueSX == null && mQueueU == null) {
                ht.remove(this);
            }
        }

        ht.releaseExclusive();
    }

    void unlockToShared(LockOwner locker, Latch latch) {
        if (mOwner == locker) {
            deleteGhost(latch);

            mOwner = null;
            LatchCondition queueU = mQueueU;
            int count = mLockCount;

            if (count != ~0) {
                if ((count &= 0x7fffffff) >= 0x7ffffffe) {
                    throw new IllegalStateException("Too many shared locks held");
                }
                addSharedLockOwner(count, locker);
            } else {
                addSharedLockOwner(0, locker);
                LatchCondition queueSX = mQueueSX;
                if (queueSX != null) {
                    if (queueU != null) {
                        queueU.signal();
                    }
                    if (!queueSX.signalRelease(latch)) {
                        latch.releaseExclusive();
                    }
                    return;
                }
            }

            if (queueU != null && queueU.signalRelease(latch)) {
                return;
            }
        } else if ((mLockCount == 0 || !isSharedLockOwner(locker)) && !isClosed(locker)) {
            throw new IllegalStateException("Lock not held");
        }

        latch.releaseExclusive();
    }

    void unlockToUpgradable(LockOwner locker, Latch latch) {
        if (mOwner != locker) {
            if (isClosed(locker)) {
                latch.releaseExclusive();
                return;
            }
            String message = "Exclusive or upgradable lock not held";
            if (mLockCount == 0 || !isSharedLockOwner(locker)) {
                message = "Lock not held";
            }
            throw new IllegalStateException(message);
        }
        if (mLockCount != ~0) {
            latch.releaseExclusive();
            return;
        }
        deleteGhost(latch);
        mLockCount = 0x80000000;
        LatchCondition queueSX = mQueueSX;
        if (queueSX == null || !queueSX.signalSharedRelease(latch)) {
            latch.releaseExclusive();
        }
    }

    private static boolean isClosed(LockOwner locker) {
        Database db = locker.getDatabase();
        return db != null && db.isClosed();
    }

    void deleteGhost(Latch latch) {
        // TODO: 可以优化由于回滚而导致的解锁。 实际上不需要删除
        Object obj = mSharedLockOwnersObj;
        if (!(obj instanceof GhostFrame)) {
            return;
        }

        final GhostFrame frame = (GhostFrame) obj;
        mSharedLockOwnersObj = null;

        final Database db = mOwner.getDatabase();
        if (db == null) {
            // Database was closed.
            return;
        }

        frame.action(db, latch, this);
    }

    PendingTxn transferExclusive(LockOwner locker, LockManager.LockHT ht, PendingTxn pending) {
        if (mLockCount == ~0) {
            // Held exclusively. Must double check expected owner because Locker tracks Lock
            // instance multiple times for handling upgrades. Without this check, Lock can be
            // added to pending set multiple times.
            if (mOwner == locker) {
                if (pending == null) {
                    pending = new PendingTxn(this);
                } else {
                    pending.add(this);
                }
                mOwner = pending;
            }
            ht.releaseExclusive();
        } else {
            // Unlock upgradable or shared lock. Note that ht is passed along, to allow the
            // latch to be released. This also permits it to delete a ghost, but this shouldn't
            // be possible. An exclusive lock would have been held and detected above.
            unlock(locker, ht);
        }
        return pending;
    }

    boolean matches(long indexId, byte[] key, int hash) {
        return mHashCode == hash && mIndexId == indexId && Arrays.equals(mKey, key);
    }

    /**
     * Must hold exclusive lock to be valid.
     */
    void setGhostFrame(GhostFrame frame) {
        mSharedLockOwnersObj = frame;
    }

    void setSharedLockOwner(LockOwner owner) {
        mSharedLockOwnersObj = owner;
    }

    /**
     * Is null, a LockOwner, a LockOwnerHTEntry[], or a GhostFrame.
     */
    Object getSharedLockOwner() {
        return mSharedLockOwnersObj;
    }

    /**
     * @param lockType TYPE_SHARED, TYPE_UPGRADABLE, or TYPE_EXCLUSIVE
     */
    void detectDeadlock(Locker locker, int lockType, long nanosTimeout)
            throws DeadlockException {
        DeadlockDetector detector = new DeadlockDetector(locker);
        if (detector.scan()) {
            Object att = findOwnerAttachment(locker, lockType);
            throw new DeadlockException(nanosTimeout, att,
                    detector.mGuilty,
                    detector.newDeadlockSet(lockType));
        }
    }

    /**
     * Find an exclusive owner attachment, or the first found shared owner attachment. Might
     * acquire and release a shared latch to access the shared owner attachment.
     *
     * @param locker   pass null if already latched
     * @param lockType TYPE_SHARED, TYPE_UPGRADABLE, or TYPE_EXCLUSIVE
     */
    Object findOwnerAttachment(Locker locker, int lockType) {
        // See note in DeadlockDetector regarding unlatched access to this Lock.

        LockOwner owner = mOwner;
        if (owner != null) {
            Object att = owner.attachment();
            if (att != null) {
                return att;
            }
        }

        if (lockType != LockManager.TYPE_EXCLUSIVE) {
            // Only an exclusive lock request can be blocked by shared locks.
            return null;
        }

        Object sharedObj = mSharedLockOwnersObj;
        if (sharedObj == null) {
            return null;
        }

        if (sharedObj instanceof LockOwner) {
            return ((LockOwner) sharedObj).attachment();
        }

        if (sharedObj instanceof LockOwnerHTEntry[]) {
            if (locker != null) {
                // Need a latch to safely check the shared lock owner hashtable.
                LockManager manager = locker.mManager;
                if (manager != null) {
                    LockManager.LockHT ht = manager.getLockHT(mHashCode);
                    ht.acquireShared();
                    try {
                        return findOwnerAttachment(null, lockType);
                    } finally {
                        ht.releaseShared();
                    }
                }
            } else {
                LockOwnerHTEntry[] entries = (LockOwnerHTEntry[]) sharedObj;

                for (int i = entries.length; --i >= 0; ) {
                    for (LockOwnerHTEntry e = entries[i]; e != null; e = e.mNext) {
                        owner = e.mOwner;
                        if (owner != null) {
                            Object att = owner.attachment();
                            if (att != null) {
                                return att;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean isSharedLockOwner(LockOwner locker) {
        Object sharedObj = mSharedLockOwnersObj;
        if (sharedObj == locker) {
            return true;
        }
        if (sharedObj instanceof LockOwnerHTEntry[]) {
            return lockerHTcontains((LockOwnerHTEntry[]) sharedObj, locker);
        }
        return false;
    }

    /**
     * @return ACQUIRED, OWNED_SHARED, or null
     */
    private LockResult tryLockShared(LockOwner locker) {
        int count = mLockCount;
        if (count == ~0) {
            return null;
        }
        if (count != 0 && isSharedLockOwner(locker)) {
            return LockResult.OWNED_SHARED;
        }
        if ((count & 0x7fffffff) >= 0x7ffffffe) {
            throw new IllegalStateException("Too many shared locks held");
        }
        addSharedLockOwner(count, locker);
        return LockResult.ACQUIRED;
    }

    private void addSharedLockOwner(int count, LockOwner locker) {
        count++;
        Object sharedObj = mSharedLockOwnersObj;
        if (sharedObj == null) {
            mSharedLockOwnersObj = locker;
        } else if (sharedObj instanceof LockOwnerHTEntry[]) {
            LockOwnerHTEntry[] entries = (LockOwnerHTEntry[]) sharedObj;
            lockerHTadd(entries, count & 0x7fffffff, locker);
        } else {
            // Initial capacity of must be a power of 2.
            LockOwnerHTEntry[] entries = new LockOwnerHTEntry[8];
            lockerHTadd(entries, (LockOwner) sharedObj);
            lockerHTadd(entries, locker);
            mSharedLockOwnersObj = entries;
        }
        mLockCount = count;
    }

    private static boolean lockerHTcontains(LockOwnerHTEntry[] entries, LockOwner locker) {
        int hash = locker.hashCode();
        for (LockOwnerHTEntry e = entries[hash & (entries.length - 1)]; e != null; e = e.mNext) {
            if (e.mOwner == locker) {
                return true;
            }
        }
        return false;
    }

    private void lockerHTadd(LockOwnerHTEntry[] entries, int newSize, LockOwner locker) {
        if (newSize > (entries.length >> 1)) {
            int capacity = entries.length << 1;
            LockOwnerHTEntry[] newEntries = new LockOwnerHTEntry[capacity];
            int newMask = capacity - 1;

            for (int i = entries.length; --i >= 0; ) {
                for (LockOwnerHTEntry e = entries[i]; e != null; ) {
                    LockOwnerHTEntry next = e.mNext;
                    int ix = e.mOwner.hashCode() & newMask;
                    e.mNext = newEntries[ix];
                    newEntries[ix] = e;
                    e = next;
                }
            }

            mSharedLockOwnersObj = entries = newEntries;
        }

        lockerHTadd(entries, locker);
    }

    private static void lockerHTadd(LockOwnerHTEntry[] entries, LockOwner locker) {
        int index = locker.hashCode() & (entries.length - 1);
        LockOwnerHTEntry e = new LockOwnerHTEntry();
        e.mOwner = locker;
        e.mNext = entries[index];
        entries[index] = e;
    }

    private static boolean lockerHTremove(LockOwnerHTEntry[] entries, LockOwner locker) {
        int index = locker.hashCode() & (entries.length - 1);
        for (LockOwnerHTEntry e = entries[index], prev = null; e != null; e = e.mNext) {
            if (e.mOwner == locker) {
                if (prev == null) {
                    entries[index] = e.mNext;
                } else {
                    prev.mNext = e.mNext;
                }
                return true;
            } else {
                prev = e;
            }
        }
        return false;
    }

    private static LockOwner lockerHTgetOne(LockOwnerHTEntry[] entries) {
        for (LockOwnerHTEntry e : entries) {
            if (e != null) {
                return e.mOwner;
            }
        }
        throw new AssertionError("No lockers in hashtable");
    }

    /**
     * Entry for simple hashtable of LockOwners.
     */
    static final class LockOwnerHTEntry {
        LockOwner mOwner;
        LockOwnerHTEntry mNext;
    }
}
