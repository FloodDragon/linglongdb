package com.linglong.rpc.server.skeleton.liveliness;

import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.common.config.Constants;
import com.linglong.rpc.common.event.Dispatcher;
import com.linglong.rpc.common.event.EventHandler;
import com.linglong.rpc.exception.RpcException;
import com.linglong.rpc.common.protocol.Heartbeat;
import com.linglong.rpc.common.remoting.Channel;
import com.linglong.base.utils.SystemClock;
import com.linglong.base.utils.ThreadPoolUtils;
import com.linglong.rpc.server.event.ChannelInboundEvent;
import com.linglong.rpc.server.event.HeartbeatEvent;
import com.linglong.rpc.server.event.enums.HeartbeatEnum;
import com.linglong.rpc.server.skeleton.liveliness.listener.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author Stereo on 2019/3/28.
 */
public class Liveliness extends AbstractLivelinessMonitor<Channel> implements EventHandler<HeartbeatEvent> {

    private static Logger LOG = LoggerFactory.getLogger(Liveliness.class);

    private int expireIntvl;
    private Dispatcher dispatcher;
    private ExecutorService worker = ThreadPoolUtils.newFixedThreadPool(5);
    private ClientListenerRegistryImpl clientListenerRegistry = new ClientListenerRegistryImpl();
    private Map<String, Channel> heartbeatChannelMap = new ConcurrentHashMap<>();//clientId:Channel

    public Liveliness(Config config, Dispatcher dispatcher) {
        super("Liveliness");
        this.dispatcher = dispatcher;
        this.expireIntvl = config.getHeartBeatExpireInterval();
    }

    @Override
    public ClientListenerRegistry getClientListenerRegistry() {
        return clientListenerRegistry;
    }

    @Override
    public Collection<Channel> living() {
        return Collections.unmodifiableCollection(heartbeatChannelMap.values());
    }

    @Override
    protected void serviceInit() throws Exception {
        setExpireInterval(expireIntvl);
        setMonitorInterval(expireIntvl / 3);
    }

    @Override
    protected void serviceStop() throws Exception {
        super.serviceStop();
        worker.shutdownNow();
    }

    @Override
    protected synchronized void expire(Channel channel) {
        //获取客户端Id
        String clientId = getClientId(channel);
        //删除心跳通道
        heartbeatChannelMap.remove(clientId);
        dispatcher.getEventHandler().handle(new ChannelInboundEvent(clientId, channel));
        ClientLiveExpiredListener clientLiveExpiredListener = clientListenerRegistry.getClientLiveExpiredListener();
        if (clientLiveExpiredListener != null) {
            worker.execute(() -> clientLiveExpiredListener.expired(clientId, channel));
        }
    }

    protected synchronized void register(Heartbeat heartbeat, Channel channel) {
        super.register(channel);
        String clientId = heartbeat.getClient_id();
        setClientId(channel, clientId);
        heartbeatChannelMap.put(clientId, channel);
        ClientRegisterListener clientRegisterListener = clientListenerRegistry.getClientRegisterListener();
        if (clientRegisterListener != null) {
            worker.execute(() -> clientRegisterListener.registered(clientId, channel));
        }
    }

    protected synchronized void unregister(Heartbeat heartbeat) {
        String clientId = heartbeat.getClient_id();
        Channel channel = heartbeatChannelMap.remove(clientId);
        unregister(clientId, channel);
    }

    protected synchronized void unregister(String clientId, Channel channel) {
        super.unregister(channel);
        ClientUnregisterListener clientUnregisterListener = clientListenerRegistry.getClientUnregisterListener();
        if (clientUnregisterListener != null) {
            worker.execute(() -> clientUnregisterListener.unregistered(clientId, channel));
        }
    }

    protected synchronized void heartbeat(Heartbeat heartbeat, Channel channel) {
        String clientId = heartbeat.getClient_id();
        if (running.containsKey(channel) && heartbeatChannelMap.containsKey(clientId)) {
            receivedPing(channel);
            ClientHeartbeatBodyListener clientHeartbeatBodyListener = clientListenerRegistry.getClientHeartbeatBodyListener();
            if (heartbeat.getBody() != null && clientHeartbeatBodyListener != null) {
                worker.execute(() -> clientHeartbeatBodyListener.process(clientId, heartbeat.getBody()));
            }
        } else {
            register(heartbeat, channel);
        }
    }

    @Override
    public void handle(HeartbeatEvent event) {
        Channel channel = event.getChannel();
        HeartbeatEnum type = event.getType();
        Heartbeat heartbeat = event.getHeartbeat();
        try {
            switch (type) {
                case REGISTER:
                    register(heartbeat, channel);
                    break;
                case UNREGISTER:
                    if (heartbeat != null) {
                        unregister(heartbeat);
                        break;
                    } else {
                        String clientId = getClientId(channel);
                        if (StringUtils.isNotBlank(clientId))
                            unregister(clientId, channel);
                        else
                            LOG.error("unregister client id not found channel {}", channel);
                        return;
                    }
                case HEARTBEAT:
                    heartbeat(heartbeat, channel);
                    break;
            }
            heartbeat.setServer_time(SystemClock.now());
            event.getChannel().send(event.getPacket(), true);
        } catch (RpcException e) {
            LOG.error("{} reply heartbeat failed", getName());
        }
    }

    private String getClientId(Channel channel) {
        return (String) channel.getAttribute(Constants.CHANNEL_CLIENT_ID);
    }

    private void setClientId(Channel channel, String clientId) {
        channel.setAttribute(Constants.CHANNEL_CLIENT_ID, clientId);
    }
}
