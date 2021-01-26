package com.glodon.linglong.base.exception;

/**
 * @author Stereo
 */
public class UnmodifiableReplicaException extends DatabaseException {
    private static final long serialVersionUID = 1L;

    public UnmodifiableReplicaException() {
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }
}

