
package com.linglong.sql.exception;

public final class LinglongSQLParsingException extends RuntimeException {
    public LinglongSQLParsingException() {
    }

    public LinglongSQLParsingException(String message) {
        super(message);
    }

    public LinglongSQLParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public LinglongSQLParsingException(Throwable cause) {
        super(cause);
    }

    public LinglongSQLParsingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
