package com.linglong.protocol.message;


/**
 * @author Stereo on 2021/3/15.
 */
public class KeyValueRequest extends IndexRequest {
    /* 索引键 */
    private byte[] key;
    /* 索引值 */
    private byte[] value;
    /* 索引旧值 */
    private byte[] oldValue;

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

    public byte[] getOldValue() {
        return oldValue;
    }

    public void setOldValue(byte[] oldValue) {
        this.oldValue = oldValue;
    }
}
