package com.glodon.linglong.engine.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Stereo
 */
public interface Snapshot extends Closeable {

    long length();

    long position();

    void writeTo(OutputStream out) throws IOException;

    void close() throws IOException;
}
