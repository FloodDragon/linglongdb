package com.linglong.rpc.server.event;


import com.linglong.rpc.common.event.Event;
import com.linglong.rpc.common.protocol.Heartbeat;
import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.common.remoting.Channel;
import com.linglong.rpc.common.utils.SystemClock;
import com.linglong.rpc.server.event.enums.HeartbeatEnum;

/**
 * @author Stereo on 2019/3/28.
 */
public class HeartbeatEvent implements Event<HeartbeatEnum> {
    private long timestamp;
    private HeartbeatEnum type;
    private Packet packet;
    private Channel channel;

    public HeartbeatEvent(HeartbeatEnum type, Channel channel, Packet packet) {
        this(type, channel);
        this.packet = packet;
    }

    protected HeartbeatEvent(HeartbeatEnum type, Channel channel) {
        this.type = type;
        this.channel = channel;
        this.timestamp = SystemClock.now();
    }

    @Override
    public HeartbeatEnum getType() {
        return type;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public Heartbeat getHeartbeat() {
        return packet.getHeartbeat();
    }

    public Packet getPacket() {
        return packet;
    }

    public Channel getChannel() {
        return channel;
    }
}
