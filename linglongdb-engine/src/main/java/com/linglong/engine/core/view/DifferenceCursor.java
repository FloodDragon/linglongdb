package com.linglong.engine.core.view;

import com.linglong.engine.core.frame.Cursor;
import com.linglong.engine.core.lock.LockResult;
import com.linglong.engine.core.tx.Transaction;

import java.io.IOException;

/**
 * @author Stereo
 */
public final class DifferenceCursor extends MergeCursor {
    public DifferenceCursor(Transaction txn, MergeView view, Cursor first, Cursor second) {
        super(txn, view, first, second);
    }

    @Override
    protected MergeCursor newCursor(Cursor first, Cursor second) {
        return new DifferenceCursor(mTxn, mView, first, second);
    }

    @Override
    protected LockResult select(Transaction txn) throws IOException {
        final byte[] k1 = mFirst.key();
        if (k1 == null) {
            reset();
            return LockResult.UNOWNED;
        }

        while (true) {
            final byte[] k2 = mSecond.key();
            if (k2 == null) {
                mCompare = -2 ^ mDirection; // is 1 when reversed
                return selectFirst(txn, k1);
            } else {
                final int cmp = getComparator().compare(k1, k2);
                if (cmp == 0) {
                    mCompare = 0;
                    return selectCombine(txn, k1);
                } else if ((cmp ^ mDirection) < 0) {
                    mCompare = cmp;
                    return selectFirst(txn, k1);
                } else if (mDirection == DIRECTION_FORWARD) {
                    mSecond.findNearbyGe(k1);
                } else {
                    mSecond.findNearbyLe(k1);
                }
            }
        }
    }

    @Override
    protected void doStore(byte[] key, byte[] value) throws IOException {
        if (value == null) {
            mFirst.store(null);
        } else {
            byte[][] values = mView.mCombiner.separate(key, value);

            byte[] first, second;
            check:
            {
                if (values != null) {
                    first = values[0];
                    second = values[1];
                    if (first != null) {
                        break check;
                    }
                }
                throw storeFail();
            }

            mFirst.store(first);
            mSecond.store(second);
        }
    }
}
