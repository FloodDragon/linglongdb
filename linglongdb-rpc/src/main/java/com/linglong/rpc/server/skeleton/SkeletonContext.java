package com.linglong.rpc.server.skeleton;

import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.common.event.AsyncDispatcher;
import com.linglong.rpc.common.event.Dispatcher;
import com.linglong.rpc.common.life.LifeService;
import com.linglong.rpc.common.remoting.Channel;
import com.linglong.rpc.common.service.IService;
import com.linglong.rpc.server.event.enums.HeartbeatEnum;
import com.linglong.rpc.server.event.enums.ServiceEnum;
import com.linglong.rpc.server.skeleton.liveliness.ILiveliness;
import com.linglong.rpc.server.skeleton.liveliness.Liveliness;
import com.linglong.rpc.server.skeleton.service.ServiceEventHandler;
import com.linglong.rpc.server.skeleton.service.ServiceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 控制层上下文
 *
 * @author Stereo
 */
public class SkeletonContext extends AbstractSkeletonContext {
    public static Logger logger = LoggerFactory.getLogger(SkeletonContext.class);

    private Dispatcher dispatcher;
    private Liveliness liveliness;
    private ServiceEventHandler serviceHandler;
    protected Map<String, IService> serviceMap;
    private static ThreadLocal<WeakReference<Object>> threadLocal = new ThreadLocal<WeakReference<Object>>();

    public SkeletonContext(Config config) {
        super("SkeletonContext");
        this.dispatcher = new AsyncDispatcher();
        this.serviceMap = new ConcurrentHashMap<>();
        this.liveliness = new Liveliness(config, this.dispatcher);
        this.serviceHandler = new ServiceHandler(this, config);
    }

    @Override
    protected void serviceInit() throws Exception {
        //事件处理器初始化
        ((LifeService) dispatcher).init();
        //业务处理器初始化
        ((LifeService) serviceHandler).init();
        //心跳检测初始化
        ((LifeService) liveliness).init();
        //注册业务处理事件
        dispatcher.register(ServiceEnum.class, serviceHandler);
        //注册心跳检测
        dispatcher.register(HeartbeatEnum.class, liveliness);
    }

    @Override
    protected void serviceStart() throws Exception {
        ((LifeService) dispatcher).start();
        ((LifeService) serviceHandler).start();
        ((LifeService) liveliness).start();
    }

    @Override
    protected void serviceStop() throws Exception {
        ((LifeService) dispatcher).stop();
        ((LifeService) serviceHandler).stop();
        ((LifeService) liveliness).stop();
    }

    public static Object getObjectLocal() {
        WeakReference<Object> ref = threadLocal.get();
        if (ref != null) {
            return ref.get();
        } else {
            return null;
        }
    }

    public static void setObjectLocal(Object object) {
        if (object != null) {
            threadLocal.set(new WeakReference<Object>(object));
        } else {
            threadLocal.remove();
        }
    }

    @Override
    public void registerService(final IService service) {
        if (this.serviceMap.containsKey(service.getServiceName()))
            return;
        this.serviceMap.put(service.getServiceName(), service);
        service.setServiceContext(this);
        service.onRegister();
    }

    @Override
    public IService retrieveService(String serviceName) {
        if (null != serviceMap.get(serviceName)) {
            return this.serviceMap.get(serviceName);
        }
        for (IService action : this)
            return action.resolveService(serviceName);
        return null;
    }

    @Override
    public IService removeService(String serviceName) {
        if (hasService(serviceName)) {
            IService action = serviceMap.get(serviceName);
            serviceMap.remove(serviceName);
            action.onRemove();
            return action;
        }
        return null;
    }

    @Override
    public boolean hasService(String serviceName) {
        return serviceMap.containsKey(serviceName);
    }

    @Override
    public Iterator<IService> iterator() {
        return serviceMap.values().iterator();
    }

    @Override
    public ServiceEventHandler getServiceHandler() {
        return serviceHandler;
    }

    @Override
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    public ILiveliness<Channel> getLiveliness() {
        return liveliness;
    }
}