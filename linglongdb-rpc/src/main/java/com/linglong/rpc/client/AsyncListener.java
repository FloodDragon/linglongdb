package com.linglong.rpc.client;

/**
 * @author Stereo
 */
public interface AsyncListener<T> {
	void asyncReturn(T returnValue);
}
