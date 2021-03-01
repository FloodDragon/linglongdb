package com.linglong.base.exception;

/**
 * @author Stereo
 */
public class IncompleteRestoreException extends CorruptDatabaseException {
    private static final long serialVersionUID = 1L;

    public IncompleteRestoreException() {
    }

    public IncompleteRestoreException(String message) {
        super(message);
    }

    public IncompleteRestoreException(Throwable cause) {
        super(cause);
    }

    public IncompleteRestoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
