package com.glodon.linglong.replication;

import com.glodon.linglong.base.concurrent.Latch;
import com.glodon.linglong.base.concurrent.LatchCondition;
import com.glodon.linglong.base.common.Utils;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;

import static com.glodon.linglong.base.common.IOUtils.closeQuietly;
import static com.glodon.linglong.base.common.IOUtils.encodeLongLE;
import static com.glodon.linglong.base.common.IOUtils.rethrow;

/**
 * 复制核心控制器(基于RAFT共识协议)
 *
 * @author Stereo
 */
final class Controller extends Latch implements StreamReplicator, Channel {
    private static final int MODE_FOLLOWER = 0, MODE_CANDIDATE = 1, MODE_LEADER = 2;
    private static final int ELECTION_DELAY_LOW_MILLIS = 200, ELECTION_DELAY_HIGH_MILLIS = 300;
    private static final int QUERY_TERMS_RATE_MILLIS = 1;
    private static final int MISSING_DELAY_LOW_MILLIS = 400, MISSING_DELAY_HIGH_MILLIS = 600;
    private static final int SYNC_COMMIT_RETRY_MILLIS = 100;
    private static final int CONNECT_TIMEOUT_MILLIS = 500;
    private static final int SNAPSHOT_REPLY_TIMEOUT_MILLIS = 5000;
    private static final int JOIN_TIMEOUT_MILLIS = 5000;
    private static final int MISSING_DATA_REQUEST_SIZE = 100_000;

    private static final byte CONTROL_OP_JOIN = 1, CONTROL_OP_UPDATE_ROLE = 2,
            CONTROL_OP_UNJOIN = 3;

    private static final byte[] EMPTY_DATA = new byte[0];

    private final BiConsumer<Level, String> mEventListener;
    private final Scheduler mScheduler;
    private final ChannelManager mChanMan;
    private final StateLog mStateLog;

    private final LatchCondition mSyncCommitCondition;

    private GroupFile mGroupFile;

    private Role mLocalRole;

    private Peer[] mConsensusPeers;
    private Channel[] mConsensusChannels;
    private Channel[] mAllChannels;

    private Channel mLeaderReplyChannel;
    private Channel mLeaderRequestChannel;

    private int mLocalMode;

    private long mCurrentTerm;
    private int mGrantsRemaining;
    private int mElectionValidated;
    private long mValidatedTerm;
    private long mLeaderCommitIndex;

    private LogWriter mLeaderLogWriter;
    private ReplWriter mLeaderReplWriter;

    private long mMissingContigIndex = Long.MAX_VALUE;
    private boolean mSkipMissingDataTask;
    private volatile boolean mReceivingMissingData;

    private volatile long mNextQueryTermTime = Long.MIN_VALUE;

    private volatile Consumer<byte[]> mControlMessageAcceptor;

    private int mSnapshotSessionCount;

    static Controller open(BiConsumer<Level, String> eventListener,
                           StateLog log, long groupToken, File groupFile,
                           SocketAddress localAddress, SocketAddress listenAddress,
                           Role localRole, Set<SocketAddress> seeds, ServerSocket localSocket)
            throws IOException {
        GroupFile gf = GroupFile.open(eventListener, groupFile, localAddress, seeds.isEmpty());
        Controller con = new Controller(eventListener, log, groupToken, gf);
        con.init(groupFile, localAddress, listenAddress, localRole, seeds, localSocket);
        return con;
    }

    private Controller(BiConsumer<Level, String> eventListener,
                       StateLog log, long groupToken, GroupFile gf)
            throws IOException {
        mEventListener = eventListener;
        mStateLog = log;
        mScheduler = new Scheduler();
        mChanMan = new ChannelManager(mScheduler, groupToken, gf == null ? 0 : gf.groupId());
        mGroupFile = gf;
        mSyncCommitCondition = new LatchCondition();
    }

    private void init(File groupFile,
                      SocketAddress localAddress, SocketAddress listenAddress,
                      Role localRole, Set<SocketAddress> seeds, ServerSocket localSocket)
            throws IOException {
        acquireExclusive();
        try {
            mLocalRole = localRole;

            if (mGroupFile == null) {
                for (int trials = 2; --trials >= 0; ) {
                    try {
                        GroupJoiner joiner = new GroupJoiner
                                (mEventListener, groupFile, mChanMan.getGroupToken(),
                                        localAddress, listenAddress);

                        joiner.join(seeds, JOIN_TIMEOUT_MILLIS);

                        mGroupFile = joiner.mGroupFile;
                        mChanMan.setGroupId(mGroupFile.groupId());
                        break;
                    } catch (JoinException e) {
                        if (trials <= 0) {
                            throw e;
                        }
                    }
                }
            }

            mChanMan.setLocalMemberId(mGroupFile.localMemberId(), localSocket);

            refreshPeerSet();
        } catch (Throwable e) {
            closeQuietly(localSocket);
            closeQuietly(this);
            throw e;
        } finally {
            releaseExclusive();
        }
    }

    /**
     * 刷新对等网络
     */
    private void refreshPeerSet() {
        Map<Long, Channel> currentPeerChannels = new HashMap<>();

        if (mAllChannels != null) {
            for (Channel channel : mAllChannels) {
                currentPeerChannels.put(channel.peer().mMemberId, channel);
            }
        }

        List<Peer> consensusPeers = new ArrayList<>();
        List<Channel> consensusChannels = new ArrayList<>();
        List<Channel> allChannels = new ArrayList<>();

        for (Peer peer : mGroupFile.allPeers()) {
            Channel channel = currentPeerChannels.remove(peer.mMemberId);

            if (channel == null) {
                channel = mChanMan.connect(peer, this);
            }

            allChannels.add(channel);

            if (peer.mRole == Role.NORMAL || peer.mRole == Role.STANDBY) {
                consensusPeers.add(peer);
                consensusChannels.add(channel);
            }
        }

        for (long toRemove : currentPeerChannels.keySet()) {
            mChanMan.disconnect(toRemove);
        }

        mConsensusPeers = consensusPeers.toArray(new Peer[0]);
        mConsensusChannels = consensusChannels.toArray(new Channel[0]);
        mAllChannels = allChannels.toArray(new Channel[0]);

        if (mLeaderReplWriter != null) {
            mLeaderReplWriter.update(mLeaderLogWriter, mAllChannels, consensusChannels.isEmpty());
        }

        if (mLocalMode != MODE_FOLLOWER && mGroupFile.localMemberRole() != Role.NORMAL) {
            toFollower("local role changed");
        }

        mSyncCommitCondition.signalAll();
    }

    @Override
    public void start() throws IOException {
        start(null);
    }

    @Override
    public SnapshotReceiver restore(Map<String, String> options) throws IOException {
        if (mChanMan.isStarted()) {
            throw new IllegalStateException("Already started");
        }

        SocketSnapshotReceiver receiver = requestSnapshot(options);

        if (receiver != null) {
            try {
                start(receiver);
            } catch (Throwable e) {
                closeQuietly(receiver);
                throw e;
            }
        }

        return receiver;
    }

    private void start(SocketSnapshotReceiver receiver) throws IOException {
        if (!mChanMan.isStarted()) {
            if (receiver != null) {
                mStateLog.truncateAll(receiver.prevTerm(), receiver.term(), receiver.index());
            }

            mChanMan.start(this);

            scheduleElectionTask();
            scheduleMissingDataTask();

            mChanMan.joinAcceptor(this::requestJoin);

            acquireExclusive();
            boolean quickElection = mConsensusChannels.length == 0;
            mElectionValidated = quickElection ? -1 : 1;
            releaseExclusive();

            if (quickElection) {
                doElectionTask();
            }
        }

        if (receiver == null) {
            acquireShared();
            boolean roleChange = mGroupFile.localMemberRole() != mLocalRole;
            releaseShared();

            if (roleChange) {
                mScheduler.execute(this::roleChangeTask);
            }
        }
    }

    @Override
    public Reader newReader(long index, boolean follow) {
        if (follow) {
            return mStateLog.openReader(index);
        }

        acquireShared();
        try {
            Reader reader;
            if (mLeaderLogWriter != null
                    && index >= mLeaderLogWriter.termStartIndex()
                    && index < mLeaderLogWriter.termEndIndex()) {
                reader = null;
            } else {
                reader = mStateLog.openReader(index);
            }
            return reader;
        } finally {
            releaseShared();
        }
    }

    @Override
    public Writer newWriter() {
        return createWriter(-1);
    }

    @Override
    public Writer newWriter(long index) {
        if (index < 0) {
            throw new IllegalArgumentException();
        }
        return createWriter(index);
    }

    private Writer createWriter(long index) {
        acquireExclusive();
        try {
            if (mLeaderReplWriter != null) {
                throw new IllegalStateException("Writer already exists");
            }
            if (mLeaderLogWriter == null || (index >= 0 && index != mLeaderLogWriter.index())) {
                return null;
            }
            ReplWriter writer = new ReplWriter
                    (mLeaderLogWriter, mAllChannels, mConsensusPeers.length == 0);
            mLeaderReplWriter = writer;
            return writer;
        } finally {
            releaseExclusive();
        }
    }

    void writerClosed(ReplWriter writer) {
        acquireExclusive();
        if (mLeaderReplWriter == writer) {
            mLeaderReplWriter = null;
        }
        releaseExclusive();
    }

    @Override
    public void sync() throws IOException {
        mStateLog.sync();
    }

    @Override
    public boolean syncCommit(long index, long nanosTimeout) throws IOException {
        long nanosEnd = nanosTimeout <= 0 ? 0 : System.nanoTime() + nanosTimeout;

        while (true) {
            if (mStateLog.isDurable(index)) {
                break;
            }

            if (nanosTimeout == 0) {
                return false;
            }

            TermLog termLog = mStateLog.termLogAt(index);
            long prevTerm = termLog.prevTermAt(index);
            long term = termLog.term();

            long commitIndex = mStateLog.syncCommit(prevTerm, term, index);

            if (index > commitIndex) {
                if (commitIndex >= 0) {
                    throw new IllegalStateException
                            ("Invalid commit index: " + index + " > " + commitIndex);
                }
            } else {
                acquireShared();
                Channel[] channels = mConsensusChannels;
                releaseShared();

                if (channels.length == 0) {
                    mStateLog.commitDurable(index);
                    break;
                }

                for (Channel channel : channels) {
                    channel.syncCommit(this, prevTerm, term, index);
                }

                long actualTimeout = SYNC_COMMIT_RETRY_MILLIS * 1_000_000L;
                if (nanosTimeout >= 0) {
                    actualTimeout = Math.min(nanosTimeout, actualTimeout);
                }

                acquireExclusive();
                if (mStateLog.isDurable(index)) {
                    releaseExclusive();
                    break;
                }
                int result = mSyncCommitCondition.await(this, actualTimeout, nanosEnd);
                releaseExclusive();

                if (result < 0) {
                    throw new InterruptedIOException();
                }
            }

            if (nanosTimeout > 0 && (nanosTimeout -= nanosEnd - System.nanoTime()) < 0) {
                nanosTimeout = 0;
            }
        }

        acquireShared();
        Channel[] channels = mAllChannels;
        releaseShared();

        for (Channel channel : channels) {
            channel.compact(this, index);
        }

        return true;
    }

    @Override
    public void compact(long index) throws IOException {
        long lowestIndex = index;

        acquireShared();
        try {
            for (Channel channel : mAllChannels) {
                lowestIndex = Math.min(lowestIndex, channel.peer().mCompactIndex);
            }
        } finally {
            releaseShared();
        }

        mStateLog.compact(lowestIndex);
    }

    @Override
    public long getLocalMemberId() {
        return mChanMan.getLocalMemberId();
    }

    @Override
    public SocketAddress getLocalAddress() {
        acquireShared();
        SocketAddress addr = mGroupFile.localMemberAddress();
        releaseShared();
        return addr;
    }

    @Override
    public Role getLocalRole() {
        acquireShared();
        Role role = mGroupFile.localMemberRole();
        releaseShared();
        return role;
    }

    @Override
    public Socket connect(SocketAddress addr) throws IOException {
        return mChanMan.connectPlain(addr);
    }

    @Override
    public void socketAcceptor(Consumer<Socket> acceptor) {
        mChanMan.socketAcceptor(acceptor);
    }

    @Override
    public void controlMessageReceived(long index, byte[] message) throws IOException {
        boolean quickCommit = false;
        Role oldRole = null, newRole = null;

        acquireExclusive();
        try {
            boolean refresh;

            switch (message[0]) {
                default:
                    return;
                case CONTROL_OP_JOIN:
                    refresh = mGroupFile.applyJoin(index, message) != null;
                    break;
                case CONTROL_OP_UPDATE_ROLE:
                    oldRole = mGroupFile.localMemberRole();
                    if (refresh = mGroupFile.applyUpdateRole(message)) {
                        newRole = mGroupFile.localMemberRole();
                    }
                    break;
                case CONTROL_OP_UNJOIN:
                    refresh = mGroupFile.applyRemovePeer(message);
                    break;
            }

            // TODO: 追随者应该很早就通知领导者他们现在的团队

            if (refresh) {
                refreshPeerSet();

                if (mLocalMode == MODE_LEADER) {
                    // TODO: 当对所有内容都实现快速提交回复时，不需要。
                    quickCommit = true;
                }
            }
        } finally {
            releaseExclusive();
        }

        if (quickCommit) {
            mScheduler.execute(this::affirmLeadership);
        }
    }

    @Override
    public void controlMessageAcceptor(Consumer<byte[]> acceptor) {
        mControlMessageAcceptor = acceptor;
    }

    @Override
    public SocketSnapshotReceiver requestSnapshot(Map<String, String> options) throws IOException {
        acquireShared();
        Channel[] channels = mAllChannels;
        releaseShared();

        if (channels.length == 0) {
            return null;
        }

        waitForConnections(channels);

        final Object requestedBy = Thread.currentThread();
        for (Channel channel : channels) {
            channel.peer().resetSnapshotScore(new SnapshotScore(requestedBy, channel));
            channel.snapshotScore(this);
        }

        long timeoutMillis = SNAPSHOT_REPLY_TIMEOUT_MILLIS;
        long end = System.currentTimeMillis() + timeoutMillis;

        List<SnapshotScore> results = new ArrayList<>(channels.length);

        for (int i = 0; i < channels.length; ) {
            Channel channel = channels[i];
            SnapshotScore score = channel.peer().awaitSnapshotScore(requestedBy, timeoutMillis);
            if (score != null) {
                results.add(score);
            }
            if (++i >= channels.length) {
                break;
            }
            timeoutMillis = end - System.currentTimeMillis();
            if (timeoutMillis <= 0) {
                break;
            }
        }

        if (results.isEmpty()) {
            throw new ConnectException("Unable to obtain a snapshot from a peer (timed out)");
        }

        Collections.shuffle(results);
        Collections.sort(results);

        Socket sock = mChanMan.connectSnapshot(results.get(0).mChannel.peer().mAddress);

        try {
            return new SocketSnapshotReceiver(mGroupFile, sock, options);
        } catch (IOException e) {
            closeQuietly(sock);
            throw e;
        }
    }

    @Override
    public void snapshotRequestAcceptor(Consumer<SnapshotSender> acceptor) {
        mChanMan.snapshotRequestAcceptor(sock -> {
            SnapshotSender sender;
            try {
                sender = new Sender(sock);
            } catch (IOException e) {
                closeQuietly(sock);
                return;
            } catch (Throwable e) {
                closeQuietly(sock);
                throw e;
            }

            mScheduler.execute(() -> acceptor.accept(sender));
        });
    }

    final class Sender extends SocketSnapshotSender {
        boolean mClosed;

        Sender(Socket socket) throws IOException {
            super(mGroupFile, socket);
            adjustSnapshotSessionCount(this, +1);
        }

        @Override
        public void close() throws IOException {
            adjustSnapshotSessionCount(this, -1);
            super.close();
        }

        @Override
        TermLog termLogAt(long index) {
            return mStateLog.termLogAt(index);
        }
    }

    private void adjustSnapshotSessionCount(Sender sender, int amt) {
        acquireExclusive();

        if (!sender.mClosed) {
            mSnapshotSessionCount += amt;
            if (amt < 0) {
                sender.mClosed = true;
            }
        }

        releaseExclusive();
    }

    final class ReplWriter implements Writer {
        private final LogWriter mWriter;
        private Channel[] mPeerChannels;
        private boolean mSelfCommit;

        ReplWriter(LogWriter writer, Channel[] peerChannels, boolean selfCommit) {
            mWriter = writer;
            mPeerChannels = peerChannels;
            mSelfCommit = selfCommit;
        }

        @Override
        public long term() {
            return mWriter.term();
        }

        @Override
        public long termStartIndex() {
            return mWriter.termStartIndex();
        }

        @Override
        public long termEndIndex() {
            return mWriter.termEndIndex();
        }

        @Override
        public long index() {
            return mWriter.index();
        }

        @Override
        public long commitIndex() {
            return mWriter.commitIndex();
        }

        /**
         * 复制数据广播到集群中其他节点
         *
         * @param data
         * @param offset
         * @param length
         * @param highestIndex
         * @return
         * @throws IOException
         */
        @Override
        public int write(byte[] data, int offset, int length, long highestIndex)
                throws IOException {
            Channel[] peerChannels;
            long prevTerm, term, index, commitIndex;
            int amt;

            synchronized (this) {
                peerChannels = mPeerChannels;

                if (peerChannels == null) {
                    return -1;
                }

                LogWriter writer = mWriter;

                prevTerm = writer.prevTerm();
                term = writer.term();
                index = writer.index();

                amt = writer.write(data, offset, length, highestIndex);

                if (amt <= 0) {
                    if (length > 0) {
                        mPeerChannels = null;
                    }
                    return amt;
                }

                mStateLog.captureHighest(writer);
                highestIndex = writer.mHighestIndex;

                if (mSelfCommit) {
                    mStateLog.commit(highestIndex);
                }

                if (peerChannels.length == 0) {
                    return amt;
                }

                commitIndex = writer.mCommitIndex;
            }

            // TODO: stream it
            data = Arrays.copyOfRange(data, offset, offset + length);

            for (Channel peerChan : peerChannels) {
                peerChan.writeData(null, prevTerm, term, index, highestIndex, commitIndex, data);
            }

            return amt;
        }

        @Override
        public long waitForCommit(long index, long nanosTimeout) throws InterruptedIOException {
            return mWriter.waitForCommit(index, nanosTimeout);
        }

        @Override
        public void uponCommit(long index, LongConsumer task) {
            mWriter.uponCommit(index, task);
        }

        @Override
        public void close() {
            mWriter.release();
            writerClosed(this);
        }

        synchronized void update(LogWriter writer, Channel[] peerChannels, boolean selfCommit) {
            if (mWriter == writer && mPeerChannels != null) {
                mPeerChannels = peerChannels;
                mSelfCommit = selfCommit;
            }
        }

        synchronized void deactivate() {
            mPeerChannels = null;
            mSelfCommit = false;
        }
    }

    @Override
    public void close() throws IOException {
        mChanMan.stop();
        mScheduler.shutdown();
        mStateLog.close();
    }

    void uncaught(Throwable e) {
        if (!mChanMan.isStopped()) {
            Utils.uncaught(e);
        }
    }

    Scheduler scheduler() {
        return mScheduler;
    }

    void partitioned(boolean enable) {
        mChanMan.partitioned(enable);
    }

    private static void waitForConnections(Channel[] channels) throws InterruptedIOException {
        int timeoutMillis = CONNECT_TIMEOUT_MILLIS;
        for (Channel channel : channels) {
            timeoutMillis = channel.waitForConnection(timeoutMillis);
        }
    }

    private void scheduleMissingDataTask() {
        int delayMillis = ThreadLocalRandom.current()
                .nextInt(MISSING_DELAY_LOW_MILLIS, MISSING_DELAY_HIGH_MILLIS);
        mScheduler.schedule(this::missingDataTask, delayMillis);
    }

    private void missingDataTask() {
        if (tryAcquireShared()) {
            if (mLocalMode == MODE_LEADER) {
                mMissingContigIndex = Long.MAX_VALUE;
                mSkipMissingDataTask = true;
                releaseShared();
                return;
            }
            releaseShared();
        }

        if (mReceivingMissingData) {
            mReceivingMissingData = false;
        }

        class Collector implements IndexRange {
            long[] mRanges;
            int mSize;

            @Override
            public void range(long startIndex, long endIndex) {
                if (mRanges == null) {
                    mRanges = new long[16];
                } else if (mSize >= mRanges.length) {
                    mRanges = Arrays.copyOf(mRanges, mRanges.length << 1);
                }
                mRanges[mSize++] = startIndex;
                mRanges[mSize++] = endIndex;
            }
        }
        ;

        Collector collector = new Collector();
        mMissingContigIndex = mStateLog.checkForMissingData(mMissingContigIndex, collector);

        for (int i = 0; i < collector.mSize; ) {
            long startIndex = collector.mRanges[i++];
            long endIndex = collector.mRanges[i++];
            requestMissingData(startIndex, endIndex);
        }

        scheduleMissingDataTask();
    }

    private void requestMissingData(final long startIndex, final long endIndex) {
        event(Level.FINE, () -> "Requesting missing data: [" + startIndex + ", " + endIndex + ')');

        // TODO: 需要一种方法来中止未完成的请求。

        long remaining = endIndex - startIndex;
        long index = startIndex;


        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        acquireShared();
        Channel[] channels = mConsensusChannels;
        releaseShared();

        doRequestData:
        while (remaining > 0) {
            long amt = Math.min(remaining, MISSING_DATA_REQUEST_SIZE);

            int selected = rnd.nextInt(channels.length);
            int attempts = 0;

            while (true) {
                Channel channel = channels[selected];

                if (channel.queryData(this, index, index + amt)) {
                    break;
                }

                if (++attempts >= channels.length) {
                    break doRequestData;
                }

                selected++;
                if (selected >= channels.length) {
                    selected = 0;
                }
            }

            index += amt;
            remaining -= amt;
        }
    }

    private void scheduleElectionTask() {
        int delayMillis = ThreadLocalRandom.current()
                .nextInt(ELECTION_DELAY_LOW_MILLIS, ELECTION_DELAY_HIGH_MILLIS);
        mScheduler.schedule(this::electionTask, delayMillis);
    }

    private void electionTask() {
        try {
            doElectionTask();
        } finally {
            scheduleElectionTask();
        }
    }

    /**
     * 执行选举任务
     */
    private void doElectionTask() {
        Channel[] peerChannels;
        long term, candidateId;
        LogInfo info;

        acquireExclusive();
        try {
            if (mLocalMode == MODE_LEADER) {
                doAffirmLeadership();
                return;
            }

            if (mElectionValidated >= 0) {
                mElectionValidated--;
                releaseExclusive();
                return;
            }

            if (mLocalMode == MODE_CANDIDATE) {
                toFollower("election timed out");
            }

            mLeaderReplyChannel = null;
            mLeaderRequestChannel = null;

            if (mGroupFile.localMemberRole() != Role.NORMAL) {
                releaseExclusive();
                return;
            }

            mLocalMode = MODE_CANDIDATE;

            peerChannels = mConsensusChannels;

            info = mStateLog.captureHighest();

            candidateId = mChanMan.getLocalMemberId();

            try {
                mCurrentTerm = term = mStateLog.incrementCurrentTerm(1, candidateId);
            } catch (IOException e) {
                releaseExclusive();
                uncaught(e);
                return;
            }

            mGrantsRemaining = (peerChannels.length + 1) / 2;

            mElectionValidated = 1;
        } catch (Throwable e) {
            releaseExclusive();
            throw e;
        }

        if (mGrantsRemaining == 0) {
            toLeader(term, info.mHighestIndex);
        } else {
            releaseExclusive();

            event(Level.INFO, "Local member is a candidate: term=" + term + ", highestTerm=" +
                    info.mTerm + ", highestIndex=" + info.mHighestIndex);

            for (Channel peerChan : peerChannels) {
                peerChan.requestVote(null, term, candidateId, info.mTerm, info.mHighestIndex);
            }
        }
    }

    private void affirmLeadership() {
        acquireExclusive();
        doAffirmLeadership();
    }

    /**
     * 进行维护领导地位
     */
    private void doAffirmLeadership() {
        LogWriter writer;
        long highestIndex, commitIndex;

        try {
            writer = mLeaderLogWriter;

            if (writer == null) {
                return;
            }

            mStateLog.captureHighest(writer);
            highestIndex = writer.mHighestIndex;
            commitIndex = writer.mCommitIndex;

            if (commitIndex >= highestIndex || commitIndex > mLeaderCommitIndex) {
                mElectionValidated = 5;
                mLeaderCommitIndex = commitIndex;
            } else if (mElectionValidated >= 0) {
                mElectionValidated--;
            } else {
                toFollower("commit index is stalled");
                return;
            }
        } finally {
            releaseExclusive();
        }

        Channel[] peerChannels = mAllChannels;

        long prevTerm = writer.prevTerm();
        long term = writer.term();
        long index = writer.index();

        for (Channel peerChan : peerChannels) {
            peerChan.writeData(null, prevTerm, term, index, highestIndex, commitIndex, EMPTY_DATA);
        }
    }

    private void toFollower(String reason) {
        final int originalMode = mLocalMode;

        if (originalMode != MODE_FOLLOWER) {
            mLocalMode = MODE_FOLLOWER;

            if (mLeaderLogWriter != null) {
                mLeaderLogWriter.release();
                mLeaderLogWriter = null;
            }

            if (mLeaderReplWriter != null) {
                mLeaderReplWriter.deactivate();
            }

            StringBuilder b = new StringBuilder("Local member ");

            if (originalMode == MODE_LEADER) {
                if (mSkipMissingDataTask) {
                    mSkipMissingDataTask = false;
                    scheduleMissingDataTask();
                }
                b.append("leadership");
            } else {
                b.append("candidacy");
            }

            b.append(" lost: ");

            if (reason == null) {
                b.append("term=").append(mCurrentTerm);
            } else {
                b.append(reason);
            }

            event(Level.INFO, b.toString());
        }
    }

    private void requestJoin(Socket s) {
        try {
            try {
                if (doRequestJoin(s)) {
                    return;
                }
            } catch (IllegalStateException e) {
                OutputStream out = s.getOutputStream();
                out.write(new byte[]{GroupJoiner.OP_ERROR, ErrorCodes.INVALID_ADDRESS});
            }
        } catch (Throwable e) {
            closeQuietly(s);
            rethrow(e);
        }

        closeQuietly(s);
    }

    /**
     * 接受加入集群请求
     *
     * @param s
     * @return
     * @throws IOException
     */
    private boolean doRequestJoin(Socket s) throws IOException {
        ChannelInputStream in = new ChannelInputStream(s.getInputStream(), 100);

        SocketAddress addr;
        long memberId;

        int op = in.read();

        switch (op) {
            default:
                return joinFailure(s, ErrorCodes.UNKNOWN_OPERATION);
            case GroupJoiner.OP_ADDRESS:
            case GroupJoiner.OP_UNJOIN_ADDRESS:
                addr = GroupFile.parseSocketAddress(in.readStr(in.readIntLE()));
                memberId = 0;
                break;
            case GroupJoiner.OP_UNJOIN_MEMBER:
                addr = null;
                memberId = in.readLongLE();
                break;
        }

        OutputStream out = s.getOutputStream();

        acquireShared();
        boolean isLeader = mLocalMode == MODE_LEADER;
        Channel leaderReplyChannel = mLeaderReplyChannel;
        releaseShared();

        if (!isLeader) {
            Peer leaderPeer;
            if (leaderReplyChannel == null || (leaderPeer = leaderReplyChannel.peer()) == null) {
                return joinFailure(s, ErrorCodes.NO_LEADER);
            }
            EncodingOutputStream eout = new EncodingOutputStream();
            eout.write(GroupJoiner.OP_ADDRESS);
            eout.encodeStr(leaderPeer.mAddress.toString());
            out.write(eout.toByteArray());
            return false;
        }

        Consumer<byte[]> acceptor = mControlMessageAcceptor;

        if (acceptor == null) {
            return joinFailure(s, ErrorCodes.NO_ACCEPTOR);
        }

        byte[] message;

        switch (op) {
            case GroupJoiner.OP_ADDRESS:
                /**
                 * 首次节点连接
                 * 重要: gfIn为组文件内容
                 * 在GroupFile的applyJoin中会将组文件读取发送至连接者，帮助他发现所有节点
                 */
                message = mGroupFile.proposeJoin(CONTROL_OP_JOIN, addr, (gfIn, index) -> {
                    try {
                        if (gfIn == null) {
                            out.write(new byte[]{
                                    GroupJoiner.OP_ERROR, ErrorCodes.VERSION_MISMATCH
                            });
                            return;
                        }

                        TermLog termLog = mStateLog.termLogAt(index);

                        byte[] buf = new byte[1000];
                        int off = 0;
                        buf[off++] = GroupJoiner.OP_JOINED;
                        encodeLongLE(buf, off, termLog.prevTermAt(index));
                        off += 8;
                        encodeLongLE(buf, off, termLog.term());
                        off += 8;
                        encodeLongLE(buf, off, index);
                        off += 8;

                        while (true) {
                            int amt;
                            try {
                                amt = gfIn.read(buf, off, buf.length - off);
                            } catch (IOException e) {
                                uncaught(e);
                                return;
                            }
                            if (amt < 0) {
                                break;
                            }
                            out.write(buf, 0, off + amt);
                            off = 0;
                        }
                    } catch (IOException e) {
                        // Ignore.
                    } finally {
                        closeQuietly(out);
                    }
                });

                break;

            case GroupJoiner.OP_UNJOIN_ADDRESS:
                if (mGroupFile.localMemberAddress().equals(addr)) {
                    memberId = mGroupFile.localMemberId();
                } else {
                    for (Peer peer : mGroupFile.allPeers()) {
                        if (peer.mAddress.equals(addr)) {
                            memberId = peer.mMemberId;
                            break;
                        }
                    }
                }

            case GroupJoiner.OP_UNJOIN_MEMBER:
                message = mGroupFile.proposeRemovePeer(CONTROL_OP_UNJOIN, memberId, success -> {
                    try {
                        byte[] reply;

                        if (success) {
                            reply = new byte[]{GroupJoiner.OP_UNJOINED};
                        } else {
                            reply = new byte[]{
                                    GroupJoiner.OP_ERROR, ErrorCodes.VERSION_MISMATCH
                            };
                        }

                        out.write(reply);
                    } catch (IOException e) {
                        // Ignore.
                    } finally {
                        closeQuietly(out);
                    }
                });
                break;

            default:
                throw new AssertionError();
        }

        mScheduler.schedule(() -> {
            if (mGroupFile.discardProposeConsumer(message)) {
                closeQuietly(s);
            }
        }, JOIN_TIMEOUT_MILLIS);

        acceptor.accept(message);
        return true;
    }

    private static boolean joinFailure(Socket s, byte errorCode) throws IOException {
        s.getOutputStream().write(new byte[]{GroupJoiner.OP_ERROR, errorCode});
        return false;
    }

    private void roleChangeTask() {
        acquireShared();
        Role desiredRole = mLocalRole;
        if (desiredRole == mGroupFile.localMemberRole()) {
            releaseShared();
            return;
        }
        long groupVersion = mGroupFile.version();
        long localMemberId = mGroupFile.localMemberId();
        releaseShared();

        Channel requestChannel = leaderRequestChannel();

        if (requestChannel != null) {
            requestChannel.updateRole(this, groupVersion, localMemberId, desiredRole);
        }

        // Check again later.
        mScheduler.schedule(this::roleChangeTask, ELECTION_DELAY_LOW_MILLIS);
    }

    private Channel leaderRequestChannel() {
        Channel requestChannel;
        Channel replyChannel;

        acquireShared();
        boolean exclusive = false;

        while (true) {
            if (mLocalMode == MODE_LEADER) {
                release(exclusive);
                return this;
            }
            requestChannel = mLeaderRequestChannel;
            if (requestChannel != null || (replyChannel = mLeaderReplyChannel) == null) {
                release(exclusive);
                return requestChannel;
            }
            if (exclusive || tryUpgrade()) {
                break;
            }
            releaseShared();
            acquireExclusive();
            exclusive = true;
        }

        Peer leader = replyChannel.peer();

        for (Channel channel : mAllChannels) {
            if (leader.equals(channel.peer())) {
                mLeaderRequestChannel = requestChannel = channel;
                break;
            }
        }

        releaseExclusive();

        return requestChannel;
    }

    private void event(Level level, String message) {
        if (mEventListener != null) {
            try {
                mEventListener.accept(level, message);
            } catch (Throwable e) {
                // Ignore.
            }
        }
    }

    private void event(Level level, Supplier<String> message) {
        if (mEventListener != null) {
            try {
                mEventListener.accept(level, message.get());
            } catch (Throwable e) {
                // Ignore.
            }
        }
    }

    @Override
    public void unknown(Channel from, int op) {
        event(Level.WARNING,
                "Unknown operation received from: " + from.peer().mAddress + ", op=" + op);
    }

    @Override
    public boolean nop(Channel from) {
        return true;
    }

    @Override
    public boolean requestVote(Channel from, long term, long candidateId,
                               long highestTerm, long highestIndex) {
        long currentTerm;
        acquireExclusive();
        try {
            final long originalTerm = mCurrentTerm;

            mCurrentTerm = currentTerm = mStateLog.checkCurrentTerm(term);

            if (currentTerm > originalTerm) {
                toFollower(null);
            }

            if (currentTerm >= originalTerm && !isBehind(highestTerm, highestIndex)) {
                if (mStateLog.checkCandidate(candidateId)) {
                    currentTerm |= 1L << 63;
                    mElectionValidated = 1;
                }
            }
        } catch (IOException e) {
            uncaught(e);
            return false;
        } finally {
            releaseExclusive();
        }

        from.requestVoteReply(null, currentTerm);
        return true;
    }

    private boolean isBehind(long term, long index) {
        LogInfo info = mStateLog.captureHighest();
        return term < info.mTerm || (term == info.mTerm && index < info.mHighestIndex);
    }

    @Override
    public boolean requestVoteReply(Channel from, long term) {
        acquireExclusive();

        final long originalTerm = mCurrentTerm;
        String reason;

        if (term < 0 && (term &= ~(1L << 63)) == originalTerm) {
            if (--mGrantsRemaining > 0 || mLocalMode != MODE_CANDIDATE) {
                releaseExclusive();
                return true;
            }

            if (mLocalMode == MODE_CANDIDATE) {
                LogInfo info = mStateLog.captureHighest();
                toLeader(term, info.mHighestIndex);
                return true;
            }

            reason = "stale vote reply";
        } else {
            try {
                mCurrentTerm = mStateLog.checkCurrentTerm(term);
            } catch (IOException e) {
                releaseExclusive();
                uncaught(e);
                return false;
            }

            if (mCurrentTerm <= originalTerm) {
                releaseExclusive();
                return true;
            }

            reason = "vote denied";
        }

        toFollower(reason);
        releaseExclusive();
        return true;
    }

    private void toLeader(long term, long index) {
        try {
            event(Level.INFO, "Local member is the leader: term=" + term + ", index=" + index);

            long prevTerm = mStateLog.termLogAt(index).prevTermAt(index);

            mLeaderLogWriter = mStateLog.openWriter(prevTerm, term, index);
            mLocalMode = MODE_LEADER;
            for (Channel channel : mAllChannels) {
                channel.peer().mMatchIndex = 0;
            }

            if (mConsensusPeers.length == 0) {
                mStateLog.captureHighest(mLeaderLogWriter);
                mStateLog.commit(mLeaderLogWriter.mHighestIndex);
            }
        } catch (Throwable e) {
            releaseExclusive();
            uncaught(e);
            return;
        }

        doAffirmLeadership();
    }

    @Override
    public boolean queryTerms(Channel from, long startIndex, long endIndex) {
        mStateLog.queryTerms(startIndex, endIndex, (prevTerm, term, index) -> {
            from.queryTermsReply(null, prevTerm, term, index);
        });

        return true;
    }

    @Override
    public boolean queryTermsReply(Channel from, long prevTerm, long term, long startIndex) {
        try {
            queryReplyTermCheck(term);

            mStateLog.defineTerm(prevTerm, term, startIndex);
        } catch (IOException e) {
            uncaught(e);
        }

        return true;
    }

    private void queryReplyTermCheck(long term) throws IOException {
        acquireShared();
        long originalTerm = mCurrentTerm;

        if (term < originalTerm) {
            releaseShared();
            return;
        }

        if (!tryUpgrade()) {
            releaseShared();
            acquireExclusive();
            originalTerm = mCurrentTerm;
        }

        try {
            if (term > originalTerm) {
                mCurrentTerm = mStateLog.checkCurrentTerm(term);
                if (mCurrentTerm > originalTerm) {
                    toFollower(null);
                }
            }
        } finally {
            releaseExclusive();
        }
    }

    @Override
    public boolean queryData(Channel from, long startIndex, long endIndex) {
        if (endIndex <= startIndex) {
            return true;
        }

        try {
            LogReader reader = mStateLog.openReader(startIndex);

            try {
                long remaining = endIndex - startIndex;
                byte[] buf = new byte[(int) Math.min(9000, remaining)];

                while (true) {
                    long currentTerm = 0;

                    long prevTerm = reader.prevTerm();
                    long index = reader.index();
                    long term = reader.term();

                    int require = (int) Math.min(buf.length, remaining);
                    int amt = reader.tryRead(buf, 0, require);

                    if (amt == 0) {
                        acquireShared();
                        try {
                            if (mLocalMode == MODE_LEADER) {
                                int any = reader.tryReadAny(buf, amt, require);
                                if (any > 0) {
                                    currentTerm = mCurrentTerm;
                                    amt += any;
                                }
                            }
                        } finally {
                            releaseShared();
                        }
                    }

                    if (amt <= 0) {
                        if (amt < 0) {
                            reader.release();
                            reader = mStateLog.openReader(startIndex);
                        }
                        break;
                    }

                    byte[] data = new byte[amt];
                    System.arraycopy(buf, 0, data, 0, amt);

                    from.queryDataReply(null, currentTerm, prevTerm, term, index, data);

                    startIndex += amt;
                    remaining -= amt;

                    if (remaining <= 0) {
                        break;
                    }
                }
            } finally {
                reader.release();
            }
        } catch (IOException e) {
            uncaught(e);
        }

        return true;
    }

    @Override
    public boolean queryDataReply(Channel from, long currentTerm,
                                  long prevTerm, long term, long index, byte[] data) {
        if (currentTerm != 0 && !validateLeaderTerm(from, currentTerm)) {
            return false;
        }

        mReceivingMissingData = true;

        try {
            queryReplyTermCheck(term);

            LogWriter writer = mStateLog.openWriter(prevTerm, term, index);
            if (writer != null) {
                try {
                    writer.write(data, 0, data.length, 0);
                } finally {
                    writer.release();
                }
            }
        } catch (IOException e) {
            uncaught(e);
        }

        return true;
    }

    /**
     * 接受数据写入Log中
     *
     * @param from
     * @param prevTerm
     * @param term
     * @param index
     * @param highestIndex
     * @param commitIndex
     * @param data
     * @return
     */
    @Override
    public boolean writeData(Channel from, long prevTerm, long term, long index,
                             long highestIndex, long commitIndex, byte[] data) {
        if (!validateLeaderTerm(from, term)) {
            return false;
        }

        try {
            LogWriter writer = mStateLog.openWriter(prevTerm, term, index);

            if (writer == null) {
                long now = System.currentTimeMillis();
                if (now >= mNextQueryTermTime) {
                    LogInfo info = mStateLog.captureHighest();
                    if (highestIndex > info.mCommitIndex && index > info.mCommitIndex) {
                        Channel requestChannel = leaderRequestChannel();
                        if (requestChannel != null && requestChannel != this) {
                            requestChannel.queryTerms(this, info.mCommitIndex, index);
                        }
                    }
                    mNextQueryTermTime = now + QUERY_TERMS_RATE_MILLIS;
                }

                return true;
            }

            try {
                writer.write(data, 0, data.length, highestIndex);
                mStateLog.commit(commitIndex);
                mStateLog.captureHighest(writer);
                long highestTerm = writer.mTerm;
                if (highestTerm < term) {
                    return true;
                }
                term = highestTerm;
                highestIndex = writer.mHighestIndex;
            } finally {
                writer.release();
            }
        } catch (IOException e) {
            uncaught(e);
        }

        from.writeDataReply(null, term, highestIndex);
        return true;
    }

    private boolean validateLeaderTerm(Channel from, long term) {
        acquireShared();
        long originalTerm = mCurrentTerm;

        vadidate:
        {
            if (term == originalTerm) {
                if (mElectionValidated > 0) {
                    releaseShared();
                    return true;
                }
                if (tryUpgrade()) {
                    break vadidate;
                }
            } else if (term < originalTerm) {
                releaseShared();
                return false;
            }

            if (!tryUpgrade()) {
                releaseShared();
                acquireExclusive();
                originalTerm = mCurrentTerm;
                if (term < originalTerm) {
                    releaseExclusive();
                    return false;
                }
            }

            if (term != originalTerm) {
                try {
                    assert term > originalTerm;
                    try {
                        mCurrentTerm = mStateLog.checkCurrentTerm(term);
                    } catch (IOException e) {
                        uncaught(e);
                        releaseExclusive();
                        return false;
                    }
                    if (mCurrentTerm <= originalTerm) {
                        releaseExclusive();
                        return true;
                    }
                    toFollower(null);
                } catch (Throwable e) {
                    releaseExclusive();
                    throw e;
                }
            }
        }

        mLeaderReplyChannel = from;
        mElectionValidated = 1;

        boolean first = false;
        if (term != mValidatedTerm) {
            mValidatedTerm = term;
            first = true;
        }

        releaseExclusive();

        if (first) {
            event(Level.INFO, "Remote member is the leader: " + from.peer().mAddress +
                    ", term=" + term);
        }

        return true;
    }

    @Override
    public boolean writeDataReply(Channel from, long term, long highestIndex) {
        long commitIndex;

        acquireExclusive();
        try {
            if (mLocalMode != MODE_LEADER) {
                return true;
            }

            final long originalTerm = mCurrentTerm;

            if (term != originalTerm) {
                try {
                    mCurrentTerm = mStateLog.checkCurrentTerm(term);
                } catch (IOException e) {
                    uncaught(e);
                    return false;
                }

                if (mCurrentTerm > originalTerm) {
                    toFollower(null);
                } else {
                    // 无法代表较早的条款提交。
                }
                return true;
            }

            Peer peer = from.peer();
            long matchIndex = peer.mMatchIndex;

            if (highestIndex <= matchIndex || mConsensusPeers.length == 0) {
                return true;
            }

            peer.mMatchIndex = highestIndex;
            Arrays.sort(mConsensusPeers);

            commitIndex = mConsensusPeers[mConsensusPeers.length >> 1].mMatchIndex;
        } finally {
            releaseExclusive();
        }

        mStateLog.commit(commitIndex);
        return true;
    }

    @Override
    public boolean syncCommit(Channel from, long prevTerm, long term, long index) {
        try {
            if (mStateLog.syncCommit(prevTerm, term, index) >= index) {
                from.syncCommitReply(null, mGroupFile.version(), term, index);
            }
            return true;
        } catch (IOException e) {
            uncaught(e);
            return false;
        }
    }

    @Override
    public boolean syncCommitReply(Channel from, long groupVersion, long term, long index) {
        checkGroupVersion(groupVersion);

        long durableIndex;

        acquireExclusive();
        try {
            TermLog termLog;
            if (mConsensusPeers.length == 0
                    || (termLog = mStateLog.termLogAt(index)) == null || term != termLog.term()) {
                return true;
            }

            Peer peer = from.peer();
            long syncMatchIndex = peer.mSyncMatchIndex;

            if (index > syncMatchIndex) {
                peer.mSyncMatchIndex = index;
            }

            Arrays.sort(mConsensusPeers, (a, b) ->
                    Long.compare(a.mSyncMatchIndex, b.mSyncMatchIndex));

            durableIndex = mConsensusPeers[mConsensusPeers.length >> 1].mSyncMatchIndex;
        } finally {
            releaseExclusive();
        }

        try {
            if (mStateLog.commitDurable(durableIndex)) {
                acquireExclusive();
                try {
                    mSyncCommitCondition.signalAll();
                } finally {
                    releaseExclusive();
                }
            }
        } catch (IOException e) {
            uncaught(e);
        }

        return true;
    }

    @Override
    public boolean compact(Channel from, long index) {
        from.peer().mCompactIndex = index;
        return true;
    }

    @Override
    public boolean snapshotScore(Channel from) {
        if (!mChanMan.hasSnapshotRequestAcceptor()) {
            from.snapshotScoreReply(null, Integer.MAX_VALUE, Float.POSITIVE_INFINITY);
            return true;
        }

        acquireShared();
        int sessionCount = mSnapshotSessionCount;
        int mode = mLocalMode;
        releaseShared();

        from.snapshotScoreReply(null, sessionCount, mode == MODE_LEADER ? 1 : -1);

        return true;
    }

    @Override
    public boolean snapshotScoreReply(Channel from, int activeSessions, float weight) {
        from.peer().snapshotScoreReply(activeSessions, weight);
        return true;
    }

    @Override
    public boolean updateRole(Channel from, long groupVersion, long memberId, Role role) {
        Consumer<byte[]> acceptor;
        byte[] message = null;
        byte result;

        acquireShared();
        tryUpdateRole:
        try {
            final long givenVersion = groupVersion;
            groupVersion = mGroupFile.version();

            acceptor = mControlMessageAcceptor;

            if (acceptor == null) {
                result = ErrorCodes.NO_ACCEPTOR;
                break tryUpdateRole;
            }

            if (mLocalMode != MODE_LEADER) {
                result = ErrorCodes.NOT_LEADER;
                break tryUpdateRole;
            }

            if (givenVersion != groupVersion) {
                result = ErrorCodes.VERSION_MISMATCH;
                break tryUpdateRole;
            }

            Peer key = new Peer(memberId);
            Peer peer = mGroupFile.allPeers().ceiling(key);

            if (peer == null || peer.mMemberId != memberId) {
                result = ErrorCodes.UNKNOWN_MEMBER;
                break tryUpdateRole;
            }

            if (peer.mRole == role) {
                result = ErrorCodes.SUCCESS;
                break tryUpdateRole;
            }

            message = mGroupFile.proposeUpdateRole(CONTROL_OP_UPDATE_ROLE, memberId, role);

            if (peer.mRole == Role.OBSERVER || role == Role.OBSERVER) {
                int count = 0;
                for (Peer cp : mConsensusPeers) {
                    if (cp.mGroupVersion == groupVersion) {
                        count++;
                    }
                }

                if (count < ((mConsensusPeers.length + 1) >> 1)) {
                    message = null;
                    result = ErrorCodes.NO_CONSENSUS;
                    for (Channel channel : mConsensusChannels) {
                        channel.groupVersion(this, groupVersion);
                    }

                    break tryUpdateRole;
                }
            }

            result = 0;
        } finally {
            releaseShared();
        }

        if (message != null) {
            acceptor.accept(message);
        }

        from.updateRoleReply(null, groupVersion, memberId, result);

        return true;
    }

    @Override
    public boolean updateRoleReply(Channel from, long groupVersion, long memberId, byte result) {
        if (result != ErrorCodes.SUCCESS) {
            acquireShared();
            boolean ok = mLocalRole == mGroupFile.localMemberRole();
            releaseShared();
            if (!ok) {
                event(ErrorCodes.levelFor(result),
                        "Unable to update role: " + ErrorCodes.toString(result));
            }
        }

        checkGroupVersion(groupVersion);

        return true;
    }

    @Override
    public boolean groupVersion(Channel from, long groupVersion) {
        from.peer().updateGroupVersion(groupVersion);
        from.groupVersionReply(null, mGroupFile.version());
        return true;
    }

    @Override
    public boolean groupVersionReply(Channel from, long groupVersion) {
        from.peer().updateGroupVersion(groupVersion);
        return true;
    }

    @Override
    public boolean groupFile(Channel from, long groupVersion) throws IOException {
        if (groupVersion < mGroupFile.version()) {
            OutputStream out = from.groupFileReply(null, null);
            if (out != null) {
                mGroupFile.writeTo(out);
                out.flush();
            }
        }
        return true;
    }

    @Override
    public OutputStream groupFileReply(Channel from, InputStream in) throws IOException {
        boolean refresh;
        Role oldRole = null, newRole = null;

        acquireExclusive();
        try {
            oldRole = mGroupFile.localMemberRole();
            refresh = mGroupFile.readFrom(in);
            if (refresh) {
                refreshPeerSet();
                newRole = mGroupFile.localMemberRole();
            }
        } finally {
            releaseExclusive();
        }

        if (refresh) {
            Channel requestChannel = leaderRequestChannel();
            if (requestChannel != null && requestChannel != this) {
                requestChannel.groupVersion(this, mGroupFile.version());
            }
        }

        return null;
    }

    private void checkGroupVersion(long groupVersion) {
        if (groupVersion > mGroupFile.version()) {
            Channel requestChannel = leaderRequestChannel();
            if (requestChannel != null && requestChannel != this) {
                try {
                    requestChannel.groupFile(this, mGroupFile.version());
                } catch (IOException e) {
                    // Ignore.
                }
            }
        }
    }
}
