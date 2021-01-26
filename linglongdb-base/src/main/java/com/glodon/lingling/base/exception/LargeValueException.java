package com.glodon.lingling.base.exception;

/**
 * @author Stereo
 */
public class LargeValueException extends DatabaseException {
    private static final long serialVersionUID = 1L;

    private final long mLength;

    public LargeValueException(long length) {
        super(createMessage(length));
        mLength = length;
    }

    public LargeValueException(long length, Throwable cause) {
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
        return "Value is too large: " + Long.toUnsignedString(length);
    }
}
