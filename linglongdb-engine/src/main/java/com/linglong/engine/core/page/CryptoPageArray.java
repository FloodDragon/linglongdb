package com.linglong.engine.core.page;

import com.linglong.base.common.Crypto;
import com.linglong.base.exception.DatabaseException;
import com.linglong.base.io.PageArray;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 *
 * @author Stereo
 */
final class CryptoPageArray extends PageArray {
    private final PageArray mSource;
    private final Crypto mCrypto;

    CryptoPageArray(PageArray source, Crypto crypto) {
        super(source.pageSize());
        mSource = source;
        mCrypto = crypto;
    }

    @Override
    public boolean isDirectIO() {
        return mSource.isDirectIO();
    }

    @Override
    public boolean isReadOnly() {
        return mSource.isReadOnly();
    }

    @Override
    public boolean isEmpty() throws IOException {
        return mSource.isEmpty();
    }

    @Override
    public long getPageCount() throws IOException {
        return mSource.getPageCount();
    }

    @Override
    public void setPageCount(long count) throws IOException {
        mSource.setPageCount(count);
    }

    @Override
    public long getPageCountLimit() throws IOException {
        return mSource.getPageCountLimit();
    }

    @Override
    public void readPage(long index, byte[] dst) throws IOException {
        try {
            mSource.readPage(index, dst);
            mCrypto.decryptPage(index, pageSize(), dst, 0);
        } catch (GeneralSecurityException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void readPage(long index, byte[] dst, int offset, int length) throws IOException {
        int pageSize = pageSize();
        if (offset == 0 && length == pageSize) {
            readPage(index, dst);
            return;
        }

        byte[] page = new byte[pageSize];

        readPage(index, page);
        System.arraycopy(page, 0, dst, offset, length);
    }

    @Override
    public void readPage(long index, long dstPtr) throws IOException {
        try {
            mSource.readPage(index, dstPtr);
            mCrypto.decryptPage(index, pageSize(), dstPtr, 0);
        } catch (GeneralSecurityException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void readPage(long index, long dstPtr, int offset, int length) throws IOException {
        int pageSize = pageSize();
        if (offset == 0 && length == pageSize) {
            readPage(index, dstPtr);
            return;
        }

        long page = DirectPageOps.p_alloc(pageSize, mSource.isDirectIO());
        try {
            readPage(index, page);
            DirectPageOps.p_copy(page, 0, dstPtr, offset, length);
        } finally {
            DirectPageOps.p_delete(page);
        }
    }

    @Override
    public void writePage(long index, byte[] src, int offset) throws IOException {
        try {
            int pageSize = pageSize();
            // Unknown if source contents can be destroyed, so create a new one.
            byte[] encrypted = new byte[pageSize];

            mCrypto.encryptPage(index, pageSize, src, offset, encrypted, 0);
            mSource.writePage(index, encrypted, 0);
        } catch (GeneralSecurityException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void writePage(long index, long srcPtr, int offset) throws IOException {
        try {
            int pageSize = pageSize();
            // Unknown if source contents can be destroyed, so create a new one.
            long encrypted = DirectPageOps.p_alloc(pageSize, mSource.isDirectIO());
            try {
                mCrypto.encryptPage(index, pageSize, srcPtr, offset, encrypted, 0);
                mSource.writePage(index, encrypted, 0);
            } finally {
                DirectPageOps.p_delete(encrypted);
            }
        } catch (GeneralSecurityException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public byte[] evictPage(long index, byte[] buf) throws IOException {
        try {
            // Page is being evicted, and so buf contents can be destroyed.
            mCrypto.encryptPage(index, pageSize(), buf, 0);
            return mSource.evictPage(index, buf);
        } catch (GeneralSecurityException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public long evictPage(long index, long bufPtr) throws IOException {
        try {
            // Page is being evicted, and so buf contents can be destroyed.
            mCrypto.encryptPage(index, pageSize(), bufPtr, 0);
            return mSource.evictPage(index, bufPtr);
        } catch (GeneralSecurityException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void sync(boolean metadata) throws IOException {
        mSource.sync(metadata);
    }

    @Override
    public void close(Throwable cause) throws IOException {
        mSource.close(cause);
    }
}
