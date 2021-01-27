package com.glodon.linglong.engine;

import com.glodon.linglong.base.exception.ViewConstraintException;

import java.io.IOException;

/**
 * @author Stereo
 */
@FunctionalInterface
public interface Filter extends Transformer {
    boolean isAllowed(byte[] key, byte[] value) throws IOException;

    @Override
    default byte[] transformValue(byte[] value, byte[] key, byte[] tkey)
            throws IOException {
        return isAllowed(key, value) ? value : null;
    }

    @Override
    default byte[] inverseTransformValue(byte[] tvalue, byte[] key, byte[] tkey)
            throws IOException, ViewConstraintException {
        if (!isAllowed(key, tvalue)) {
            throw new ViewConstraintException("Filtered out");
        }
        return tvalue;
    }
}
