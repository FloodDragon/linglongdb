package com.linglong.rpc.common.codec;

import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.serialization.msgpack.MessagePack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * MessagePack编码器,
 *
 * @注意 前4位代表数据包大小, 剩下其余都是数据包, 防止粘包/断保
 * <p>
 * Created by liuj-ai on 2019/11/15.
 */
public class MsgPackEncoder extends MessageToByteEncoder<Packet> {

    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    private final MessagePack messagePack = new MessagePack();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf out) throws Exception {
        int startIdx = out.writerIndex();
        ByteBufOutputStream bout = new ByteBufOutputStream(out);
        bout.write(LENGTH_PLACEHOLDER);
        byte[] data = messagePack.write(packet);
        bout.write(data);
        bout.flush();
        bout.close();
        int endIdx = out.writerIndex();
        out.setInt(startIdx, endIdx - startIdx - 4);
    }
}
