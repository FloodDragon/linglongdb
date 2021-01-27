package com.glodon.linglong.engine.lock;

import com.glodon.linglong.base.exception.LockFailureException;
import com.glodon.linglong.base.common.Utils;

import java.util.concurrent.TimeUnit;

/**
 * @author Stereo
 */
public class LockTimeoutException extends LockFailureException {
    private static final long serialVersionUID = 1L;

    private final long mNanosTimeout;
    private final Object mOwnerAttachment;
    private TimeUnit mUnit;

    public LockTimeoutException(long nanosTimeout) {
        this(nanosTimeout, null);
    }

    public LockTimeoutException(long nanosTimeout, Object attachment) {
        super((String) null);
        mNanosTimeout = nanosTimeout;
        mOwnerAttachment = attachment;
    }

    @Override
    public String getMessage() {
        return Utils.timeoutMessage(mNanosTimeout, this);
    }

    @Override
    public long getTimeout() {
        return getUnit().convert(mNanosTimeout, TimeUnit.NANOSECONDS);
    }

    @Override
    public Object getOwnerAttachment() {
        return mOwnerAttachment;
    }

    @Override
    public TimeUnit getUnit() {
        TimeUnit unit = mUnit;
        if (unit != null) {
            return unit;
        }
        return mUnit = Utils.inferUnit(TimeUnit.NANOSECONDS, mNanosTimeout);
    }
}
