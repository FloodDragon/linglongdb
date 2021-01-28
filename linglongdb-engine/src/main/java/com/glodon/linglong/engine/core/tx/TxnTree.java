package com.glodon.linglong.engine.core.tx;

import com.glodon.linglong.engine.core.LocalDatabase;
import com.glodon.linglong.engine.core.Node;
import com.glodon.linglong.engine.core.Tree;
import com.glodon.linglong.engine.core.TreeCursor;

/**
 * @author Stereo
 */
public final class TxnTree extends Tree {
    public TxnTree(LocalDatabase db, long id, byte[] idBytes, Node root) {
        super(db, id, idBytes, root);
    }

    @Override
    public TreeCursor newCursor(Transaction txn) {
        return new TxnTreeCursor(this, txn);
    }
}
