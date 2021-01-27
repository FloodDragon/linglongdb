package com.glodon.linglong.replication;

import com.glodon.linglong.base.common.Utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * 复制通道输入流
 *
 * @author Stereo
 */
final class ChannelInputStream extends InputStream {
    private static final AtomicLongFieldUpdater<ChannelInputStream> cReadAmountUpdater =
            AtomicLongFieldUpdater.newUpdater(ChannelInputStream.class, "mReadAmount");

    private final InputStream mSource;
    private final byte[] mBuffer;
    private int mPos;
    private int mEnd;
    private volatile long mReadAmount;

    ChannelInputStream(InputStream source, int size) {
        mSource = source;
        mBuffer = new byte[size];
    }

    long resetReadAmount() {
        return cReadAmountUpdater.getAndSet(this, 0);
    }

    byte readByte() throws IOException {
        int b = read();
        if (b < 0) {
            throw new EOFException();
        }
        return (byte) b;
    }

    int readIntLE() throws IOException {
        fillBuffer(4);
        int pos = mPos;
        int value = Utils.decodeIntLE(mBuffer, pos);
        mPos = pos + 4;
        return value;
    }

    long readLongLE() throws IOException {
        fillBuffer(8);
        int pos = mPos;
        long value = Utils.decodeLongLE(mBuffer, pos);
        mPos = pos + 8;
        return value;
    }

    String readStr(int len) throws IOException {
        if (len <= mBuffer.length) {
            fillBuffer(len);
            String str = new String(mBuffer, mPos, len, StandardCharsets.UTF_8);
            mPos += len;
            return str;
        } else {
            byte[] b = new byte[len];
            readFully(b, 0, b.length);
            return new String(b, StandardCharsets.UTF_8);
        }
    }

    void readFully(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int amt = read(b, off, len);
            if (amt <= 0) {
                throw new EOFException();
            }
            off += amt;
            len -= amt;
        }
    }

    void skipFully(long n) throws IOException {
        while (n > 0) {
            long amt = skip(n);
            if (amt <= 0) {
                throw new EOFException();
            }
            n -= amt;
        }
    }

    @Override
    public int read() throws IOException {
        int pos = mPos;
        int avail = mEnd - pos;
        byte[] buf = mBuffer;

        if (avail <= 0) {
            avail = mSource.read(buf, 0, buf.length);
            if (avail <= 0) {
                return -1;
            }
            cReadAmountUpdater.getAndAdd(this, avail);
            pos = 0;
            mEnd = avail;
        }

        int b = buf[pos++] & 0xff;
        mPos = pos;
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int avail = mEnd - mPos;
        byte[] buf = mBuffer;

        if (avail <= 0) {
            if (len >= buf.length) {
                int amt = mSource.read(b, off, len);
                if (amt > 0) {
                    cReadAmountUpdater.getAndAdd(this, amt);
                }
                return amt;
            }
            avail = mSource.read(buf, 0, buf.length);
            if (avail <= 0) {
                return -1;
            }
            cReadAmountUpdater.getAndAdd(this, avail);
            mPos = 0;
            mEnd = avail;
        }

        len = Math.min(avail, len);
        System.arraycopy(buf, mPos, b, off, len);
        mPos += len;
        return len;
    }

    @Override
    public long skip(long n) throws IOException {
        int avail = mEnd - mPos;

        if (avail > 0) {
            if (n >= avail) {
                mPos = 0;
                mEnd = 0;
                return avail;
            }
            mPos += n;
            return n;
        }

        long amt = mSource.skip(n);

        if (amt > 0) {
            cReadAmountUpdater.getAndAdd(this, amt);
        }

        return amt;
    }

    @Override
    public int available() throws IOException {
        return mEnd - mPos + mSource.available();
    }

    @Override
    public void close() throws IOException {
        mPos = 0;
        mEnd = 0;
        mSource.close();
    }

    public void drainTo(OutputStream out) throws IOException {
        byte[] buf = mBuffer;

        int avail = mEnd - mPos;
        if (avail > 0) {
            out.write(buf, mPos, avail);
            mPos = 0;
            mEnd = 0;
        }

        int amt;
        while ((amt = mSource.read(buf)) > 0) {
            out.write(buf, 0, amt);
        }
    }

    private void fillBuffer(int required) throws IOException {
        int avail = mEnd - mPos;
        if ((required -= avail) > 0) {
            fillBuffer(required, avail);
        }
    }

    private void fillBuffer(int required, int avail) throws IOException {
        byte[] buf = mBuffer;
        int end = mEnd;
        int tail = buf.length - end;
        if (tail < required) {
            System.arraycopy(buf, mPos, buf, 0, avail);
            mPos = 0;
            mEnd = end = avail;
            tail = buf.length - end;
        }

        while (true) {
            avail = mSource.read(buf, end, tail);
            if (avail <= 0) {
                throw new EOFException();
            }
            cReadAmountUpdater.getAndAdd(this, avail);
            end += avail;
            mEnd = end;
            required -= avail;
            if (required <= 0) {
                break;
            }
            tail -= avail;
        }
    }
}
