package com.glodon.lingling.base.io;

import com.glodon.lingling.base.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;

/**
 * @author Stereo
 */
final class NioMapping extends Mapping {
    private final RandomAccessFile mRaf;
    private final FileChannel mChannel;
    private final MappedByteBuffer mBuffer;

    NioMapping(File file, boolean readOnly, long position, int size) throws IOException {
        mRaf = new RandomAccessFile(file, readOnly ? "r" : "rw");
        mChannel = mRaf.getChannel();
        mBuffer = mChannel.map(readOnly ? READ_ONLY : READ_WRITE, position, size);
    }

    @Override
    void read(int start, byte[] b, int off, int len) {
        ByteBuffer src = mBuffer.slice();
        src.position(start);
        src.get(b, off, len);
    }

    @Override
    void read(int start, ByteBuffer dst) {
        ByteBuffer src = mBuffer.slice();
        src.limit(start + dst.remaining());
        src.position(start);
        dst.put(src);
    }

    @Override
    void write(int start, byte[] b, int off, int len) {
        ByteBuffer dst = mBuffer.slice();
        dst.position(start);
        dst.put(b, off, len);
    }

    @Override
    void write(int start, ByteBuffer src) {
        ByteBuffer dst = mBuffer.slice();
        dst.position(start);
        dst.put(src);
    }

    @Override
    void sync(boolean metadata) throws IOException {
        mBuffer.force();
        mChannel.force(metadata);
    }

    @Override
    public void close() throws IOException {
        Utils.delete(mBuffer);
        mRaf.close();
    }
}
