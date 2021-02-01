package com.glodon.linglong.replication;

import com.glodon.linglong.replication.confg.ReplicatorConfig;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Stereo
 */
public interface StreamReplicator extends DirectReplicator {

    static StreamReplicator open(ReplicatorConfig config) throws IOException {
        if (config == null) {
            throw new IllegalArgumentException("No configuration");
        }

        File base = config.getBaseFile();
        if (base == null) {
            throw new IllegalArgumentException("No base file configured");
        }

        if (base.isDirectory()) {
            throw new IllegalArgumentException("Base file is a directory: " + base);
        }

        long groupToken = config.getGroupToken();
        if (groupToken == 0) {
            throw new IllegalArgumentException("No group token configured");
        }

        SocketAddress localAddress = config.getLocalAddress();
        if (localAddress == null) {
            throw new IllegalArgumentException("No local address configured");
        }

        SocketAddress listenAddress = config.getListenAddress();
        ServerSocket localSocket = config.getLocalSocket();

        if (listenAddress == null) {
            listenAddress = localAddress;
            if (listenAddress instanceof InetSocketAddress) {
                int port = ((InetSocketAddress) listenAddress).getPort();
                listenAddress = new InetSocketAddress(port);
            }
        }

        if (localSocket == null) {
            localSocket = ChannelManager.newServerSocket(listenAddress);
        }

        Set<SocketAddress> seeds = config.getSeeds();

        if (seeds == null) {
            seeds = Collections.emptySet();
        }

        if (config.isMkdirs()) {
            base.getParentFile().mkdirs();
        }

        return Controller.open(config.getEventListener(),
                new FileStateLog(base), groupToken,
                new File(base.getPath() + ".group"),
                localAddress, listenAddress, config.getLocalRole(),
                seeds, localSocket);
    }

    @Override
    Reader newReader(long index, boolean follow);

    @Override
    Writer newWriter();

    @Override
    Writer newWriter(long index);

    void controlMessageReceived(long index, byte[] message) throws IOException;

    void controlMessageAcceptor(Consumer<byte[]> acceptor);


    interface Reader extends DirectReplicator.Reader {

        default int read(byte[] buf) throws IOException {
            return read(buf, 0, buf.length);
        }

        int read(byte[] buf, int offset, int length) throws IOException;

        default void readFully(byte[] buf, int offset, int length) throws IOException {
            while (true) {
                int amt = read(buf, offset, length);
                if (amt <= 0) {
                    throw new EOFException();
                }
                if ((length -= amt) <= 0) {
                    break;
                }
                offset += amt;
            }
        }
    }

    interface Writer extends DirectReplicator.Writer {

        default int write(byte[] messages) throws IOException {
            return write(messages, 0, messages.length);
        }

        default int write(byte[] messages, int offset, int length) throws IOException {
            return write(messages, offset, length, index() + length);
        }

        int write(byte[] messages, int offset, int length, long highestIndex) throws IOException;
    }
}
