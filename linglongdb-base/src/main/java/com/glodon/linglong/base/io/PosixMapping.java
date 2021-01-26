package com.glodon.linglong.base.io;

import com.glodon.linglong.base.util.UnsafeAccess;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Stereo
 */
@SuppressWarnings("restriction")
final class PosixMapping extends Mapping {
    private final DirectAccess mDirectAccess;
    private final long mAddr;
    private final int mSize;

    PosixMapping(int fd, boolean readOnly, long position, int size) throws IOException {
        mDirectAccess = new DirectAccess();
        int prot = readOnly ? 1 : (1 | 2); // PROT_READ | PROT_WRITE
        int flags = 1; // MAP_SHARED
        mAddr = PosixFileIO.mmapFd(size, prot, flags, fd, position);
        mSize = size;
    }

    @Override
    void read(int start, byte[] b, int off, int len) {
        UNSAFE.copyMemory(null, mAddr + start, b, ARRAY + off, len);
    }

    @Override
    void read(int start, ByteBuffer dst) {
        dst.put(mDirectAccess.prepare(mAddr + start, dst.remaining()));
    }

    @Override
    void write(int start, byte[] b, int off, int len) {
        UNSAFE.copyMemory(b, ARRAY + off, null, mAddr + start, len);
    }

    @Override
    void write(int start, ByteBuffer src) {
        mDirectAccess.prepare(mAddr + start, src.remaining()).put(src);
    }

    @Override
    void sync(boolean metadata) throws IOException {
        PosixFileIO.msyncAddr(mAddr, mSize);
    }

    @Override
    public void close() throws IOException {
        PosixFileIO.munmapAddr(mAddr, mSize);
    }

    private static final sun.misc.Unsafe UNSAFE = UnsafeAccess.obtain();
    private static final long ARRAY = (long) UNSAFE.arrayBaseOffset(byte[].class);
}
