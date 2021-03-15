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
    /* 消息版本 */
    private String version;
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

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public byte getProcessType() {
        return processType;
    }

    public void setProcessType(byte processType) {
        this.processType = processType;
    }
}
