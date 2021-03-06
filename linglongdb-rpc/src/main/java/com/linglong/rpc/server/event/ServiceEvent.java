package com.linglong.rpc.server.event;


import com.linglong.rpc.common.event.Event;
import com.linglong.rpc.common.remoting.Channel;
import com.linglong.base.utils.SystemClock;
import com.linglong.rpc.server.event.enums.ServiceEnum;

/**
 * @author Stereo on 2019/3/28.
 */
public class ServiceEvent<T> implements Event<ServiceEnum> {
    private T target;
    private long timestamp;
    private ServiceEnum type;
    private Channel channel;

    public ServiceEvent(T target, ServiceEnum type, Channel channel) {
        this.timestamp = SystemClock.now();
        this.target = target;
        this.type = type;
        this.channel = channel;
    }

    @Override
    public ServiceEnum getType() {
        return type;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public T getTarget() {
        return target;
    }

    public Channel getChannel() {
        return channel;
    }
}
