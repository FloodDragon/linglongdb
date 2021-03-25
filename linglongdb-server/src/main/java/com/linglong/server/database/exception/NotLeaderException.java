package com.linglong.server.database.exception;

/**
 * Created by liuj-ai on 2021/3/22.
 */
public class NotLeaderException extends ProcessException {

    public NotLeaderException() {
    }

    public NotLeaderException(String message) {
        super(message);
    }

    public NotLeaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotLeaderException(Throwable cause) {
        super(cause);
    }

    public NotLeaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
