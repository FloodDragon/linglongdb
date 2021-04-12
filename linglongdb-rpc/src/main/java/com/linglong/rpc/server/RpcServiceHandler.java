package com.linglong.rpc.server;


import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.common.config.Constants;
import com.linglong.rpc.common.event.Dispatcher;
import com.linglong.rpc.common.event.EventHandler;
import com.linglong.rpc.exception.RpcException;
import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.common.remoting.Channel;
import com.linglong.rpc.common.remoting.ChannelHandler;
import com.linglong.rpc.common.remoting.RpcChannel;
import com.linglong.rpc.common.utils.NetUtil;
import com.linglong.rpc.server.event.ChannelInboundEvent;
import com.linglong.rpc.server.event.DataStreamRequestEvent;
import com.linglong.rpc.server.event.HeartbeatEvent;
import com.linglong.rpc.server.event.RequestEvent;
import com.linglong.rpc.server.event.enums.ChannelInboundEnum;
import com.linglong.rpc.server.event.enums.HeartbeatEnum;
import com.linglong.rpc.server.skeleton.service.ServiceEventHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Stereo
 * @version 2013.12.19
 */
@io.netty.channel.ChannelHandler.Sharable
public class RpcServiceHandler extends ChannelInboundHandlerAdapter implements ChannelHandler, EventHandler<ChannelInboundEvent> {

    private static Logger LOG = LoggerFactory.getLogger(RpcServiceHandler.class);
    private Config config;
    private Dispatcher dispatcher;
    private ServiceEventHandler serviceHandler;
    private final Map<String, Channel> channels = new ConcurrentHashMap<>(); // <ip:port, channel>

    public Map<String, Channel> getChannels() {
        return channels;
    }

    public RpcServiceHandler(RpcServiceServer rpcServiceServer) {
        this.config = rpcServiceServer.getConfig();
        this.dispatcher = rpcServiceServer.getServiceContext().getDispatcher();
        this.dispatcher.register(ChannelInboundEnum.class, this);
        this.serviceHandler = rpcServiceServer.getServiceContext().getServiceHandler();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        RpcChannel channel = RpcChannel.getOrAddChannel(ctx.channel(), config, this);
        try {
            connected(channel);
        } finally {
            RpcChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        RpcChannel channel = RpcChannel.getOrAddChannel(ctx.channel(), config, this);
        try {
            disconnected(channel);
        } finally {
            RpcChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        RpcChannel channel = RpcChannel.getOrAddChannel(ctx.channel(), config, this);
        try {
            received(channel, message);
        } finally {
            RpcChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        RpcChannel channel = RpcChannel.getOrAddChannel(ctx.channel(), config, this);
        try {
            caught(channel, cause);
        } finally {
            RpcChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void connected(Channel channel) throws RpcException {
        if (channel != null) {
            channels.put(NetUtil.toAddressString(channel.getRemoteAddress()), channel);
        }
        LOG.info("RpcServiceHandler channel:{} connected ", channel);
    }

    @Override
    public void disconnected(Channel channel) throws RpcException {
        if (channel != null) {
            Channel actualChannel = channels.remove(NetUtil.toAddressString(channel.getRemoteAddress()));
            if (actualChannel != null) {
                actualChannel.closeChannel();
                dispatcher.getEventHandler().handle(new HeartbeatEvent(HeartbeatEnum.UNREGISTER, channel, new Packet()));
                LOG.info("RpcServiceHandler channel:{} disconnected ", channel);
            }
        } else
            LOG.error("RpcServiceHandler channel null");
    }

    @Override
    public void sent(Channel channel, Object message) throws RpcException {
        channel.send(message);
    }

    @Override
    public void received(Channel channel, Object message) throws RpcException {
        try {
            if (message != null && message instanceof Packet) {
                final Packet packet = (Packet) message;
                byte type = packet.getType();
                switch (type) {
                    case Constants.TYPE_REQUEST:
                        serviceHandler.handleRequest(new RequestEvent(packet, channel));
                        break;
                    case Constants.TYPE_RESPONSE:
                        //不支持
                        throw new RpcException("rpc server received <RESPONSE> operation is not supported");
                    case Constants.TYPE_DATA_STREAM_REQUEST:
                        serviceHandler.handleRequest(new DataStreamRequestEvent(packet, channel));
                        break;
                    case Constants.TYPE_DATA_STREAM:
                        //不支持
                        throw new RpcException("rpc server received <TYPE_DATA_STREAM> operation is not supported");
                    case Constants.TYPE_DATA_STREAM_RESPONSE:
                        //不支持
                        throw new RpcException("rpc server received <TYPE_DATA_STREAM_RESPONSE> operation is not supported");
                    case Constants.TYPE_HEARTBEAT_REQUEST_REGISTER:
                        dispatcher.getEventHandler().handle(new HeartbeatEvent(HeartbeatEnum.REGISTER, channel, packet));
                        break;
                    case Constants.TYPE_HEARTBEAT:
                        dispatcher.getEventHandler().handle(new HeartbeatEvent(HeartbeatEnum.HEARTBEAT, channel, packet));
                        break;
                    case Constants.TYPE_HEARTBEAT_REQUEST_UNREGISTER:
                        dispatcher.getEventHandler().handle(new HeartbeatEvent(HeartbeatEnum.UNREGISTER, channel, packet));
                        break;
                    default:
                        LOG.error("RpcServiceHandler received error message:{} ", message);
                }
            } else
                LOG.error("RpcServiceHandler channelRead error message:{}", message);
        } catch (Exception e) {
            LOG.error("RpcServiceHandler handle packet:{} error", message, e);
        }
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RpcException {
        LOG.error("RpcServiceHandler channel:{} caught ", channel, exception);
    }

    @Override
    public void handle(ChannelInboundEvent event) {
        try {
            disconnected(event.getChannel());
            LOG.info("RpcServiceHandler handle expire {}", event);
        } catch (Exception ex) {
            LOG.error("RpcServiceHandler handle expire event error {}", event, ex);
        }
    }
}