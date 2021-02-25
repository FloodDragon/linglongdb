package com.glodon.linglong.replication;

import com.glodon.linglong.base.common.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * (集群俱乐部)用于组内成员进行相互发现
 *
 * @author Stereo
 */
class GroupJoiner {
    static final int OP_NOP = 0, OP_ERROR = 1, OP_ADDRESS = 2, OP_JOINED = 3,
            OP_UNJOIN_ADDRESS = 4, OP_UNJOIN_MEMBER = 5, OP_UNJOINED = 6;

    private final BiConsumer<Level, String> mEventListener;
    private final File mFile;
    private final long mGroupToken;
    private final SocketAddress mLocalAddress;
    private final SocketAddress mBindAddress;

    private Selector mSelector;
    private SocketChannel[] mSeedChannels;
    private SocketChannel mLeaderChannel;

    GroupFile mGroupFile;
    boolean mReplySuccess;

    long mPrevTerm;
    long mTerm;
    long mIndex;

    GroupJoiner(BiConsumer<Level, String> eventListener, File groupFile, long groupToken,
                SocketAddress localAddress, SocketAddress listenAddress) {
        mEventListener = eventListener;
        mFile = groupFile;
        mGroupToken = groupToken;
        mLocalAddress = localAddress;

        SocketAddress bindAddr = null;

        if (listenAddress instanceof InetSocketAddress) {
            bindAddr = new InetSocketAddress(((InetSocketAddress) listenAddress).getAddress(), 0);
        }

        mBindAddress = bindAddr;
    }

    GroupJoiner(long groupToken) {
        this(null, null, groupToken, null, null);
    }

    /**
     * 加入集群
     *
     * @param seeds
     * @param timeoutMillis
     * @throws IOException
     */
    void join(Set<SocketAddress> seeds, int timeoutMillis) throws IOException {
        try {
            doJoin(seeds, timeoutMillis, out -> {
                out.write(OP_ADDRESS);
                out.encodeStr(mLocalAddress.toString());
            });
        } finally {
            close();
        }
    }

    /**
     * 退出集群
     *
     * @param seeds
     * @param timeoutMillis
     * @param memberId
     * @throws IOException
     */
    void unjoin(Set<SocketAddress> seeds, int timeoutMillis, long memberId) throws IOException {
        try {
            doJoin(seeds, timeoutMillis, out -> {
                out.write(OP_UNJOIN_MEMBER);
                out.encodeLongLE(memberId);
            });
        } finally {
            close();
        }
    }

    /**
     * 退出集群
     *
     * @param seeds
     * @param timeoutMillis
     * @param memberAddr
     * @throws IOException
     */
    void unjoin(Set<SocketAddress> seeds, int timeoutMillis, SocketAddress memberAddr)
            throws IOException {
        try {
            doJoin(seeds, timeoutMillis, out -> {
                out.write(OP_UNJOIN_ADDRESS);
                out.encodeStr(memberAddr.toString());
            });
        } finally {
            close();
        }
    }

    private void doJoin(Set<SocketAddress> seeds, long timeoutMillis,
                        Consumer<EncodingOutputStream> cout)
            throws IOException {
        if (seeds == null) {
            throw new IllegalArgumentException();
        }

        if (mSeedChannels != null) {
            throw new IllegalStateException();
        }

        EncodingOutputStream out = new EncodingOutputStream();
        out.write(ChannelManager.newConnectHeader(mGroupToken, 0, 0, ChannelManager.TYPE_JOIN));

        cout.accept(out);

        final byte[] command = out.toByteArray();

        mSelector = Selector.open();
        mSeedChannels = new SocketChannel[seeds.size()];

        int i = 0;
        for (SocketAddress addr : seeds) {
            SocketChannel channel = SocketChannel.open();
            mSeedChannels[i++] = channel;
            prepareChannel(channel, addr);
        }

        int expected = seeds.size();

        Set<String> joinFailureMessages = new TreeSet<>();
        Set<String> connectFailureMessages = new TreeSet<>();

        long end = System.currentTimeMillis() + timeoutMillis;

        while (expected > 0 && timeoutMillis > 0) {
            mSelector.select(timeoutMillis);

            Set<SelectionKey> keys = mSelector.selectedKeys();

            for (SelectionKey key : keys) {
                SocketChannel channel = (SocketChannel) key.channel();

                try {
                    if (key.isConnectable()) {
                        channel.finishConnect();
                        key.interestOps(SelectionKey.OP_WRITE);
                    } else if (key.isWritable()) {
                        channel.write(ByteBuffer.wrap(command));
                        key.interestOps(SelectionKey.OP_READ);
                    } else {
                        key.cancel();
                        channel.configureBlocking(true);
                        SocketAddress addr = processReply(channel.socket(), timeoutMillis);
                        expected--;

                        if (addr != null && mLeaderChannel == null && !seeds.contains(addr)) {
                            expected++;
                            channel = SocketChannel.open();
                            prepareChannel(channel, addr);
                            mLeaderChannel = channel;
                        }
                    }
                } catch (JoinException e) {
                    Utils.closeQuietly(channel);
                    expected--;
                    joinFailureMessages.add(e.getMessage());
                } catch (IOException e) {
                    Utils.closeQuietly(channel);
                    expected--;
                    connectFailureMessages.add(e.toString());
                }
            }

            keys.clear();

            if (mReplySuccess) {
                return;
            }

            timeoutMillis = end - System.currentTimeMillis();
        }

        String fullMessage;

        Set<String> failureMessages = new LinkedHashSet<>();
        failureMessages.addAll(joinFailureMessages);
        failureMessages.addAll(connectFailureMessages);

        if (failureMessages.isEmpty()) {
            fullMessage = "timed out";
        } else {
            StringBuilder b = null;
            Iterator<String> it = failureMessages.iterator();

            while (true) {
                String message = it.next();
                if (b == null) {
                    if (!it.hasNext()) {
                        fullMessage = message;
                        break;
                    }
                    b = new StringBuilder();
                } else {
                    b.append("; ");
                }
                b.append(message);
                if (!it.hasNext()) {
                    fullMessage = b.toString();
                    break;
                }
            }
        }

        throw new JoinException(fullMessage);
    }

    private void prepareChannel(SocketChannel channel, SocketAddress addr) throws IOException {
        if (mBindAddress != null) {
            channel.bind(mBindAddress);
        }

        channel.configureBlocking(false);
        channel.register(mSelector, SelectionKey.OP_CONNECT);
        channel.connect(addr);
    }

    /**
     * 处理加入集群后结果(只有leader才会响应OP_JOINED指令,响应OP_ADDRESS代表需要重新访问leader节点)
     *
     * @param s
     * @param timeoutMillis
     * @return
     * @throws IOException
     */
    private SocketAddress processReply(Socket s, long timeoutMillis) throws IOException {
        InputStream in = s.getInputStream();

        if (timeoutMillis >= 0) {
            int intTimeout;
            if (timeoutMillis == 0) {
                intTimeout = 1;
            } else {
                intTimeout = (int) Math.min(timeoutMillis, Integer.MAX_VALUE);
            }
            s.setSoTimeout(intTimeout);
        }

        SocketAddress addr = null;

        byte[] header = ChannelManager.readHeader(in, mGroupToken, 0);

        if (header != null) {
            ChannelInputStream cin = new ChannelInputStream(in, 1000);
            int op = cin.read();
            if (op == OP_ADDRESS) {
                addr = GroupFile.parseSocketAddress(cin.readStr(cin.readIntLE()));
                if (!(addr instanceof InetSocketAddress)
                        || ((InetSocketAddress) addr).getAddress().isAnyLocalAddress()) {
                    // Invalid address.
                    addr = null;
                }
            } else if (op == OP_JOINED) {
                //加入集群后返回数据
                mPrevTerm = cin.readLongLE();
                mTerm = cin.readLongLE();
                mIndex = cin.readLongLE();
                //返回的地址数据落盘放到组文件里
                try (FileOutputStream out = new FileOutputStream(mFile)) {
                    cin.drainTo(out);
                }
                mGroupFile = GroupFile.open(mEventListener, mFile, mLocalAddress, false);
                mReplySuccess = true;
            } else if (op == OP_UNJOINED) {
                mReplySuccess = true;
            } else if (op == OP_ERROR) {
                throw new JoinException(ErrorCodes.toString(cin.readByte()));
            }
        }

        Utils.closeQuietly(s);

        return addr;
    }

    private void close() {
        Utils.closeQuietly(mSelector);
        mSelector = null;

        if (mSeedChannels != null) {
            for (SocketChannel channel : mSeedChannels) {
                Utils.closeQuietly(channel);
            }
            mSeedChannels = null;
        }

        Utils.closeQuietly(mLeaderChannel);
        mLeaderChannel = null;
    }
}
