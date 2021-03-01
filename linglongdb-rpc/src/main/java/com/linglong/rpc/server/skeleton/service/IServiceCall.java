package com.linglong.rpc.server.skeleton.service;

import java.util.Set;

/**
 * @author Stereo
 * @version 2013.12.19
 */
public interface IServiceCall {

    String getId();

    boolean isSuccess();

    Class<?> getReturnType();

    Object getResult();

    Object getResultPacket();

    void setResult(Object result);

    String getMethodName();

    String getInterfaceName();

    Object[] getArguments();

    byte getStatus();

    Exception getException();

    void setStatus(byte status);

    Set<ICallback> getCallbacks();

    void registerCallback(ICallback callback);

    void setException(Exception exception);

    void cleanArguments();
}