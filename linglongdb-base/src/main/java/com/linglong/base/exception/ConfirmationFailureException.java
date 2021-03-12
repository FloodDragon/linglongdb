package com.linglong.base.exception;

/**
 * @author Stereo on 2021/1/26.
 */
public class ConfirmationFailureException extends DatabaseException {
    private static final long serialVersionUID = 1L;

    public ConfirmationFailureException() {
    }

    public ConfirmationFailureException(String message) {
        super(message);
    }

    public ConfirmationFailureException(Throwable cause) {
        super(cause);
    }

    public ConfirmationFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }
}
