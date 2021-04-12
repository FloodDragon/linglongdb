package com.linglong.server.database.process;

import com.linglong.base.concurrent.LatchCondition;
import com.linglong.base.concurrent.RWLock;
import com.linglong.server.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 迭代处理器
 * <p>
 * Created by liuj-ai on 2021/3/26.
 */
public abstract class ProcessIterator extends Actor implements Iterator<Map.Entry<byte[], byte[]>> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ProcessIterator.class);

    public interface ProcessIteratorFunction {
        Map.Entry<byte[], byte[]> apply(byte[] key, byte[] value);
    }

    private String id;
    private volatile boolean isDone;
    protected final RWLock rwLock = new RWLock();
    private final Queue<Map.Entry<byte[], byte[]>> queue;
    private final LatchCondition notEmpty = new LatchCondition();
    protected final LatchCondition notFull = new LatchCondition();
    protected final LatchCondition started = new LatchCondition();

    public ProcessIterator() {
        this(32);
    }

    public ProcessIterator(int maxCapacity) {
        this.id = MixAll.getUUID();
        this.queue = new MaxCapacityQueue<>(new LinkedList<>(), maxCapacity);
        new Daemon(this).start();
        this.waitStarted();
    }

    private void waitStarted() {
        try {
            rwLock.acquireExclusive();
            started.await(rwLock);
        } finally {
            rwLock.releaseExclusive();
        }
    }

    @Override
    protected void doAct() throws InterruptedException {
        try {
            rwLock.acquireExclusive();
            this.started.signal();
            this.isDone = true;
            done((k, v) -> apply(k, v));
            this.isDone = false;
            while (!queue.isEmpty()) {
                notFull.await(rwLock);
            }
        } catch (Exception e) {
            LOGGER.error("do act error.", e);
        } finally {
            super.stop();
            rwLock.releaseExclusive();
        }
    }

    private Map.Entry<byte[], byte[]> apply(byte[] key, byte[] value) {
        Map.Entry<byte[], byte[]> entry = new AbstractMap.SimpleEntry<>(key, value);
        boolean added;
        loop:
        for (; ; ) {
            added = queue.offer(entry);
            if (!added) {
                notFull.await(rwLock);
            } else {
                break loop;
            }
        }
        notEmpty.signal();
        return entry;
    }

    @Override
    public boolean hasNext() {
        try {
            rwLock.acquireShared();
            return !this.isDone && !queue.isEmpty();
        } finally {
            rwLock.releaseShared();
        }
    }

    protected abstract void done(ProcessIteratorFunction function) throws Exception;

    @Override
    public Map.Entry<byte[], byte[]> next() {
        try {
            rwLock.acquireExclusive();
            Map.Entry<byte[], byte[]> entry;
            for (; ; ) {
                entry = queue.poll();
                if (entry == null) {
                    notEmpty.await(rwLock);
                } else {
                    break;
                }
            }
            notFull.signal();
            return entry;
        } finally {
            rwLock.releaseExclusive();
        }
    }

    public String getId() {
        return id;
    }
}
