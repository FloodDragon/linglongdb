package com.linglong.rpc.client.ds;

import com.linglong.rpc.common.protocol.Packet;
import com.linglong.rpc.common.service.IService;

/**
 * 数据流调用接口
 * <p>
 * <p>
 * Created by liuj-ai on 2021/4/8.
 */
public interface DataStream<S extends IService> {

    void onStream(Packet packet);

    /**
     * 执行调用数据流
     *
     * @param executor
     * @return
     */
    void call(DataStreamExecutor<S> executor, DataStreamHandler handler);
}


