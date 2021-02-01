package com.glodon.linglong.engine.core.updater;

import com.glodon.linglong.base.exception.UnpositionedCursorException;
import com.glodon.linglong.engine.core.frame.Cursor;
import com.glodon.linglong.engine.core.CursorScanner;
import com.glodon.linglong.engine.core.lock.LockResult;
import com.glodon.linglong.engine.core.tx.Transaction;
import com.glodon.linglong.engine.core.frame.ViewUtils;

import java.io.IOException;

/**
 * @author Stereo
 */
public class CursorNonRepeatableUpdater extends CursorScanner implements Updater {
    private LockResult mLockResult;

    public CursorNonRepeatableUpdater(Cursor cursor) throws IOException {
        super(cursor);
        mLockResult = cursor.first();
        cursor.register();
    }

    @Override
    public boolean step() throws IOException {
        LockResult result = mLockResult;
        if (result == null) {
            return false;
        }

        Cursor c = mCursor;

        tryStep:
        {
            if (result.isAcquired()) {
                c.link().unlock();
            }
            try {
                result = c.next();
            } catch (UnpositionedCursorException e) {
                break tryStep;
            } catch (Throwable e) {
                throw ViewUtils.fail(this, e);
            }
            if (c.key() != null) {
                mLockResult = result;
                return true;
            }
        }

        mLockResult = null;
        finished();

        return false;
    }

    @Override
    public boolean step(long amount) throws IOException {
        if (amount < 0) {
            throw new IllegalArgumentException();
        }

        LockResult result = mLockResult;
        if (result == null) {
            return false;
        }

        Cursor c = mCursor;

        tryStep:
        {
            if (amount > 0) {
                if (result.isAcquired()) {
                    c.link().unlock();
                }
                try {
                    result = c.skip(amount);
                } catch (UnpositionedCursorException e) {
                    break tryStep;
                } catch (Throwable e) {
                    throw ViewUtils.fail(this, e);
                }
            }
            if (c.key() != null) {
                mLockResult = result;
                return true;
            }
        }

        mLockResult = null;
        finished();

        return false;
    }

    @Override
    public boolean update(byte[] value) throws IOException {
        Cursor c = mCursor;

        try {
            c.store(value);
        } catch (UnpositionedCursorException e) {
            close();
            return false;
        } catch (Throwable e) {
            throw ViewUtils.fail(this, e);
        }

        postUpdate();

        LockResult result;
        tryStep:
        {
            try {
                result = c.next();
            } catch (UnpositionedCursorException e) {
                break tryStep;
            } catch (Throwable e) {
                throw ViewUtils.fail(this, e);
            }
            if (c.key() != null) {
                mLockResult = result;
                return true;
            }
        }

        mLockResult = null;
        finished();

        return false;
    }

    @Override
    public void close() throws IOException {
        mCursor.reset();
        if (mLockResult != null) {
            mLockResult = null;
            finished();
        }
    }

    protected void postUpdate() throws IOException {
    }

    protected void finished() throws IOException {
        Transaction txn = mCursor.link();
        txn.commit();
        txn.exit();
    }
}
