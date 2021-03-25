package com.linglong.protocol.message;

import com.linglong.rpc.serialization.msgpack.BeanMessage;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public class CountResponse extends Response implements BeanMessage {

    private long count;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
