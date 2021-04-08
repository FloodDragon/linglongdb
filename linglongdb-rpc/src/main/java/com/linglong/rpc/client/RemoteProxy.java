package com.linglong.rpc.client;

import com.linglong.rpc.client.ds.DataStream;
import com.linglong.rpc.client.ds.DataStreamExecutor;
import com.linglong.rpc.client.ds.DataStreamHandler;
import com.linglong.rpc.common.config.Constants;
import com.linglong.rpc.common.life.LifeService;
import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.common.service.IService;
import com.linglong.rpc.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 远程调用代理
 *
 * @author Stereo on 2019/12/10.
 */
public final class RemoteProxy<S extends IService> implements InvocationHandler {

    private static Logger LOG = LoggerFactory.getLogger(RemoteProxy.class);
    private S service;
    private Class<S> _type;
    private ClientProxy _clientProxy;
    private DataStream<S> _dataStream;
    private final WeakHashMap<Method, String> _mangleMap = new WeakHashMap<>();
    private final Map<String, DataStreamHandler> dataStreamHandlerMap = new ConcurrentHashMap<>();
    private final ThreadLocal<DataStreamHandler> dataStreamHandlerThreadLocal = new ThreadLocal<>();

    protected RemoteProxy(ClientProxy proxy, Class<S> type) {
        this._clientProxy = proxy;
        this._type = type;
        this._dataStream = new DataStreamImpl();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (_clientProxy.getServiceState().equals(LifeService.STATE.STARTED)) {
            String mangleName;
            synchronized (_mangleMap) {
                mangleName = _mangleMap.get(method);
            }
            if (mangleName == null) {
                String methodName = method.getName();
                Class<?>[] params = method.getParameterTypes();

                if (methodName.equals("equals") && params.length == 1
                        && params[0].equals(Object.class)) {
                    Object value = args[0];
                    if (value == null || !Proxy.isProxyClass(value.getClass()))
                        return Boolean.FALSE;
                    Object proxyHandler = Proxy.getInvocationHandler(value);
                    if (!(proxyHandler instanceof RemoteProxy))
                        return Boolean.FALSE;
                    RemoteProxy handler = (RemoteProxy) proxyHandler;
                    return new Boolean(_clientProxy.equals(handler.getClientProxy()));
                } else if (methodName.equals("hashCode") && params.length == 0)
                    return new Integer(_clientProxy.hashCode());
                else if (methodName.equals("getType"))
                    return proxy.getClass().getInterfaces()[0].getName();
                else if (methodName.equals("toString") && params.length == 0)
                    return "Proxy[" + _clientProxy.toString() + "]";
                mangleName = method.getName();
                synchronized (_mangleMap) {
                    _mangleMap.put(method, mangleName);
                }
            }

            //构建消息体
            final Packet packet = Packet.packetRequest(_type.getName(), method.getName(), method.getReturnType(), args);
            //发送请求
            return sendRequest(packet);
        } else {
            throw new RpcException("ClientProxy >>> state is not started");
        }
    }

    protected Object sendRequest(Packet packet) throws RpcException {
        DataStreamHandler dataStreamHandler = dataStreamHandlerThreadLocal.get();
        if (dataStreamHandler != null) {
            dataStreamHandlerMap.put(packet.getId(), dataStreamHandler);
        }
        try {
            //发送请求体
            AsyncFuture<Packet> future = _clientProxy.sendPacket(packet, this._dataStream);
            //接收响应体
            return receiveResponse(future);
        } finally {
            if (dataStreamHandler != null) {
                dataStreamHandlerMap.remove(packet.getId());
            }
        }
    }

    protected Packet waitResponse(AsyncFuture<Packet> future) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(getClientProxy().getConfig().getReadTimeout(), TimeUnit.MILLISECONDS);
    }

    protected Object receiveResponse(AsyncFuture<Packet> future) throws RpcException {
        Packet response = null;
        try {
            //等待响应结果
            response = waitResponse(future);
            //返回结果
            Object result = response.getResult();
            byte state = response.getState();
            String exc = null;
            switch (state) {
                case Constants.STATUS_PENDING:
                    exc = "ClientProxy >>> request is not processed";
                    break;
                case Constants.STATUS_SUCCESS_RESULT:
                    if (isReturnType(response.getReturnType(), result.getClass())) {
                        return result;
                    } else
                        exc = "ClientProxy >>> result type error";
                    break;
                case Constants.STATUS_SUCCESS_NULL:
                    return null;
                case Constants.STATUS_SUCCESS_VOID:
                    return null;
                case Constants.STATUS_SERVICE_NOT_FOUND:
                    exc = "ClientProxy >>> request Service is not found";
                    throw new ServiceNotFoundException(exc);
                case Constants.STATUS_METHOD_NOT_FOUND:
                    exc = "ClientProxy >>> request action method is not found";
                    throw new MethodNotFoundException(exc);
                case Constants.STATUS_ACCESS_DENIED:
                    exc = "ClientProxy >>> request action access denied";
                    throw new NotAllowedException(exc);
                case Constants.STATUS_INVOCATION_EXCEPTION:
                    exc = "ClientProxy >>> request action method invocation failed";
                    throw new InvocationException(exc);
                case Constants.STATUS_GENERAL_EXCEPTION:
                    exc = response.getException();
                    break;
            }
            if (exc != null) {
                throw new RpcException(exc);
            } else {
                return null;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException ex) {
            future.done(null);
            throw new RpcException("ClientProxy >>> read packet timeout " + "packet : " + response);
        }
    }

    private boolean isReturnType(Class<?> original, Class<?> current) {
        Map<Class, String> primitiveClassMap = Constants.primitiveClassMap;
        if (primitiveClassMap.get(original) != null) {
            return primitiveClassMap.get(original).equals(primitiveClassMap.get(current));
        } else {
            return original.isAssignableFrom(current);
        }
    }

    private ClientProxy getClientProxy() {
        return _clientProxy;
    }

    final DataStream<S> getDataStream() {
        return _dataStream;
    }

    final void setService(S service) {
        this.service = service;
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
                executor.execute(RemoteProxy.this.service);
            } finally {
                dataStreamHandlerThreadLocal.remove();
            }
        }
    }
}
