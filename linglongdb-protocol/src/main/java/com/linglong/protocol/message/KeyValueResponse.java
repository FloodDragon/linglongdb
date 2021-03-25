package com.linglong.protocol.message;

import com.linglong.rpc.serialization.msgpack.BeanMessage;

/**
 * @author Stereo on 2021/3/15.
 */
public class KeyValueResponse extends Response implements BeanMessage {
    /* 索引值 */
    private byte[] value;
    /* 是否成功 */
    private boolean successful;

    public byte[] getValue() {
        return value;
    }

    public KeyValueResponse setValue(byte[] value) {
        this.value = value;
        return this;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public KeyValueResponse setSuccessful(boolean successful) {
        this.successful = successful;
        return this;
    }
}
