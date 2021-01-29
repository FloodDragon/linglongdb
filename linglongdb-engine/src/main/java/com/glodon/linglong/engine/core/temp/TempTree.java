package com.glodon.linglong.engine.core.temp;

import com.glodon.linglong.engine.core.LocalDatabase;
import com.glodon.linglong.engine.core.Node;
import com.glodon.linglong.engine.core.Tree;
import com.glodon.linglong.engine.core.TreeCursor;
import com.glodon.linglong.engine.core.tx.Transaction;

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
