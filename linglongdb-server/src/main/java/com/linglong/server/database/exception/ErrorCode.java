package com.linglong.server.database.exception;

/**
 * Created by liuj-ai on 2021/3/22.
 */
public enum ErrorCode {
    INDEX_WRITE_PARAMETER_ERROR(90000, "write parameter error.");

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
