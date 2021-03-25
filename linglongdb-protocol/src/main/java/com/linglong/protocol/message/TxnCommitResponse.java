package com.linglong.protocol.message;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public class TxnCommitResponse extends TxnResponse {

    private boolean commited;

    public boolean isCommited() {
        return commited;
    }

    public void setCommited(boolean commited) {
        this.commited = commited;
    }
}
