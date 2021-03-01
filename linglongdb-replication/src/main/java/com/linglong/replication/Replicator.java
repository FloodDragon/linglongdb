
package com.linglong.replication;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.function.Consumer;

/**
 * @author Stereo
 */
public interface Replicator extends Closeable {
    
    long getLocalMemberId();

    SocketAddress getLocalAddress();

    Role getLocalRole();

    Socket connect(SocketAddress addr) throws IOException;

    void socketAcceptor(Consumer<Socket> acceptor);

    void sync() throws IOException;
}
