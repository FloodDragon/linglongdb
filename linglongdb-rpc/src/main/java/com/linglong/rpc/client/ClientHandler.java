package com.linglong.rpc.client;

import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.common.remoting.RpcChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stereo on 2019/12/10.
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private static Logger LOG = LoggerFactory.getLogger(ClientHandler.class);

    Config config;
    AbstractClient client;

    protected ClientHandler(AbstractClient client, Config config) {
        this.client = client;
        this.config = config;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        RpcChannel channel = RpcChannel.getOrAddChannel(ctx.channel(), config, client);
        if (channel != null) {
            channel.disconnected(channel);
        } else
            LOG.warn("ClientHandler channelInactive channel is null");
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        RpcChannel channel = RpcChannel.getOrAddChannel(ctx.channel(), config, client);
        if (channel != null) {
            channel.connected(channel);
        } else
            LOG.warn("ClientHandler channelActive channel is null");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //LOG.debug("ClientHandler.channelRead msg is " + msg);
        //super.channelRead(ctx,msg);
        RpcChannel channel = RpcChannel.getOrAddChannel(ctx.channel(), config, client);
        if (channel != null) {
            channel.received(channel, msg);
        } else
            LOG.warn("ClientHandler channelRead channel is null");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("ClientHandler.exceptionCaught", cause);
        super.exceptionCaught(ctx, cause);
        RpcChannel channel = RpcChannel.getOrAddChannel(ctx.channel(), config, client);
        if (channel != null) {
            channel.caught(channel, cause);
        } else
            LOG.warn("ClientHandler exceptionCaught channel is null");
    }
}