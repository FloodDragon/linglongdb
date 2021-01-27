package com.glodon.linglong.replication.confg;

import com.glodon.linglong.base.common.LocalHost;
import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.replication.GroupFile;
import com.glodon.linglong.replication.Role;

import java.io.File;
import java.io.Serializable;
import java.net.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.logging.Level;

/**
 * @author Stereo
 */
public class ReplicatorConfig implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;

    File mBaseFile;
    boolean mMkdirs;
    long mGroupToken;
    SocketAddress mLocalAddress;
    SocketAddress mListenAddress;
    ServerSocket mLocalSocket;
    Role mLocalRole;
    Set<SocketAddress> mSeeds;
    transient BiConsumer<Level, String> mEventListener;

    public ReplicatorConfig() {
        createFilePath(true);
        localRole(Role.NORMAL);
    }

    public ReplicatorConfig baseFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException();
        }
        mBaseFile = file.getAbsoluteFile();
        return this;
    }

    public ReplicatorConfig baseFilePath(String path) {
        if (path == null) {
            throw new IllegalArgumentException();
        }
        mBaseFile = new File(path).getAbsoluteFile();
        return this;
    }

    public ReplicatorConfig createFilePath(boolean mkdirs) {
        mMkdirs = mkdirs;
        return this;
    }

    public ReplicatorConfig groupToken(long groupToken) {
        if (groupToken == 0) {
            throw new IllegalArgumentException();
        }
        mGroupToken = groupToken;
        return this;
    }

    public ReplicatorConfig localPort(int port) throws UnknownHostException {
        if (port <= 0) {
            throw new IllegalArgumentException();
        }
        mLocalAddress = new InetSocketAddress(LocalHost.getLocalHost(), port);
        mListenAddress = new InetSocketAddress(port);
        return this;
    }

    public ReplicatorConfig localAddress(SocketAddress addr) {
        if (addr == null) {
            throw new IllegalArgumentException();
        }
        if (addr instanceof InetSocketAddress) {
            if (((InetSocketAddress) addr).getAddress().isAnyLocalAddress()) {
                throw new IllegalArgumentException("Wildcard address: " + addr);
            }
        }
        mLocalAddress = addr;
        return this;
    }

    public ReplicatorConfig listenAddress(SocketAddress addr) {
        if (addr == null) {
            throw new IllegalArgumentException();
        }
        mListenAddress = addr;
        return this;
    }

    public ReplicatorConfig localSocket(ServerSocket ss) throws UnknownHostException {
        mLocalSocket = ss;
        mListenAddress = ss.getLocalSocketAddress();
        mLocalAddress = mListenAddress;

        if (mLocalAddress instanceof InetSocketAddress) {
            InetSocketAddress sockAddr = (InetSocketAddress) mLocalAddress;
            InetAddress addr = sockAddr.getAddress();
            if (addr.isAnyLocalAddress()) {
                mLocalAddress = new InetSocketAddress
                        (LocalHost.getLocalHost(), sockAddr.getPort());
            }
        }

        return this;
    }

    public ReplicatorConfig localRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException();
        }
        mLocalRole = role;
        return this;
    }

    public ReplicatorConfig addSeed(String addressString) throws UnknownHostException {
        if (addressString == null) {
            throw new IllegalArgumentException();
        }
        SocketAddress addr = GroupFile.parseSocketAddress(addressString);
        if (addr == null) {
            throw new IllegalArgumentException("Malformed address: " + addressString);
        }
        return addSeed(addr);
    }

    public ReplicatorConfig addSeed(String hostname, int port) {
        return addSeed(new InetSocketAddress(hostname, port));
    }

    public ReplicatorConfig addSeed(SocketAddress addr) {
        if (addr == null) {
            throw new IllegalArgumentException();
        }
        if (mSeeds == null) {
            mSeeds = new HashSet<>();
        }
        mSeeds.add(addr);
        return this;
    }

    public ReplicatorConfig eventListener(BiConsumer<Level, String> listener) {
        mEventListener = listener;
        return this;
    }

    @Override
    public ReplicatorConfig clone() {
        ReplicatorConfig copy;
        try {
            copy = (ReplicatorConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw Utils.rethrow(e);
        }

        if (mSeeds != null) {
            copy.mSeeds = new HashSet<>(mSeeds);
        }
        return copy;
    }
}
