package com.linglong.protocol.message;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public class TxnRequest extends Request {
    
    private long txnId;

    public long getTxnId() {
        return txnId;
    }

    public void setTxnId(long txnId) {
        this.txnId = txnId;
    }
}
