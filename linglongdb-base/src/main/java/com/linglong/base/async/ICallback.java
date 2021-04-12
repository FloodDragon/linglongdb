package com.linglong.base.async;


@FunctionalInterface
public interface ICallback<T, V> {

    default void begin() {
    }

    void result(boolean success, T param, WorkResult<V> workResult);
}
