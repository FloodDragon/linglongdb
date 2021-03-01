package com.linglong.rpc.server.skeleton.service;

import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.common.config.Constants;
import com.linglong.rpc.common.event.Event;
import com.linglong.rpc.exception.RpcException;
import com.linglong.rpc.common.life.AbstractService;
import com.linglong.rpc.common.remoting.Channel;
import com.linglong.rpc.common.service.IServiceContext;
import com.linglong.rpc.common.utils.Daemon;
import com.linglong.rpc.common.utils.ThreadPoolUtils;
import com.linglong.rpc.server.event.RequestEvent;
import com.linglong.rpc.server.event.ResponseEvent;
import com.linglong.rpc.server.event.enums.ServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @author Stereo
 * @version 2013.12.21 首版
 */
public class ServiceHandler extends AbstractService implements ServiceEventHandler<RequestEvent, ResponseEvent> {

    private static Logger LOG = LoggerFactory.getLogger(ServiceHandler.class);
    private Config config;
    private ExecutorService handlerPool;
    private final IServiceInvoker serviceInvoker;

    public ServiceHandler(IServiceContext skeletonContext, Config config) {
        super("ServiceHandler");
        serviceInvoker = new ServiceInvoker(skeletonContext);
        this.config = config;
    }

    void initHandlerPool() throws RpcException {
        int minPoolSize;
        int aliveTime;
        int maxPoolSize = config.getBusinessPoolSize();
        if (Constants.THREADPOOL_TYPE_FIXED.equals(config.getBusinessPoolType())) {
            minPoolSize = maxPoolSize;
            aliveTime = 0;
        } else if (Constants.THREADPOOL_TYPE_CACHED.equals(config.getBusinessPoolType())) {
            minPoolSize = 20;
            maxPoolSize = Math.max(minPoolSize, maxPoolSize);
            aliveTime = 60000;
        } else {
            throw new RpcException("HandlerPool-" + config.getBusinessPoolType());
        }
        boolean isPriority = Constants.QUEUE_TYPE_PRIORITY.equals(config.getBusinessPoolQueueType());
        BlockingQueue<Runnable> configQueue = ThreadPoolUtils.buildQueue(config.getBusinessPoolQueueSize(), isPriority);
        Daemon.DaemonFactory threadFactory = new Daemon.DaemonFactory();
        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
            private int i = 1;

            @Override
            public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
                if (i++ % 7 == 0) {
                    i = 1;
                    LOG.warn("Task:{} has been reject for InvokerPool exhausted!" +
                                    " pool:{}, active:{}, queue:{}, taskcnt: {}",
                            new Object[]{
                                    runnable,
                                    executor.getPoolSize(),
                                    executor.getActiveCount(),
                                    executor.getQueue().size(),
                                    executor.getTaskCount()
                            });
                }
                throw new RejectedExecutionException("Biz thread pool of provider has bean exhausted");
            }
        };
        handlerPool = new ThreadPoolExecutor(minPoolSize, maxPoolSize,
                aliveTime, TimeUnit.MILLISECONDS,
                configQueue, threadFactory, handler);
    }

    void shutdown() {
        if (handlerPool != null && !handlerPool.isShutdown())
            handlerPool.shutdown();
    }

    @Override
    public void handleRequest(RequestEvent request) throws Exception {
        handlerPool.execute(() -> {
            ServiceContext.begin(request.getTarget(), request.getChannel());
            try {
                //创建服务调用
                ServiceCall call = new ServiceCall(request.getTarget());
                //执行调用
                serviceInvoker.invoke(call);
                //清理调用入参数据
                call.cleanArguments();
                //响应结果
                replyResponse(new ResponseEvent(request.getTarget(), request.getChannel()));
            } catch (Exception ex) {
                LOG.error("service handler request handle failed, request packet:{} ", request.getTarget(), ex);
            } finally {
                ServiceContext.end();
            }
        });
    }

    @Override
    public void replyResponse(ResponseEvent response) throws Exception {
        Channel channel = response.getChannel();
        channel.send(response.getTarget(), true);
    }

    @Override
    public IServiceInvoker getServiceInvoker() {
        return serviceInvoker;
    }

    @Override
    public void handle(final Event<ServiceEnum> event) {
        ServiceEnum type = event.getType();
        LOG.info("ServiceHandler handle event:{} type:{}", event, type);
    }

    @Override
    protected void serviceInit() throws Exception {
        initHandlerPool();
    }

    @Override
    protected void serviceStart() throws Exception {
    }

    @Override
    protected void serviceStop() throws Exception {
        shutdown();
    }
}