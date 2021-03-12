package com.linglong.rpc.server.skeleton;

import com.linglong.rpc.common.event.Dispatcher;
import com.linglong.rpc.common.life.AbstractService;
import com.linglong.rpc.common.remoting.Channel;
import com.linglong.rpc.common.service.IService;
import com.linglong.rpc.common.service.IServiceContext;
import com.linglong.rpc.server.skeleton.liveliness.ILiveliness;
import com.linglong.rpc.server.skeleton.service.ServiceEventHandler;

/**
 * @author Stereo on 2020/4/27.
 */
public abstract class AbstractSkeletonContext extends AbstractService implements Iterable<IService>, IServiceContext {

    public AbstractSkeletonContext(String name) {
        super(name);
    }

    /**
     * 获得在线状态
     *
     * @return
     */
    public abstract ILiveliness<Channel> getLiveliness();

    /**
     * 获取处理器
     *
     * @return
     */
    public abstract ServiceEventHandler getServiceHandler();

    /**
     * 获取中央处理器
     *
     * @return
     */
    public abstract Dispatcher getDispatcher();
}
