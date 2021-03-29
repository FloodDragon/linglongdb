package com.linglong.server.database.process;


/**
 * Created by liuj-ai on 2021/3/26.
 */
public interface ProcessEntryFunction<R> {

    R apply(byte[] key, byte[] value) throws Exception;

}
