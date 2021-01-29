package com.glodon.linglong.engine.core.frame;

import java.io.IOException;

/**
 * @author Stereo
 */
public interface Sorter {

    void add(byte[] key, byte[] value) throws IOException;

    Index finish() throws IOException;

    Scanner finishScan() throws IOException;

    Scanner finishScanReverse() throws IOException;

    long progress();

    void reset() throws IOException;
}
