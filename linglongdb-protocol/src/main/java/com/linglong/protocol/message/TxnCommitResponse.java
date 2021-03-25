package com.linglong.protocol.message;

import com.linglong.rpc.serialization.msgpack.BeanMessage;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public class TxnCommitResponse extends TxnResponse implements BeanMessage {

    private boolean commited;

    public boolean isCommited() {
        return commited;
    }

    public void setCommited(boolean commited) {
        this.commited = commited;
    }
}
