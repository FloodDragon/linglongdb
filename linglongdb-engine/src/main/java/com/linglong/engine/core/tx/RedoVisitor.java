package com.linglong.engine.core.tx;

import java.io.IOException;

/**
 * @author Stereo
 */
public interface RedoVisitor {

    boolean reset() throws IOException;

    boolean timestamp(long timestamp) throws IOException;

    boolean shutdown(long timestamp) throws IOException;

    boolean close(long timestamp) throws IOException;

    boolean endFile(long timestamp) throws IOException;

    boolean control(byte[] message) throws IOException;

    boolean store(long indexId, byte[] key, byte[] value) throws IOException;

    boolean storeNoLock(long indexId, byte[] key, byte[] value) throws IOException;

    boolean renameIndex(long txnId, long indexId, byte[] newName) throws IOException;

    boolean deleteIndex(long txnId, long indexId) throws IOException;

    boolean txnPrepare(long txnId) throws IOException;

    boolean txnEnter(long txnId) throws IOException;

    boolean txnRollback(long txnId) throws IOException;

    boolean txnRollbackFinal(long txnId) throws IOException;

    boolean txnCommit(long txnId) throws IOException;

    boolean txnCommitFinal(long txnId) throws IOException;

    boolean txnEnterStore(long txnId, long indexId, byte[] key, byte[] value)
            throws IOException;

    boolean txnStore(long txnId, long indexId, byte[] key, byte[] value) throws IOException;

    boolean txnStoreCommit(long txnId, long indexId, byte[] key, byte[] value)
            throws IOException;

    boolean txnStoreCommitFinal(long txnId, long indexId, byte[] key, byte[] value)
            throws IOException;

    boolean cursorRegister(long cursorId, long indexId) throws IOException;

    boolean cursorUnregister(long cursorId) throws IOException;

    boolean cursorStore(long cursorId, long txnId, byte[] key, byte[] value)
            throws IOException;

    boolean cursorFind(long cursorId, long txnId, byte[] key) throws IOException;

    boolean cursorValueSetLength(long cursorId, long txnId, long length) throws IOException;

    boolean cursorValueWrite(long cursorId, long txnId,
                             long pos, byte[] buf, int off, int len)
            throws IOException;

    boolean cursorValueClear(long cursorId, long txnId, long pos, long length)
            throws IOException;

    boolean txnLockShared(long txnId, long indexId, byte[] key) throws IOException;

    boolean txnLockUpgradable(long txnId, long indexId, byte[] key) throws IOException;

    boolean txnLockExclusive(long txnId, long indexId, byte[] key) throws IOException;

    boolean txnCustom(long txnId, byte[] message) throws IOException;

    boolean txnCustomLock(long txnId, byte[] message, long indexId, byte[] key)
            throws IOException;
}
