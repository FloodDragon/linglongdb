package com.linglong.server.database.process;

import com.linglong.engine.core.tx.Transaction;

/**
 * Created by liuj-ai on 2021/3/24.
 */
public class _TxnOptions {
    Long txnId;
    boolean willCommit;
    Transaction transaction;

    public _TxnOptions txnId(Long txnId) {
        this.txnId = txnId;
        return this;
    }

    public _TxnOptions commit() {
        this.willCommit = true;
        return this;
    }

    public _TxnOptions rollback() {
        this.willCommit = false;
        return this;
    }

    public _TxnOptions transaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    public Long getTxnId() {
        return txnId;
    }
}
