package com.linglong.server.database.controller;

import com.linglong.base.bytecode.Proxy;
import com.linglong.base.utils.ReflectUtils;
import com.linglong.protocol.message.Request;
import com.linglong.protocol.message.Response;
import com.linglong.base.utils.SystemClock;
import com.linglong.rpc.common.service.IService;
import com.linglong.rpc.server.skeleton.service.Service;
import com.linglong.server.database.controller.annotation.Leader;
import com.linglong.server.database.exception.ProcessException;
import com.linglong.server.database.exception.ErrorCode;
import com.linglong.server.database.process.DatabaseProcessor;
import com.linglong.server.database.process.LeaderCoordinator;
import com.linglong.server.utils.MixAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * <p>
 * Created by liuj-ai on 2021/3/22.
 */
public abstract class AbsController<E extends IService> extends Service implements InvocationHandler {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbsController.class);

    private final E proxy;
    private Class<E> serviceClazz;
    protected DatabaseProcessor databaseProcessor;
    protected LeaderCoordinator leaderCoordinator;

    public AbsController(Class<E> cls, DatabaseProcessor databaseProcessor) {
        super(cls);
        this.serviceClazz = cls;
        this.databaseProcessor = databaseProcessor;
        this.leaderCoordinator = databaseProcessor.getLeaderCoordinator();
        this.proxy = createProxy();
    }

    private Leader findLeaderAnnotation(Method method, Object[] args) {
        try {
            String[] parameterTypes;
            if (args != null) {
                parameterTypes = new String[args.length];
                for (int i = 0; i < args.length; i++) {
                    parameterTypes[i] = args[i].getClass().getName();
                }
            } else {
                parameterTypes = null;
            }
            method = ReflectUtils.findMethodByMethodSignature(this.getClass(), method.getName(), parameterTypes);
        } catch (Exception e) {
            //ignore
        }
        Leader leader = method.getAnnotation(Leader.class);
        if (leader == null) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            for (Annotation ann : annotations) {
                if (ann.annotationType() == Leader.class) {
                    leader = (Leader) ann;
                }
            }
        }
        return leader;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        IService service;
        Leader leader;
        if (databaseProcessor.isReplicaEnabled() && null != (leader = findLeaderAnnotation(method, args)) && leaderCoordinator.isNeedTransferToLeader()) {
            service = leaderCoordinator.getLeaderService(serviceClazz);
        } else {
            service = this;
        }
        return method.invoke(service, args);
    }

    protected final E createProxy() {
        return (E) Proxy.getProxy(serviceClazz).newInstance(this);
    }

    public final E getInstance() {
        return this.proxy;
    }

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
