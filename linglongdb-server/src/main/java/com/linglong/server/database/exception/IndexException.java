package com.linglong.server.database.exception;

/**
 * Created by liuj-ai on 2021/3/22.
 */
public class IndexException extends RuntimeException {

    public IndexException() {
    }

    public IndexException(String message) {
        super(message);
    }

    public IndexException(String message, Throwable cause) {
        super(message, cause);
    }

    public IndexException(Throwable cause) {
        super(cause);
    }

    public IndexException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
