package com.linglong.rpc.common.codec;

import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.serialization.msgpack.MessagePack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * MessagePack 解码器, 基于长度的拆包
 * <p>
 * @author Stereo
 */
public class MsgPackDecoder extends LengthFieldBasedFrameDecoder {

    private final MessagePack messagePack = new MessagePack();

    public MsgPackDecoder(int maxFrameLength) {
        super(maxFrameLength, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        ByteBufInputStream bin = new ByteBufInputStream(frame);
        int len = bin.available();
        if (len > 0) {
            byte[] data = new byte[len];
            bin.read(data, 0, len);
            Packet packet = messagePack.read(data, Packet.class);
            bin.close();
            return packet;
        } else
            return null;
    }

    @Override
    protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        return buffer.slice(index, length);
    }
}
