package com.linglong.server.database.process;


import com.linglong.replication.DatabaseReplicator;
import com.linglong.replication.Peer;
import com.linglong.rpc.client.ClientProxy;
import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.common.service.IService;
import com.linglong.server.config.RpcServerProperties;
import com.linglong.server.database.exception.NotLeaderException;
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

        LeaderContext(int servicePort, Peer peer) {
            this.peer = peer;
            this.servicePort = servicePort;
            init();
        }

        private synchronized void init() {
            InetSocketAddress address = (InetSocketAddress) peer.getAddress();
            Config config = new Config(address.getAddress().getHostAddress(), servicePort);
            clientProxy = new ClientProxy(config);
            clientProxy.start();
            peerLeaderContextMap.put(this.peer, this);
        }

        synchronized void destroy() {
            peerLeaderContextMap.remove(this.peer);
            if (clientProxy != null) {
                clientProxy.stop();
            }
        }

        <E extends IService> E getRemoteService(Class<E> serviceClazz) {
            if (clientProxy.isConnected()) {
                return clientProxy.create(serviceClazz);
            } else {
                throw new NotLeaderException("disconnect from leader node.");
            }
        }
    }

    LeaderCoordinator(DatabaseReplicator replicator, RpcServerProperties rpcServerProperties) {
        this.replicator = replicator;
        this.rpcServerProperties = rpcServerProperties;
    }

    public boolean isNeedTransferToLeader() {
        return !replicator.isLocalLeader();
    }

    public <E extends IService> E getLeaderService(Class<E> serviceClazz) throws Exception {
        Peer peer = replicator.getLeaderPeer();
        LeaderContext leaderContext;
        if (!peerLeaderContextMap.containsKey(peer)) {
            synchronized (peer) {
                if (!peerLeaderContextMap.containsKey(peer)) {
                    new LeaderContext(rpcServerProperties.getServerPort(), peer);
                }
            }
        }
        leaderContext = peerLeaderContextMap.get(peer);
        return leaderContext.getRemoteService(serviceClazz);
    }

    synchronized void reset() {
        if (!peerLeaderContextMap.isEmpty()) {
            for (Map.Entry<Peer, LeaderContext> entry : peerLeaderContextMap.entrySet()) {
                entry.getValue().destroy();
            }
            peerLeaderContextMap.clear();
        }
    }
}
