package com.glodon.linglong.engine.core;


import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.engine.core.lock.CommitLock;
import com.glodon.linglong.engine.core.page.DirectPageOps;
import com.glodon.linglong.engine.core.tx.LocalTransaction;
import com.glodon.linglong.engine.core.tx.Transaction;
import com.glodon.linglong.engine.event.EventListener;
import com.glodon.linglong.engine.event.EventType;

import java.io.IOException;

import static java.lang.System.arraycopy;

/**
 * @author Stereo
 */
public final class FragmentedTrash {
    final Tree mTrash;

    public FragmentedTrash(Tree trash) {
        mTrash = trash;
    }

    void add(LocalTransaction txn, long indexId,
             long entry, int keyStart, int keyLen, int valueStart, int valueLen)
            throws IOException {
        byte[] payload = new byte[valueLen];
        DirectPageOps.p_copyToArray(entry, valueStart, payload, 0, valueLen);

        TreeCursor cursor = prepareEntry(txn.txnId());
        byte[] key = cursor.key();
        try {
            txn.setHasTrash();
            cursor.store(payload);
            cursor.reset();
        } catch (Throwable e) {
            try {
                txn.borked(e, false, true);
            } catch (Throwable e2) {
                e = e2;
            }
            throw Utils.closeOnFailure(cursor, e);
        }

        int tidLen = key.length - 8;
        int payloadLen = keyLen + tidLen;
        if (payloadLen > payload.length) {
            payload = new byte[payloadLen];
        }
        DirectPageOps.p_copyToArray(entry, keyStart, payload, 0, keyLen);
        arraycopy(key, 8, payload, keyLen, tidLen);

        txn.pushUndeleteFragmented(indexId, payload, 0, payloadLen);
    }

    private TreeCursor prepareEntry(long txnId) throws IOException {

        byte[] prefix = new byte[8];
        Utils.encodeLongBE(prefix, 0, txnId);

        TreeCursor cursor = new TreeCursor(mTrash, Transaction.BOGUS);
        try {
            cursor.autoload(false);
            cursor.findGt(prefix);
            byte[] key = cursor.key();
            if (key == null || Utils.compareUnsigned(key, 0, 8, prefix, 0, 8) != 0) {
                key = new byte[8 + 1];
                arraycopy(prefix, 0, key, 0, 8);
                key[8] = (byte) 0xff;
                cursor.findNearby(key);
            } else {
                cursor.findNearby(Utils.decrementReverseUnsignedVar(key, 8));
            }
            return cursor;
        } catch (Throwable e) {
            throw Utils.closeOnFailure(cursor, e);
        }
    }

    public void remove(long txnId, Tree index, byte[] undoEntry) throws IOException {
        long undo = DirectPageOps.p_transfer(undoEntry, false);

        byte[] indexKey, trashKey;
        try {
            DatabaseAccess dbAccess = mTrash.mRoot;
            indexKey = Node.retrieveKeyAtLoc(dbAccess, undo, 0);

            int tidLoc = Node.keyLengthAtLoc(undo, 0);
            int tidLen = undoEntry.length - tidLoc;
            trashKey = new byte[8 + tidLen];
            Utils.encodeLongBE(trashKey, 0, txnId);
            DirectPageOps.p_copyToArray(undo, tidLoc, trashKey, 8, tidLen);
        } finally {
            DirectPageOps.p_delete(undo);
        }

        remove(index, indexKey, trashKey);
    }

    public void remove(Tree index, byte[] indexKey, byte[] trashKey) throws IOException {
        TreeCursor trashCursor = new TreeCursor(mTrash, Transaction.BOGUS);
        try {
            trashCursor.find(trashKey);

            if (index == null) {
                deleteFragmented(mTrash.mDatabase, trashCursor);
            } else {
                byte[] fragmented = trashCursor.value();
                if (fragmented != null) {
                    TreeCursor ixCursor = new TreeCursor(index, Transaction.BOGUS);
                    try {
                        ixCursor.find(indexKey);
                        ixCursor.storeFragmented(fragmented);
                        ixCursor.reset();
                    } catch (Throwable e) {
                        throw Utils.closeOnFailure(ixCursor, e);
                    }
                    trashCursor.store(null);
                }
            }

            trashCursor.reset();
        } catch (Throwable e) {
            throw Utils.closeOnFailure(trashCursor, e);
        }
    }

    public void emptyTrash(long txnId) throws IOException {
        byte[] prefix = new byte[8];
        Utils.encodeLongBE(prefix, 0, txnId);

        LocalDatabase db = mTrash.mDatabase;
        final CommitLock commitLock = db.commitLock();

        TreeCursor cursor = new TreeCursor(mTrash, Transaction.BOGUS);
        try {
            cursor.autoload(false);
            cursor.findGt(prefix);

            while (true) {
                byte[] key = cursor.key();
                if (key == null || Utils.compareUnsigned(key, 0, 8, prefix, 0, 8) != 0) {
                    break;
                }

                CommitLock.Shared shared = commitLock.acquireShared();
                try {
                    deleteFragmented(db, cursor);
                } finally {
                    shared.release();
                }

                cursor.next();
            }

            cursor.reset();
        } catch (Throwable e) {
            throw Utils.closeOnFailure(cursor, e);
        }
    }

    boolean emptyAllTrash(EventListener listener) throws IOException {
        boolean found = false;

        LocalDatabase db = mTrash.mDatabase;
        final CommitLock commitLock = db.commitLock();

        TreeCursor cursor = new TreeCursor(mTrash, Transaction.BOGUS);
        try {
            cursor.autoload(false);
            cursor.first();

            if (cursor.key() != null) {
                if (listener != null) {
                    listener.notify(EventType.RECOVERY_DELETE_FRAGMENTS,
                            "Deleting unused large fragments");
                }

                do {
                    CommitLock.Shared shared = commitLock.acquireShared();
                    try {
                        found |= deleteFragmented(db, cursor);
                    } finally {
                        shared.release();
                    }

                    cursor.next();
                } while (cursor.key() != null);
            }

            cursor.reset();
        } catch (Throwable e) {
            throw Utils.closeOnFailure(cursor, e);
        }

        return found;
    }

    private static boolean deleteFragmented(LocalDatabase db, Cursor cursor) throws IOException {
        cursor.load();
        byte[] value = cursor.value();
        if (value == null) {
            return false;
        } else {
            long fragmented = DirectPageOps.p_transfer(value, false);
            try {
                db.deleteFragments(fragmented, 0, value.length);
                cursor.store(null);
            } finally {
                DirectPageOps.p_delete(fragmented);
            }
            return true;
        }
    }
}
