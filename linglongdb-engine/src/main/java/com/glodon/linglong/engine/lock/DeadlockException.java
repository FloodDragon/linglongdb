package com.glodon.linglong.engine.lock;

/**
 * @author Stereo
 */
public class DeadlockException extends LockTimeoutException {
    private static final long serialVersionUID = 1L;

    private final boolean mGuilty;
    private final DeadlockSet mSet;

    public DeadlockException(long nanosTimeout, Object attachment, boolean guilty, DeadlockSet set) {
        super(nanosTimeout, attachment);
        mGuilty = guilty;
        mSet = set;
    }

    public boolean isGuilty() {
        return mGuilty;
    }

    public DeadlockSet getDeadlockSet() {
        return mSet;
    }

    @Override
    public String getMessage() {
        return getMessage(true);
    }

    public String getShortMessage() {
        return getMessage(false);
    }

    private String getMessage(boolean full) {
        StringBuilder b = new StringBuilder(super.getMessage());
        b.append("; caller ");
        if (mGuilty) {
            b.append("helped cause the deadlock");
        } else {
            b.append("might be innocent");
        }
        b.append('.');
        if (full) {
            b.append(" Deadlock set: ");
            mSet.appendMembers(b);
        }
        return b.toString();
    }
}
