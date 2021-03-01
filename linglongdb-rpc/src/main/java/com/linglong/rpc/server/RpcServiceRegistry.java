package com.linglong.rpc.server;

import com.linglong.rpc.common.service.IService;
import com.linglong.rpc.common.service.IServiceContext;

/**
 * @author Stereo
 * @version 2013.12.19
 */
public class RpcServiceRegistry {

    private IServiceContext skeletonContext;

    public RpcServiceRegistry(IServiceContext skeletonContext) {
        this.skeletonContext = skeletonContext;
    }

    public void registerService(IService service) {
        skeletonContext.registerService(service);
    }

    public IService retrieveService(String serviceName) {
        return skeletonContext.retrieveService(serviceName);
    }

    public IService removeService(String serviceName) {
        return skeletonContext.removeService(serviceName);
    }

    public boolean hasService(String serviceName) {
        return skeletonContext.hasService(serviceName);
    }

    public IServiceContext getSkeletonContext() {
        return skeletonContext;
    }
}
