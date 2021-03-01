package com.linglong.base.common;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Stereo
 */
@FunctionalInterface
public interface EntryFunction {
    byte[] apply(byte[] key, byte[] value) throws IOException;

    default EntryFunction andThen(EntryFunction after) {
        Objects.requireNonNull(after);

        return (key, value) -> {
            return after.apply(key, apply(key, value));
        };
    }
}
