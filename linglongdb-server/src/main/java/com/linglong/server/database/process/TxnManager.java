package com.linglong.server.database.process;

import com.linglong.base.concurrent.RWLock;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.frame.Database;
import com.linglong.engine.core.tx.Transaction;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by liuj-ai on 2021/4/27.
 */
public class TxnManager {

    private final Database database;
    private final RWLock txnLock = new RWLock();
    /* txnid ->  Transaction */
    private final Map<Long, Transaction> txnMap = new LinkedHashMap<>();

    TxnManager(Database database) {
        this.database = database;
    }

    Transaction findTxn(long txnId) {
        try {
            txnLock.acquireShared();
            return txnMap.get(txnId);
        } finally {
            txnLock.releaseShared();
        }
    }

    Transaction removeTxn(long txnId) {
        try {
            txnLock.acquireExclusive();
            if (txnMap.containsKey(txnId)) {
                return txnMap.remove(txnId);
            } else {
                return null;
            }
        } finally {
            txnLock.releaseExclusive();
        }
    }

    Transaction begin(DurabilityMode mode) {
        Transaction transaction = mode == null ? database.newTransaction() : database.newTransaction(mode);
        try {
            txnLock.acquireExclusive();
            txnMap.put(transaction.getId(), transaction);
            return transaction;
        } finally {
            txnLock.releaseExclusive();
        }
    }

    boolean commit(long txnId) throws IOException {
        Transaction txn = removeTxn(txnId);
        if (txn != null) {
            txn.commit();
            return true;
        } else {
            return false;
        }
    }

    void rollback(long txnId) throws IOException {
        Transaction txn = removeTxn(txnId);
        if (txn != null) {
            txn.reset();
        }
    }
}
