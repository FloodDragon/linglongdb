package com.linglong.engine.core.tx;

import com.linglong.base.exception.DatabaseException;
import com.linglong.base.exception.LockFailureException;
import com.linglong.engine.config.DurabilityMode;
import com.linglong.engine.core.lock.DeadlockException;
import com.linglong.engine.core.lock.LockMode;
import com.linglong.engine.core.lock.LockResult;

import java.io.Flushable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Stereo
 */
public interface Transaction extends Flushable {
    Transaction BOGUS = LocalTransaction.BOGUS;

    void lockMode(LockMode mode);

    LockMode lockMode();

    void lockTimeout(long timeout, TimeUnit unit);

    long lockTimeout(TimeUnit unit);

    void durabilityMode(DurabilityMode mode);

    DurabilityMode durabilityMode();

    void check() throws DatabaseException;

    void commit() throws IOException;

    void commitAll() throws IOException;

    void enter() throws IOException;

    void exit() throws IOException;

    void reset() throws IOException;

    default void reset(Throwable cause) {
        try {
            reset();
        } catch (Throwable e) {
            // Ignore.
        }
    }

    LockResult lockShared(long indexId, byte[] key) throws LockFailureException;

    LockResult lockUpgradable(long indexId, byte[] key) throws LockFailureException;

    LockResult lockExclusive(long indexId, byte[] key) throws LockFailureException;

    void customRedo(byte[] message, long indexId, byte[] key) throws IOException;

    void customUndo(byte[] message) throws IOException;

    boolean isNested();

    int nestingLevel();

    LockResult tryLockShared(long indexId, byte[] key, long nanosTimeout)
            throws DeadlockException;

    LockResult lockShared(long indexId, byte[] key, long nanosTimeout)
            throws LockFailureException;

    LockResult tryLockUpgradable(long indexId, byte[] key, long nanosTimeout)
            throws DeadlockException;

    LockResult lockUpgradable(long indexId, byte[] key, long nanosTimeout)
            throws LockFailureException;

    LockResult tryLockExclusive(long indexId, byte[] key, long nanosTimeout)
            throws DeadlockException;

    LockResult lockExclusive(long indexId, byte[] key, long nanosTimeout)
            throws LockFailureException;

    LockResult lockCheck(long indexId, byte[] key);

    long lastLockedIndex();

    byte[] lastLockedKey();

    void unlock();

    void unlockToShared();

    void unlockToUpgradable();

    void unlockCombine();

    default long getId() {
        return 0;
    }

    default void prepare() throws IOException {
        throw new UnsupportedOperationException();
    }

    default void attach(Object obj) {
        throw new UnsupportedOperationException();
    }

    default Object attachment() {
        return null;
    }
}
