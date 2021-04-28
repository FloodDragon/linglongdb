package com.linglong.server.database.controller;

import com.linglong.protocol.message.Request;
import com.linglong.protocol.message.Response;
import com.linglong.base.utils.SystemClock;
import com.linglong.rpc.server.skeleton.service.Service;
import com.linglong.server.database.exception.ProcessException;
import com.linglong.server.database.exception.ErrorCode;
import com.linglong.server.database.process.DatabaseProcessor;
import com.linglong.server.utils.MixAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO 需要增加拦截器功能
 * <p>
 * Created by liuj-ai on 2021/3/22.
 */
public abstract class AbsController<E> extends Service {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbsController.class);

    protected DatabaseProcessor databaseProcessor;

    public AbsController(Class<?> cls, DatabaseProcessor databaseProcessor) {
        super(cls);
        this.databaseProcessor = databaseProcessor;
    }

    public abstract E getInstance();

    protected <R extends Response> R response(Request request, Class<R> rc, ErrorCode errorCode) {
        return response(request, rc, errorCode, null);
    }

    protected <R extends Response> R response(Request request, Class<R> rc, Exception ex) {
        return response(request, rc, null, ex);
    }

    protected <R extends Response> R response(Request request, Class<R> rc) {
        return response(request, rc, null, null);
    }

    protected <R extends Response> R response(Request request, Class<R> rc, ErrorCode errorCode, Exception ex) {
        try {
            R r = rc.newInstance();
            if (request != null) {
                r.setId(request.getId());
                r.setXid(request.getXid());
            } else {
                r.setId(MixAll.getUUID());
            }
            if (errorCode != null) {
                r.setErrorCode(errorCode.getCode());
                r.setErrorMessage(errorCode.getMessage());
            } else if (ex != null) {
                r.setErrorCode(ErrorCode.SERVER_HANDLER_ERROR.getCode());
                r.setErrorMessage(MixAll.parseThrowable(ex));
            } else {
                r.setErrorCode(0);
            }
            r.setTimestamp(SystemClock.now());
            return r;
        } catch (Exception e) {
            LOGGER.error("", e);
            throw new ProcessException();
        }
    }
}
