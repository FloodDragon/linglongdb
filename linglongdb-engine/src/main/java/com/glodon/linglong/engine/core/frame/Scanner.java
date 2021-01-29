package com.glodon.linglong.engine.core.frame;

import com.glodon.linglong.base.common.EntryConsumer;
import com.glodon.linglong.engine.core.frame.Cursor;
import com.glodon.linglong.engine.core.view.ViewUtils;

import java.io.IOException;
import java.util.Comparator;

/**
 * @author Stereo
 */
public interface Scanner extends AutoCloseable {
    Comparator<byte[]> getComparator();

    byte[] key();

    byte[] value();

    boolean step() throws IOException;

    default boolean step(long amount) throws IOException {
        if (amount > 0) while (true) {
            boolean result = step();
            if (!result || --amount <= 0) {
                return result;
            }
        }
        if (amount == 0) {
            return key() != null;
        }
        throw ViewUtils.fail(this, new IllegalArgumentException());
    }

    default void scanAll(EntryConsumer action) throws IOException {
        while (true) {
            byte[] key = key();
            if (key == null) {
                return;
            }
            try {
                action.accept(key, value());
            } catch (Throwable e) {
                throw ViewUtils.fail(this, e);
            }
            step();
        }
    }

    @Override
    void close() throws IOException;
}
