package com.linglong.io;

import com.linglong.base.common.CauseCloseable;

import java.io.IOException;

/**
 * 固定大小的页面的持久数组。
 * 每个页面都是唯一的由从零开始的64位索引标识。
 *
 * @author Stereo
 */
public abstract class PageArray implements CauseCloseable {
    final int mPageSize;

    protected PageArray(int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be at least 1: " + pageSize);
        }
        mPageSize = pageSize;
    }

    public boolean isDirectIO() {
        return false;
    }

    public final int pageSize() {
        return mPageSize;
    }

    public abstract boolean isReadOnly();

    public abstract boolean isEmpty() throws IOException;

    public abstract long getPageCount() throws IOException;

    public abstract void setPageCount(long count) throws IOException;

    public long getPageCountLimit() throws IOException {
        return -1;
    }

    public void readPage(long index, byte[] dst) throws IOException {
        readPage(index, dst, 0, mPageSize);
    }

    public abstract void readPage(long index, byte[] dst, int offset, int length)
            throws IOException;

    public void readPage(long index, long dstPtr) throws IOException {
        readPage(index, dstPtr, 0, mPageSize);
    }

    public void readPage(long index, long dstPtr, int offset, int length)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    public void writePage(long index, byte[] src) throws IOException {
        writePage(index, src, 0);
    }

    public abstract void writePage(long index, byte[] src, int offset) throws IOException;

    public void writePage(long index, long srcPtr) throws IOException {
        writePage(index, srcPtr, 0);
    }

    public void writePage(long index, long srcPtr, int offset) throws IOException {
        throw new UnsupportedOperationException();
    }

    public byte[] evictPage(long index, byte[] buf) throws IOException {
        writePage(index, buf);
        return buf;
    }

    public long evictPage(long index, long bufPtr) throws IOException {
        writePage(index, bufPtr);
        return bufPtr;
    }

    public void cachePage(long index, byte[] src) throws IOException {
        cachePage(index, src, 0);
    }

    public void cachePage(long index, byte[] src, int offset) throws IOException {
    }

    public void cachePage(long index, long srcPtr) throws IOException {
        cachePage(index, srcPtr, 0);
    }

    public void cachePage(long index, long srcPtr, int offset) throws IOException {
    }

    public void uncachePage(long index) throws IOException {
    }

    public long directPagePointer(long index) throws IOException {
        throw new UnsupportedOperationException();
    }

    public long dirtyPage(long index) throws IOException {
        throw new UnsupportedOperationException();
    }

    public long copyPage(long srcIndex, long dstIndex) throws IOException {
        throw new UnsupportedOperationException();
    }

    public long copyPageFromPointer(long srcPointer, long dstIndex) throws IOException {
        throw new UnsupportedOperationException();
    }

    public abstract void sync(boolean metadata) throws IOException;

    public void syncPage(long index) throws IOException {
        sync(false);
    }

    @Override
    public void close() throws IOException {
        close(null);
    }

    @Override
    public abstract void close(Throwable cause) throws IOException;

    public PageArray open() throws IOException {
        return this;
    }
}
