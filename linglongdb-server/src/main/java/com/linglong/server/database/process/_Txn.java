package com.linglong.server.database.process;

import com.linglong.engine.core.tx.Transaction;

/**
 * Created by liuj-ai on 2021/3/24.
 */
public class _Txn {
    Long txnId;
    boolean willCommit;
    Transaction transaction;

    public _Txn txnId(Long txnId) {
        this.txnId = txnId;
        return this;
    }

    public _Txn commit() {
        this.willCommit = true;
        return this;
    }

    public _Txn rollback() {
        this.willCommit = false;
        return this;
    }

    public _Txn transaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    public Long getTxnId() {
        return txnId;
    }
}
