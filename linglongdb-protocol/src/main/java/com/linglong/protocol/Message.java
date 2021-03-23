package com.linglong.protocol;

import com.linglong.rpc.serialization.msgpack.BeanMessage;

/**
 * @author Stereo
 */
public abstract class Message implements BeanMessage {
    /* 请求/响应ID */
    private String id;
    /* 请求/响应时间 */
    private long timestamp;
    /* 事务id */
    private String xid;
    /* 消息处理错误码 */
    private int errorCode;
    /* 消息处理错误信息 */
    private String errorMessage;
    /* 操作类型 */
    private byte processType;

    public String getId() {
        return id;
    }

    public Message setId(String id) {
        this.id = id;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Message setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getXid() {
        return xid;
    }

    public Message setXid(String xid) {
        this.xid = xid;
        return this;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public Message setErrorCode(int errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Message setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public byte getProcessType() {
        return processType;
    }

    public Message setProcessType(byte processType) {
        this.processType = processType;
        return this;
    }
}
