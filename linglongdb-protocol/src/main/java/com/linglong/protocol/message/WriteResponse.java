package com.linglong.protocol.message;

import com.linglong.protocol.Message;

/**
 * @author Stereo on 2021/3/15.
 */
public class WriteResponse extends Message {
    /* 索引值 */
    private byte[] value;
    /* 是否成功 */
    private boolean successful;

    public byte[] getValue() {
        return value;
    }

    public WriteResponse setValue(byte[] value) {
        this.value = value;
        return this;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public WriteResponse setSuccessful(boolean successful) {
        this.successful = successful;
        return this;
    }
}
