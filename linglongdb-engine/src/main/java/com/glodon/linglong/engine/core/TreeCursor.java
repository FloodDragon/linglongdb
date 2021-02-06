package com.glodon.linglong.engine.core;


import com.glodon.linglong.base.common.CauseCloseable;
import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.exception.*;
import com.glodon.linglong.base.common.KeyComparator;
import com.glodon.linglong.base.common.Ordering;
import com.glodon.linglong.engine.config.DurabilityMode;
import com.glodon.linglong.engine.core.frame.AbstractValueAccessor;
import com.glodon.linglong.engine.core.frame.Cursor;
import com.glodon.linglong.engine.core.frame.Index;
import com.glodon.linglong.engine.core.lock.*;
import com.glodon.linglong.engine.core.page.DirectPageOps;
import com.glodon.linglong.engine.core.tx.LocalTransaction;
import com.glodon.linglong.engine.core.tx.RedoWriter;
import com.glodon.linglong.engine.core.tx.Transaction;
import com.glodon.linglong.engine.core.tx.TransactionContext;
import com.glodon.linglong.engine.core.frame.ViewUtils;
import com.glodon.linglong.engine.observer.CompactionObserver;
import com.glodon.linglong.engine.observer.VerificationObserver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

import static com.glodon.linglong.base.common.Utils.*;

/**
 * @author Stereo
 */
public class TreeCursor extends AbstractValueAccessor implements CauseCloseable, Cursor {
    private static final int LIMIT_LE = 1, LIMIT_LT = 2, LIMIT_GE = -1, LIMIT_GT = -2;

    final Tree mTree;

    public Tree getTree() {
        return mTree;
    }

    LocalTransaction mTxn;


    public void setTxn(LocalTransaction mTxn) {
        this.mTxn = mTxn;
    }

    public LocalTransaction getTxn() {
        return mTxn;
    }

    CursorFrame mFrame;

    byte[] mKey;

    public byte[] getKey() {
        return mKey;
    }

    byte[] mValue;

    public void setValue(byte[] mValue) {
        this.mValue = mValue;
    }

    public byte[] getValue() {
        return mValue;
    }

    boolean mKeyOnly;

    public void setKeyOnly(boolean mKeyOnly) {
        this.mKeyOnly = mKeyOnly;
    }

    public boolean isKeyOnly() {
        return mKeyOnly;
    }

    private int mKeyHash;

    long mCursorId;

    public void setCursorId(long mCursorId) {
        this.mCursorId = mCursorId;
    }

    public long getCursorId() {
        return mCursorId;
    }

    public TreeCursor(Tree tree, Transaction txn) {
        mTxn = tree.check(txn);
        mTree = tree;
    }

    public TreeCursor(Tree tree) {
        mTree = tree;
    }

    @Override
    public final Ordering getOrdering() {
        return Ordering.ASCENDING;
    }

    @Override
    public Comparator<byte[]> getComparator() {
        return KeyComparator.THE;
    }

    @Override
    public final Transaction link(Transaction txn) {
        LocalTransaction old = mTxn;
        mTxn = mTree.check(txn);
        return old;
    }

    @Override
    public final Transaction link() {
        return mTxn;
    }

    @Override
    public final byte[] key() {
        return mKey;
    }

    @Override
    public final byte[] value() {
        return mValue;
    }

    void valueStats(long[] stats) throws IOException {
        stats[0] = -1;
        stats[1] = 0;
        if (mValue != null && mValue != NOT_LOADED) {
            stats[0] = mValue.length;
            stats[1] = 0;
            return;
        }
        CursorFrame frame = frameSharedNotSplit();
        Node node = frame.getNode();
        try {
            int pos = frame.getNodePos();
            if (pos >= 0) {
                node.retrieveLeafValueStats(pos, stats);
            }
        } finally {
            node.releaseShared();
        }
    }


    @Override
    public final boolean autoload(boolean mode) {
        boolean old = mKeyOnly;
        mKeyOnly = !mode;
        return !old;
    }

    @Override
    public final boolean autoload() {
        return !mKeyOnly;
    }

    @Override
    public final int compareKeyTo(byte[] rkey) {
        byte[] lkey = mKey;
        return compareUnsigned(lkey, 0, lkey.length, rkey, 0, rkey.length);
    }

    @Override
    public final int compareKeyTo(byte[] rkey, int offset, int length) {
        byte[] lkey = mKey;
        return compareUnsigned(lkey, 0, lkey.length, rkey, offset, length);
    }

    @Override
    public final boolean register() throws IOException {
        if (mCursorId == 0) {
            LocalTransaction txn = mTxn;
            if (txn == null) {
                if (storeMode() > 1) {
                    return false;
                }

                LocalDatabase db = mTree.mDatabase;
                RedoWriter redo = db.txnRedoWriter();

                if (redo.adjustTransactionId(1) <= 0) {
                    return false;
                }

                Tree cursorRegistry = db.openCursorRegistry();

                CommitLock.Shared shared = db.commitLock().acquireShared();
                try {
                    TransactionContext context = db.anyTransactionContext();
                    long cursorId = context.nextTransactionId();
                    context.redoCursorRegister(redo, cursorId, mTree.mId);
                    mCursorId = cursorId;
                    db.registerCursor(cursorRegistry, this);
                } catch (UnmodifiableReplicaException e) {
                    return false;
                } finally {
                    shared.release();
                }
            } else {
                if (txn.durabilityMode() == DurabilityMode.NO_REDO) {
                    return false;
                }

                LocalDatabase db = mTree.mDatabase;
                Tree cursorRegistry = db.openCursorRegistry();

                CommitLock.Shared shared = db.commitLock().acquireShared();
                try {
                    if (!txn.tryRedoCursorRegister(this)) {
                        return false;
                    }
                    db.registerCursor(cursorRegistry, this);
                } catch (UnmodifiableReplicaException e) {
                    return false;
                } finally {
                    shared.release();
                }
            }
        }

        return true;
    }

    @Override
    public final void unregister() {
        long cursorId = mCursorId;
        if (cursorId != 0) {
            doUnregister(mTxn, cursorId);
        }
    }

    private int keyHash() {
        int hash = mKeyHash;
        if (hash == 0) {
            mKeyHash = hash = LockManager.hash(mTree.mId, mKey);
        }
        return hash;
    }

    @Override
    public final LockResult first() throws IOException {
        reset();

        if (!toFirst(new CursorFrame(), latchRootNode())) {
            return LockResult.UNOWNED;
        }

        LocalTransaction txn = mTxn;
        LockResult result = tryCopyCurrent(txn);

        if (result != null) {
            // Extra check for filtering ghosts.
            if (mValue != null) {
                return result;
            }
        } else if ((result = lockAndCopyIfExists(txn)) != null) {
            return result;
        }

        return next();
    }

    private boolean toFirst(CursorFrame frame, Node node) throws IOException {
        return toFirstLeaf(frame, node).hasKeys() ? true : toNext(mFrame);
    }

    final void firstLeaf() throws IOException {
        reset();
        toFirstLeaf(new CursorFrame(), latchRootNode());
        mFrame.getNode().releaseShared();
    }

    private Node toFirstLeaf(CursorFrame frame, Node node) throws IOException {
        try {
            while (true) {
                frame.bind(node, 0);
                if (node.mSplit != null) {
                    node = finishSplitShared(frame, node);
                    if (frame.getNodePos() != 0) {
                        frame.bindOrReposition(node, 0);
                    }
                }
                if (node.isLeaf()) {
                    mFrame = frame;
                    return node;
                }
                node = mTree.mDatabase.latchToChild(node, 0);
                frame = new CursorFrame(frame);
            }
        } catch (Throwable e) {
            throw cleanup(e, frame);
        }
    }

    private Node toFirstInternal(CursorFrame frame, Node node) throws IOException {
        try {
            while (true) {
                frame.bind(node, 0);
                if (node.mSplit != null) {
                    node = finishSplitShared(frame, node);
                    if (frame.getNodePos() != 0) {
                        frame.bindOrReposition(node, 0);
                    }
                }
                if (node.isBottomInternal()) {
                    mFrame = frame;
                    return node;
                }
                node = mTree.mDatabase.latchToChild(node, 0);
                frame = new CursorFrame(frame);
            }
        } catch (Throwable e) {
            throw cleanup(e, frame);
        }
    }

    @Override
    public final LockResult last() throws IOException {
        reset();

        if (!toLast(new CursorFrame(), latchRootNode())) {
            return LockResult.UNOWNED;
        }

        LocalTransaction txn = mTxn;
        LockResult result = tryCopyCurrent(txn);

        if (result != null) {
            if (mValue != null) {
                return result;
            }
        } else if ((result = lockAndCopyIfExists(txn)) != null) {
            return result;
        }

        return previous();
    }

    private boolean toLast(CursorFrame frame, Node node) throws IOException {
        return toLastLeaf(frame, node).hasKeys() ? true : toPrevious(mFrame);
    }

    private Node toLastLeaf(CursorFrame frame, Node node) throws IOException {
        try {
            while (true) {
                Split split = node.mSplit;
                if (split != null) {
                    frame.bind(node, split.highestPos(node));
                    node = finishSplitShared(frame, node);
                }

                if (node.isLeaf()) {
                    frame.bindOrReposition(node, Math.max(0, node.highestLeafPos()));
                    mFrame = frame;
                    return node;
                }

                int childPos = node.highestInternalPos();
                frame.bindOrReposition(node, childPos);
                node = mTree.mDatabase.latchToChild(node, childPos);

                frame = new CursorFrame(frame);
            }
        } catch (Throwable e) {
            throw cleanup(e, frame);
        }
    }

    private Node toLastInternal(CursorFrame frame, Node node) throws IOException {
        try {
            while (true) {
                Split split = node.mSplit;
                if (split != null) {
                    frame.bind(node, split.highestPos(node));
                    node = finishSplitShared(frame, node);
                }
                int childPos = node.highestInternalPos();
                frame.bindOrReposition(node, childPos);
                if (node.isBottomInternal()) {
                    mFrame = frame;
                    return node;
                }
                node = mTree.mDatabase.latchToChild(node, childPos);
                frame = new CursorFrame(frame);
            }
        } catch (Throwable e) {
            throw cleanup(e, frame);
        }
    }

    @Override
    public final LockResult skip(long amount) throws IOException {
        if (amount == 0) {
            LocalTransaction txn = mTxn;
            if (txn != null && txn != Transaction.BOGUS) {
                byte[] key = mKey;
                if (key != null) {
                    return txn.getManager().check(txn, mTree.mId, key, keyHash());
                }
            }
            return LockResult.UNOWNED;
        }

        mCursorId &= ~(1L << 63); // key will change, but cursor isn't reset

        try {
            CursorFrame frame = frameSharedNotSplit();
            if (amount > 0) {
                if (amount > 1 && (frame = skipNextGap(frame, amount - 1, null)) == null) {
                    return LockResult.UNOWNED;
                }
                return next(mTxn, frame);
            } else {
                if (amount < -1 && (frame = skipPreviousGap(frame, -1 - amount, null)) == null) {
                    return LockResult.UNOWNED;
                }
                return previous(mTxn, frame);
            }
        } catch (Throwable e) {
            throw handleException(e, false);
        }
    }

    @Override
    public final LockResult skip(long amount, byte[] limitKey, boolean inclusive)
            throws IOException {
        if (amount == 0 || limitKey == null) {
            return skip(amount);
        }

        mCursorId &= ~(1L << 63); // key will change, but cursor isn't reset

        try {
            CursorFrame frame = frameSharedNotSplit();
            if (amount > 0) {
                if (amount > 1 && (frame = skipNextGap(frame, amount - 1, limitKey)) == null) {
                    return LockResult.UNOWNED;
                }
                return nextCmp(limitKey, inclusive ? LIMIT_LE : LIMIT_LT, frame);
            } else {
                if (amount < -1
                        && (frame = skipPreviousGap(frame, -1 - amount, limitKey)) == null) {
                    return LockResult.UNOWNED;
                }
                return previousCmp(limitKey, inclusive ? LIMIT_GE : LIMIT_GT, frame);
            }
        } catch (Throwable e) {
            throw handleException(e, false);
        }
    }

    @Override
    public final LockResult next() throws IOException {
        return next(mTxn, frameSharedNotSplit());
    }

    @Override
    public final LockResult nextLe(byte[] limitKey) throws IOException {
        return nextCmp(limitKey, LIMIT_LE);
    }

    @Override
    public final LockResult nextLt(byte[] limitKey) throws IOException {
        return nextCmp(limitKey, LIMIT_LT);
    }

    private LockResult nextCmp(byte[] limitKey, int limitMode) throws IOException {
        Utils.keyCheck(limitKey);
        return nextCmp(limitKey, limitMode, frameSharedNotSplit());
    }

    private LockResult nextCmp(byte[] limitKey, int limitMode, CursorFrame frame)
            throws IOException {
        mCursorId &= ~(1L << 63); // key will change, but cursor isn't reset
        LocalTransaction txn = mTxn;

        while (true) {
            if (!toNext(frame)) {
                return LockResult.UNOWNED;
            }
            LockResult result = tryCopyCurrentCmp(txn, limitKey, limitMode);
            if (result != null) {
                if (mKey == null || mValue != null) {
                    return result;
                }
            } else if ((result = lockAndCopyIfExists(txn)) != null) {
                return result;
            }
            frame = frameSharedNotSplit();
        }
    }

    private LockResult next(LocalTransaction txn, CursorFrame frame) throws IOException {
        mCursorId &= ~(1L << 63); // key will change, but cursor isn't reset

        while (true) {
            if (!toNext(frame)) {
                return LockResult.UNOWNED;
            }
            LockResult result = tryCopyCurrent(txn);
            if (result != null) {
                // Extra check for filtering ghosts.
                if (mValue != null) {
                    return result;
                }
            } else if ((result = lockAndCopyIfExists(txn)) != null) {
                return result;
            }
            frame = frameSharedNotSplit();
        }
    }

    private boolean toNext(CursorFrame frame) throws IOException {
        while (true) {
            Node node = toNextLeaf(frame);
            if (node == null) {
                return false;
            }
            if (node.hasKeys()) {
                return true;
            }
            frame = mFrame;
        }
    }

    private void nextLeaf() throws IOException {
        Node node = toNextLeaf(frameSharedNotSplit());
        if (node != null) {
            node.releaseShared();
        }
    }

    private void skipToNextLeaf() throws IOException {
        mFrame.setNodePos(Integer.MAX_VALUE - 1);
        nextLeaf();
    }


    private Node toNextLeaf(CursorFrame frame) throws IOException {

        start:
        while (true) {
            Node node = frame.getNode();

            quick:
            {
                int pos = frame.getNodePos();
                if (pos < 0) {
                    pos = ~2 - pos; // eq: (~pos) - 2;
                    if (pos >= node.highestLeafPos()) {
                        break quick;
                    }
                    frame.setNotFoundKey(null);
                } else if (pos >= node.highestLeafPos()) {
                    break quick;
                }
                frame.setNodePos(pos + 2);
                return node;
            }

            while (true) {
                CursorFrame parentFrame = frame.getParentFrame();

                if (parentFrame == null) {
                    node.releaseShared();
                    reset();
                    return null;
                }

                Node parentNode;

                latchParent:
                {
                    splitCheck:
                    {
                        parentNode = parentFrame.tryAcquireShared();
                        node.releaseShared();

                        if (parentNode == null) {
                            parentNode = parentFrame.acquireShared();
                            if (parentNode.mSplit == null) {
                                break splitCheck;
                            }
                        } else if (parentNode.mSplit == null) {
                            break latchParent;
                        }

                        parentNode = finishSplitShared(parentFrame, parentNode);
                    }

                    if (frame != mFrame) {
                        parentNode.releaseShared();
                        frame = frameSharedNotSplit();
                        continue start;
                    }

                    node = frame.acquireShared();

                    if (node.mSplit != null) {
                        parentNode.releaseShared();
                        finishSplitShared(frame, node);
                        continue start;
                    }

                    quick:
                    {
                        int pos = frame.getNodePos();
                        if (pos < 0) {
                            pos = ~2 - pos; // eq: (~pos) - 2;
                            if (pos >= node.highestLeafPos()) {
                                break quick;
                            }
                            frame.setNotFoundKey(null);
                        } else if (pos >= node.highestLeafPos()) {
                            break quick;
                        }
                        parentNode.releaseShared();
                        frame.setNodePos(pos + 2);
                        return node;
                    }

                    node.releaseShared();
                }

                int parentPos = parentFrame.getNodePos();

                if (parentPos < parentNode.highestInternalPos()) {
                    parentFrame.popChilden(mFrame);
                    parentFrame.setNodePos(parentPos += 2);
                    frame = new CursorFrame(parentFrame);
                    return toFirstLeaf(frame, mTree.mDatabase.latchToChild(parentNode, parentPos));
                }

                frame = parentFrame;
                node = parentNode;
            }
        }
    }

    private Node toNextInternal(CursorFrame frame) throws IOException {
        start:
        while (true) {
            Node node = frame.getNode();

            quick:
            {
                int pos = frame.getNodePos();
                if (pos >= node.highestInternalPos()) {
                    break quick;
                }
                frame.setNodePos(pos + 2);
                return node;
            }

            while (true) {
                CursorFrame parentFrame = frame.getParentFrame();

                if (parentFrame == null) {
                    node.releaseShared();
                    reset();
                    return null;
                }

                Node parentNode;

                latchParent:
                {
                    splitCheck:
                    {
                        parentNode = parentFrame.tryAcquireShared();
                        node.releaseShared();

                        if (parentNode == null) {
                            parentNode = parentFrame.acquireShared();
                            if (parentNode.mSplit == null) {
                                break splitCheck;
                            }
                        } else if (parentNode.mSplit == null) {
                            break latchParent;
                        }

                        parentNode = finishSplitShared(parentFrame, parentNode);
                    }

                    if (frame != mFrame) {
                        parentNode.releaseShared();
                        frame = frameSharedNotSplit();
                        continue start;
                    }

                    node = frame.acquireShared();

                    if (node.mSplit != null) {
                        parentNode.releaseShared();
                        finishSplitShared(frame, node);
                        continue start;
                    }

                    quick:
                    {
                        int pos = frame.getNodePos();
                        if (pos >= node.highestInternalPos()) {
                            break quick;
                        }
                        parentNode.releaseShared();
                        frame.setNodePos(pos + 2);
                        return node;
                    }

                    node.releaseShared();
                }


                int parentPos = parentFrame.getNodePos();

                if (parentPos < parentNode.highestInternalPos()) {
                    parentFrame.popChilden(mFrame);
                    parentFrame.setNodePos(parentPos += 2);
                    frame = new CursorFrame(parentFrame);
                    Node child = mTree.mDatabase.latchToChild(parentNode, parentPos);
                    return toFirstInternal(frame, child);
                }

                frame = parentFrame;
                node = parentNode;
            }
        }
    }

    private CursorFrame skipNextGap(CursorFrame frame, long amount, byte[] inLimit)
            throws IOException {
        start:
        while (true) {
            int pos = frame.getNodePos();
            if (pos < 0) {
                pos = ~pos;
            }

            Node node = frame.getNode();
            int avail = (node.highestLeafPos() + 2 - pos) >> 1;

            if (amount < avail) {
                frame.setNodePos(pos + (((int) amount) << 1));
                return frame;
            }

            amount -= avail;
            node.releaseShared();

            mFrame = frame = frame.pop();

            if (frame == null) {
                // Nothing left.
                return null;
            }

            node = frame.acquireShared();
            if (node.mSplit != null) {
                node = finishSplitShared(frame, node);
            }

            while (true) {
                if (!node.isBottomInternal()) {
                    if (node.mPage == DirectPageOps.p_closedTreePage()) {
                        return null;
                    }
                    try {
                        throw new CorruptDatabaseException(node.toString());
                    } finally {
                        node.releaseShared();
                    }
                }

                if (inLimit != null && frame.getNodePos() <= node.highestKeyPos()) {
                    int cmp;
                    try {
                        cmp = node.compareKey(frame.getNodePos(), inLimit);
                    } catch (Throwable e) {
                        resetLatched(node);
                        throw e;
                    }

                    if (cmp > 0) {
                        resetLatched(node);
                        return null;
                    }
                }

                node = toNextInternal(frame);

                if (node == null) {
                    return null;
                }

                frame = mFrame;

                Node child;
                int childCount;

                obtainCount:
                {
                    childCount = node.retrieveChildEntryCount(frame.getNodePos());

                    if (childCount >= 0) {
                        if (amount >= childCount) {
                            amount -= childCount;
                            continue;
                        }
                        child = mTree.mDatabase.latchToChild(node, frame.getNodePos());
                        break obtainCount;
                    }

                    child = mTree.mDatabase.latchChildRetainParent(node, frame.getNodePos());

                    if (child.mSplit != null) {
                        node.releaseShared();
                        frame = new CursorFrame(frame);
                        toFirstLeaf(frame, child);
                        continue start;
                    }

                    childCount = child.countNonGhostKeys();

                    if (mTree.allowStoredCounts() &&
                            child.mCachedState == Node.CACHED_CLEAN && node.tryUpgrade()) {
                        try {
                            CommitLock.Shared shared =
                                    mTree.mDatabase.commitLock().tryAcquireShared();
                            if (shared != null) {
                                try {
                                    node = notSplitDirty(frame);
                                    node.storeChildEntryCount(frame.getNodePos(), childCount);
                                } catch (Throwable e) {
                                    child.releaseShared();
                                    throw e;
                                } finally {
                                    shared.release();
                                }
                            }
                        } finally {
                            node.downgrade();
                        }
                    }

                    if (amount >= childCount) {
                        child.releaseShared();
                        amount -= childCount;
                        continue;
                    }

                    node.releaseShared();
                }

                try {
                    frame = new CursorFrame(frame);
                    frame.bind(child, ((int) amount) << 1);
                    mFrame = frame;
                    return frame;
                } catch (Throwable e) {
                    child.releaseShared();
                    throw e;
                }
            }
        }
    }

    long count(byte[] lowKey, TreeCursor high) throws IOException {
        mKeyOnly = true;

        if (lowKey == null) {
            first();
        } else {
            findGe(lowKey);
        }

        if (mKey == null || (high != null && compareUnsigned(mKey, high.mKey) >= 0)) {
            // Found nothing.
            return 0;
        }

        CursorFrame frame = frameSharedNotSplit();

        int pos = frame.getNodePos();
        if (pos < 0) {
            pos = ~pos;
        }

        Node node = frame.getNode();
        int lowPos = node.searchVecStart() + pos;

        if (high != null && node == high.mFrame.getNode()) {
            long count = countNonGhostKeys(node, lowPos, high);
            node.releaseShared();
            return count;
        }

        long count = node.countNonGhostKeys(lowPos, node.searchVecEnd());
        node.releaseShared();

        mFrame = frame = frame.pop();

        if (frame == null) {
            return count;
        }

        node = frame.acquireShared();
        if (node.mSplit != null) {
            node = finishSplitShared(frame, node);
        }

        while (true) {
            if (!node.isBottomInternal()) {
                try {
                    if (node.mPage == DirectPageOps.p_closedTreePage()) {
                        return count;
                    }
                    throw new CorruptDatabaseException(node.toString());
                } finally {
                    node.releaseShared();
                }
            }

            node = toNextInternal(frame);

            if (node == null) {
                return count;
            }

            frame = mFrame;

            if (high != null) {
                CursorFrame highFrame;

                while (node == (highFrame = high.mFrame.getParentFrame()).getNode() &&
                        frame.getNodePos() >= highFrame.getNodePos()) {
                    Node child = high.mFrame.acquireShared();
                    node.releaseShared();

                    if (child.mSplit != null) {
                        finishSplitShared(high.mFrame, child).releaseShared();
                        node = frame.acquireShared();
                        continue;
                    }

                    count += countNonGhostKeys(child, child.searchVecStart(), high);
                    child.releaseShared();
                    return count;
                }
            }

            int childCount = node.retrieveChildEntryCount(frame.getNodePos());

            if (childCount >= 0) {
                count += childCount;
                continue;
            }

            Node child = mTree.mDatabase.latchChildRetainParent(node, frame.getNodePos());
            childCount = child.countNonGhostKeys();
            count += childCount;

            if (mTree.allowStoredCounts() &&
                    child.mCachedState == Node.CACHED_CLEAN && node.tryUpgrade()) {
                try {
                    CommitLock.Shared shared = mTree.mDatabase.commitLock().tryAcquireShared();
                    if (shared != null) {
                        try {
                            node = notSplitDirty(frame);
                            node.storeChildEntryCount(frame.getNodePos(), childCount);
                            continue;
                        } finally {
                            shared.release();
                            child.releaseShared();
                        }
                    }
                } finally {
                    node.downgrade();
                }
            }

            if (child.mSplit != null) {
                Node sibling = child.mSplit.latchSibling();
                count += sibling.countNonGhostKeys();
                sibling.releaseShared();
            }

            child.releaseShared();
        }
    }

    private static long countNonGhostKeys(Node node, int lowPos, TreeCursor high) {
        int highPos = high.mFrame.getNodePos();
        if (highPos < 0) {
            highPos = ~highPos;
        }
        return node.countNonGhostKeys(lowPos, node.searchVecStart() + highPos - 2);
    }

    @Override
    public final LockResult previous() throws IOException {
        return previous(mTxn, frameSharedNotSplit());
    }

    @Override
    public final LockResult previousGe(byte[] limitKey) throws IOException {
        return previousCmp(limitKey, LIMIT_GE);
    }

    @Override
    public final LockResult previousGt(byte[] limitKey) throws IOException {
        return previousCmp(limitKey, LIMIT_GT);
    }

    private LockResult previousCmp(byte[] limitKey, int limitMode) throws IOException {
        Utils.keyCheck(limitKey);
        return previousCmp(limitKey, limitMode, frameSharedNotSplit());
    }

    private LockResult previousCmp(byte[] limitKey, int limitMode, CursorFrame frame)
            throws IOException {
        mCursorId &= ~(1L << 63); // key will change, but cursor isn't reset
        LocalTransaction txn = mTxn;

        while (true) {
            if (!toPrevious(frame)) {
                return LockResult.UNOWNED;
            }
            LockResult result = tryCopyCurrentCmp(txn, limitKey, limitMode);
            if (result != null) {
                if (mKey == null || mValue != null) {
                    return result;
                }
            } else if ((result = lockAndCopyIfExists(txn)) != null) {
                return result;
            }
            frame = frameSharedNotSplit();
        }
    }

    private LockResult previous(LocalTransaction txn, CursorFrame frame)
            throws IOException {
        mCursorId &= ~(1L << 63); // key will change, but cursor isn't reset

        while (true) {
            if (!toPrevious(frame)) {
                return LockResult.UNOWNED;
            }
            LockResult result = tryCopyCurrent(txn);
            if (result != null) {
                // Extra check for filtering ghosts.
                if (mValue != null) {
                    return result;
                }
            } else if ((result = lockAndCopyIfExists(txn)) != null) {
                return result;
            }
            frame = frameSharedNotSplit();
        }
    }

    private boolean toPrevious(CursorFrame frame) throws IOException {
        while (true) {
            Node node = toPreviousLeaf(frame);
            if (node == null) {
                return false;
            }
            if (node.hasKeys()) {
                return true;
            }
            frame = mFrame;
        }
    }

    private Node toPreviousLeaf(CursorFrame frame) throws IOException {
        start:
        while (true) {
            Node node = frame.getNode();

            quick:
            {
                int pos = frame.getNodePos();
                if (pos < 0) {
                    pos = ~pos;
                    if (pos == 0) {
                        break quick;
                    }
                    frame.setNotFoundKey(null);
                } else if (pos == 0) {
                    break quick;
                }
                frame.setNodePos(pos - 2);
                return node;
            }

            while (true) {
                CursorFrame parentFrame = frame.getParentFrame();

                if (parentFrame == null) {
                    node.releaseShared();
                    reset();
                    return null;
                }

                Node parentNode;

                latchParent:
                {
                    splitCheck:
                    {
                        parentNode = parentFrame.tryAcquireShared();
                        node.releaseShared();

                        if (parentNode == null) {
                            parentNode = parentFrame.acquireShared();
                            if (parentNode.mSplit == null) {
                                break splitCheck;
                            }
                        } else if (parentNode.mSplit == null) {
                            break latchParent;
                        }

                        parentNode = finishSplitShared(parentFrame, parentNode);
                    }

                    if (frame != mFrame) {
                        parentNode.releaseShared();
                        frame = frameSharedNotSplit();
                        continue start;
                    }

                    node = frame.acquireShared();

                    if (node.mSplit != null) {
                        parentNode.releaseShared();
                        finishSplitShared(frame, node);
                        continue start;
                    }

                    quick:
                    {
                        int pos = frame.getNodePos();
                        if (pos < 0) {
                            pos = ~pos;
                            if (pos == 0) {
                                break quick;
                            }
                            frame.setNotFoundKey(null);
                        } else if (pos == 0) {
                            break quick;
                        }
                        parentNode.releaseShared();
                        frame.setNodePos(pos - 2);
                        return node;
                    }

                    node.releaseShared();
                }

                int parentPos = parentFrame.getNodePos();

                if (parentPos > 0) {
                    parentFrame.popChilden(mFrame);
                    parentFrame.setNodePos(parentPos -= 2);
                    frame = new CursorFrame(parentFrame);
                    return toLastLeaf(frame, mTree.mDatabase.latchToChild(parentNode, parentPos));
                }

                frame = parentFrame;
                node = parentNode;
            }
        }
    }

    private Node toPreviousInternal(CursorFrame frame) throws IOException {
        start:
        while (true) {
            Node node = frame.getNode();

            quick:
            {
                int pos = frame.getNodePos();
                if (pos == 0) {
                    break quick;
                }
                frame.setNodePos(pos - 2);
                return node;
            }

            while (true) {
                CursorFrame parentFrame = frame.getParentFrame();

                if (parentFrame == null) {
                    node.releaseShared();
                    reset();
                    return null;
                }

                Node parentNode;

                latchParent:
                {
                    splitCheck:
                    {
                        parentNode = parentFrame.tryAcquireShared();
                        node.releaseShared();

                        if (parentNode == null) {
                            parentNode = parentFrame.acquireShared();
                            if (parentNode.mSplit == null) {
                                break splitCheck;
                            }
                        } else if (parentNode.mSplit == null) {
                            break latchParent;
                        }

                        parentNode = finishSplitShared(parentFrame, parentNode);
                    }

                    if (frame != mFrame) {
                        parentNode.releaseShared();
                        frame = frameSharedNotSplit();
                        continue start;
                    }

                    node = frame.acquireShared();

                    if (node.mSplit != null) {
                        parentNode.releaseShared();
                        finishSplitShared(frame, node);
                        continue start;
                    }

                    quick:
                    {
                        int pos = frame.getNodePos();
                        if (pos == 0) {
                            break quick;
                        }
                        parentNode.releaseShared();
                        frame.setNodePos(pos - 2);
                        return node;
                    }

                    node.releaseShared();
                }

                int parentPos = parentFrame.getNodePos();

                if (parentPos > 0) {
                    parentFrame.popChilden(mFrame);
                    parentFrame.setNodePos(parentPos -= 2);
                    frame = new CursorFrame(parentFrame);
                    Node child = mTree.mDatabase.latchToChild(parentNode, parentPos);
                    return toLastInternal(frame, child);
                }

                frame = parentFrame;
                node = parentNode;
            }
        }
    }

    private CursorFrame skipPreviousGap(CursorFrame frame, long amount, byte[] inLimit)
            throws IOException {
        start:
        while (true) {
            int pos = frame.getNodePos();
            if (pos < 0) {
                pos = ~pos;
            }

            Node node = frame.getNode();
            int avail = (pos + 2) >> 1;

            if (amount < avail) {
                frame.setNodePos(pos - (((int) amount) << 1));
                return frame;
            }

            amount -= avail;
            node.releaseShared();

            mFrame = frame = frame.pop();

            if (frame == null) {
                return null;
            }

            node = frame.acquireShared();
            if (node.mSplit != null) {
                node = finishSplitShared(frame, node);
            }

            while (true) {
                if (!node.isBottomInternal()) {
                    try {
                        if (node.mPage == DirectPageOps.p_closedTreePage()) {
                            return null;
                        }
                        throw new CorruptDatabaseException(node.toString());
                    } finally {
                        node.releaseShared();
                    }
                }

                if (inLimit != null && frame.getNodePos() > 0) {
                    int cmp;
                    try {
                        cmp = node.compareKey(frame.getNodePos() - 2, inLimit);
                    } catch (Throwable e) {
                        resetLatched(node);
                        throw e;
                    }

                    if (cmp < 0) {
                        resetLatched(node);
                        return null;
                    }
                }

                node = toPreviousInternal(frame);

                if (node == null) {
                    return null;
                }

                frame = mFrame;

                Node child;
                int childCount;

                obtainCount:
                {
                    childCount = node.retrieveChildEntryCount(frame.getNodePos());

                    if (childCount >= 0) {
                        if (amount >= childCount) {
                            amount -= childCount;
                            continue;
                        }
                        child = mTree.mDatabase.latchToChild(node, frame.getNodePos());
                        break obtainCount;
                    }

                    child = mTree.mDatabase.latchChildRetainParent(node, frame.getNodePos());

                    if (child.mSplit != null) {
                        node.releaseShared();
                        frame = new CursorFrame(frame);
                        toLastLeaf(frame, child);
                        continue start;
                    }

                    childCount = child.countNonGhostKeys();

                    if (mTree.allowStoredCounts() &&
                            child.mCachedState == Node.CACHED_CLEAN && node.tryUpgrade()) {
                        try {
                            CommitLock.Shared shared =
                                    mTree.mDatabase.commitLock().tryAcquireShared();
                            if (shared != null) {
                                try {
                                    node = notSplitDirty(frame);
                                    node.storeChildEntryCount(frame.getNodePos(), childCount);
                                } catch (Throwable e) {
                                    child.releaseShared();
                                    throw e;
                                } finally {
                                    shared.release();
                                }
                            }
                        } finally {
                            node.downgrade();
                        }
                    }

                    if (amount >= childCount) {
                        child.releaseShared();
                        amount -= childCount;
                        continue;
                    }

                    node.releaseShared();
                }

                try {
                    frame = new CursorFrame(frame);
                    frame.bind(child, child.highestKeyPos() - (((int) amount) << 1));
                    mFrame = frame;
                    return frame;
                } catch (Throwable e) {
                    child.releaseShared();
                    throw e;
                }
            }
        }
    }

    private LockResult tryCopyCurrent(LocalTransaction txn) throws IOException {
        final Node node;
        final int pos;
        {
            CursorFrame leaf = mFrame;
            node = leaf.getNode();
            pos = leaf.getNodePos();
        }

        try {
            mKeyHash = 0;

            final int lockType;
            if (txn == null) {
                lockType = 0;
            } else {
                LockMode mode = txn.lockMode();
                if (mode.isNoReadLock()) {
                    node.retrieveLeafEntry(pos, this);
                    return LockResult.UNOWNED;
                } else {
                    lockType = mode.getRepeatable();
                }
            }

            mKey = node.retrieveKey(pos);
            mValue = NOT_LOADED;

            try {
                int keyHash = keyHash();

                if (lockType == 0) {
                    if (mTree.isLockAvailable(txn, mKey, keyHash)) {
                        mValue = mKeyOnly ? node.hasLeafValue(pos) : node.retrieveLeafValue(pos);
                        return LockResult.UNOWNED;
                    } else {
                        return null;
                    }
                }

                LockResult result = txn.tryLock(lockType, mTree.mId, mKey, keyHash, 0L);

                if (result.isHeld()) {
                    mValue = mKeyOnly ? node.hasLeafValue(pos) : node.retrieveLeafValue(pos);
                    return result;
                } else {
                    return null;
                }
            } catch (DeadlockException e) {
                return null;
            }
        } finally {
            node.releaseShared();
        }
    }

    private LockResult tryCopyCurrentCmp(LocalTransaction txn, byte[] limitKey, int limitMode)
            throws IOException {
        try {
            return doTryCopyCurrentCmp(txn, limitKey, limitMode);
        } catch (Throwable e) {
            mFrame.getNode().releaseShared();
            throw e;
        }
    }

    private LockResult doTryCopyCurrentCmp(LocalTransaction txn, byte[] limitKey, int limitMode)
            throws IOException {
        final Node node;
        final int pos;
        {
            CursorFrame leaf = mFrame;
            node = leaf.getNode();
            pos = leaf.getNodePos();
        }

        byte[] key = node.retrieveKeyCmp(pos, limitKey, limitMode);

        check:
        {
            if (key != null) {
                if (key != limitKey) {
                    mKey = key;
                    break check;
                } else if ((limitMode & 1) != 0) {
                    mKey = key.clone();
                    break check;
                }
            }

            node.releaseShared();
            reset();
            return LockResult.UNOWNED;
        }

        mKeyHash = 0;

        LockResult result;
        obtainResult:
        {
            final int lockType;
            if (txn == null) {
                lockType = 0;
            } else {
                LockMode mode = txn.lockMode();
                if (mode.isNoReadLock()) {
                    mValue = mKeyOnly ? node.hasLeafValue(pos) : node.retrieveLeafValue(pos);
                    result = LockResult.UNOWNED;
                    break obtainResult;
                } else {
                    lockType = mode.getRepeatable();
                }
            }

            mValue = NOT_LOADED;
            int keyHash = keyHash();

            if (lockType == 0) {
                if (mTree.isLockAvailable(txn, mKey, keyHash)) {
                    mValue = mKeyOnly ? node.hasLeafValue(pos) : node.retrieveLeafValue(pos);
                    result = LockResult.UNOWNED;
                } else {
                    result = null;
                }
                break obtainResult;
            } else {
                result = txn.tryLock(lockType, mTree.mId, mKey, keyHash, 0L);
            }

            if (result.isHeld()) {
                mValue = mKeyOnly ? node.hasLeafValue(pos) : node.retrieveLeafValue(pos);
            } else {
                result = null;
            }
        }

        node.releaseShared();
        return result;
    }

    private LockResult lockAndCopyIfExists(LocalTransaction txn) throws IOException {
        int keyHash = keyHash();

        if (txn == null) {
            Locker locker = mTree.lockSharedLocal(mKey, keyHash);
            try {
                if (copyIfExists() != null) {
                    return LockResult.UNOWNED;
                }
            } finally {
                locker.unlock();
            }
        } else {
            LockResult result;

            int lockType = txn.lockMode().getRepeatable();

            if (lockType == 0) {
                if ((result = txn.lockShared(mTree.mId, mKey, keyHash)) == LockResult.ACQUIRED) {
                    result = LockResult.UNOWNED;
                }
            } else {
                result = txn.lock(lockType, mTree.mId, mKey, keyHash, txn.getLockTimeoutNanos());
            }

            if (copyIfExists() != null) {
                if (result == LockResult.UNOWNED) {
                    txn.unlock();
                }
                return result;
            }

            if (result == LockResult.UNOWNED || result == LockResult.ACQUIRED) {
                txn.unlock();
            }
        }
        return null;
    }

    private byte[] copyIfExists() throws IOException {
        byte[] value;

        CursorFrame frame = frameSharedNotSplit();
        Node node = frame.getNode();
        try {
            int pos = frame.getNodePos();
            if (pos < 0) {
                value = null;
            } else {
                value = mKeyOnly ? node.hasLeafValue(pos) : node.retrieveLeafValue(pos);
            }
        } finally {
            node.releaseShared();
        }

        mValue = value;
        return value;
    }

    private LocalTransaction prepareFind(byte[] key) {
        Utils.keyCheck(key);
        LocalTransaction txn = mTxn;
        int hash;
        selectHash:
        {
            if (txn != null) {
                LockMode mode = txn.lockMode();
                if (mode == LockMode.READ_UNCOMMITTED || mode == LockMode.UNSAFE) {
                    hash = 0;
                    break selectHash;
                }
            }
            hash = LockManager.hash(mTree.mId, key);
        }
        mKey = key;
        mKeyHash = hash;
        return txn;
    }

    private static final int
            VARIANT_REGULAR = 0,
            VARIANT_RETAIN = 1, // retain node latch
            VARIANT_CHECK = 2; // retain node latch, don't lock entry, don't load entry

    @Override
    public final LockResult find(byte[] key) throws IOException {
        reset();
        return doFind(key);
    }

    final LockResult doFind(byte[] key) throws IOException {
        return find(prepareFind(key), key, VARIANT_REGULAR, new CursorFrame(), latchRootNode());
    }

    @Override
    public final LockResult findGe(byte[] key) throws IOException {
        reset();
        LocalTransaction txn = prepareFind(key);
        LockResult result = find(txn, key, VARIANT_RETAIN, new CursorFrame(), latchRootNode());
        if (mValue != null) {
            mFrame.getNode().releaseShared();
            return result;
        } else {
            if (result == LockResult.ACQUIRED) {
                txn.unlock();
            }
            return next(txn, mFrame);
        }
    }

    @Override
    public final LockResult findLe(byte[] key) throws IOException {
        reset();
        LocalTransaction txn = prepareFind(key);
        LockResult result = find(txn, key, VARIANT_RETAIN, new CursorFrame(), latchRootNode());
        if (mValue != null) {
            mFrame.getNode().releaseShared();
            return result;
        } else {
            if (result == LockResult.ACQUIRED) {
                txn.unlock();
            }
            return previous(txn, mFrame);
        }
    }

    @Override
    public final LockResult findGt(byte[] key) throws IOException {
        findNoLock(key);
        return next(mTxn, mFrame);
    }

    @Override
    public final LockResult findLt(byte[] key) throws IOException {
        findNoLock(key);
        return previous(mTxn, mFrame);
    }

    private void findNoLock(byte[] key) throws IOException {
        reset();
        Utils.keyCheck(key);
        find(null, key, VARIANT_CHECK, new CursorFrame(), latchRootNode());
    }

    @Override
    public final LockResult findNearby(byte[] key) throws IOException {
        mCursorId &= ~(1L << 63); // key will change, but cursor isn't reset
        LocalTransaction txn = prepareFind(key);

        Node node;
        CursorFrame frame = mFrame;
        if (frame == null) {
            frame = new CursorFrame();
            node = latchRootNode();
        } else {
            node = frame.acquireShared();
            if (node.mSplit != null) {
                node = finishSplitShared(frame, node);
            }

            int startPos = frame.getNodePos();
            if (startPos < 0) {
                startPos = ~startPos;
            }

            int pos = node.binarySearch(key, startPos);

            if (pos >= 0) {
                frame.setNotFoundKey(null);
                frame.setNodePos(pos);
                try {
                    LockResult result = tryLockKey(txn);
                    if (result == null) {
                        mValue = NOT_LOADED;
                    } else {
                        try {
                            mValue = mKeyOnly ? node.hasLeafValue(pos)
                                    : node.retrieveLeafValue(pos);
                            return result;
                        } catch (Throwable e) {
                            mValue = NOT_LOADED;
                            throw e;
                        }
                    }
                } finally {
                    node.releaseShared();
                }
                return doLoad(txn, key, frame, VARIANT_REGULAR);
            } else if ((pos != ~0 || (node.type() & Node.LOW_EXTREMITY) != 0) &&
                    (~pos <= node.highestLeafPos() || (node.type() & Node.HIGH_EXTREMITY) != 0)) {
                frame.setNotFoundKey(key);
                frame.setNodePos(pos);
                LockResult result = tryLockKey(txn);
                if (result == null) {
                    mValue = NOT_LOADED;
                    node.releaseShared();
                } else {
                    mValue = null;
                    node.releaseShared();
                    return result;
                }
                return doLoad(txn, key, frame, VARIANT_REGULAR);
            }

            mFrame = null;

            while (true) {
                CursorFrame parent = frame.pop();

                if (parent == null) {
                    Node root = mTree.mRoot;
                    if (node != root) {
                        node.releaseShared();
                        root.acquireShared();
                        node = root;
                    }
                    frame = null;
                    break;
                }

                node.releaseShared();
                frame = parent;
                node = frame.acquireShared();

                if (node.mSplit != null) {
                    node = finishSplitShared(frame, node);
                }

                try {
                    pos = Node.internalPos(node.binarySearch(key, frame.getNodePos()));
                } catch (Throwable e) {
                    node.releaseShared();
                    throw cleanup(e, frame);
                }

                if ((pos == 0 && (node.type() & Node.LOW_EXTREMITY) == 0) ||
                        (pos >= node.highestInternalPos() && (node.type() & Node.HIGH_EXTREMITY) == 0)) {
                    continue;
                }

                frame.setNodePos(pos);
                try {
                    node = mTree.mDatabase.latchToChild(node, pos);
                } catch (Throwable e) {
                    throw cleanup(e, frame);
                }
                break;
            }

            frame = new CursorFrame(frame);
        }

        return find(txn, key, VARIANT_REGULAR, frame, node);
    }

    private LockResult find(LocalTransaction txn, byte[] key, int variant,
                            CursorFrame frame, Node node)
            throws IOException {
        while (true) {
            if (node.isLeaf()) {
                int pos;
                if (node.mSplit == null) {
                    try {
                        pos = node.binarySearch(key);
                    } catch (Throwable e) {
                        node.releaseShared();
                        throw cleanup(e, frame);
                    }
                    frame.bind(node, pos);
                } else {
                    try {
                        pos = node.mSplit.binarySearchLeaf(node, key);
                    } catch (Throwable e) {
                        node.releaseShared();
                        throw cleanup(e, frame);
                    }
                    frame.bind(node, pos);
                    if (pos < 0) {
                        frame.setNotFoundKey(key);
                    }
                    node = finishSplitShared(frame, node);
                    pos = frame.getNodePos();
                }

                mFrame = frame;

                if (variant == VARIANT_CHECK) {
                    if (pos < 0) {
                        frame.setNotFoundKey(key);
                        mValue = null;
                    } else {
                        mValue = NOT_LOADED;
                    }
                    return LockResult.UNOWNED;
                }

                LockResult result = tryLockKey(txn);

                if (result == null) {
                    if (pos < 0) {
                        frame.setNotFoundKey(key);
                    }
                    mValue = NOT_LOADED;
                    node.releaseShared();
                    return doLoad(txn, key, frame, variant);
                }

                if (pos < 0) {
                    frame.setNotFoundKey(key);
                    mValue = null;
                } else {
                    try {
                        mValue = mKeyOnly ? node.hasLeafValue(pos) : node.retrieveLeafValue(pos);
                    } catch (Throwable e) {
                        mValue = NOT_LOADED;
                        node.releaseShared();
                        throw e;
                    }
                }

                if (variant == VARIANT_REGULAR) {
                    node.releaseShared();
                }

                return result;
            }

            Split split = node.mSplit;
            if (split == null) {
                int childPos;
                try {
                    childPos = Node.internalPos(node.binarySearch(key));
                } catch (Throwable e) {
                    node.releaseShared();
                    throw cleanup(e, frame);
                }
                frame.bind(node, childPos);
                try {
                    node = mTree.mDatabase.latchToChild(node, childPos);
                } catch (Throwable e) {
                    throw cleanup(e, frame);
                }
            } else {
                final Node sibling = split.latchSibling();

                final Node left, right;
                if (split.mSplitRight) {
                    left = node;
                    right = sibling;
                } else {
                    left = sibling;
                    right = node;
                }

                final Node selected;
                final int selectedPos;

                try {
                    if (split.compare(key) < 0) {
                        selected = left;
                        selectedPos = Node.internalPos(left.binarySearch(key));
                        frame.bind(node, selectedPos);
                        right.releaseShared();
                    } else {
                        selected = right;
                        selectedPos = Node.internalPos(right.binarySearch(key));
                        frame.bind(node, left.highestInternalPos() + 2 + selectedPos);
                        left.releaseShared();
                    }
                } catch (Throwable e) {
                    node.releaseShared();
                    sibling.releaseShared();
                    throw cleanup(e, frame);
                }

                try {
                    node = mTree.mDatabase.latchToChild(selected, selectedPos);
                } catch (Throwable e) {
                    throw cleanup(e, frame);
                }
            }

            frame = new CursorFrame(frame);
        }
    }

    private LockResult tryLockKey(LocalTransaction txn) {
        LockMode mode;

        if (txn == null || (mode = txn.lockMode()) == LockMode.READ_COMMITTED) {
            return mTree.isLockAvailable(txn, mKey, mKeyHash) ? LockResult.UNOWNED : null;
        }

        try {
            if (mode.isNoReadLock()) {
                return LockResult.UNOWNED;
            }

            LockResult result = txn.tryLock(mode.getRepeatable(), mTree.mId, mKey, mKeyHash, 0L);

            return result.isHeld() ? result : null;
        } catch (DeadlockException e) {
            return null;
        }
    }

    @Override
    public final LockResult random(byte[] lowKey, byte[] highKey) throws IOException {
        if (lowKey != null && highKey != null && compareUnsigned(lowKey, highKey) >= 0) {
            reset();
            return LockResult.UNOWNED;
        }

        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        start:
        while (true) {
            reset();
            CursorFrame frame = new CursorFrame();
            Node node = latchRootNode();

            while (true) {
                if (node.mSplit != null) {
                    frame.bind(node, 0);
                    node = finishSplitShared(frame, node);
                }

                int pos;
                try {
                    pos = randomPosition(rnd, node, lowKey, highKey);
                } catch (Throwable e) {
                    node.releaseShared();
                    throw cleanup(e, frame);
                }
                if (pos < 0) {
                    mFrame = frame;
                    resetLatched(node);
                    if (isRangeEmpty(lowKey, highKey)) {
                        return LockResult.UNOWNED;
                    }
                    continue start;
                }

                frame.bindOrReposition(node, pos);

                if (node.isLeaf()) {
                    mFrame = frame;
                    LocalTransaction txn;
                    try {
                        txn = prepareFind(node.retrieveKey(pos));
                    } catch (Throwable e) {
                        resetLatched(node);
                        throw e;
                    }

                    LockResult result;
                    if ((result = tryLockKey(txn)) == null) {
                        mValue = NOT_LOADED;
                        node.releaseShared();
                        result = doLoad(txn, mKey, frame, VARIANT_REGULAR);
                    } else {
                        try {
                            mValue = mKeyOnly ? node.hasLeafValue(pos)
                                    : node.retrieveLeafValue(pos);
                        } catch (Throwable e) {
                            mValue = NOT_LOADED;
                            node.releaseShared();
                            throw e;
                        }
                        node.releaseShared();
                    }

                    if (mValue == null) {
                        if (result == LockResult.ACQUIRED) {
                            txn.unlock();
                        }

                        frame = frameSharedNotSplit();

                        if (rnd.nextBoolean()) {
                            result = highKey == null ? next(txn, frame)
                                    : nextCmp(highKey, LIMIT_LT, frame);
                            if (mValue == null) {
                                return first();
                            }
                        } else {
                            result = lowKey == null ? previous(txn, frame)
                                    : previousCmp(lowKey, LIMIT_GE, frame);
                            if (mValue == null) {
                                return last();
                            }
                        }
                    }

                    return result;
                }

                try {
                    node = mTree.mDatabase.latchToChild(node, pos);
                } catch (Throwable e) {
                    throw cleanup(e, frame);
                }

                frame = new CursorFrame(frame);
            }
        }
    }

    byte[] randomNode(byte[] lowKey, byte[] highKey) throws IOException {
        if (lowKey != null && highKey != null && compareUnsigned(lowKey, highKey) >= 0) {
            reset();
            return null;
        }

        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        start:
        while (true) {
            reset();
            CursorFrame frame = new CursorFrame();
            Node node = latchRootNode();

            int remainingAttemptsBIN = 2;

            int remainingAttemptsLN = 2;

            search:
            while (true) {
                if (node.mSplit != null) {
                    frame.bindOrReposition(node, 0);
                    node = finishSplitShared(frame, node);
                }

                int pos;
                if (node.isLeaf()) {
                    pos = node.highestLeafPos() >> 31; // -1 or 0
                } else {
                    try {
                        pos = randomPosition(rnd, node, lowKey, highKey);
                    } catch (Throwable e) {
                        node.releaseShared();
                        throw cleanup(e, frame);
                    }
                }

                if (pos < 0) {
                    mFrame = frame;
                    resetLatched(node);
                    if (isRangeEmpty(lowKey, highKey)) {
                        return null;
                    }
                    continue start;
                }

                frame.bindOrReposition(node, pos);

                if (node.isLeaf()) {
                    byte[] startKey = node.retrieveKey(pos);

                    byte[] endKey;
                    {
                        int highPos = node.highestLeafPos();
                        endKey = highPos == pos ? startKey : node.retrieveKey(highPos);
                    }

                    mFrame = frame;
                    LocalTransaction txn;
                    try {
                        txn = prepareFind(startKey);
                    } catch (Throwable e) {
                        resetLatched(node);
                        throw e;
                    }

                    if (tryLockKey(txn) == null) {
                        mValue = NOT_LOADED;
                        node.releaseShared();
                        doLoad(txn, mKey, frame, VARIANT_REGULAR);
                    } else {
                        try {
                            mValue = mKeyOnly ? node.hasLeafValue(pos)
                                    : node.retrieveLeafValue(pos);
                        } catch (Throwable e) {
                            mValue = NOT_LOADED;
                            node.releaseShared();
                            throw e;
                        }
                        node.releaseShared();
                    }

                    return endKey;
                }

                long childId = node.retrieveChildRefId(pos);
                Node child = mTree.mDatabase.nodeMapGet(childId);

                if (child != null) {
                    if (node.isBottomInternal()) {
                        if (--remainingAttemptsLN >= 0) {
                            continue search;
                        }

                        try {
                            int spos = 0;
                            if (lowKey != null) {
                                spos = Node.internalPos(node.binarySearch(lowKey));
                            }

                            int highestInternalPos = node.highestInternalPos();
                            int highestKeyPos = node.highestKeyPos();
                            for (; spos <= highestInternalPos; spos += 2) {
                                childId = node.retrieveChildRefId(spos);
                                child = mTree.mDatabase.nodeMapGet(childId);
                                if (child == null) {
                                    pos = spos;
                                    frame.bindOrReposition(node, pos);
                                    break;
                                }
                                if (highKey != null && spos <= highestKeyPos
                                        && node.compareKey(spos, highKey) >= 0) {
                                    break;
                                }
                            }
                        } catch (Throwable t) {
                        }
                    } else {
                        child.acquireShared();
                        try {
                            if (childId == child.mId && child.isBottomInternal()
                                    && --remainingAttemptsBIN >= 0) {
                                continue search;
                            }
                        } finally {
                            child.releaseShared();
                        }
                    }
                }

                try {
                    node = mTree.mDatabase.latchToChild(node, pos);
                } catch (Throwable e) {
                    throw cleanup(e, frame);
                }

                frame = new CursorFrame(frame);
            }
        }
    }

    private LockResult doLoad(LocalTransaction txn, byte[] key, CursorFrame leaf, int variant)
            throws IOException {
        LockResult result;
        Locker locker;

        if (txn == null) {
            result = LockResult.UNOWNED;
            locker = mTree.lockSharedLocal(key, keyHash());
        } else {
            LockMode mode = txn.lockMode();
            if (mode.isNoReadLock()) {
                result = LockResult.UNOWNED;
                locker = null;
            } else {
                int keyHash = keyHash();
                if (mode == LockMode.READ_COMMITTED) {
                    result = txn.lockShared(mTree.mId, key, keyHash);
                    if (result == LockResult.ACQUIRED) {
                        result = LockResult.UNOWNED;
                        locker = txn;
                    } else {
                        locker = null;
                    }
                } else {
                    result = txn.lock
                            (mode.getRepeatable(), mTree.mId, key, keyHash, txn.getLockTimeoutNanos());
                    locker = null;
                }
            }
        }

        try {
            Node node = leaf.acquireShared();
            if (node.mSplit != null) {
                node = finishSplitShared(leaf, node);
            }
            try {
                int pos = leaf.getNodePos();
                mValue = pos < 0 ? null
                        : mKeyOnly ? node.hasLeafValue(pos) : node.retrieveLeafValue(pos);
            } catch (Throwable e) {
                node.releaseShared();
                throw e;
            }
            if (variant == VARIANT_REGULAR) {
                node.releaseShared();
            }
            return result;
        } finally {
            if (locker != null) {
                locker.unlock();
            }
        }
    }

    Index.Stats analyze() throws IOException {
        double entryCount, keyBytes, valueBytes, freeBytes, totalBytes;

        CursorFrame frame = frameSharedNotSplit();
        Node node = frame.getNode();
        try {
            entryCount = node.numKeys();

            int pos = frame.getNodePos();
            int numKeys;

            freeBytes = node.availableBytes();
            totalBytes = pageSize(node.mPage);

            if (pos < 0 || (numKeys = node.numKeys()) <= 0) {
                keyBytes = 0;
                valueBytes = 0;
            } else {
                long[] stats = new long[2];

                node.retrieveKeyStats(pos, stats);
                keyBytes = ((double) stats[0]) * numKeys;
                totalBytes += ((double) stats[1]) * pageSize(node.mPage);

                node.retrieveLeafValueStats(pos, stats);
                valueBytes = ((double) stats[0]) * numKeys;
                totalBytes += ((double) stats[1]) * pageSize(node.mPage);
            }

            frame = frame.pop();
        } catch (Throwable e) {
            resetLatched(node);
            throw e;
        }

        node.releaseShared();

        while (frame != null) {
            double scalar;
            int availBytes;
            int pageSize;

            node = frame.acquireShared();
            try {
                scalar = node.numKeys() + 1;
                availBytes = node.availableInternalBytes();
                pageSize = pageSize(node.mPage);
                frame = frame.pop();
            } finally {
                node.releaseShared();
            }

            entryCount *= scalar;
            keyBytes *= scalar;
            valueBytes *= scalar;
            freeBytes *= scalar;
            totalBytes *= scalar;

            freeBytes += availBytes;
            totalBytes += pageSize;
        }

        return new Index.Stats(entryCount, keyBytes, valueBytes, freeBytes, totalBytes);
    }

    @Override
    public final LockResult lock() throws IOException {
        final byte[] key = mKey;
        ViewUtils.positionCheck(key);

        final CursorFrame leaf = frame();
        final LocalTransaction txn = mTxn;

        LockResult result;
        final Locker locker;

        try {
            if (txn == null) {
                int keyHash = keyHash();
                if (tryLockLoad(txn, key, keyHash, mKeyOnly, leaf)) {
                    return LockResult.UNOWNED;
                }
                locker = mTree.lockSharedLocal(key, keyHash);
                result = LockResult.UNOWNED;
            } else {
                LockMode mode = txn.lockMode();
                if (mode.isNoReadLock()) {
                    return LockResult.UNOWNED;
                }
                int keyHash = keyHash();
                if (mode == LockMode.READ_COMMITTED) {
                    if (tryLockLoad(txn, key, keyHash, mKeyOnly, leaf)) {
                        return LockResult.UNOWNED;
                    }
                    result = txn.lockShared(mTree.mId, key, keyHash);
                    if (result != LockResult.ACQUIRED) {
                        return result;
                    }
                    result = LockResult.UNOWNED;
                    locker = txn;
                } else {
                    result = txn.lock
                            (mode.getRepeatable(), mTree.mId, key, keyHash, txn.getLockTimeoutNanos());
                    if (result != LockResult.ACQUIRED) {
                        return result;
                    }
                    locker = null;
                }
            }
        } catch (LockFailureException e) {
            mValue = NOT_LOADED;
            throw e;
        }

        try {
            Node node = leaf.acquireShared();
            if (node.mSplit != null) {
                node = finishSplitShared(leaf, node);
            }
            try {
                int pos = leaf.getNodePos();
                mValue = pos < 0 ? null
                        : mKeyOnly ? node.hasLeafValue(pos) : node.retrieveLeafValue(pos);
            } catch (Throwable e) {
                node.releaseShared();
                throw e;
            }
            node.releaseShared();
            return result;
        } finally {
            if (locker != null) {
                locker.unlock();
            }
        }
    }

    @Override
    public final LockResult load() throws IOException {
        final byte[] key = mKey;
        ViewUtils.positionCheck(key);

        final CursorFrame leaf = frame();
        final LocalTransaction txn = mTxn;

        LockResult result;
        final Locker locker;

        try {
            if (txn == null) {
                int keyHash = keyHash();
                if (tryLockLoad(txn, key, keyHash, false, leaf)) {
                    return LockResult.UNOWNED;
                }
                locker = mTree.lockSharedLocal(key, keyHash);
                result = LockResult.UNOWNED;
            } else {
                LockMode mode = txn.lockMode();
                if (mode.isNoReadLock()) {
                    result = LockResult.UNOWNED;
                    locker = null;
                } else {
                    int keyHash = keyHash();
                    if (mode == LockMode.READ_COMMITTED) {
                        if (tryLockLoad(txn, key, keyHash, false, leaf)) {
                            return LockResult.UNOWNED;
                        }
                        result = txn.lockShared(mTree.mId, key, keyHash);
                        if (result == LockResult.ACQUIRED) {
                            result = LockResult.UNOWNED;
                            locker = txn;
                        } else {
                            locker = null;
                        }
                    } else {
                        result = txn.lock
                                (mode.getRepeatable(), mTree.mId, key, keyHash, txn.getLockTimeoutNanos());
                        locker = null;
                    }
                }
            }
        } catch (LockFailureException e) {
            mValue = NOT_LOADED;
            throw e;
        }

        try {
            Node node = leaf.acquireShared();
            if (node.mSplit != null) {
                node = finishSplitShared(leaf, node);
            }
            try {
                int pos = leaf.getNodePos();
                mValue = pos < 0 ? null : node.retrieveLeafValue(pos);
            } catch (Throwable e) {
                node.releaseShared();
                throw e;
            }
            node.releaseShared();
            return result;
        } finally {
            if (locker != null) {
                locker.unlock();
            }
        }
    }

    private boolean tryLockLoad(LocalTransaction txn, byte[] key, int keyHash, boolean keyOnly,
                                CursorFrame leaf)
            throws IOException {
        Node node = leaf.tryAcquireShared();
        if (node != null) {
            if (node.mSplit != null) {
                node = finishSplitShared(leaf, node);
            }
            try {
                if (mTree.isLockAvailable(txn, key, keyHash)) {
                    int pos = leaf.getNodePos();
                    mValue = pos < 0 ? null
                            : keyOnly ? node.hasLeafValue(pos) : node.retrieveLeafValue(pos);
                    return true;
                }
            } finally {
                node.releaseShared();
            }
        }
        return false;
    }

    protected int storeMode() {
        return 0;
    }

    @Override
    public final void store(byte[] value) throws IOException {
        final LocalTransaction txn = mTxn;

        if (txn == null) {
            storeAutoCommit(value);
        } else {
            byte[] key = mKey;
            ViewUtils.positionCheck(key);
            try {
                if (txn.lockMode() != LockMode.UNSAFE) {
                    txn.lockExclusive(mTree.mId, key, keyHash());
                }
                if (storeMode() <= 1) {
                    storeAndRedo(txn, value);
                } else {
                    storeNoRedo(txn, value);
                }
            } catch (Throwable e) {
                throw handleException(e, false);
            }
        }
    }

    private void storeAutoCommit(byte[] value) throws IOException {
        byte[] key = mKey;
        ViewUtils.positionCheck(key);

        try {
            final LocalTransaction txn;
            int mode = storeMode();
            if (mode == 0) {
                txn = null;
            } else if (mode == 1) {
                LocalDatabase db = mTree.mDatabase;
                txn = db.threadLocalTransaction(db.mDurabilityMode.alwaysRedo());
                try {
                    txn.lockExclusive(mTree.mId, key, keyHash());
                    txn.storeCommit(true, this, value);
                    return;
                } catch (Throwable e) {
                    db.removeThreadLocalTransaction();
                    txn.reset();
                    throw e;
                }
            } else {
                txn = LocalTransaction.BOGUS;
            }

            final Locker locker = mTree.lockExclusiveLocal(key, keyHash());
            try {
                storeAndRedo(txn, value);
            } finally {
                locker.unlock();
            }
        } catch (Throwable e) {
            throw handleException(e, false);
        }
    }

    @Override
    public final void commit(byte[] value) throws IOException {
        final LocalTransaction txn = mTxn;

        if (txn == null) {
            storeAutoCommit(value);
        } else {
            byte[] key = mKey;
            ViewUtils.positionCheck(key);

            try {
                store:
                {
                    int mode = storeMode();
                    if (mode <= 1) storeRedo:{
                        if (txn.lockMode() != LockMode.UNSAFE) {
                            txn.lockExclusive(mTree.mId, key, keyHash());
                            if (txn.getDurabilityMode() == DurabilityMode.NO_REDO) {
                                break storeRedo;
                            }
                            txn.storeCommit(mode != 0, this, value);
                            return;
                        }
                        storeAndRedo(txn, value);
                        break store;
                    }
                    else {
                        if (txn.lockMode() != LockMode.UNSAFE) {
                            txn.lockExclusive(mTree.mId, key, keyHash());
                        }
                    }

                    storeNoRedo(txn, value);
                }

                txn.commit();
            } catch (Throwable e) {
                throw handleException(e, false);
            }
        }
    }

    final byte[] findAndStore(byte[] key, byte[] value) throws IOException {
        mKey = key;
        LocalTransaction txn = mTxn;

        try {
            if (txn == null) {
                final int hash = LockManager.hash(mTree.mId, key);
                mKeyHash = hash;
                int mode = storeMode();
                if (mode != 0) {
                    LocalDatabase db = mTree.mDatabase;
                    if (mode == 1) {
                        txn = db.threadLocalTransaction(db.mDurabilityMode.alwaysRedo());
                        //TODO 测试使用
                        //System.out.println("测试txn对象是否进行转变 ======>  " + txn);
                        try {
                            txn.lockExclusive(mTree.mId, key, hash);
                            byte[] result = doFindAndStore(txn, key, value);
                            txn.commit();
                            return result;
                        } catch (Throwable e) {
                            db.removeThreadLocalTransaction();
                            txn.reset();
                            throw e;
                        }
                    } else {
                        txn = LocalTransaction.BOGUS;
                    }
                }

                final Locker locker = mTree.lockExclusiveLocal(key, hash);
                try {
                    //无事务找数据并存储当前数据
                    return doFindAndStore(txn, key, value);
                } finally {
                    locker.unlock();
                }
            } else {
                if (txn.lockMode() == LockMode.UNSAFE) {
                    mKeyHash = 0;
                } else {
                    final int hash = LockManager.hash(mTree.mId, key);
                    mKeyHash = hash;
                    txn.lockExclusive(mTree.mId, key, hash);
                }
                //带事务找数据并存储当前数据
                return doFindAndStore(txn, key, value);
            }
        } catch (Throwable e) {
            throw handleException(e, false); // no reset on safe exception
        }
    }

    /**
     * 真实的 ---> 查找并存储
     *
     * @param txn
     * @param key
     * @param value
     * @return
     * @throws IOException
     */
    private byte[] doFindAndStore(LocalTransaction txn, byte[] key, byte[] value)
            throws IOException {
        find(null, key, VARIANT_CHECK, new CursorFrame(), latchRootNode());

        CursorFrame leaf = mFrame;
        Node node = leaf.getNode();

        CommitLock.Shared shared;
        byte[] originalValue;

        if (!node.tryUpgrade()) {
            node.releaseShared();
            leaf.acquireExclusive();
        }

        if (value == null) {
            shared = prepareDelete(leaf);

            if (shared == null) {
                return null;
            }

            int pos = leaf.getNodePos();
            node = leaf.getNode();

            try {
                originalValue = mKeyOnly ? node.hasLeafValue(pos) : node.retrieveLeafValue(pos);
            } catch (Throwable e) {
                node.releaseExclusive();
                shared.release();
                throw e;
            }

            deleteNoRedo(txn, leaf, shared);
        } else {
            //预写redo存储到叶子节点
            shared = prepareStore(leaf);

            int pos = leaf.getNodePos();

            if (pos < 0) {
                originalValue = null;
            } else {
                node = leaf.getNode();
                try {
                    originalValue = mKeyOnly ? node.hasLeafValue(pos)
                            : node.retrieveLeafValue(pos);
                } catch (Throwable e) {
                    node.releaseExclusive();
                    shared.release();
                    throw e;
                }
            }

            storeNoRedo(txn, leaf, shared, value);
        }

        if (storeMode() <= 1) {
            //存储redo文件value进行预写日志
            redoStore(txn, shared, value);
        } else {
            shared.release();
        }

        return originalValue;
    }

    static final byte[]
            MODIFY_INSERT = new byte[0], MODIFY_REPLACE = new byte[0], MODIFY_UPDATE = new byte[0];

    final boolean findAndModify(byte[] key, byte[] oldValue, byte[] newValue) throws IOException {
        LocalTransaction txn = mTxn;

        try {
            if (txn == null) {
                final int hash;
                if (key == null) {
                    key = mKey;
                    ViewUtils.positionCheck(key);
                    hash = keyHash();
                } else {
                    mKey = key;
                    mKeyHash = hash = LockManager.hash(mTree.mId, key);
                }

                int mode = storeMode();
                if (mode != 0) {
                    LocalDatabase db = mTree.mDatabase;
                    if (mode == 1) {
                        txn = db.threadLocalTransaction(db.mDurabilityMode.alwaysRedo());
                        try {
                            txn.lockExclusive(mTree.mId, key, hash);
                            boolean result = doFindAndModify(txn, key, oldValue, newValue);
                            txn.commit();
                            return result;
                        } catch (Throwable e) {
                            db.removeThreadLocalTransaction();
                            txn.reset();
                            throw e;
                        }
                    } else {
                        txn = LocalTransaction.BOGUS;
                    }
                }

                final Locker locker = mTree.lockExclusiveLocal(key, hash);
                try {
                    return doFindAndModify(txn, key, oldValue, newValue);
                } finally {
                    locker.unlock();
                }
            }

            LockResult result;

            LockMode mode = txn.lockMode();
            if (mode == LockMode.UNSAFE) {
                if (key == null) {
                    key = mKey;
                    ViewUtils.positionCheck(key);
                } else {
                    mKey = key;
                    mKeyHash = 0;
                }
                result = LockResult.OWNED_EXCLUSIVE;
            } else {
                final int hash;
                if (key == null) {
                    key = mKey;
                    ViewUtils.positionCheck(key);
                    hash = keyHash();
                } else {
                    mKey = key;
                    mKeyHash = hash = LockManager.hash(mTree.mId, key);
                }
                result = txn.lockExclusive(mTree.mId, key, hash);
                if (result == LockResult.ACQUIRED && mode.getRepeatable() != 0) {
                    result = LockResult.UPGRADED;
                }
            }

            try {
                if (doFindAndModify(txn, key, oldValue, newValue)) {
                    return true;
                }
            } catch (Throwable e) {
                try {
                    if (result == LockResult.ACQUIRED) {
                        txn.unlock();
                    } else if (result == LockResult.UPGRADED) {
                        txn.unlockToUpgradable();
                    }
                } catch (Throwable e2) {
                }

                throw e;
            }

            if (result == LockResult.ACQUIRED) {
                txn.unlock();
            } else if (result == LockResult.UPGRADED) {
                txn.unlockToUpgradable();
            }

            return false;
        } catch (Throwable e) {
            throw handleException(e, false);
        }
    }

    private boolean doFindAndModify(LocalTransaction txn,
                                    byte[] key, byte[] oldValue, byte[] newValue)
            throws IOException {
        CursorFrame leaf;

        if (key == null) {
            leaf = frameExclusive();
        } else {
            Node nn;
            find(null, key, VARIANT_CHECK, new CursorFrame(), nn = latchRootNode());

            leaf = mFrame;
            Node node = leaf.getNode();

            if (!node.tryUpgrade()) {
                node.releaseShared();
                leaf.acquireExclusive();
            }
        }

        CommitLock.Shared shared;
        if (newValue == null) {
            shared = prepareDelete(leaf);
            if (shared == null) {
                // Entry doesn't exist.
                return oldValue == null || oldValue == MODIFY_INSERT;
            }
        } else {
            shared = prepareStore(leaf);
        }

        Node node = leaf.getNode();
        int pos = leaf.getNodePos();

        byte[] originalValue;

        if (pos < 0) {
            originalValue = null;
        } else {
            try {
                originalValue = mKeyOnly ? node.hasLeafValue(pos) : node.retrieveLeafValue(pos);
            } catch (Throwable e) {
                node.releaseExclusive();
                shared.release();
                throw e;
            }
        }

        doStore:
        {
            check:
            {
                if (oldValue == MODIFY_INSERT) {
                    if (originalValue == null) {
                        // Insert allowed.
                        break check;
                    }
                } else if (oldValue == MODIFY_REPLACE) {
                    if (originalValue != null) {
                        // Replace allowed.
                        break check;
                    }
                } else if (oldValue == MODIFY_UPDATE) {
                    if (!Arrays.equals(originalValue, newValue)) {
                        // Update allowed.
                        break check;
                    }
                } else {
                    if (originalValue != null) {
                        if (Arrays.equals(oldValue, originalValue)) {
                            // Update allowed.
                            break check;
                        }
                    } else if (oldValue == null) {
                        if (newValue == null) {
                            // Update allowed, but nothing changed.
                            node.releaseExclusive();
                            break doStore;
                        } else {
                            // Update allowed.
                            break check;
                        }
                    }
                }

                node.releaseExclusive();
                shared.release();
                return false;
            }

            if (newValue == null) {
                if (pos < 0) {
                    node.releaseExclusive();
                    break doStore;
                }
                deleteNoRedo(txn, leaf, shared);
            } else {
                storeNoRedo(txn, leaf, shared, newValue);
            }

            if (storeMode() <= 1) {
                redoStore(txn, shared, newValue);
                return true;
            }
        }

        shared.release();

        return true;
    }

    public final boolean deleteGhost(byte[] key) throws IOException {
        try {
            find(null, key, VARIANT_CHECK, new CursorFrame(), latchRootNode());

            try {
                CursorFrame leaf = mFrame;
                Node node = leaf.getNode();

                if (!node.tryUpgrade()) {
                    node.releaseShared();
                    leaf.acquireExclusive();
                }

                CommitLock.Shared shared = prepareDelete(leaf);

                if (shared != null) {
                    node = leaf.getNode();
                    if (node.mPage == DirectPageOps.p_closedTreePage()) {
                        node.releaseExclusive();
                        shared.release();
                        return false;
                    }
                    if (node.hasLeafValue(leaf.getNodePos()) == null) {
                        deleteNoRedo(LocalTransaction.BOGUS, leaf, shared);
                    } else {
                        node.releaseExclusive();
                    }
                    shared.release();
                }

                return true;
            } finally {
                reset();
            }
        } catch (Throwable e) {
            throw handleException(e, true);
        }
    }

    public final void storeNoRedo(LocalTransaction txn, byte[] value) throws IOException {
        CursorFrame leaf = frameExclusive();
        CommitLock.Shared shared;

        if (value == null) {
            shared = prepareDelete(leaf);
            if (shared != null) {
                deleteNoRedo(txn, leaf, shared);
                shared.release();
            }
        } else {
            shared = prepareStore(leaf);
            storeNoRedo(txn, leaf, shared, value);
            shared.release();
        }

        mValue = value;
    }

    final void storeAndRedo(LocalTransaction txn, byte[] value) throws IOException {
        CursorFrame leaf = frameExclusive();
        CommitLock.Shared shared;

        if (value == null) {
            shared = prepareDelete(leaf);
            if (shared == null) {
                mValue = null;
                return;
            }
            deleteNoRedo(txn, leaf, shared);
        } else {
            shared = prepareStore(leaf);
            storeNoRedo(txn, leaf, shared, value);
        }

        mValue = value;

        redoStore(txn, shared, value);
    }

    private void redoStore(LocalTransaction txn, CommitLock.Shared shared, byte[] value)
            throws IOException {
        long commitPos;
        try {
            if (txn == null) {
                commitPos = mTree.redoStoreNullTxn(mKey, value);
            } else if (txn.getDurabilityMode() == DurabilityMode.NO_REDO) {
                return;
            } else if (txn.lockMode() != LockMode.UNSAFE) {
                long cursorId = mCursorId;
                if (cursorId == 0) {
                    txn.redoStore(mTree.mId, mKey, value);
                } else {
                    txn.redoCursorStore(cursorId & ~(1L << 63), mKey, value);
                    mCursorId = cursorId | (1L << 63);
                }
                return;
            } else {
                commitPos = mTree.redoStoreNoLock(mKey, value, txn.getDurabilityMode());
            }
        } finally {
            shared.release();
        }

        if (commitPos != 0) {
            mTree.txnCommitSync(txn, commitPos);
        }
    }

    private CommitLock.Shared prepareDelete(CursorFrame leaf) throws IOException {
        if (leaf.getNodePos() < 0) {
            leaf.getNode().releaseExclusive();
            return null;
        }

        CommitLock commitLock = mTree.mDatabase.commitLock();
        CommitLock.Shared shared = commitLock.tryAcquireShared();

        if (shared == null) {
            leaf.getNode().releaseExclusive();
            shared = commitLock.acquireShared();
            leaf.acquireExclusive();

            if (leaf.getNodePos() < 0) {
                leaf.getNode().releaseExclusive();
                shared.release();
                return null;
            }
        }

        try {
            notSplitDirty(leaf);
        } catch (Throwable e) {
            shared.release();
            throw e;
        }

        if (leaf.getNodePos() < 0) {
            leaf.getNode().releaseExclusive();
            shared.release();
            return null;
        }

        return shared;
    }

    private void deleteNoRedo(LocalTransaction txn, CursorFrame leaf, CommitLock.Shared shared)
            throws IOException {
        try {
            Node node = leaf.getNode();
            int pos = leaf.getNodePos();
            byte[] key = mKey;

            try {
                if (txn != null && txn.lockMode() != LockMode.UNSAFE) {
                    node.txnDeleteLeafEntry(txn, mTree, key, keyHash(), pos);
                } else {
                    node.deleteLeafEntry(pos);
                    node.postDelete(pos, key);
                }
            } catch (Throwable e) {
                node.releaseExclusive();
                throw e;
            }

            if (node.shouldLeafMerge()) {
                mergeLeaf(leaf, node);
            } else {
                node.releaseExclusive();
            }
        } catch (Throwable e) {
            shared.release();
            DatabaseException.rethrowIfRecoverable(e);
            if (txn != null) {
                txn.reset(e);
            }
            throw e;
        }
    }

    private CommitLock.Shared prepareStore(CursorFrame leaf) throws IOException {
        CommitLock commitLock = mTree.mDatabase.commitLock();
        CommitLock.Shared shared = commitLock.tryAcquireShared();

        if (shared == null) {
            leaf.getNode().releaseExclusive();
            shared = commitLock.acquireShared();
            leaf.acquireExclusive();
        }

        try {
            notSplitDirty(leaf);
        } catch (Throwable e) {
            shared.release();
            throw e;
        }

        return shared;
    }

    private void storeNoRedo(LocalTransaction txn, CursorFrame leaf, CommitLock.Shared shared,
                             byte[] value)
            throws IOException {
        try {
            Node node = leaf.getNode();
            int pos = leaf.getNodePos();
            byte[] key = mKey;

            if (pos >= 0) {

                try {
                    if (txn != null && txn.lockMode() != LockMode.UNSAFE) {
                        node.txnPreUpdateLeafEntry(txn, mTree, key, pos);
                    }
                    node.updateLeafValue(leaf, mTree, pos, 0, value);
                } catch (Throwable e) {
                    node.releaseExclusive();
                    throw e;
                }

                if (node.shouldLeafMerge()) {
                    mergeLeaf(leaf, node);
                } else {
                    if (node.mSplit != null) {
                        node = mTree.finishSplit(leaf, node);
                    }
                    node.releaseExclusive();
                }
            } else {
                try {
                    if (txn != null && txn.lockMode() != LockMode.UNSAFE) {
                        txn.pushUninsert(mTree.mId, key);
                    }
                    node.insertLeafEntry(leaf, mTree, ~pos, key, value);
                } catch (Throwable e) {
                    node.releaseExclusive();
                    throw e;
                }

                node = postInsert(leaf, node, key);

                node.releaseExclusive();
            }
        } catch (Throwable e) {
            shared.release();
            DatabaseException.rethrowIfRecoverable(e);
            if (txn != null) {
                txn.reset(e);
            }
            throw e;
        }
    }

    private Node postInsert(CursorFrame leaf, Node node, byte[] key) throws IOException {
        int pos = leaf.getNodePos();
        int newPos = ~pos;

        leaf.setNodePos(newPos);
        leaf.setNotFoundKey(null);

        CursorFrame frame = node.mLastCursorFrame;
        do {
            if (frame == leaf) {
                continue;
            }

            int framePos = frame.getNodePos();

            if (framePos == pos) {

                byte[] frameKey = frame.getNotFoundKey();
                if (frameKey != null) {
                    int compare = compareUnsigned(frameKey, key);
                    if (compare > 0) {
                        frame.setNodePos(framePos - 2);
                    } else if (compare == 0) {
                        frame.setNodePos(newPos);
                        frame.setNotFoundKey(null);
                    }
                }
            } else if (framePos >= newPos) {
                frame.setNodePos(framePos + 2);
            } else if (framePos < pos) {
                frame.setNodePos(framePos - 2);
            }
        } while ((frame = frame.getPrevCousin()) != null);

        if (node.mSplit != null) {
            node = mTree.finishSplit(leaf, node);
        }

        return node;
    }

    final void storeFragmented(byte[] value) throws IOException {
        ViewUtils.positionCheck(mKey);
        if (value == null) {
            throw new IllegalArgumentException("Value is null");
        }

        final CursorFrame leaf = frameExclusive();
        final CommitLock.Shared shared = prepareStore(leaf);
        Node node = leaf.getNode();

        try {
            final int pos = leaf.getNodePos();
            if (pos >= 0) {
                try {
                    node.updateLeafValue(leaf, mTree, pos, Node.ENTRY_FRAGMENTED, value);
                } catch (Throwable e) {
                    node.releaseExclusive();
                    throw e;
                }
                if (node.mSplit != null) {
                    node = mTree.finishSplit(leaf, node);
                }
            } else {
                byte[] key = mKey;
                try {
                    node.insertFragmentedLeafEntry(leaf, mTree, ~pos, key, value);
                } catch (Throwable e) {
                    node.releaseExclusive();
                    throw e;
                }
                node = postInsert(leaf, node, key);
            }

            mValue = NOT_LOADED;

            node.releaseExclusive();
        } catch (Throwable e) {
            throw handleException(e, false);
        } finally {
            shared.release();
        }
    }

    final Node insertBlank(CursorFrame leaf, Node node, long vlength) throws IOException {
        byte[] key = mKey;
        try {
            node.insertBlankLeafEntry(leaf, mTree, ~leaf.getNodePos(), key, vlength);
        } catch (Throwable e) {
            node.releaseExclusive();
            throw e;
        }
        return postInsert(leaf, node, key);
    }

    final boolean deleteAll() throws IOException {
        autoload(false);

        final LocalDatabase db = mTree.mDatabase;
        final CommitLock commitLock = db.commitLock();

        CommitLock.Shared shared = commitLock.acquireShared();
        try {
            if (db.isClosed()) {
                return false;
            }
            firstLeaf();
        } finally {
            shared.release();
        }

        while (true) {
            shared = commitLock.acquireShared();
            try {
                if (db.isClosed()) {
                    return false;
                }

                mFrame.acquireExclusive();

                Node node = notSplitDirty(mFrame);

                if (node.hasKeys()) {
                    try {
                        node.deleteLeafEntry(0);
                    } catch (Throwable e) {
                        node.releaseExclusive();
                        throw e;
                    }

                    if (node.hasKeys()) {
                        node.releaseExclusive();
                        continue;
                    }
                }

                if (!deleteLowestNode(mFrame, node)) {
                    mFrame = null;
                    reset();
                    return true;
                }
            } finally {
                shared.release();
            }
        }
    }

    final void deleteNext() throws IOException {
        final LocalDatabase db = mTree.mDatabase;
        final CommitLock commitLock = db.commitLock();

        Node node;

        while (true) {
            CommitLock.Shared shared = commitLock.acquireShared();
            try {
                if (db.isClosed()) {
                    throw new ClosedIndexException();
                }

                mFrame.acquireExclusive();

                node = notSplitDirty(mFrame);

                if (node.hasKeys()) {
                    try {
                        node.deleteLeafEntry(0);
                    } catch (Throwable e) {
                        node.releaseExclusive();
                        throw e;
                    }

                    if (node.hasKeys()) {
                        break;
                    }
                }

                if (!deleteLowestNode(mFrame, node)) {
                    mFrame = null;
                    reset();
                    return;
                }

                mFrame.acquireExclusive();

                node = notSplitDirty(mFrame);

                if (node.hasKeys()) {
                    break;
                }
            } finally {
                shared.release();
            }
        }

        try {
            mKey = node.retrieveKey(0);
            if (!mKeyOnly) {
                mValue = node.retrieveLeafValue(0);
            }
        } finally {
            node.releaseExclusive();
        }
    }

    private boolean deleteLowestNode(final CursorFrame frame, final Node node) throws IOException {
        node.mLastCursorFrame = null;

        LocalDatabase db = mTree.mDatabase;

        if (node == mTree.mRoot) {
            try {
                node.asTrimmedRoot();
            } finally {
                node.releaseExclusive();
            }
            return false;
        }

        db.prepareToDelete(node);

        CursorFrame parentFrame = frame.getParentFrame();
        Node parentNode = parentFrame.acquireExclusive();

        if (parentNode.hasKeys()) {
            parentNode.deleteLeftChildRef(0);
        } else {
            if (!deleteLowestNode(parentFrame, parentNode)) {
                db.finishDeleteNode(node);
                return false;
            }
            parentNode = parentFrame.acquireExclusive();
        }

        Node next = mTree.mDatabase.latchChildRetainParentEx(parentNode, 0, true);

        try {
            if (db.markDirty(mTree, next)) {
                parentNode.updateChildRefId(0, next.mId);
            }
        } finally {
            parentNode.releaseExclusive();
        }

        frame.setNode(next);
        frame.setNodePos(0);
        next.mLastCursorFrame = frame;
        next.type((byte) (next.type() | Node.LOW_EXTREMITY));
        next.releaseExclusive();

        db.finishDeleteNode(node);

        return true;
    }

    final void deletePrevious() throws IOException {
        final LocalDatabase db = mTree.mDatabase;
        final CommitLock commitLock = db.commitLock();

        Node node;

        while (true) {
            CommitLock.Shared shared = commitLock.acquireShared();
            try {
                if (db.isClosed()) {
                    throw new ClosedIndexException();
                }

                mFrame.acquireExclusive();

                node = notSplitDirty(mFrame);

                if (node.hasKeys()) {
                    try {
                        node.deleteLeafEntry(node.highestLeafPos());
                    } catch (Throwable e) {
                        node.releaseExclusive();
                        throw e;
                    }

                    if (node.hasKeys()) {
                        break;
                    }
                }

                if (!deleteHighestNode(mFrame, node)) {
                    mFrame = null;
                    reset();
                    return;
                }

                mFrame.acquireExclusive();

                node = notSplitDirty(mFrame);

                if (node.hasKeys()) {
                    break;
                }
            } finally {
                shared.release();
            }
        }

        try {
            int pos = node.highestLeafPos();
            mKey = node.retrieveKey(pos);
            if (!mKeyOnly) {
                mValue = node.retrieveLeafValue(pos);
            }
        } finally {
            node.releaseExclusive();
        }
    }

    private boolean deleteHighestNode(final CursorFrame frame, final Node node) throws IOException {
        node.mLastCursorFrame = null;

        LocalDatabase db = mTree.mDatabase;

        if (node == mTree.mRoot) {
            try {
                node.asTrimmedRoot();
            } finally {
                node.releaseExclusive();
            }
            return false;
        }

        db.prepareToDelete(node);

        CursorFrame parentFrame = frame.getParentFrame();
        Node parentNode = parentFrame.acquireExclusive();

        if (parentNode.hasKeys()) {
            parentNode.deleteRightChildRef(parentNode.highestInternalPos());
        } else {
            if (!deleteHighestNode(parentFrame, parentNode)) {
                db.finishDeleteNode(node);
                return false;
            }
            parentNode = parentFrame.acquireExclusive();
        }

        int pos = parentNode.highestInternalPos();
        Node previous = mTree.mDatabase.latchChildRetainParentEx(parentNode, pos, true);

        try {
            if (db.markDirty(mTree, previous)) {
                parentNode.updateChildRefId(pos, previous.mId);
            }
        } finally {
            parentNode.releaseExclusive();
        }

        frame.setNode(previous);
        frame.setNodePos(previous.highestPos());
        previous.mLastCursorFrame = frame;
        previous.type((byte) (previous.type() | Node.HIGH_EXTREMITY));
        previous.releaseExclusive();

        db.finishDeleteNode(node);

        return true;
    }

    private int randomPosition(ThreadLocalRandom rnd, Node node, byte[] lowKey, byte[] highKey)
            throws IOException {
        int pos = 0;
        if (highKey == null) {
            pos = node.highestPos() + 2;
        } else {
            pos = node.binarySearch(highKey);
            if (pos < 0) {
                pos = ~pos;
            }
            if (!node.isLeaf()) {
                pos += 2;
            }
        }

        if (lowKey == null) {
            if (pos > 0) {
                pos = (pos == 2) ? 0 : (rnd.nextInt(pos >> 1) << 1);
                return pos;
            }
        } else {
            int lowPos = node.binarySearch(lowKey);
            if (!node.isLeaf()) {
                lowPos = Node.internalPos(lowPos);
            } else if (lowPos < 0) {
                lowPos = ~lowPos;
            }
            int range = pos - lowPos;
            if (range > 0) {
                pos = (range == 2) ? lowPos : lowPos + (rnd.nextInt(range >> 1) << 1);
                return pos;
            }
        }
        return -1;
    }

    private boolean isRangeEmpty(byte[] lowKey, byte[] highKey) throws IOException {
        boolean oldKeyOnly = mKeyOnly;
        LocalTransaction oldTxn = mTxn;
        try {
            mTxn = LocalTransaction.BOGUS;
            mKeyOnly = true;
            if (lowKey == null) {
                first();
            } else {
                findGe(lowKey);
            }
            if (mKey == null || (highKey != null && Utils.compareUnsigned(mKey, highKey) >= 0)) {
                return true;
            }
            return false;
        } finally {
            reset();
            mKeyOnly = oldKeyOnly;
            mTxn = oldTxn;
        }
    }

    private IOException handleException(Throwable e, boolean reset) throws IOException {
        mTree.mDatabase.checkClosed();

        if (mFrame == null && e instanceof IllegalStateException) {
            if (reset) {
                reset();
            }
            throw (IllegalStateException) e;
        }

        if (e instanceof DatabaseException) {
            DatabaseException de = (DatabaseException) e;
            if (de.isRecoverable()) {
                if (reset) {
                    reset();
                }
                throw de;
            }
        }

        try {
            throw closeOnFailure(mTree.mDatabase, e);
        } finally {
            reset();
        }
    }

    @Override
    public final long valueLength() throws IOException {
        CursorFrame frame;
        try {
            frame = frameSharedNotSplit();
        } catch (IllegalStateException e) {
            valueCheckOpen();
            throw e;
        }

        long result = TreeValue.action(null, this, frame, TreeValue.OP_LENGTH, 0, null, 0, 0);
        frame.getNode().releaseShared();
        return result;
    }

    @Override
    public final void valueLength(long length) throws IOException {
        try {
            if (length <= 0) {
                store(length == 0 ? Utils.EMPTY_BYTES : null);
            } else {
                doValueModify(storeMode(), TreeValue.OP_SET_LENGTH, length, Utils.EMPTY_BYTES, 0, 0);
            }
        } catch (IllegalStateException e) {
            valueCheckOpen();
            throw e;
        }
    }

    @Override
    public final int doValueRead(long pos, byte[] buf, int off, int len) throws IOException {
        CursorFrame frame;
        try {
            frame = frameSharedNotSplit();
        } catch (IllegalStateException e) {
            valueCheckOpen();
            throw e;
        }

        long result = TreeValue.action(null, this, frame, TreeValue.OP_READ, pos, buf, off, len);
        frame.getNode().releaseShared();
        return (int) result;
    }

    @Override
    public final void doValueWrite(long pos, byte[] buf, int off, int len) throws IOException {
        try {
            doValueModify(storeMode(), TreeValue.OP_WRITE, pos, buf, off, len);
        } catch (IllegalStateException e) {
            valueCheckOpen();
            throw e;
        }
    }

    @Override
    public final void doValueClear(long pos, long length) throws IOException {
        try {
            doValueModify(storeMode(), TreeValue.OP_CLEAR, pos, Utils.EMPTY_BYTES, 0, length);
        } catch (IllegalStateException e) {
            valueCheckOpen();
            throw e;
        }
    }

    private void doValueModify(int mode, int op, long pos, byte[] buf, int off, long len)
            throws IOException {
        LocalTransaction txn = mTxn;

        if (txn == null) {
            LocalDatabase db = mTree.mDatabase;

            if (mode > 1) {
                txn = db.threadLocalTransaction(DurabilityMode.NO_REDO);
            } else {
                DurabilityMode durabilityMode = db.mDurabilityMode;
                if (mode != 0) {
                    txn = db.threadLocalTransaction(durabilityMode.alwaysRedo());
                } else {
                    byte[] key = mKey;
                    ViewUtils.positionCheck(key);
                    txn = db.threadLocalTransaction(durabilityMode);
                    txn.setLockMode(LockMode.UNSAFE);
                    txn.lockExclusive(mTree.mId, key, keyHash());
                }
            }

            try {
                mTxn = txn;
                doValueModify(mode, op, pos, buf, off, len);
                txn.commit();
            } catch (Throwable e) {
                db.removeThreadLocalTransaction();
                txn.reset();
                throw e;
            } finally {
                mTxn = null;
            }

            return;
        }

        byte[] key = mKey;
        ViewUtils.positionCheck(key);

        LocalTransaction undoTxn = null;

        if (txn.lockMode() != LockMode.UNSAFE) {
            txn.lockExclusive(mTree.mId, key, keyHash());
            undoTxn = txn;
        }

        final CursorFrame leaf = frameExclusive();
        final CommitLock.Shared shared = prepareStore(leaf);

        try {
            TreeValue.action(undoTxn, this, leaf, op, pos, buf, off, len);
            Node node = leaf.getNode();

            if (op == TreeValue.OP_SET_LENGTH && node.shouldLeafMerge()) {
                mergeLeaf(leaf, node);
            } else {
                node.releaseExclusive();
            }

            if (mode <= 1 && txn.durabilityMode() != DurabilityMode.NO_REDO) {
                txn.redoCursorValueModify(this, op, pos, buf, off, len);
            }
        } finally {
            shared.release();
        }
    }

    @Override
    public final int valueStreamBufferSize(int bufferSize) {
        if (bufferSize <= 1) {
            if (bufferSize < 0) {
                bufferSize = mTree.mDatabase.mPageSize;
            } else {
                bufferSize = 1;
            }
        }
        return bufferSize;
    }

    @Override
    public final void valueCheckOpen() {
        if (mKey == null) {
            throw new IllegalStateException("Accessor closed");
        }
    }

    @Override
    public final TreeCursor copy() {
        TreeCursor copy = copyNoValue();
        copy.mKeyOnly = mKeyOnly;
        copy.mValue = ViewUtils.copyValue(mValue);
        return copy;
    }

    private TreeCursor copyNoValue() {
        TreeCursor copy = new TreeCursor(mTree, mTxn);
        CursorFrame frame = mFrame;
        if (frame != null) {
            CursorFrame frameCopy = new CursorFrame();
            frame.copyInto(frameCopy);
            copy.mFrame = frameCopy;
        }
        copy.mKey = mKey;
        copy.mKeyHash = mKeyHash;
        return copy;
    }

    private Node latchRootNode() {
        Node root = mTree.mRoot;
        root.acquireShared();
        return root;
    }

    @Override
    public final void reset() {
        mKey = null;
        mKeyHash = 0;
        mValue = null;

        CursorFrame frame = mFrame;
        mFrame = null;

        if (frame != null) {
            CursorFrame.popAll(frame);
        }

        unregister();
    }

    private void resetLatched(Node node) {
        node.releaseShared();
        reset();
    }

    private RuntimeException cleanup(Throwable e, CursorFrame frame) {
        mFrame = frame;
        reset();
        return rethrow(e);
    }

    @Override
    public final void close() {
        reset();
    }

    @Override
    public final void close(Throwable cause) {
        try {
            if (cause instanceof DatabaseException) {
                DatabaseException de = (DatabaseException) cause;
                if (de.isRecoverable()) {
                    return;
                }
            }
            throw closeOnFailure(mTree.mDatabase, cause);
        } catch (IOException e) {
            // Ignore.
        } finally {
            reset();
        }
    }

    final void appendTransfer(Node source) throws IOException {
        try {
            final CursorFrame tleaf = mFrame;
            Node tnode = tleaf.acquireExclusive();
            tnode = notSplitDirty(tleaf);

            try {
                final long spage = source.mPage;
                final int sloc = DirectPageOps.p_ushortGetLE(spage, source.searchVecStart());
                final int encodedLen = Node.leafEntryLengthAtLoc(spage, sloc);

                final int tpos = tleaf.getNodePos();
                final int tloc = tnode.createLeafEntry(null, mTree, tpos, encodedLen);

                if (tloc < 0) {
                    tnode.splitLeafAscendingAndCopyEntry(mTree, source, 0, encodedLen, tpos);
                    tnode = mTree.finishSplit(tleaf, tnode);
                } else {
                    DirectPageOps.p_copy(spage, sloc, tnode.mPage, tloc, encodedLen);
                }
                tleaf.setNodePos(tleaf.getNodePos() + 2);
            } finally {
                tnode.releaseExclusive();
            }

            int searchVecStart = source.searchVecStart();
            int searchVecEnd = source.searchVecEnd();

            if (searchVecStart == searchVecEnd) {
                source.searchVecEnd(searchVecEnd - 2);
            } else {
                source.searchVecStart(searchVecStart + 2);
            }
        } catch (Throwable e) {
            throw handleException(e, false);
        }
    }

    final void appendTransfer(TreeCursor source) throws IOException {
        final CommitLock.Shared shared = mTree.mDatabase.commitLock().acquireShared();
        CursorFrame sleaf;
        try {
            final CursorFrame tleaf = mFrame;
            Node tnode = tleaf.acquireExclusive();
            tnode = notSplitDirty(tleaf);

            sleaf = source.mFrame;
            Node snode = sleaf.acquireExclusive();

            try {
                snode = source.notSplitDirty(sleaf);
                final int spos = sleaf.getNodePos();

                try {
                    final long spage = snode.mPage;
                    final int sloc = DirectPageOps.p_ushortGetLE(spage, snode.searchVecStart() + spos);
                    final int encodedLen = Node.leafEntryLengthAtLoc(spage, sloc);

                    final int tpos = tleaf.getNodePos();
                    final int tloc = tnode.createLeafEntry(null, mTree, tpos, encodedLen);

                    if (tloc < 0) {
                        tnode.splitLeafAscendingAndCopyEntry(mTree, snode, spos, encodedLen, tpos);
                        tnode = mTree.finishSplit(tleaf, tnode);
                    } else {
                        DirectPageOps.p_copy(spage, sloc, tnode.mPage, tloc, encodedLen);
                    }

                    tleaf.setNodePos(tleaf.getNodePos() + 2);

                    snode.finishDeleteLeafEntry(spos, encodedLen);
                    snode.postDelete(spos, null);
                } catch (Throwable e) {
                    snode.releaseExclusive();
                    throw e;
                }
            } finally {
                tnode.releaseExclusive();
            }

            if (snode.hasKeys()) {
                snode.downgrade();
            } else {
                source.mergeLeaf(sleaf, snode);
                sleaf = source.frameSharedNotSplit();
            }
        } catch (Throwable e) {
            throw handleException(e, false);
        } finally {
            shared.release();
        }

        source.next(LocalTransaction.BOGUS, sleaf);
    }

    private void doUnregister(LocalTransaction txn, long cursorId) {
        cursorId &= ~(1L << 63);

        try {
            LocalDatabase db = mTree.mDatabase;

            TransactionContext context;
            RedoWriter redo;
            if (txn == null) {
                context = db.anyTransactionContext();
                redo = db.txnRedoWriter();
            } else {
                context = txn.getContext();
                redo = txn.getRedo();
            }

            context.redoCursorUnregister(redo, cursorId);

            db.unregisterCursor(this);
        } catch (UnmodifiableReplicaException e) {
            // Ignore.
        } catch (IOException e) {
            throw rethrow(e);
        }
    }

    final int height() {
        int height = 0;
        CursorFrame frame = mFrame;
        while (frame != null) {
            height++;
            frame = frame.getParentFrame();
        }
        return height;
    }

    final boolean compact(long highestNodeId, CompactionObserver observer) throws IOException {
        int height = height();

        Node[] frameNodes = new Node[height];

        CursorFrame frame = mFrame;

        outer:
        while (true) {
            for (int level = 0; level < height; level++) {
                Node node = frame.acquireShared();
                if (frameNodes[level] == node) {
                    node.releaseShared();
                    break;
                } else {
                    frameNodes[level] = node;
                    long id = compactFrame(highestNodeId, frame, node);
                    if (id > highestNodeId) {
                        return false;
                    }
                    try {
                        if (!observer.indexNodeVisited(id)) {
                            return false;
                        }
                    } catch (Throwable e) {
                        uncaught(e);
                        return false;
                    }
                }
                frame = frame.getParentFrame();
            }

            frame = frameSharedNotSplit();
            Node node = frame.getNode();

            quick:
            {
                final int end = node.highestLeafPos();
                int pos = frame.getNodePos();
                if (pos < 0) {
                    pos = ~pos;
                }
                for (; pos <= end; pos += 2) {
                    if (node.isFragmentedLeafValue(pos)) {
                        break quick;
                    }
                }
                node.releaseShared();
                skipToNextLeaf();
                if ((frame = mFrame) == null) {
                    return true;
                }
                continue outer;
            }

            while (true) {
                try {
                    int nodePos = frame.getNodePos();
                    if (nodePos >= 0 && node.isFragmentedLeafValue(nodePos)) {
                        int pLen = pageSize(node.mPage);
                        long pos = 0;
                        while (true) {
                            int result = TreeValue.compactCheck(frame, pos, highestNodeId);

                            if (result < 0) {
                                break;
                            }

                            if (result > 0) {
                                Node n = node;
                                node = null;

                                if (!n.tryUpgrade()) {
                                    n.releaseShared();
                                    frame.acquireExclusive();
                                }

                                CommitLock.Shared shared = prepareStore(frame);

                                try {
                                    TreeValue.action(null, this, frame, TreeValue.OP_WRITE,
                                            pos, TreeValue.TOUCH_VALUE, 0, 0);
                                } finally {
                                    shared.release();
                                }

                                node = frame.getNode();
                                node.downgrade();

                                if (node.mId > highestNodeId) {
                                    return false;
                                }
                            }

                            pos += pLen;
                        }
                    }
                } finally {
                    if (node != null) {
                        node.releaseShared();
                    }
                }

                nextLeaf();

                if (mFrame == null) {
                    return true;
                }

                frame = frameSharedNotSplit();
                Node next = frame.getNode();

                if (next != node) {
                    next.releaseShared();
                    break;
                }
            }
        }
    }

    private long compactFrame(long highestNodeId, CursorFrame frame, Node node)
            throws IOException {
        long id = node.mId;
        node.releaseShared();

        if (id > highestNodeId) {
            LocalDatabase db = mTree.mDatabase;
            CommitLock.Shared shared = db.commitLock().acquireShared();
            try {
                node = frame.acquireExclusive();
                id = node.mId;
                if (id > highestNodeId) {
                    node = notSplitDirty(frame);
                    id = node.mId;
                }
                node.releaseExclusive();
            } finally {
                shared.release();
            }
        }

        return id;
    }

    public final boolean equalPositions(TreeCursor other) {
        if (this == other) {
            return true;
        }

        CursorFrame thisFrame = mFrame;
        CursorFrame otherFrame = other.mFrame;
        while (true) {
            if (thisFrame == null) {
                return otherFrame == null;
            } else if (otherFrame == null) {
                return false;
            }
            if (thisFrame.getNode() != otherFrame.getNode()) {
                return false;
            }
            if (thisFrame.getNodePos() != otherFrame.getNodePos()) {
                return false;
            }
            thisFrame = thisFrame.getParentFrame();
            otherFrame = otherFrame.getParentFrame();
        }
    }

    public final boolean verifyExtremities(byte extremity) throws IOException {
        Node node = latchRootNode();
        try {
            while (true) {
                if ((node.type() & extremity) == 0) {
                    return false;
                }
                if (node.isLeaf()) {
                    return true;
                }
                int pos = 0;
                if (extremity == Node.HIGH_EXTREMITY) {
                    pos = node.highestInternalPos();
                }
                node = mTree.mDatabase.latchToChild(node, pos);
            }
        } finally {
            node.releaseShared();
        }
    }

    final boolean verify(final int height, VerificationObserver observer) throws IOException {
        if (height > 0) {
            final Node[] stack = new Node[height];
            while (key() != null) {
                if (!verifyFrames(height, stack, mFrame, observer)) {
                    return false;
                }
                skipToNextLeaf();
            }
        }
        return true;
    }

    @SuppressWarnings("fallthrough")
    private boolean verifyFrames(int level, Node[] stack, CursorFrame frame,
                                 VerificationObserver observer)
            throws IOException {
        CursorFrame parentFrame = frame.getParentFrame();
        Node childNode;

        if (parentFrame == null) {
            childNode = frame.acquireShared();
        } else {
            Node parentNode = parentFrame.getNode();
            int parentLevel = level - 1;
            if (parentLevel > 0 && stack[parentLevel] != parentNode) {
                parentNode = parentFrame.acquireShared();
                parentNode.releaseShared();
                if (stack[parentLevel] != parentNode) {
                    stack[parentLevel] = parentNode;
                    if (!verifyFrames(parentLevel, stack, parentFrame, observer)) {
                        return false;
                    }
                }
            }

            parentNode = parentFrame.acquireShared();
            try {
                childNode = frame.acquireShared();

                boolean result;
                try {
                    result = verifyParentChildFrames
                            (level, parentFrame, parentNode, frame, childNode, observer);
                } catch (Throwable e) {
                    childNode.releaseShared();
                    throw e;
                }

                if (!result) {
                    childNode.releaseShared();
                    return false;
                }
            } finally {
                parentNode.releaseShared();
            }
        }

        return childNode.verifyTreeNode(level, observer);
    }

    private boolean verifyParentChildFrames(int level,
                                            CursorFrame parentFrame, Node parentNode,
                                            CursorFrame childFrame, Node childNode,
                                            VerificationObserver observer)
            throws IOException {
        final long childId = childNode.mId;


        if (childNode.hasKeys() && parentNode.hasKeys()
                && childNode.mSplit == null && parentNode.mSplit == null) {
            int parentPos = parentFrame.getNodePos();

            int childPos;
            boolean left;
            if (parentPos >= parentNode.highestInternalPos()) {
                parentPos = parentNode.highestKeyPos();
                childPos = 0;
                left = false;
            } else {
                childPos = childNode.highestKeyPos();
                left = true;
            }

            byte[] parentKey = parentNode.retrieveKey(parentPos);
            byte[] childKey = childNode.retrieveKey(childPos);

            int compare = compareUnsigned(childKey, parentKey);

            if (left) {
                if (compare >= 0) {
                    observer.setFailed(true);
                    if (!observer.indexNodeFailed
                            (childId, level, "Child keys are not less than parent key: " + parentNode)) {
                        return false;
                    }
                }
            } else if (childNode.isInternal()) {
                if (compare <= 0) {
                    observer.setFailed(true);
                    if (!observer.indexNodeFailed
                            (childId, level,
                                    "Internal child keys are not greater than parent key: " + parentNode)) {
                        return false;
                    }
                }
            } else if (compare < 0) {
                observer.setFailed(true);
                if (!observer.indexNodeFailed
                        (childId, level,
                                "Child keys are not greater than or equal to parent key: " + parentNode)) {
                    return false;
                }
            }

            if ((childNode.type() & Node.LOW_EXTREMITY) != 0
                    && (parentNode.type() & Node.LOW_EXTREMITY) == 0) {
                observer.setFailed(true);
                if (!observer.indexNodeFailed
                        (childId, level, "Child is low extremity but parent is not: " + parentNode)) {
                    return false;
                }
            }

            if ((childNode.type() & Node.HIGH_EXTREMITY) != 0
                    && (parentNode.type() & Node.HIGH_EXTREMITY) == 0) {
                observer.setFailed(true);
                if (!observer.indexNodeFailed
                        (childId, level, "Child is high extremity but parent is not: " + parentNode)) {
                    return false;
                }
            }
        }

        switch (parentNode.type()) {
            case Node.TYPE_TN_IN:
                if (childNode.isLeaf() && parentNode.mId > 1) {
                    observer.setFailed(true);
                    if (!observer.indexNodeFailed
                            (childId, level,
                                    "Child is a leaf, but parent is a regular internal node: " + parentNode)) {
                        return false;
                    }
                }
                break;
            case Node.TYPE_TN_BIN:
                if (!childNode.isLeaf()) {
                    observer.setFailed(true);
                    if (!observer.indexNodeFailed
                            (childId, level,
                                    "Child is not a leaf, but parent is a bottom internal node: " + parentNode)) {
                        return false;
                    }
                }
                break;
            default:
                if (!parentNode.isLeaf()) {
                    break;
                }
            case Node.TYPE_TN_LEAF:
                observer.setFailed(true);
                if (!observer.indexNodeFailed
                        (childId, level, "Child parent is a leaf node: " + parentNode)) {
                    return false;
                }
                break;
        }

        return true;
    }

    private CursorFrame frame() {
        CursorFrame frame = mFrame;
        ViewUtils.positionCheck(frame);
        return frame;
    }

    protected final CursorFrame frameExclusive() {
        CursorFrame frame = frame();
        frame.acquireExclusive();
        return frame;
    }

    final CursorFrame frameSharedNotSplit() throws IOException {
        CursorFrame frame = frame();
        Node node = frame.acquireShared();
        if (node.mSplit != null) {
            finishSplitShared(frame, node);
        }
        return frame;
    }

    final Node finishSplitShared(final CursorFrame frame, Node node) throws IOException {
        doSplit:
        {
            CommitLock commitLock = mTree.mDatabase.commitLock();
            CommitLock.Shared shared = commitLock.tryAcquireShared();
            try {
                if (shared == null || !node.tryUpgrade()) {
                    node.releaseShared();
                    if (shared == null) {
                        shared = commitLock.acquireShared();
                    }
                    node = frame.acquireExclusive();
                    if (node.mSplit == null) {
                        break doSplit;
                    }
                }
                node = mTree.finishSplit(frame, node);
            } finally {
                if (shared != null) {
                    shared.release();
                }
            }
        }
        node.downgrade();
        return node;
    }

    final Node notSplitDirty(final CursorFrame frame) throws IOException {
        Node node = frame.getNode();

        while (true) {
            if (node.mSplit != null) {
                return mTree.finishSplit(frame, node);
            }

            LocalDatabase db = mTree.mDatabase;
            if (!db.shouldMarkDirty(node)) {
                return node;
            }

            CursorFrame parentFrame = frame.getParentFrame();
            if (parentFrame == null) {
                try {
                    db.doMarkDirty(mTree, node);
                    return node;
                } catch (Throwable e) {
                    node.releaseExclusive();
                    throw e;
                }
            }

            Node parentNode = parentFrame.tryAcquireExclusive();

            if (parentNode != null) {

                if (parentNode.mSplit == null && !db.shouldMarkDirty(parentNode)) {
                    try {
                        db.doMarkDirty(mTree, node);
                        parentNode.updateChildRefId(parentFrame.getNodePos(), node.mId);
                        return node;
                    } catch (Throwable e) {
                        node.releaseExclusive();
                        throw e;
                    } finally {
                        parentNode.releaseExclusive();
                    }
                }

                node.releaseExclusive();
            } else {
                node.releaseExclusive();
                parentFrame.acquireExclusive();
            }

            notSplitDirty(parentFrame).releaseExclusive();

            node = frame.acquireExclusive();
        }
    }

    void mergeLeaf(final CursorFrame leaf, Node node) throws IOException {
        final CursorFrame parentFrame = leaf.getParentFrame();

        if (parentFrame == null) {
            node.releaseExclusive();
            return;
        }

        Node parentNode = parentFrame.tryAcquireExclusive();
        if (parentNode == null) {
            node.releaseExclusive();
            node = null;
            parentNode = parentFrame.acquireExclusive();
        }

        Node leftNode;
        doMerge:
        {
            Node rightNode;
            int leftPos;
            select:
            while (true) {
                latchNode:
                {
                    if (parentNode.mSplit != null) {
                        if (node != null) {
                            node.releaseExclusive();
                        }
                        parentNode = mTree.finishSplit(parentFrame, parentNode);
                    } else if (node != null) {
                        break latchNode;
                    }

                    node = leaf.acquireExclusive();
                }

                int nodeAvail = node.availableLeafBytes();
                if (!node.shouldMerge(nodeAvail)) {
                    node.releaseExclusive();
                    parentNode.releaseExclusive();
                    return;
                }

                int leftAvail;

                int pos = parentFrame.getNodePos();
                if (pos == 0) {
                    leftNode = null;
                    leftAvail = -1;
                } else {
                    try {
                        leftNode = mTree.mDatabase
                                .latchChildRetainParentEx(parentNode, pos - 2, false);
                    } catch (Throwable e) {
                        node.releaseExclusive();
                        throw e;
                    }

                    if (leftNode == null) {
                        leftAvail = -1;
                    } else {
                        if (leftNode.mSplit != null) {
                            // Finish sibling split.
                            node.releaseExclusive();
                            node = null;
                            parentNode.insertSplitChildRef(parentFrame, mTree, pos - 2, leftNode);
                            continue;
                        }

                        if (!node.hasKeys()) {
                            leftPos = parentFrame.getNodePos() - 2;
                            rightNode = node;
                            break select;
                        }

                        leftAvail = leftNode.availableLeafBytes();
                    }
                }

                int rightAvail;

                if (pos >= parentNode.highestInternalPos()) {
                    rightNode = null;
                    rightAvail = -1;
                } else {
                    try {
                        rightNode = mTree.mDatabase
                                .latchChildRetainParentEx(parentNode, pos + 2, false);
                    } catch (Throwable e) {
                        if (leftNode != null) {
                            leftNode.releaseExclusive();
                        }
                        node.releaseExclusive();
                        throw e;
                    }

                    if (rightNode == null) {
                        rightAvail = -1;
                    } else {
                        if (rightNode.mSplit != null) {
                            // Finish sibling split.
                            if (leftNode != null) {
                                leftNode.releaseExclusive();
                            }
                            node.releaseExclusive();
                            node = null;
                            parentNode.insertSplitChildRef(parentFrame, mTree, pos + 2, rightNode);
                            continue;
                        }

                        rightAvail = rightNode.availableLeafBytes();
                    }
                }

                if (leftAvail <= rightAvail) {
                    if (leftNode != null) {
                        leftNode.releaseExclusive();
                    }
                    leftPos = parentFrame.getNodePos();
                    leftNode = node;
                    leftAvail = nodeAvail;
                } else {
                    if (rightNode != null) {
                        rightNode.releaseExclusive();
                    }
                    leftPos = parentFrame.getNodePos() - 2;
                    rightNode = node;
                    rightAvail = nodeAvail;
                }

                int rem = leftAvail + rightAvail - pageSize(node.mPage) + Node.TN_HEADER_SIZE;

                if (rem >= 0) {
                    break select;
                }

                if (rightNode != null) {
                    rightNode.releaseExclusive();
                }

                break doMerge;
            }

            try {
                if (mTree.markDirty(leftNode)) {
                    parentNode.updateChildRefId(leftPos, leftNode.mId);
                }
            } catch (Throwable e) {
                leftNode.releaseExclusive();
                rightNode.releaseExclusive();
                parentNode.releaseExclusive();
                throw e;
            }

            try {
                Node.moveLeafToLeftAndDelete(mTree, leftNode, rightNode);
            } catch (Throwable e) {
                leftNode.releaseExclusive();
                parentNode.releaseExclusive();
                throw e;
            }

            parentNode.deleteRightChildRef(leftPos + 2);
        }

        mergeInternal(parentFrame, parentNode, leftNode);
    }

    private void mergeInternal(CursorFrame frame, Node node, Node childNode) throws IOException {
        if (!node.shouldInternalMerge()) {
            childNode.releaseExclusive();
            node.releaseExclusive();
            return;
        }

        if (!node.hasKeys() && node == mTree.mRoot) {
            mTree.rootDelete(childNode);
            return;
        }

        childNode.releaseExclusive();

        CursorFrame parentFrame = frame.getParentFrame();

        if (parentFrame == null) {
            node.releaseExclusive();
            return;
        }

        Node parentNode = parentFrame.tryAcquireExclusive();
        if (parentNode == null) {
            node.releaseExclusive();
            node = null;
            parentNode = parentFrame.acquireExclusive();
        }

        if (parentNode.isLeaf()) {
            throw new AssertionError("Parent node is a leaf");
        }

        Node leftNode, rightNode;
        int nodeAvail;
        while (true) {
            latchNode:
            {
                if (parentNode.mSplit != null) {
                    if (node != null) {
                        node.releaseExclusive();
                    }
                    parentNode = mTree.finishSplit(parentFrame, parentNode);
                } else if (node != null) {
                    // Should already be latched.
                    break latchNode;
                }

                node = frame.acquireExclusive();
            }

            if (!node.shouldMerge(nodeAvail = node.availableInternalBytes())) {
                node.releaseExclusive();
                parentNode.releaseExclusive();
                return;
            }

            int pos = parentFrame.getNodePos();
            if (pos == 0) {
                leftNode = null;
            } else {
                try {
                    leftNode = mTree.mDatabase
                            .latchChildRetainParentEx(parentNode, pos - 2, false);
                } catch (Throwable e) {
                    node.releaseExclusive();
                    throw e;
                }

                if (leftNode != null && leftNode.mSplit != null) {
                    node.releaseExclusive();
                    node = null;
                    parentNode.insertSplitChildRef(parentFrame, mTree, pos - 2, leftNode);
                    continue;
                }
            }

            if (pos >= parentNode.highestInternalPos()) {
                rightNode = null;
            } else {
                try {
                    rightNode = mTree.mDatabase
                            .latchChildRetainParentEx(parentNode, pos + 2, false);
                } catch (Throwable e) {
                    if (leftNode != null) {
                        leftNode.releaseExclusive();
                    }
                    node.releaseExclusive();
                    throw e;
                }

                if (rightNode != null && rightNode.mSplit != null) {
                    if (leftNode != null) {
                        leftNode.releaseExclusive();
                    }
                    node.releaseExclusive();
                    node = null;
                    parentNode.insertSplitChildRef(parentFrame, mTree, pos + 2, rightNode);
                    continue;
                }
            }

            break;
        }

        int leftAvail;
        if (leftNode == null) {
            if (rightNode == null) {
                mergeInternal(parentFrame, parentNode, node);
                return;
            }
            leftAvail = -1;
        } else {
            leftAvail = leftNode.availableInternalBytes();
        }

        int rightAvail = rightNode == null ? -1 : rightNode.availableInternalBytes();

        int leftPos;
        if (leftAvail <= rightAvail) {
            if (leftNode != null) {
                leftNode.releaseExclusive();
            }
            leftPos = parentFrame.getNodePos();
            leftNode = node;
            leftAvail = nodeAvail;
        } else {
            if (rightNode != null) {
                rightNode.releaseExclusive();
            }
            leftPos = parentFrame.getNodePos() - 2;
            rightNode = node;
            rightAvail = nodeAvail;
        }

        long parentPage = parentNode.mPage;
        int parentEntryLoc = DirectPageOps.p_ushortGetLE(parentPage, parentNode.searchVecStart() + leftPos);
        int parentEntryLen = Node.keyLengthAtLoc(parentPage, parentEntryLoc);
        int remaining = leftAvail - parentEntryLen
                + rightAvail - pageSize(parentPage) + (Node.TN_HEADER_SIZE - 2);

        if (remaining < 0) {
            if (rightNode != null) {
                rightNode.releaseExclusive();
            }
        } else {
            try {
                if (mTree.markDirty(leftNode)) {
                    parentNode.updateChildRefId(leftPos, leftNode.mId);
                }
            } catch (Throwable e) {
                leftNode.releaseExclusive();
                rightNode.releaseExclusive();
                parentNode.releaseExclusive();
                throw e;
            }

            try {
                Node.moveInternalToLeftAndDelete
                        (mTree, leftNode, rightNode, parentPage, parentEntryLoc, parentEntryLen);
            } catch (Throwable e) {
                leftNode.releaseExclusive();
                parentNode.releaseExclusive();
                throw e;
            }
            parentNode.deleteRightChildRef(leftPos + 2);
        }

        mergeInternal(parentFrame, parentNode, leftNode);
    }

    private int pageSize(long page) {
        // return page.length;
        return mTree.pageSize();
    }
}
