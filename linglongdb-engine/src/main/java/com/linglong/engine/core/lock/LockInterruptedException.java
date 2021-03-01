package com.linglong.engine.core.lock;

import com.linglong.base.exception.LockFailureException;

/**
 * @author Stereo
 */
public class LockInterruptedException extends LockFailureException {
    private static final long serialVersionUID = 1L;

    public LockInterruptedException() {
        super();
    }
}
