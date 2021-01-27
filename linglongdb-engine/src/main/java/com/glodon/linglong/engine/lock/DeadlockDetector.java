package com.glodon.linglong.engine.lock;

import com.glodon.linglong.engine.core.Index;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 由Locker内部使用。 仅检测由独立引起的死锁线程。
 * 线程“自我死锁”由相同的单独的储物柜引起未检测到线程。
 * 这是因为只有一个线程被阻塞。
 * 检测器依赖于要锁定的多个线程来等待锁定。
 * 储物柜未在任何特定线程中注册，因此锁定不能归线程所有。
 * 如果更改，则检测器可以看到线程是自我死锁的。
 *
 * @author Stereo
 */
final class DeadlockDetector {
    private final Locker mOrigin;
    private final Set<LockOwner> mLockers;
    final Set<Lock> mLocks;

    boolean mGuilty;

    DeadlockDetector(Locker locker) {
        mOrigin = locker;
        mLockers = new LinkedHashSet<>();
        mLocks = new LinkedHashSet<>();
    }

    DeadlockSet newDeadlockSet(int lockType) {
        DeadlockSet.OwnerInfo[] infoSet = new DeadlockSet.OwnerInfo[mLocks.size()];
        final LockManager manager = mOrigin.mManager;

        int i = 0;
        for (Lock lock : mLocks) {
            DeadlockSet.OwnerInfo info = new DeadlockSet.OwnerInfo();
            infoSet[i] = info;

            info.mIndexId = lock.mIndexId;

            Index ix = manager.indexById(info.mIndexId);
            if (ix != null) {
                info.mIndexName = ix.getName();
            }

            byte[] key = lock.mKey;
            if (key != null) {
                key = key.clone();
            }
            info.mKey = key;

            info.mAttachment = lock.findOwnerAttachment(mOrigin, lockType);

            i++;
        }

        return new DeadlockSet(infoSet);
    }

    boolean scan() {
        return scan(mOrigin);
    }

    private boolean scan(LockOwner locker) {
        boolean found = false;

        outer:
        while (true) {
            Lock lock = locker.mWaitingFor;
            if (lock == null) {
                return found;
            }

            mLocks.add(lock);

            if (mLockers.isEmpty()) {
                mLockers.add(locker);
            } else {
                mGuilty |= mOrigin == locker;
                if (!mLockers.add(locker)) {
                    return true;
                }
            }

            LockOwner owner = lock.mOwner;
            Object shared = lock.getSharedLockOwner();

            if (owner != null && owner != locker) {
                if (shared == null) {
                    locker = owner;
                    continue outer;
                }
                found |= scan(owner);
            }

            if (shared instanceof LockOwner) {
                locker = (LockOwner) shared;
                continue outer;
            }

            if (!(shared instanceof Lock.LockOwnerHTEntry[])) {
                return found;
            }

            Lock.LockOwnerHTEntry[] entries = (Lock.LockOwnerHTEntry[]) shared;
            for (int i = entries.length; --i >= 0; ) {
                for (Lock.LockOwnerHTEntry e = entries[i]; e != null; ) {
                    Lock.LockOwnerHTEntry next = e.mNext;
                    if (i == 0 && next == null) {
                        locker = e.mOwner;
                        continue outer;
                    }
                    found |= scan(e.mOwner);
                    e = next;
                }
            }

            return found;
        }
    }
}
