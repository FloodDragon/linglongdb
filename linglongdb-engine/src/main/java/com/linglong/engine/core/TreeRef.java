package com.linglong.engine.core;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 *
 */
public final class TreeRef extends WeakReference<Tree> {
    final long mId;
    final byte[] mName;
    final Node mRoot;

    public TreeRef(Tree tree, ReferenceQueue<? super Tree> queue) {
        super(tree, queue);
        mId = tree.mId;
        mName = tree.mName;
        mRoot = tree.mRoot;
    }
}
