package com.linglong.rpc.server.event;


import com.linglong.rpc.common.event.Event;
import com.linglong.rpc.common.remoting.Channel;
import com.linglong.rpc.common.utils.SystemClock;
import com.linglong.rpc.server.event.enums.ChannelInboundEnum;

/**
 * Created by liuj-ai on 2019/3/28.
 */
public class ChannelInboundEvent implements Event<ChannelInboundEnum> {
    private long timestamp;
    private String clientId;
    private Channel channel;
    private ChannelInboundEnum type;

    public ChannelInboundEvent(String clientId, Channel channel) {
        this.type = ChannelInboundEnum.EXPIRE;
        this.clientId = clientId;
        this.channel = channel;
        this.timestamp = SystemClock.now();
    }

    public String getClientId() {
        return clientId;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public ChannelInboundEnum getType() {
        return type;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ExpireEvent{" +
                "timestamp=" + timestamp +
                ", clientId='" + clientId + '\'' +
                ", channel=" + channel +
                '}';
    }
}
