package com.linglong.base.exception;

/**
 * @author Stereo
 */
public class LockFailureException extends DatabaseException {
    private static final long serialVersionUID = 1L;

    public LockFailureException() {
    }

    public LockFailureException(String message) {
        super(message);
    }

    public LockFailureException(Throwable cause) {
        super(cause);
    }

    public LockFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }
}
