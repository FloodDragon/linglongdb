package com.glodon.linglong.engine.core.view;

import com.glodon.linglong.base.common.Ordering;
import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.exception.LockFailureException;
import com.glodon.linglong.engine.core.frame.AbstractValueAccessor;
import com.glodon.linglong.engine.core.frame.Cursor;
import com.glodon.linglong.engine.core.frame.Transformer;
import com.glodon.linglong.engine.core.lock.LockResult;
import com.glodon.linglong.engine.core.tx.Transaction;
import com.glodon.linglong.engine.core.frame.ViewUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Stereo
 */
public final class TransformedCursor extends AbstractValueAccessor implements Cursor {
    private final Cursor mSource;
    private final Transformer mTransformer;

    private byte[] mKey;
    private byte[] mValue;

    public TransformedCursor(Cursor source, Transformer transformer) {
        mSource = source;
        mTransformer = transformer;
    }

    @Override
    public Ordering getOrdering() {
        return mTransformer.transformedOrdering(mSource.getOrdering());
    }

    @Override
    public Comparator<byte[]> getComparator() {
        return mTransformer.transformedComparator(mSource.getComparator());
    }

    @Override
    public Transaction link(Transaction txn) {
        return mSource.link(txn);
    }

    @Override
    public Transaction link() {
        return mSource.link();
    }

    @Override
    public byte[] key() {
        return mKey;
    }

    @Override
    public byte[] value() {
        return mValue;
    }

    @Override
    public boolean autoload(boolean mode) {
        return mSource.autoload(mode);
    }

    @Override
    public boolean autoload() {
        return mSource.autoload();
    }

    @Override
    public final int compareKeyTo(byte[] rkey) {
        return mSource.compareKeyTo(mTransformer.inverseTransformKey(rkey));
    }

    @Override
    public final int compareKeyTo(byte[] rkey, int offset, int length) {
        if (offset != 0 || length != rkey.length) {
            byte[] newRkey = new byte[length];
            System.arraycopy(rkey, offset, newRkey, 0, length);
            rkey = newRkey;
        }
        return mSource.compareKeyTo(mTransformer.inverseTransformKey(rkey));
    }

    @Override
    public boolean register() throws IOException {
        return mSource.register();
    }

    @Override
    public void unregister() {
        mSource.unregister();
    }

    @Override
    public LockResult first() throws IOException {
        LockResult result;
        try {
            result = mSource.first();
        } catch (LockFailureException e) {
            throw transformCurrent(e);
        }
        result = transformCurrent(result);
        return result == null ? next() : result;
    }

    @Override
    public LockResult last() throws IOException {
        LockResult result;
        try {
            result = mSource.last();
        } catch (LockFailureException e) {
            throw transformCurrent(e);
        }
        result = transformCurrent(result);
        return result == null ? previous() : result;
    }

    @Override
    public LockResult skip(long amount) throws IOException {
        return amount == 0 ? mSource.skip(0) : ViewUtils.skipWithLocks(this, amount);
    }

    @Override
    public LockResult skip(long amount, byte[] limitKey, boolean inclusive) throws IOException {
        return ViewUtils.skipWithLocks(this, amount, limitKey, inclusive);
    }

    @Override
    public LockResult next() throws IOException {
        final Cursor c = mSource;
        while (true) {
            LockResult result;
            try {
                result = c.next();
            } catch (LockFailureException e) {
                throw transformCurrent(e);
            }
            result = transformCurrent(result);
            if (result != null) {
                return result;
            }
        }
    }

    @Override
    public LockResult nextLe(final byte[] limitTKey) throws IOException {
        byte[] limitKey = inverseTransformKey(limitTKey);
        if (limitKey == null) {
            limitKey = mTransformer.inverseTransformKeyLt(limitTKey);
            if (limitKey == null) {
                reset();
                return LockResult.UNOWNED;
            }
        }
        final Cursor c = mSource;
        while (true) {
            LockResult result;
            try {
                result = c.nextLe(limitKey);
            } catch (LockFailureException e) {
                throw transformCurrent(e);
            }
            result = transformCurrent(result);
            if (result != null) {
                return result;
            }
        }
    }

    @Override
    public LockResult nextLt(final byte[] limitTKey) throws IOException {
        final Cursor c = mSource;
        LockResult result;
        byte[] limitKey = inverseTransformKey(limitTKey);
        if (limitKey == null) {
            limitKey = mTransformer.inverseTransformKeyLt(limitTKey);
            if (limitKey == null) {
                reset();
                return LockResult.UNOWNED;
            }
            while (true) {
                try {
                    result = c.nextLe(limitKey);
                } catch (LockFailureException e) {
                    throw transformCurrent(e);
                }
                result = transformCurrent(result);
                if (result != null) {
                    return result;
                }
            }
        } else {
            while (true) {
                try {
                    result = c.nextLt(limitKey);
                } catch (LockFailureException e) {
                    throw transformCurrent(e);
                }
                result = transformCurrent(result);
                if (result != null) {
                    return result;
                }
            }
        }
    }

    @Override
    public LockResult previous() throws IOException {
        final Cursor c = mSource;
        while (true) {
            LockResult result;
            try {
                result = c.previous();
            } catch (LockFailureException e) {
                throw transformCurrent(e);
            }
            result = transformCurrent(result);
            if (result != null) {
                return result;
            }
        }
    }

    @Override
    public LockResult previousGe(final byte[] limitTKey) throws IOException {
        byte[] limitKey = inverseTransformKey(limitTKey);
        if (limitKey == null) {
            limitKey = mTransformer.inverseTransformKeyGt(limitTKey);
            if (limitKey == null) {
                reset();
                return LockResult.UNOWNED;
            }
        }
        final Cursor c = mSource;
        while (true) {
            LockResult result;
            try {
                result = c.previousGe(limitKey);
            } catch (LockFailureException e) {
                throw transformCurrent(e);
            }
            result = transformCurrent(result);
            if (result != null) {
                return result;
            }
        }
    }

    @Override
    public LockResult previousGt(final byte[] limitTKey) throws IOException {
        final Cursor c = mSource;
        LockResult result;
        byte[] limitKey = inverseTransformKey(limitTKey);
        if (limitKey == null) {
            limitKey = mTransformer.inverseTransformKeyGt(limitTKey);
            if (limitKey == null) {
                reset();
                return LockResult.UNOWNED;
            }
            while (true) {
                try {
                    result = c.previousGe(limitKey);
                } catch (LockFailureException e) {
                    throw transformCurrent(e);
                }
                result = transformCurrent(result);
                if (result != null) {
                    return result;
                }
            }
        } else {
            while (true) {
                try {
                    result = c.previousGt(limitKey);
                } catch (LockFailureException e) {
                    throw transformCurrent(e);
                }
                result = transformCurrent(result);
                if (result != null) {
                    return result;
                }
            }
        }
    }

    @Override
    public LockResult find(final byte[] tkey) throws IOException {
        mKey = tkey;
        final byte[] key = inverseTransformKey(tkey);
        if (key == null) {
            mValue = null;
            mSource.reset();
            return LockResult.UNOWNED;
        }
        mValue = NOT_LOADED;
        return transformCurrent(mSource.find(key), tkey);
    }

    @Override
    public LockResult findGe(final byte[] tkey) throws IOException {
        byte[] key = inverseTransformKey(tkey);
        if (key == null) {
            key = mTransformer.inverseTransformKeyGt(tkey);
            if (key == null) {
                reset();
                return LockResult.UNOWNED;
            }
        }

        LockResult result;
        try {
            result = mSource.findGe(key);
        } catch (LockFailureException e) {
            throw transformCurrent(e);
        }

        result = transformCurrent(result);
        return result == null ? next() : result;
    }

    @Override
    public LockResult findGt(final byte[] tkey) throws IOException {
        final Cursor c = mSource;
        LockResult result;
        try {
            byte[] key = inverseTransformKey(tkey);
            if (key == null) {
                key = mTransformer.inverseTransformKeyGt(tkey);
                if (key == null) {
                    reset();
                    return LockResult.UNOWNED;
                }
                result = c.findGe(key);
            } else {
                result = c.findGt(key);
            }
        } catch (LockFailureException e) {
            throw transformCurrent(e);
        }

        result = transformCurrent(result);
        return result == null ? next() : result;
    }

    @Override
    public LockResult findLe(final byte[] tkey) throws IOException {
        byte[] key = inverseTransformKey(tkey);
        if (key == null) {
            key = mTransformer.inverseTransformKeyLt(tkey);
            if (key == null) {
                reset();
                return LockResult.UNOWNED;
            }
        }

        LockResult result;
        try {
            result = mSource.findLe(key);
        } catch (LockFailureException e) {
            throw transformCurrent(e);
        }

        result = transformCurrent(result);
        return result == null ? previous() : result;
    }

    @Override
    public LockResult findLt(final byte[] tkey) throws IOException {
        final Cursor c = mSource;
        LockResult result;
        try {
            byte[] key = inverseTransformKey(tkey);
            if (key == null) {
                key = mTransformer.inverseTransformKeyLt(tkey);
                if (key == null) {
                    reset();
                    return LockResult.UNOWNED;
                }
                result = c.findLe(key);
            } else {
                result = c.findLt(key);
            }
        } catch (LockFailureException e) {
            throw transformCurrent(e);
        }

        result = transformCurrent(result);
        return result == null ? previous() : result;
    }

    @Override
    public LockResult findNearby(final byte[] tkey) throws IOException {
        mKey = tkey;
        final byte[] key = inverseTransformKey(tkey);
        if (key == null) {
            mValue = null;
            mSource.reset();
            return LockResult.UNOWNED;
        }
        mValue = NOT_LOADED;
        return transformCurrent(mSource.findNearby(key), tkey);
    }

    @Override
    public LockResult random(final byte[] lowTKey, final byte[] highTKey) throws IOException {
        byte[] lowKey = null;
        if (lowTKey != null) {
            lowKey = mTransformer.inverseTransformKey(lowTKey);
            if (lowKey == null) {
                lowKey = mTransformer.inverseTransformKeyGt(lowTKey);
                if (lowKey == null) {
                    reset();
                    return LockResult.UNOWNED;
                }
            }
        }

        byte[] highKey = null;
        if (highTKey != null) {
            highKey = mTransformer.inverseTransformKey(highTKey);
            if (highKey == null) {
                highKey = mTransformer.inverseTransformKeyLt(highTKey);
                if (highKey == null) {
                    reset();
                    return LockResult.UNOWNED;
                }
            }
        }

        LockResult result;
        try {
            result = mSource.random(lowKey, highKey);
        } catch (LockFailureException e) {
            throw transformCurrent(e);
        }

        result = transformCurrent(result);

        if (result == null) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                result = next();
                if (mKey == null) {
                    // Reached the end, so wrap around.
                    result = first();
                }
            } else {
                result = previous();
                if (mKey == null) {
                    // Reached the end, so wrap around.
                    result = last();
                }
            }
        }

        return result;
    }

    @Override
    public LockResult lock() throws IOException {
        if (mKey != null && mSource.key() == null) {
            throw TransformedView.fail();
        }
        return mSource.lock();
    }

    @Override
    public LockResult load() throws IOException {
        final byte[] tkey = mKey;
        ViewUtils.positionCheck(tkey);
        if (mSource.key() == null) {
            throw TransformedView.fail();
        }
        mValue = NOT_LOADED;
        return transformCurrent(mSource.load(), tkey);
    }

    @Override
    public void store(final byte[] tvalue) throws IOException {
        final byte[] tkey = mKey;
        ViewUtils.positionCheck(tkey);
        final Cursor c = mSource;
        final byte[] key = c.key();
        if (key == null) {
            throw TransformedView.fail();
        }
        c.store(mTransformer.inverseTransformValue(tvalue, key, tkey));
        mValue = tvalue;
    }

    @Override
    public void commit(final byte[] tvalue) throws IOException {
        final byte[] tkey = mKey;
        ViewUtils.positionCheck(tkey);
        final Cursor c = mSource;
        final byte[] key = c.key();
        if (key == null) {
            throw TransformedView.fail();
        }
        c.commit(mTransformer.inverseTransformValue(tvalue, key, tkey));
        mValue = tvalue;
    }

    @Override
    public Cursor copy() {
        TransformedCursor copy = new TransformedCursor(mSource.copy(), mTransformer);
        copy.mKey = Utils.cloneArray(mKey);
        copy.mValue = ViewUtils.copyValue(mValue);
        return copy;
    }

    @Override
    public void reset() {
        mKey = null;
        mValue = null;
        mSource.reset();
    }

    @Override
    public void close() {
        reset();
    }

    @Override
    public long valueLength() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void valueLength(long length) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int doValueRead(long pos, byte[] buf, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void doValueWrite(long pos, byte[] buf, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void doValueClear(long pos, long length) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int valueStreamBufferSize(int bufferSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void valueCheckOpen() {
        throw new UnsupportedOperationException();
    }

    private byte[] inverseTransformKey(final byte[] tkey) {
        Utils.keyCheck(tkey);
        return mTransformer.inverseTransformKey(tkey);
    }

    private LockFailureException transformCurrent(LockFailureException e) throws IOException {
        mValue = NOT_LOADED;
        try {
            mKey = mTransformer.transformKey(mSource);
        } catch (Throwable e2) {
            reset();
            throw e2;
        }
        return e;
    }

    private LockResult transformCurrent(LockResult result) throws IOException {
        final Cursor c = mSource;

        final byte[] key = c.key();
        if (key == null) {
            mKey = null;
            mValue = null;
            return LockResult.UNOWNED;
        }

        byte[] tkey = mTransformer.transformKey(c);
        mKey = tkey;

        if (c.value() == null) {
            mValue = null;
            if (tkey != null) {
                return result;
            }
        } else {
            if (tkey != null) {
                byte[] tvalue = mTransformer.transformValue(c, tkey);
                if (tvalue != null) {
                    mValue = tvalue;
                    return result;
                }
            }
            mValue = null;
        }
        if (result == LockResult.ACQUIRED) {
            c.link().unlock();
        }
        return null;
    }

    private LockResult transformCurrent(LockResult result, final byte[] tkey) throws IOException {
        final Cursor c = mSource;

        if (c.value() == null) {
            mValue = null;
            return result;
        }

        byte[] tvalue = mTransformer.transformValue(c, tkey);
        mValue = tvalue;

        if (tvalue == null && result == LockResult.ACQUIRED) {
            c.link().unlock();
            result = LockResult.UNOWNED;
        }

        return result;
    }

    public Cursor source() {
        return mSource;
    }
}
