package com.glodon.linglong.engine.core.view;

import com.glodon.linglong.engine.core.frame.Combiner;
import com.glodon.linglong.engine.core.frame.Cursor;
import com.glodon.linglong.engine.core.frame.View;
import com.glodon.linglong.engine.core.tx.Transaction;

import java.io.IOException;

/**
 * @author Stereo
 */
public final class UnionView extends MergeView {
    public UnionView(Combiner combiner, View first, View second) {
        super(combiner, first, second);
    }

    @Override
    protected byte[] doLoad(Transaction txn, byte[] key) throws IOException {
        return mCombiner.loadUnion(txn, key, mFirst, mSecond);
    }

    @Override
    protected MergeCursor newCursor(Transaction txn, MergeView view,
                                    Cursor first, Cursor second) {
        return new UnionCursor(txn, view, first, second);
    }

    @Override
    protected String type() {
        return "union";
    }
}
