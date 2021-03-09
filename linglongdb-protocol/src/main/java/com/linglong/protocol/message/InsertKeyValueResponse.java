package com.linglong.protocol.message;

import com.linglong.protocol.Message;

/**
 * Created by liuj-ai on 2021/3/9.
 */
public class InsertKeyValueResponse extends Message {
    private int errorCode;
    private String errorMessage;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
