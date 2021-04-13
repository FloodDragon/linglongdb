package com.linglong.protocol.message;

import com.linglong.rpc.serialization.msgpack.BeanMessage;

/**
 * Created by liuj-ai on 2021/4/13.
 */
public class IndexScanItemResponse extends Response implements BeanMessage {
    /* 索引键 */
    private byte[] key;
    /* 索引值 */
    private byte[] value;

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}
