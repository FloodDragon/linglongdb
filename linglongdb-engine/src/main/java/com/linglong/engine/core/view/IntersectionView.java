package com.linglong.engine.core.view;

import com.linglong.engine.core.frame.Combiner;
import com.linglong.engine.core.frame.Cursor;
import com.linglong.engine.core.frame.View;
import com.linglong.engine.core.tx.Transaction;

import java.io.IOException;

/**
 * @author Stereo
 */
public final class IntersectionView extends MergeView {
    public IntersectionView(Combiner combiner, View first, View second) {
        super(combiner, first, second);
    }

    @Override
    protected byte[] doLoad(Transaction txn, byte[] key) throws IOException {
        return mCombiner.loadIntersection(txn, key, mFirst, mSecond);
    }

    @Override
    protected MergeCursor newCursor(Transaction txn, MergeView view,
                                    Cursor first, Cursor second) {
        return new IntersectionCursor(txn, view, first, second);
    }

    @Override
    protected String type() {
        return "intersection";
    }
}
