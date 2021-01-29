package com.glodon.linglong.engine.core;

import com.glodon.linglong.base.common.KeyComparator;
import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.exception.ClosedIndexException;
import com.glodon.linglong.base.exception.LockFailureException;
import com.glodon.linglong.base.common.Ordering;
import com.glodon.linglong.engine.config.DurabilityMode;
import com.glodon.linglong.engine.core.frame.CursorFrame;
import com.glodon.linglong.engine.core.frame.Filter;
import com.glodon.linglong.engine.core.frame.Index;
import com.glodon.linglong.engine.core.lock.*;
import com.glodon.linglong.engine.core.page.DirectPageOps;
import com.glodon.linglong.engine.core.tx.LocalTransaction;
import com.glodon.linglong.engine.core.tx.RedoWriter;
import com.glodon.linglong.engine.core.tx.Transaction;
import com.glodon.linglong.engine.core.view.ViewUtils;
import com.glodon.linglong.engine.observer.CompactionObserver;
import com.glodon.linglong.engine.observer.VerificationObserver;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

/**
 * B-tree实现
 *
 * @author Stereo
 */
public class Tree implements View, Index {
    static final int
            REGISTRY_ID = 0,
            REGISTRY_KEY_MAP_ID = 1,
            CURSOR_REGISTRY_ID = 2,
            FRAGMENTED_TRASH_ID = 3,
            MAX_RESERVED_ID = 0xff;

    static boolean isInternal(long id) {
        return (id & ~0xff) == 0;
    }

    final LocalDatabase mDatabase;

    public LocalDatabase getDatabase() {
        return mDatabase;
    }

    final LockManager mLockManager;

    final long mId;

    final byte[] mIdBytes;

    final Node mRoot;

    volatile byte[] mName;

    private Node mStubTail;

    public Tree(LocalDatabase db, long id, byte[] idBytes, Node root) {
        mDatabase = db;
        mLockManager = db.mLockManager;
        mId = id;
        mIdBytes = idBytes;
        mRoot = root;
    }

    final int pageSize() {
        return mDatabase.pageSize();
    }

    @Override
    public final String toString() {
        return ViewUtils.toString(this);
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
    public final long getId() {
        return mId;
    }

    @Override
    public final byte[] getName() {
        return Utils.cloneArray(mName);
    }

    @Override
    public final String getNameString() {
        byte[] name = mName;
        if (name == null) {
            return null;
        }
        return new String(name, StandardCharsets.UTF_8);
    }

    @Override
    public TreeCursor newCursor(Transaction txn) {
        return new TreeCursor(this, txn);
    }

    @Override
    public Transaction newTransaction(DurabilityMode durabilityMode) {
        return mDatabase.newTransaction(durabilityMode);
    }

    @Override
    public long count(byte[] lowKey, byte[] highKey) throws IOException {
        TreeCursor cursor = newCursor(Transaction.BOGUS);
        TreeCursor high = null;
        try {
            if (highKey != null) {
                high = newCursor(Transaction.BOGUS);
                high.mKeyOnly = true;
                high.find(highKey);
                if (high.mKey == null) {
                    return 0;
                }
            }
            return cursor.count(lowKey, high);
        } finally {
            cursor.reset();
            if (high != null) {
                high.reset();
            }
        }
    }

    @Override
    public final byte[] load(Transaction txn, byte[] key) throws IOException {
        LocalTransaction local = check(txn);

        if (local != null) {
            int lockType = local.lockMode().getRepeatable();
            if (lockType != 0) {
                int hash = LockManager.hash(mId, key);
                local.lock(lockType, mId, key, hash, local.getLockTimeoutNanos());
            }
        }

        Node node = mRoot;
        node.acquireShared();

        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        while (!node.isLeaf()) {
            int childPos;
            try {
                childPos = Node.internalPos(node.binarySearch(key));
            } catch (Throwable e) {
                node.releaseShared();
                throw e;
            }

            long childId = node.retrieveChildRefId(childPos);
            Node childNode = mDatabase.nodeMapGetShared(childId);

            if (childNode != null) {
                node.releaseShared();
                node = childNode;
                node.used(rnd);
            } else {
                node = node.loadChild(mDatabase, childId, Node.OPTION_PARENT_RELEASE_SHARED);
            }

            if (node.mSplit != null) {
                node = node.mSplit.selectNode(node, key);
            }
        }

        CursorFrame frame;
        int keyHash;

        search:
        try {
            final long page = node.mPage;
            final int keyLen = key.length;
            int lowPos = node.searchVecStart();
            int highPos = node.searchVecEnd();

            int lowMatch = 0;
            int highMatch = 0;

            outer:
            while (lowPos <= highPos) {
                int midPos = ((lowPos + highPos) >> 1) & ~1;

                int compareLoc, compareLen, i;
                compare:
                {
                    compareLoc = DirectPageOps.p_ushortGetLE(page, midPos);
                    compareLen = DirectPageOps.p_byteGet(page, compareLoc++);
                    if (compareLen >= 0) {
                        compareLen++;
                    } else {
                        int header = compareLen;
                        compareLen = ((compareLen & 0x3f) << 8) | DirectPageOps.p_ubyteGet(page, compareLoc++);

                        if ((header & Node.ENTRY_FRAGMENTED) != 0) {
                            byte[] compareKey = mDatabase.reconstructKey
                                    (page, compareLoc, compareLen);

                            int fullCompareLen = compareKey.length;

                            int minLen = Math.min(fullCompareLen, keyLen);
                            i = Math.min(lowMatch, highMatch);
                            for (; i < minLen; i++) {
                                byte cb = compareKey[i];
                                byte kb = key[i];
                                if (cb != kb) {
                                    if ((cb & 0xff) < (kb & 0xff)) {
                                        lowPos = midPos + 2;
                                        lowMatch = i;
                                    } else {
                                        highPos = midPos - 2;
                                        highMatch = i;
                                    }
                                    continue outer;
                                }
                            }

                            compareLoc += compareLen - fullCompareLen;
                            compareLen = fullCompareLen;

                            break compare;
                        }
                    }

                    int minLen = Math.min(compareLen, keyLen);
                    i = Math.min(lowMatch, highMatch);
                    for (; i < minLen; i++) {
                        byte cb = DirectPageOps.p_byteGet(page, compareLoc + i);
                        byte kb = key[i];
                        if (cb != kb) {
                            if ((cb & 0xff) < (kb & 0xff)) {
                                lowPos = midPos + 2;
                                lowMatch = i;
                            } else {
                                highPos = midPos - 2;
                                highMatch = i;
                            }
                            continue outer;
                        }
                    }
                }

                if (compareLen < keyLen) {
                    lowPos = midPos + 2;
                    lowMatch = i;
                } else if (compareLen > keyLen) {
                    highPos = midPos - 2;
                    highMatch = i;
                } else {
                    if ((local != null && local.lockMode() != LockMode.READ_COMMITTED) ||
                            mLockManager.isAvailable
                                    (local, mId, key, keyHash = LockManager.hash(mId, key))) {
                        return Node.retrieveLeafValueAtLoc(node, page, compareLoc + compareLen);
                    }
                    frame = new CursorFrame();
                    int pos = midPos - node.searchVecStart();
                    if (node.mSplit != null) {
                        pos = node.mSplit.adjustBindPosition(pos);
                    }
                    frame.bind(node, pos);
                    break search;
                }
            }

            if ((local != null && local.lockMode() != LockMode.READ_COMMITTED) ||
                    mLockManager.isAvailable(local, mId, key, keyHash = LockManager.hash(mId, key))) {
                return null;
            }

            frame = new CursorFrame();
            frame.setNotFoundKey(key);
            int pos = lowPos - node.searchVecStart();
            if (node.mSplit != null) {
                pos = node.mSplit.adjustBindPosition(pos);
            }
            frame.bind(node, ~pos);
            break search;
        } finally {
            node.releaseShared();
        }

        try {
            Locker locker;
            if (local == null) {
                locker = lockSharedLocal(key, keyHash);
            } else if (local.lockShared(mId, key, keyHash) == LockResult.ACQUIRED) {
                locker = local;
            } else {
                locker = null;
            }

            try {
                node = frame.acquireShared();
                try {
                    int pos = frame.getNodePos();
                    if (pos < 0) {
                        return null;
                    } else if (node.mSplit == null) {
                        return node.retrieveLeafValue(pos);
                    } else {
                        return node.mSplit.retrieveLeafValue(node, pos);
                    }
                } finally {
                    node.releaseShared();
                }
            } finally {
                if (locker != null) {
                    locker.unlock();
                }
            }
        } finally {
            CursorFrame.popAll(frame);
        }
    }

    @Override
    public final boolean exists(Transaction txn, byte[] key) throws IOException {
        LocalTransaction local = check(txn);

        if (local != null) {
            int lockType = local.lockMode().getRepeatable();
            if (lockType != 0) {
                int hash = LockManager.hash(mId, key);
                local.lock(lockType, mId, key, hash, local.getLockTimeoutNanos());
            }
        }

        Node node = mRoot;
        node.acquireShared();

        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        while (!node.isLeaf()) {
            int childPos;
            try {
                childPos = Node.internalPos(node.binarySearch(key));
            } catch (Throwable e) {
                node.releaseShared();
                throw e;
            }

            long childId = node.retrieveChildRefId(childPos);
            Node childNode = mDatabase.nodeMapGetShared(childId);

            if (childNode != null) {
                node.releaseShared();
                node = childNode;
                node.used(rnd);
            } else {
                node = node.loadChild(mDatabase, childId, Node.OPTION_PARENT_RELEASE_SHARED);
            }

            if (node.mSplit != null) {
                node = node.mSplit.selectNode(node, key);
            }
        }

        CursorFrame frame;
        int keyHash;

        try {
            int pos = node.binarySearch(key);

            if ((local != null && local.lockMode() != LockMode.READ_COMMITTED) ||
                    mLockManager.isAvailable(local, mId, key, keyHash = LockManager.hash(mId, key))) {
                return pos >= 0 && node.hasLeafValue(pos) != null;
            }

            frame = new CursorFrame();

            if (pos >= 0) {
                if (node.mSplit != null) {
                    pos = node.mSplit.adjustBindPosition(pos);
                }
            } else {
                frame.setNotFoundKey(key);
                if (node.mSplit != null) {
                    pos = ~node.mSplit.adjustBindPosition(~pos);
                }
            }

            frame.bind(node, pos);
        } finally {
            node.releaseShared();
        }

        try {
            Locker locker;
            if (local == null) {
                locker = lockSharedLocal(key, keyHash);
            } else if (local.lockShared(mId, key, keyHash) == LockResult.ACQUIRED) {
                locker = local;
            } else {
                locker = null;
            }

            try {
                node = frame.acquireShared();
                int pos = frame.getNodePos();
                boolean result = pos >= 0 && node.hasLeafValue(pos) != null;
                node.releaseShared();
                return result;
            } finally {
                if (locker != null) {
                    locker.unlock();
                }
            }
        } finally {
            CursorFrame.popAll(frame);
        }
    }

    @Override
    public final void store(Transaction txn, byte[] key, byte[] value) throws IOException {
        Utils.keyCheck(key);
        TreeCursor cursor = newCursor(txn);
        try {
            cursor.mKeyOnly = true;
            cursor.findAndStore(key, value);
        } finally {
            cursor.reset();
        }
    }

    @Override
    public final byte[] exchange(Transaction txn, byte[] key, byte[] value) throws IOException {
        Utils.keyCheck(key);
        TreeCursor cursor = newCursor(txn);
        try {
            return cursor.findAndStore(key, value);
        } finally {
            cursor.reset();
        }
    }

    @Override
    public final boolean insert(Transaction txn, byte[] key, byte[] value) throws IOException {
        Utils.keyCheck(key);
        TreeCursor cursor = newCursor(txn);
        try {
            cursor.mKeyOnly = true;
            return cursor.findAndModify(key, TreeCursor.MODIFY_INSERT, value);
        } finally {
            cursor.reset();
        }
    }

    @Override
    public final boolean replace(Transaction txn, byte[] key, byte[] value) throws IOException {
        Utils.keyCheck(key);
        TreeCursor cursor = newCursor(txn);
        try {
            cursor.mKeyOnly = true;
            return cursor.findAndModify(key, TreeCursor.MODIFY_REPLACE, value);
        } finally {
            cursor.reset();
        }
    }

    @Override
    public final boolean update(Transaction txn, byte[] key, byte[] value) throws IOException {
        Utils.keyCheck(key);
        TreeCursor cursor = newCursor(txn);
        try {
            // TODO: 通过禁用自动加载进行优化，并进行就地比较
            return cursor.findAndModify(key, TreeCursor.MODIFY_UPDATE, value);
        } finally {
            cursor.reset();
        }
    }

    @Override
    public final boolean update(Transaction txn, byte[] key, byte[] oldValue, byte[] newValue)
            throws IOException {
        Utils.keyCheck(key);
        TreeCursor cursor = newCursor(txn);
        try {
            return cursor.findAndModify(key, oldValue, newValue);
        } finally {
            cursor.reset();
        }
    }

    @Override
    public LockResult touch(Transaction txn, byte[] key) throws LockFailureException {
        LocalTransaction local = check(txn);

        LockMode mode;
        if (local == null || (mode = local.lockMode()) == LockMode.READ_COMMITTED) {
            int hash = LockManager.hash(mId, key);
            if (!isLockAvailable(local, key, hash)) {
                // Acquire and release.
                if (local == null) {
                    lockSharedLocal(key, hash).unlock();
                } else {
                    LockResult result = local.lock(0, mId, key, hash, local.getLockTimeoutNanos());
                    if (result == LockResult.ACQUIRED) {
                        local.unlock();
                    }
                }
            }
        } else if (!mode.isNoReadLock()) {
            int hash = LockManager.hash(mId, key);
            return local.lock(mode.getRepeatable(), mId, key, hash, local.getLockTimeoutNanos());
        }

        return LockResult.UNOWNED;
    }

    @Override
    public final LockResult tryLockShared(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException {
        return check(txn).tryLockShared(mId, key, nanosTimeout);
    }

    @Override
    public final LockResult lockShared(Transaction txn, byte[] key) throws LockFailureException {
        return check(txn).lockShared(mId, key);
    }

    @Override
    public final LockResult tryLockUpgradable(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException {
        return check(txn).tryLockUpgradable(mId, key, nanosTimeout);
    }

    @Override
    public final LockResult lockUpgradable(Transaction txn, byte[] key)
            throws LockFailureException {
        return check(txn).lockUpgradable(mId, key);
    }

    @Override
    public final LockResult tryLockExclusive(Transaction txn, byte[] key, long nanosTimeout)
            throws DeadlockException {
        return check(txn).tryLockExclusive(mId, key, nanosTimeout);
    }

    @Override
    public final LockResult lockExclusive(Transaction txn, byte[] key)
            throws LockFailureException {
        return check(txn).lockExclusive(mId, key);
    }

    @Override
    public final LockResult lockCheck(Transaction txn, byte[] key) {
        return check(txn).lockCheck(mId, key);
    }

    @Override
    public View viewGe(byte[] key) {
        return BoundedView.viewGe(this, key);
    }

    @Override
    public View viewGt(byte[] key) {
        return BoundedView.viewGt(this, key);
    }

    @Override
    public View viewLe(byte[] key) {
        return BoundedView.viewLe(this, key);
    }

    @Override
    public View viewLt(byte[] key) {
        return BoundedView.viewLt(this, key);
    }

    @Override
    public View viewPrefix(byte[] prefix, int trim) {
        return BoundedView.viewPrefix(this, prefix, trim);
    }

    @Override
    public final boolean isUnmodifiable() {
        return isClosed();
    }

    @Override
    public final boolean isModifyAtomic() {
        return true;
    }

    @Override
    public long evict(Transaction txn, byte[] lowKey, byte[] highKey,
                      Filter evictionFilter, boolean autoload)
            throws IOException {
        long length = 0;
        TreeCursor cursor = newCursor(txn);
        cursor.autoload(autoload);

        try {
            byte[] endKey = cursor.randomNode(lowKey, highKey);
            if (endKey == null) {
                return length;
            }

            if (lowKey != null) {
                if (Utils.compareUnsigned(lowKey, endKey) > 0) {
                    return length;
                }
                if (cursor.compareKeyTo(lowKey) < 0) {
                    cursor.findNearby(lowKey);
                }
            }

            if (highKey != null && Utils.compareUnsigned(highKey, endKey) <= 0) {
                endKey = highKey;
            }

            long[] stats = new long[2];
            while (cursor.key() != null) {
                byte[] key = cursor.key();
                byte[] value = cursor.value();
                if (value != null) {
                    cursor.valueStats(stats);
                    if (stats[0] > 0 &&
                            (evictionFilter == null || evictionFilter.isAllowed(key, value))) {
                        length += key.length + stats[0];
                        cursor.store(null);
                    }
                } else {
                }
                cursor.nextLe(endKey);
            }
        } finally {
            cursor.reset();
        }
        return length;
    }

    @Override
    public Stats analyze(byte[] lowKey, byte[] highKey) throws IOException {
        TreeCursor cursor = newCursor(Transaction.BOGUS);
        try {
            cursor.mKeyOnly = true;
            cursor.random(lowKey, highKey);
            return cursor.key() == null ? new Stats(0, 0, 0, 0, 0) : cursor.analyze();
        } catch (Throwable e) {
            cursor.reset();
            throw e;
        }
    }

    final Index observableView() {
        return isInternal(mId) ? new UnmodifiableView(this) : this;
    }

    final boolean compactTree(Index view, long highestNodeId, CompactionObserver observer)
            throws IOException {
        try {
            if (!observer.indexBegin(view)) {
                return false;
            }
        } catch (Throwable e) {
            Utils.uncaught(e);
            return false;
        }

        TreeCursor cursor = newCursor(Transaction.BOGUS);
        try {
            cursor.mKeyOnly = true;

            cursor.firstLeaf();

            if (!cursor.compact(highestNodeId, observer)) {
                return false;
            }

            try {
                if (!observer.indexComplete(view)) {
                    return false;
                }
            } catch (Throwable e) {
                Utils.uncaught(e);
                return false;
            }

            return true;
        } finally {
            cursor.reset();
        }
    }

    @Override
    public final boolean verify(VerificationObserver observer) throws IOException {
        if (observer == null) {
            observer = new VerificationObserver();
        }
        Index view = observableView();
        observer.setFailed(false);
        verifyTree(view, observer);
        boolean passed = !observer.isFailed();
        observer.indexComplete(view, passed, null);
        return passed;
    }

    final boolean verifyTree(Index view, VerificationObserver observer) throws IOException {
        TreeCursor cursor = newCursor(Transaction.BOGUS);
        try {
            cursor.mKeyOnly = true;
            cursor.first(); // 必须以加载的key开始
            int height = cursor.height();
            if (!observer.indexBegin(view, height)) {
                cursor.reset();
                return false;
            }
            if (!cursor.verify(height, observer)) {
                cursor.reset();
                return false;
            }
            cursor.reset();
        } catch (Throwable e) {
            observer.setFailed(true);
            throw e;
        }
        return true;
    }

    @Override
    public final void close() throws IOException {
        close(false, false, false);
    }

    final Node close(boolean forDelete, final boolean rootLatched) throws IOException {
        return close(forDelete, rootLatched, false);
    }

    final void forceClose() throws IOException {
        close(false, false, true);
    }

    private Node close(boolean forDelete, final boolean rootLatched, boolean force)
            throws IOException {
        Node root = mRoot;

        if (!rootLatched) {
            root.acquireExclusive();
        }

        try {
            if (root.mPage == DirectPageOps.p_closedTreePage()) {
                // Already closed.
                return null;
            }

            if (!force && isInternal(mId)) {
                throw new IllegalStateException("Cannot close an internal index");
            }

            if (root.hasKeys()) {
                root.releaseExclusive();
                mDatabase.commitLock().acquireExclusive();
                try {
                    root.acquireExclusive();
                    if (root.mPage == DirectPageOps.p_closedTreePage()) {
                        return null;
                    }
                    root.invalidateCursors();
                } finally {
                    mDatabase.commitLock().releaseExclusive();
                }
            } else {
                root.invalidateCursors();
            }

            Node newRoot = root.cloneNode();
            mDatabase.swapIfDirty(root, newRoot);

            if (root.mId > 0) {
                mDatabase.nodeMapRemove(root);
            }

            root.closeRoot();

            if (forDelete) {
                mDatabase.treeClosed(this);
                return newRoot;
            }

            newRoot.acquireShared();
            try {
                mDatabase.treeClosed(this);
                newRoot.makeEvictableNow();
                if (newRoot.mId > 0) {
                    mDatabase.nodeMapPut(newRoot);
                }
            } finally {
                newRoot.releaseShared();
            }

            return null;
        } finally {
            if (!rootLatched) {
                root.releaseExclusive();
            }
        }
    }

    @Override
    public final boolean isClosed() {
        Node root = mRoot;
        root.acquireShared();
        boolean closed = root.mPage == DirectPageOps.p_closedTreePage();
        root.releaseShared();
        return closed;
    }

    @Override
    public final void drop() throws IOException {
        drop(true).run();
    }

    final Runnable drop(boolean mustBeEmpty) throws IOException {
        CommitLock.Shared shared = mDatabase.commitLock().acquireShared();

        Node root;
        try {
            root = mRoot;
            root.acquireExclusive();
        } catch (Throwable e) {
            shared.release();
            throw e;
        }

        try {
            try {
                if (root.mPage == DirectPageOps.p_closedTreePage()) {
                    throw new ClosedIndexException();
                }

                if (mustBeEmpty && (!root.isLeaf() || root.hasKeys())) {
                    throw new IllegalStateException("Cannot drop a non-empty index");
                }

                if (isInternal(mId)) {
                    throw new IllegalStateException("Cannot close an internal index");
                }
            } catch (Throwable e) {
                shared.release();
                throw e;
            }

            return mDatabase.deleteTree(this, shared);
        } finally {
            root.releaseExclusive();
        }
    }

    final boolean deleteAll() throws IOException {
        return newCursor(Transaction.BOGUS).deleteAll();
    }

    static Tree graftTempTree(Tree lowTree, Tree highTree) throws IOException {

        TreeCursor lowCursor, highCursor;

        lowCursor = lowTree.newCursor(Transaction.BOGUS);
        try {
            lowCursor.mKeyOnly = true;
            lowCursor.last();

            highCursor = highTree.newCursor(Transaction.BOGUS);
            try {
                highCursor.mKeyOnly = true;
                highCursor.first();

                CommitLock.Shared shared = lowTree.mDatabase.commitLock().acquireShared();
                try {
                    return doGraftTempTree(lowTree, highTree, lowCursor, highCursor);
                } finally {
                    shared.release();
                }
            } finally {
                highCursor.reset();
            }
        } finally {
            lowCursor.reset();
        }
    }

    private static Tree doGraftTempTree(Tree lowTree, Tree highTree,
                                        TreeCursor lowCursor, TreeCursor highCursor)
            throws IOException {

        byte[] midKey;
        CursorFrame lowFrame, highFrame;
        {
            lowFrame = lowCursor.frameExclusive();
            Node lowNode = lowCursor.notSplitDirty(lowFrame);
            try {
                highFrame = highCursor.frameExclusive();
                Node highNode = highCursor.notSplitDirty(highFrame);
                try {
                    midKey = lowNode.midKey(lowNode.highestLeafPos(), highNode, 0);
                } finally {
                    highNode.releaseExclusive();
                }
            } finally {
                lowNode.releaseExclusive();
            }
        }

        Tree survivor, victim;
        CursorFrame survivorFrame;
        Node victimNode;

        while (true) {
            CursorFrame lowParent = lowFrame.getParentFrame();
            CursorFrame highParent = highFrame.getParentFrame();

            if (highParent == null) {
                survivor = lowTree;
                survivorFrame = lowFrame;
                victim = highTree;
                victimNode = highFrame.acquireExclusive();
                break;
            } else if (lowParent == null) {
                survivor = highTree;
                survivorFrame = highFrame;
                victim = lowTree;
                victimNode = lowFrame.acquireExclusive();
                break;
            }

            lowFrame = lowParent;
            highFrame = highParent;
        }

        Node survivorNode;
        try {
            Split split = new Split(lowTree == survivor, victimNode);
            split.setKey(survivor, midKey);
            survivorNode = survivorFrame.acquireExclusive();
            survivorNode.mSplit = split;
        } finally {
            victimNode.releaseExclusive();
        }

        try {
            clearExtremityBits(lowCursor.mFrame, survivorFrame, ~Node.HIGH_EXTREMITY);
            clearExtremityBits(highCursor.mFrame, survivorFrame, ~Node.LOW_EXTREMITY);

            survivor.finishSplit(survivorFrame, survivorNode).releaseExclusive();
        } catch (Throwable e) {
            survivorNode.cleanupFragments(e, survivorNode.mSplit.fragmentedKey());
            throw e;
        }

        victim.mDatabase.removeGraftedTempTree(victim);

        Node rootNode = survivor.mRoot;
        rootNode.acquireExclusive();

        if (rootNode.numKeys() == 1 && rootNode.isInternal()) {
            LocalDatabase db = survivor.mDatabase;
            Node leftNode = db.latchChildRetainParentEx(rootNode, 0, true);
            Node rightNode;
            try {
                rightNode = db.latchChildRetainParentEx(rootNode, 2, true);
            } catch (Throwable e) {
                leftNode.releaseExclusive();
                throw e;
            }

            tryMerge:
            {
                if (leftNode.isLeaf()) {
                    int leftAvail = leftNode.availableLeafBytes();
                    int rightAvail = rightNode.availableLeafBytes();

                    int remaining = leftAvail
                            + rightAvail - survivor.pageSize() + Node.TN_HEADER_SIZE;

                    if (remaining < 0) {
                        break tryMerge;
                    }

                    try {
                        Node.moveLeafToLeftAndDelete(survivor, leftNode, rightNode);
                    } catch (Throwable e) {
                        leftNode.releaseExclusive();
                        rootNode.releaseExclusive();
                        throw e;
                    }
                } else {

                    long rootPage = rootNode.mPage;
                    int rootEntryLoc = DirectPageOps.p_ushortGetLE(rootPage, rootNode.searchVecStart());
                    int rootEntryLen = Node.keyLengthAtLoc(rootPage, rootEntryLoc);

                    int leftAvail = leftNode.availableInternalBytes();
                    int rightAvail = rightNode.availableInternalBytes();

                    int remaining = leftAvail - rootEntryLen
                            + rightAvail - survivor.pageSize() + (Node.TN_HEADER_SIZE - 2);

                    if (remaining < 0) {
                        break tryMerge;
                    }

                    try {
                        Node.moveInternalToLeftAndDelete
                                (survivor, leftNode, rightNode, rootPage, rootEntryLoc, rootEntryLen);
                    } catch (Throwable e) {
                        leftNode.releaseExclusive();
                        rootNode.releaseExclusive();
                        throw e;
                    }
                }

                rootNode.deleteRightChildRef(2);
                survivor.rootDelete(leftNode);
                return survivor;
            }

            rightNode.releaseExclusive();
            leftNode.releaseExclusive();
        }

        rootNode.releaseExclusive();

        return survivor;
    }

    private static void clearExtremityBits(CursorFrame frame, CursorFrame stop, int mask) {
        do {
            if (frame == stop) {
                Node node = frame.getNode();
                node.type((byte) (node.type() & mask));
                break;
            }
            Node node = frame.acquireExclusive();
            node.type((byte) (node.type() & mask));
            node.releaseExclusive();
            frame = frame.getParentFrame();
        } while (frame != null);
    }

    @FunctionalInterface
    interface NodeVisitor {
        void visit(Node node) throws IOException;
    }


    final void traverseLoaded(NodeVisitor visitor) throws IOException {
        Node node = mRoot;
        node.acquireExclusive();

        if (node.mSplit != null) {
            CursorFrame frame = new CursorFrame();
            frame.bind(node, 0);
            try {
                node = finishSplit(frame, node);
            } catch (Throwable e) {
                CursorFrame.popAll(frame);
                throw e;
            }
        }

        CursorFrame frame = null;
        int pos = 0;

        while (true) {
            toLower:
            while (node.isInternal()) {
                final int highestPos = node.highestInternalPos();
                while (true) {
                    if (pos > highestPos) {
                        break toLower;
                    }
                    long childId = node.retrieveChildRefId(pos);
                    Node child = mDatabase.nodeMapGetExclusive(childId);
                    if (child != null) {
                        frame = new CursorFrame(frame);
                        frame.bind(node, pos);
                        node.releaseExclusive();
                        node = child;
                        pos = 0;
                        continue toLower;
                    }
                    pos += 2;
                }
            }

            try {
                visitor.visit(node);
            } catch (Throwable e) {
                CursorFrame.popAll(frame);
                throw e;
            }

            if (frame == null) {
                return;
            }

            node = frame.acquireExclusive();

            if (node.mSplit != null) {
                try {
                    node = finishSplit(frame, node);
                } catch (Throwable e) {
                    CursorFrame.popAll(frame);
                    throw e;
                }
            }

            pos = frame.getNodePos();
            frame = frame.pop();
            pos += 2;
        }
    }

    final void writeCachePrimer(final DataOutput dout) throws IOException {
        traverseLoaded((node) -> {
            byte[] midKey;
            try {
                if (!node.isLeaf()) {
                    return;
                }
                int numKeys = node.numKeys();
                if (numKeys > 1) {
                    int highPos = numKeys & ~1;
                    midKey = node.midKey(highPos - 2, node, highPos);
                } else if (numKeys == 1) {
                    midKey = node.retrieveKey(0);
                } else {
                    return;
                }
            } finally {
                node.releaseExclusive();
            }

            if (midKey.length < 0xffff) {
                dout.writeShort(midKey.length);
                dout.write(midKey);
            }
        });

        dout.writeShort(0xffff);
    }

    final void applyCachePrimer(DataInput din) throws IOException {
        new Primer(din).run();
    }

    static final void skipCachePrimer(DataInput din) throws IOException {
        while (true) {
            int len = din.readUnsignedShort();
            if (len == 0xffff) {
                break;
            }
            while (len > 0) {
                int amt = din.skipBytes(len);
                if (amt <= 0) {
                    break;
                }
                len -= amt;
            }
        }
    }

    private class Primer {
        private final DataInput mDin;
        private final int mTaskLimit;

        private int mTaskCount;
        private boolean mFinished;
        private IOException mEx;

        Primer(DataInput din) {
            mDin = din;
            // TODO: 应限制I/O并发级别
            // TODO: 以改善并发
            mTaskLimit = Runtime.getRuntime().availableProcessors() * 8;
        }

        void run() throws IOException {
            synchronized (this) {
                mTaskCount++;
            }

            prime();

            synchronized (this) {
                while (true) {
                    if (mEx != null) {
                        throw mEx;
                    }
                    if (mTaskCount <= 0) {
                        break;
                    }
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new InterruptedIOException();
                    }
                }
            }
        }

        void prime() {
            try {
                TreeCursor c = newCursor(Transaction.BOGUS);

                try {
                    c.mKeyOnly = true;

                    while (true) {
                        byte[] key;

                        synchronized (this) {
                            if (mFinished) {
                                return;
                            }

                            int len = mDin.readUnsignedShort();

                            if (len == 0xffff) {
                                mFinished = true;
                                return;
                            }

                            key = new byte[len];
                            mDin.readFully(key);

                            if (mTaskCount < mTaskLimit) spawn:{
                                Thread task;
                                try {
                                    task = new Thread(() -> prime());
                                } catch (Throwable e) {
                                    break spawn;
                                }
                                mTaskCount++;
                                task.start();
                            }
                        }

                        c.findNearby(key);
                    }
                } catch (IOException e) {
                    synchronized (this) {
                        if (mEx == null) {
                            mEx = e;
                        }
                    }
                } finally {
                    c.reset();
                }
            } finally {
                synchronized (this) {
                    mTaskCount--;
                    notifyAll();
                }
            }
        }
    }

    final boolean allowStoredCounts() {
        return true;
    }

    final Node finishSplit(final CursorFrame frame, Node node) throws IOException {
        while (true) {
            if (node == mRoot) {

                Node stub = mStubTail;

                if (stub == null) {
                    try {
                        node.finishSplitRoot();
                    } finally {
                        node.releaseExclusive();
                    }
                } else withStub:{
                    if (!stub.tryAcquireExclusive()) {

                        node.releaseExclusive();
                        stub.acquireExclusive();

                        try {
                            node = frame.tryAcquireExclusive();
                        } catch (Throwable e) {
                            stub.releaseExclusive();
                            throw e;
                        }

                        if (node == null) {
                            stub.releaseExclusive();
                            break withStub;
                        }

                        if (node.mSplit == null) {
                            stub.releaseExclusive();
                            return node;
                        }

                        if (node != mRoot || stub != mStubTail) {
                            node.releaseExclusive();
                            stub.releaseExclusive();
                            break withStub;
                        }
                    }

                    try {
                        node.finishSplitRoot();
                        mStubTail = stub.mNodeMapNext;
                    } finally {
                        node.releaseExclusive();
                        stub.releaseExclusive();
                    }
                }

                node = frame.acquireExclusive();

                if (node.mSplit != null) {
                    continue;
                }

                return node;
            }

            final CursorFrame parentFrame = frame.getParentFrame();
            node.releaseExclusive();

            Node parentNode = parentFrame.acquireExclusive();
            while (true) {
                if (parentNode.mSplit != null) {
                    parentNode = finishSplit(parentFrame, parentNode);
                }
                node = frame.acquireExclusive();
                if (node.mSplit == null) {
                    parentNode.releaseExclusive();
                    return node;
                }
                if (node == mRoot) {
                    parentNode.releaseExclusive();
                    break;
                }
                parentNode.insertSplitChildRef(parentFrame, this, parentFrame.getNodePos(), node);
            }
        }
    }

    final void rootDelete(Node child) throws IOException {
        Node stub = new Node(mRoot.mContext);

        stub.mNodeMapNext = mStubTail;
        mStubTail = stub;

        mRoot.rootDelete(this, child, stub);
    }

    final LocalTransaction check(Transaction txn) throws IllegalArgumentException {
        if (txn instanceof LocalTransaction) {
            LocalTransaction local = (LocalTransaction) txn;
            LocalDatabase txnDb = local.getDatabase();
            if (txnDb == mDatabase || txnDb == null) {
                return local;
            }
        }
        if (txn != null) {
            if (txn == Transaction.BOGUS) return LocalTransaction.BOGUS;
            throw new IllegalArgumentException("Transaction belongs to a different database");
        }
        return null;
    }

    final boolean isLockAvailable(Locker locker, byte[] key, int hash) {
        return mLockManager.isAvailable(locker, mId, key, hash);
    }

    final Locker lockSharedLocal(byte[] key, int hash) throws LockFailureException {
        return mLockManager.lockSharedLocal(mId, key, hash);
    }

    final Locker lockExclusiveLocal(byte[] key, int hash) throws LockFailureException {
        return mLockManager.lockExclusiveLocal(mId, key, hash);
    }

    final long redoStoreNullTxn(byte[] key, byte[] value) throws IOException {
        RedoWriter redo = mDatabase.mRedoWriter;
        DurabilityMode mode;
        if (redo == null || (mode = mDatabase.mDurabilityMode) == DurabilityMode.NO_REDO) {
            return 0;
        }
        return mDatabase.anyTransactionContext().redoStoreAutoCommit
                (redo.txnRedoWriter(), mId, key, value, mode);
    }

    final long redoStoreNoLock(byte[] key, byte[] value, DurabilityMode mode) throws IOException {
        RedoWriter redo = mDatabase.mRedoWriter;
        if (redo == null) {
            return 0;
        }
        return mDatabase.anyTransactionContext().redoStoreNoLockAutoCommit
                (redo.txnRedoWriter(), mId, key, value, mode);
    }

    final void txnCommitSync(LocalTransaction txn, long commitPos) throws IOException {
        mDatabase.mRedoWriter.txnCommitSync(txn, commitPos);
    }

    final boolean markDirty(Node node) throws IOException {
        return mDatabase.markDirty(this, node);
    }
}
