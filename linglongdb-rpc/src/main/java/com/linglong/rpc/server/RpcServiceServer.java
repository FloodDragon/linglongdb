package com.linglong.rpc.server;


import com.linglong.rpc.common.codec.MsgPackDecoder;
import com.linglong.rpc.common.codec.MsgPackEncoder;
import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.exception.RpcException;
import com.linglong.rpc.common.life.AbstractService;
import com.linglong.rpc.common.life.LifeService;
import com.linglong.rpc.common.utils.NetUtil;
import com.linglong.rpc.server.skeleton.AbstractSkeletonContext;
import com.linglong.rpc.server.skeleton.SkeletonContext;
import com.linglong.rpc.server.skeleton.liveliness.ILiveliness;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.linglong.rpc.common.remoting.ChannelHandler;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * 自研RPC服务器
 *
 * @author Stereo
 * @version 2013.12.19
 */
public class RpcServiceServer extends AbstractService implements Server {

    private static Logger LOG = LoggerFactory.getLogger(RpcServiceServer.class);
    private Config config;
    private Channel channel;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private AbstractSkeletonContext serviceContext;
    private RpcServiceRegistry registry;
    private volatile boolean closed;
    private RpcServiceHandler rpcServiceHandler;
    private Map<String, com.linglong.rpc.common.remoting.Channel> channels;

    public RpcServiceServer() {
        this(new Config());
    }

    public RpcServiceServer(Config config) {
        super("RpcServiceServer" + ":" + config.getRemoteAddress().toString());
        this.config = config;
        this.serviceContext = new SkeletonContext(config);
        this.registry = new RpcServiceRegistry(serviceContext);
    }

    @Override
    protected void serviceInit() throws Exception {
        //业务上下文
        ((LifeService) serviceContext).init();
        final SslContext sslCtx;
        if (config.isSsl()) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }
        Class clazz;
        if (config.isUseEpoll()) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup(config.getChildNioEventThreads());
            clazz = EpollServerSocketChannel.class;
        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(config.getChildNioEventThreads());
            clazz = NioServerSocketChannel.class;
        }
        rpcServiceHandler = new RpcServiceHandler(this);
        channels = rpcServiceHandler.getChannels();
        bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(clazz)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
                .option(ChannelOption.SO_LINGER, config.getSoLinger())
                .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_SNDBUF, config.getSendBufferSize())
                .option(ChannelOption.SO_RCVBUF, config.getReceiveBufferSize())
                .localAddress(config.getRemoteAddress())
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc()));
                        }
                        p.addLast("decoder", new MsgPackEncoder())
                                .addLast("encoder", new MsgPackDecoder(config.getPayload()))
                                .addLast("handler", rpcServiceHandler);
                    }
                });
    }

    @Override
    protected void serviceStart() throws Exception {
        serviceInit();
        if (serviceContext != null)
            ((LifeService) serviceContext).start();
        if (bootstrap != null) {
            channel = bootstrap.bind(config.getHost(), config.getPort()).sync().channel();
        }
        closed = false;
    }

    @Override
    protected void serviceStop() throws Exception {
        if (serviceContext != null)
            ((LifeService) serviceContext).stop();
        if (bootstrap != null && channel != null && bossGroup != null && workerGroup != null) {
            channel.close().sync();
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            bootstrap = null;
            channel = null;
            bossGroup = null;
            workerGroup = null;
        }
        closed = true;
    }

    public RpcServiceRegistry getRpcRegistry() {
        return registry;
    }

    public Config getConfig() {
        return config;
    }

    protected AbstractSkeletonContext getServiceContext() {
        return serviceContext;
    }

    public ILiveliness<com.linglong.rpc.common.remoting.Channel> liveliness() {
        return serviceContext.getLiveliness();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return rpcServiceHandler;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return config.getRemoteAddress();
    }

    @Override
    public void send(Object message) throws RpcException {
        send(message, true);
    }

    @Override
    public void send(Object message, boolean sent) throws RpcException {
        Collection<com.linglong.rpc.common.remoting.Channel> channels = getChannels();
        for (com.linglong.rpc.common.remoting.Channel channel : channels) {
            if (channel.isConnected()) {
                channel.send(message, sent);
            }
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public Collection<com.linglong.rpc.common.remoting.Channel> getChannels() {
        Collection<com.linglong.rpc.common.remoting.Channel> chs = new HashSet<>();
        for (com.linglong.rpc.common.remoting.Channel channel : this.channels.values()) {
            if (channel.isConnected()) {
                chs.add(channel);
            } else {
                channels.remove(NetUtil.toAddressString(channel.getRemoteAddress()));
            }
        }
        return chs;
    }

    @Override
    public com.linglong.rpc.common.remoting.Channel getChannel(InetSocketAddress remoteAddress) {
        return channels.get(NetUtil.toAddressString(remoteAddress));
    }

    @Override
    public boolean isBound() {
        return channel.isActive();
    }
}
