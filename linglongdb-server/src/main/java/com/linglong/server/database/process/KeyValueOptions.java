package com.linglong.server.database.process;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by liuj-ai on 2021/3/24.
 */
public class KeyValueOptions extends IndexName {
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
    //索引数据扫描<扫描数据,是否继续扫描>
    Consumer<Map.Entry<byte[], byte[]>> scanFunc;

    public KeyValueOptions key(byte[] key) {
        this.key = key;
        return this;
    }

    public KeyValueOptions value(byte[] value) {
        this.value = value;
        return this;
    }

    public KeyValueOptions oldValue(byte[] oldValue) {
        this.oldValue = oldValue;
        return this;
    }

    public KeyValueOptions lowKey(byte[] lowKey) {
        this.lowKey = lowKey;
        return this;
    }

    public KeyValueOptions highKey(byte[] highKey) {
        this.highKey = highKey;
        return this;
    }

    public KeyValueOptions count(long count) {
        this.count = count;
        return this;
    }

    public KeyValueOptions newTxn() {
        this.newTxn = true;
        this.openedTxn = false;
        this.openedTxnId = null;
        return this;
    }

    public KeyValueOptions indexName(String name) {
        super.indexName(name);
        return this;
    }

    public KeyValueOptions txn(Long txnId) {
        this.openedTxn = true;
        this.newTxn = false;
        this.openedTxnId = txnId;
        return this;
    }

    public KeyValueOptions evict(KeyValueEvictFilter filter) {
        this.evictFilter = filter;
        return this;
    }

    public KeyValueOptions scanFunc(Consumer<Map.Entry<byte[], byte[]>> scanFunc) {
        this.scanFunc = scanFunc;
        return this;
    }

    public KeyValueOptions updater(KeyValueUpdater updater) {
        this.updater = updater;
        return this;
    }

    public KeyValueOptions pid(String pid) {
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

    public Consumer<Map.Entry<byte[], byte[]>> getScanFunc() {
        return scanFunc;
    }
}
