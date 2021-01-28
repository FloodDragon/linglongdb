package com.glodon.linglong.engine.core;

/**
 * @author Stereo
 */
final class UnmodifiableCursor extends WrappedCursor<Cursor> {
    UnmodifiableCursor(Cursor source) {
        super(source);
    }

    @Override
    public Cursor copy() {
        return new UnmodifiableCursor(source.copy());
    }
}
