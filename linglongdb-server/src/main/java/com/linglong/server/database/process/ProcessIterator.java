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
    private volatile boolean hasNext;
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
            this.hasNext = true;
            done((k, v) -> apply(k, v));
            while (queue.size() != 0) {
                notFull.await(rwLock);
            }
        } catch (Exception e) {
            LOGGER.error("do act error.", e);
        } finally {
            this.hasNext = false;
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
            return this.hasNext;
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

    public static void main(String[] args) throws InterruptedException {
        ProcessIterator iterator = new ProcessIterator() {
            @Override
            protected void done(ProcessIteratorFunction function) throws Exception {
                for (int i = 0; i < 100; i++) {
                    byte[] bytes = String.valueOf(i).getBytes();
                    function.apply(bytes, bytes);
                    System.out.println("生产 " + i);
                }
            }
        };
        while (iterator.hasNext()) {
            Map.Entry<byte[], byte[]> entry = iterator.next();
            System.out.println("消费 -> key=" + new String(entry.getKey()) + " value=" + new String(entry.getValue()));
        }
        System.out.println("测试执行结束");
    }
}
