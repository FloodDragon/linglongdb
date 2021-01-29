package com.glodon.linglong.engine.core;

import com.glodon.linglong.base.exception.UnpositionedCursorException;

import java.io.IOException;
import java.util.Comparator;

/**
 * @author Stereo
 */
public class CursorScanner implements Scanner {
    protected final Cursor mCursor;

    public CursorScanner(Cursor cursor) throws IOException {
        mCursor = cursor;
    }

    @Override
    public Comparator<byte[]> getComparator() {
        return mCursor.getComparator();
    }

    @Override
    public byte[] key() {
        return mCursor.key();
    }

    @Override
    public byte[] value() {
        return mCursor.value();
    }

    @Override
    public boolean step() throws IOException {
        Cursor c = mCursor;
        try {
            c.next();
            return c.key() != null;
        } catch (UnpositionedCursorException e) {
            return false;
        } catch (Throwable e) {
            throw ViewUtils.fail(this, e);
        }
    }

    @Override
    public boolean step(long amount) throws IOException {
        Cursor c = mCursor;
        if (amount > 0) {
            try {
                c.skip(amount);
            } catch (UnpositionedCursorException e) {
                return false;
            } catch (Throwable e) {
                throw ViewUtils.fail(this, e);
            }
        } else if (amount < 0) {
            throw new IllegalArgumentException();
        }
        return c.key() != null;
    }

    @Override
    public void close() throws IOException {
        mCursor.reset();
    }
}
