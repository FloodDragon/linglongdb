package com.linglong.protocol.message;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public class KeyLowHighRequest extends IndexRequest {

    private byte[] lowKey;
    private byte[] highKey;

    public byte[] getLowKey() {
        return lowKey;
    }

    public void setLowKey(byte[] lowKey) {
        this.lowKey = lowKey;
    }

    public byte[] getHighKey() {
        return highKey;
    }

    public void setHighKey(byte[] highKey) {
        this.highKey = highKey;
    }
}
