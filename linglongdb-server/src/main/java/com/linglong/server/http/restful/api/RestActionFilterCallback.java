package com.linglong.server.http.restful.api;


/**
 * @author Stereo on 2019/9/11.
 */
public class RestActionFilterCallback<R> {

    interface Callable<V> {
        V call();
    }

    private Callable<R> function;
    private R filterResult;

    public RestActionFilterCallback(Callable<R> function) {
        this.function = function;
    }

    public void call() {
        this.filterResult = function.call();
    }

    public R getFilterResult() {
        return filterResult;
    }
}
