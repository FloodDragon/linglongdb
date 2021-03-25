package com.linglong.server.database.exception;

/**
 * Created by liuj-ai on 2021/3/22.
 */
public enum ErrorCode {
    SERVER_HANDLER_ERROR(90000, "server handler error."),
    PARAMETER_ERROR(90001, "parameter error.");

    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ErrorCode{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
