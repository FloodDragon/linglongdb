package com.glodon.linglong.engine.core.page;

import com.glodon.linglong.base.exception.DatabaseException;

/**
 * Used by DurablePageDb.
 *
 * @author Stereo
 */
class WrongPageSize extends Exception {
    private static final long serialVersionUID = 1L;

    final int mExpected;
    final int mActual;

    WrongPageSize(int expected, int actual) {
        mExpected = expected;
        mActual = actual;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    DatabaseException rethrow() throws DatabaseException {
        throw new DatabaseException
            ("Actual page size does not match configured page size: "
             + mActual + " != " + mExpected);
    }
}
