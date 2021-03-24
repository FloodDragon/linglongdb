package com.linglong.server.database.process;

import com.linglong.engine.core.frame.Filter;

import java.io.IOException;

/**
 * Created by liuj-ai on 2021/3/24.
 */
public interface KeyValueEvictFilter extends Filter {

    default boolean isAllowed(byte[] key, byte[] value) throws IOException {
        return doFilter(key, value);
    }

    boolean doFilter(byte[] key, byte[] value) throws IOException;
}
