package com.linglong.server.database.controller;

import com.linglong.rpc.server.skeleton.service.Service;
import com.linglong.server.database.process.DatabaseProcessor;

/**
 * Created by liuj-ai on 2021/3/22.
 */
public abstract class AbsController extends Service {

    protected DatabaseProcessor databaseProcessor;

    public AbsController(Class<?> cls, DatabaseProcessor databaseProcessor) {
        super(cls);
        this.databaseProcessor = databaseProcessor;
    }

//    public WriteResponse response(Message message) {
//        return message == null ? new WriteResponse() : (WriteResponse) new WriteResponse()
//                .setId(message.getId())
//                .setTimestamp(SystemClock.now())
//                .setProcessType(message.getProcessType())
//                .setXid(message.getXid());
//    }
//
//    public WriteResponse error(Message message, ErrorCode errorCode) {
//        return (WriteResponse) response(message).setErrorCode(errorCode.getCode()).setErrorMessage(errorCode.getMessage());
//    }
}
