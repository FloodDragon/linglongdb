package com.glodon.linglong.engine.core.view;

import com.glodon.linglong.engine.core.frame.Cursor;

/**
 * @author Stereo
 */
public final class UnmodifiableCursor extends WrappedCursor<Cursor> {
    public UnmodifiableCursor(Cursor source) {
        super(source);
    }

    @Override
    public Cursor copy() {
        return new UnmodifiableCursor(source.copy());
    }
}
