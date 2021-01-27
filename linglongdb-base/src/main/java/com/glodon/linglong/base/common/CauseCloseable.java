package com.glodon.linglong.base.common;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Stereo
 */
public interface CauseCloseable extends Closeable {
    void close(Throwable cause) throws IOException;
}
