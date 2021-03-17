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

    public void setValue(byte[] value) {
        this.value = value;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
