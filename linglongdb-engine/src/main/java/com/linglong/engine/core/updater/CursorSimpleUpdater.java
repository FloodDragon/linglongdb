package com.linglong.engine.core.updater;

import com.linglong.base.exception.UnpositionedCursorException;
import com.linglong.engine.core.CursorScanner;
import com.linglong.engine.core.frame.Cursor;
import com.linglong.engine.core.frame.ViewUtils;

import java.io.IOException;

/**
 * @author Stereo
 */
public class CursorSimpleUpdater extends CursorScanner implements Updater {

    public CursorSimpleUpdater(Cursor cursor) throws IOException {
        super(cursor);
        cursor.first();
        cursor.register();
    }

    @Override
    public boolean update(byte[] value) throws IOException {
        try {
            mCursor.store(value);
        } catch (UnpositionedCursorException e) {
            return false;
        } catch (Throwable e) {
            throw ViewUtils.fail(this, e);
        }
        return step();
    }
}
