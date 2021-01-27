package com.glodon.linglong.engine.lock;


import com.glodon.linglong.base.exception.*;
import com.glodon.linglong.engine.core.Database;

import java.lang.ref.WeakReference;

import static com.glodon.linglong.engine.lock.LockManager.*;

/**
 * @author Stereo
 */
class Locker extends LockOwner {
    final LockManager mManager;
    ParentScope mParentScope;
    Object mTailBlock;

    Locker(LockManager manager) {
        mManager = manager;
    }

    private LockManager manager() {
        LockManager manager = mManager;
        if (manager == null) {
            throw new IllegalStateException("Transaction is bogus");
        }
        return manager;
    }

    @Override
    public final Database getDatabase() {
        LockManager manager = mManager;
        if (manager != null) {
            WeakReference<Database> ref = manager.mDatabaseRef;
            if (ref != null) {
                return ref.get();
            }
        }
        return null;
    }

    @Override
    public void attach(Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object attachment() {
        return null;
    }

    public final boolean isNested() {
        return mParentScope != null;
    }

    public final int nestingLevel() {
        int count = 0;
        ParentScope parent = mParentScope;
        while (parent != null) {
            count++;
            parent = parent.mParentScope;
        }
        return count;
    }

    final LockResult tryLock(int lockType, long indexId, byte[] key, int hash, long nanosTimeout)
            throws DeadlockException {
        LockResult result = manager().getLockHT(hash)
                .tryLock(lockType, this, indexId, key, hash, nanosTimeout);

        if (result == LockResult.TIMED_OUT_LOCK) {
            Lock waitingFor = mWaitingFor;
            if (waitingFor != null) {
                try {
                    if (nanosTimeout != 0) {
                        waitingFor.detectDeadlock(this, lockType, nanosTimeout);
                    }
                } finally {
                    mWaitingFor = null;
                }
            }
        }

        return result;
    }

    final LockResult lock(int lockType, long indexId, byte[] key, int hash, long nanosTimeout)
            throws LockFailureException {
        LockResult result = manager().getLockHT(hash)
                .tryLock(lockType, this, indexId, key, hash, nanosTimeout);
        if (result.isHeld()) {
            return result;
        }
        throw failed(lockType, result, nanosTimeout);
    }

    @SuppressWarnings("incomplete-switch")
    final LockResult lockNT(int lockType, long indexId, byte[] key, int hash, long nanosTimeout)
            throws LockFailureException {
        LockResult result = manager().getLockHT(hash)
                .tryLock(lockType, this, indexId, key, hash, nanosTimeout);
        if (!result.isHeld()) {
            switch (result) {
                case ILLEGAL:
                    throw new IllegalUpgradeException();
                case INTERRUPTED:
                    throw new LockInterruptedException();
            }
        }
        return result;
    }

    public final LockResult tryLockShared(long indexId, byte[] key, long nanosTimeout)
            throws DeadlockException {
        return tryLock(TYPE_SHARED, indexId, key, hash(indexId, key), nanosTimeout);
    }

    final LockResult tryLockShared(long indexId, byte[] key, int hash, long nanosTimeout)
            throws DeadlockException {
        return tryLock(TYPE_SHARED, indexId, key, hash, nanosTimeout);
    }

    public final LockResult lockShared(long indexId, byte[] key, long nanosTimeout)
            throws LockFailureException {
        return lock(TYPE_SHARED, indexId, key, hash(indexId, key), nanosTimeout);
    }

    final LockResult lockShared(long indexId, byte[] key, int hash, long nanosTimeout)
            throws LockFailureException {
        return lock(TYPE_SHARED, indexId, key, hash, nanosTimeout);
    }

    final LockResult lockSharedNT(long indexId, byte[] key, int hash, long nanosTimeout)
            throws LockFailureException {
        return lockNT(TYPE_SHARED, indexId, key, hash, nanosTimeout);
    }

    public final LockResult tryLockUpgradable(long indexId, byte[] key, long nanosTimeout)
            throws DeadlockException {
        return tryLock(TYPE_UPGRADABLE, indexId, key, hash(indexId, key), nanosTimeout);
    }

    final LockResult tryLockUpgradable(long indexId, byte[] key, int hash, long nanosTimeout)
            throws DeadlockException {
        return tryLock(TYPE_UPGRADABLE, indexId, key, hash, nanosTimeout);
    }

    public final LockResult lockUpgradable(long indexId, byte[] key, long nanosTimeout)
            throws LockFailureException {
        return lock(TYPE_UPGRADABLE, indexId, key, hash(indexId, key), nanosTimeout);
    }

    final LockResult lockUpgradable(long indexId, byte[] key, int hash, long nanosTimeout)
            throws LockFailureException {
        return lock(TYPE_UPGRADABLE, indexId, key, hash, nanosTimeout);
    }

    final LockResult lockUpgradableNT(long indexId, byte[] key, int hash, long nanosTimeout)
            throws LockFailureException {
        return lockNT(TYPE_UPGRADABLE, indexId, key, hash, nanosTimeout);
    }

    public final LockResult tryLockExclusive(long indexId, byte[] key, long nanosTimeout)
            throws DeadlockException {
        return tryLock(TYPE_EXCLUSIVE, indexId, key, hash(indexId, key), nanosTimeout);
    }

    final LockResult tryLockExclusive(long indexId, byte[] key, int hash, long nanosTimeout)
            throws DeadlockException {
        return tryLock(TYPE_EXCLUSIVE, indexId, key, hash, nanosTimeout);
    }

    public final LockResult lockExclusive(long indexId, byte[] key, long nanosTimeout)
            throws LockFailureException {
        return lock(TYPE_EXCLUSIVE, indexId, key, hash(indexId, key), nanosTimeout);
    }

    final LockResult lockExclusive(long indexId, byte[] key, int hash, long nanosTimeout)
            throws LockFailureException {
        return lock(TYPE_EXCLUSIVE, indexId, key, hash, nanosTimeout);
    }

    final LockResult lockExclusive(Lock lock, long nanosTimeout) throws LockFailureException {
        LockResult result = mManager.getLockHT(lock.mHashCode)
                .tryLockExclusive(this, lock, nanosTimeout);
        if (result.isHeld()) {
            return result;
        }
        throw failed(TYPE_EXCLUSIVE, result, nanosTimeout);
    }

    final LockResult lockExclusiveNT(long indexId, byte[] key, int hash, long nanosTimeout)
            throws LockFailureException {
        return lockNT(TYPE_EXCLUSIVE, indexId, key, hash, nanosTimeout);
    }

    final boolean canAttemptUpgrade(int count) {
        LockUpgradeRule lockUpgradeRule = mManager.mDefaultLockUpgradeRule;
        return lockUpgradeRule == LockUpgradeRule.UNCHECKED
                | (lockUpgradeRule == LockUpgradeRule.LENIENT & count == 1);
    }

    final Lock lockSharedNoPush(long indexId, byte[] key) throws LockFailureException {
        int hash = hash(indexId, key);
        LockManager.LockHT ht = mManager.getLockHT(hash);

        Lock lock;
        LockResult result;

        ht.acquireExclusive();
        try {
            lock = ht.lockAccess(indexId, key, hash);
            result = lock.tryLockShared(ht, this, -1);
        } finally {
            ht.releaseExclusive();
        }

        if (!result.isHeld()) {
            throw failed(TYPE_SHARED, result, -1);
        }

        return result == LockResult.ACQUIRED ? lock : null;
    }

    final Lock lockUpgradableNoPush(long indexId, byte[] key) throws LockFailureException {
        int hash = hash(indexId, key);
        LockManager.LockHT ht = mManager.getLockHT(hash);

        Lock lock;
        LockResult result;

        ht.acquireExclusive();
        try {
            lock = ht.lockAccess(indexId, key, hash);
            result = lock.tryLockUpgradable(ht, this, -1);
        } finally {
            ht.releaseExclusive();
        }

        if (!result.isHeld()) {
            throw failed(TYPE_UPGRADABLE, result, -1);
        }

        return result == LockResult.ACQUIRED ? lock : null;
    }

    LockFailureException failed(int lockType, LockResult result, long nanosTimeout)
            throws DeadlockException {
        Lock waitingFor;

        switch (result) {
            case TIMED_OUT_LOCK:
                waitingFor = mWaitingFor;
                if (waitingFor != null) {
                    try {
                        waitingFor.detectDeadlock(this, lockType, nanosTimeout);
                    } finally {
                        mWaitingFor = null;
                    }
                }
                break;
            case ILLEGAL:
                return new IllegalUpgradeException();
            case INTERRUPTED:
                return new LockInterruptedException();
            default:
                waitingFor = mWaitingFor;
                mWaitingFor = null;
        }

        if (result.isTimedOut()) {
            Object att = waitingFor == null ? null
                    : waitingFor.findOwnerAttachment(this, lockType);
            return new LockTimeoutException(nanosTimeout, att);
        }

        return new LockFailureException();
    }

    public final LockResult lockCheck(long indexId, byte[] key) {
        return manager().check(this, indexId, key, hash(indexId, key));
    }

    public final long lastLockedIndex() {
        return peek().mIndexId;
    }

    public final byte[] lastLockedKey() {
        return peek().mKey;
    }

    private Lock peek() {
        Object tailObj = mTailBlock;
        if (tailObj == null) {
            throw new IllegalStateException("No locks held");
        }
        return (tailObj instanceof Lock) ? ((Lock) tailObj) : (((Block) tailObj).last());
    }

    public final void unlock() {
        Object tailObj = mTailBlock;
        if (tailObj == null) {
            throw new IllegalStateException("No locks held");
        }
        if (tailObj instanceof Lock) {
            ParentScope parent = mParentScope;
            if (parent != null && parent.mTailBlock == tailObj) {
                throw new IllegalStateException("Cannot cross a scope boundary");
            }
            mTailBlock = null;
            mManager.unlock(this, (Lock) tailObj);
        } else {
            Block.unlockLast((Block) tailObj, this);
        }
    }

    public final void unlockToShared() {
        Object tailObj = mTailBlock;
        if (tailObj == null) {
            throw new IllegalStateException("No locks held");
        }
        if (tailObj instanceof Lock) {
            ParentScope parent = mParentScope;
            if (parent != null && parent.mTailBlock == tailObj) {
                throw new IllegalStateException("Cannot cross a scope boundary");
            }
            mManager.unlockToShared(this, (Lock) tailObj);
        } else {
            Block.unlockLastToShared((Block) tailObj, this);
        }
    }

    public final void unlockToUpgradable() {
        Object tailObj = mTailBlock;
        if (tailObj == null) {
            throw new IllegalStateException("No locks held");
        }
        if (tailObj instanceof Lock) {
            ParentScope parent = mParentScope;
            if (parent != null && parent.mTailBlock == tailObj) {
                throw new IllegalStateException("Cannot cross a scope boundary");
            }
            mManager.unlockToUpgradable(this, (Lock) tailObj);
        } else {
            Block.unlockLastToUpgradable((Block) tailObj, this);
        }
    }

    public final void unlockCombine() {
        Object tailObj = mTailBlock;
        if (tailObj == null) {
            throw new IllegalStateException("No locks held");
        }
        if (tailObj instanceof Lock) {
            ParentScope parent = mParentScope;
            if (parent != null && parent.mTailBlock == tailObj) {
                throw new IllegalStateException("Cannot cross a scope boundary");
            }
        } else {
            Block.unlockCombine((Block) tailObj, this);
        }
    }

    final ParentScope scopeEnter() {
        ParentScope parent = new ParentScope();
        parent.mParentScope = mParentScope;
        Object tailObj = mTailBlock;
        parent.mTailBlock = tailObj;
        if (tailObj instanceof Block) {
            parent.mTailBlockSize = ((Block) tailObj).mSize;
        }
        mParentScope = parent;
        return parent;
    }

    final void promote() {
        Object tailObj = mTailBlock;
        if (tailObj != null) {
            ParentScope parent = mParentScope;
            parent.mTailBlock = tailObj;
            if (tailObj instanceof Block) {
                parent.mTailBlockSize = ((Block) tailObj).mSize;
            }
        }
    }

    final void scopeUnlockAll() {
        ParentScope parent = mParentScope;
        Object parentTailObj;
        if (parent == null || (parentTailObj = parent.mTailBlock) == null) {
            Object tailObj = mTailBlock;
            if (tailObj instanceof Lock) {
                mManager.unlock(this, (Lock) tailObj);
                mTailBlock = null;
            } else {
                Block tail = (Block) tailObj;
                if (tail != null) {
                    do {
                        tail.unlockToSavepoint(this, 0);
                        tail = tail.pop();
                    } while (tail != null);
                    mTailBlock = null;
                }
            }
        } else if (parentTailObj instanceof Lock) {
            Object tailObj = mTailBlock;
            if (tailObj instanceof Block) {
                Block tail = (Block) tailObj;
                while (true) {
                    Block prev = tail.peek();
                    if (prev == null) {
                        tail.unlockToSavepoint(this, 1);
                        break;
                    }
                    tail.unlockToSavepoint(this, 0);
                    tail.discard();
                    tail = prev;
                }
                mTailBlock = tail;
            }
        } else {
            Block tail = (Block) mTailBlock;
            while (tail != parentTailObj) {
                tail.unlockToSavepoint(this, 0);
                tail = tail.pop();
            }
            tail.unlockToSavepoint(this, parent.mTailBlockSize);
            mTailBlock = tail;
        }
    }

    final PendingTxn transferExclusive() {
        PendingTxn pending;

        Object tailObj = mTailBlock;
        if (tailObj instanceof Lock) {
            pending = mManager.transferExclusive(this, (Lock) tailObj, null);
        } else if (tailObj == null) {
            pending = new PendingTxn(null);
        } else {
            pending = null;
            Block tail = (Block) tailObj;
            do {
                pending = tail.transferExclusive(this, pending);
                tail = tail.pop();
            } while (tail != null);
        }

        mTailBlock = null;

        return pending;
    }

    final ParentScope scopeExit() {
        scopeUnlockAll();
        return popScope();
    }

    final void scopeExitAll() {
        mParentScope = null;
        scopeUnlockAll();
        mTailBlock = null;
    }

    final void discardAllLocks() {
        mParentScope = null;
        mTailBlock = null;
    }

    final void push(Lock lock) {
        Object tailObj = mTailBlock;
        if (tailObj == null) {
            mTailBlock = lock;
        } else if (tailObj instanceof Lock) {
            mTailBlock = new Block((Lock) tailObj, lock);
        } else {
            ((Block) tailObj).pushLock(this, lock, 0);
        }
    }

    final void pushUpgrade(Lock lock) {
        Object tailObj = mTailBlock;
        if (tailObj == null) {
            Block block = new Block(lock);
            block.firstUpgrade();
            mTailBlock = block;
        } else if (tailObj instanceof Lock) {
            if (tailObj != lock || mParentScope != null) {
                Block block = new Block((Lock) tailObj, lock);
                block.secondUpgrade();
                mTailBlock = block;
            }
        } else {
            ((Block) tailObj).pushLock(this, lock, 1L << 63);
        }
    }

    private ParentScope popScope() {
        ParentScope parent = mParentScope;
        if (parent == null) {
            mTailBlock = null;
        } else {
            mTailBlock = parent.mTailBlock;
            mParentScope = parent.mParentScope;
        }
        return parent;
    }

    static final class Block {
        private static final int FIRST_BLOCK_CAPACITY = 8;
        private static final int HIGHEST_BLOCK_CAPACITY = 64;

        private Lock[] mLocks;
        private long mUpgrades;
        int mSize;
        private long mUnlockGroup;

        private Block mPrev;

        Block(Lock first) {
            (mLocks = new Lock[FIRST_BLOCK_CAPACITY])[0] = first;
            mSize = 1;
        }

        Block(Lock first, Lock second) {
            Lock[] locks = new Lock[FIRST_BLOCK_CAPACITY];
            locks[0] = first;
            locks[1] = second;
            mLocks = locks;
            mSize = 2;
        }

        void firstUpgrade() {
            mUpgrades = 1L << 63;
        }

        void secondUpgrade() {
            mUpgrades = 1L << 62;
        }

        private Block(Block prev, Lock first, long upgrade) {
            mPrev = prev;
            int capacity = prev.mLocks.length;
            if (capacity < FIRST_BLOCK_CAPACITY) {
                capacity = FIRST_BLOCK_CAPACITY;
            } else if (capacity < HIGHEST_BLOCK_CAPACITY) {
                capacity <<= 1;
            }
            (mLocks = new Lock[capacity])[0] = first;
            mUpgrades = upgrade;
            mSize = 1;
        }

        void pushLock(Locker locker, Lock lock, long upgrade) {
            Lock[] locks = mLocks;
            int size = mSize;

            ParentScope parent;
            if (upgrade != 0
                    && ((parent = locker.mParentScope) == null || parent.mTailBlockSize != size)
                    && locks[size - 1] == lock) {
                return;
            }

            if (size < locks.length) {
                locks[size] = lock;
                mUpgrades |= upgrade >>> size;
                mSize = size + 1;
            } else {
                locker.mTailBlock = new Block(this, lock, upgrade);
            }
        }

        Lock last() {
            return mLocks[mSize - 1];
        }

        static void unlockLast(Block block, Locker locker) {
            int size = block.mSize;
            while (true) {
                size--;

                long upgrades = block.mUpgrades;
                long mask = (1L << 63) >>> size;
                if ((upgrades & mask) != 0) {
                    throw new IllegalStateException("Cannot unlock non-immediate upgrade");
                }

                Lock[] locks = block.mLocks;
                Lock lock = locks[size];
                block.parentCheck(locker, lock);

                locker.mManager.unlock(locker, lock);

                locks[size] = null;

                if (size == 0) {
                    Block prev = block.mPrev;
                    locker.mTailBlock = prev;
                    block.mPrev = null;
                    if ((block.mUnlockGroup & mask) == 0) {
                        return;
                    }
                    block = prev;
                    size = block.mSize;
                } else {
                    block.mUpgrades = upgrades & ~mask;
                    block.mSize = size;
                    long unlockGroup = block.mUnlockGroup;
                    if ((unlockGroup & mask) == 0) {
                        return;
                    }
                    block.mUnlockGroup = unlockGroup & ~mask;
                }
            }
        }

        static void unlockLastToShared(Block block, Locker locker) {
            int size = block.mSize;
            while (true) {
                size--;

                long mask = (1L << 63) >>> size;
                if ((block.mUpgrades & mask) != 0) {
                    throw new IllegalStateException("Cannot unlock non-immediate upgrade");
                }

                Lock lock = block.mLocks[size];
                block.parentCheck(locker, lock);

                locker.mManager.unlockToShared(locker, lock);

                if ((block.mUnlockGroup & mask) == 0) {
                    return;
                }

                if (size == 0) {
                    block = block.mPrev;
                    size = block.mSize;
                }
            }
        }

        static void unlockLastToUpgradable(Block block, Locker locker) {
            int size = block.mSize;
            while (true) {
                size--;

                Lock[] locks = block.mLocks;
                Lock lock = locks[size];
                block.parentCheck(locker, lock);

                locker.mManager.unlockToUpgradable(locker, lock);

                long upgrades = block.mUpgrades;
                long mask = (1L << 63) >>> size;

                if ((upgrades & mask) == 0) {
                    if ((block.mUnlockGroup & mask) == 0) {
                        return;
                    }
                    if (size == 0) {
                        block = block.mPrev;
                        size = block.mSize;
                    }
                } else {
                    locks[size] = null;

                    if (size == 0) {
                        Block prev = block.mPrev;
                        locker.mTailBlock = prev;
                        block.mPrev = null;
                        if ((block.mUnlockGroup & mask) == 0) {
                            return;
                        }
                        block = prev;
                        size = block.mSize;
                    } else {
                        block.mUpgrades = upgrades & ~mask;
                        block.mSize = size;
                        long unlockGroup = block.mUnlockGroup;
                        if ((unlockGroup & mask) == 0) {
                            return;
                        }
                        block.mUnlockGroup = unlockGroup & ~mask;
                    }
                }
            }
        }

        static void unlockCombine(Block block, Locker locker) {
            while (true) {
                int size = block.mSize - 1;

                long mask = block.mUnlockGroup | (~(1L << 63) >>> size);

                mask = ~mask & (mask + 1);

                if (mask == 0) {
                    block = block.mPrev;
                    continue;
                }

                long upgrades = block.mUpgrades;

                long prevMask;
                if (size != 0) {
                    prevMask = upgrades >> 1;
                } else {
                    Block prev = block.mPrev;
                    if (prev == null) {
                        return;
                    }
                    prevMask = prev.mUpgrades << (prev.mSize - 1);
                }

                if (((upgrades ^ prevMask) & mask) != 0) {
                    throw new IllegalStateException("Cannot combine an acquire with an upgrade");
                }

                block.mUnlockGroup |= mask;
                return;
            }
        }

        private void parentCheck(Locker locker, Lock lock) throws IllegalStateException {
            ParentScope parent = locker.mParentScope;
            if (parent != null) {
                Object parentTail = parent.mTailBlock;
                if (parentTail == lock || (parentTail == this && parent.mTailBlockSize == mSize)) {
                    throw new IllegalStateException("Cannot cross a scope boundary");
                }
            }
        }

        void unlockToSavepoint(Locker locker, int targetSize) {
            int size = mSize;
            if (size > targetSize) {
                Lock[] locks = mLocks;
                LockManager manager = locker.mManager;
                size--;
                long mask = (1L << 63) >>> size;
                long upgrades = mUpgrades;
                while (true) {
                    Lock lock = locks[size];
                    if ((upgrades & mask) != 0) {
                        manager.unlockToUpgradable(locker, lock);
                    } else {
                        manager.unlock(locker, lock);
                    }
                    locks[size] = null;
                    if (size == targetSize) {
                        break;
                    }
                    size--;
                    mask <<= 1;
                }
                mUpgrades = upgrades & ~(~0L >>> size);
                mSize = size;
            }
        }

        PendingTxn transferExclusive(Locker locker, PendingTxn pending) {
            int size = mSize;
            if (size > 0) {
                Lock[] locks = mLocks;
                LockManager manager = locker.mManager;
                do {
                    Lock lock = locks[--size];
                    pending = manager.transferExclusive(locker, lock, pending);
                } while (size != 0);
            }
            return pending;
        }

        Block pop() {
            Block prev = mPrev;
            mPrev = null;
            return prev;
        }

        Block peek() {
            return mPrev;
        }

        void discard() {
            mPrev = null;
        }
    }

    static final class ParentScope {
        ParentScope mParentScope;
        Object mTailBlock;
        int mTailBlockSize;

        LockMode mLockMode;
        long mLockTimeoutNanos;
        int mHasState;
        long mSavepoint;
    }
}
