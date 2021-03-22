package com.linglong.server.database.controller;

import com.linglong.protocol.Message;
import com.linglong.protocol.message.WriteResponse;
import com.linglong.server.database.exception.ErrorCode;
import com.linglong.server.utils.SystemClock;

/**
 * Created by liuj-ai on 2021/3/22.
 */
public final class ResponseBuilder {

    public final static WriteResponse response(Message message) {
        return message == null ? new WriteResponse() : (WriteResponse) new WriteResponse()
                .setId(message.getId())
                .setTimestamp(SystemClock.now())
                .setProcessType(message.getProcessType())
                .setXid(message.getXid());
    }

    public final static WriteResponse error(Message message, ErrorCode errorCode) {
        return (WriteResponse) response(message).setErrorCode(errorCode.getCode()).setErrorMessage(errorCode.getMessage());
    }
}
