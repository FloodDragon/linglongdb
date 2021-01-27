package com.glodon.linglong.replication;

import com.glodon.linglong.base.common.Utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author Stereo
 */
abstract class SocketSnapshotSender extends OutputStream implements SnapshotSender {
    private final GroupFile mGroupFile;
    private final Socket mSocket;
    private final OutputStream mOut;
    private final Map<String, String> mOptions;

    private static final AtomicIntegerFieldUpdater<SocketSnapshotSender> cSendingUpdater =
            AtomicIntegerFieldUpdater.newUpdater(SocketSnapshotSender.class, "mSending");

    private volatile int mSending;

    SocketSnapshotSender(GroupFile groupFile, Socket socket) throws IOException {
        OptionsDecoder dec;
        try {
            dec = new OptionsDecoder(socket.getInputStream());
        } catch (EOFException e) {
            Utils.closeQuietly(socket);
            throw new IOException("Disconnected");
        }

        int encoding = dec.decodeIntLE();
        if (encoding != 0) {
            Utils.closeQuietly(socket);
            throw new IOException("Unknown encoding: " + encoding);
        }

        mGroupFile = groupFile;
        mSocket = socket;
        mOut = socket.getOutputStream();
        mOptions = dec.decodeMap();
    }

    @Override
    public final SocketAddress receiverAddress() {
        return mSocket.getRemoteSocketAddress();
    }

    @Override
    public final Map<String, String> options() {
        return mOptions;
    }

    @Override
    public final OutputStream begin(long length, long index, Map<String, String> options)
            throws IOException {
        if (!cSendingUpdater.compareAndSet(this, 0, 1)) {
            throw new IllegalStateException("Already began");
        }

        try {
            TermLog termLog = termLogAt(index);
            if (termLog == null) {
                throw new IllegalStateException("Unknown term at index: " + index);
            }

            OptionsEncoder enc = new OptionsEncoder();
            enc.encodeIntLE(0); // encoding format
            enc.encodeLongLE(length);
            enc.encodeLongLE(termLog.prevTermAt(index));
            enc.encodeLongLE(termLog.term());
            enc.encodeLongLE(index);
            enc.encodeMap(options == null ? Collections.emptyMap() : options);
            enc.writeTo(this);

            mGroupFile.writeTo(this);

            return this;
        } catch (Throwable e) {
            Utils.closeQuietly(this);
            throw e;
        }
    }

    @Override
    public final void write(int b) throws IOException {
        mOut.write(b);
    }

    @Override
    public final void write(byte[] b, int off, int len) throws IOException {
        mOut.write(b, off, len);
    }

    @Override
    public final void flush() throws IOException {
        mOut.flush();
    }

    @Override
    public void close() throws IOException {
        mSocket.close();
    }

    abstract TermLog termLogAt(long index) throws IOException;
}
