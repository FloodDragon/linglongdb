package com.glodon.linglong.base.exception;

/** 
 * @author Stereo
 */
public class ViewConstraintException extends DatabaseException {
    private static final long serialVersionUID = 1L;

    public ViewConstraintException() {
    }

    public ViewConstraintException(String message) {
        super(message);
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }
}
