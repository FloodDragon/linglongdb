package com.glodon.linglong.replication;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Stereo
 */
final class RestoreInputStream extends InputStream {
    private final InputStream mSource;

    private volatile long mReceived;
    private volatile boolean mFinished;

    RestoreInputStream(InputStream source) {
        mSource = source;
    }

    public long received() {
        return mReceived;
    }

    public boolean isFinished() {
        return mFinished;
    }

    @Override
    public int read() throws IOException {
        int b = mSource.read();
        if (b < 0) {
            mFinished = true;
        } else {
            mReceived++;
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int amt = mSource.read(b, off, len);
        if (amt < 0) {
            mFinished = true;
        } else {
            mReceived += amt;
        }
        return amt;
    }

    @Override
    public long skip(long n) throws IOException {
        n = mSource.skip(n);
        if (n < 0) {
            mFinished = true;
        } else {
            mReceived += n;
        }
        return n;
    }

    @Override
    public int available() throws IOException {
        return mSource.available();
    }

    @Override
    public void close() throws IOException {
        mFinished = true;
        mSource.close();
    }
}
