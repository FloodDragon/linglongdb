package com.glodon.linglong.engine.core.tx;

import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.exception.DatabaseException;
import com.glodon.linglong.base.exception.LockFailureException;
import com.glodon.linglong.base.exception.UnmodifiableReplicaException;
import com.glodon.linglong.engine.config.DurabilityMode;
import com.glodon.linglong.engine.core.LocalDatabase;
import com.glodon.linglong.engine.core.TreeCursor;
import com.glodon.linglong.engine.core.TreeValue;
import com.glodon.linglong.engine.core.lock.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Stereo
 */
public final class LocalTransaction extends Locker implements Transaction {
    public static final LocalTransaction BOGUS = new LocalTransaction();

    static final int
            HAS_SCOPE = 1, // When set, scope has been entered but not logged.
            HAS_COMMIT = 2, // When set, transaction has committable changes.
            HAS_TRASH = 4, /* When set, fragmented values are in the trash and must be
                            fully deleted after committing the top-level scope. */
            HAS_PREPARE = 8; // When set, transaction is prepared for two-phase commit.

    final LocalDatabase mDatabase;
    final TransactionContext mContext;

    public TransactionContext getContext() {
        return mContext;
    }

    RedoWriter mRedo;

    public void setRedo(RedoWriter mRedo) {
        this.mRedo = mRedo;
    }

    public RedoWriter getRedo() {
        return mRedo;
    }

    DurabilityMode mDurabilityMode;

    public void setDurabilityMode(DurabilityMode mDurabilityMode) {
        this.mDurabilityMode = mDurabilityMode;
    }

    public DurabilityMode getDurabilityMode() {
        return mDurabilityMode;
    }

    LockMode mLockMode;

    public void setLockMode(LockMode mLockMode) {
        this.mLockMode = mLockMode;
    }

    public LockMode getLockMode() {
        return mLockMode;
    }

    long mLockTimeoutNanos;

    public void setLockTimeoutNanos(long mLockTimeoutNanos) {
        this.mLockTimeoutNanos = mLockTimeoutNanos;
    }

    public long getLockTimeoutNanos() {
        return mLockTimeoutNanos;
    }

    private int mHasState;
    private long mSavepoint;
    long mTxnId;

    private UndoLog mUndoLog;

    private Object mAttachment;

    private Object mBorked;

    public LocalTransaction(LocalDatabase db, RedoWriter redo, DurabilityMode durabilityMode,
                            LockMode lockMode, long timeoutNanos) {
        super(db.mLockManager);
        mDatabase = db;
        mContext = db.selectTransactionContext(this);
        mRedo = redo;
        mDurabilityMode = durabilityMode;
        mLockMode = lockMode;
        mLockTimeoutNanos = timeoutNanos;
    }

    public LocalTransaction(LocalDatabase db, long txnId, LockMode lockMode, long timeoutNanos) {
        this(db, null, DurabilityMode.NO_REDO, lockMode, timeoutNanos);
        mTxnId = txnId;
    }

    public LocalTransaction(LocalDatabase db, long txnId, LockMode lockMode, long timeoutNanos,
                            int hasState) {
        this(db, null, DurabilityMode.NO_REDO, lockMode, timeoutNanos);
        mTxnId = txnId;
        mHasState = hasState;
    }

    private LocalTransaction() {
        super(null);
        mDatabase = null;
        mContext = null;
        mRedo = null;
        mDurabilityMode = DurabilityMode.NO_REDO;
        mLockMode = LockMode.UNSAFE;
        mBorked = this;
    }

    final void recoveredScope(long savepoint, int hasState) {
        ParentScope parentScope = super.scopeEnter();
        parentScope.mLockMode = mLockMode;
        parentScope.mLockTimeoutNanos = mLockTimeoutNanos;
        parentScope.mHasState = mHasState;
        parentScope.mSavepoint = mSavepoint;
        mSavepoint = savepoint;
        mHasState = hasState;
    }

    public final void recoveredUndoLog(UndoLog undo) {
        mContext.register(undo);
        mUndoLog = undo;
    }

    public final void recoverPrepared(RedoWriter redo, DurabilityMode durabilityMode,
                                      LockMode lockMode, long timeoutNanos) {
        mHasState |= HAS_COMMIT;

        mRedo = redo;
        mLockMode = lockMode;
        mDurabilityMode = durabilityMode;
        mLockTimeoutNanos = timeoutNanos;
    }

    @Override
    public void attach(Object obj) {
        mAttachment = obj;
    }

    @Override
    public Object attachment() {
        return mAttachment;
    }

    @Override
    public final void lockMode(LockMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Lock mode is null");
        } else {
            bogusCheck();
            mLockMode = mode;
        }
    }

    @Override
    public final LockMode lockMode() {
        return mLockMode;
    }

    @Override
    public final void lockTimeout(long timeout, TimeUnit unit) {
        bogusCheck();
        mLockTimeoutNanos = Utils.toNanos(timeout, unit);
    }

    @Override
    public final long lockTimeout(TimeUnit unit) {
        return unit.convert(mLockTimeoutNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public final void durabilityMode(DurabilityMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Durability mode is null");
        } else {
            bogusCheck();
            mDurabilityMode = mode;
        }
    }

    @Override
    public final DurabilityMode durabilityMode() {
        return mDurabilityMode;
    }

    @Override
    public final void check() throws DatabaseException {
        Object borked = mBorked;
        if (borked != null) {
            check(borked);
        }
    }

    private void check(Object borked) throws DatabaseException {
        if (borked == BOGUS) {
            throw new IllegalStateException("Transaction is bogus");
        } else if (borked instanceof Throwable) {
            throw new InvalidTransactionException((Throwable) borked);
        } else {
            throw new InvalidTransactionException(String.valueOf(borked));
        }
    }

    private void bogusCheck() {
        if (mBorked == BOGUS) {
            throw new IllegalStateException("Transaction is bogus");
        }
    }

    @Override
    public final void commit() throws IOException {
        Object borked = mBorked;
        if (borked != null) {
            if (borked == BOGUS) {
                return;
            }
            check(borked);
        }

        try {
            ParentScope parentScope = mParentScope;
            if (parentScope == null) {
                UndoLog undo = mUndoLog;
                if (undo == null) {
                    int hasState = mHasState;
                    if ((hasState & HAS_COMMIT) != 0) {
                        long commitPos = mContext.redoCommitFinal(mRedo, mTxnId, mDurabilityMode);
                        mHasState = hasState & ~(HAS_SCOPE | HAS_COMMIT);
                        if (commitPos != 0) {
                            if (mDurabilityMode == DurabilityMode.SYNC) {
                                mRedo.txnCommitSync(this, commitPos);
                            } else {
                                commitPending(commitPos, null);
                                return;
                            }
                        }
                    }
                    super.scopeUnlockAll();
                } else {
                    final CommitLock.Shared shared = mDatabase.commitLock().acquireShared();
                    long commitPos;
                    try {
                        if ((commitPos = (mHasState & HAS_COMMIT)) != 0) {
                            commitPos = mContext.redoCommitFinal(mRedo, mTxnId, mDurabilityMode);
                            mHasState &= ~(HAS_SCOPE | HAS_COMMIT);
                        }
                        undo.pushCommit();
                    } finally {
                        shared.release();
                    }

                    if (commitPos != 0) {
                        if (mDurabilityMode == DurabilityMode.SYNC) {
                            mRedo.txnCommitSync(this, commitPos);
                        } else {
                            commitPending(commitPos, undo);
                            return;
                        }
                    }

                    super.scopeUnlockAll();

                    undo.truncate(true);

                    mContext.unregister(undo);
                    mUndoLog = null;

                    int hasState = mHasState;
                    if ((hasState & HAS_TRASH) != 0) {
                        mDatabase.fragmentedTrash().emptyTrash(mTxnId);
                        mHasState = hasState & ~HAS_TRASH;
                    }
                }

                mTxnId = 0;
            } else {
                int hasState = mHasState;
                if ((hasState & HAS_COMMIT) != 0) {
                    mContext.redoCommit(mRedo, mTxnId);
                    mHasState = hasState & ~(HAS_SCOPE | HAS_COMMIT);
                    parentScope.mHasState |= HAS_COMMIT;
                }

                super.promote();

                UndoLog undo = mUndoLog;
                if (undo != null) {
                    mSavepoint = undo.scopeCommit();
                }
            }
        } catch (Throwable e) {
            borked(e, true, true);
        }
    }

    private void commitPending(long commitPos, UndoLog undo) throws IOException {
        PendingTxn pending = transferExclusive();
        pending.mContext = mContext;
        pending.mTxnId = mTxnId;
        pending.mCommitPos = commitPos;
        pending.mUndoLog = undo;
        pending.mHasState = mHasState;
        pending.attach(mAttachment);

        mUndoLog = null;
        mHasState = 0;
        mTxnId = 0;

        mRedo.txnCommitPending(pending);
    }

    public final void storeCommit(boolean requireUndo, TreeCursor cursor, byte[] value)
            throws IOException {
        if (mRedo == null) {
            cursor.storeNoRedo(this, value);
            commit();
            return;
        }

        check();

        long txnId = mTxnId;

        final CommitLock.Shared shared = mDatabase.commitLock().acquireShared();
        try {
            if (txnId == 0) {
                txnId = assignTransactionId();
            }
        } catch (Throwable e) {
            shared.release();
            throw e;
        }

        try {
            int hasState = mHasState;
            byte[] key = cursor.getKey();

            ParentScope parentScope = mParentScope;
            if (parentScope == null) {
                long commitPos;
                try {
                    cursor.storeNoRedo(requireUndo ? this : LocalTransaction.BOGUS, value);

                    if ((hasState & HAS_SCOPE) == 0) {
                        mContext.redoEnter(mRedo, txnId);
                        mHasState = hasState | HAS_SCOPE;
                    }

                    long cursorId = cursor.getCursorId();
                    if (cursorId == 0) {
                        long indexId = cursor.getTree().getId();
                        if (value == null) {
                            commitPos = mContext.redoDeleteCommitFinal
                                    (mRedo, txnId, indexId, key, mDurabilityMode);
                        } else {
                            commitPos = mContext.redoStoreCommitFinal
                                    (mRedo, txnId, indexId, key, value, mDurabilityMode);
                        }
                    } else {
                        if (value == null) {
                            mContext.redoCursorDelete(mRedo, cursorId, txnId, key);
                        } else {
                            mContext.redoCursorStore(mRedo, cursorId, txnId, key, value);
                        }
                        commitPos = mContext.redoCommitFinal(mRedo, txnId, mDurabilityMode);
                    }
                } catch (Throwable e) {
                    shared.release();
                    throw e;
                }

                mHasState = hasState & ~(HAS_SCOPE | HAS_COMMIT);

                UndoLog undo = mUndoLog;
                if (undo == null) {
                    shared.release();
                    if (commitPos != 0) {
                        if (mDurabilityMode == DurabilityMode.SYNC) {
                            mRedo.txnCommitSync(this, commitPos);
                        } else {
                            commitPending(commitPos, null);
                            return;
                        }
                    }
                    super.scopeUnlockAll();
                } else {
                    try {
                        undo.pushCommit();
                    } finally {
                        shared.release();
                    }

                    if (commitPos != 0) {
                        if (mDurabilityMode == DurabilityMode.SYNC) {
                            mRedo.txnCommitSync(this, commitPos);
                        } else {
                            commitPending(commitPos, undo);
                            return;
                        }
                    }

                    super.scopeUnlockAll();

                    undo.truncate(true);

                    mContext.unregister(undo);
                    mUndoLog = null;

                    if ((hasState & HAS_TRASH) != 0) {
                        mDatabase.fragmentedTrash().emptyTrash(mTxnId);
                        mHasState = hasState & ~HAS_TRASH;
                    }
                }

                mTxnId = 0;
            } else {
                try {
                    cursor.storeNoRedo(this, value);

                    long cursorId = cursor.getCursorId();
                    if (cursorId == 0) {
                        long indexId = cursor.getTree().getId();
                        if ((hasState & HAS_SCOPE) == 0) {
                            setScopeState(parentScope);
                            if (value == null) {
                                mContext.redoDelete
                                        (mRedo, RedoOps.OP_TXN_DELETE, txnId, indexId, key);
                            } else {
                                mContext.redoStore
                                        (mRedo, RedoOps.OP_TXN_STORE, txnId, indexId, key, value);
                            }
                        } else {
                            if (value == null) {
                                mContext.redoDelete
                                        (mRedo, RedoOps.OP_TXN_DELETE_COMMIT, txnId, indexId, key);
                            } else {
                                mContext.redoStore
                                        (mRedo, RedoOps.OP_TXN_STORE_COMMIT, txnId, indexId, key, value);
                            }
                        }
                    } else {
                        if ((hasState & HAS_SCOPE) == 0) {
                            setScopeState(parentScope);
                            if (value == null) {
                                mContext.redoCursorDelete(mRedo, cursorId, txnId, key);
                            } else {
                                mContext.redoCursorStore(mRedo, cursorId, txnId, key, value);
                            }
                        } else {
                            if (value == null) {
                                mContext.redoCursorDelete(mRedo, cursorId, txnId, key);
                            } else {
                                mContext.redoCursorStore(mRedo, cursorId, txnId, key, value);
                            }
                            mContext.redoCommit(mRedo, txnId);
                        }
                    }
                } finally {
                    shared.release();
                }

                mHasState = hasState & ~(HAS_SCOPE | HAS_COMMIT);
                parentScope.mHasState |= HAS_COMMIT;

                super.promote();

                UndoLog undo = mUndoLog;
                if (undo != null) {
                    mSavepoint = undo.scopeCommit();
                }
            }
        } catch (Throwable e) {
            borked(e, true, true); // rollback = true, rethrow = true
        }
    }

    @Override
    public final void commitAll() throws IOException {
        while (true) {
            commit();
            if (mParentScope == null) {
                break;
            }
            exit();
        }
    }

    @Override
    public final void enter() throws IOException {
        check();

        try {
            ParentScope parentScope = super.scopeEnter();
            parentScope.mLockMode = mLockMode;
            parentScope.mLockTimeoutNanos = mLockTimeoutNanos;
            parentScope.mHasState = mHasState;

            UndoLog undo = mUndoLog;
            if (undo != null) {
                parentScope.mSavepoint = mSavepoint;
                mSavepoint = undo.scopeEnter();
            }

            mHasState &= ~(HAS_SCOPE | HAS_COMMIT);
        } catch (Throwable e) {
            borked(e, true, true); // rollback = true, rethrow = true
        }
    }

    @Override
    public final void exit() {
        if (mBorked != null) {
            super.scopeExit();
            return;
        }

        try {
            ParentScope parentScope = mParentScope;
            if (parentScope == null) {
                try {
                    int hasState = mHasState;
                    if ((hasState & HAS_SCOPE) != 0) {
                        mContext.redoRollbackFinal(mRedo, mTxnId);
                    }
                    mHasState = 0;
                } catch (UnmodifiableReplicaException e) {
                    // Suppress and let undo proceed.
                }

                UndoLog undo = mUndoLog;
                if (undo != null) {
                    undo.rollback();
                }

                // Exit and release all locks obtained in this scope.
                super.scopeExit();

                mSavepoint = 0;
                if (undo != null) {
                    mContext.unregister(undo);
                    mUndoLog = null;
                }

                mTxnId = 0;
            } else {
                try {
                    int hasState = mHasState;
                    if ((hasState & HAS_SCOPE) != 0) {
                        mContext.redoRollback(mRedo, mTxnId);
                        mHasState = hasState & ~(HAS_SCOPE | HAS_COMMIT);
                    }
                } catch (UnmodifiableReplicaException e) {
                    // Suppress and let undo proceed.
                }

                UndoLog undo = mUndoLog;
                if (undo != null) {
                    undo.scopeRollback(mSavepoint);
                }

                super.scopeExit();

                mLockMode = parentScope.mLockMode;
                mLockTimeoutNanos = parentScope.mLockTimeoutNanos;
                mHasState |= parentScope.mHasState;
                mSavepoint = parentScope.mSavepoint;
            }
        } catch (Throwable e) {
            borked(e, true, false); // rollback = true, rethrow = false
        }
    }

    @Override
    public final void reset() {
        if (mBorked == null) {
            try {
                rollback();
            } catch (Throwable e) {
                borked(e, true, false); // rollback = true, rethrow = false
            }
        } else {
            super.scopeExitAll();
        }
    }

    @Override
    public final void reset(Throwable cause) {
        if (cause == null) {
            try {
                reset();
            } catch (Throwable e) {
                // Ignore. Transaction is borked as a side-effect.
            }
        } else {
            borked(cause, true, false); // rollback = true, rethrow = false
        }
    }

    private void rollback() throws IOException {
        int hasState = mHasState;
        ParentScope parentScope = mParentScope;
        while (parentScope != null) {
            hasState |= parentScope.mHasState;
            parentScope = parentScope.mParentScope;
        }

        try {
            if ((hasState & (HAS_SCOPE | HAS_COMMIT)) != 0) {
                mContext.redoRollbackFinal(mRedo, mTxnId);
            }
            mHasState = 0;
        } catch (UnmodifiableReplicaException e) {
            // Suppress and let undo proceed.
        }

        UndoLog undo = mUndoLog;
        if (undo != null) {
            undo.rollback();
        }

        super.scopeExitAll();

        mSavepoint = 0;
        if (undo != null) {
            mContext.unregister(undo);
            mUndoLog = null;
        }

        mTxnId = 0;
    }

    @Override
    public void flush() throws IOException {
        if (mTxnId != 0) {
            mContext.flush();
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(Transaction.class.getName());

        if (this == BOGUS) {
            return b.append('.').append("BOGUS").toString();
        }

        b.append('@').append(Integer.toHexString(hashCode()));

        b.append(" {");
        b.append("id").append(": ").append(mTxnId);
        b.append(", ");
        b.append("durabilityMode").append(": ").append(mDurabilityMode);
        b.append(", ");
        b.append("lockMode").append(": ").append(mLockMode);
        b.append(", ");
        b.append("lockTimeout").append(": ");
        TimeUnit unit = Utils.inferUnit(TimeUnit.NANOSECONDS, mLockTimeoutNanos);
        Utils.appendTimeout(b, lockTimeout(unit), unit);

        Object att = mAttachment;
        if (att != null) {
            b.append(", ");
            b.append("attachment").append(": ").append(att);
        }

        Object borked = mBorked;
        if (borked != null) {
            b.append(", ");
            b.append("invalid").append(": ").append(borked);
        }

        return b.append('}').toString();
    }

    @Override
    public final LockResult lockShared(long indexId, byte[] key) throws LockFailureException {
        return super.lockShared(indexId, key, mLockTimeoutNanos);
    }

    final LockResult lockShared(long indexId, byte[] key, int hash) throws LockFailureException {
        return super.lockShared(indexId, key, hash, mLockTimeoutNanos);
    }

    @Override
    public final LockResult lockUpgradable(long indexId, byte[] key) throws LockFailureException {
        return super.lockUpgradable(indexId, key, mLockTimeoutNanos);
    }

    final LockResult lockUpgradable(long indexId, byte[] key, int hash)
            throws LockFailureException {
        return super.lockUpgradable(indexId, key, hash, mLockTimeoutNanos);
    }

    @Override
    public final LockResult lockExclusive(long indexId, byte[] key) throws LockFailureException {
        return super.lockExclusive(indexId, key, mLockTimeoutNanos);
    }

    final LockResult lockExclusive(long indexId, byte[] key, int hash)
            throws LockFailureException {
        return super.lockExclusive(indexId, key, hash, mLockTimeoutNanos);
    }

    final LockResult lockExclusive(Lock lock) throws LockFailureException {
        return super.lockExclusive(lock, mLockTimeoutNanos);
    }

    @Override
    public final void customRedo(byte[] message, long indexId, byte[] key) throws IOException {
        if (mDatabase.getCustomTxnHandler() == null) {
            throw new IllegalStateException("Custom transaction handler is not installed");
        }
        check();

        if (mRedo == null) {
            return;
        }

        long txnId = mTxnId;

        if (txnId == 0) {
            final CommitLock.Shared shared = mDatabase.commitLock().acquireShared();
            try {
                txnId = assignTransactionId();
            } finally {
                shared.release();
            }
        }

        int hasState = mHasState;
        if ((hasState & HAS_SCOPE) == 0) {
            ParentScope parentScope = mParentScope;
            if (parentScope != null) {
                setScopeState(parentScope);
            }
            mContext.redoEnter(mRedo, txnId);
        }

        mHasState = hasState | (HAS_SCOPE | HAS_COMMIT);

        if (indexId == 0) {
            if (key != null) {
                throw new IllegalArgumentException("Key cannot be used if indexId is zero");
            }
            mContext.redoCustom(mRedo, txnId, message);
        } else {
            LockResult result = lockCheck(indexId, key);
            if (result != LockResult.OWNED_EXCLUSIVE) {
                throw new IllegalStateException("Lock isn't owned exclusively: " + result);
            }
            mContext.redoCustomLock(mRedo, txnId, message, indexId, key);
        }
    }

    @Override
    public final void customUndo(byte[] message) throws IOException {
        if (mDatabase.getCustomTxnHandler() == null) {
            throw new IllegalStateException("Custom transaction handler is not installed");
        }

        check();

        final CommitLock.Shared shared = mDatabase.commitLock().acquireShared();
        try {
            undoLog().pushCustom(message);
        } catch (Throwable e) {
            borked(e, true, true); // rollback = true, rethrow = true
        } finally {
            shared.release();
        }
    }

    @Override
    public final long getId() {
        long txnId = mTxnId;

        if (txnId == 0 && mRedo != null) {
            final CommitLock.Shared shared = mDatabase.commitLock().acquireShared();
            try {
                txnId = assignTransactionId();
            } finally {
                shared.release();
            }
        }

        return txnId < 0 ? 0 : txnId;
    }

    @Override
    public final void prepare() throws IOException {
        check();

        if (mDatabase.getRecoveryHandler() == null) {
            throw new IllegalStateException("Transaction recovery handler is not installed");
        }

        if (mRedo == null || mDurabilityMode == DurabilityMode.NO_REDO) {
            throw new IllegalStateException("Cannot prepare a no-redo transaction");
        }

        try {
            if ((mHasState & HAS_PREPARE) == 0) {
                pushUndoPrepare();
            }

            long commitPos = mContext.redoPrepare(mRedo, txnId(), mDurabilityMode);
            if (commitPos != 0 && mDurabilityMode == DurabilityMode.SYNC) {
                mRedo.txnCommitSync(this, commitPos);
            }
        } catch (Throwable e) {
            borked(e, true, true); // rollback = true, rethrow = true
        }

        mHasState |= HAS_PREPARE;
    }

    public final void prepareNoRedo() throws IOException {
        check();

        try {
            if ((mHasState & HAS_PREPARE) == 0) {
                pushUndoPrepare();
                mHasState |= HAS_PREPARE;
            }
        } catch (Throwable e) {
            borked(e, true, true); // rollback = true, rethrow = true
        }
    }

    private void pushUndoPrepare() throws IOException {
        final CommitLock.Shared shared = mDatabase.commitLock().acquireShared();
        try {
            undoLog().pushPrepare();
        } finally {
            shared.release();
        }
    }

    public final boolean recoveryCleanup(boolean finish) throws IOException {
        finish = mTxnId < 0 | (finish & (mHasState & HAS_PREPARE) == 0);

        UndoLog undo = mUndoLog;
        if (undo != null) {
            switch (undo.peek(true)) {
                default:
                    break;

                case UndoLog.OP_COMMIT:
                    undo.deleteGhosts();
                    finish = true;
                    break;

                case UndoLog.OP_COMMIT_TRUNCATE:
                    undo.truncate(false);
                    finish = true;
                    break;
            }
        }

        if (finish) {
            reset();
        }

        return finish;
    }

    public final void redoStore(long indexId, byte[] key, byte[] value) throws IOException {
        check();

        if (mRedo == null) {
            return;
        }

        long txnId = mTxnId;

        if (txnId == 0) {
            txnId = assignTransactionId();
        }

        try {
            int hasState = mHasState;

            mHasState = hasState | HAS_COMMIT;

            if ((hasState & HAS_SCOPE) == 0) {
                ParentScope parentScope = mParentScope;
                if (parentScope != null) {
                    setScopeState(parentScope);
                }
                if (value == null) {
                    mContext.redoDelete(mRedo, RedoOps.OP_TXN_ENTER_DELETE, txnId, indexId, key);
                } else {
                    mContext.redoStore(mRedo, RedoOps.OP_TXN_ENTER_STORE, txnId, indexId, key, value);
                }
                mHasState = hasState | (HAS_SCOPE | HAS_COMMIT);
            } else {
                if (value == null) {
                    mContext.redoDelete(mRedo, RedoOps.OP_TXN_DELETE, txnId, indexId, key);
                } else {
                    mContext.redoStore(mRedo, RedoOps.OP_TXN_STORE, txnId, indexId, key, value);
                }
            }
        } catch (Throwable e) {
            borked(e, false, true); // rollback = false, rethrow = true
        }
    }

    public final void redoCursorStore(long cursorId, byte[] key, byte[] value) throws IOException {
        check();

        if (mRedo == null) {
            return;
        }

        long txnId = mTxnId;

        if (txnId == 0) {
            txnId = assignTransactionId();
        }

        try {
            int hasState = mHasState;

            mHasState = hasState | HAS_COMMIT;

            if ((hasState & HAS_SCOPE) == 0) {
                ParentScope parentScope = mParentScope;
                if (parentScope != null) {
                    setScopeState(parentScope);
                }
                mContext.redoEnter(mRedo, txnId);
            }

            if (value == null) {
                mContext.redoCursorDelete(mRedo, cursorId, txnId, key);
            } else {
                mContext.redoCursorStore(mRedo, cursorId, txnId, key, value);
            }

            mHasState = hasState | (HAS_SCOPE | HAS_COMMIT);
        } catch (Throwable e) {
            borked(e, false, true); // rollback = false, rethrow = true
        }
    }

    private void setScopeState(ParentScope scope) throws IOException {
        int hasState = scope.mHasState;
        if ((hasState & HAS_SCOPE) == 0) {
            ParentScope parentScope = scope.mParentScope;
            if (parentScope != null) {
                setScopeState(parentScope);
            }

            mContext.redoEnter(mRedo, mTxnId);
            scope.mHasState = hasState | HAS_SCOPE;
        }
    }

    public final long txnId() {
        long txnId = mTxnId;
        if (txnId == 0) {
            txnId = mContext.nextTransactionId();
            if (mRedo != null) {
                txnId = mRedo.adjustTransactionId(txnId);
            }
            mTxnId = txnId;
        }
        return txnId;
    }

    private long assignTransactionId() {
        long txnId = mContext.nextTransactionId();
        txnId = mRedo.adjustTransactionId(txnId);
        mTxnId = txnId;
        return txnId;
    }

    public final boolean tryRedoCursorRegister(TreeCursor cursor) throws IOException {
        if (mRedo == null || (mTxnId <= 0 && mRedo.adjustTransactionId(1) <= 0)) {
            return false;
        } else {
            doRedoCursorRegister(cursor);
            return true;
        }
    }

    private long doRedoCursorRegister(TreeCursor cursor) throws IOException {
        long cursorId = mContext.nextTransactionId();
        try {
            mContext.redoCursorRegister(mRedo, cursorId, cursor.getTree().getId());
        } catch (Throwable e) {
            borked(e, false, true); // rollback = false, rethrow = true
        }
        cursor.setCursorId(cursorId);
        return cursorId;
    }

    public final void redoCursorValueModify(TreeCursor cursor, int op,
                                            long pos, byte[] buf, int off, long len)
            throws IOException {
        check();

        if (mRedo == null) {
            return;
        }

        long txnId = mTxnId;

        if (txnId == 0) {
            txnId = assignTransactionId();
        }

        try {
            int hasState = mHasState;
            if ((hasState & HAS_SCOPE) == 0) {
                ParentScope parentScope = mParentScope;
                if (parentScope != null) {
                    setScopeState(parentScope);
                }
                mContext.redoEnter(mRedo, txnId);
            }

            mHasState = hasState | (HAS_SCOPE | HAS_COMMIT);

            long cursorId = cursor.getCursorId();

            if (cursorId < 0) {
                cursorId &= ~(1L << 63);
            } else {
                if (cursorId == 0) {
                    cursorId = doRedoCursorRegister(cursor);
                }
                mContext.redoCursorFind(mRedo, cursorId, txnId, cursor.getKey());
                cursor.setCursorId(cursorId | (1L << 63));
            }

            if (op == TreeValue.OP_SET_LENGTH) {
                mContext.redoCursorValueSetLength(mRedo, cursorId, txnId, pos);
            } else if (op == TreeValue.OP_WRITE) {
                mContext.redoCursorValueWrite(mRedo, cursorId, txnId, pos, buf, off, (int) len);
            } else {
                mContext.redoCursorValueClear(mRedo, cursorId, txnId, pos, len);
            }
        } catch (Throwable e) {
            borked(e, false, true); // rollback = false, rethrow = true
        }
    }

    public final void setHasTrash() {
        mHasState |= HAS_TRASH;
    }

    public final void pushUndoStore(long indexId, byte op, long payload, int off, int len)
            throws IOException {
        check();
        try {
            undoLog().pushNodeEncoded(indexId, op, payload, off, len);
        } catch (Throwable e) {
            borked(e, false, true); // rollback = false, rethrow = true
        }
    }

    public final void pushUninsert(long indexId, byte[] key) throws IOException {
        check();
        try {
            undoLog().pushUninsert(indexId, key);
        } catch (Throwable e) {
            borked(e, false, true); // rollback = false, rethrow = true
        }
    }

    public final void pushUndeleteFragmented(long indexId, byte[] payload, int off, int len)
            throws IOException {
        check();
        try {
            undoLog().pushNodeEncoded(indexId, UndoLog.OP_UNDELETE_FRAGMENTED, payload, off, len);
        } catch (Throwable e) {
            borked(e, false, true); // rollback = false, rethrow = true
        }
    }

    public final void pushUncreate(long indexId, byte[] key) throws IOException {
        check();
        try {
            undoLog().pushUncreate(indexId, key);
        } catch (Throwable e) {
            borked(e, false, true); // rollback = false, rethrow = true
        }
    }

    public final void pushUnextend(long indexId, byte[] key, long length) throws IOException {
        check();
        try {
            undoLog().pushUnextend(mSavepoint, indexId, key, length);
        } catch (Throwable e) {
            borked(e, false, true); // rollback = false, rethrow = true
        }
    }

    public final void pushUnalloc(long indexId, byte[] key, long pos, long length) throws IOException {
        check();
        try {
            undoLog().pushUnalloc(indexId, key, pos, length);
        } catch (Throwable e) {
            borked(e, false, true); // rollback = false, rethrow = true
        }
    }

    public final void pushUnwrite(long indexId, byte[] key, long pos, long b, int off, int len)
            throws IOException {
        check();
        try {
            undoLog().pushUnwrite(indexId, key, pos, b, off, len);
        } catch (Throwable e) {
            borked(e, false, true); // rollback = false, rethrow = true
        }
    }

    private UndoLog undoLog() throws IOException {
        UndoLog undo = mUndoLog;
        if (undo == null) {
            undo = new UndoLog(mDatabase, txnId());

            ParentScope parentScope = mParentScope;
            while (parentScope != null) {
                undo.doScopeEnter();
                parentScope = parentScope.mParentScope;
            }

            mContext.register(undo);
            mUndoLog = undo;
        }
        return undo;
    }

    public final void borked(Throwable borked, boolean rollback, boolean rethrow) {
        boolean closed = mDatabase == null ? false : mDatabase.isClosed();

        if (mBorked == null) {
            if (closed) {
                Utils.initCause(borked, mDatabase.closedCause());
                mBorked = borked;
            } else if (rollback) {
                try {
                    rollback();
                } catch (Throwable rollbackFailed) {

                    Utils.suppress(borked, rollbackFailed);
                    try {
                        Utils.closeOnFailure(mDatabase, borked);
                    } catch (Throwable e) {
                        // Ignore.
                    }

                    discardAllLocks();
                }

                mBorked = borked;

                mUndoLog = null;
            }
        }

        if (rethrow || !closed) {
            Utils.rethrow(borked);
        }
    }
}
