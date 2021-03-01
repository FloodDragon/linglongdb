package com.linglong.rpc.common.service;

/**
 * 控制接口
 *
 * @author Stereo
 */
public interface IService {

    void onRemove();

    void onRegister();

    String getServiceName();

    IService resolveService(String actionName);

    void setServiceContext(IServiceContext actionContext);
}
