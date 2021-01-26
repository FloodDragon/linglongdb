package com.glodon.lingling.base.exception;

/**
 * @author Stereo
 */
public class LargeKeyException extends DatabaseException {
    private static final long serialVersionUID = 1L;

    private final long mLength;

    public LargeKeyException(long length) {
        super(createMessage(length));
        mLength = length;
    }

    public LargeKeyException(long length, Throwable cause) {
        super(createMessage(length), cause);
        mLength = length;
    }

    public long getLength() {
        return mLength;
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }

    private static String createMessage(long length) {
        return "Key is too large: " + Long.toUnsignedString(length);
    }
}
