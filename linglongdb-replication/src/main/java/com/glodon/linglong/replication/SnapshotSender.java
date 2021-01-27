package com.glodon.linglong.replication;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketAddress;
import java.util.Map;

/**
 * @author Stereo
 */
public interface SnapshotSender extends Closeable {

    SocketAddress receiverAddress();

    Map<String, String> options();

    OutputStream begin(long length, long index, Map<String, String> options) throws IOException;
}
