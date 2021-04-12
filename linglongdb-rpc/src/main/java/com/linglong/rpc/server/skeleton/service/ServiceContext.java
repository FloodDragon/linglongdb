package com.linglong.rpc.server.skeleton.service;


import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.common.remoting.Channel;

/**
 * Created by LiuJing on 16-8-17.
 */
public class ServiceContext {
    private Packet _request;
    private Channel _channel;
    private DataStreamTransfer _dataStreamTransfer;
    private static final ThreadLocal<ServiceContext> _localContext = new ThreadLocal<ServiceContext>();

    protected static void begin(Packet request, Channel channel) {
        begin(request, channel, null);
    }

    protected static void begin(Packet request, Channel channel, DataStreamTransfer dataStreamTransfer) {
        ServiceContext context = (ServiceContext) _localContext.get();
        if (context == null) {
            context = new ServiceContext();
            _localContext.set(context);
        }
        context._request = request;
        context._channel = channel;
        context._dataStreamTransfer = dataStreamTransfer;
    }

    protected static void end() {
        ServiceContext context = (ServiceContext) _localContext.get();
        if (context != null) {
            context._request = null;
            context._channel = null;
            context._dataStreamTransfer = null;
            _localContext.set(null);
        }
    }

    public static Packet getRequestPacket() {
        ServiceContext context = (ServiceContext) _localContext.get();

        if (context != null)
            return context._request;
        else
            return null;
    }

    public static Channel getChannelHandlerContext() {
        ServiceContext context = (ServiceContext) _localContext.get();
        if (context != null)
            return context._channel;
        else
            return null;
    }

    public static Channel getChannel() {
        ServiceContext context = (ServiceContext) _localContext.get();
        if (context != null)
            return context._channel;
        else
            return null;
    }

    public static DataStreamTransfer getDataStreamTransfer() {
        ServiceContext context = (ServiceContext) _localContext.get();
        if (context != null)
            return context._dataStreamTransfer;
        else
            return null;
    }
}
