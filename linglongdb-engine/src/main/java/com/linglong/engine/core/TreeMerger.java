package com.linglong.engine.core;

import com.linglong.engine.core.frame.Chain;
import com.linglong.engine.core.tx.Transaction;

import java.util.concurrent.Executor;

/**
 * @author Stereo
 */
abstract class TreeMerger extends TreeSeparator {

    TreeMerger(LocalDatabase db, Tree[] sources, Executor executor, int workerCount) {
        super(db, sources, executor, workerCount);
    }

    @Override
    protected void finished(Chain<Tree> firstRange) {
        Tree merged = firstRange.element();

        if (merged != null) merge:{
            Chain<Tree> range = firstRange.next();

            while (range != null) {
                Tree tree = range.element();

                if (tree != null) {
                    try {
                        merged = Tree.graftTempTree(merged, tree);
                    } catch (Throwable e) {
                        failed(e);

                        merged(merged);

                        while (true) {
                            remainder(tree);
                            do {
                                range = range.next();
                                if (range == null) {
                                    break merge;
                                }
                                tree = range.element();
                            } while (tree == null);
                        }
                    }
                }

                range = range.next();
            }

            merged(merged);
        }

        for (Tree source : mSources) {
            if (isEmpty(source)) {
                try {
                    mDatabase.quickDeleteTemporaryTree(source);
                    continue;
                } catch (Throwable e) {
                    failed(e);
                }
            }

            remainder(source);
        }

        remainder(null);
    }

    protected abstract void merged(Tree tree);

    protected abstract void remainder(Tree tree);

    private static boolean isEmpty(Tree tree) {
        Node root = tree.mRoot;
        root.acquireShared();
        boolean empty = root.isLeaf() && !root.hasKeys();
        root.releaseShared();

        if (!empty) {
            TreeCursor c = tree.newCursor(Transaction.BOGUS);
            try {
                c.mKeyOnly = true;
                c.first();
                empty = c.key() == null;
            } catch (Throwable e) {
                // Ignore.
            } finally {
                c.reset();
            }
        }

        return empty;
    }
}
