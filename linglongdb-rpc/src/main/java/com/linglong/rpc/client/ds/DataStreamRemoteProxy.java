package com.linglong.rpc.client.ds;

import com.linglong.rpc.client.AsyncFuture;
import com.linglong.rpc.client.ClientProxy;
import com.linglong.rpc.client.RemoteProxy;
import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.common.service.IService;
import com.linglong.rpc.exception.RpcException;

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
    private final Map<String, DataStreamHandler> dataStreamHandlerMap = new ConcurrentHashMap<>();
    private final ThreadLocal<DataStreamHandler> dataStreamHandlerThreadLocal = new ThreadLocal<>();

    public DataStreamRemoteProxy(ClientProxy proxy, Class<S> type) {
        super(proxy, type);
        this._dataStream = new DataStreamImpl();
    }

    public final void setService(S service) {
        this.service = service;
    }

    public final DataStream<S> getDataStream() {
        return _dataStream;
    }

    public class DataStreamImpl implements DataStream<S> {
        @Override
        public void onStream(Packet packet) {
            DataStreamHandler handler;
            if (null != (handler = dataStreamHandlerMap.get(packet.getId()))) {
                handler.handle(packet.getResult());
            }
        }

        @Override
        public void call(DataStreamExecutor<S> executor, DataStreamHandler handler) {
            try {
                dataStreamHandlerThreadLocal.set(handler);
                executor.execute(DataStreamRemoteProxy.this.service);
            } finally {
                dataStreamHandlerThreadLocal.remove();
            }
        }
    }

    protected Object sendRequest(Packet packet) throws RpcException {
        Packet response = null;
        DataStreamHandler dataStreamHandler = dataStreamHandlerThreadLocal.get();
        if (dataStreamHandler != null) {
            dataStreamHandlerMap.put(packet.getId(), dataStreamHandler);
        }
        //发送请求体
        AsyncFuture<Packet> future = send(packet, _dataStream);
        try {
            response = future.get(getClientProxy().getConfig().getReadTimeout(), TimeUnit.MILLISECONDS);
            //接收响应体
            return receiveResponse(response);
        } catch (InterruptedException | TimeoutException | ExecutionException ex) {
            future.done(null);
            throw new RpcException("ClientProxy >>> read packet timeout " + "packet : " + response);
        } finally {
            if (dataStreamHandler != null) {
                dataStreamHandlerMap.remove(packet.getId());
            }
        }
    }
}
