package com.linglong.engine.core.updater;

import com.linglong.engine.core.frame.Cursor;

import java.io.IOException;

/**
 * @author Stereo
 */
public class CursorAutoCommitUpdater extends CursorNonRepeatableUpdater {

    public CursorAutoCommitUpdater(Cursor cursor) throws IOException {
        super(cursor);
    }

    @Override
    protected void postUpdate() throws IOException {
        mCursor.link().commit();
    }

    @Override
    protected void finished() throws IOException {
        mCursor.link().exit();
    }
}
