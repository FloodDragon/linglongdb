package com.linglong.rpc.common.service;


/**
 * Skeleton控制器上下文
 *
 * @author Stereo
 */
public interface IServiceContext {
    /**
     * 注册service
     *
     * @param service
     */
    void registerService(IService service);

    /**
     * 检索service
     *
     * @param serviceName
     * @return
     */
    IService retrieveService(String serviceName);

    /**
     * 注销service
     *
     * @param serviceName
     * @return
     */
    IService removeService(String serviceName);

    /**
     * 是否有serviceName的service
     *
     * @param serviceName
     * @return
     */
    boolean hasService(String serviceName);

}