package com.linglong.rpc.client;

import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.common.service.IService;

/**
 * 数据流调用接口
 * <p>
 * Created by liuj-ai on 2021/4/8.
 */
public interface DataStream<S extends IService> {

    void onStream(Packet packet, AsyncFuture<Packet> asyncFuture);

    void call(DataStreamExecutor<S> executor, DataStreamHandler handler);
}


