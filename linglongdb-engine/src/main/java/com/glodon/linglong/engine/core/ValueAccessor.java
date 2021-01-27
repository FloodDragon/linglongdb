package com.glodon.linglong.engine.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Stereo
 */
public interface ValueAccessor extends Closeable {
    long valueLength() throws IOException;

    void valueLength(long length) throws IOException;

    int valueRead(long pos, byte[] buf, int off, int len) throws IOException;

    void valueWrite(long pos, byte[] buf, int off, int len) throws IOException;

    void valueClear(long pos, long length) throws IOException;

    InputStream newValueInputStream(long pos) throws IOException;

    InputStream newValueInputStream(long pos, int bufferSize) throws IOException;

    OutputStream newValueOutputStream(long pos) throws IOException;

    OutputStream newValueOutputStream(long pos, int bufferSize) throws IOException;

    @Override
    void close();
}
