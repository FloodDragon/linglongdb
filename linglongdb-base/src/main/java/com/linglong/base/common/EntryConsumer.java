package com.linglong.base.common;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Stereo
 */
@FunctionalInterface
public interface EntryConsumer {
    void accept(byte[] key, byte[] value) throws IOException;

    default EntryConsumer andThen(EntryConsumer after) {
        Objects.requireNonNull(after);

        return (key, value) -> {
            accept(key, value);
            after.accept(key, value);
        };
    }
}
