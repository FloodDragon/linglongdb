package com.linglong.server.database.exception;

/**
 * Created by liuj-ai on 2021/3/23.
 */
public class TxnNotFoundException extends RuntimeException {

    private Long txnId;

    public TxnNotFoundException(Long txnId) {
        this.txnId = txnId;
    }

    public TxnNotFoundException(Long txnId, String message) {
        super(message);
        this.txnId = txnId;
    }

    public TxnNotFoundException(Long txnId, String message, Throwable cause) {
        super(message, cause);
        this.txnId = txnId;
    }

    public TxnNotFoundException(Long txnId, Throwable cause) {
        super(cause);
        this.txnId = txnId;
    }

    public TxnNotFoundException(Long txnId, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.txnId = txnId;
    }
}
