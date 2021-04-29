package com.linglong.server.database.process;


import com.linglong.replication.DatabaseReplicator;
import com.linglong.replication.Peer;
import com.linglong.rpc.client.ClientProxy;
import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.common.service.IService;
import com.linglong.server.config.RpcServerProperties;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * 集群组协调
 *
 * @author Stereo on 2021/3/17.
 */
public class LeaderCoordinator {

    /* 数据库复制器 */
    private final RpcServerProperties rpcServerProperties;
    private final DatabaseReplicator replicator;
    private final Map<Peer, LeaderContext> peerLeaderContextMap = new HashMap<>();

    class LeaderContext {
        private final Peer peer;
        private final int servicePort;
        private ClientProxy clientProxy;
        private final Map<Class<?>, IService> remoteService = new HashMap<>();
        private final Map<Method, IService> remoteMethodService = new HashMap<>();

        LeaderContext(int servicePort, Peer peer) {
            this.peer = peer;
            this.servicePort = servicePort;
        }

        synchronized void start() {
            InetSocketAddress address = (InetSocketAddress) peer.getAddress();
            Config config = new Config(address.getHostName(), servicePort);
            clientProxy = new ClientProxy(config);
            clientProxy.start();
        }

        synchronized void stop() {
            remoteService.clear();
            remoteMethodService.clear();
            if (clientProxy != null) {
                clientProxy.stop();
            }
        }

        <E extends IService> E getRemoteService(Class<E> serviceClazz) {
            E service;
            if (!remoteService.containsKey(serviceClazz)) {
                synchronized (this) {
                    if (!remoteService.containsKey(serviceClazz)) {
                        service = clientProxy.create(serviceClazz);
                        Method[] methods = serviceClazz.getDeclaredMethods();
                        for (Method m : methods) {
                            remoteMethodService.put(m, service);
                        }
                        remoteService.put(serviceClazz, service);
                    }
                }
            }
            return (E) remoteService.get(serviceClazz);
        }
    }


    LeaderCoordinator(DatabaseReplicator replicator,
                      RpcServerProperties rpcServerProperties) {
        this.replicator = replicator;
        this.rpcServerProperties = rpcServerProperties;
    }

    /**
     * coordinator  ->  判断读/写  ->   读 -> 走本地读
     * 写 ->   判断Leader 否 -> 走RPC 广播到Leader进行处理写
     * 是 -> 本地写
     * -> IndexController
     * -> LeaderCoordinator ( rpc client -> server ) -> CURD SERVICE
     * -> TableController
     * <p>
     * <p>
     * 协调器
     * 1.当前节点是否是Leader
     * 2.提供Leader peer client
     * 3.转发到leader
     */
    public boolean isNeedTransferToLeader() {
        boolean isLeader = replicator.isLocalLeader();
        return !isLeader;
    }

    /**
     * 获取Leader节点Service
     *
     * @param serviceClazz
     * @param <E>
     * @return
     * @throws Exception
     */
    public <E extends IService> E getLeaderService(Class<E> serviceClazz) throws Exception {
        Peer peer = replicator.getLeaderPeer();
        LeaderContext leaderContext;
        if (!peerLeaderContextMap.containsKey(peer)) {
            synchronized (peer) {
                if (!peerLeaderContextMap.containsKey(peer)) {
                    leaderContext = new LeaderContext(rpcServerProperties.getServerPort(), peer);
                    leaderContext.start();
                }
            }
        }
        leaderContext = peerLeaderContextMap.get(peer);
        return leaderContext.getRemoteService(serviceClazz);
    }
}
