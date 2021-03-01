package com.linglong.engine.core.updater;

import com.linglong.base.common.EntryFunction;
import com.linglong.engine.core.frame.Scanner;
import com.linglong.engine.core.frame.ViewUtils;

import java.io.Flushable;
import java.io.IOException;

/**
 * @author Stereo
 */
public interface Updater extends Scanner, Flushable {

    boolean update(byte[] value) throws IOException;

    default void updateAll(EntryFunction action) throws IOException {
        while (true) {
            byte[] key = key();
            if (key == null) {
                return;
            }
            byte[] value;
            try {
                value = action.apply(key, value());
            } catch (Throwable e) {
                throw ViewUtils.fail(this, e);
            }
            update(value);
        }
    }

    @Override
    default void flush() throws IOException {
    }
}
