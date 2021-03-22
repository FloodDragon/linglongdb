package com.linglong.protocol.message;

import com.linglong.protocol.Message;

/**
 * @author Stereo on 2021/3/9.
 */
public class QueryResponse extends Message {
    /* 索引名称 */
    private String index;
    /* 索引键 */
    private byte[] key;
    /* 索引值 */
    private byte[] value;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

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
