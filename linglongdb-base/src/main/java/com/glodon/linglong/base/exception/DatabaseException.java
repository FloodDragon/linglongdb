package com.glodon.linglong.base.exception;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Stereo
 */
public class DatabaseException extends IOException {
    private static final long serialVersionUID = 1L;

    public DatabaseException() {
    }

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns false if database should be closed as a result of this exception.
     */
    public boolean isRecoverable() {
        return false;
    }

    /**
     * Applicable to timeout exceptions.
     */
    public long getTimeout() {
        return 0;
    }

    /**
     * Applicable to timeout exceptions.
     */
    public TimeUnit getUnit() {
        return null;
    }

    /**
     * Applicable to timeout exceptions.
     */
    public Object getOwnerAttachment() {
        return null;
    }

    /**
     * Rethrows if given a recoverable exception.
     */
    public static void rethrowIfRecoverable(Throwable e) throws DatabaseException {
        if (e instanceof DatabaseException) {
            DatabaseException de = (DatabaseException) e;
            if (de.isRecoverable()) {
                throw de;
            }
        }
    }
}
