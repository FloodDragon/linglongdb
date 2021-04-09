package com.linglong.rpc.server.event;

import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.common.remoting.Channel;
import com.linglong.rpc.server.event.enums.ServiceEnum;

/**
 * Created by liuj-ai on 2021/4/9.
 */
public class DataStreamRequestEvent extends RequestEvent {

    public DataStreamRequestEvent(Packet target, Channel channel) {
        super(target, channel);
    }

    @Override
    public ServiceEnum getType() {
        return ServiceEnum.DATA_STREAM_REQUEST;
    }
}
