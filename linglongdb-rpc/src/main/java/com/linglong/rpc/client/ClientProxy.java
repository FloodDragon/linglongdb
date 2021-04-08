package com.linglong.rpc.client;


import com.linglong.rpc.client.ds.DataStream;
import com.linglong.rpc.client.ds.DataStreamRemoteProxy;
import com.linglong.rpc.common.bytecode.Proxy;
import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.common.service.IService;

import java.lang.reflect.InvocationHandler;

/**
 * RPC客户端代理
 * <p>
 *
 * @author Stereo on 2019/12/10.
 */
public final class ClientProxy extends AbstractClient {

    private final ClassLoader loader;

    public ClientProxy() {
        this(new Config());
    }

    public ClientProxy(Config config) {
        this(config, Thread.currentThread().getContextClassLoader());
    }

    public ClientProxy(Config config, ClassLoader loader) {
        super("ClientProxy" + ":" + config.getRemoteAddress().toString(), config);
        this.loader = loader;
    }

    public <S extends IService> S create(final Class<S> api) {
        return create(api, loader);
    }

    public <S extends IService> S create(Class<S> api, ClassLoader classLoader) {
        RemoteProxy proxy = new RemoteProxy(this, api);
        return (S) Proxy.getProxy(classLoader, new Class[]{api}).newInstance(proxy);
    }

    public <S extends IService> DataStream<S> createDataStream(final Class<S> api) {
        return createDataStream(api, loader);
    }

    public <S extends IService> DataStream<S> createDataStream(final Class<S> api, ClassLoader classLoader) {
        DataStreamRemoteProxy proxy = new DataStreamRemoteProxy(this, api);
        proxy.setService((S) Proxy.getProxy(classLoader, new Class[]{api}).newInstance(proxy));
        return proxy.getDataStream();
    }
}