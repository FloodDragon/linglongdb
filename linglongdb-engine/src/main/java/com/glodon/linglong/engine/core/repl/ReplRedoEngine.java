package com.glodon.linglong.engine.core.repl;

import com.glodon.linglong.base.common.LHashTable;
import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.concurrent.Latch;
import com.glodon.linglong.base.concurrent.Worker;
import com.glodon.linglong.base.concurrent.WorkerGroup;
import com.glodon.linglong.base.exception.ClosedIndexException;
import com.glodon.linglong.base.exception.DatabaseException;
import com.glodon.linglong.base.exception.LockFailureException;
import com.glodon.linglong.base.exception.UnmodifiableReplicaException;
import com.glodon.linglong.engine.core.frame.Cursor;
import com.glodon.linglong.engine.core.frame.Index;
import com.glodon.linglong.engine.core.LocalDatabase;
import com.glodon.linglong.engine.core.TreeCursor;
import com.glodon.linglong.engine.core.lock.Lock;
import com.glodon.linglong.engine.core.lock.LockMode;
import com.glodon.linglong.engine.core.lock.Locker;
import com.glodon.linglong.engine.core.tx.LocalTransaction;
import com.glodon.linglong.engine.core.tx.RedoVisitor;
import com.glodon.linglong.engine.core.tx.RedoWriter;
import com.glodon.linglong.engine.core.tx.Transaction;
import com.glodon.linglong.engine.event.EventListener;
import com.glodon.linglong.engine.event.EventType;
import com.glodon.linglong.engine.extend.ReplicationManager;
import com.glodon.linglong.engine.extend.TransactionHandler;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author Stereo
 */
public class ReplRedoEngine implements RedoVisitor, ThreadFactory {
    private static final int MAX_QUEUE_SIZE = 100;
    private static final int MAX_KEEP_ALIVE_MILLIS = 60_000;
    private static final long INFINITE_TIMEOUT = -1L;
    private static final String ATTACHMENT = "replication";

    private static final long HASH_SPREAD = -7046029254386353131L;

    final ReplicationManager mManager;
    final LocalDatabase mDatabase;

    final ReplRedoController mController;

    private final WorkerGroup mWorkerGroup;

    private final Latch mDecodeLatch;

    private final TxnTable mTransactions;

    private final LHashTable.Obj<SoftReference<Index>> mIndexes;

    private final CursorTable mCursors;

    private ReplRedoDecoder mDecoder;

    public ReplRedoEngine(ReplicationManager manager, int maxThreads,
                          LocalDatabase db, LHashTable.Obj<LocalTransaction> txns,
                          LHashTable.Obj<TreeCursor> cursors)
            throws IOException {
        if (maxThreads <= 0) {
            int procCount = Runtime.getRuntime().availableProcessors();
            maxThreads = maxThreads == 0 ? procCount : (-maxThreads * procCount);
            if (maxThreads <= 0) {
                maxThreads = Integer.MAX_VALUE;
            }
        }

        mManager = manager;
        mDatabase = db;

        mController = new ReplRedoController(this);

        mDecodeLatch = new Latch();

        if (maxThreads <= 1) {
            mWorkerGroup = null;
        } else {
            mWorkerGroup = WorkerGroup.make(maxThreads - 1,
                    MAX_QUEUE_SIZE,
                    MAX_KEEP_ALIVE_MILLIS, TimeUnit.MILLISECONDS,
                    this);
        }

        final TxnTable txnTable;
        if (txns == null) {
            txnTable = new TxnTable(16);
        } else {
            txnTable = new TxnTable(txns.size());

            txns.traverse(te -> {
                long scrambledTxnId = mix(te.key);
                LocalTransaction txn = te.value;
                if (!txn.recoveryCleanup(false)) {
                    txnTable.insert(scrambledTxnId).mTxn = txn;
                }
                return true;
            });
        }

        mTransactions = txnTable;

        mIndexes = new LHashTable.Obj<>(16);

        final CursorTable cursorTable;
        if (cursors == null) {
            cursorTable = new CursorTable(4);
        } else {
            cursorTable = new CursorTable(cursors.size());

            cursors.traverse(ce -> {
                long scrambledCursorId = mix(ce.key);
                cursorTable.insert(scrambledCursorId).mCursor = ce.value;
                // Delete entry.
                return true;
            });
        }

        mCursors = cursorTable;
    }

    public RedoWriter initWriter(long redoNum) {
        mController.initCheckpointNumber(redoNum);
        return mController;
    }

    public void startReceiving(long initialPosition, long initialTxnId) {
        try {
            mDecodeLatch.acquireExclusive();
            try {
                if (mDecoder == null || mDecoder.mDeactivated) {
                    mDecoder = new ReplRedoDecoder
                            (mManager, initialPosition, initialTxnId, mDecodeLatch);
                    newThread(this::decode).start();
                }
            } finally {
                mDecodeLatch.releaseExclusive();
            }
        } catch (Throwable e) {
            fail(e);
        }
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("ReplicationReceiver-" + Long.toUnsignedString(t.getId()));
        t.setUncaughtExceptionHandler((thread, exception) -> fail(exception, true));
        return t;
    }

    @Override
    public boolean reset() throws IOException {
        doReset();
        return true;
    }

    private LHashTable.Obj<LocalTransaction> doReset() throws IOException {
        final LHashTable.Obj<LocalTransaction> remaining;

        if (mTransactions.size() == 0) {
            remaining = null;
        } else {
            remaining = new LHashTable.Obj<>(16);

            mTransactions.traverse(te -> {
                runTask(te, new Worker.Task() {
                    public void run() throws IOException {
                        LocalTransaction txn = te.mTxn;
                        if (!txn.recoveryCleanup(true)) {
                            synchronized (remaining) {
                                remaining.insert(te.key).value = txn;
                            }
                        }
                    }
                });

                return true;
            });
        }

        if (mWorkerGroup != null) {
            mWorkerGroup.join(false);
        }

        synchronized (mCursors) {
            mCursors.traverse(entry -> {
                TreeCursor cursor = entry.mCursor;
                mDatabase.unregisterCursor(cursor);
                cursor.close();
                return true;
            });
        }

        if (remaining == null || remaining.size() == 0) {
            return null;
        }

        remaining.traverse(entry -> {
            mTransactions.insert(entry.key).mTxn = entry.value;
            return false;
        });

        return remaining;
    }

    @Override
    public boolean timestamp(long timestamp) throws IOException {
        return true;
    }

    @Override
    public boolean shutdown(long timestamp) throws IOException {
        return true;
    }

    @Override
    public boolean close(long timestamp) throws IOException {
        return true;
    }

    @Override
    public boolean endFile(long timestamp) throws IOException {
        return true;
    }

    @Override
    public boolean control(byte[] message) throws IOException {
        if (mWorkerGroup != null) {
            mWorkerGroup.join(false);
        }

        mManager.control(mDecoder.getIn().getPos(), message);

        return true;
    }

    @Override
    public boolean store(long indexId, byte[] key, byte[] value) throws IOException {
        Locker locker = new Locker(mDatabase.mLockManager);
        locker.attach(ATTACHMENT);
        locker.tryLockUpgradable(indexId, key, INFINITE_TIMEOUT);

        runTaskAnywhere(new Worker.Task() {
            public void run() throws IOException {
                try {
                    Index ix = getIndex(indexId);

                    locker.lockExclusive(indexId, key, INFINITE_TIMEOUT);

                    doStore(Transaction.BOGUS, indexId, key, value);
                } finally {
                    locker.scopeUnlockAll();
                }
            }
        });

        return true;
    }

    @Override
    public boolean storeNoLock(long indexId, byte[] key, byte[] value) throws IOException {
        return store(indexId, key, value);
    }

    @Override
    public boolean renameIndex(long txnId, long indexId, byte[] newName) throws IOException {
        Index ix = getIndex(indexId);

        if (ix == null) {
            return true;
        }

        byte[] oldName = ix.getName();

        try {
            mDatabase.renameIndex(ix, newName, txnId);
        } catch (RuntimeException e) {
            EventListener listener = mDatabase.eventListener();
            if (listener != null) {
                listener.notify(EventType.REPLICATION_WARNING,
                        "Unable to rename index: %1$s", Utils.rootCause(e));
                return true;
            }
        }

        runTaskAnywhere(new Worker.Task() {
            public void run() {
                mManager.notifyRename(ix, oldName, newName.clone());
            }
        });

        return true;
    }

    @Override
    public boolean deleteIndex(long txnId, long indexId) {
        TxnEntry te = getTxnEntry(txnId);

        runTask(te, new Worker.Task() {
            public void run() throws IOException {
                LocalTransaction txn = te.mTxn;
                Index ix = getIndex(txn, indexId);
                synchronized (mIndexes) {
                    mIndexes.remove(indexId);
                }

                try {
                    txn.commit();
                } finally {
                    txn.exit();
                }

                if (ix != null) {
                    ix.close();
                    try {
                        mManager.notifyDrop(ix);
                    } catch (Throwable e) {
                        Utils.uncaught(e);
                    }
                }

                Runnable task = mDatabase.replicaDeleteTree(indexId);

                if (task != null) {
                    try {
                        Thread deletion = new Thread
                                (task, "IndexDeletion-" +
                                        (ix == null ? indexId : ix.getNameString()));
                        deletion.setDaemon(true);
                        deletion.start();
                    } catch (Throwable e) {
                        EventListener listener = mDatabase.eventListener();
                        if (listener != null) {
                            listener.notify(EventType.REPLICATION_WARNING,
                                    "Unable to immediately delete index: %1$s",
                                    Utils.rootCause(e));
                        }
                    }
                }
            }
        });

        return true;
    }

    @Override
    public boolean txnPrepare(long txnId) throws IOException {
        TxnEntry te = getTxnEntry(txnId);

        runTask(te, new Worker.Task() {
            public void run() throws IOException {
                te.mTxn.prepareNoRedo();
            }
        });

        return true;
    }

    @Override
    public boolean txnEnter(long txnId) throws IOException {
        long scrambledTxnId = mix(txnId);
        TxnEntry te = mTransactions.get(scrambledTxnId);

        if (te == null) {
            mTransactions.insert(scrambledTxnId).mTxn = newTransaction(txnId);
        } else {
            runTask(te, new Worker.Task() {
                public void run() throws IOException {
                    te.mTxn.enter();
                }
            });
        }

        return true;
    }

    @Override
    public boolean txnRollback(long txnId) {
        TxnEntry te = getTxnEntry(txnId);

        runTask(te, new Worker.Task() {
            public void run() {
                te.mTxn.exit();
            }
        });

        return true;
    }

    @Override
    public boolean txnRollbackFinal(long txnId) {
        TxnEntry te = removeTxnEntry(txnId);

        if (te != null) {
            runTask(te, new Worker.Task() {
                public void run() {
                    te.mTxn.reset();
                }
            });
        }

        return true;
    }

    @Override
    public boolean txnCommit(long txnId) {
        TxnEntry te = getTxnEntry(txnId);
        runTask(te, new CommitTask(te));
        return true;
    }

    private static final class CommitTask extends Worker.Task {
        private final TxnEntry mEntry;

        CommitTask(TxnEntry entry) {
            mEntry = entry;
        }

        @Override
        public void run() throws IOException {
            mEntry.mTxn.commit();
        }
    }

    @Override
    public boolean txnCommitFinal(long txnId) {
        TxnEntry te = removeTxnEntry(txnId);
        if (te != null) {
            runTask(te, new CommitFinalTask(te));
        }
        return true;
    }

    private static final class CommitFinalTask extends Worker.Task {
        private final TxnEntry mEntry;

        CommitFinalTask(TxnEntry entry) {
            mEntry = entry;
        }

        @Override
        public void run() throws IOException {
            mEntry.mTxn.commitAll();
        }
    }

    @Override
    public boolean txnEnterStore(long txnId, long indexId, byte[] key, byte[] value)
            throws IOException {
        long scrambledTxnId = mix(txnId);
        TxnEntry te = mTransactions.get(scrambledTxnId);

        LocalTransaction txn;
        boolean newTxn;
        if (te == null) {
            txn = newTransaction(txnId);
            te = mTransactions.insert(scrambledTxnId);
            te.mTxn = txn;
            newTxn = true;
        } else {
            txn = te.mTxn;
            newTxn = false;
        }

        Lock lock = txn.lockUpgradableNoPush(indexId, key);

        runTask(te, new Worker.Task() {
            public void run() throws IOException {
                if (!newTxn) {
                    txn.enter();
                }
                if (lock != null) {
                    txn.push(lock);
                }
                doStore(txn, indexId, key, value);
            }
        });

        return true;
    }

    @Override
    public boolean txnStore(long txnId, long indexId, byte[] key, byte[] value)
            throws LockFailureException {
        TxnEntry te = getTxnEntry(txnId);
        LocalTransaction txn = te.mTxn;

        Lock lock = txn.lockUpgradableNoPush(indexId, key);

        runTask(te, new Worker.Task() {
            public void run() throws IOException {
                if (lock != null) {
                    txn.push(lock);
                }
                doStore(txn, indexId, key, value);
            }
        });

        return true;
    }

    @Override
    public boolean txnStoreCommit(long txnId, long indexId, byte[] key, byte[] value)
            throws LockFailureException {
        TxnEntry te = getTxnEntry(txnId);
        LocalTransaction txn = te.mTxn;

        Lock lock = txn.lockUpgradableNoPush(indexId, key);

        runTask(te, new Worker.Task() {
            public void run() throws IOException {
                if (lock != null) {
                    txn.push(lock);
                }
                doStore(txn, indexId, key, value);
                txn.commit();
            }
        });

        return true;
    }

    @Override
    public boolean txnStoreCommitFinal(long txnId, long indexId, byte[] key, byte[] value)
            throws LockFailureException {
        TxnEntry te = removeTxnEntry(txnId);

        LocalTransaction txn;
        if (te == null) {
            txn = newTransaction(txnId);
        } else {
            txn = te.mTxn;
        }

        Lock lock = txn.lockUpgradableNoPush(indexId, key);

        Worker.Task task = new Worker.Task() {
            public void run() throws IOException {
                if (lock != null) {
                    txn.push(lock);
                }
                doStore(txn, indexId, key, value);
                txn.commitAll();
            }
        };

        if (te == null) {
            runTaskAnywhere(task);
        } else {
            runTask(te, task);
        }

        return true;
    }

    private void doStore(Transaction txn, long indexId, byte[] key, byte[] value)
            throws IOException {
        Index ix = getIndex(indexId);

        while (ix != null) {
            try {
                ix.store(txn, key, value);
                return;
            } catch (Throwable e) {
                ix = reopenIndex(e, indexId);
            }
        }
    }

    @Override
    public boolean cursorRegister(long cursorId, long indexId) throws IOException {
        long scrambledCursorId = mix(cursorId);
        Index ix = getIndex(indexId);
        if (ix != null) {
            TreeCursor tc = (TreeCursor) ix.newCursor(Transaction.BOGUS);
            tc.setKeyOnly(true);
            synchronized (mCursors) {
                mCursors.insert(scrambledCursorId).mCursor = tc;
            }
        }
        return true;
    }

    @Override
    public boolean cursorUnregister(long cursorId) {
        long scrambledCursorId = mix(cursorId);
        CursorEntry ce;
        synchronized (mCursors) {
            ce = mCursors.remove(scrambledCursorId);
        }

        if (ce != null) {
            TreeCursor tc = ce.mCursor;
            Worker w = ce.mWorker;
            if (w == null) {
                tc.reset();
            } else {
                w.enqueue(new Worker.Task() {
                    public void run() throws IOException {
                        tc.reset();
                    }
                });
            }
        }

        return true;
    }

    @Override
    public boolean cursorStore(long cursorId, long txnId, byte[] key, byte[] value)
            throws LockFailureException {
        CursorEntry ce = getCursorEntry(cursorId);
        if (ce == null) {
            return true;
        }

        TxnEntry te = getTxnEntry(txnId);
        LocalTransaction txn = te.mTxn;
        TreeCursor tc = ce.mCursor;

        ce.mKey = key;
        Lock lock = txn.lockUpgradableNoPush(tc.getTree().getId(), key);

        runCursorTask(ce, te, new Worker.Task() {
            public void run() throws IOException {
                if (lock != null) {
                    txn.push(lock);
                }

                TreeCursor tc = ce.mCursor;
                tc.setTxn(txn);
                tc.findNearby(key);

                do {
                    try {
                        tc.store(value);
                        tc.setValue(Cursor.NOT_LOADED);
                        break;
                    } catch (ClosedIndexException e) {
                        tc = reopenCursor(e, ce);
                    }
                } while (tc != null);
            }
        });

        return true;
    }

    @Override
    public boolean cursorFind(long cursorId, long txnId, byte[] key) throws LockFailureException {
        CursorEntry ce = getCursorEntry(cursorId);
        if (ce == null) {
            return true;
        }

        TxnEntry te = getTxnEntry(txnId);
        LocalTransaction txn = te.mTxn;
        TreeCursor tc = ce.mCursor;

        ce.mKey = key;
        Lock lock = txn.lockUpgradableNoPush(tc.getTree().getId(), key);

        runCursorTask(ce, te, new Worker.Task() {
            public void run() throws IOException {
                if (lock != null) {
                    txn.push(lock);
                }
                TreeCursor tc = ce.mCursor;
                tc.setTxn(txn);
                tc.findNearby(key);
            }
        });

        return true;
    }

    @Override
    public boolean cursorValueSetLength(long cursorId, long txnId, long length)
            throws LockFailureException {
        CursorEntry ce = getCursorEntry(cursorId);
        if (ce == null) {
            return true;
        }

        TxnEntry te = getTxnEntry(txnId);
        LocalTransaction txn = te.mTxn;
        TreeCursor tc = ce.mCursor;

        Lock lock = txn.lockUpgradableNoPush(tc.getTree().getId(), ce.mKey);

        runCursorTask(ce, te, new Worker.Task() {
            public void run() throws IOException {
                if (lock != null) {
                    txn.push(lock);
                }

                TreeCursor tc = ce.mCursor;
                tc.setTxn(txn);

                do {
                    try {
                        tc.valueLength(length);
                        break;
                    } catch (ClosedIndexException e) {
                        tc = reopenCursor(e, ce);
                    }
                } while (tc != null);
            }
        });

        return true;
    }

    @Override
    public boolean cursorValueWrite(long cursorId, long txnId,
                                    long pos, byte[] buf, int off, int len)
            throws LockFailureException {
        CursorEntry ce = getCursorEntry(cursorId);
        if (ce == null) {
            return true;
        }

        // TODO: 使用缓冲池。
        byte[] data = Arrays.copyOfRange(buf, off, off + len);

        TxnEntry te = getTxnEntry(txnId);
        LocalTransaction txn = te.mTxn;
        TreeCursor tc = ce.mCursor;

        Lock lock = txn.lockUpgradableNoPush(tc.getTree().getId(), ce.mKey);

        runCursorTask(ce, te, new Worker.Task() {
            public void run() throws IOException {
                if (lock != null) {
                    txn.push(lock);
                }

                TreeCursor tc = ce.mCursor;
                tc.setTxn(txn);

                do {
                    try {
                        tc.valueWrite(pos, data, 0, data.length);
                        break;
                    } catch (ClosedIndexException e) {
                        tc = reopenCursor(e, ce);
                    }
                } while (tc != null);
            }
        });

        return true;
    }

    @Override
    public boolean cursorValueClear(long cursorId, long txnId, long pos, long length)
            throws LockFailureException {
        CursorEntry ce = getCursorEntry(cursorId);
        if (ce == null) {
            return true;
        }

        TxnEntry te = getTxnEntry(txnId);
        LocalTransaction txn = te.mTxn;
        TreeCursor tc = ce.mCursor;

        Lock lock = txn.lockUpgradableNoPush(tc.getTree().getId(), ce.mKey);

        runCursorTask(ce, te, new Worker.Task() {
            public void run() throws IOException {
                if (lock != null) {
                    txn.push(lock);
                }

                TreeCursor tc = ce.mCursor;
                tc.setTxn(txn);

                do {
                    try {
                        tc.valueClear(pos, length);
                        break;
                    } catch (ClosedIndexException e) {
                        tc = reopenCursor(e, ce);
                    }
                } while (tc != null);
            }
        });

        return true;
    }

    private void runCursorTask(CursorEntry ce, TxnEntry te, Worker.Task task) {
        Worker w = ce.mWorker;
        if (w == null) {
            w = te.mWorker;
            if (w == null) {
                w = runTaskAnywhere(task);
                te.mWorker = w;
            } else {
                w.enqueue(task);
            }
            ce.mWorker = w;
        } else {
            Worker txnWorker = te.mWorker;
            if (w != txnWorker) {
                if (txnWorker == null) {
                    txnWorker = w;
                    te.mWorker = w;
                } else {
                    w.join(false);
                    ce.mWorker = txnWorker;
                }
            }
            txnWorker.enqueue(task);
        }
    }

    @Override
    public boolean txnLockShared(long txnId, long indexId, byte[] key)
            throws LockFailureException {
        TxnEntry te = getTxnEntry(txnId);
        LocalTransaction txn = te.mTxn;

        Lock lock = txn.lockSharedNoPush(indexId, key);

        // TODO: 如果事务刚刚创建，则不需要运行特殊任务。
        if (lock != null) {
            runTask(te, new LockPushTask(txn, lock));
        }

        return true;
    }

    @Override
    public boolean txnLockUpgradable(long txnId, long indexId, byte[] key)
            throws LockFailureException {
        TxnEntry te = getTxnEntry(txnId);
        LocalTransaction txn = te.mTxn;

        Lock lock = txn.lockUpgradableNoPush(indexId, key);

        // TODO: 如果事务刚刚创建，则不需要运行特殊任务。
        if (lock != null) {
            runTask(te, new LockPushTask(txn, lock));
        }

        return true;
    }

    private static final class LockPushTask extends Worker.Task {
        private final LocalTransaction mTxn;
        private final Lock mLock;

        LockPushTask(LocalTransaction txn, Lock lock) {
            mTxn = txn;
            mLock = lock;
        }

        @Override
        public void run() {
            mTxn.push(mLock);
        }
    }

    @Override
    public boolean txnLockExclusive(long txnId, long indexId, byte[] key)
            throws LockFailureException {
        TxnEntry te = getTxnEntry(txnId);
        LocalTransaction txn = te.mTxn;

        Lock lock = txn.lockUpgradableNoPush(indexId, key);

        // TODO: 一开始可以获取独占，但必须知道要使用什么推送模式（0或1）
        // TODO: 如果事务刚刚创建，则不需要运行特殊任务。
        runTask(te, new Worker.Task() {
            public void run() throws IOException {
                if (lock != null) {
                    txn.push(lock);
                }
                txn.lockExclusive(indexId, key, INFINITE_TIMEOUT);
            }
        });

        return true;
    }

    @Override
    public boolean txnCustom(long txnId, byte[] message) throws IOException {
        TransactionHandler handler = customHandler();
        TxnEntry te = getTxnEntry(txnId);
        LocalTransaction txn = te.mTxn;

        runTask(te, new Worker.Task() {
            public void run() throws IOException {
                handler.redo(mDatabase, txn, message);
            }
        });

        return true;
    }

    @Override
    public boolean txnCustomLock(long txnId, byte[] message, long indexId, byte[] key)
            throws IOException {
        TransactionHandler handler = customHandler();
        TxnEntry te = getTxnEntry(txnId);
        LocalTransaction txn = te.mTxn;

        Lock lock = txn.lockUpgradableNoPush(indexId, key);

        runTask(te, new Worker.Task() {
            public void run() throws IOException {
                if (lock != null) {
                    txn.push(lock);
                }

                txn.lockExclusive(indexId, key, INFINITE_TIMEOUT);

                handler.redo(mDatabase, txn, message, indexId, key);
            }
        });

        return true;
    }

    long decodePosition() {
        mDecodeLatch.acquireShared();
        try {
            return getDecodePosition();
        } finally {
            mDecodeLatch.releaseShared();
        }
    }

    private long getDecodePosition() {
        ReplRedoDecoder decoder = mDecoder;
        return decoder == null ? mManager.readPosition() : decoder.getDecodePosition();
    }

    long suspendedDecodePosition() {
        return getDecodePosition();
    }

    long suspendedDecodeTransactionId() {
        ReplRedoDecoder decoder = mDecoder;
        if (decoder != null) {
            return decoder.getDecodeTransactionId();
        }
        throw new IllegalStateException("Not decoding");
    }

    void suspend() {
        mDecodeLatch.acquireShared();

        if (mWorkerGroup != null) {
            mWorkerGroup.join(false);
        }
    }

    void resume() {
        mDecodeLatch.releaseShared();
    }

    private TxnEntry getTxnEntry(long txnId) {
        long scrambledTxnId = mix(txnId);
        TxnEntry te = mTransactions.get(scrambledTxnId);

        if (te == null) {
            LocalTransaction txn = newTransaction(txnId);
            te = mTransactions.insert(scrambledTxnId);
            te.mTxn = txn;
        }

        return te;
    }

    private void runTask(TxnEntry te, Worker.Task task) {
        Worker w = te.mWorker;
        if (w == null) {
            te.mWorker = runTaskAnywhere(task);
        } else {
            w.enqueue(task);
        }
    }

    private Worker runTaskAnywhere(Worker.Task task) {
        if (mWorkerGroup == null) {
            try {
                task.run();
            } catch (Throwable e) {
                Utils.uncaught(e);
            }
            return null;
        } else {
            return mWorkerGroup.enqueue(task);
        }
    }

    private LocalTransaction newTransaction(long txnId) {
        LocalTransaction txn = new LocalTransaction
                (mDatabase, txnId, LockMode.UPGRADABLE_READ, INFINITE_TIMEOUT);
        txn.attach(ATTACHMENT);
        return txn;
    }

    private TxnEntry removeTxnEntry(long txnId) {
        long scrambledTxnId = mix(txnId);
        return mTransactions.remove(scrambledTxnId);
    }

    private Index getIndex(Transaction txn, long indexId) throws IOException {
        LHashTable.ObjEntry<SoftReference<Index>> entry = mIndexes.get(indexId);
        if (entry != null) {
            Index ix = entry.value.get();
            if (ix != null) {
                return ix;
            }
        }
        return openIndex(txn, indexId, entry);
    }

    private Index getIndex(long indexId) throws IOException {
        return getIndex(null, indexId);
    }

    private Index openIndex(Transaction txn, long indexId, Object cleanup) throws IOException {
        Index ix = mDatabase.anyIndexById(txn, indexId);
        if (ix == null) {
            return null;
        }

        SoftReference<Index> ref = new SoftReference<>(ix);

        synchronized (mIndexes) {
            mIndexes.insert(indexId).value = ref;

            if (cleanup != null) {
                mIndexes.traverse(e -> e.value.get() == null);
            }
        }
        return ix;
    }

    private Index openIndex(long indexId) throws IOException {
        return openIndex(null, indexId, null);
    }

    private Index reopenIndex(Throwable e, long indexId) throws IOException {
        checkClosedIndex(e);
        return openIndex(indexId);
    }

    private CursorEntry getCursorEntry(long cursorId) {
        long scrambledCursorId = mix(cursorId);
        CursorEntry ce = mCursors.get(scrambledCursorId);
        if (ce == null) {
            synchronized (mCursors) {
                ce = mCursors.get(scrambledCursorId);
            }
        }
        return ce;
    }

    private TreeCursor reopenCursor(Throwable e, CursorEntry ce) throws IOException {
        checkClosedIndex(e);

        long scrambledCursorId = mix(ce.key);
        TreeCursor tc = ce.mCursor;
        Index ix = openIndex(tc.getTree().getId());

        if (ix == null) {
            synchronized (mCursors) {
                mCursors.remove(scrambledCursorId);
            }
        } else {
            LocalTransaction txn = tc.getTxn();
            byte[] key = tc.key();
            tc.reset();

            tc = (TreeCursor) ix.newCursor(txn);
            tc.setKeyOnly(true);
            tc.setTxn(txn);
            tc.find(key);

            synchronized (mCursors) {
                if (ce == mCursors.get(scrambledCursorId)) {
                    ce.mCursor = tc;
                    return tc;
                }
            }
        }

        tc.reset();

        return null;
    }

    private static void checkClosedIndex(final Throwable e) {
        Throwable cause = e;
        while (true) {
            if (cause instanceof ClosedIndexException) {
                break;
            }
            cause = cause.getCause();
            if (cause == null) {
                Utils.rethrow(e);
            }
        }
    }

    private void decode() {
        final ReplRedoDecoder decoder = mDecoder;
        LHashTable.Obj<LocalTransaction> remaining;

        try {
            while (!decoder.run(this)) ;

            if (mWorkerGroup != null) {
                mWorkerGroup.join(false);
            }

            remaining = doReset();
        } catch (Throwable e) {
            fail(e);
            return;
        } finally {
            decoder.mDeactivated = true;
        }

        RedoWriter redo;

        try {
            redo = mController.leaderNotify();
        } catch (UnmodifiableReplicaException e) {
            return;
        } catch (Throwable e) {
            Utils.closeQuietly(mDatabase, e);
            return;
        }

        if (mDatabase.shouldInvokeRecoveryHandler(remaining) && redo != null) {
            mDatabase.invokeRecoveryHandler(remaining, redo);
        }
    }

    private TransactionHandler customHandler() throws DatabaseException {
        TransactionHandler handler = mDatabase.getCustomTxnHandler();
        if (handler == null) {
            throw new DatabaseException("Custom transaction handler is not installed");
        }
        return handler;
    }

    void fail(Throwable e) {
        fail(e, false);
    }

    void fail(Throwable e, boolean isUncaught) {
        if (!mDatabase.isClosed()) {
            EventListener listener = mDatabase.eventListener();
            if (listener != null) {
                listener.notify(EventType.REPLICATION_PANIC,
                        "Unexpected replication exception: %1$s", Utils.rootCause(e));
            } else if (isUncaught) {
                Thread t = Thread.currentThread();
                t.getThreadGroup().uncaughtException(t, e);
            } else {
                Utils.uncaught(e);
            }
        }
        // Panic.
        Utils.closeQuietly(mDatabase, e);
    }

    UnmodifiableReplicaException unmodifiable() throws DatabaseException {
        mDatabase.checkClosed();
        return new UnmodifiableReplicaException();
    }

    private static long mix(long txnId) {
        return HASH_SPREAD * txnId;
    }

    static final class TxnEntry extends LHashTable.Entry<TxnEntry> {
        LocalTransaction mTxn;
        Worker mWorker;
    }

    static final class TxnTable extends LHashTable<TxnEntry> {
        TxnTable(int capacity) {
            super(capacity);
        }

        @Override
        protected TxnEntry newEntry() {
            return new TxnEntry();
        }
    }

    static final class CursorEntry extends LHashTable.Entry<CursorEntry> {
        TreeCursor mCursor;
        Worker mWorker;
        byte[] mKey;
    }

    static final class CursorTable extends LHashTable<CursorEntry> {
        CursorTable(int capacity) {
            super(capacity);
        }

        @Override
        protected CursorEntry newEntry() {
            return new CursorEntry();
        }
    }
}
