package com.linglong.base.async;

public interface ITimeoutWorker<T, V> extends IWorker<T, V> {

    long timeOut();

    boolean enableTimeOut();
}
