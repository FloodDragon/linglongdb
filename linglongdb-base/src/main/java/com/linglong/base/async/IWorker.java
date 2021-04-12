package com.linglong.base.async;

import java.util.Map;

@FunctionalInterface
public interface IWorker<T, V> {
    
    V action(T object, Map<String, WorkerDefine> allDefines);

    default V defaultValue() {
        return null;
    }
}
