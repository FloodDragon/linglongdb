package com.glodon.linglong.base.exception;

import java.util.concurrent.TimeUnit;

import com.glodon.linglong.base.util.Utils;

/**
 * Created by liuj-ai on 2021/1/26.
 */
public class ConfirmationTimeoutException extends ConfirmationFailureException {
    private static final long serialVersionUID = 1L;

    private final long mNanosTimeout;

    private TimeUnit mUnit;

    /**
     * @param nanosTimeout negative is interpreted as infinite wait
     */
    public ConfirmationTimeoutException(long nanosTimeout) {
        super((String) null);
        mNanosTimeout = nanosTimeout;
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
    public TimeUnit getUnit() {
        TimeUnit unit = mUnit;
        if (unit != null) {
            return unit;
        }
        return mUnit = Utils.inferUnit(TimeUnit.NANOSECONDS, mNanosTimeout);
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }
}
