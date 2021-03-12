package com.linglong.rpc.client;

/**
 * @author Stereo
 */
public interface Callback<T> {

	void call(T value);

	Class<?> getAcceptValueType();
}
