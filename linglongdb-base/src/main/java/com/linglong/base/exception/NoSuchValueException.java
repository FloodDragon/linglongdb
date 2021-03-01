package com.linglong.base.exception;

/**
 * @author Stereo
 */
public class NoSuchValueException extends DatabaseException {
    private static final long serialVersionUID = 1L;

    public NoSuchValueException() {
        super();
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }
}
