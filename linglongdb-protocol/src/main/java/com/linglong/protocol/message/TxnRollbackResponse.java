package com.linglong.protocol.message;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public class TxnRollbackResponse extends TxnResponse {
    
    private boolean rollback;

    public boolean isRollback() {
        return rollback;
    }

    public void setRollback(boolean rollback) {
        this.rollback = rollback;
    }
}
