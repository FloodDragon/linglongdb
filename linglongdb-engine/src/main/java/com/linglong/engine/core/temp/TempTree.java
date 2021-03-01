package com.linglong.engine.core.temp;

import com.linglong.engine.core.LocalDatabase;
import com.linglong.engine.core.Node;
import com.linglong.engine.core.Tree;
import com.linglong.engine.core.TreeCursor;
import com.linglong.engine.core.tx.Transaction;

/**
 * @author Stereo
 */
public final class TempTree extends Tree {
    
    public TempTree(LocalDatabase db, long id, byte[] idBytes, Node root) {
        super(db, id, idBytes, root);
    }

    @Override
    public TreeCursor newCursor(Transaction txn) {
        return new TempTreeCursor(this, txn);
    }
}
