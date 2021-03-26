package com.linglong.server.database.process;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by liuj-ai on 2021/3/24.
 */
public class _Options extends _IndexName {
    byte[] key;
    byte[] value;
    byte[] oldValue;
    byte[] lowKey;
    byte[] highKey;
    long count;
    String pid;

    //开启新事务
    boolean newTxn;
    //已开启事务
    Long openedTxnId;
    boolean openedTxn;
    //索引更新器
    KeyValueUpdater updater;
    //索引数据驱逐过滤
    KeyValueEvictFilter evictFilter;
    //索引数据扫描
    Consumer<Map.Entry<byte[], byte[]>> scanConsumer;

    public _Options key(byte[] key) {
        this.key = key;
        return this;
    }

    public _Options value(byte[] value) {
        this.value = value;
        return this;
    }

    public _Options oldValue(byte[] value) {
        this.oldValue = oldValue;
        return this;
    }

    public _Options lowKey(byte[] lowKey) {
        this.lowKey = lowKey;
        return this;
    }

    public _Options highKey(byte[] highKey) {
        this.highKey = highKey;
        return this;
    }

    public _Options count(long count) {
        this.count = count;
        return this;
    }

    public _Options newTxn() {
        this.newTxn = true;
        this.openedTxn = false;
        this.openedTxnId = null;
        return this;
    }

    public _Options indexName(String name) {
        super.indexName(name);
        return this;
    }

    public _Options txn(Long txnId) {
        this.openedTxn = true;
        this.newTxn = false;
        this.openedTxnId = txnId;
        return this;
    }

    public _Options evict(KeyValueEvictFilter filter) {
        this.evictFilter = filter;
        return this;
    }

    public _Options scan(Consumer<Map.Entry<byte[], byte[]>> consumer) {
        this.scanConsumer = consumer;
        return this;
    }

    public _Options updater(KeyValueUpdater updater) {
        this.updater = updater;
        return this;
    }

    public _Options pid(String pid) {
        this.pid = pid;
        return this;
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public byte[] getOldValue() {
        return oldValue;
    }

    public byte[] getLowKey() {
        return lowKey;
    }

    public byte[] getHighKey() {
        return highKey;
    }

    public long getCount() {
        return count;
    }

    public boolean isNewTxn() {
        return newTxn;
    }

    public Long getOpenedTxnId() {
        return openedTxnId;
    }

    public boolean isOpenedTxn() {
        return openedTxn;
    }

    public KeyValueUpdater getUpdater() {
        return updater;
    }

    public KeyValueEvictFilter getEvictFilter() {
        return evictFilter;
    }

    public Consumer<Map.Entry<byte[], byte[]>> getScanConsumer() {
        return scanConsumer;
    }
}
