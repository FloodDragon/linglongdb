package com.linglong.rpc.server.event;


import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.common.remoting.Channel;
import com.linglong.rpc.server.event.enums.ServiceEnum;

/**
 * @author Stereo on 2019/3/28.
 */
public class ResponseEvent extends ServiceEvent<Packet> {
    public ResponseEvent(Packet target, Channel channel) {
        super(target, ServiceEnum.RESPONSE, channel);
    }
}
