package com.linglong.rpc.client;



import com.linglong.rpc.common.bytecode.Proxy;
import com.linglong.rpc.common.config.Config;

import java.lang.reflect.InvocationHandler;

/**
 * RPC客户端代理
 * <p>
 * @author Stereo on 2019/12/10.
 */
public class ClientProxy extends AbstractClient {

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

    public <T> T create(final Class<T> api) {
        return create(api, loader);
    }

    public <T> T create(Class<T> api, ClassLoader classLoader) {
        InvocationHandler invocationHandler = new RemoteProxy(this, api);
        return (T) Proxy.getProxy(classLoader, new Class[]{api}).newInstance(invocationHandler);
    }
}