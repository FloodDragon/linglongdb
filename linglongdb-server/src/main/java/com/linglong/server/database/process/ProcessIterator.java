package com.linglong.server.database.process;

import com.linglong.server.utils.Actor;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 迭代处理器
 * <p>
 * Created by liuj-ai on 2021/3/26.
 */
public abstract class ProcessIterator<R> extends Actor implements Iterator<R> {

    private Exception exception;

    public ProcessIterator() {
    }

    @Override
    protected void doAct() throws InterruptedException {

    }

    @Override
    public boolean hasNext() {
        return false;
    }

    protected abstract void done() throws Exception;

    protected abstract R apply(byte[] key, byte[] value);

    @Override
    public R next() {

        return null;
    }
}
