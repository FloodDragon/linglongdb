package com.linglong.protocol.message;

import com.linglong.rpc.serialization.msgpack.BeanMessage;

/**
 * Created by liuj-ai on 2021/3/26.
 */
public class IndexScanResponse extends Response implements BeanMessage {
    private long scanTotal;

    public long getScanTotal() {
        return scanTotal;
    }

    public void setScanTotal(long scanTotal) {
        this.scanTotal = scanTotal;
    }
}
