package com.linglong.engine.core.tx;

import com.linglong.engine.core.TreeCursor;

/**
 * @author Stereo
 */
final public class TxnTreeCursor extends TreeCursor {
    public TxnTreeCursor(TxnTree tree, Transaction txn) {
        super(tree, txn);
    }

    public TxnTreeCursor(TxnTree tree) {
        super(tree);
    }

    @Override
    protected int storeMode() {
        // Always undo.
        return 1;
    }
}
