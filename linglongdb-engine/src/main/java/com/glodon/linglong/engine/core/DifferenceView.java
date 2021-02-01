package com.glodon.linglong.engine.core;

import com.glodon.linglong.engine.core.frame.Combiner;
import com.glodon.linglong.engine.core.frame.Cursor;
import com.glodon.linglong.engine.core.frame.View;
import com.glodon.linglong.engine.core.tx.Transaction;

import java.io.IOException;

/**
 * @author Stereo
 */
public final class DifferenceView extends MergeView {
    public DifferenceView(Combiner combiner, View first, View second) {
        super(combiner, first, second);
    }

    @Override
    protected byte[] doLoad(Transaction txn, byte[] key) throws IOException {
        return mCombiner.loadDifference(txn, key, mFirst, mSecond);
    }

    @Override
    protected MergeCursor newCursor(Transaction txn, MergeView view,
                                    Cursor first, Cursor second) {
        return new DifferenceCursor(txn, view, first, second);
    }

    @Override
    protected String type() {
        return "difference";
    }
}
