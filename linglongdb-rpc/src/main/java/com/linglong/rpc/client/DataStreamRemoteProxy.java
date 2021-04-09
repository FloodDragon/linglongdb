package com.linglong.rpc.client;

import com.linglong.rpc.common.config.Constants;
import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.common.service.IService;
import com.linglong.rpc.common.utils.SystemClock;
import com.linglong.rpc.exception.RpcException;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
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
        private DataStreamHandler handler;
        private AsyncFuture<Packet> future;
        private volatile long lastActiveTime;

        private DataStreamProcessor(String id, DataStreamHandler handler, AsyncFuture<Packet> future) {
            this.id = id;
            this.future = future;
            this.handler = handler;
            this.lastActiveTime = SystemClock.now();
            this.readTimeout = getClientProxy().getConfig().getReadTimeout();
            DataStreamRemoteProxy.this.dataStreamProcessorMap.put(id, this);
        }

        private void process(Packet packet) {
            byte type = packet.getType();
            if (type == Constants.TYPE_DATA_STREAM) {
                lastActiveTime = SystemClock.now();
                if (handler != null) {
                    handler.handle(packet.getResult());
                }
            } else {
                future.done(packet);
            }
        }

        private boolean isActive() {
            return SystemClock.now() - lastActiveTime < readTimeout;
        }

        private void holdOn() throws InterruptedException, ExecutionException, TimeoutException {
            while (isActive()) {

            }
            response = future.get(readTimeout, TimeUnit.MILLISECONDS);
        }

        private void clear() {
            DataStreamRemoteProxy.this.dataStreamProcessorMap.remove(id);
            this.handler = null;
        }
    }

    public class DataStreamImpl implements DataStream<S> {
        @Override
        public void onStream(Packet packet) {
            DataStreamProcessor processor;
            if (null != (processor = dataStreamProcessorMap.get(packet.getId()))) {
                processor.process(packet);
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
        AsyncFuture<Packet> future = send(packet, _dataStream);
        DataStreamProcessor processor = new DataStreamProcessor(packet.getId(), dataStreamHandlerThreadLocal.get(), future);
        try {
            processor.holdOn();
            return receiveResponse(processor.response);
        } catch (InterruptedException | TimeoutException | ExecutionException ex) {
            future.done(null);
            throw new RpcException("ClientProxy >>> read packet timeout " + "packet : " + processor.response);
        } finally {
            processor.clear();
        }
    }
}
