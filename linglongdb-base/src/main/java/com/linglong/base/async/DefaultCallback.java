package com.linglong.base.async;


public class DefaultCallback<T, V> implements ICallback<T, V> {
    @Override
    public void begin() {

    }

    @Override
    public void result(boolean success, T param, WorkResult<V> workResult) {
    }
}
