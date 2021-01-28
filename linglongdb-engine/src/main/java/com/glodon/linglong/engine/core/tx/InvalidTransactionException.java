package com.glodon.linglong.engine.core.tx;

import com.glodon.linglong.base.exception.DatabaseException;

/**
 * @author Stereo
 */
public class InvalidTransactionException extends DatabaseException {
    private static final long serialVersionUID = 1L;

    public InvalidTransactionException() {
    }

    public InvalidTransactionException(String message) {
        super(message);
    }

    public InvalidTransactionException(Throwable cause) {
        super(cause);
    }

    public InvalidTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }
}
