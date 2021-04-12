package com.linglong.rpc.server.event;

import com.linglong.rpc.common.config.Constants;
import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.common.remoting.Channel;
import com.linglong.rpc.server.event.enums.ServiceEnum;

/**
 * Created by liuj-ai on 2021/4/9.
 */
public class DataStreamResponseEvent extends ResponseEvent {

    public DataStreamResponseEvent(Packet target, Channel channel) {
        super(target, channel);
        target.setType(Constants.TYPE_DATA_STREAM_RESPONSE);
    }

    @Override
    public ServiceEnum getType() {
        return ServiceEnum.DATA_STREAM_RESPONSE;
    }
}
