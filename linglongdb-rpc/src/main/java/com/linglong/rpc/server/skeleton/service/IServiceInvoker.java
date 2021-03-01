package com.linglong.rpc.server.skeleton.service;

/**
 * IServiceInvoker
 *
 * @author Stereo
 * @version 2013.12.19
 */
public interface IServiceInvoker {

    String BEAN_NAME = "serviceInvoker";

    Object getService(String serviceName);

    boolean invoke(IServiceCall call);

    boolean invoke(IServiceCall call, Object service);
}
