package com.linglong.engine.core.page;

import java.io.Closeable;

/**
 * @author Stereo
 */
public interface PageCache extends Closeable {

    boolean add(long pageId, byte[] page, int offset, boolean canEvict);

    boolean add(long pageId, long pagePtr, int offset, boolean canEvict);

    boolean copy(long pageId, int start, byte[] page, int offset);

    boolean copy(long pageId, int start, long pagePtr, int offset);

    boolean remove(long pageId, byte[] page, int offset, int length);

    boolean remove(long pageId, long pagePtr, int offset, int length);

    long capacity();

    long maxEntryCount();

    @Override
    void close();
}
