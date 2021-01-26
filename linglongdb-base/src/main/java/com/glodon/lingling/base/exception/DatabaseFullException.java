package com.glodon.lingling.base.exception;

/**
 * @author Stereo
 */
public class DatabaseFullException extends DatabaseException {
    private static final long serialVersionUID = 1L;

    public DatabaseFullException() {
    }

    public DatabaseFullException(Throwable cause) {
        super(cause);
    }

    public DatabaseFullException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseFullException(String message) {
        super(message);
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }
}
