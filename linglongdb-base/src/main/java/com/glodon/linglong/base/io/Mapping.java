package com.glodon.linglong.base.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Stereo
 */
abstract class Mapping implements Closeable {
    static Mapping open(File file, boolean readOnly, long position, int size) throws IOException {
        return new NioMapping(file, readOnly, position, size);
    }

    abstract void read(int start, byte[] b, int off, int len);

    abstract void read(int start, ByteBuffer b);

    abstract void write(int start, byte[] b, int off, int len);

    abstract void write(int start, ByteBuffer b);

    abstract void sync(boolean metadata) throws IOException;
}
