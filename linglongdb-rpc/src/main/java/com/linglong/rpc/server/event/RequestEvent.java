package com.linglong.rpc.server.event;


import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.common.remoting.Channel;
import com.linglong.rpc.server.event.enums.ServiceEnum;

/**
 * Created by liuj-ai on 2019/3/28.
 */
public class RequestEvent extends ServiceEvent<Packet> {
    public RequestEvent(Packet target, Channel channel) {
        super(target, ServiceEnum.REQUEST, channel);
    }
}
