package com.glodon.linglong.engine.extend;

import com.glodon.linglong.engine.core.frame.Database;
import com.glodon.linglong.engine.core.tx.Transaction;

import java.io.IOException;
import java.util.concurrent.locks.Lock;

/**
 * @author Stereo
 */
public interface TransactionHandler {

    void redo(Database db, Transaction txn, byte[] message)
            throws IOException;

    void redo(Database db, Transaction txn, byte[] message, long indexId, byte[] key)
            throws IOException;

    void undo(Database db, byte[] message) throws IOException;

    default void setCheckpointLock(Database db, Lock lock) {
    }

    default Object checkpointStart(Database db) throws IOException {
        return null;
    }

    default void checkpointFinish(Database db, Object obj) throws IOException {
    }
}
