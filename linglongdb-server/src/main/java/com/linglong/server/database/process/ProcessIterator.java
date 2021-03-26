package com.linglong.server.database.process;

import com.linglong.server.utils.Actor;
import com.linglong.server.utils.Daemon;
import com.linglong.server.utils.WaitNotifyObject;

import java.util.Iterator;

/**
 * 迭代处理器
 * <p>
 * Created by liuj-ai on 2021/3/26.
 */
public class ProcessIterator<R> extends Actor implements Iterator<R> {

    private ProcessFuture<R> future;
    private Exception exception;

    public ProcessIterator(ProcessFuture<R> future) {
        this.future = future;
        new Daemon(this).start();
    }

    @Override
    protected void doAct() throws InterruptedException {
        try {
            future.process(null);
        } catch (Exception ex) {
            this.exception = ex;
        } finally {
            stop();
        }
    }

    @Override
    public boolean hasNext() {
        return !future.isDone();
    }

    @Override
    public R next() {
        return future.get();
    }

    public ProcessFuture<R> getFuture() {
        return future;
    }

    public Exception getException() {
        return exception;
    }
}
