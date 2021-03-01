package com.linglong.engine.core.view;

import com.linglong.base.common.Utils;
import com.linglong.engine.core.frame.Cursor;
import com.linglong.engine.core.frame.View;
import com.linglong.engine.core.tx.Transaction;
import com.linglong.engine.core.frame.ViewUtils;

import java.io.IOException;

/**
 * @author Stereo
 */
public final class BoundedView extends SubView {
    public static View viewGe(View view, byte[] key) {
        Utils.keyCheck(key);
        return new BoundedView(view, key, null, 0);
    }

    public static View viewGt(View view, byte[] key) {
        Utils.keyCheck(key);
        return new BoundedView(view, key, null, START_EXCLUSIVE);
    }

    public static View viewLe(View view, byte[] key) {
        Utils.keyCheck(key);
        return new BoundedView(view, null, key, 0);
    }

    public static View viewLt(View view, byte[] key) {
        Utils.keyCheck(key);
        return new BoundedView(view, null, key, END_EXCLUSIVE);
    }

    public static View viewPrefix(View view, byte[] prefix, int trim) {
        prefixCheck(prefix, trim);

        byte[] end = prefix.clone();
        int mode;
        if (Utils.increment(end, 0, end.length)) {
            mode = END_EXCLUSIVE;
        } else {
            end = null;
            mode = 0;
        }

        view = new BoundedView(view, prefix, end, mode);

        if (trim > 0) {
            view = new TrimmedView(view, prefix, trim);
        }

        return view;
    }

    static final int START_EXCLUSIVE = 0xfffffffe, END_EXCLUSIVE = 1;

    final byte[] mStart;
    final byte[] mEnd;
    final int mMode;

    BoundedView(View source, byte[] start, byte[] end, int mode) {
        super(source);
        mStart = start;
        mEnd = end;
        mMode = mode;
    }

    @Override
    public Cursor newCursor(Transaction txn) {
        return new BoundedCursor(this, mSource.newCursor(txn));
    }

    @Override
    public long count(byte[] lowKey, byte[] highKey) throws IOException {
        return mSource.count(adjustLowKey(lowKey), adjustHighKey(highKey));
    }

    @Override
    public View viewGe(byte[] key) {
        Utils.keyCheck(key);
        if (startRangeCompare(key) <= 0) {
            return this;
        }
        return new BoundedView(mSource, key, mEnd, mMode & ~START_EXCLUSIVE);
    }

    @Override
    public View viewGt(byte[] key) {
        Utils.keyCheck(key);
        if (startRangeCompare(key) < 0) {
            return this;
        }
        return new BoundedView(mSource, key, mEnd, mMode | START_EXCLUSIVE);
    }

    @Override
    public View viewLe(byte[] key) {
        Utils.keyCheck(key);
        if (endRangeCompare(key) >= 0) {
            return this;
        }
        return new BoundedView(mSource, mStart, key, mMode & ~END_EXCLUSIVE);
    }

    @Override
    public View viewLt(byte[] key) {
        Utils.keyCheck(key);
        if (endRangeCompare(key) > 0) {
            return this;
        }
        return new BoundedView(mSource, mStart, key, mMode | END_EXCLUSIVE);
    }

    @Override
    public View viewPrefix(byte[] prefix, int trim) {
        prefixCheck(prefix, trim);

        View view = viewGe(prefix);

        byte[] end = prefix.clone();
        if (Utils.increment(end, 0, end.length)) {
            view = view.viewLt(end);
        }

        if (trim > 0) {
            view = new TrimmedView(view, prefix, trim);
        }

        return view;
    }

    @Override
    boolean inRange(byte[] key) {
        return startRangeCompare(key) >= 0 && endRangeCompare(key) <= 0;
    }

    int startRangeCompare(byte[] key) {
        byte[] start = mStart;
        return start == null ? 1 : startRangeCompare(start, key);
    }

    int startRangeCompare(byte[] start, byte[] key) {
        int result = Utils.compareUnsigned(key, 0, key.length, start, 0, start.length);
        return result != 0 ? result : (mMode & START_EXCLUSIVE);
    }

    int endRangeCompare(byte[] key) {
        byte[] end = mEnd;
        return end == null ? -1 : endRangeCompare(end, key);
    }

    int endRangeCompare(byte[] end, byte[] key) {
        int result = Utils.compareUnsigned(key, 0, key.length, end, 0, end.length);
        return result != 0 ? result : (mMode & END_EXCLUSIVE);
    }

    byte[] adjustLowKey(byte[] lowKey) {
        byte[] start = mStart;
        if (start != null && (lowKey == null || startRangeCompare(start, lowKey) < 0)) {
            lowKey = start;
            if ((mMode & START_EXCLUSIVE) != 0) {
                lowKey = ViewUtils.appendZero(lowKey);
            }
        }
        return lowKey;
    }

    byte[] adjustHighKey(byte[] highKey) {
        byte[] end = mEnd;
        if (end != null && (highKey == null || endRangeCompare(end, highKey) > 0)) {
            highKey = end;
            if ((mMode & END_EXCLUSIVE) == 0) {
                highKey = ViewUtils.appendZero(highKey);
            }
        }
        return highKey;
    }
}
