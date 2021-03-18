package com.linglong.replication;

import com.linglong.base.common.Utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Map;

/**
 * @author Stereo
 */
final class SocketSnapshotReceiver implements SnapshotReceiver {
    private final Socket mSocket;
    private final long mLength;
    private final long mPrevTerm;
    private final long mTerm;
    private final long mIndex;
    private final Map<String, String> mOptions;

    SocketSnapshotReceiver(GroupFile groupFile, Socket socket, Map<String, String> requestOptions)
            throws IOException {
        OptionsEncoder enc = new OptionsEncoder();
        enc.encodeIntLE(0); // encoding format
        enc.encodeMap(requestOptions == null ? Collections.emptyMap() : requestOptions);
        enc.writeTo(socket.getOutputStream());

        OptionsDecoder dec;
        try {
            dec = new OptionsDecoder(socket.getInputStream());
        } catch (EOFException e) {
            e.printStackTrace();
            Utils.closeQuietly(socket);
            throw new IOException("Disconnected");
        }

        int encoding = dec.decodeIntLE();
        if (encoding != 0) {
            Utils.closeQuietly(socket);
            throw new IOException("Unknown encoding: " + encoding);
        }

        mSocket = socket;
        mLength = dec.decodeLongLE();
        mPrevTerm = dec.decodeLongLE();
        mTerm = dec.decodeLongLE();
        mIndex = dec.decodeLongLE();
        mOptions = dec.decodeMap();

        groupFile.readFrom(socket.getInputStream());
    }

    @Override
    public SocketAddress senderAddress() {
        return mSocket.getRemoteSocketAddress();
    }

    @Override
    public Map<String, String> options() {
        return mOptions;
    }

    @Override
    public long length() {
        return mLength;
    }

    public long prevTerm() {
        return mPrevTerm;
    }

    public long term() {
        return mTerm;
    }

    @Override
    public long index() {
        return mIndex;
    }

    @Override
    public InputStream inputStream() throws IOException {
        return mSocket.getInputStream();
    }

    @Override
    public void close() throws IOException {
        mSocket.close();
    }

    @Override
    public String toString() {
        return "SnapshotReceiver: {sender=" + senderAddress() + ", length=" + length() +
                ", prevTerm=" + prevTerm() + ", term=" + term() + ", index=" + index() + '}';
    }
}
