package com.glodon.linglong.base.exception;

/**
 * Created by liuj-ai on 2021/1/26.
 */
public class CacheExhaustedException extends DatabaseException {
    private static final long serialVersionUID = 1L;

    public CacheExhaustedException() {
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }
}
