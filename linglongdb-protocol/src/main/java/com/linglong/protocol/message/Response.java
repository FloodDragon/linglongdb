package com.linglong.protocol.message;

import com.linglong.rpc.common.utils.SystemClock;
import com.linglong.rpc.serialization.msgpack.BeanMessage;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public class Response implements BeanMessage {

    /* 响应ID */
    private String id;
    /* 事务ID */
    private Long xid;
    /* 错误码 */
    private int errorCode;
    /* 错误信息 */
    private String errorMessage;
    /* 响应时间 */
    private long timestamp;

    public Response() {
        this.timestamp = SystemClock.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getXid() {
        return xid;
    }

    public void setXid(Long xid) {
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
