package com.glodon.linglong.engine.core;

import com.glodon.linglong.base.exception.UnmodifiableViewException;
import com.glodon.linglong.engine.Ordering;
import com.glodon.linglong.engine.core.lock.LockResult;
import com.glodon.linglong.engine.core.tx.Transaction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;

/**
 * @author Stereo
 */
public abstract class WrappedCursor<C extends Cursor> implements Cursor {
    protected final C source;

    protected WrappedCursor(C source) {
        this.source = source;
    }

    @Override
    public long valueLength() throws IOException {
        return source.valueLength();
    }

    @Override
    public void valueLength(long length) throws IOException {
        throw new UnmodifiableViewException();
    }

    @Override
    public int valueRead(long pos, byte[] buf, int off, int len) throws IOException {
        return source.valueRead(pos, buf, off, len);
    }

    @Override
    public void valueWrite(long pos, byte[] buf, int off, int len) throws IOException {
        throw new UnmodifiableViewException();
    }

    @Override
    public void valueClear(long pos, long length) throws IOException {
        throw new UnmodifiableViewException();
    }

    @Override
    public InputStream newValueInputStream(long pos) throws IOException {
        return source.newValueInputStream(pos);
    }

    @Override
    public InputStream newValueInputStream(long pos, int bufferSize) throws IOException {
        return source.newValueInputStream(pos, bufferSize);
    }

    @Override
    public OutputStream newValueOutputStream(long pos) throws IOException {
        throw new UnmodifiableViewException();
    }

    @Override
    public OutputStream newValueOutputStream(long pos, int bufferSize) throws IOException {
        throw new UnmodifiableViewException();
    }

    @Override
    public Ordering getOrdering() {
        return source.getOrdering();
    }

    @Override
    public Comparator<byte[]> getComparator() {
        return source.getComparator();
    }

    @Override
    public Transaction link(Transaction txn) {
        return source.link(txn);
    }

    @Override
    public Transaction link() {
        return source.link();
    }

    @Override
    public byte[] key() {
        return source.key();
    }

    @Override
    public byte[] value() {
        return source.value();
    }

    @Override
    public boolean autoload(boolean mode) {
        return source.autoload(mode);
    }

    @Override
    public boolean autoload() {
        return source.autoload();
    }

    @Override
    public int compareKeyTo(byte[] rkey) {
        return source.compareKeyTo(rkey);
    }

    @Override
    public int compareKeyTo(byte[] rkey, int offset, int length) {
        return source.compareKeyTo(rkey, offset, length);
    }

    @Override
    public boolean register() throws IOException {
        return source.register();
    }

    @Override
    public void unregister() {
        source.unregister();
    }

    @Override
    public LockResult first() throws IOException {
        return source.first();
    }

    @Override
    public LockResult last() throws IOException {
        return source.last();
    }

    @Override
    public LockResult skip(long amount) throws IOException {
        return source.skip(amount);
    }

    @Override
    public LockResult skip(long amount, byte[] limitKey, boolean inclusive) throws IOException {
        return source.skip(amount, limitKey, inclusive);
    }

    @Override
    public LockResult next() throws IOException {
        return source.next();
    }

    @Override
    public LockResult nextLe(byte[] limitKey) throws IOException {
        return source.nextLe(limitKey);
    }

    @Override
    public LockResult nextLt(byte[] limitKey) throws IOException {
        return source.nextLt(limitKey);
    }

    @Override
    public LockResult previous() throws IOException {
        return source.previous();
    }

    @Override
    public LockResult previousGe(byte[] limitKey) throws IOException {
        return source.previousGe(limitKey);
    }

    @Override
    public LockResult previousGt(byte[] limitKey) throws IOException {
        return source.previousGt(limitKey);
    }

    @Override
    public LockResult find(byte[] key) throws IOException {
        return source.find(key);
    }

    @Override
    public LockResult findGe(byte[] key) throws IOException {
        return source.findGe(key);
    }

    @Override
    public LockResult findGt(byte[] key) throws IOException {
        return source.findGt(key);
    }

    @Override
    public LockResult findLe(byte[] key) throws IOException {
        return source.findLe(key);
    }

    @Override
    public LockResult findLt(byte[] key) throws IOException {
        return source.findLt(key);
    }

    @Override
    public LockResult findNearby(byte[] key) throws IOException {
        return source.findNearby(key);
    }

    @Override
    public LockResult findNearbyGe(byte[] key) throws IOException {
        return source.findNearbyGe(key);
    }

    @Override
    public LockResult findNearbyGt(byte[] key) throws IOException {
        return source.findNearbyGt(key);
    }

    @Override
    public LockResult findNearbyLe(byte[] key) throws IOException {
        return source.findNearbyLe(key);
    }

    @Override
    public LockResult findNearbyLt(byte[] key) throws IOException {
        return source.findNearbyLt(key);
    }

    @Override
    public LockResult random(byte[] lowKey, byte[] highKey) throws IOException {
        return source.random(lowKey, highKey);
    }

    @Override
    public LockResult lock() throws IOException {
        return source.lock();
    }

    @Override
    public LockResult load() throws IOException {
        return source.load();
    }

    @Override
    public void store(byte[] value) throws IOException {
        throw new UnmodifiableViewException();
    }

    @Override
    public void commit(byte[] value) throws IOException {
        throw new UnmodifiableViewException();
    }

    @Override
    public void reset() {
        source.reset();
    }

    @Override
    public void close() {
        source.close();
    }
}
