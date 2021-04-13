package com.linglong.rpc.client;

import com.linglong.rpc.common.config.Constants;
import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.common.service.IService;
import com.linglong.rpc.exception.RpcException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

/**
 * 数据流远程代理
 * <p>
 * Created by liuj-ai on 2021/4/8.
 */
public class DataStreamRemoteProxy<S extends IService> extends RemoteProxy<S> {

    protected S service;
    private DataStream<S> _dataStream;
    private final ThreadLocal<DataStreamHandler> dataStreamHandlerThreadLocal = new ThreadLocal<>();
    private final Map<String, DataStreamProcessor> dataStreamProcessorMap = new ConcurrentHashMap<>();

    protected DataStreamRemoteProxy(ClientProxy proxy, Class<S> type) {
        super(proxy, type);
        this._dataStream = new DataStreamImpl();
    }

    protected final void setService(S service) {
        this.service = service;
    }

    protected final DataStream<S> getDataStream() {
        return _dataStream;
    }

    private final class DataStreamProcessor {
        private String id;
        private Packet response;
        private final int readTimeout;
        private final int maxCapacity;
        private DataStreamHandler handler;
        private volatile long pollSize = 0;
        private final ArrayDeque<Object> dataQueue;
        private volatile boolean completed = false;

        private DataStreamProcessor(String id,
                                    DataStreamHandler handler) {
            this.id = id;
            this.handler = handler;
            this.maxCapacity = handler.getQueueMaxCapacity();
            this.dataQueue = new ArrayDeque<>(maxCapacity);
            this.readTimeout = getClientProxy().getConfig().getReadTimeout();
            DataStreamRemoteProxy.this.dataStreamProcessorMap.put(id, this);
        }

        private void process(Packet packet, AsyncFuture<Packet> returnFuture) {
            byte type = packet.getType();
            if (type == Constants.TYPE_DATA_STREAM) {
                synchronized (this) {
                    while (this.maxCapacity <= dataQueue.size()) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            //ignore
                        }
                    }
                    if (!completed) {
                        dataQueue.offer(packet.getResult());
                        notifyAll();
                    }
                }
            } else if (type == Constants.TYPE_DATA_STREAM_RESPONSE) {
                returnFuture.done(packet);
                completed();
            } else {
                //ignore
            }
        }

        private void holdOn(AsyncFuture<Packet> returnFuture) throws InterruptedException, ExecutionException, TimeoutException {
            try {
                loop:
                while (!completed) {
                    Object data;
                    synchronized (this) {
                        inner:
                        while (dataQueue.size() == 0) {
                            long pollSize = this.pollSize;
                            wait(readTimeout);
                            if (completed) {
                                break inner;
                            }
                            if (this.pollSize == pollSize && dataQueue.size() == 0) {
                                throw new TimeoutException();
                            }
                        }
                        if (!completed) {
                            pollSize++;
                            data = dataQueue.poll();
                        } else {
                            break loop;
                        }
                    }
                    if (handler != null) {
                        handler.handle(data);
                    }
                }
                if (dataQueue.size() > 0) {
                    Iterator<Object> it = dataQueue.iterator();
                    while (it.hasNext()) {
                        if (handler != null) {
                            handler.handle(it.next());
                        }
                    }
                }
                response = returnFuture.get(readTimeout, TimeUnit.MILLISECONDS);
            } finally {
                completed();
                this.handler = null;
                returnFuture.done(null);
                DataStreamRemoteProxy.this.dataStreamProcessorMap.remove(id);
                dataQueue.clear();
            }
        }

        private void completed() {
            if (!completed) {
                synchronized (this) {
                    if (!completed) {
                        completed = true;
                        notifyAll();
                    }
                }
            }
        }
    }

    public class DataStreamImpl implements DataStream<S> {
        @Override
        public void onStream(Packet packet, AsyncFuture<Packet> future) {
            DataStreamProcessor processor;
            if (null != (processor = dataStreamProcessorMap.get(packet.getId()))) {
                processor.process(packet, future);
            } else {
                LOG.error("not found data stream processor.");
            }
        }

        @Override
        public void call(DataStreamExecutor<S> executor, DataStreamHandler handler) {
            if (executor != null) {
                try {
                    dataStreamHandlerThreadLocal.set(handler);
                    executor.execute(DataStreamRemoteProxy.this.service);
                } finally {
                    dataStreamHandlerThreadLocal.remove();
                }
            }
        }
    }

    @Override
    protected Packet packetRequest(String serviceName, Method method, Object[] args) {
        return Packet.packetDataStreamRequest(serviceName, method.getName(), method.getReturnType(), args);
    }

    protected AsyncFuture<Packet> send(Packet packet, DataStream<?> dataStream) throws RpcException {
        return getClientProxy().sendPacket(packet, dataStream);
    }

    @Override
    protected Object sendRequest(Packet packet) throws RpcException {
        DataStreamProcessor processor = new DataStreamProcessor(packet.getId(), dataStreamHandlerThreadLocal.get());
        AsyncFuture<Packet> returnFuture = send(packet, _dataStream);
        try {
            processor.holdOn(returnFuture);
            return receiveResponse(processor.response);
        } catch (InterruptedException | TimeoutException | ExecutionException ex) {
            throw new RpcException("ClientProxy >>> read packet timeout " + "packet : " + processor.response);
        }
    }
}
