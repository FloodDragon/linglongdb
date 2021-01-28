package com.glodon.linglong.engine.core;


import com.glodon.linglong.engine.event.EventListener;

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

    /**
     * Copies a fragmented value to the trash and pushes an entry to the undo
     * log. Caller must hold commit lock.
     *
     * @param entry      _Node page; starts with variable length key
     * @param keyStart   inclusive index into entry for key; includes key header
     * @param keyLen     length of key
     * @param valueStart inclusive index into entry for fragmented value; excludes value header
     * @param valueLen   length of value
     */
    void add(LocalTransaction txn, long indexId,
             long entry, int keyStart, int keyLen, int valueStart, int valueLen)
            throws IOException {
        // It would be nice if cursor store supported array slices. Instead, a
        // temporary array needs to be created.
        byte[] payload = new byte[valueLen];
        DirectPageOps.p_copyToArray(entry, valueStart, payload, 0, valueLen);

        TreeCursor cursor = prepareEntry(txn.txnId());
        byte[] key = cursor.key();
        try {
            // Write trash entry first, ensuring that the undo log entry will refer to
            // something valid. Cursor is bound to a bogus transaction, and so it won't acquire
            // locks or attempt to write to the redo log. A failure here is pretty severe,
            // since it implies that the main database file cannot be written to. One possible
            // "recoverable" cause is a disk full, but this can still cause a database panic if
            // it occurs during critical operations like internal node splits.
            txn.setHasTrash();
            cursor.store(payload);
            cursor.reset();
        } catch (Throwable e) {
            try {
                // Always expected to rethrow an exception, not necessarily the original.
                txn.borked(e, false, true);
            } catch (Throwable e2) {
                e = e2;
            }
            throw com.glodon.my.io.Utils.closeOnFailure(cursor, e);
        }

        // Now write the undo log entry.

        int tidLen = key.length - 8;
        int payloadLen = keyLen + tidLen;
        if (payloadLen > payload.length) {
            // Cannot re-use existing temporary array.
            payload = new byte[payloadLen];
        }
        DirectPageOps.p_copyToArray(entry, keyStart, payload, 0, keyLen);
        arraycopy(key, 8, payload, keyLen, tidLen);

        txn.pushUndeleteFragmented(indexId, payload, 0, payloadLen);
    }

    /**
     * Returns a cursor ready to store a new trash entry. Caller must reset or
     * close the cursor when done.
     */
    private TreeCursor prepareEntry(long txnId) throws IOException {
        // Key entry format is transaction id prefix, followed by a variable
        // length integer. Integer is reverse encoded, and newer entries within
        // the transaction have lower integer values.

        byte[] prefix = new byte[8];
        Utils.encodeLongBE(prefix, 0, txnId);

        TreeCursor cursor = new TreeCursor(mTrash, Transaction.BOGUS);
        try {
            cursor.autoload(false);
            cursor.findGt(prefix);
            byte[] key = cursor.key();
            if (key == null || Utils.compareUnsigned(key, 0, 8, prefix, 0, 8) != 0) {
                // Create first entry for this transaction.
                key = new byte[8 + 1];
                arraycopy(prefix, 0, key, 0, 8);
                key[8] = (byte) 0xff;
                cursor.findNearby(key);
            } else {
                // Decrement from previously created entry. Although key will
                // be modified, it doesn't need to be cloned because no
                // transaction was used by the search. The key instance is not
                // shared with the lock manager.
                cursor.findNearby(com.glodon.my.Utils.decrementReverseUnsignedVar(key, 8));
            }
            return cursor;
        } catch (Throwable e) {
            throw Utils.closeOnFailure(cursor, e);
        }
    }

    /**
     * Remove an entry from the trash, as an undo operation. Original entry is
     * stored back into index.
     *
     * @param index index to store entry into; pass null to fully delete it instead
     */
    void remove(long txnId, Tree index, byte[] undoEntry) throws IOException {
        // Extract the index and trash keys.

        long undo = DirectPageOps.p_transfer(undoEntry, false);

        byte[] indexKey, trashKey;
        try {
            _DatabaseAccess dbAccess = mTrash.mRoot;
            indexKey = _Node.retrieveKeyAtLoc(dbAccess, undo, 0);

            int tidLoc = _Node.keyLengthAtLoc(undo, 0);
            int tidLen = undoEntry.length - tidLoc;
            trashKey = new byte[8 + tidLen];
            Utils.encodeLongBE(trashKey, 0, txnId);
            DirectPageOps.p_copyToArray(undo, tidLoc, trashKey, 8, tidLen);
        } finally {
            DirectPageOps.p_delete(undo);
        }

        remove(index, indexKey, trashKey);
    }

    /**
     * Remove an entry from the trash, as an undo operation. Original entry is
     * stored back into index.
     *
     * @param index index to store entry into; pass null to fully delete it instead
     */
    void remove(Tree index, byte[] indexKey, byte[] trashKey) throws IOException {
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

    /**
     * Non-transactionally deletes all fragmented values for the given
     * top-level transaction.
     */
    public void emptyTrash(long txnId) throws IOException {
        byte[] prefix = new byte[8];
        Utils.encodeLongBE(prefix, 0, txnId);

        _LocalDatabase db = mTrash.mDatabase;
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

    /**
     * Non-transactionally deletes all fragmented values. Expected to be called only during
     * recovery, and never when other calls into the trash are being made concurrently.
     *
     * @return true if any trash was found
     */
    boolean emptyAllTrash(EventListener listener) throws IOException {
        boolean found = false;

        _LocalDatabase db = mTrash.mDatabase;
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

    private static boolean deleteFragmented(_LocalDatabase db, Cursor cursor) throws IOException {
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
