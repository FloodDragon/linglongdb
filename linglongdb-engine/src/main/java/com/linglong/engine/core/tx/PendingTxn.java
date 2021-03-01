package com.linglong.engine.core.tx;

import com.linglong.engine.core.LocalDatabase;
import com.linglong.engine.core.lock.Lock;
import com.linglong.engine.core.lock.LockManager;
import com.linglong.engine.core.lock.LockOwner;

import java.io.IOException;

/**
 * @author Stereo
 */
public final class PendingTxn extends LockOwner {
    private final Lock mFirst;
    private Lock[] mRest;
    private int mRestSize;

    TransactionContext mContext;
    long mTxnId;
    long mCommitPos;

    public long getCommitPos() {
        return mCommitPos;
    }

    UndoLog mUndoLog;
    int mHasState;
    private Object mAttachment;

    PendingTxn mPrev;

    public void setPrev(PendingTxn mPrev) {
        this.mPrev = mPrev;
    }

    public PendingTxn getPrev() {
        return mPrev;
    }

    public PendingTxn(Lock first) {
        mFirst = first;
    }

    @Override
    public final LocalDatabase getDatabase() {
        return mUndoLog.getDatabase();
    }

    @Override
    public void attach(Object obj) {
        mAttachment = obj;
    }

    @Override
    public Object attachment() {
        return mAttachment;
    }

    public void add(Lock lock) {
        Lock first = mFirst;
        if (first == null) {
            throw new IllegalStateException("cannot add lock");
        }
        Lock[] rest = mRest;
        if (rest == null) {
            rest = new Lock[8];
            mRest = rest;
            mRestSize = 1;
            rest[0] = lock;
        } else {
            int size = mRestSize;
            if (size >= rest.length) {
                Lock[] newRest = new Lock[rest.length << 1];
                System.arraycopy(rest, 0, newRest, 0, rest.length);
                mRest = rest = newRest;
            }
            rest[size] = lock;
            mRestSize = size + 1;
        }
    }

    public void commit(LocalDatabase db) throws IOException {
        // See Transaction.commit for more info.

        unlockAll(db);

        UndoLog undo = mUndoLog;
        if (undo != null) {
            undo.truncate(true);
            mContext.unregister(undo);
        }

        if ((mHasState & LocalTransaction.HAS_TRASH) != 0) {
            db.fragmentedTrash().emptyTrash(mTxnId);
        }
    }

    public void rollback(LocalDatabase db) throws IOException {
        UndoLog undo = mUndoLog;
        if (undo != null) {
            undo.rollback();
        }

        unlockAll(db);

        if (undo != null) {
            mContext.unregister(undo);
        }
    }

    private void unlockAll(LocalDatabase db) {
        Lock first = mFirst;
        if (first != null) {
            LockManager manager = db.mLockManager;
            manager.unlock(this, first);
            Lock[] rest = mRest;
            if (rest != null) {
                for (Lock lock : rest) {
                    if (lock == null) {
                        return;
                    }
                    manager.unlock(this, lock);
                }
            }
        }
    }
}
