package com.linglong.engine.core.view;

import com.linglong.base.exception.NoSuchValueException;
import com.linglong.base.exception.ViewConstraintException;
import com.linglong.engine.core.frame.Cursor;
import com.linglong.engine.core.lock.LockResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Stereo
 */
public final class KeyOnlyCursor extends WrappedCursor<Cursor> {
    KeyOnlyCursor(Cursor source) {
        super(source);
        source.autoload(false);
    }

    @Override
    public long valueLength() throws IOException {
        return source.valueLength();
    }

    @Override
    public void valueLength(long length) throws IOException {
        if (length >= 0) {
            throw new ViewConstraintException();
        }
        source.store(null);
    }

    @Override
    public int valueRead(long pos, byte[] buf, int off, int len) throws IOException {
        return source.valueRead(pos, buf, off, 0);
    }

    @Override
    public InputStream newValueInputStream(long pos) throws IOException {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                if (value() == null) {
                    throw new NoSuchValueException();
                }
                return -1;
            }
        };
    }

    @Override
    public InputStream newValueInputStream(long pos, int bufferSize) throws IOException {
        return newValueInputStream(pos);
    }

    @Override
    public OutputStream newValueOutputStream(long pos) throws IOException {
        throw new ViewConstraintException();
    }

    @Override
    public OutputStream newValueOutputStream(long pos, int bufferSize) throws IOException {
        return newValueOutputStream(pos);
    }

    @Override
    public byte[] value() {
        return KeyOnlyView.valueScrub(source.value());
    }

    @Override
    public boolean autoload(boolean mode) {
        return false;
    }

    @Override
    public boolean autoload() {
        return false;
    }

    @Override
    public LockResult load() throws IOException {
        return source.lock();
    }

    @Override
    public void store(byte[] value) throws IOException {
        KeyOnlyView.valueCheck(value);
        source.store(null);
    }

    @Override
    public void commit(byte[] value) throws IOException {
        KeyOnlyView.valueCheck(value);
        source.commit(null);
    }

    @Override
    public Cursor copy() {
        return new KeyOnlyCursor(source.copy());
    }
}
