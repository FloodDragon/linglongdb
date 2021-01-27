package com.glodon.linglong.base.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

/**
 * @author Stereo
 */
public interface Crypto {
    default void encryptPage(long pageIndex, int pageSize, byte[] page, int pageOffset)
            throws GeneralSecurityException {
        encryptPage(pageIndex, pageSize, page, pageOffset, page, pageOffset);
    }

    void encryptPage(long pageIndex, int pageSize,
                     byte[] src, int srcOffset, byte[] dst, int dstOffset)
            throws GeneralSecurityException;

    default void encryptPage(long pageIndex, int pageSize, long pagePtr, int pageOffset)
            throws GeneralSecurityException {
        encryptPage(pageIndex, pageSize, pagePtr, pageOffset, pagePtr, pageOffset);
    }

    default void encryptPage(long pageIndex, int pageSize,
                             long srcPtr, int srcOffset, long dstPtr, int dstOffset)
            throws GeneralSecurityException {
        throw new UnsupportedOperationException();
    }

    default void decryptPage(long pageIndex, int pageSize, byte[] page, int pageOffset)
            throws GeneralSecurityException {
        decryptPage(pageIndex, pageSize, page, pageOffset, page, pageOffset);
    }

    void decryptPage(long pageIndex, int pageSize,
                     byte[] src, int srcOffset, byte[] dst, int dstOffset)
            throws GeneralSecurityException;

    default void decryptPage(long pageIndex, int pageSize, long pagePtr, int pageOffset)
            throws GeneralSecurityException {
        decryptPage(pageIndex, pageSize, pagePtr, pageOffset, pagePtr, pageOffset);
    }

    default void decryptPage(long pageIndex, int pageSize,
                             long srcPtr, int srcOffset, long dstPtr, int dstOffset)
            throws GeneralSecurityException {
        throw new UnsupportedOperationException();
    }

    OutputStream newEncryptingStream(long id, OutputStream out)
            throws GeneralSecurityException, IOException;

    InputStream newDecryptingStream(long id, InputStream in)
            throws GeneralSecurityException, IOException;
}
