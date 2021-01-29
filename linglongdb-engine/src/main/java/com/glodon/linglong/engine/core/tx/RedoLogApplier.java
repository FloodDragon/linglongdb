package com.glodon.linglong.engine.core.tx;

import com.glodon.linglong.base.common.LHashTable;
import com.glodon.linglong.base.exception.DatabaseException;
import com.glodon.linglong.engine.core.frame.Cursor;
import com.glodon.linglong.engine.core.frame.Index;
import com.glodon.linglong.engine.core.LocalDatabase;
import com.glodon.linglong.engine.core.TreeCursor;
import com.glodon.linglong.engine.core.lock.LockMode;
import com.glodon.linglong.engine.extend.TransactionHandler;

import java.io.IOException;

/**
 * @author Stereo
 */
final public class RedoLogApplier implements RedoVisitor {
    private final LocalDatabase mDatabase;
    private final LHashTable.Obj<LocalTransaction> mTransactions;
    private final LHashTable.Obj<Index> mIndexes;
    private final LHashTable.Obj<TreeCursor> mCursors;

    long mHighestTxnId;

    public long getHighestTxnId() {
        return mHighestTxnId;
    }

    public RedoLogApplier(LocalDatabase db, LHashTable.Obj<LocalTransaction> txns,
                          LHashTable.Obj<TreeCursor> cursors) {
        mDatabase = db;
        mTransactions = txns;
        mIndexes = new LHashTable.Obj<>(16);
        mCursors = cursors;
    }

    public void resetCursors() {
        mCursors.traverse(entry -> {
            TreeCursor cursor = entry.value;
            // Unregister first, to prevent close from writing a redo log entry.
            mDatabase.unregisterCursor(cursor);
            cursor.close();
            return false;
        });
    }

    @Override
    public boolean timestamp(long timestamp) {
        return true;
    }

    @Override
    public boolean shutdown(long timestamp) {
        return true;
    }

    @Override
    public boolean close(long timestamp) {
        return true;
    }

    @Override
    public boolean endFile(long timestamp) {
        return true;
    }

    @Override
    public boolean control(byte[] message) {
        return true;
    }

    @Override
    public boolean reset() {
        return true;
    }

    @Override
    public boolean store(long indexId, byte[] key, byte[] value) throws IOException {
        return storeNoLock(indexId, key, value);
    }

    @Override
    public boolean storeNoLock(long indexId, byte[] key, byte[] value) throws IOException {
        Index ix = openIndex(indexId);
        if (ix != null) {
            ix.store(Transaction.BOGUS, key, value);
        }
        return true;
    }

    @Override
    public boolean renameIndex(long txnId, long indexId, byte[] newName) throws IOException {
        checkHighest(txnId);
        Index ix = openIndex(indexId);
        if (ix != null) {
            mDatabase.renameIndex(ix, newName, txnId);
        }
        return true;
    }

    @Override
    public boolean deleteIndex(long txnId, long indexId) throws IOException {
        LocalTransaction txn = txn(txnId);

        Index ix;
        {
            LHashTable.ObjEntry<Index> entry = mIndexes.remove(indexId);
            if (entry == null) {
                ix = mDatabase.anyIndexById(txn, indexId);
            } else {
                ix = entry.value;
            }
        }

        if (ix != null) {
            ix.close();
        }

        return true;
    }

    @Override
    public boolean txnPrepare(long txnId) throws IOException {
        LocalTransaction txn = txn(txnId);
        if (txn != null) {
            txn.prepareNoRedo();
        }
        return true;
    }

    @Override
    public boolean txnEnter(long txnId) throws IOException {
        LocalTransaction txn = txn(txnId);
        if (txn == null) {
            txn = new LocalTransaction(mDatabase, txnId, LockMode.UPGRADABLE_READ, 0L);
            mTransactions.insert(txnId).value = txn;
        } else {
            txn.enter();
        }
        return true;
    }

    @Override
    public boolean txnRollback(long txnId) throws IOException {
        Transaction txn = txn(txnId);
        if (txn != null) {
            txn.exit();
        }
        return true;
    }

    @Override
    public boolean txnRollbackFinal(long txnId) throws IOException {
        checkHighest(txnId);
        Transaction txn = mTransactions.removeValue(txnId);
        if (txn != null) {
            txn.reset();
        }
        return true;
    }

    @Override
    public boolean txnCommit(long txnId) throws IOException {
        Transaction txn = txn(txnId);
        if (txn != null) {
            txn.commit();
            txn.exit();
        }
        return true;
    }

    @Override
    public boolean txnCommitFinal(long txnId) throws IOException {
        checkHighest(txnId);
        LocalTransaction txn = mTransactions.removeValue(txnId);
        if (txn != null) {
            txn.commitAll();
        }
        return true;
    }

    @Override
    public boolean txnEnterStore(long txnId, long indexId, byte[] key, byte[] value)
            throws IOException {
        txnEnter(txnId);
        return txnStore(txnId, indexId, key, value);
    }

    @Override
    public boolean txnStore(long txnId, long indexId, byte[] key, byte[] value)
            throws IOException {
        Transaction txn = txn(txnId);
        if (txn != null) {
            Index ix = openIndex(indexId);
            if (ix != null) {
                ix.store(txn, key, value);
            }
        }
        return true;
    }

    @Override
    public boolean txnStoreCommit(long txnId, long indexId, byte[] key, byte[] value)
            throws IOException {
        txnStore(txnId, indexId, key, value);
        return txnCommit(txnId);
    }

    @Override
    public boolean txnStoreCommitFinal(long txnId, long indexId, byte[] key, byte[] value)
            throws IOException {
        txnStore(txnId, indexId, key, value);
        return txnCommitFinal(txnId);
    }

    @Override
    public boolean cursorRegister(long cursorId, long indexId) throws IOException {
        Index ix = openIndex(indexId);
        if (ix != null) {
            TreeCursor c = (TreeCursor) ix.newCursor(Transaction.BOGUS);
            c.autoload(false);
            mCursors.insert(cursorId).value = c;
        }
        return true;
    }

    @Override
    public boolean cursorUnregister(long cursorId) {
        LHashTable.ObjEntry<TreeCursor> entry = mCursors.remove(cursorId);
        if (entry != null) {
            entry.value.reset();
        }
        return true;
    }

    @Override
    public boolean cursorStore(long cursorId, long txnId, byte[] key, byte[] value)
            throws IOException {
        LHashTable.ObjEntry<TreeCursor> entry = mCursors.get(cursorId);
        if (entry != null) {
            LocalTransaction txn = txn(txnId);
            if (txn != null) {
                TreeCursor c = entry.value;
                c.setTxn(txn);
                c.findNearby(key);
                c.store(value);
                c.setValue(Cursor.NOT_LOADED);
            }
        }
        return true;
    }

    @Override
    public boolean cursorFind(long cursorId, long txnId, byte[] key) throws IOException {
        LHashTable.ObjEntry<TreeCursor> entry = mCursors.get(cursorId);
        if (entry != null) {
            LocalTransaction txn = txn(txnId);
            if (txn != null) {
                TreeCursor c = entry.value;
                c.setTxn(txn);
                c.findNearby(key);
            }
        }
        return true;
    }

    @Override
    public boolean cursorValueSetLength(long cursorId, long txnId, long length)
            throws IOException {
        LHashTable.ObjEntry<TreeCursor> entry = mCursors.get(cursorId);
        if (entry != null) {
            LocalTransaction txn = txn(txnId);
            if (txn != null) {
                readyCursorValueOp(entry, txn).valueLength(length);
            }
        }
        return true;
    }

    @Override
    public boolean cursorValueWrite(long cursorId, long txnId,
                                    long pos, byte[] buf, int off, int len)
            throws IOException {
        LHashTable.ObjEntry<TreeCursor> entry = mCursors.get(cursorId);
        if (entry != null) {
            LocalTransaction txn = txn(txnId);
            if (txn != null) {
                readyCursorValueOp(entry, txn).valueWrite(pos, buf, off, len);
            }
        }
        return true;
    }

    @Override
    public boolean cursorValueClear(long cursorId, long txnId, long pos, long length)
            throws IOException {
        LHashTable.ObjEntry<TreeCursor> entry = mCursors.get(cursorId);
        if (entry != null) {
            LocalTransaction txn = txn(txnId);
            if (txn != null) {
                readyCursorValueOp(entry, txn).valueClear(pos, length);
            }
        }
        return true;
    }

    private TreeCursor readyCursorValueOp(LHashTable.ObjEntry<TreeCursor> entry,
                                          LocalTransaction txn)
            throws IOException {
        TreeCursor c = entry.value;
        LocalTransaction oldTxn = c.getTxn();
        c.setTxn(txn);
        if (oldTxn != txn) {
            c.findNearby(c.getKey());
        }
        return c;
    }

    @Override
    public boolean txnLockShared(long txnId, long indexId, byte[] key) throws IOException {
        Transaction txn = txn(txnId);
        if (txn != null) {
            txn.lockShared(indexId, key);
        }
        return true;
    }

    @Override
    public boolean txnLockUpgradable(long txnId, long indexId, byte[] key) throws IOException {
        Transaction txn = txn(txnId);
        if (txn != null) {
            txn.lockUpgradable(indexId, key);
        }
        return true;
    }

    @Override
    public boolean txnLockExclusive(long txnId, long indexId, byte[] key) throws IOException {
        Transaction txn = txn(txnId);
        if (txn != null) {
            txn.lockExclusive(indexId, key);
        }
        return true;
    }

    @Override
    public boolean txnCustom(long txnId, byte[] message) throws IOException {
        Transaction txn = txn(txnId);
        if (txn != null) {
            LocalDatabase db = mDatabase;
            TransactionHandler handler = db.getCustomTxnHandler();
            if (handler == null) {
                throw new DatabaseException("Custom transaction handler is not installed");
            }
            handler.redo(db, txn, message);
        }
        return true;
    }

    @Override
    public boolean txnCustomLock(long txnId, byte[] message, long indexId, byte[] key)
            throws IOException {
        Transaction txn = txn(txnId);
        if (txn != null) {
            LocalDatabase db = mDatabase;
            TransactionHandler handler = db.getCustomTxnHandler();
            if (handler == null) {
                throw new DatabaseException("Custom transaction handler is not installed");
            }
            txn.lockExclusive(indexId, key);
            handler.redo(db, txn, message, indexId, key);
        }
        return true;
    }

    private LocalTransaction txn(long txnId) {
        checkHighest(txnId);
        return mTransactions.getValue(txnId);
    }

    private void checkHighest(long txnId) {
        if (txnId > mHighestTxnId) {
            mHighestTxnId = txnId;
        }
    }

    private Index openIndex(long indexId) throws IOException {
        LHashTable.ObjEntry<Index> entry = mIndexes.get(indexId);
        if (entry != null) {
            return entry.value;
        }
        Index ix = mDatabase.anyIndexById(indexId);
        if (ix != null) {
            // Maintain a strong reference to the index.
            mIndexes.insert(indexId).value = ix;
        }
        return ix;
    }
}
