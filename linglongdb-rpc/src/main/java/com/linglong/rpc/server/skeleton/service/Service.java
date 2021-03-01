package com.linglong.rpc.server.skeleton.service;


import com.linglong.rpc.common.service.IService;
import com.linglong.rpc.common.service.IServiceContext;

/**
 * @author Stereo
 */
public abstract class Service implements IService {

    protected IServiceContext actionContext;
    protected String serviceName = "serviceName";

    @Override
    public void onRegister() {
    }

    @Override
    public void onRemove() {
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    public Service(Class<?> cls) {
        this.serviceName = cls.getName();
    }

    @Override
    public IService resolveService(String actionName) {
        if (this.serviceName.equals(actionName)) {
            return this;
        } else {
            return null;
        }
    }

    @Override
    public void setServiceContext(IServiceContext actionContext) {
        this.actionContext = actionContext;
    }
}