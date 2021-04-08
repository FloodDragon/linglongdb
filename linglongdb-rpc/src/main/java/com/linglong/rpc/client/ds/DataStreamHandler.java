package com.linglong.rpc.client.ds;


/**
 * 数据流监听器
 * <p>
 * Created by liuj-ai on 2021/4/7.
 */
public interface DataStreamHandler {

    void handle(Object data);
}
