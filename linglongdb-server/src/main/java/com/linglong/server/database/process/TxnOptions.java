package com.linglong.server.database.process;

import com.linglong.engine.core.tx.Transaction;

/**
 * Created by liuj-ai on 2021/3/24.
 */
public class TxnOptions {
    Long txnId;
    boolean willCommit;
    Transaction transaction;

    public TxnOptions txnId(Long txnId) {
        this.txnId = txnId;
        return this;
    }

    public TxnOptions commit() {
        this.willCommit = true;
        return this;
    }

    public TxnOptions rollback() {
        this.willCommit = false;
        return this;
    }

    public TxnOptions transaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    public Long getTxnId() {
        return txnId;
    }
}
