package com.glodon.linglong.base.exception;

/**
 * @author Stereo
 */
public class UnpositionedCursorException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public UnpositionedCursorException() {
    }

    public UnpositionedCursorException(String message) {
        super(message);
    }

    public UnpositionedCursorException(Throwable cause) {
        super(cause);
    }

    public UnpositionedCursorException(String message, Throwable cause) {
        super(message, cause);
    }
}
