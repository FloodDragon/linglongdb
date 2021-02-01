package com.glodon.linglong.engine.core.updater;

import com.glodon.linglong.base.exception.UnpositionedCursorException;
import com.glodon.linglong.engine.core.CursorScanner;
import com.glodon.linglong.engine.core.frame.Cursor;
import com.glodon.linglong.engine.core.frame.ViewUtils;
import com.glodon.linglong.engine.core.lock.LockMode;
import com.glodon.linglong.engine.core.tx.Transaction;

import java.io.IOException;

/**
 * @author Stereo
 */
public class CursorUpgradableUpdater extends CursorScanner implements Updater {
    private LockMode mOriginalMode;

    public CursorUpgradableUpdater(Cursor cursor) throws IOException {
        super(cursor);
        Transaction txn = cursor.link();
        mOriginalMode = txn.lockMode();
        txn.lockMode(LockMode.UPGRADABLE_READ);
        cursor.first();
        cursor.register();
    }

    @Override
    public boolean step() throws IOException {
        tryStep:
        {
            Cursor c = mCursor;
            try {
                c.next();
            } catch (UnpositionedCursorException e) {
                break tryStep;
            } catch (Throwable e) {
                throw ViewUtils.fail(this, e);
            }
            if (c.key() != null) {
                return true;
            }
        }
        resetTxnMode();
        return false;
    }

    @Override
    public boolean step(long amount) throws IOException {
        if (amount < 0) {
            throw new IllegalArgumentException();
        }
        tryStep:
        {
            Cursor c = mCursor;
            if (amount > 0) {
                try {
                    c.skip(amount);
                } catch (UnpositionedCursorException e) {
                    break tryStep;
                } catch (Throwable e) {
                    throw ViewUtils.fail(this, e);
                }
            }
            if (c.key() != null) {
                return true;
            }
        }
        resetTxnMode();
        return false;
    }

    @Override
    public boolean update(byte[] value) throws IOException {
        try {
            mCursor.store(value);
        } catch (UnpositionedCursorException e) {
            resetTxnMode();
            return false;
        } catch (Throwable e) {
            throw ViewUtils.fail(this, e);
        }
        return step();
    }

    @Override
    public void close() throws IOException {
        resetTxnMode();
        mCursor.reset();
    }

    private void resetTxnMode() {
        LockMode original = mOriginalMode;
        if (original != null) {
            mOriginalMode = null;
            mCursor.link().lockMode(original);
        }
    }
}
