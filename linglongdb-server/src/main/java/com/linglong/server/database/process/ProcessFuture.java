package com.linglong.server.database.process;


import com.linglong.server.utils.WaitNotifyObject;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by liuj-ai on 2021/3/26.
 */
public abstract class ProcessFuture<R> extends WaitNotifyObject implements Processor<Void, R> {

    protected final Queue<R> queue = new ArrayDeque<>(20);

    /**
     * 是否完成
     *
     * @return
     */
    abstract boolean isDone();

    /**
     * 获取结果
     *
     * @return
     */
    abstract R get();
}
