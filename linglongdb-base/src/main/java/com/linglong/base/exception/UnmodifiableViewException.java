package com.linglong.base.exception;

/**
 * @author Stereo
 */
public class UnmodifiableViewException extends ViewConstraintException {
    private static final long serialVersionUID = 2L;

    public UnmodifiableViewException() {
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }
}
