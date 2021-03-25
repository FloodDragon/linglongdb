package com.linglong.protocol.message;

import com.linglong.rpc.common.utils.SystemClock;
import com.linglong.rpc.common.utils.UUID;
import com.linglong.rpc.serialization.msgpack.BeanMessage;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public class Request implements BeanMessage {
    /* 请求ID */
    private String id;
    /* 事务ID */
    private Long xid;
    /* 请求时间 */
    private long timestamp;

    public Request() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = SystemClock.now();
    }

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

    public Long getXid() {
        return xid;
    }

    public void setXid(Long xid) {
        this.xid = xid;
    }
}
