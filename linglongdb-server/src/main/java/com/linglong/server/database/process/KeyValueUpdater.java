package com.linglong.server.database.process;

import com.linglong.base.common.EntryFunction;

import java.io.IOException;

/**
 * Created by liuj-ai on 2021/3/24.
 */
public interface KeyValueUpdater extends EntryFunction {
    default byte[] apply(byte[] key, byte[] value) throws IOException {
        return doUpdate(key, value);
    }

    byte[] doUpdate(byte[] key, byte[] value) throws IOException;
}
