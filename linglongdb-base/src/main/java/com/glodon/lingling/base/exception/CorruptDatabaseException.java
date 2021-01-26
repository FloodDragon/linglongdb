package com.glodon.lingling.base.exception;

/**
 * @author Stereo
 */
public class CorruptDatabaseException extends DatabaseException {
    private static final long serialVersionUID = 1L;

    public CorruptDatabaseException() {
    }

    public CorruptDatabaseException(String message) {
        super(message);
    }

    public CorruptDatabaseException(Throwable cause) {
        super(cause);
    }

    public CorruptDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
