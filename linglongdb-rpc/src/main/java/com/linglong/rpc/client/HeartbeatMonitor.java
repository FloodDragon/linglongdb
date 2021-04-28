package com.linglong.rpc.client;

import com.linglong.rpc.common.config.Constants;
import com.linglong.rpc.exception.RpcException;
import com.linglong.rpc.common.life.AbstractService;
import com.linglong.rpc.common.protocol.Heartbeat;
import com.linglong.rpc.common.protocol.Packet;
import com.linglong.base.utils.Daemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 心跳监测器
 * <p>
 * @author Stereo
 */
public final class HeartbeatMonitor extends AbstractService implements Runnable, HeartbeatPostMan, HeartBeatState {

    private static Logger LOG = LoggerFactory.getLogger(HeartbeatMonitor.class);
    private Daemon thread;
    private AbstractClient client;
    final int heartBeatRate;
    final int heartbeatQuantity;
    volatile boolean running;
    volatile Heartbeat heartbeat;
    volatile int wrapFailed;
    volatile int wrapSucceed;
    volatile State state;
    final FailoverHandler failoverHandler;
    final Queue<String> maildelivery;

    public HeartbeatMonitor(AbstractClient _client) {
        super("HeartbeatReport");
        client = _client;
        maildelivery = new ConcurrentLinkedQueue<>();
        heartbeat = new Heartbeat(_client.getClientId());
        heartBeatRate = _client.getConfig().getHeartBeatRate();
        failoverHandler = _client.getFailoverHandler();
        heartbeatQuantity = _client.getConfig().getHeartBeatQuantity();
    }

    @Override
    public void run() {
        while (running) {
            try {
                heartbeat();
                Thread.sleep(heartBeatRate);
            } catch (InterruptedException ex) {
                LOG.info(getName() + " thread interrupted.");
            } catch (Exception ex) {
                LOG.error(getName() + " error.", ex);
            }
        }
    }

    @Override
    protected void serviceInit() throws Exception {
        LOG.info(getName() + " init.");
    }

    @Override
    protected void serviceStart() throws Exception {
        reportHeartBeat(Constants.TYPE_HEARTBEAT_REQUEST_REGISTER);
        running = true;
        thread = new Daemon(this);
        state = State.BORN;
        thread.start();
    }

    @Override
    protected void serviceStop() throws Exception {
        running = false;
        thread.interrupt();
        state = State.CEASE;
        reportHeartBeat(Constants.TYPE_HEARTBEAT_REQUEST_UNREGISTER);
    }

    void heartbeat() {
        heartbeat.now();
        heartbeat.setBody(!maildelivery.isEmpty() ? maildelivery.poll() : null);
        reportHeartBeat(Constants.TYPE_HEARTBEAT);
    }

    void reportHeartBeat(byte type) {
        if (state == State.CEASE) {
            LOG.error("{} state cease.", getName());
            return;
        } else if (state == State.RECOVERY) {
            LOG.info("{} recovering.", getName());
            try {
                if (failoverHandler != null) {
                    failoverHandler.failover(client);
                }
                client.doReconnect();
                LOG.info("{} recovered.", getName());
                state = State.BORN;
            } catch (RpcException ex) {
                LOG.error(getName() + " recover failed will continue.");
            }
            return;
        } else {
            try {
                AsyncFuture<Packet> future = client.sendPacket(Packet.packetHeartBeat(heartbeat, type));
                try {
                    heartbeat = future.get(client.getConfig().getReadTimeout(), TimeUnit.MILLISECONDS).getHeartbeat();
                } catch (InterruptedException | TimeoutException ex) {
                    future.done(null);
                }

                client.serverTimestamp = heartbeat.getServer_time();
                wrapFailed = 0;
                wrapSucceed++;
                //如果连续成功心跳次数超过阈值, 则认为当前连接状态健康.
                if (wrapSucceed >= heartbeatQuantity) {
                    state = State.HEALTHY;
                }
                //否则则认为当前连接状态.
                else {
                    state = State.CONNECTED;
                }
            } catch (Exception ex) {

                wrapSucceed = 0;
                wrapFailed++;
                //如果连续失败心跳次数超过阈值, 则认为当前连接需要恢复了.
                if (wrapFailed >= heartbeatQuantity) {
                    LOG.error(getName() + " reportHeartBeat fail and reaching reconnect condition.");
                    state = State.RECOVERY;
                }
                //否则则认为当前连接已丢失.
                else {
                    state = State.LOST;
                    LOG.error(getName() + " reportHeartBeat lost.");
                }
            }
        }
    }

    public State getState() {
        return state;
    }

    @Override
    public boolean deliver(String message) {
        return maildelivery.offer(message);
    }
}