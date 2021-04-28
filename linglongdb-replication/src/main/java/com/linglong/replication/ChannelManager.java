
package com.linglong.replication;

import com.linglong.base.concurrent.Latch;
import com.linglong.io.*;
import com.linglong.base.common.Utils;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Consumer;
import java.util.function.LongPredicate;
import java.util.zip.Checksum;

/**
 * 不同类型客户端管理
 * 复制通道管理
 *
 * @author Stereo
 */
final class ChannelManager {
    static final long MAGIC_NUMBER = 480921776540805866L;

    static final int TYPE_CONTROL = 0, TYPE_PLAIN = 1, TYPE_JOIN = 2, TYPE_SNAPSHOT = 3;

    private static final int CONNECT_TIMEOUT_MILLIS = 5000;
    private static final int MIN_RECONNECT_DELAY_MILLIS = 10;
    private static final int MAX_RECONNECT_DELAY_MILLIS = 1000;
    private static final int INITIAL_READ_TIMEOUT_MILLIS = 1000;
    private static final int WRITE_CHECK_DELAY_MILLIS = 125;
    private static final int INIT_HEADER_SIZE = 40;

    private static final int
            OP_NOP = 0,  //OP_NOP_REPLY = 1,
            OP_REQUEST_VOTE = 2, OP_REQUEST_VOTE_REPLY = 3,
            OP_QUERY_TERMS = 4, OP_QUERY_TERMS_REPLY = 5,
            OP_QUERY_DATA = 6, OP_QUERY_DATA_REPLY = 7,
            OP_WRITE_DATA = 8, OP_WRITE_DATA_REPLY = 9,
            OP_SYNC_COMMIT = 10, OP_SYNC_COMMIT_REPLY = 11,
            OP_COMPACT = 12, //OP_COMPACT_REPLY = 13,
            OP_SNAPSHOT_SCORE = 14, OP_SNAPSHOT_SCORE_REPLY = 15,
            OP_UPDATE_ROLE = 16, OP_UPDATE_ROLE_REPLY = 17,
            OP_GROUP_VERSION = 18, OP_GROUP_VERSION_REPLY = 19,
            OP_GROUP_FILE = 20, OP_GROUP_FILE_REPLY = 21;

    private final Scheduler mScheduler;
    private final long mGroupToken;
    private final Map<SocketAddress, Peer> mPeerMap;
    private final TreeSet<Peer> mPeerSet;
    private final Set<SocketChannel> mChannels;

    private long mGroupId;
    private long mLocalMemberId;
    private ServerSocket mServerSocket;

    private Channel mLocalServer;

    private volatile Consumer<Socket> mSocketAcceptor;
    private volatile Consumer<Socket> mJoinAcceptor;
    private volatile Consumer<Socket> mSnapshotRequestAcceptor;

    volatile boolean mPartitioned;

    ChannelManager(Scheduler scheduler, long groupToken, long groupId) {
        if (scheduler == null) {
            throw new IllegalArgumentException();
        }
        mScheduler = scheduler;
        mGroupToken = groupToken;
        mPeerMap = new HashMap<>();
        mPeerSet = new TreeSet<>((a, b) -> Long.compare(a.mMemberId, b.mMemberId));
        mChannels = new HashSet<>();
        setGroupId(groupId);
    }

    static ServerSocket newServerSocket(SocketAddress listenAddress) throws IOException {
        ServerSocket ss = new ServerSocket();
        try {
            ss.setReuseAddress(true);
            ss.bind(listenAddress);
            return ss;
        } catch (Throwable e) {
            Utils.closeQuietly(ss);
            throw e;
        }
    }

    synchronized void setLocalMemberId(long localMemberId, ServerSocket ss)
            throws IOException {
        if (localMemberId == 0 || ss == null) {
            throw new IllegalArgumentException();
        }
        if (mLocalMemberId != 0) {
            throw new IllegalStateException();
        }
        mLocalMemberId = localMemberId;
        mServerSocket = ss;
    }

    long getGroupToken() {
        return mGroupToken;
    }

    synchronized long getGroupId() {
        return mGroupId;
    }

    synchronized void setGroupId(long groupId) {
        mGroupId = groupId;
    }

    synchronized long getLocalMemberId() {
        return mLocalMemberId;
    }

    synchronized boolean isStarted() {
        return mLocalServer != null;
    }

    synchronized boolean start(Channel localServer) throws IOException {
        if (localServer == null) {
            throw new IllegalArgumentException();
        }

        if (mLocalMemberId == 0) {
            throw new IllegalStateException();
        }

        if (mLocalServer != null) {
            return false;
        }

        execute(this::acceptLoop);

        mLocalServer = localServer;

        // Start task.
        checkWrites();

        return true;
    }

    void socketAcceptor(Consumer<Socket> acceptor) {
        mSocketAcceptor = acceptor;
    }

    void joinAcceptor(Consumer<Socket> acceptor) {
        mJoinAcceptor = acceptor;
    }

    void snapshotRequestAcceptor(Consumer<Socket> acceptor) {
        mSnapshotRequestAcceptor = acceptor;
    }

    boolean hasSnapshotRequestAcceptor() {
        return mSnapshotRequestAcceptor != null;
    }

    synchronized boolean stop() {
        if (mServerSocket == null) {
            return false;
        }

        Utils.closeQuietly(mServerSocket);
        mServerSocket = null;

        mLocalServer = null;

        for (SocketChannel channel : mChannels) {
            channel.disconnect();
        }

        mChannels.clear();

        return true;
    }

    synchronized boolean isStopped() {
        return mServerSocket == null;
    }

    synchronized void partitioned(boolean enable) {
        mPartitioned = enable;

        if (enable) {
            for (SocketChannel channel : mChannels) {
                channel.closeSocket();
            }
        }
    }

    private void execute(Runnable task) {
        if (!mScheduler.execute(task)) {
            stop();
        }
    }

    private void schedule(Runnable task, long delayMillis) {
        if (!mScheduler.schedule(task, delayMillis)) {
            stop();
        }
    }

    Channel connect(Peer peer, Channel localServer) {
        if (peer.mMemberId == 0 || peer.mAddress == null || localServer == null) {
            throw new IllegalArgumentException();
        }

        ClientChannel client;

        synchronized (this) {
            if (mLocalMemberId == peer.mMemberId) {
                throw new IllegalArgumentException("Cannot connect to self");
            }

            Peer existing = mPeerSet.ceiling(peer); // findGe

            if (existing != null && existing.mMemberId == peer.mMemberId
                    && !existing.mAddress.equals(peer.mAddress)) {
                throw new IllegalStateException("Already connected with a different address");
            }

            client = new ClientChannel(peer, localServer);

            if (mPeerMap.putIfAbsent(peer.mAddress, peer) != null) {
                throw new IllegalStateException("Duplicate address: " + peer);
            }

            mPeerSet.add(peer);
            mChannels.add(client);
        }

        execute(client::connect);

        return client;
    }

    Socket connectPlain(SocketAddress addr) throws IOException {
        return connectSocket(addr, TYPE_PLAIN);
    }

    Socket connectSnapshot(SocketAddress addr) throws IOException {
        return connectSocket(addr, TYPE_SNAPSHOT);
    }

    private Socket connectSocket(SocketAddress addr, int connectionType) throws IOException {
        if (addr == null) {
            throw new IllegalArgumentException();
        }

        Peer peer;
        synchronized (this) {
            peer = mPeerMap.get(addr);
            if (peer == null) {
                throw new ConnectException("Not a group member: " + addr);
            }
        }

        Socket s = doConnect(peer, connectionType);

        if (s == null) {
            throw new ConnectException("Rejected");
        }

        return s;
    }

    Socket doConnect(Peer peer, int connectionType) throws IOException {
        if (mPartitioned) {
            return null;
        }

        Socket s = new Socket();

        doConnect:
        try {
            s.connect(peer.mAddress, CONNECT_TIMEOUT_MILLIS);

            long groupId;
            long localMemberId;

            synchronized (this) {
                groupId = mGroupId;
                localMemberId = mLocalMemberId;
            }

            byte[] header = newConnectHeader(mGroupToken, groupId, localMemberId, connectionType);

            s.getOutputStream().write(header);

            header = readHeader(s);
            if (header == null) {
                break doConnect;
            }

            // Verify expected member id.
            if (Utils.decodeLongLE(header, 24) != peer.mMemberId) {
                break doConnect;
            }

            int actualType = Utils.decodeIntLE(header, 32);

            if (actualType != connectionType) {
                break doConnect;
            }

            return s;
        } catch (IOException e) {
            Utils.closeQuietly(s);
            throw e;
        }

        Utils.closeQuietly(s);
        return null;
    }

    static byte[] newConnectHeader(long groupToken, long groupId, long memberId, int conType) {
        byte[] header = new byte[INIT_HEADER_SIZE];
        Utils.encodeLongLE(header, 0, MAGIC_NUMBER);
        Utils.encodeLongLE(header, 8, groupToken);
        Utils.encodeLongLE(header, 16, groupId);
        Utils.encodeLongLE(header, 24, memberId);
        Utils.encodeIntLE(header, 32, conType);

        encodeHeaderCrc(header);

        return header;
    }

    void disconnect(long remoteMemberId) {
        if (remoteMemberId == 0) {
            throw new IllegalArgumentException();
        }

        disconnect(id -> id == remoteMemberId);
    }

    synchronized void disconnect(LongPredicate tester) {
        Iterator<SocketChannel> it = mChannels.iterator();
        while (it.hasNext()) {
            SocketChannel channel = it.next();
            long memberId = channel.mPeer.mMemberId;
            if (tester.test(memberId)) {
                it.remove();
                channel.disconnect();
                Peer peer = mPeerSet.ceiling(new Peer(memberId)); // findGe
                if (peer != null && peer.mMemberId == memberId) {
                    mPeerSet.remove(peer);
                    mPeerMap.remove(peer.mAddress);
                }
            }
        }
    }

    synchronized void unregister(SocketChannel channel) {
        mChannels.remove(channel);
    }

    /**
     * 接收连接主程
     */
    private void acceptLoop() {
        ServerSocket ss;
        Channel localServer;
        synchronized (this) {
            ss = mServerSocket;
            localServer = mLocalServer;
        }

        if (ss == null) {
            return;
        }

        while (true) {
            try {
                doAccept(ss, localServer);
            } catch (Throwable e) {
                synchronized (this) {
                    if (ss != mServerSocket) {
                        return;
                    }
                }
                Utils.uncaught(e);
                Thread.yield();
            }
        }
    }

    /**
     * 接受连接后根据不同的连接类型进行处理
     *
     * @param ss
     * @param localServer
     * @throws IOException
     */
    private void doAccept(final ServerSocket ss, final Channel localServer) throws IOException {
        Socket s = ss.accept();

        if (mPartitioned) {
            Utils.closeQuietly(s);
            return;
        }

        ServerChannel server = null;

        try {
            byte[] header = readHeader(s);
            if (header == null) {
                return;
            }

            long remoteMemberId = Utils.decodeLongLE(header, 24);
            int connectionType = Utils.decodeIntLE(header, 32);

            Consumer<Socket> acceptor = null;

            checkType:
            {
                switch (connectionType) {
                    case TYPE_CONTROL:
                        break checkType;
                    case TYPE_PLAIN:
                        acceptor = mSocketAcceptor;
                        break;
                    case TYPE_JOIN:
                        acceptor = mJoinAcceptor;
                        break;
                    case TYPE_SNAPSHOT:
                        acceptor = mSnapshotRequestAcceptor;
                        break;
                }

                if (acceptor != null) {
                    break checkType;
                }

                Utils.closeQuietly(s);
                return;
            }

            synchronized (this) {
                Peer peer;
                if (remoteMemberId == 0) {
                    peer = null;
                } else {
                    peer = mPeerSet.ceiling(new Peer(remoteMemberId)); // findGe
                    if (peer == null || peer.mMemberId != remoteMemberId) {
                        Utils.closeQuietly(s);
                        return;
                    }
                }

                if (connectionType == TYPE_CONTROL) {
                    if (peer == null) {
                        Utils.closeQuietly(s);
                        return;
                    }
                    acceptor = server = new ServerChannel(peer, localServer);
                    mChannels.add(server);
                }

                Utils.encodeLongLE(header, 24, mLocalMemberId);
            }

            encodeHeaderCrc(header);

            final Consumer<Socket> facceptor = acceptor;

            execute(() -> {
                try {
                    s.getOutputStream().write(header);
                    facceptor.accept(s);
                    return;
                } catch (IOException e) {
                    // Ignore.
                } catch (Throwable e) {
                    Utils.uncaught(e);
                }
                Closeable c = s;
                if (facceptor instanceof ServerChannel) {
                    c = (ServerChannel) facceptor;
                }
                Utils.closeQuietly(c);
            });
        } catch (Throwable e) {
            Utils.closeQuietly(s);
            Utils.closeQuietly(server);
            throw e;
        }
    }

    static void encodeHeaderCrc(byte[] header) {
        Checksum crc = CRC32C.newInstance();
        crc.update(header, 0, header.length - 4);
        Utils.encodeIntLE(header, header.length - 4, (int) crc.getValue());
    }

    byte[] readHeader(Socket s) {
        byte[] header;
        try {
            s.setSoTimeout(INITIAL_READ_TIMEOUT_MILLIS);
            header = readHeader(s.getInputStream(), mGroupToken, getGroupId());
            s.setSoTimeout(0);
            s.setTcpNoDelay(true);
        } catch (IOException e) {
            header = null;
        }

        if (header == null) {
            Utils.closeQuietly(s);
        }

        return header;
    }

    static byte[] readHeader(InputStream in, long groupToken, long groupId) throws IOException {
        byte[] header = new byte[INIT_HEADER_SIZE];
        Utils.readFully(in, header, 0, header.length);

        if (Utils.decodeLongLE(header, 0) != MAGIC_NUMBER) {
            return null;
        }

        if (Utils.decodeLongLE(header, 8) != groupToken) {
            return null;
        }

        int connectionType = Utils.decodeIntLE(header, 32);

        if (connectionType != TYPE_JOIN && (Utils.decodeLongLE(header, 16) != groupId)) {
            return null;
        }

        Checksum crc = CRC32C.newInstance();
        crc.update(header, 0, header.length - 4);
        if (Utils.decodeIntLE(header, header.length - 4) != (int) crc.getValue()) {
            return null;
        }

        return header;
    }

    private synchronized void checkWrites() {
        if (mServerSocket == null) {
            return;
        }

        for (SocketChannel channel : mChannels) {
            int state = channel.mWriteState;
            if (state >= channel.maxWriteTagCount()) {
                if (cWriteStateUpdater.compareAndSet(channel, state, 0)) {
                    channel.closeSocket();
                }
            } else if (state == 1) {
                cWriteStateUpdater.compareAndSet(channel, 1, 2);
            }
        }

        schedule(this::checkWrites, WRITE_CHECK_DELAY_MILLIS);
    }

    static final AtomicIntegerFieldUpdater<SocketChannel> cWriteStateUpdater =
            AtomicIntegerFieldUpdater.newUpdater(SocketChannel.class, "mWriteState");

    /**
     * 网络通道(内部实现集群节点请求指令，集群节点处理指令)
     * ClientChannel触发处理指令时,代表是服务器响应指令
     * ServerChannel触发触发指令是,代表是客户端请求指令
     */
    abstract class SocketChannel extends Latch implements Channel, Closeable, Consumer<Socket> {
        final Peer mPeer;
        private Channel mLocalServer;
        private volatile Socket mSocket;
        private OutputStream mOut;
        private ChannelInputStream mIn;
        private int mReconnectDelay;

        // 0: not writing;  1: writing;  2+: tagged to be closed due to timeout
        volatile int mWriteState;

        SocketChannel(Peer peer, Channel localServer) {
            mPeer = peer;
            mLocalServer = localServer;
        }

        void connect() {
            InputStream in;
            synchronized (this) {
                in = mIn;
            }

            try {
                connected(doConnect(mPeer, TYPE_CONTROL));
            } catch (IOException e) {
                // Ignore.
            }

            reconnect(in);
        }

        void reconnect(InputStream existing) {
            Channel localServer;
            Socket s;
            int delay;
            synchronized (this) {
                if (existing != mIn) {
                    return;
                }
                localServer = mLocalServer;
                s = mSocket;
                mSocket = null;
                mOut = null;
                mIn = null;
                delay = Math.max(mReconnectDelay, MIN_RECONNECT_DELAY_MILLIS);
                mReconnectDelay = Math.min(delay << 1, MAX_RECONNECT_DELAY_MILLIS);
            }

            Utils.closeQuietly(s);

            if (localServer != null) {
                schedule(this::connect, delay);
            }
        }

        @Override
        public void close() {
            unregister(this);
            disconnect();
        }

        void disconnect() {
            Socket s;
            synchronized (this) {
                mLocalServer = null;
                s = mSocket;
                mSocket = null;
                mOut = null;
                mIn = null;
            }

            Utils.closeQuietly(s);
        }

        void closeSocket() {
            Utils.closeQuietly(mSocket);
        }

        @Override
        public void accept(Socket s) {
            connected(s);
        }

        synchronized boolean connected(Socket s) {
            if (mPartitioned) {
                Utils.closeQuietly(s);
                return false;
            }

            OutputStream out;
            ChannelInputStream in;
            try {
                out = s.getOutputStream();
                in = new ChannelInputStream(s.getInputStream(), 8192);
            } catch (Throwable e) {
                Utils.closeQuietly(s);
                return false;
            }

            Utils.closeQuietly(mSocket);

            acquireExclusive();
            mSocket = s;
            mOut = out;
            mIn = in;
            mReconnectDelay = 0;
            releaseExclusive();

            execute(this::inputLoop);

            notifyAll();

            return true;
        }

        /**
         * 输入主程逻辑(客户端与服务端公用一套处理逻辑)
         */
        private void inputLoop() {
            Channel localServer;
            ChannelInputStream in;
            synchronized (this) {
                localServer = mLocalServer;
                in = mIn;
            }

            if (localServer == null || in == null) {
                return;
            }

            try {
                while (true) {
                    long header = in.readLongLE();

                    // TODO: 处理读取增量，以便通道可以按
                    long readDelta = header >>> 32;

                    int opAndLength = (int) header;
                    int commandLength = (opAndLength >> 8) & 0xffffff;
                    int op = opAndLength & 0xff;
                    //****** 跟踪BUG后续删除 ******
                    //System.out.println(this.getClass().getSimpleName() + " 读取OP ---------> " + op);
                    switch (op) {
                        case OP_NOP:
                            localServer.nop(this);
                            break;
                        case OP_REQUEST_VOTE:
                            localServer.requestVote(this, in.readLongLE(), in.readLongLE(),
                                    in.readLongLE(), in.readLongLE());
                            commandLength -= (8 * 4);
                            break;
                        case OP_REQUEST_VOTE_REPLY:
                            localServer.requestVoteReply(this, in.readLongLE());
                            commandLength -= (8 * 1);
                            break;
                        case OP_QUERY_TERMS:
                            localServer.queryTerms(this, in.readLongLE(), in.readLongLE());
                            commandLength -= (8 * 2);
                            break;
                        case OP_QUERY_TERMS_REPLY:
                            localServer.queryTermsReply(this, in.readLongLE(),
                                    in.readLongLE(), in.readLongLE());
                            commandLength -= (8 * 3);
                            break;
                        case OP_QUERY_DATA:
                            localServer.queryData(this, in.readLongLE(), in.readLongLE());
                            commandLength -= (8 * 2);
                            break;
                        case OP_QUERY_DATA_REPLY:
                            long currentTerm = in.readLongLE();
                            long prevTerm = in.readLongLE();
                            long term = in.readLongLE();
                            long index = in.readLongLE();
                            commandLength -= (8 * 4);
                            byte[] data = new byte[commandLength];
                            Utils.readFully(in, data, 0, data.length);
                            localServer.queryDataReply(this, currentTerm, prevTerm, term, index, data);
                            commandLength = 0;
                            break;
                        case OP_WRITE_DATA:
                            prevTerm = in.readLongLE();
                            term = in.readLongLE();
                            index = in.readLongLE();
                            long highestIndex = in.readLongLE();
                            long commitIndex = in.readLongLE();
                            commandLength -= (8 * 5);
                            data = new byte[commandLength];
                            Utils.readFully(in, data, 0, data.length);
                            localServer.writeData(this, prevTerm, term, index,
                                    highestIndex, commitIndex, data);
                            commandLength = 0;
                            break;
                        case OP_WRITE_DATA_REPLY:
                            localServer.writeDataReply(this, in.readLongLE(), in.readLongLE());
                            commandLength -= (8 * 2);
                            break;
                        case OP_SYNC_COMMIT:
                            localServer.syncCommit(this, in.readLongLE(),
                                    in.readLongLE(), in.readLongLE());
                            commandLength -= (8 * 3);
                            break;
                        case OP_SYNC_COMMIT_REPLY:
                            localServer.syncCommitReply(this, in.readLongLE(),
                                    in.readLongLE(), in.readLongLE());
                            commandLength -= (8 * 3);
                            break;
                        case OP_COMPACT:
                            localServer.compact(this, in.readLongLE());
                            commandLength -= (8 * 1);
                            break;
                        case OP_SNAPSHOT_SCORE:
                            localServer.snapshotScore(this);
                            break;
                        case OP_SNAPSHOT_SCORE_REPLY:
                            localServer.snapshotScoreReply(this, in.readIntLE(),
                                    Float.intBitsToFloat(in.readIntLE()));
                            commandLength -= (4 * 2);
                            break;
                        case OP_UPDATE_ROLE:
                            localServer.updateRole(this, in.readLongLE(), in.readLongLE(),
                                    Role.decode(in.readByte()));
                            commandLength -= (8 * 2 + 1);
                            break;
                        case OP_UPDATE_ROLE_REPLY:
                            localServer.updateRoleReply(this, in.readLongLE(), in.readLongLE(),
                                    in.readByte());
                            commandLength -= (8 * 2 + 1);
                            break;
                        case OP_GROUP_VERSION:
                            localServer.groupVersion(this, in.readLongLE());
                            commandLength -= (8 * 1);
                            break;
                        case OP_GROUP_VERSION_REPLY:
                            localServer.groupVersionReply(this, in.readLongLE());
                            commandLength -= (8 * 1);
                            break;
                        case OP_GROUP_FILE:
                            localServer.groupFile(this, in.readLongLE());
                            commandLength -= (8 * 1);
                            break;
                        case OP_GROUP_FILE_REPLY:
                            localServer.groupFileReply(this, in);
                            break;
                        default:
                            localServer.unknown(this, op);
                            break;
                    }

                    in.skipFully(commandLength);
                }
            } catch (IOException e) {
                // Ignore.
            } catch (Throwable e) {
                Utils.uncaught(e);
            }

            reconnect(in);
        }

        @Override
        public Peer peer() {
            return mPeer;
        }

        @Override
        public int waitForConnection(int timeoutMillis) throws InterruptedIOException {
            if (timeoutMillis == 0) {
                return 0;
            }

            long end = Long.MIN_VALUE;

            synchronized (this) {
                while (true) {
                    if (mSocket != null || mLocalServer == null) {
                        return timeoutMillis;
                    }

                    try {
                        if (timeoutMillis < 0) {
                            wait();
                        } else {
                            long now = System.currentTimeMillis();
                            if (end == Long.MIN_VALUE) {
                                end = now + timeoutMillis;
                            } else {
                                timeoutMillis = (int) (end - now);
                            }
                            if (timeoutMillis <= 0) {
                                return 0;
                            }
                            wait(timeoutMillis);
                        }
                    } catch (InterruptedException e) {
                        throw new InterruptedIOException();
                    }
                }
            }
        }

        @Override
        public void unknown(Channel from, int op) {
            // Not a normal remote call.
        }

        @Override
        public boolean nop(Channel from) {
            return writeCommand(OP_NOP);
        }

        @Override
        public boolean requestVote(Channel from, long term, long candidateId,
                                   long highestTerm, long highestIndex) {
            return writeCommand(OP_REQUEST_VOTE, term, candidateId, highestTerm, highestIndex);
        }

        @Override
        public boolean requestVoteReply(Channel from, long term) {
            return writeCommand(OP_REQUEST_VOTE_REPLY, term);
        }

        @Override
        public boolean queryTerms(Channel from, long startIndex, long endIndex) {
            return writeCommand(OP_QUERY_TERMS, startIndex, endIndex);
        }

        @Override
        public boolean queryTermsReply(Channel from, long prevTerm, long term, long startIndex) {
            return writeCommand(OP_QUERY_TERMS_REPLY, prevTerm, term, startIndex);
        }

        @Override
        public boolean queryData(Channel from, long startIndex, long endIndex) {
            return writeCommand(OP_QUERY_DATA, startIndex, endIndex);
        }

        @Override
        public boolean queryDataReply(Channel from, long currentTerm,
                                      long prevTerm, long term, long index, byte[] data) {
            if (data.length > ((1 << 24) - (8 * 3))) {
                // TODO: break it up into several commands
                throw new IllegalArgumentException("Too large");
            }

            acquireExclusive();
            try {
                OutputStream out = mOut;
                if (out == null) {
                    return false;
                }
                byte[] command = new byte[(8 + 8 * 4) + data.length];
                prepareCommand(out, command, OP_QUERY_DATA_REPLY, 0, command.length - 8);
                Utils.encodeLongLE(command, 8, currentTerm);
                Utils.encodeLongLE(command, 16, prevTerm);
                Utils.encodeLongLE(command, 24, term);
                Utils.encodeLongLE(command, 32, index);
                System.arraycopy(data, 0, command, 40, data.length);
                return writeCommand(out, command, 0, command.length);
            } finally {
                releaseExclusive();
            }
        }

        @Override
        public boolean writeData(Channel from, long prevTerm, long term, long index,
                                 long highestIndex, long commitIndex, byte[] data) {
            if (data.length > ((1 << 24) - (8 * 5))) {
                // TODO: break it up into several commands
                throw new IllegalArgumentException("Too large");
            }

            acquireExclusive();
            try {
                OutputStream out = mOut;
                if (out == null) {
                    return false;
                }
                byte[] command = new byte[(8 + 8 * 5) + data.length];
                prepareCommand(out, command, OP_WRITE_DATA, 0, command.length - 8);
                Utils.encodeLongLE(command, 8, prevTerm);
                Utils.encodeLongLE(command, 16, term);
                Utils.encodeLongLE(command, 24, index);
                Utils.encodeLongLE(command, 32, highestIndex);
                Utils.encodeLongLE(command, 40, commitIndex);
                System.arraycopy(data, 0, command, 48, data.length);
                return writeCommand(out, command, 0, command.length);
            } finally {
                releaseExclusive();
            }
        }

        @Override
        public boolean writeDataReply(Channel from, long term, long highestIndex) {
            return writeCommand(OP_WRITE_DATA_REPLY, term, highestIndex);
        }

        @Override
        public boolean syncCommit(Channel from, long prevTerm, long term, long index) {
            return writeCommand(OP_SYNC_COMMIT, prevTerm, term, index);
        }

        @Override
        public boolean syncCommitReply(Channel from, long groupVersion, long term, long index) {
            return writeCommand(OP_SYNC_COMMIT_REPLY, groupVersion, term, index);
        }

        @Override
        public boolean compact(Channel from, long index) {
            return writeCommand(OP_COMPACT, index);
        }

        @Override
        public boolean snapshotScore(Channel from) {
            return writeCommand(OP_SNAPSHOT_SCORE);
        }

        @Override
        public boolean snapshotScoreReply(Channel from, int activeSessions, float weight) {
            acquireExclusive();
            try {
                OutputStream out = mOut;
                if (out == null) {
                    return false;
                }
                byte[] command = new byte[8 + 4 * 2];
                prepareCommand(out, command, OP_SNAPSHOT_SCORE_REPLY, 0, 4 * 2);
                Utils.encodeIntLE(command, 8, activeSessions);
                Utils.encodeIntLE(command, 12, Float.floatToIntBits(weight));
                return writeCommand(out, command, 0, command.length);
            } finally {
                releaseExclusive();
            }
        }

        @Override
        public boolean updateRole(Channel from, long groupVersion, long memberId, Role role) {
            return writeCommand(OP_UPDATE_ROLE, groupVersion, memberId, role.mCode);
        }

        @Override
        public boolean updateRoleReply(Channel from,
                                       long groupVersion, long memberId, byte result) {
            return writeCommand(OP_UPDATE_ROLE_REPLY, groupVersion, memberId, result);
        }

        @Override
        public boolean groupVersion(Channel from, long groupVersion) {
            return writeCommand(OP_GROUP_VERSION, groupVersion);
        }

        @Override
        public boolean groupVersionReply(Channel from, long groupVersion) {
            return writeCommand(OP_GROUP_VERSION_REPLY, groupVersion);
        }

        @Override
        public boolean groupFile(Channel from, long groupVersion) {
            return writeCommand(OP_GROUP_FILE, groupVersion);
        }

        @Override
        public OutputStream groupFileReply(Channel from, InputStream in) throws IOException {
            acquireExclusive();
            try {
                OutputStream out = mOut;
                if (out != null) {
                    byte[] command = new byte[8];
                    prepareCommand(out, command, OP_GROUP_FILE_REPLY, 0, 0);
                    if (writeCommand(out, command, 0, command.length)) {
                        return out;
                    }
                }
            } finally {
                releaseExclusive();
            }
            return null;
        }

        private boolean writeCommand(int op) {
            acquireExclusive();
            try {
                OutputStream out = mOut;
                if (out == null) {
                    return false;
                }
                byte[] command = new byte[8];
                prepareCommand(out, command, op, 0, 0);
                return writeCommand(out, command, 0, command.length);
            } finally {
                releaseExclusive();
            }
        }

        private boolean writeCommand(int op, long a) {
            acquireExclusive();
            try {
                OutputStream out = mOut;
                if (out == null) {
                    return false;
                }
                byte[] command = new byte[8 + 8 * 1];
                prepareCommand(out, command, op, 0, 8 * 1);
                Utils.encodeLongLE(command, 8, a);
                return writeCommand(out, command, 0, command.length);
            } finally {
                releaseExclusive();
            }
        }

        private boolean writeCommand(int op, long a, long b) {
            acquireExclusive();
            try {
                OutputStream out = mOut;
                if (out == null) {
                    return false;
                }
                byte[] command = new byte[8 + 8 * 2];
                prepareCommand(out, command, op, 0, 8 * 2);
                Utils.encodeLongLE(command, 8, a);
                Utils.encodeLongLE(command, 16, b);
                return writeCommand(out, command, 0, command.length);
            } finally {
                releaseExclusive();
            }
        }

        private boolean writeCommand(int op, long a, long b, byte c) {
            acquireExclusive();
            try {
                OutputStream out = mOut;
                if (out == null) {
                    return false;
                }
                byte[] command = new byte[8 + 8 * 2 + 1];
                prepareCommand(out, command, op, 0, 8 * 2 + 1);
                Utils.encodeLongLE(command, 8, a);
                Utils.encodeLongLE(command, 16, b);
                command[24] = c;
                return writeCommand(out, command, 0, command.length);
            } finally {
                releaseExclusive();
            }
        }

        private boolean writeCommand(int op, long a, long b, long c) {
            acquireExclusive();
            try {
                OutputStream out = mOut;
                if (out == null) {
                    return false;
                }
                byte[] command = new byte[8 + 8 * 3];
                prepareCommand(out, command, op, 0, 8 * 3);
                Utils.encodeLongLE(command, 8, a);
                Utils.encodeLongLE(command, 16, b);
                Utils.encodeLongLE(command, 24, c);
                return writeCommand(out, command, 0, command.length);
            } finally {
                releaseExclusive();
            }
        }

        private boolean writeCommand(int op, long a, long b, long c, long d) {
            acquireExclusive();
            try {
                OutputStream out = mOut;
                if (out == null) {
                    return false;
                }
                byte[] command = new byte[8 + 8 * 4];
                prepareCommand(out, command, op, 0, 8 * 4);
                Utils.encodeLongLE(command, 8, a);
                Utils.encodeLongLE(command, 16, b);
                Utils.encodeLongLE(command, 24, c);
                Utils.encodeLongLE(command, 32, d);
                return writeCommand(out, command, 0, command.length);
            } finally {
                releaseExclusive();
            }
        }

        /**
         * 调用者必须持有独占锁,防止缓存非安全性
         *
         * @param command must have at least 8 bytes, used for the header
         * @param length  max allowed is 16,777,216 bytes
         */
        private void prepareCommand(OutputStream out, byte[] command,
                                    int op, int offset, int length) {
            ChannelInputStream in = mIn;
            long bytesRead = in == null ? 0 : in.resetReadAmount();

            if (Long.compareUnsigned(bytesRead, 1L << 32) >= 0) {
                // Won't fit in the field, so send a bunch of nops.
                Utils.encodeIntLE(command, offset, 0);
                Utils.encodeIntLE(command, offset + 4, (int) ((1L << 32) - 1));
                do {
                    bytesRead -= ((1L << 32) - 1);
                    writeCommand(out, command, offset, 8);
                } while (Long.compareUnsigned(bytesRead, 1L << 32) >= 0);
            }

            Utils.encodeIntLE(command, offset, (length << 8) | (byte) op);
            Utils.encodeIntLE(command, offset + 4, (int) bytesRead);
        }

        private boolean writeCommand(OutputStream out, byte[] command, int offset, int length) {
            try {
                mWriteState = 1;
                out.write(command, offset, length);
                mWriteState = 0;
                return true;
            } catch (IOException e) {
                mOut = null;
                // Close and attempt to reconnect.
                closeSocket();
                return false;
            }
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + ": {peer=" + mPeer + ", socket=" + mSocket + '}';
        }

        abstract int maxWriteTagCount();
    }

    final class ClientChannel extends SocketChannel {
        ClientChannel(Peer peer, Channel localServer) {
            super(peer, localServer);
        }

        @Override
        int maxWriteTagCount() {
            return 2;
        }
    }

    final class ServerChannel extends SocketChannel {
        ServerChannel(Peer peer, Channel localServer) {
            super(peer, localServer);
        }

        @Override
        void reconnect(InputStream existing) {
            close();
        }

        @Override
        int maxWriteTagCount() {
            return 50;
        }
    }
}
