package com.linglong.rpc.client;

/**
 * Created by liuj-ai on 2019/12/11.
 */
public interface AsyncListener<T> {
	void asyncReturn(T returnValue);
}
