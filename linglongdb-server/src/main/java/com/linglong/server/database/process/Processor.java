package com.linglong.server.database.process;

/**
 * Created by liuj-ai on 2021/3/26.
 */
public interface Processor<T, R> {

    R doProcess(T t) throws Exception;

    default R process(T t) throws Exception {
        R r = null;
        try {
            before(t);
            return r = doProcess(t);
        } finally {
            after(r);
        }
    }

    default void before(T t) throws Exception {
    }

    default void after(R r) throws Exception {
    }
}
