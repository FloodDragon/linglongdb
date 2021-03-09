package com.linglong.protocol.message;

import com.linglong.protocol.Message;

/**
 * Created by liuj-ai on 2021/3/9.
 */
public class InsertKeyValueRequest extends Message {
    /* 索引名称 */
    private String index;
    private byte[] key;
    private byte[] value;
    /* 事务id */
    private String xid;
    /* 是否开启事务 */
    private boolean isOpenTx;

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

    public boolean isOpenTx() {
        return isOpenTx;
    }

    public void setOpenTx(boolean openTx) {
        isOpenTx = openTx;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }
}
