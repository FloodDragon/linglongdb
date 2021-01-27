
package com.glodon.linglong.replication;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.Map;

/**
 * @author Stereo
 */
public interface SnapshotReceiver extends Closeable {

    SocketAddress senderAddress();

    Map<String, String> options();

    long length();

    long index();

    InputStream inputStream() throws IOException;
}
