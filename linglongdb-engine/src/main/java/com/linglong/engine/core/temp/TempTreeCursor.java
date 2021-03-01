package com.linglong.engine.core.temp;

import com.linglong.engine.core.TreeCursor;
import com.linglong.engine.core.tx.Transaction;

/**
 * @author Stereo
 */
public final class TempTreeCursor extends TreeCursor {
    public TempTreeCursor(TempTree tree, Transaction txn) {
        super(tree, txn);
    }

    public TempTreeCursor(TempTree tree) {
        super(tree);
    }

    @Override
    protected int storeMode() {
        // Never redo.
        return 2;
    }
}
