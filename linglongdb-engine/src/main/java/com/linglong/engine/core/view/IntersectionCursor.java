package com.linglong.engine.core.view;

import com.linglong.engine.core.frame.Cursor;
import com.linglong.engine.core.lock.LockResult;
import com.linglong.engine.core.tx.Transaction;

import java.io.IOException;

/**
 * @author Stereo
 */
public final class IntersectionCursor extends MergeCursor {
    public IntersectionCursor(Transaction txn, MergeView view, Cursor first, Cursor second) {
        super(txn, view, first, second);
    }

    @Override
    protected MergeCursor newCursor(Cursor first, Cursor second) {
        return new IntersectionCursor(mTxn, mView, first, second);
    }

    @Override
    protected LockResult select(Transaction txn) throws IOException {
        while (true) {
            final byte[] k1 = mFirst.key();
            if (k1 == null) {
                reset();
                return LockResult.UNOWNED;
            }
            final byte[] k2 = mSecond.key();
            if (k2 == null) {
                reset();
                return LockResult.UNOWNED;
            }
            final int cmp = getComparator().compare(k1, k2);
            if (cmp == 0) {
                mCompare = cmp;
                return selectCombine(txn, k1);
            } else if (mDirection == DIRECTION_FORWARD) {
                if (cmp < 0) {
                    mFirst.findNearbyGe(k2);
                } else {
                    mSecond.findNearbyGe(k1);
                }
            } else {
                if (cmp > 0) {
                    mFirst.findNearbyLe(k2);
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
                    if (first != null && second != null) {
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
