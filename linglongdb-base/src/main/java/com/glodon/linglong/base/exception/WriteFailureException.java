package com.glodon.linglong.base.exception;

/**
 * @author Stereo
 */
public class WriteFailureException extends DatabaseFullException {
    private static final long serialVersionUID = 1L;

    public WriteFailureException() {
    }

    public WriteFailureException(Throwable cause) {
        super(cause);
    }
}
