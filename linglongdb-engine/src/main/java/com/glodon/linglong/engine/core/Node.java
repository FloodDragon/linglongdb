package com.glodon.linglong.engine.core;


import com.glodon.linglong.base.common.LHashTable;
import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.concurrent.Clutch;
import com.glodon.linglong.base.exception.*;
import com.glodon.linglong.engine.core.page.DirectPageOps;
import com.glodon.linglong.engine.core.page.PageDb;
import com.glodon.linglong.engine.core.frame.CursorFrame;
import com.glodon.linglong.engine.core.frame.GhostFrame;
import com.glodon.linglong.engine.core.tx.LocalTransaction;
import com.glodon.linglong.engine.core.tx.UndoLog;
import com.glodon.linglong.engine.observer.VerificationObserver;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;


/**
 * @author Stereo
 */
public final class Node extends Clutch implements DatabaseAccess {
    public static final byte
            CACHED_CLEAN = 0, // 0b0000
            CACHED_DIRTY_0 = 2, // 0b0010
            CACHED_DIRTY_1 = 3; // 0b0011

    public static final byte
            TYPE_NONE = 0,
    // TYPE_FRAGMENT = (byte) 0x20, // 0b0010_000_0 (never persisted)
    TYPE_UNDO_LOG = (byte) 0x40, // 0b0100_000_0
            TYPE_TN_IN = (byte) 0x64, // 0b0110_010_0
            TYPE_TN_BIN = (byte) 0x74, // 0b0111_010_0
            TYPE_TN_LEAF = (byte) 0x80; // 0b1000_000_0

    public static final byte LOW_EXTREMITY = 0x02, HIGH_EXTREMITY = 0x08;

    public static final int TN_HEADER_SIZE = 12;

    private static final int CLOSED_ID = -1;

    public static final int ENTRY_FRAGMENTED = 0x40;

    final NodeContext mContext;

    Node mMoreUsed; // points to more recently used node
    Node mLessUsed; // points to less recently used node

    Node mNextDirty;
    Node mPrevDirty;

    long mPage;

    public long getPage() {
        return mPage;
    }

    volatile long mId;

    public void setId(long mId) {
        this.mId = mId;
    }

    public long getId() {
        return mId;
    }

    byte mCachedState;

    public byte getCachedState() {
        return mCachedState;
    }

    // private byte mType;
    // private int mGarbage;
    // private int mLeftSegTail;
    // private int mRightSegTail;
    // private int mSearchVecStart;
    // private int mSearchVecEnd;

    Node mNodeMapNext;

    transient volatile CursorFrame mLastCursorFrame;

    public void setLastCursorFrame(CursorFrame mLastCursorFrame) {
        this.mLastCursorFrame = mLastCursorFrame;
    }

    public CursorFrame getLastCursorFrame() {
        return mLastCursorFrame;
    }

    transient Split mSplit;

    public Split getSplit() {
        return mSplit;
    }

    Node(NodeContext context, long page) {
        mContext = context;
        mPage = page;
    }

    Node(NodeContext context) {
        super(EXCLUSIVE);

        mContext = context;
        mPage = DirectPageOps.p_stubTreePage();

        mId = -1;

        mCachedState = CACHED_CLEAN;

        // type(TYPE_TN_IN);
        // garbage(0);
        // leftSegTail(TN_HEADER_SIZE);
        // rightSegTail(TN_HEADER_SIZE + 8 - 1);
        // searchVecStart(TN_HEADER_SIZE);
        // searchVecEnd(TN_HEADER_SIZE - 2);
    }

    private Node(long id) {
        super(EXCLUSIVE);
        mContext = null;
        mId = id;
    }

    public void delete(LocalDatabase db) {
        acquireExclusive();
        try {
            doDelete(db);
        } finally {
            releaseExclusive();
        }
    }

    void doDelete(LocalDatabase db) {
        if (db.mFullyMapped) {
            closeRoot();
            return;
        }

        long page = mPage;
        if (page != DirectPageOps.p_closedTreePage()) {
            DirectPageOps.p_delete(page);
            closeRoot();
        }
    }

    @Override
    protected Clutch.Pack getPack() {
        return mContext;
    }

    @Override
    public LocalDatabase getDatabase() {
        return mContext.mDatabase;
    }

    void asEmptyRoot() {
        mId = 0;
        mCachedState = CACHED_CLEAN;
        type((byte) (TYPE_TN_LEAF | LOW_EXTREMITY | HIGH_EXTREMITY));
        clearEntries();
    }

    void asTrimmedRoot() {
        asEmptyLeaf(LOW_EXTREMITY | HIGH_EXTREMITY);
    }

    void asEmptyLeaf(int extremity) {
        type((byte) (TYPE_TN_LEAF | extremity));
        clearEntries();
    }

    void asSortLeaf() {
        type((byte) (TYPE_TN_LEAF | LOW_EXTREMITY | HIGH_EXTREMITY));
        garbage(0);
        leftSegTail(TN_HEADER_SIZE);
        int pageSize = pageSize(mPage);
        rightSegTail(pageSize - 1);
        searchVecStart(TN_HEADER_SIZE);
        searchVecEnd(TN_HEADER_SIZE - 2); // inclusive
    }

    void closeRoot() {
        mId = CLOSED_ID;
        mCachedState = CACHED_CLEAN;
        mPage = DirectPageOps.p_closedTreePage();
        readFields();
    }

    Node cloneNode() {
        Node newNode = new Node(mContext, mPage);
        newNode.mId = mId;
        newNode.mCachedState = mCachedState;
        // newNode.type(type());
        // newNode.garbage(garbage());
        // newNode.leftSegTail(leftSegTail());
        // newNode.rightSegTail(rightSegTail());
        // newNode.searchVecStart(searchVecStart());
        // newNode.searchVecEnd(searchVecEnd());
        return newNode;
    }

    private void clearEntries() {
        garbage(0);
        leftSegTail(TN_HEADER_SIZE);
        int pageSize = pageSize(mPage);
        rightSegTail(pageSize - 1);
        // Search vector location must be even.
        searchVecStart((TN_HEADER_SIZE + ((pageSize - TN_HEADER_SIZE) >> 1)) & ~1);
        searchVecEnd(searchVecStart() - 2); // inclusive
    }

    void used(ThreadLocalRandom rnd) {
        mContext.used(this, rnd);
    }

    void unused() {
        mContext.unused(this);
    }

    public void makeEvictable() {
        mContext.makeEvictable(this);
    }

    public void makeEvictableNow() {
        mContext.makeEvictableNow(this);
    }

    public void makeUnevictable() {
        mContext.makeUnevictable(this);
    }

    static final int OPTION_PARENT_RELEASE_SHARED = 0b001, OPTION_CHILD_ACQUIRE_EXCLUSIVE = 0b100;

    Node loadChild(LocalDatabase db, long childId, int options) throws IOException {
        Node lock;
        try {
            lock = new Node(childId);

            if (childId <= 1) {
                throw new AssertionError("Illegal child id: " + childId);
            }
        } catch (Throwable e) {
            releaseEither();
            throw e;
        }

        try {
            while (true) {
                Node childNode = db.nodeMapPutIfAbsent(lock);
                if (childNode == null) {
                    break;
                }

                if ((options & OPTION_CHILD_ACQUIRE_EXCLUSIVE) == 0) {
                    childNode.acquireShared();
                    if (childId == childNode.mId) {
                        return childNode;
                    }
                    childNode.releaseShared();
                } else {
                    childNode.acquireExclusive();
                    if (childId == childNode.mId) {
                        return childNode;
                    }
                    childNode.releaseExclusive();
                }
            }
        } finally {
            if ((options & OPTION_PARENT_RELEASE_SHARED) != 0) {
                releaseShared();
            }
        }

        try {
            Node childNode;
            try {
                childNode = db.allocLatchedNode(childId);
                childNode.mId = childId;
            } catch (Throwable e) {
                db.nodeMapRemove(lock);
                throw e;
            }

            db.nodeMapReplace(lock, childNode);

            try {
                childNode.read(db, childId);
            } catch (Throwable e) {
                db.nodeMapRemove(childNode);
                childNode.mId = 0;
                childNode.type(TYPE_NONE);
                childNode.releaseExclusive();
                throw e;
            }

            if ((options & OPTION_CHILD_ACQUIRE_EXCLUSIVE) == 0) {
                childNode.downgrade();
            }

            return childNode;
        } catch (Throwable e) {
            if ((options & OPTION_PARENT_RELEASE_SHARED) == 0) {
                releaseEither();
            }
            throw e;
        } finally {
            lock.mId = 0;
            lock.releaseExclusive();
        }
    }

    private Node tryLatchChildNotSplit(int childPos) throws IOException {
        final long childId = retrieveChildRefId(childPos);
        final LocalDatabase db = getDatabase();
        Node childNode = db.nodeMapGet(childId);

        latchChild:
        {
            if (childNode != null) {
                if (!childNode.tryAcquireExclusive()) {
                    return null;
                }
                if (childId == childNode.mId) {
                    break latchChild;
                }
                childNode.releaseExclusive();
            }
            childNode = loadChild(db, childId, OPTION_CHILD_ACQUIRE_EXCLUSIVE);
        }

        if (childNode.mSplit == null) {
            return childNode;
        } else {
            childNode.releaseExclusive();
            return null;
        }
    }

    void finishSplitRoot() throws IOException {
        LocalDatabase db = mContext.mDatabase;
        Node child = db.allocDirtyNode();
        db.nodeMapPut(child);
        long newRootPage;

        // newRootPage = child.mPage;
        // child.mPage = mPage;
        // child.type(type());
        // child.garbage(garbage());
        // child.leftSegTail(leftSegTail());
        // child.rightSegTail(rightSegTail());
        // child.searchVecStart(searchVecStart());
        // child.searchVecEnd(searchVecEnd());
        if (db.mFullyMapped) {
            newRootPage = mPage;
            DirectPageOps.p_copy(newRootPage, 0, child.mPage, 0, db.pageSize());
        } else {
            newRootPage = child.mPage;
            child.mPage = mPage;
        }

        final Split split = mSplit;
        final Node sibling = rebindSplitFrames(split);
        mSplit = null;

        for (CursorFrame frame = mLastCursorFrame; frame != null; ) {
            CursorFrame prev = frame.getPrevCousin();
            frame.rebind(child, frame.getNodePos());
            frame = prev;
        }

        Node left, right;
        if (split.mSplitRight) {
            left = child;
            right = sibling;
        } else {
            left = sibling;
            right = child;
        }

        int leftSegTail = split.copySplitKeyToParent(newRootPage, TN_HEADER_SIZE);

        final int searchVecStart = pageSize(newRootPage) -
                (((pageSize(newRootPage) - leftSegTail + (2 + 8 + 8)) >> 1) & ~1);
        DirectPageOps.p_shortPutLE(newRootPage, searchVecStart, TN_HEADER_SIZE);
        DirectPageOps.p_longPutLE(newRootPage, searchVecStart + 2, left.mId);
        DirectPageOps.p_longPutLE(newRootPage, searchVecStart + 2 + 8, right.mId);

        byte newType = isLeaf() ? (byte) (TYPE_TN_BIN | LOW_EXTREMITY | HIGH_EXTREMITY)
                : (byte) (TYPE_TN_IN | LOW_EXTREMITY | HIGH_EXTREMITY);

        mPage = newRootPage;
        // type(newType);
        // garbage(0);
        DirectPageOps.p_intPutLE(newRootPage, 0, newType & 0xff); // type, reserved byte, and garbage
        leftSegTail(leftSegTail);
        rightSegTail(pageSize(newRootPage) - 1);
        searchVecStart(searchVecStart);
        searchVecEnd(searchVecStart);

        CursorFrame lock = new CursorFrame();
        addParentFrames(lock, left, 0);
        addParentFrames(lock, right, 2);

        child.releaseExclusive();
        sibling.releaseExclusive();

        sibling.makeEvictable();
    }

    private void addParentFrames(CursorFrame lock, Node child, int pos) {
        for (CursorFrame frame = child.mLastCursorFrame; frame != null; ) {
            CursorFrame lockResult = frame.tryLock(lock);
            if (lockResult != null) {
                try {
                    CursorFrame parentFrame = frame.getParentFrame();
                    if (parentFrame == null) {
                        parentFrame = new CursorFrame();
                        parentFrame.bind(this, pos);
                        frame.setParentFrame(parentFrame);
                    } else {
                        parentFrame.rebind(this, pos);
                    }
                } finally {
                    frame.unlock(lockResult);
                }
            }

            frame = frame.getPrevCousin();
        }
    }

    public void read(LocalDatabase db, long id) throws IOException {
        db.readNode(this, id);
        try {
            readFields();
        } catch (IllegalStateException e) {
            throw new CorruptDatabaseException(e.getMessage());
        }
    }

    private void readFields() throws IllegalStateException {
        long page = mPage;

        byte type = DirectPageOps.p_byteGet(page, 0);

        // type(type);
        // garbage(p_ushortGetLE(page, 2));

        if (type != TYPE_UNDO_LOG) {
            // leftSegTail(p_ushortGetLE(page, 4));
            // rightSegTail(p_ushortGetLE(page, 6));
            // searchVecStart(p_ushortGetLE(page, 8));
            // searchVecEnd(p_ushortGetLE(page, 10));
            type &= ~(LOW_EXTREMITY | HIGH_EXTREMITY);
            if (type >= 0 && type != TYPE_TN_IN && type != TYPE_TN_BIN) {
                throw new IllegalStateException("Unknown node type: " + type + ", id: " + mId);
            }
        }

        if (DirectPageOps.p_byteGet(page, 1) != 0) {
            throw new IllegalStateException
                    ("Illegal reserved byte in node: " + DirectPageOps.p_byteGet(page, 1) + ", id: " + mId);
        }
    }

    void write(PageDb db) throws WriteFailureException {
        long page = prepareWrite();
        try {
            db.writePage(mId, page);
        } catch (IOException e) {
            throw new WriteFailureException(e);
        }
    }

    private long prepareWrite() {
        if (mSplit != null) {
            throw new AssertionError("Cannot write partially split node");
        }

        long page = mPage;

        // if (type() != TYPE_FRAGMENT) {
        // p_bytePut(page, 0, type());
        // p_bytePut(page, 1, 0);
        // p_shortPutLE(page, 2, garbage());
        // if (type() != TYPE_UNDO_LOG) {
        // p_shortPutLE(page, 4, leftSegTail());
        // p_shortPutLE(page, 6, rightSegTail());
        // p_shortPutLE(page, 8, searchVecStart());
        // p_shortPutLE(page, 10, searchVecEnd());
        // }
        // }
        return page;
    }

    boolean evict(LocalDatabase db) throws IOException {
        CursorFrame last = mLastCursorFrame;

        if (last != null) {
            CursorFrame frame = last;
            do {
                if (!(frame instanceof GhostFrame)) {
                    releaseExclusive();
                    return false;
                }
                frame = frame.getPrevCousin();
            } while (frame != null);

            do {
                frame = last.getPrevCousin();
                CursorFrame.popAll(last);
                last = frame;
            } while (last != null);
        }

        try {
            long id = mId;
            if (id > 0) {
                PageDb pageDb = db.mPageDb;
                if (mCachedState == CACHED_CLEAN) {
                    pageDb.cachePage(id, mPage);
                } else {
                    long page = prepareWrite();
                    long newPage = pageDb.evictPage(id, page);
                    if (newPage != page) {
                        mPage = newPage;
                    }
                    mCachedState = CACHED_CLEAN;
                }

                db.nodeMapRemove(this, Long.hashCode(id));
                mId = 0;

                //type(TYPE_NONE);
            }

            return true;
        } catch (Throwable e) {
            releaseExclusive();
            throw e;
        }
    }

    void invalidateCursors() {
        invalidateCursors(createClosedNode());
    }

    private void invalidateCursors(Node closed) {
        int pos = isLeaf() ? -1 : 0;

        closed.acquireExclusive();
        try {
            for (CursorFrame frame = mLastCursorFrame; frame != null; ) {
                CursorFrame prev = frame.getPrevCousin();
                frame.rebind(closed, pos);
                frame = prev;
            }
        } finally {
            closed.releaseExclusive();
        }

        if (!isInternal()) {
            return;
        }

        LocalDatabase db = mContext.mDatabase;

        closed = null;

        int childPtr = searchVecEnd() + 2;
        final int highestPtr = childPtr + (highestInternalPos() << 2);
        for (; childPtr <= highestPtr; childPtr += 8) {
            long childId = DirectPageOps.p_uint48GetLE(mPage, childPtr);
            Node child = db.nodeMapGetExclusive(childId);
            if (child != null) {
                try {
                    if (closed == null) {
                        closed = createClosedNode();
                    }
                    child.invalidateCursors(closed);
                } finally {
                    child.releaseExclusive();
                }
            }
        }
    }

    private static Node createClosedNode() {
        Node closed = new Node(null, DirectPageOps.p_closedTreePage());
        closed.mId = CLOSED_ID;
        closed.mCachedState = CACHED_CLEAN;
        closed.readFields();
        return closed;
    }

    private int pageSize(long page) {
        // return page.length;
        return mContext.pageSize();
    }

    public byte type() {
        // return mType;
        return DirectPageOps.p_byteGet(mPage, 0);
    }

    public void type(byte type) {
        // mType = type;
        DirectPageOps.p_shortPutLE(mPage, 0, type & 0xff); // clear reserved byte too
    }

    int garbage() {
        // return mGarbage;
        return DirectPageOps.p_ushortGetLE(mPage, 2);
    }

    void garbage(int garbage) {
        // mGarbage = garbage;
        DirectPageOps.p_shortPutLE(mPage, 2, garbage);
    }

    public int undoTop() {
        // return mGarbage;
        return DirectPageOps.p_ushortGetLE(mPage, 2);
    }

    public void undoTop(int top) {
        // mGarbage = top;
        DirectPageOps.p_shortPutLE(mPage, 2, top);
    }

    private int leftSegTail() {
        // return mLeftSegTail;
        return DirectPageOps.p_ushortGetLE(mPage, 4);
    }

    private void leftSegTail(int tail) {
        // mLeftSegTail = tail;
        DirectPageOps.p_shortPutLE(mPage, 4, tail);
    }

    private int rightSegTail() {
        // return mRightSegTail;
        return DirectPageOps.p_ushortGetLE(mPage, 6);
    }

    private void rightSegTail(int tail) {
        // mRightSegTail = tail;
        DirectPageOps.p_shortPutLE(mPage, 6, tail);
    }

    int searchVecStart() {
        // return mSearchVecStart;
        return DirectPageOps.p_ushortGetLE(mPage, 8);
    }

    void searchVecStart(int start) {
        // mSearchVecStart = start;
        DirectPageOps.p_shortPutLE(mPage, 8, start);
    }

    int searchVecEnd() {
        // return mSearchVecEnd;
        return DirectPageOps.p_ushortGetLE(mPage, 10);
    }

    void searchVecEnd(int end) {
        // mSearchVecEnd = end;
        DirectPageOps.p_shortPutLE(mPage, 10, end);
    }

    boolean isLeaf() {
        return type() < 0;
    }

    boolean isInternal() {
        return (type() & 0xe0) == 0x60;
    }

    boolean isBottomInternal() {
        return (type() & 0xf0) == 0x70;
    }

    boolean isNonBottomInternal() {
        return (type() & 0xf0) == 0x60;
    }

    int numKeys() {
        return (searchVecEnd() - searchVecStart() + 2) >> 1;
    }

    boolean hasKeys() {
        return searchVecEnd() >= searchVecStart();
    }

    int highestKeyPos() {
        return searchVecEnd() - searchVecStart();
    }

    int highestPos() {
        int pos = searchVecEnd() - searchVecStart();
        if (!isLeaf()) {
            pos += 2;
        }
        return pos;
    }

    int highestLeafPos() {
        return searchVecEnd() - searchVecStart();
    }

    int highestInternalPos() {
        return searchVecEnd() - searchVecStart() + 2;
    }

    int availableBytes() {
        return isLeaf() ? availableLeafBytes() : availableInternalBytes();
    }

    int availableLeafBytes() {
        return garbage() + searchVecStart() - searchVecEnd()
                - leftSegTail() + rightSegTail() + (1 - 2);
    }

    int availableInternalBytes() {
        return garbage() + 5 * (searchVecStart() - searchVecEnd())
                - leftSegTail() + rightSegTail() + (1 - (5 * 2 + 8));
    }

    int countNonGhostKeys() {
        return countNonGhostKeys(searchVecStart(), searchVecEnd());
    }

    int countNonGhostKeys(int lowPos, int highPos) {
        final long page = mPage;

        int count = 0;
        for (int i = lowPos; i <= highPos; i += 2) {
            int loc = DirectPageOps.p_ushortGetLE(page, i);
            if (DirectPageOps.p_byteGet(page, loc + keyLengthAtLoc(page, loc)) != -1) {
                count++;
            }
        }

        return count;
    }

    boolean shouldLeafMerge() {
        return shouldMerge(availableLeafBytes());
    }

    boolean shouldInternalMerge() {
        return shouldMerge(availableInternalBytes());
    }

    boolean shouldMerge(int availBytes) {
        return mSplit == null
                && (((type() & (LOW_EXTREMITY | HIGH_EXTREMITY)) == 0
                && availBytes >= ((pageSize(mPage) - TN_HEADER_SIZE) >> 1))
                || !hasKeys());
    }

    int binarySearch(byte[] key) throws IOException {
        final long page = mPage;
        final int keyLen = key.length;
        int lowPos = searchVecStart();
        int highPos = searchVecEnd();

        int lowMatch = 0;
        int highMatch = 0;

        outer:
        while (lowPos <= highPos) {
            int midPos = ((lowPos + highPos) >> 1) & ~1;

            int compareLen, i;
            compare:
            {
                int compareLoc = DirectPageOps.p_ushortGetLE(page, midPos);
                compareLen = DirectPageOps.p_byteGet(page, compareLoc++);
                if (compareLen >= 0) {
                    compareLen++;
                } else {
                    int header = compareLen;
                    compareLen = ((compareLen & 0x3f) << 8) | DirectPageOps.p_ubyteGet(page, compareLoc++);

                    if ((header & ENTRY_FRAGMENTED) != 0) {
                        // Note: An optimized version wouldn't need to copy the whole key.
                        byte[] compareKey = getDatabase()
                                .reconstructKey(page, compareLoc, compareLen);
                        compareLen = compareKey.length;

                        int minLen = Math.min(compareLen, keyLen);
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
                return midPos - searchVecStart();
            }
        }

        return ~(lowPos - searchVecStart());
    }

    int binarySearch(byte[] key, int midPos) throws IOException {
        int lowPos = searchVecStart();
        int highPos = searchVecEnd();
        if (lowPos > highPos) {
            return -1;
        }
        midPos += lowPos;
        if (midPos > highPos) {
            midPos = highPos;
        }

        final long page = mPage;
        final int keyLen = key.length;

        int lowMatch = 0;
        int highMatch = 0;

        while (true) {
            compare:
            {
                int compareLen, i;
                c2:
                {
                    int compareLoc = DirectPageOps.p_ushortGetLE(page, midPos);
                    compareLen = DirectPageOps.p_byteGet(page, compareLoc++);
                    if (compareLen >= 0) {
                        compareLen++;
                    } else {
                        int header = compareLen;
                        compareLen = ((compareLen & 0x3f) << 8) | DirectPageOps.p_ubyteGet(page, compareLoc++);

                        if ((header & ENTRY_FRAGMENTED) != 0) {
                            byte[] compareKey = getDatabase()
                                    .reconstructKey(page, compareLoc, compareLen);
                            compareLen = compareKey.length;

                            int minLen = Math.min(compareLen, keyLen);
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
                                    break compare;
                                }
                            }

                            break c2;
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
                            break compare;
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
                    return midPos - searchVecStart();
                }
            }

            if (lowPos > highPos) {
                break;
            }

            midPos = ((lowPos + highPos) >> 1) & ~1;
        }

        return ~(lowPos - searchVecStart());
    }

    static int internalPos(int pos) {
        return pos < 0 ? ~pos : (pos + 2);
    }

    int compareKey(int pos, byte[] rightKey) throws IOException {
        final long page = mPage;
        int loc = DirectPageOps.p_ushortGetLE(page, searchVecStart() + pos);
        int keyLen = DirectPageOps.p_byteGet(page, loc++);
        if (keyLen >= 0) {
            keyLen++;
        } else {
            int header = keyLen;
            keyLen = ((keyLen & 0x3f) << 8) | DirectPageOps.p_ubyteGet(page, loc++);
            if ((header & ENTRY_FRAGMENTED) != 0) {
                // Note: An optimized version wouldn't need to copy the whole key.
                byte[] leftKey = getDatabase().reconstructKey(page, loc, keyLen);
                return Utils.compareUnsigned(leftKey, 0, leftKey.length, rightKey, 0, rightKey.length);
            }
        }
        return DirectPageOps.p_compareKeysPageToArray(page, loc, keyLen, rightKey, 0, rightKey.length);
    }

    static int compareKeys(Node left, int leftLoc, Node right, int rightLoc) throws IOException {
        final long leftPage = left.mPage;
        final long rightPage = right.mPage;

        int leftLen = DirectPageOps.p_byteGet(leftPage, leftLoc++);
        int rightLen = DirectPageOps.p_byteGet(rightPage, rightLoc++);

        c1:
        {
            c2:
            {
                if (leftLen >= 0) {
                    leftLen++;
                    break c2;
                }

                int leftHeader = leftLen;
                leftLen = ((leftLen & 0x3f) << 8) | DirectPageOps.p_ubyteGet(leftPage, leftLoc++);
                if ((leftHeader & ENTRY_FRAGMENTED) == 0) {
                    break c2;
                }

                byte[] leftKey = left.getDatabase().reconstructKey(leftPage, leftLoc, leftLen);

                if (rightLen >= 0) {
                    rightLen++;
                } else {
                    int rightHeader = rightLen;
                    rightLen = ((rightLen & 0x3f) << 8) | DirectPageOps.p_ubyteGet(rightPage, rightLoc++);
                    if ((rightHeader & ENTRY_FRAGMENTED) != 0) {
                        byte[] rightKey = right.getDatabase()
                                .reconstructKey(rightPage, rightLoc, rightLen);
                        return Utils.compareUnsigned(leftKey, 0, leftKey.length,
                                rightKey, 0, rightKey.length);
                    }
                }

                return -DirectPageOps.p_compareKeysPageToArray(rightPage, rightLoc, rightLen,
                        leftKey, 0, leftKey.length);
            } // end c2

            if (rightLen >= 0) {
                rightLen++;
                break c1;
            }

            int rightHeader = rightLen;
            rightLen = ((rightLen & 0x3f) << 8) | DirectPageOps.p_ubyteGet(rightPage, rightLoc++);
            if ((rightHeader & ENTRY_FRAGMENTED) == 0) {
                break c1;
            }

            byte[] rightKey = right.getDatabase().reconstructKey(rightPage, rightLoc, rightLen);
            return DirectPageOps.p_compareKeysPageToArray(leftPage, leftLoc, leftLen,
                    rightKey, 0, rightKey.length);
        } // end c1

        return DirectPageOps.p_compareKeysPageToPage(leftPage, leftLoc, leftLen, rightPage, rightLoc, rightLen);
    }

    void retrieveKeyStats(int pos, long[] stats) throws IOException {
        final long page = mPage;
        int loc = DirectPageOps.p_ushortGetLE(page, searchVecStart() + pos);

        int keyLen = DirectPageOps.p_byteGet(page, loc++);
        if (keyLen >= 0) {
            keyLen++;
        } else {
            int header = keyLen;
            keyLen = ((keyLen & 0x3f) << 8) | DirectPageOps.p_ubyteGet(page, loc++);
            if ((header & ENTRY_FRAGMENTED) != 0) {
                getDatabase().reconstruct(page, loc, keyLen, stats);
                return;
            }
        }

        stats[0] = keyLen;
        stats[1] = 0;
    }

    byte[] retrieveKey(int pos) throws IOException {
        final long page = mPage;
        return retrieveKeyAtLoc(this, page, DirectPageOps.p_ushortGetLE(page, searchVecStart() + pos));
    }

    byte[] retrieveKeyAtLoc(final long page, int loc) throws IOException {
        return retrieveKeyAtLoc(this, page, loc);
    }

    public static byte[] retrieveKeyAtLoc(DatabaseAccess dbAccess, final long page, int loc)
            throws IOException {
        int keyLen = DirectPageOps.p_byteGet(page, loc++);
        if (keyLen >= 0) {
            keyLen++;
        } else {
            int header = keyLen;
            keyLen = ((keyLen & 0x3f) << 8) | DirectPageOps.p_ubyteGet(page, loc++);
            if ((header & ENTRY_FRAGMENTED) != 0) {
                return dbAccess.getDatabase().reconstructKey(page, loc, keyLen);
            }
        }
        byte[] key = new byte[keyLen];
        DirectPageOps.p_copyToArray(page, loc, key, 0, keyLen);
        return key;
    }

    private boolean retrieveActualKeyAtLoc(final long page, int loc,
                                           final byte[][] akeyRef)
            throws IOException {
        boolean result = true;

        int keyLen = DirectPageOps.p_byteGet(page, loc++);
        if (keyLen >= 0) {
            keyLen++;
        } else {
            int header = keyLen;
            keyLen = ((keyLen & 0x3f) << 8) | DirectPageOps.p_ubyteGet(page, loc++);
            result = (header & ENTRY_FRAGMENTED) == 0;
        }
        byte[] akey = new byte[keyLen];
        DirectPageOps.p_copyToArray(page, loc, akey, 0, keyLen);
        akeyRef[0] = akey;

        return result;
    }

    byte[] retrieveKeyCmp(int pos, byte[] limitKey, int limitMode) throws IOException {
        final long page = mPage;
        int loc = DirectPageOps.p_ushortGetLE(page, searchVecStart() + pos);
        int keyLen = DirectPageOps.p_byteGet(page, loc++);
        if (keyLen >= 0) {
            keyLen++;
        } else {
            int header = keyLen;
            keyLen = ((keyLen & 0x3f) << 8) | DirectPageOps.p_ubyteGet(page, loc++);

            if ((header & ENTRY_FRAGMENTED) != 0) {
                byte[] key = getDatabase().reconstructKey(page, loc, keyLen);
                int cmp = Utils.compareUnsigned(key, limitKey);
                if (cmp == 0) {
                    return limitKey;
                } else {
                    return (cmp ^ limitMode) < 0 ? key : null;
                }
            }
        }

        int cmp = DirectPageOps.p_compareKeysPageToArray(page, loc, keyLen, limitKey, 0, limitKey.length);
        if (cmp == 0) {
            return limitKey;
        } else if ((cmp ^ limitMode) < 0) {
            byte[] key = new byte[keyLen];
            DirectPageOps.p_copyToArray(page, loc, key, 0, keyLen);
            return key;
        } else {
            return null;
        }
    }

    public static byte[][] retrieveKeyValueAtLoc(DatabaseAccess dbAccess,
                                                 final long page, int loc)
            throws IOException {
        int header = DirectPageOps.p_byteGet(page, loc++);

        int keyLen;
        byte[] key;
        copyKey:
        {
            if (header >= 0) {
                keyLen = header + 1;
            } else {
                keyLen = ((header & 0x3f) << 8) | DirectPageOps.p_ubyteGet(page, loc++);
                if ((header & ENTRY_FRAGMENTED) != 0) {
                    key = dbAccess.getDatabase().reconstructKey(page, loc, keyLen);
                    break copyKey;
                }
            }
            key = new byte[keyLen];
            DirectPageOps.p_copyToArray(page, loc, key, 0, keyLen);
        }

        return new byte[][]{key, retrieveLeafValueAtLoc(null, page, loc + keyLen)};
    }

    public static byte[] expandKeyAtLoc(DatabaseAccess dbAccess, long page, int loc, int len,
                                        boolean stripValueHeader)
            throws IOException {
        int endLoc = loc + len;

        int keyLen = ((DirectPageOps.p_byteGet(page, loc++) & 0x3f) << 8) | DirectPageOps.p_ubyteGet(page, loc++);

        int valueLoc = loc + keyLen;
        int valueLen = endLoc - valueLoc;

        if (stripValueHeader) {
            int skip = 1;
            int header = DirectPageOps.p_byteGet(page, valueLoc);
            if (header < 0) {
                if ((header & 0x20) == 0) {
                    skip = 2;
                } else if (header != -1) {
                    skip = 3;
                }
            }
            valueLoc += skip;
            valueLen -= skip;
        }

        byte[] key = dbAccess.getDatabase().reconstructKey(page, loc, keyLen);
        int keyHeaderLen = Utils.calcUnsignedVarIntLength(key.length);

        byte[] expanded = new byte[keyHeaderLen + key.length + valueLen];

        int offset = Utils.encodeUnsignedVarInt(expanded, 0, key.length);
        System.arraycopy(key, 0, expanded, offset, key.length);
        offset += key.length;
        DirectPageOps.p_copyToArray(page, valueLoc, expanded, offset, valueLen);

        return expanded;
    }

    private byte[] midKey(int lowPos, byte[] highKey) throws IOException {
        final long lowPage = mPage;
        int lowLoc = DirectPageOps.p_ushortGetLE(lowPage, searchVecStart() + lowPos);
        int lowKeyLen = DirectPageOps.p_byteGet(lowPage, lowLoc);
        if (lowKeyLen < 0) {
            return Utils.midKey(retrieveKeyAtLoc(lowPage, lowLoc), highKey);
        } else {
            return DirectPageOps.p_midKeyLowPage(lowPage, lowLoc + 1, lowKeyLen + 1, highKey, 0);
        }
    }

    private byte[] midKey(byte[] lowKey, int highPos) throws IOException {
        final long highPage = mPage;
        int highLoc = DirectPageOps.p_ushortGetLE(highPage, searchVecStart() + highPos);
        int highKeyLen = DirectPageOps.p_byteGet(highPage, highLoc);
        if (highKeyLen < 0) {
            return Utils.midKey(lowKey, retrieveKeyAtLoc(highPage, highLoc));
        } else {
            return DirectPageOps.p_midKeyHighPage(lowKey, 0, lowKey.length, highPage, highLoc + 1);
        }
    }

    byte[] midKey(int lowPos, Node highNode, int highPos) throws IOException {
        final long lowPage = mPage;
        int lowLoc = DirectPageOps.p_ushortGetLE(lowPage, searchVecStart() + lowPos);
        int lowKeyLen = DirectPageOps.p_byteGet(lowPage, lowLoc);
        if (lowKeyLen < 0) {
            return highNode.midKey(retrieveKeyAtLoc(lowPage, lowLoc), highPos);
        }

        lowLoc++;
        lowKeyLen++;

        final long highPage = highNode.mPage;
        int highLoc = DirectPageOps.p_ushortGetLE(highPage, highNode.searchVecStart() + highPos);
        int highKeyLen = DirectPageOps.p_byteGet(highPage, highLoc);
        if (highKeyLen < 0) {
            byte[] highKey = retrieveKeyAtLoc(highPage, highLoc);
            return DirectPageOps.p_midKeyLowPage(lowPage, lowLoc, lowKeyLen, highKey, 0);
        }

        return DirectPageOps.p_midKeyLowHighPage(lowPage, lowLoc, lowKeyLen, highPage, highLoc + 1);
    }

    public byte[] hasLeafValue(int pos) {
        final long page = mPage;
        int loc = DirectPageOps.p_ushortGetLE(page, searchVecStart() + pos);
        loc += keyLengthAtLoc(page, loc);
        return DirectPageOps.p_byteGet(page, loc) == -1 ? null : Cursor.NOT_LOADED;
    }

    void retrieveLeafValueStats(int pos, long[] stats) throws IOException {
        final long page = mPage;
        int loc = DirectPageOps.p_ushortGetLE(page, searchVecStart() + pos);
        loc += keyLengthAtLoc(page, loc);

        final int header = DirectPageOps.p_byteGet(page, loc++);

        int len;
        if (header >= 0) {
            len = header;
        } else {
            if ((header & 0x20) == 0) {
                len = 1 + (((header & 0x1f) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
            } else if (header != -1) {
                len = 1 + (((header & 0x0f) << 16)
                        | (DirectPageOps.p_ubyteGet(page, loc++) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
            } else {
                stats[0] = 0;
                stats[1] = 0;
                return;
            }
            if ((header & ENTRY_FRAGMENTED) != 0) {
                getDatabase().reconstruct(page, loc, len, stats);
                return;
            }
        }

        stats[0] = len;
        stats[1] = 0;
    }

    byte[] retrieveLeafValue(int pos) throws IOException {
        final long page = mPage;
        int loc = DirectPageOps.p_ushortGetLE(page, searchVecStart() + pos);
        loc += keyLengthAtLoc(page, loc);
        return retrieveLeafValueAtLoc(this, page, loc);
    }

    static byte[] retrieveLeafValueAtLoc(DatabaseAccess dbAccess, long page, int loc)
            throws IOException {
        final int header = DirectPageOps.p_byteGet(page, loc++);
        if (header == 0) {
            return Utils.EMPTY_BYTES;
        }

        int len;
        if (header >= 0) {
            len = header;
        } else {
            if ((header & 0x20) == 0) {
                len = 1 + (((header & 0x1f) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
            } else if (header != -1) {
                len = 1 + (((header & 0x0f) << 16)
                        | (DirectPageOps.p_ubyteGet(page, loc++) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
            } else {
                return null;
            }
            if ((header & ENTRY_FRAGMENTED) != 0) {
                return dbAccess.getDatabase().reconstruct(page, loc, len);
            }
        }

        byte[] value = new byte[len];
        DirectPageOps.p_copyToArray(page, loc, value, 0, len);
        return value;
    }

    void retrieveLeafEntry(int pos, TreeCursor cursor) throws IOException {
        final long page = mPage;
        int loc = DirectPageOps.p_ushortGetLE(page, searchVecStart() + pos);
        int header = DirectPageOps.p_byteGet(page, loc++);

        int keyLen;
        byte[] key;
        copyKey:
        {
            if (header >= 0) {
                keyLen = header + 1;
            } else {
                keyLen = ((header & 0x3f) << 8) | DirectPageOps.p_ubyteGet(page, loc++);
                if ((header & ENTRY_FRAGMENTED) != 0) {
                    key = getDatabase().reconstructKey(page, loc, keyLen);
                    break copyKey;
                }
            }
            key = new byte[keyLen];
            DirectPageOps.p_copyToArray(page, loc, key, 0, keyLen);
        }

        loc += keyLen;
        cursor.mKey = key;

        byte[] value;
        if (cursor.mKeyOnly) {
            value = DirectPageOps.p_byteGet(page, loc) == -1 ? null : Cursor.NOT_LOADED;
        } else {
            value = retrieveLeafValueAtLoc(this, page, loc);
        }

        cursor.mValue = value;
    }

    boolean isFragmentedLeafValue(int pos) {
        final long page = mPage;
        int loc = DirectPageOps.p_ushortGetLE(page, searchVecStart() + pos);
        loc += keyLengthAtLoc(page, loc);
        int header = DirectPageOps.p_byteGet(page, loc);
        return ((header & 0xc0) >= 0xc0) & (header < -1);
    }

    void txnDeleteLeafEntry(LocalTransaction txn, Tree tree, byte[] key, int keyHash, int pos)
            throws IOException {
        GhostFrame frame = new GhostFrame();

        final long page = mPage;
        final int entryLoc = DirectPageOps.p_ushortGetLE(page, searchVecStart() + pos);
        int loc = entryLoc;

        loc += keyLengthAtLoc(page, loc);

        final int valueHeaderLoc = loc;
        int header = DirectPageOps.p_byteGet(page, loc++);

        doUndo:
        {
            if (header >= 0) {
                loc += header;
            } else {
                if ((header & 0x20) == 0) {
                    loc += 2 + (((header & 0x1f) << 8) | DirectPageOps.p_ubyteGet(page, loc));
                } else if (header != -1) {
                    loc += 3 + (((header & 0x0f) << 16)
                            | (DirectPageOps.p_ubyteGet(page, loc) << 8) | DirectPageOps.p_ubyteGet(page, loc + 1));
                } else {
                    break doUndo;
                }

                if ((header & ENTRY_FRAGMENTED) != 0) {
                    int valueStartLoc = valueHeaderLoc + 2 + ((header & 0x20) >> 5);
                    tree.mDatabase.fragmentedTrash().add
                            (txn, tree.mId, page,
                                    entryLoc, valueHeaderLoc - entryLoc,  // keyStart, keyLen
                                    valueStartLoc, loc - valueStartLoc);  // valueStart, valueLen
                    break doUndo;
                }
            }

            txn.pushUndoStore(tree.mId, UndoLog.OP_UNDELETE, page, entryLoc, loc - entryLoc);
        }

        frame.bind(this, pos);

        tree.mLockManager.ghosted(tree.mId, key, keyHash, frame);

        DirectPageOps.p_bytePut(page, valueHeaderLoc, -1);
        garbage(garbage() + loc - valueHeaderLoc - 1);
    }

    void txnPreUpdateLeafEntry(LocalTransaction txn, Tree tree, byte[] key, int pos)
            throws IOException {
        final long page = mPage;
        final int entryLoc = DirectPageOps.p_ushortGetLE(page, searchVecStart() + pos);
        int loc = entryLoc;

        loc += keyLengthAtLoc(page, loc);

        final int valueHeaderLoc = loc;
        int header = DirectPageOps.p_byteGet(page, loc++);

        examineEntry:
        {
            if (header >= 0) {
                loc += header;
                break examineEntry;
            } else {
                if ((header & 0x20) == 0) {
                    loc += 2 + (((header & 0x1f) << 8) | DirectPageOps.p_ubyteGet(page, loc));
                } else if (header != -1) {
                    loc += 3 + (((header & 0x0f) << 16)
                            | (DirectPageOps.p_ubyteGet(page, loc) << 8) | DirectPageOps.p_ubyteGet(page, loc + 1));
                } else {
                    break examineEntry;
                }

                if ((header & ENTRY_FRAGMENTED) != 0) {
                    int valueStartLoc = valueHeaderLoc + 2 + ((header & 0x20) >> 5);
                    tree.mDatabase.fragmentedTrash().add
                            (txn, tree.mId, page,
                                    entryLoc, valueHeaderLoc - entryLoc,  // keyStart, keyLen
                                    valueStartLoc, loc - valueStartLoc);  // valueStart, valueLen
                    DirectPageOps.p_bytePut(page, valueHeaderLoc, header & ~ENTRY_FRAGMENTED);
                    return;
                }
            }
        }

        txn.pushUndoStore(tree.mId, UndoLog.OP_UNUPDATE, page, entryLoc, loc - entryLoc);
    }

    long retrieveChildRefId(int pos) {
        return DirectPageOps.p_uint48GetLE(mPage, searchVecEnd() + 2 + (pos << 2));
    }

    int retrieveChildEntryCount(int pos) {
        return DirectPageOps.p_ushortGetLE(mPage, searchVecEnd() + (2 + 6) + (pos << 2)) - 1;
    }

    void storeChildEntryCount(int pos, int count) {
        if (count < 65535) { // safety check
            DirectPageOps.p_shortPutLE(mPage, searchVecEnd() + (2 + 6) + (pos << 2), count + 1);
        }
    }

    static int leafEntryLengthAtLoc(long page, final int entryLoc) {
        int loc = entryLoc + keyLengthAtLoc(page, entryLoc);
        int header = DirectPageOps.p_byteGet(page, loc++);
        if (header >= 0) {
            loc += header;
        } else {
            if ((header & 0x20) == 0) {
                loc += 2 + (((header & 0x1f) << 8) | DirectPageOps.p_ubyteGet(page, loc));
            } else if (header != -1) {
                loc += 3 + (((header & 0x0f) << 16)
                        | (DirectPageOps.p_ubyteGet(page, loc) << 8) | DirectPageOps.p_ubyteGet(page, loc + 1));
            }
        }
        return loc - entryLoc;
    }

    static int keyLengthAtLoc(long page, final int keyLoc) {
        int header = DirectPageOps.p_byteGet(page, keyLoc);
        return (header >= 0 ? header
                : (((header & 0x3f) << 8) | DirectPageOps.p_ubyteGet(page, keyLoc + 1))) + 2;
    }

    void insertLeafEntry(CursorFrame frame, Tree tree, int pos, byte[] okey, byte[] value)
            throws IOException {
        final LocalDatabase db = tree.mDatabase;

        byte[] akey = okey;
        int encodedKeyLen = calculateAllowedKeyLength(db, okey);

        if (encodedKeyLen < 0) {
            akey = db.fragmentKey(okey);
            encodedKeyLen = 2 + akey.length;
        }

        try {
            int encodedLen = encodedKeyLen + calculateLeafValueLength(value);

            int vfrag;
            if (encodedLen <= db.mMaxEntrySize) {
                vfrag = 0;
            } else {
                value = db.fragment(value, value.length,
                        db.mMaxFragmentedEntrySize - encodedKeyLen);
                if (value == null) {
                    throw new AssertionError();
                }
                encodedLen = encodedKeyLen + calculateFragmentedValueLength(value);
                vfrag = ENTRY_FRAGMENTED;
            }

            try {
                int entryLoc = createLeafEntry(frame, tree, pos, encodedLen);

                if (entryLoc < 0) {
                    splitLeafAndCreateEntry(tree, okey, akey, vfrag, value, encodedLen, pos, true);
                } else {
                    //数据
                    copyToLeafEntry(okey, akey, vfrag, value, entryLoc);
                }
            } catch (Throwable e) {
                if (vfrag == ENTRY_FRAGMENTED) {
                    cleanupFragments(e, value);
                }
                throw e;
            }
        } catch (Throwable e) {
            if (okey != akey) {
                cleanupFragments(e, akey);
            }
            throw e;
        }
    }

    void insertBlankLeafEntry(CursorFrame frame, Tree tree, int pos, byte[] okey, long vlength)
            throws IOException {
        final LocalDatabase db = tree.mDatabase;

        byte[] akey = okey;
        int encodedKeyLen = calculateAllowedKeyLength(db, okey);

        if (encodedKeyLen < 0) {
            akey = db.fragmentKey(okey);
            encodedKeyLen = 2 + akey.length;
        }

        try {
            long longEncodedLen = encodedKeyLen + calculateLeafValueLength(vlength);
            int encodedLen;

            int vfrag;
            byte[] value;
            if (longEncodedLen <= db.mMaxEntrySize) {
                vfrag = 0;
                value = new byte[(int) vlength];
                encodedLen = (int) longEncodedLen;
            } else {
                value = db.fragment(null, vlength, db.mMaxFragmentedEntrySize - encodedKeyLen);
                if (value == null) {
                    throw new AssertionError();
                }
                encodedLen = encodedKeyLen + calculateFragmentedValueLength(value);
                vfrag = ENTRY_FRAGMENTED;
            }

            try {
                int entryLoc = createLeafEntry(frame, tree, pos, encodedLen);

                if (entryLoc < 0) {
                    splitLeafAndCreateEntry(tree, okey, akey, vfrag, value, encodedLen, pos, true);
                } else {
                    copyToLeafEntry(okey, akey, vfrag, value, entryLoc);
                }
            } catch (Throwable e) {
                if (vfrag == ENTRY_FRAGMENTED) {
                    cleanupFragments(e, value);
                }
                throw e;
            }
        } catch (Throwable e) {
            if (okey != akey) {
                cleanupFragments(e, akey);
            }
            throw e;
        }
    }

    void insertFragmentedLeafEntry(CursorFrame frame,
                                   Tree tree, int pos, byte[] okey, byte[] value)
            throws IOException {
        final LocalDatabase db = tree.mDatabase;

        byte[] akey = okey;
        int encodedKeyLen = calculateAllowedKeyLength(db, okey);

        if (encodedKeyLen < 0) {
            akey = db.fragmentKey(okey);
            encodedKeyLen = 2 + akey.length;
        }

        try {
            int encodedLen = encodedKeyLen + calculateFragmentedValueLength(value);

            int entryLoc = createLeafEntry(frame, tree, pos, encodedLen);

            if (entryLoc < 0) {
                splitLeafAndCreateEntry
                        (tree, okey, akey, ENTRY_FRAGMENTED, value, encodedLen, pos, true);
            } else {
                copyToLeafEntry(okey, akey, ENTRY_FRAGMENTED, value, entryLoc);
            }
        } catch (Throwable e) {
            if (okey != akey) {
                cleanupFragments(e, akey);
            }
            throw e;
        }
    }

    private void panic(Throwable cause) {
        try {
            getDatabase().close(cause);
        } catch (Throwable e) {
            // Ignore.
        }
    }

    void cleanupFragments(Throwable cause, byte[] fragmented) {
        if (fragmented != null) {
            long copy = DirectPageOps.p_transfer(fragmented, false);
            try {
                getDatabase().deleteFragments(copy, 0, fragmented.length);
            } catch (Throwable e) {
                Utils.suppress(cause, e);
                panic(cause);
            } finally {
                DirectPageOps.p_delete(copy);
            }
        }
    }

    int createLeafEntry(final CursorFrame frame, Tree tree, int pos, final int encodedLen) {
        int searchVecStart = searchVecStart();
        int searchVecEnd = searchVecEnd();

        int leftSpace = searchVecStart - leftSegTail();
        int rightSpace = rightSegTail() - searchVecEnd - 1;

        final long page = mPage;

        int entryLoc;
        alloc:
        {
            if (pos < ((searchVecEnd - searchVecStart + 2) >> 1)) {
                if ((leftSpace -= 2) >= 0 &&
                        (entryLoc = allocPageEntry(encodedLen, leftSpace, rightSpace)) >= 0) {
                    DirectPageOps.p_copy(page, searchVecStart, page, searchVecStart -= 2, pos);
                    pos += searchVecStart;
                    searchVecStart(searchVecStart);
                    break alloc;
                }
                leftSpace += 2;
            } else {
                if ((rightSpace -= 2) >= 0 &&
                        (entryLoc = allocPageEntry(encodedLen, leftSpace, rightSpace)) >= 0) {
                    pos += searchVecStart;
                    DirectPageOps.p_copy(page, pos, page, pos + 2, (searchVecEnd += 2) - pos);
                    searchVecEnd(searchVecEnd);
                    break alloc;
                }
                rightSpace += 2;
            }

            int remaining = leftSpace + rightSpace - encodedLen - 2;

            if (garbage() > remaining) {
                if (garbage() + remaining >= 0) {
                    return compactLeaf(encodedLen, pos, true);
                }

                CursorFrame parentFrame;
                if (frame != null && (parentFrame = frame.getParentFrame()) != null) {
                    int result = tryRebalanceLeaf(tree, parentFrame, pos, encodedLen, -remaining);
                    if (result > 0) {
                        return result;
                    }
                }

                return ~(garbage() + leftSpace + rightSpace);
            }

            int vecLen = searchVecEnd - searchVecStart + 2;
            int newSearchVecStart;

            if (remaining > 0 || (rightSegTail() & 1) != 0) {
                newSearchVecStart = (rightSegTail() - vecLen + (1 - 2) - (remaining >> 1)) & ~1;

                entryLoc = leftSegTail();
                leftSegTail(entryLoc + encodedLen);
            } else if ((leftSegTail() & 1) == 0) {
                newSearchVecStart = leftSegTail() + ((remaining >> 1) & ~1);

                entryLoc = rightSegTail() - encodedLen + 1;
                rightSegTail(entryLoc - 1);
            } else {
                return compactLeaf(encodedLen, pos, true);
            }

            DirectPageOps.p_copies(page,
                    searchVecStart, newSearchVecStart, pos,
                    searchVecStart + pos, newSearchVecStart + pos + 2, vecLen - pos);

            pos += newSearchVecStart;
            searchVecStart(newSearchVecStart);
            searchVecEnd(newSearchVecStart + vecLen);
        }

        DirectPageOps.p_shortPutLE(page, pos, entryLoc);
        return entryLoc;
    }

    private int tryRebalanceLeaf(Tree tree, CursorFrame parentFrame,
                                 int pos, int insertLen, int minAmount) {
        int result;
        if ((mId & 1) == 0) {
            result = tryRebalanceLeafLeft(tree, parentFrame, pos, insertLen, minAmount);
            if (result <= 0) {
                result = tryRebalanceLeafRight(tree, parentFrame, pos, insertLen, minAmount);
            }
        } else {
            result = tryRebalanceLeafRight(tree, parentFrame, pos, insertLen, minAmount);
            if (result <= 0) {
                result = tryRebalanceLeafLeft(tree, parentFrame, pos, insertLen, minAmount);
            }
        }
        return result;
    }

    private int tryRebalanceLeafLeft(Tree tree, CursorFrame parentFrame,
                                     int pos, int insertLen, int minAmount) {
        final long rightPage = mPage;

        int moveAmount = 0;
        final int lastSearchVecLoc;
        int insertLoc = 0;
        int insertSlack = Integer.MAX_VALUE;

        check:
        {
            int searchVecLoc = searchVecStart();
            int searchVecEnd = searchVecLoc + pos - 2;

            for (; searchVecLoc < searchVecEnd; searchVecLoc += 2) {
                int entryLoc = DirectPageOps.p_ushortGetLE(rightPage, searchVecLoc);
                int encodedLen = leafEntryLengthAtLoc(rightPage, entryLoc);

                int slack = encodedLen - insertLen;
                if (slack >= 0 && slack < insertSlack) {
                    insertLoc = entryLoc;
                    insertSlack = slack;
                }

                moveAmount += encodedLen + 2;
                if (moveAmount >= minAmount && insertLoc != 0) {
                    lastSearchVecLoc = searchVecLoc + 2; // +2 to be exclusive
                    break check;
                }
            }

            return 0;
        }

        final Node parent = parentFrame.tryAcquireExclusive();
        if (parent == null) {
            return 0;
        }

        final int childPos = parentFrame.getNodePos();
        if (childPos <= 0
                || parent.mSplit != null
                || parent.mCachedState != mCachedState) {
            parent.releaseExclusive();
            return 0;
        }

        final Node left;
        try {
            left = parent.tryLatchChildNotSplit(childPos - 2);
        } catch (IOException e) {
            return 0;
        }

        if (left == null) {
            parent.releaseExclusive();
            return 0;
        }

        final byte[] newKey;
        final int newKeyLen;
        final long parentPage;
        final int parentKeyLoc;
        final int parentKeyGrowth;

        check:
        {
            try {
                int leftAvail = left.availableLeafBytes();
                if (leftAvail >= moveAmount) {
                    int highPos = lastSearchVecLoc - searchVecStart();
                    newKey = midKey(highPos - 2, this, highPos);
                    newKeyLen = calculateAllowedKeyLength(tree.mDatabase, newKey);
                    if (newKeyLen > 0) {
                        parentPage = parent.mPage;
                        parentKeyLoc = DirectPageOps.p_ushortGetLE
                                (parentPage, parent.searchVecStart() + childPos - 2);
                        parentKeyGrowth = newKeyLen - keyLengthAtLoc(parentPage, parentKeyLoc);
                        if (parentKeyGrowth <= 0 ||
                                parentKeyGrowth <= parent.availableInternalBytes()) {
                            break check;
                        }
                    }
                }
            } catch (IOException e) {
            }
            left.releaseExclusive();
            parent.releaseExclusive();
            return 0;
        }

        try {
            if (tree.mDatabase.markDirty(tree, left)) {
                parent.updateChildRefId(childPos - 2, left.mId);
            }
        } catch (IOException e) {
            left.releaseExclusive();
            parent.releaseExclusive();
            return 0;
        }

        if (parentKeyGrowth <= 0) {
            encodeNormalKey(newKey, parentPage, parentKeyLoc);
            parent.garbage(parent.garbage() - parentKeyGrowth);
        } else {
            parent.updateInternalKey(childPos - 2, parentKeyGrowth, newKey, newKeyLen);
        }

        int garbageAccum = 0;
        int searchVecLoc = searchVecStart();
        final int lastPos = lastSearchVecLoc - searchVecLoc;

        for (; searchVecLoc < lastSearchVecLoc; searchVecLoc += 2) {
            int entryLoc = DirectPageOps.p_ushortGetLE(rightPage, searchVecLoc);
            int encodedLen = leafEntryLengthAtLoc(rightPage, entryLoc);
            int leftEntryLoc = left.createLeafEntry
                    (null, tree, left.highestLeafPos() + 2, encodedLen);
            DirectPageOps.p_copy(rightPage, entryLoc, left.mPage, leftEntryLoc, encodedLen);
            garbageAccum += encodedLen;
        }

        garbage(garbage() + garbageAccum);
        searchVecStart(lastSearchVecLoc);

        final int leftEndPos = left.highestLeafPos() + 2;
        for (CursorFrame frame = mLastCursorFrame; frame != null; ) {
            CursorFrame prev = frame.getPrevCousin();
            int framePos = frame.getNodePos();
            int mask = framePos >> 31;
            int newPos = (framePos ^ mask) - lastPos;
            byte[] frameKey;
            if (newPos < 0 |
                    ((newPos == 0 & mask != 0) &&
                            ((frameKey = frame.getNotFoundKey()) != null &&
                                    Utils.compareUnsigned(frameKey, newKey) < 0))) {
                frame.rebind(left, (leftEndPos + newPos) ^ mask);
                frame.adjustParentPosition(-2);
            } else {
                frame.setNodePos(newPos ^ mask);
            }
            frame = prev;
        }

        left.releaseExclusive();
        parent.releaseExclusive();

        garbage(garbage() - insertLen);
        pos -= lastPos;
        int searchVecStart = searchVecStart();
        DirectPageOps.p_copy(rightPage, searchVecStart, rightPage, searchVecStart -= 2, pos);
        searchVecStart(searchVecStart);
        DirectPageOps.p_shortPutLE(rightPage, searchVecStart + pos, insertLoc);
        return insertLoc;
    }

    private int tryRebalanceLeafRight(Tree tree, CursorFrame parentFrame,
                                      int pos, int insertLen, int minAmount) {
        final long leftPage = mPage;

        int moveAmount = 0;
        final int firstSearchVecLoc;
        int insertLoc = 0;
        int insertSlack = Integer.MAX_VALUE;

        check:
        {
            int searchVecStart = searchVecStart() + pos;
            int searchVecLoc = searchVecEnd();

            for (; searchVecLoc > searchVecStart; searchVecLoc -= 2) {
                int entryLoc = DirectPageOps.p_ushortGetLE(leftPage, searchVecLoc);
                int encodedLen = leafEntryLengthAtLoc(leftPage, entryLoc);

                int slack = encodedLen - insertLen;
                if (slack >= 0 && slack < insertSlack) {
                    insertLoc = entryLoc;
                    insertSlack = slack;
                }

                moveAmount += encodedLen + 2;
                if (moveAmount >= minAmount && insertLoc != 0) {
                    firstSearchVecLoc = searchVecLoc;
                    break check;
                }
            }
            return 0;
        }

        final Node parent = parentFrame.tryAcquireExclusive();
        if (parent == null) {
            return 0;
        }

        final int childPos = parentFrame.getNodePos();
        if (childPos >= parent.highestInternalPos()
                || parent.mSplit != null
                || parent.mCachedState != mCachedState) {
            parent.releaseExclusive();
            return 0;
        }

        final Node right;
        try {
            right = parent.tryLatchChildNotSplit(childPos + 2);
        } catch (IOException e) {
            return 0;
        }

        if (right == null) {
            parent.releaseExclusive();
            return 0;
        }

        final byte[] newKey;
        final int newKeyLen;
        final long parentPage;
        final int parentKeyLoc;
        final int parentKeyGrowth;

        check:
        {
            try {
                int rightAvail = right.availableLeafBytes();
                if (rightAvail >= moveAmount) {
                    int highPos = firstSearchVecLoc - searchVecStart();
                    newKey = midKey(highPos - 2, this, highPos);
                    newKeyLen = calculateAllowedKeyLength(tree.mDatabase, newKey);
                    if (newKeyLen > 0) {
                        parentPage = parent.mPage;
                        parentKeyLoc = DirectPageOps.p_ushortGetLE
                                (parentPage, parent.searchVecStart() + childPos);
                        parentKeyGrowth = newKeyLen - keyLengthAtLoc(parentPage, parentKeyLoc);
                        if (parentKeyGrowth <= 0 ||
                                parentKeyGrowth <= parent.availableInternalBytes()) {
                            break check;
                        }
                    }
                }
            } catch (IOException e) {
            }
            right.releaseExclusive();
            parent.releaseExclusive();
            return 0;
        }

        try {
            if (tree.mDatabase.markDirty(tree, right)) {
                parent.updateChildRefId(childPos + 2, right.mId);
            }
        } catch (IOException e) {
            right.releaseExclusive();
            parent.releaseExclusive();
            return 0;
        }

        if (parentKeyGrowth <= 0) {
            encodeNormalKey(newKey, parentPage, parentKeyLoc);
            parent.garbage(parent.garbage() - parentKeyGrowth);
        } else {
            parent.updateInternalKey(childPos, parentKeyGrowth, newKey, newKeyLen);
        }

        int garbageAccum = 0;
        int searchVecLoc = searchVecEnd();
        final int moved = searchVecLoc - firstSearchVecLoc + 2;

        for (; searchVecLoc >= firstSearchVecLoc; searchVecLoc -= 2) {
            int entryLoc = DirectPageOps.p_ushortGetLE(leftPage, searchVecLoc);
            int encodedLen = leafEntryLengthAtLoc(leftPage, entryLoc);
            int rightEntryLoc = right.createLeafEntry(null, tree, 0, encodedLen);
            DirectPageOps.p_copy(leftPage, entryLoc, right.mPage, rightEntryLoc, encodedLen);
            garbageAccum += encodedLen;
        }

        garbage(garbage() + garbageAccum);
        searchVecEnd(firstSearchVecLoc - 2);

        for (CursorFrame frame = right.mLastCursorFrame; frame != null; ) {
            int framePos = frame.getNodePos();
            int mask = framePos >> 31;
            frame.setNodePos(((framePos ^ mask) + moved) ^ mask);
            frame = frame.getPrevCousin();
        }

        final int leftEndPos = firstSearchVecLoc - searchVecStart();
        for (CursorFrame frame = mLastCursorFrame; frame != null; ) {
            CursorFrame prev = frame.getPrevCousin();
            int framePos = frame.getNodePos();
            int mask = framePos >> 31;
            int newPos = (framePos ^ mask) - leftEndPos;
            byte[] frameKey;
            if (newPos >= 0 &
                    ((newPos != 0 | mask == 0) ||
                            ((frameKey = frame.getNotFoundKey()) != null &&
                                    Utils.compareUnsigned(frameKey, newKey) >= 0))) {
                frame.rebind(right, newPos ^ mask);
                frame.adjustParentPosition(+2);
            }
            frame = prev;
        }

        right.releaseExclusive();
        parent.releaseExclusive();

        garbage(garbage() - insertLen);
        pos += searchVecStart();
        int newSearchVecEnd = searchVecEnd() + 2;
        DirectPageOps.p_copy(leftPage, pos, leftPage, pos + 2, newSearchVecEnd - pos);
        searchVecEnd(newSearchVecEnd);
        DirectPageOps.p_shortPutLE(leftPage, pos, insertLoc);
        return insertLoc;
    }

    void insertSplitChildRef(final CursorFrame frame, Tree tree, int keyPos, Node splitChild)
            throws IOException {
        final Split split = splitChild.mSplit;
        final Node newChild = splitChild.rebindSplitFrames(split);
        try {
            splitChild.mSplit = null;

            //final Node leftChild;
            final Node rightChild;
            int newChildPos = keyPos >> 1;
            if (split.mSplitRight) {
                //leftChild = splitChild;
                rightChild = newChild;
                newChildPos++;
            } else {
                //leftChild = newChild;
                rightChild = splitChild;
            }

            for (CursorFrame f = mLastCursorFrame; f != null; ) {
                int fPos = f.getNodePos();
                if (fPos > keyPos) {
                    f.setNodePos(fPos + 2);
                }
                f = f.getPrevCousin();
            }

            for (CursorFrame childFrame = rightChild.mLastCursorFrame; childFrame != null; ) {
                childFrame.adjustParentPosition(+2);
                childFrame = childFrame.getPrevCousin();
            }

            InResult result = new InResult();
            try {
                createInternalEntry(frame, result, tree, keyPos, split.splitKeyEncodedLength(),
                        newChildPos << 3, true);
            } catch (Throwable e) {
                panic(e);
                throw e;
            }

            DirectPageOps.p_longPutLE(result.mPage, result.mNewChildLoc, newChild.mId);

            int entryLoc = result.mEntryLoc;
            if (entryLoc < 0) {
                mSplit.setKey(split);
            } else {
                split.copySplitKeyToParent(result.mPage, entryLoc);
            }
        } catch (Throwable e) {
            splitChild.releaseExclusive();
            newChild.releaseExclusive();
            releaseExclusive();
            throw e;
        }

        splitChild.releaseExclusive();
        newChild.releaseExclusive();

        try {
            newChild.makeEvictable();
        } catch (Throwable e) {
            releaseExclusive();
            throw e;
        }
    }

    private void createInternalEntry(final CursorFrame frame, InResult result,
                                     Tree tree, int keyPos, int encodedLen,
                                     int newChildPos, boolean allowSplit)
            throws IOException {
        int searchVecStart = searchVecStart();
        int searchVecEnd = searchVecEnd();

        int leftSpace = searchVecStart - leftSegTail();
        int rightSpace = rightSegTail() - searchVecEnd
                - ((searchVecEnd - searchVecStart) << 2) - 17;

        long page = mPage;

        int entryLoc;
        alloc:
        {
            if (newChildPos < ((3 * (searchVecEnd - searchVecStart + 2) + keyPos + 8) >> 1)) {

                if ((leftSpace -= 10) >= 0 &&
                        (entryLoc = allocPageEntry(encodedLen, leftSpace, rightSpace)) >= 0) {
                    DirectPageOps.p_copy(page, searchVecStart, page, searchVecStart - 10, keyPos);
                    DirectPageOps.p_copy(page, searchVecStart + keyPos,
                            page, searchVecStart + keyPos - 8,
                            searchVecEnd - searchVecStart + 2 - keyPos + newChildPos);
                    searchVecStart(searchVecStart -= 10);
                    keyPos += searchVecStart;
                    searchVecEnd(searchVecEnd -= 8);
                    newChildPos += searchVecEnd + 2;
                    break alloc;
                }

                leftSpace += 10;
            } else {
                leftSpace -= 2;
                rightSpace -= 8;

                if (leftSpace >= 0 && rightSpace >= 0 &&
                        (entryLoc = allocPageEntry(encodedLen, leftSpace, rightSpace)) >= 0) {
                    DirectPageOps.p_copy(page, searchVecStart, page, searchVecStart -= 2, keyPos);
                    searchVecStart(searchVecStart);
                    keyPos += searchVecStart;
                    DirectPageOps.p_copy(page, searchVecEnd + newChildPos + 2,
                            page, searchVecEnd + newChildPos + (2 + 8),
                            ((searchVecEnd - searchVecStart) << 2) + 8 - newChildPos);
                    newChildPos += searchVecEnd + 2;
                    break alloc;
                }

                leftSpace += 2;
                rightSpace += 8;
            }

            int remaining = leftSpace + rightSpace - encodedLen - 10;

            if (garbage() > remaining) {
                compact:
                {
                    if ((garbage() + remaining) < 0) {
                        CursorFrame parentFrame;
                        if (frame == null || (parentFrame = frame.getParentFrame()) == null) {
                            break compact;
                        }

                        if ((mId & 1) == 0) {
                            int adjust = tryRebalanceInternalLeft
                                    (tree, parentFrame, keyPos, -remaining);
                            if (adjust == 0) {
                                if (!tryRebalanceInternalRight
                                        (tree, parentFrame, keyPos, -remaining)) {
                                    break compact;
                                }
                            } else {
                                keyPos -= adjust;
                                newChildPos -= (adjust << 2);
                            }
                        } else if (!tryRebalanceInternalRight
                                (tree, parentFrame, keyPos, -remaining)) {
                            int adjust = tryRebalanceInternalLeft
                                    (tree, parentFrame, keyPos, -remaining);
                            if (adjust == 0) {
                                break compact;
                            } else {
                                keyPos -= adjust;
                                newChildPos -= (adjust << 2);
                            }
                        }
                    }

                    compactInternal(result, encodedLen, keyPos, newChildPos);
                    return;
                }


                if (!allowSplit) {
                    throw new AssertionError("Split not allowed");
                }

                splitInternal(result, tree, encodedLen, keyPos, newChildPos);
                return;
            }

            int vecLen = searchVecEnd - searchVecStart + 2;
            int childIdsLen = (vecLen << 2) + 8;
            int newSearchVecStart;

            if (remaining > 0 || (rightSegTail() & 1) != 0) {
                newSearchVecStart =
                        (rightSegTail() - vecLen - childIdsLen + (1 - 10) - (remaining >> 1)) & ~1;

                entryLoc = leftSegTail();
                leftSegTail(entryLoc + encodedLen);
            } else if ((leftSegTail() & 1) == 0) {
                newSearchVecStart = leftSegTail() + ((remaining >> 1) & ~1);

                entryLoc = rightSegTail() - encodedLen + 1;
                rightSegTail(entryLoc - 1);
            } else {
                compactInternal(result, encodedLen, keyPos, newChildPos);
                return;
            }

            int newSearchVecEnd = newSearchVecStart + vecLen;

            DirectPageOps.p_copies(page,
                    searchVecStart, newSearchVecStart, keyPos,

                    searchVecStart + keyPos,
                    newSearchVecStart + keyPos + 2,
                    vecLen - keyPos + newChildPos,

                    searchVecEnd + 2 + newChildPos,
                    newSearchVecEnd + 10 + newChildPos,
                    childIdsLen - newChildPos);

            keyPos += newSearchVecStart;
            newChildPos += newSearchVecEnd + 2;
            searchVecStart(newSearchVecStart);
            searchVecEnd(newSearchVecEnd);
        }

        DirectPageOps.p_shortPutLE(page, keyPos, entryLoc);

        result.mPage = page;
        result.mNewChildLoc = newChildPos;
        result.mEntryLoc = entryLoc;
    }

    private int tryRebalanceInternalLeft(Tree tree, CursorFrame parentFrame,
                                         int keyPos, int minAmount) {
        final Node parent = parentFrame.tryAcquireExclusive();
        if (parent == null) {
            return 0;
        }

        final int childPos = parentFrame.getNodePos();
        if (childPos <= 0
                || parent.mSplit != null
                || parent.mCachedState != mCachedState) {
            parent.releaseExclusive();
            return 0;
        }

        final long parentPage = parent.mPage;
        final long rightPage = mPage;

        int rightShrink = 0;
        int leftGrowth = 0;

        final int lastSearchVecLoc;

        check:
        {
            int searchVecLoc = searchVecStart();
            int searchVecEnd = searchVecLoc + keyPos - 2;

            for (; searchVecLoc < searchVecEnd; searchVecLoc += 2) {
                int keyLoc = DirectPageOps.p_ushortGetLE(rightPage, searchVecLoc);
                int len = keyLengthAtLoc(rightPage, keyLoc) + (2 + 8);

                rightShrink += len;
                leftGrowth += len;

                if (rightShrink >= minAmount) {
                    lastSearchVecLoc = searchVecLoc;

                    leftGrowth -= len;
                    keyLoc = DirectPageOps.p_ushortGetLE(parentPage, parent.searchVecStart() + childPos - 2);
                    leftGrowth += keyLengthAtLoc(parentPage, keyLoc) + (2 + 8);

                    break check;
                }
            }

            parent.releaseExclusive();
            return 0;
        }

        final Node left;
        try {
            left = parent.tryLatchChildNotSplit(childPos - 2);
        } catch (IOException e) {
            return 0;
        }

        if (left == null) {
            parent.releaseExclusive();
            return 0;
        }

        final int searchKeyLoc;
        final int searchKeyLen;
        final int parentKeyLoc;
        final int parentKeyLen;
        final int parentKeyGrowth;

        check:
        {
            int leftAvail = left.availableInternalBytes();
            if (leftAvail >= leftGrowth) {
                searchKeyLoc = DirectPageOps.p_ushortGetLE(rightPage, lastSearchVecLoc);
                searchKeyLen = keyLengthAtLoc(rightPage, searchKeyLoc);
                parentKeyLoc = DirectPageOps.p_ushortGetLE(parentPage, parent.searchVecStart() + childPos - 2);
                parentKeyLen = keyLengthAtLoc(parentPage, parentKeyLoc);
                parentKeyGrowth = searchKeyLen - parentKeyLen;
                if (parentKeyGrowth <= 0 || parentKeyGrowth <= parent.availableInternalBytes()) {
                    break check;
                }
            }
            left.releaseExclusive();
            parent.releaseExclusive();
            return 0;
        }

        try {
            if (tree.mDatabase.markDirty(tree, left)) {
                parent.updateChildRefId(childPos - 2, left.mId);
            }
        } catch (IOException e) {
            left.releaseExclusive();
            parent.releaseExclusive();
            return 0;
        }

        int garbageAccum = searchKeyLen;
        int searchVecLoc = searchVecStart();
        final int moved = lastSearchVecLoc - searchVecLoc + 2;

        try {
            int pos = left.highestInternalPos();
            InResult result = new InResult();
            left.createInternalEntry(null, result, tree, pos, parentKeyLen, (pos + 2) << 2, false);
            DirectPageOps.p_copy(parentPage, parentKeyLoc, left.mPage, result.mEntryLoc, parentKeyLen);

            for (; searchVecLoc < lastSearchVecLoc; searchVecLoc += 2) {
                int keyLoc = DirectPageOps.p_ushortGetLE(rightPage, searchVecLoc);
                int encodedLen = keyLengthAtLoc(rightPage, keyLoc);
                pos = left.highestInternalPos();
                left.createInternalEntry
                        (null, result, tree, pos, encodedLen, (pos + 2) << 2, false);
                DirectPageOps.p_copy(rightPage, keyLoc, left.mPage, result.mEntryLoc, encodedLen);
                garbageAccum += encodedLen;
            }
        } catch (IOException e) {
            throw Utils.rethrow(e);
        }

        if (parentKeyGrowth <= 0) {
            DirectPageOps.p_copy(rightPage, searchKeyLoc, parentPage, parentKeyLoc, searchKeyLen);
            parent.garbage(parent.garbage() - parentKeyGrowth);
        } else {
            parent.updateInternalKeyEncoded
                    (childPos - 2, parentKeyGrowth, rightPage, searchKeyLoc, searchKeyLen);
        }

        {
            int start = searchVecEnd() + 2;
            int len = moved << 2;
            int end = left.searchVecEnd();
            end = end + ((end - left.searchVecStart()) << 2) + (2 + 16) - len;
            DirectPageOps.p_copy(rightPage, start, left.mPage, end, len);
            DirectPageOps.p_copy(rightPage, start + len, rightPage, start, (start - lastSearchVecLoc) << 2);
        }

        garbage(garbage() + garbageAccum);
        searchVecStart(lastSearchVecLoc + 2);

        final int leftEndPos = left.highestInternalPos() + 2;
        for (CursorFrame frame = mLastCursorFrame; frame != null; ) {
            CursorFrame prev = frame.getPrevCousin();
            int framePos = frame.getNodePos();
            int newPos = framePos - moved;
            if (newPos < 0) {
                frame.rebind(left, leftEndPos + newPos);
                frame.adjustParentPosition(-2);
            } else {
                frame.setNodePos(newPos);
            }
            frame = prev;
        }

        left.releaseExclusive();
        parent.releaseExclusive();

        return moved;
    }

    private boolean tryRebalanceInternalRight(Tree tree, CursorFrame parentFrame,
                                              int keyPos, int minAmount) {
        final Node parent = parentFrame.tryAcquireExclusive();
        if (parent == null) {
            return false;
        }

        final int childPos = parentFrame.getNodePos();
        if (childPos >= parent.highestInternalPos()
                || parent.mSplit != null
                || parent.mCachedState != mCachedState) {
            parent.releaseExclusive();
            return false;
        }

        final long parentPage = parent.mPage;
        final long leftPage = mPage;

        int leftShrink = 0;
        int rightGrowth = 0;

        final int firstSearchVecLoc;

        check:
        {
            int searchVecStart = searchVecStart() + keyPos;
            int searchVecLoc = searchVecEnd();

            for (; searchVecLoc > searchVecStart; searchVecLoc -= 2) {
                int keyLoc = DirectPageOps.p_ushortGetLE(leftPage, searchVecLoc);
                int len = keyLengthAtLoc(leftPage, keyLoc) + (2 + 8);

                leftShrink += len;
                rightGrowth += len;

                if (leftShrink >= minAmount) {
                    firstSearchVecLoc = searchVecLoc;

                    rightGrowth -= len;
                    keyLoc = DirectPageOps.p_ushortGetLE(parentPage, parent.searchVecStart() + childPos);
                    rightGrowth += keyLengthAtLoc(parentPage, keyLoc) + (2 + 8);

                    break check;
                }
            }

            parent.releaseExclusive();
            return false;
        }

        final Node right;
        try {
            right = parent.tryLatchChildNotSplit(childPos + 2);
        } catch (IOException e) {
            return false;
        }

        if (right == null) {
            parent.releaseExclusive();
            return false;
        }

        final int searchKeyLoc;
        final int searchKeyLen;
        final int parentKeyLoc;
        final int parentKeyLen;
        final int parentKeyGrowth;

        check:
        {
            int rightAvail = right.availableInternalBytes();
            if (rightAvail >= rightGrowth) {
                searchKeyLoc = DirectPageOps.p_ushortGetLE(leftPage, firstSearchVecLoc);
                searchKeyLen = keyLengthAtLoc(leftPage, searchKeyLoc);
                parentKeyLoc = DirectPageOps.p_ushortGetLE(parentPage, parent.searchVecStart() + childPos);
                parentKeyLen = keyLengthAtLoc(parentPage, parentKeyLoc);
                parentKeyGrowth = searchKeyLen - parentKeyLen;
                if (parentKeyGrowth <= 0 || parentKeyGrowth <= parent.availableInternalBytes()) {
                    break check;
                }
            }
            right.releaseExclusive();
            parent.releaseExclusive();
            return false;
        }

        try {
            if (tree.mDatabase.markDirty(tree, right)) {
                parent.updateChildRefId(childPos + 2, right.mId);
            }
        } catch (IOException e) {
            right.releaseExclusive();
            parent.releaseExclusive();
            return false;
        }

        int garbageAccum = searchKeyLen;
        int searchVecLoc = searchVecEnd();
        final int moved = searchVecLoc - firstSearchVecLoc + 2;

        try {
            InResult result = new InResult();
            right.createInternalEntry(null, result, tree, 0, parentKeyLen, 0, false);
            DirectPageOps.p_copy(parentPage, parentKeyLoc, right.mPage, result.mEntryLoc, parentKeyLen);

            for (; searchVecLoc > firstSearchVecLoc; searchVecLoc -= 2) {
                int keyLoc = DirectPageOps.p_ushortGetLE(leftPage, searchVecLoc);
                int encodedLen = keyLengthAtLoc(leftPage, keyLoc);
                right.createInternalEntry(null, result, tree, 0, encodedLen, 0, false);
                DirectPageOps.p_copy(leftPage, keyLoc, right.mPage, result.mEntryLoc, encodedLen);
                garbageAccum += encodedLen;
            }
        } catch (IOException e) {
            throw Utils.rethrow(e);
        }

        if (parentKeyGrowth <= 0) {
            DirectPageOps.p_copy(leftPage, searchKeyLoc, parentPage, parentKeyLoc, searchKeyLen);
            parent.garbage(parent.garbage() - parentKeyGrowth);
        } else {
            parent.updateInternalKeyEncoded
                    (childPos, parentKeyGrowth, leftPage, searchKeyLoc, searchKeyLen);
        }

        {
            int start = searchVecEnd() + 2;
            int len = ((start - searchVecStart()) << 2) + 8 - (moved << 2);
            DirectPageOps.p_copy(leftPage, start, leftPage, start - moved, len);
            DirectPageOps.p_copy(leftPage, start + len, right.mPage, right.searchVecEnd() + 2, moved << 2);
        }

        garbage(garbage() + garbageAccum);
        searchVecEnd(firstSearchVecLoc - 2);

        for (CursorFrame frame = right.mLastCursorFrame; frame != null; ) {
            frame.setNodePos(frame.getNodePos() + moved);
            frame = frame.getPrevCousin();
        }

        final int adjust = firstSearchVecLoc - searchVecStart() + 4;
        for (CursorFrame frame = mLastCursorFrame; frame != null; ) {
            CursorFrame prev = frame.getPrevCousin();
            int newPos = frame.getNodePos() - adjust;
            if (newPos >= 0) {
                frame.rebind(right, newPos);
                frame.adjustParentPosition(+2);
            }
            frame = prev;
        }

        right.releaseExclusive();
        parent.releaseExclusive();

        return true;
    }

    private Node rebindSplitFrames(Split split) {
        final Node sibling = split.latchSiblingEx();
        try {
            for (CursorFrame frame = mLastCursorFrame; frame != null; ) {
                CursorFrame prev = frame.getPrevCousin();
                split.rebindFrame(frame, sibling);
                frame = prev;
            }
            return sibling;
        } catch (Throwable e) {
            sibling.releaseExclusive();
            throw e;
        }
    }

    void updateLeafValue(CursorFrame frame, Tree tree, int pos, int vfrag, byte[] value)
            throws IOException {
        long page = mPage;
        final int searchVecStart = searchVecStart();

        final int start;
        final int keyLen;
        final int garbage;
        quick:
        {
            int loc;
            start = loc = DirectPageOps.p_ushortGetLE(page, searchVecStart + pos);
            loc += keyLengthAtLoc(page, loc);

            final int valueHeaderLoc = loc;

            int len = DirectPageOps.p_byteGet(page, loc++);
            if (len < 0) largeValue:{
                int header;
                if ((len & 0x20) == 0) {
                    header = len;
                    len = 1 + (((len & 0x1f) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
                } else if (len != -1) {
                    header = len;
                    len = 1 + (((len & 0x0f) << 16)
                            | (DirectPageOps.p_ubyteGet(page, loc++) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
                } else {
                    // ghost
                    len = 0;
                    break largeValue;
                }
                if ((header & ENTRY_FRAGMENTED) != 0) {
                    tree.mDatabase.deleteFragments(page, loc, len);
                    DirectPageOps.p_bytePut(page, valueHeaderLoc, header & ~ENTRY_FRAGMENTED);
                }
            }

            final int valueLen = value.length;
            if (valueLen > len) {
                keyLen = valueHeaderLoc - start;
                garbage = garbage() + loc + len - start;
                break quick;
            }

            if (valueLen == len) {
                if (valueLen == 0) {
                    DirectPageOps.p_bytePut(page, valueHeaderLoc, 0);
                } else {
                    DirectPageOps.p_copyFromArray(value, 0, page, loc, valueLen);
                    if (vfrag != 0) {
                        DirectPageOps.p_bytePut(page, valueHeaderLoc, DirectPageOps.p_byteGet(page, valueHeaderLoc) | vfrag);
                    }
                }
            } else {
                garbage(garbage() + loc + len - copyToLeafValue
                        (page, vfrag, value, valueHeaderLoc) - valueLen);
            }

            return;
        }

        int searchVecEnd = searchVecEnd();

        int leftSpace = searchVecStart - leftSegTail();
        int rightSpace = rightSegTail() - searchVecEnd - 1;

        final int vfragOriginal = vfrag;

        int encodedLen;
        if (vfrag != 0) {
            encodedLen = keyLen + calculateFragmentedValueLength(value);
        } else {
            LocalDatabase db = tree.mDatabase;
            encodedLen = keyLen + calculateLeafValueLength(value);
            if (encodedLen > db.mMaxEntrySize) {
                value = db.fragment(value, value.length, db.mMaxFragmentedEntrySize - keyLen);
                if (value == null) {
                    throw new AssertionError();
                }
                encodedLen = keyLen + calculateFragmentedValueLength(value);
                vfrag = ENTRY_FRAGMENTED;
            }
        }

        int entryLoc;
        alloc:
        try {
            if ((entryLoc = allocPageEntry(encodedLen, leftSpace, rightSpace)) >= 0) {
                pos += searchVecStart;
                break alloc;
            }

            int remaining = leftSpace + rightSpace - encodedLen;

            if (garbage > remaining) {

                byte[][] akeyRef = new byte[1][];
                boolean isOriginal = retrieveActualKeyAtLoc(page, start, akeyRef);
                byte[] akey = akeyRef[0];

                if ((garbage + remaining) < 0) {
                    if (mSplit == null) {
                        // TODO: 使用框架重新平衡
                        byte[] okey = isOriginal ? akey : retrieveKeyAtLoc(this, page, start);
                        splitLeafAndCreateEntry
                                (tree, okey, akey, vfrag, value, encodedLen, pos, false);
                        return;
                    }

                    if (vfrag != 0) {
                        // TODO: 这会发生吗？
                        throw new DatabaseException("Fragmented entry doesn't fit");
                    }
                    LocalDatabase db = tree.mDatabase;
                    int max = Math.min(db.mMaxFragmentedEntrySize,
                            garbage + leftSpace + rightSpace);
                    value = db.fragment(value, value.length, max - keyLen);
                    if (value == null) {
                        throw new AssertionError();
                    }
                    encodedLen = keyLen + calculateFragmentedValueLength(value);
                    vfrag = ENTRY_FRAGMENTED;
                }

                garbage(garbage);
                entryLoc = compactLeaf(encodedLen, pos, false);
                page = mPage;
                entryLoc = isOriginal ? encodeNormalKey(akey, page, entryLoc)
                        : encodeFragmentedKey(akey, page, entryLoc);
                copyToLeafValue(page, vfrag, value, entryLoc);
                return;
            }

            int vecLen = searchVecEnd - searchVecStart + 2;
            int newSearchVecStart;

            if (remaining > 0 || (rightSegTail() & 1) != 0) {
                newSearchVecStart = (rightSegTail() - vecLen + (1 - 0) - (remaining >> 1)) & ~1;

                entryLoc = leftSegTail();
                leftSegTail(entryLoc + encodedLen);
            } else if ((leftSegTail() & 1) == 0) {
                newSearchVecStart = leftSegTail() + ((remaining >> 1) & ~1);

                entryLoc = rightSegTail() - encodedLen + 1;
                rightSegTail(entryLoc - 1);
            } else {
                byte[][] akeyRef = new byte[1][];
                int loc = DirectPageOps.p_ushortGetLE(page, searchVecStart + pos);
                boolean isOriginal = retrieveActualKeyAtLoc(page, loc, akeyRef);
                byte[] akey = akeyRef[0];

                garbage(garbage);
                entryLoc = compactLeaf(encodedLen, pos, false);
                page = mPage;
                entryLoc = isOriginal ? encodeNormalKey(akey, page, entryLoc)
                        : encodeFragmentedKey(akey, page, entryLoc);
                copyToLeafValue(page, vfrag, value, entryLoc);
                return;
            }

            DirectPageOps.p_copy(page, searchVecStart, page, newSearchVecStart, vecLen);

            pos += newSearchVecStart;
            searchVecStart(newSearchVecStart);
            searchVecEnd(newSearchVecStart + vecLen - 2);
        } catch (Throwable e) {
            if (vfrag == ENTRY_FRAGMENTED && vfragOriginal != ENTRY_FRAGMENTED) {
                cleanupFragments(e, value);
            }
            throw e;
        }

        DirectPageOps.p_copy(page, start, page, entryLoc, keyLen);
        copyToLeafValue(page, vfrag, value, entryLoc + keyLen);
        DirectPageOps.p_shortPutLE(page, pos, entryLoc);

        garbage(garbage);
    }

    void updateInternalKey(int pos, int growth, byte[] key, int encodedLen) {
        int entryLoc = doUpdateInternalKey(pos, growth, encodedLen);
        encodeNormalKey(key, mPage, entryLoc);
    }

    void updateInternalKeyEncoded(int pos, int growth,
                                  long key, int keyStart, int encodedLen) {
        int entryLoc = doUpdateInternalKey(pos, growth, encodedLen);
        DirectPageOps.p_copy(key, keyStart, mPage, entryLoc, encodedLen);
    }

    int doUpdateInternalKey(int pos, final int growth, final int encodedLen) {
        int garbage = garbage() + encodedLen - growth;

        int searchVecStart = searchVecStart();
        int searchVecEnd = searchVecEnd();

        int leftSpace = searchVecStart - leftSegTail();
        int rightSpace = rightSegTail() - searchVecEnd
                - ((searchVecEnd - searchVecStart) << 2) - 17;

        int entryLoc;
        alloc:
        {
            if ((entryLoc = allocPageEntry(encodedLen, leftSpace, rightSpace)) >= 0) {
                pos += searchVecStart;
                break alloc;
            }

            makeRoom:
            {
                int remaining = leftSpace + rightSpace - encodedLen;

                if (garbage > remaining) {
                    if ((garbage + remaining) < 0) {
                        throw new AssertionError();
                    }
                    break makeRoom;
                }

                int vecLen = searchVecEnd - searchVecStart + 2;
                int childIdsLen = (vecLen << 2) + 8;
                int newSearchVecStart;

                if (remaining > 0 || (rightSegTail() & 1) != 0) {
                    newSearchVecStart =
                            (rightSegTail() - vecLen - childIdsLen + (1 - 0) - (remaining >> 1)) & ~1;

                    entryLoc = leftSegTail();
                    leftSegTail(entryLoc + encodedLen);
                } else if ((leftSegTail() & 1) == 0) {
                    newSearchVecStart = leftSegTail() + ((remaining >> 1) & ~1);

                    entryLoc = rightSegTail() - encodedLen + 1;
                    rightSegTail(entryLoc - 1);
                } else {
                    break makeRoom;
                }

                long page = mPage;
                DirectPageOps.p_copy(page, searchVecStart, page, newSearchVecStart, vecLen + childIdsLen);

                pos += newSearchVecStart;
                searchVecStart(newSearchVecStart);
                searchVecEnd(newSearchVecStart + vecLen - 2);

                break alloc;
            }

            garbage(garbage);

            InResult result = new InResult();
            compactInternal(result, encodedLen, pos, Integer.MIN_VALUE);

            return result.mEntryLoc;
        }

        DirectPageOps.p_shortPutLE(mPage, pos, entryLoc);

        garbage(garbage);

        return entryLoc;
    }

    void updateChildRefId(int pos, long id) {
        DirectPageOps.p_longPutLE(mPage, searchVecEnd() + 2 + (pos << 2), id);
    }

    public void deleteLeafEntry(int pos) throws IOException {
        long page = mPage;
        int entryLoc = DirectPageOps.p_ushortGetLE(page, searchVecStart() + pos);
        finishDeleteLeafEntry(pos, doDeleteLeafEntry(page, entryLoc) - entryLoc);
    }

    private int doDeleteLeafEntry(long page, int loc) throws IOException {
        int keyLen = DirectPageOps.p_byteGet(page, loc++);
        if (keyLen >= 0) {
            loc += keyLen + 1;
        } else {
            int header = keyLen;
            keyLen = ((keyLen & 0x3f) << 8) | DirectPageOps.p_ubyteGet(page, loc++);
            if ((header & ENTRY_FRAGMENTED) != 0) {
                getDatabase().deleteFragments(page, loc, keyLen);
            }
            loc += keyLen;
        }

        int header = DirectPageOps.p_byteGet(page, loc++);
        if (header >= 0) {
            loc += header;
        } else largeValue:{
            int len;
            if ((header & 0x20) == 0) {
                len = 1 + (((header & 0x1f) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
            } else if (header != -1) {
                len = 1 + (((header & 0x0f) << 16)
                        | (DirectPageOps.p_ubyteGet(page, loc++) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
            } else {
                // ghost
                break largeValue;
            }
            if ((header & ENTRY_FRAGMENTED) != 0) {
                getDatabase().deleteFragments(page, loc, len);
            }
            loc += len;
        }

        return loc;
    }

    void finishDeleteLeafEntry(int pos, int entryLen) {
        garbage(garbage() + entryLen);

        long page = mPage;
        int searchVecStart = searchVecStart();
        int searchVecEnd = searchVecEnd();

        if (pos < ((searchVecEnd - searchVecStart) >> 1)) {
            DirectPageOps.p_copy(page, searchVecStart, page, searchVecStart += 2, pos);
            searchVecStart(searchVecStart);
        } else {
            pos += searchVecStart;
            DirectPageOps.p_copy(page, pos + 2, page, pos, searchVecEnd - pos);
            searchVecEnd(searchVecEnd - 2);
        }
    }

    public void postDelete(int pos, byte[] key) {
        int newPos = ~pos;
        CursorFrame frame = mLastCursorFrame;
        do {
            int framePos = frame.getNodePos();
            if (framePos == pos) {
                frame.setNodePos(newPos);
                frame.setNotFoundKey(key);
            } else if (framePos > pos) {
                frame.setNodePos(framePos - 2);
            } else if (framePos < newPos) {
                frame.setNodePos(framePos + 2);
            }
        } while ((frame = frame.getPrevCousin()) != null);
    }

    static void moveLeafToLeftAndDelete(Tree tree, Node leftNode, Node rightNode)
            throws IOException {
        tree.mDatabase.prepareToDelete(rightNode);

        final long rightPage = rightNode.mPage;
        final int searchVecEnd = rightNode.searchVecEnd();
        final int leftEndPos = leftNode.highestLeafPos() + 2;

        int searchVecStart = rightNode.searchVecStart();
        while (searchVecStart <= searchVecEnd) {
            int entryLoc = DirectPageOps.p_ushortGetLE(rightPage, searchVecStart);
            int encodedLen = leafEntryLengthAtLoc(rightPage, entryLoc);
            int leftEntryLoc = leftNode.createLeafEntry
                    (null, tree, leftNode.highestLeafPos() + 2, encodedLen);
            DirectPageOps.p_copy(rightPage, entryLoc, leftNode.mPage, leftEntryLoc, encodedLen);
            searchVecStart += 2;
        }

        for (CursorFrame frame = rightNode.mLastCursorFrame; frame != null; ) {
            CursorFrame prev = frame.getPrevCousin();
            int framePos = frame.getNodePos();
            frame.rebind(leftNode, framePos + (framePos < 0 ? (-leftEndPos) : leftEndPos));
            frame = prev;
        }

        leftNode.type((byte) (leftNode.type() | (rightNode.type() & HIGH_EXTREMITY)));

        tree.mDatabase.finishDeleteNode(rightNode);
    }

    static void moveInternalToLeftAndDelete(Tree tree, Node leftNode, Node rightNode,
                                            long parentPage, int parentLoc, int parentLen)
            throws IOException {
        tree.mDatabase.prepareToDelete(rightNode);

        int leftEndPos = leftNode.highestInternalPos();
        InResult result = new InResult();
        leftNode.createInternalEntry
                (null, result, tree, leftEndPos, parentLen, (leftEndPos += 2) << 2, false);

        final long rightPage = rightNode.mPage;
        int rightChildIdsLoc = rightNode.searchVecEnd() + 2;
        DirectPageOps.p_copy(rightPage, rightChildIdsLoc, result.mPage, result.mNewChildLoc, 8);
        rightChildIdsLoc += 8;

        DirectPageOps.p_copy(parentPage, parentLoc, result.mPage, result.mEntryLoc, parentLen);

        final int searchVecEnd = rightNode.searchVecEnd();

        int searchVecStart = rightNode.searchVecStart();
        while (searchVecStart <= searchVecEnd) {
            int entryLoc = DirectPageOps.p_ushortGetLE(rightPage, searchVecStart);
            int encodedLen = keyLengthAtLoc(rightPage, entryLoc);

            int pos = leftNode.highestInternalPos();
            leftNode.createInternalEntry
                    (null, result, tree, pos, encodedLen, (pos + 2) << 2, false);

            DirectPageOps.p_copy(rightPage, rightChildIdsLoc, result.mPage, result.mNewChildLoc, 8);
            rightChildIdsLoc += 8;

            DirectPageOps.p_copy(rightPage, entryLoc, result.mPage, result.mEntryLoc, encodedLen);
            searchVecStart += 2;
        }

        for (CursorFrame frame = rightNode.mLastCursorFrame; frame != null; ) {
            CursorFrame prev = frame.getPrevCousin();
            int framePos = frame.getNodePos();
            frame.rebind(leftNode, leftEndPos + framePos);
            frame = prev;
        }

        leftNode.type((byte) (leftNode.type() | (rightNode.type() & HIGH_EXTREMITY)));

        tree.mDatabase.finishDeleteNode(rightNode);
    }

    void deleteRightChildRef(int childPos) {
        for (CursorFrame frame = mLastCursorFrame; frame != null; ) {
            int framePos = frame.getNodePos();
            if (framePos >= childPos) {
                frame.setNodePos(framePos - 2);
            }
            frame = frame.getPrevCousin();
        }

        deleteChildRef(childPos);
    }

    void deleteLeftChildRef(int childPos) {
        for (CursorFrame frame = mLastCursorFrame; frame != null; ) {
            int framePos = frame.getNodePos();
            if (framePos > childPos) {
                frame.setNodePos(framePos - 2);
            }
            frame = frame.getPrevCousin();
        }

        deleteChildRef(childPos);
    }

    private void deleteChildRef(int childPos) {
        final long page = mPage;
        int keyPos = childPos == 0 ? 0 : (childPos - 2);
        int searchVecStart = searchVecStart();

        int entryLoc = DirectPageOps.p_ushortGetLE(page, searchVecStart + keyPos);
        garbage(garbage() + keyLengthAtLoc(page, entryLoc));

        childPos <<= 2;

        int searchVecEnd = searchVecEnd();

        if (childPos < (3 * (searchVecEnd - searchVecStart) + keyPos + 8) >> 1) {
            DirectPageOps.p_copy(page, searchVecStart + keyPos + 2,
                    page, searchVecStart + keyPos + (2 + 8),
                    searchVecEnd - searchVecStart - keyPos + childPos);
            DirectPageOps.p_copy(page, searchVecStart, page, searchVecStart += 10, keyPos);
            searchVecEnd(searchVecEnd + 8);
        } else {
            DirectPageOps.p_copy(page, searchVecEnd + childPos + (2 + 8),
                    page, searchVecEnd + childPos + 2,
                    ((searchVecEnd - searchVecStart) << 2) + 8 - childPos);
            DirectPageOps.p_copy(page, searchVecStart, page, searchVecStart += 2, keyPos);
        }

        searchVecStart(searchVecStart);
    }

    void rootDelete(Tree tree, Node child, Node stub) throws IOException {
        try {
            tree.mDatabase.prepareToDelete(child);

            try {
                doRootDelete(tree, child, stub);
            } catch (Throwable e) {
                child.releaseExclusive();
                throw e;
            }

            tree.mDatabase.finishDeleteNode(child);
        } finally {
            stub.releaseExclusive();
            releaseExclusive();
        }
    }

    private void doRootDelete(Tree tree, Node child, Node stub) throws IOException {
        long oldRootPage = mPage;
        // mPage = child.mPage;
        // type(child.type());
        // garbage(child.garbage());
        // leftSegTail(child.leftSegTail());
        // rightSegTail(child.rightSegTail());
        // searchVecStart(child.searchVecStart());
        // searchVecEnd(child.searchVecEnd());
        if (tree.mDatabase.mFullyMapped) {
            DirectPageOps.p_copy(child.mPage, 0, oldRootPage, 0, tree.mDatabase.pageSize());
            oldRootPage = child.mPage;
        } else {
            mPage = child.mPage;
        }

        CursorFrame lock = new CursorFrame();
        CursorFrame childLastFrame = child.lockLastFrame(lock);
        CursorFrame thisLastFrame = this.lockLastFrame(lock);


        // 1. Frames from child move to this node, the root.
        if (!CursorFrame.cLastUpdater.compareAndSet(this, thisLastFrame, childLastFrame)) {
            throw new AssertionError();
        }
        // 2. Frames of child node are cleared.
        if (!CursorFrame.cLastUpdater.compareAndSet(child, childLastFrame, null)) {
            throw new AssertionError();
        }
        // 3. Frames from empty root move to the stub.
        if (!CursorFrame.cLastUpdater.compareAndSet(stub, null, thisLastFrame)) {
            throw new AssertionError();
        }

        this.fixFrameBindings(lock, childLastFrame);
        stub.fixFrameBindings(lock, thisLastFrame);

        // child.mPage = oldRootPage;
        if (tree.mDatabase.mFullyMapped) {
            child.mPage = DirectPageOps.p_nonTreePage();
        } else {
            child.mPage = oldRootPage;
        }
    }

    private CursorFrame lockLastFrame(CursorFrame lock) {
        while (true) {
            CursorFrame last = mLastCursorFrame;
            CursorFrame lockResult = last.tryLock(lock);
            if (lockResult == last) {
                return last;
            }
            if (lockResult != null) {
                last.unlock(lockResult);
            }
        }
    }

    private void fixFrameBindings(final CursorFrame lock, CursorFrame frame) {
        CursorFrame lockResult = frame;
        while (true) {
            Node existing = frame.getNode();
            if (existing != null) {
                if (existing == this) {
                    throw new AssertionError();
                }
                frame.setNode(this);
            }

            CursorFrame prev = frame.tryLockPrevious(lock);
            frame.unlock(lockResult);
            if (prev == null) {
                return;
            }

            lockResult = frame;
            frame = prev;
        }
    }

    private static final int SMALL_KEY_LIMIT = 128;

    static int calculateAllowedKeyLength(LocalDatabase db, byte[] key) {
        int len = key.length;
        if (((len - 1) & ~(SMALL_KEY_LIMIT - 1)) == 0) {
            return len + 1;
        } else {
            return len > db.mMaxKeySize ? -1 : (len + 2);
        }
    }

    static int calculateKeyLength(byte[] key) {
        int len = key.length - 1;
        return len + ((len & ~(SMALL_KEY_LIMIT - 1)) == 0 ? 2 : 3);
    }

    private static int calculateLeafValueLength(byte[] value) {
        int len = value.length;
        return len + ((len <= 127) ? 1 : ((len <= 8192) ? 2 : 3));
    }

    private static long calculateLeafValueLength(long vlength) {
        return vlength + ((vlength <= 127) ? 1 : ((vlength <= 8192) ? 2 : 3));
    }

    private static int calculateFragmentedValueLength(byte[] value) {
        return calculateFragmentedValueLength(value.length);
    }

    static int calculateFragmentedValueLength(int vlength) {
        return vlength + ((vlength <= 8192) ? 2 : 3);
    }

    static int encodeNormalKey(final byte[] key, final long page, int pageLoc) {
        final int keyLen = key.length;

        if (keyLen <= SMALL_KEY_LIMIT && keyLen > 0) {
            DirectPageOps.p_bytePut(page, pageLoc++, keyLen - 1);
        } else {
            DirectPageOps.p_bytePut(page, pageLoc++, 0x80 | (keyLen >> 8));
            DirectPageOps.p_bytePut(page, pageLoc++, keyLen);
        }
        DirectPageOps.p_copyFromArray(key, 0, page, pageLoc, keyLen);

        return pageLoc + keyLen;
    }

    static int encodeFragmentedKey(final byte[] key, final long page, int pageLoc) {
        final int keyLen = key.length;
        DirectPageOps.p_bytePut(page, pageLoc++, (0x80 | ENTRY_FRAGMENTED) | (keyLen >> 8));
        DirectPageOps.p_bytePut(page, pageLoc++, keyLen);
        DirectPageOps.p_copyFromArray(key, 0, page, pageLoc, keyLen);
        return pageLoc + keyLen;
    }

    private int allocPageEntry(int encodedLen, int leftSpace, int rightSpace) {
        final int entryLoc;
        if (encodedLen <= leftSpace && leftSpace >= rightSpace) {
            entryLoc = leftSegTail();
            leftSegTail(entryLoc + encodedLen);
        } else if (encodedLen <= rightSpace) {
            entryLoc = rightSegTail() - encodedLen + 1;
            rightSegTail(entryLoc - 1);
        } else {
            return -1;
        }
        return entryLoc;
    }

    private void copyToLeafEntry(byte[] okey, byte[] akey, int vfrag, byte[] value, int entryLoc) {
        final long page = mPage;
        int vloc = okey == akey ? encodeNormalKey(akey, page, entryLoc)
                : encodeFragmentedKey(akey, page, entryLoc);
        copyToLeafValue(page, vfrag, value, vloc);
    }

    private static int copyToLeafValue(long page, int vfrag, byte[] value, int vloc) {
        final int vlen = value.length;
        vloc = encodeLeafValueHeader(page, vfrag, vlen, vloc);
        DirectPageOps.p_copyFromArray(value, 0, page, vloc, vlen);
        return vloc;
    }

    static int encodeLeafValueHeader(long page, int vfrag, int vlen, int vloc) {
        if (vlen <= 127 && vfrag == 0) {
            DirectPageOps.p_bytePut(page, vloc++, vlen);
        } else {
            vlen--;
            if (vlen <= 8192) {
                DirectPageOps.p_bytePut(page, vloc++, 0x80 | vfrag | (vlen >> 8));
                DirectPageOps.p_bytePut(page, vloc++, vlen);
            } else {
                DirectPageOps.p_bytePut(page, vloc++, 0xa0 | vfrag | (vlen >> 16));
                DirectPageOps.p_bytePut(page, vloc++, vlen >> 8);
                DirectPageOps.p_bytePut(page, vloc++, vlen);
            }
        }
        return vloc;
    }

    private int compactLeaf(int encodedLen, int pos, boolean forInsert) {
        long page = mPage;

        int searchVecLoc = searchVecStart();
        int newSearchVecSize = searchVecEnd() - searchVecLoc + 2;
        if (forInsert) {
            newSearchVecSize += 2;
        }
        pos += searchVecLoc;

        int newSearchVecStart;
        int searchVecCap = garbage() + rightSegTail() + 1 - leftSegTail() - encodedLen;
        newSearchVecStart = pageSize(page) - (((searchVecCap + newSearchVecSize) >> 1) & ~1);

        int destLoc = TN_HEADER_SIZE;
        int newSearchVecLoc = newSearchVecStart;
        int newLoc = 0;
        final int searchVecEnd = searchVecEnd();

        LocalDatabase db = getDatabase();
        long dest = db.removeSparePage();

        DirectPageOps.p_intPutLE(dest, 0, type() & 0xff);

        for (; searchVecLoc <= searchVecEnd; searchVecLoc += 2, newSearchVecLoc += 2) {
            if (searchVecLoc == pos) {
                newLoc = newSearchVecLoc;
                if (forInsert) {
                    newSearchVecLoc += 2;
                } else {
                    continue;
                }
            }
            DirectPageOps.p_shortPutLE(dest, newSearchVecLoc, destLoc);
            int sourceLoc = DirectPageOps.p_ushortGetLE(page, searchVecLoc);
            int len = leafEntryLengthAtLoc(page, sourceLoc);
            DirectPageOps.p_copy(page, sourceLoc, dest, destLoc, len);
            destLoc += len;
        }

        // db.addSparePage(page);
        // mPage = dest;
        // garbage(0);
        if (db.mFullyMapped) {
            DirectPageOps.p_copy(dest, 0, page, 0, pageSize(page));
            db.addSparePage(dest);
            dest = page;
        } else {
            db.addSparePage(page);
            mPage = dest;
        }

        DirectPageOps.p_shortPutLE(dest, newLoc == 0 ? newSearchVecLoc : newLoc, destLoc);

        leftSegTail(destLoc + encodedLen);
        rightSegTail(pageSize(dest) - 1);
        searchVecStart(newSearchVecStart);
        searchVecEnd(newSearchVecStart + newSearchVecSize - 2);

        return destLoc;
    }

    private void cleanupSplit(Throwable cause, Node newNode, Split split) {
        if (split != null) {
            cleanupFragments(cause, split.fragmentedKey());
        }

        try {
            getDatabase().finishDeleteNode(newNode);
        } catch (Throwable e) {
            Utils.suppress(cause, e);
            panic(cause);
        }
    }

    void splitLeafAscendingAndCopyEntry(Tree tree, Node snode, int spos, int encodedLen, int pos)
            throws IOException {

        if (mSplit != null) {
            throw new AssertionError("Node is already split");
        }

        long page = mPage;

        if (page == DirectPageOps.p_closedTreePage()) {
            throw new ClosedIndexException();
        }

        Node newNode = tree.mDatabase.allocDirtyNode(NodeContext.MODE_UNEVICTABLE);
        tree.mDatabase.nodeMapPut(newNode);

        long newPage = newNode.mPage;

        // newNode.garbage(0);
        DirectPageOps.p_intPutLE(newPage, 0, 0);

        Split split = null;
        try {
            split = newSplitRight(newNode);
            split.setKey(tree, midKey(highestLeafPos(), snode, spos));
        } catch (Throwable e) {
            cleanupSplit(e, newNode, split);
            throw e;
        }

        mSplit = split;

        newNode.rightSegTail(pageSize(newPage) - 1);
        int newSearchVecStart = pageSize(newPage) - 2;
        newNode.searchVecStart(newSearchVecStart);
        newNode.searchVecEnd(newSearchVecStart);

        final long spage = snode.mPage;
        final int sloc = DirectPageOps.p_ushortGetLE(spage, snode.searchVecStart() + spos);
        DirectPageOps.p_copy(spage, sloc, newPage, TN_HEADER_SIZE, encodedLen);
        DirectPageOps.p_shortPutLE(newPage, pageSize(newPage) - 2, TN_HEADER_SIZE);

        newNode.leftSegTail(TN_HEADER_SIZE + encodedLen);
        newNode.releaseExclusive();
    }

    private void splitLeafAndCreateEntry(Tree tree, byte[] okey, byte[] akey,
                                         int vfrag, byte[] value,
                                         int encodedLen, int pos, boolean forInsert)
            throws IOException {
        if (mSplit != null) {
            throw new AssertionError("Node is already split");
        }

        long page = mPage;

        if (page == DirectPageOps.p_closedTreePage()) {
            // Node is a closed tree root.
            throw new ClosedIndexException();
        }

        Node newNode = tree.mDatabase.allocDirtyNode(NodeContext.MODE_UNEVICTABLE);
        tree.mDatabase.nodeMapPut(newNode);

        long newPage = newNode.mPage;

        // newNode.garbage(0);
        DirectPageOps.p_intPutLE(newPage, 0, 0);

        if (forInsert && pos == 0) {
            Split split = null;
            try {
                split = newSplitLeft(newNode);
                split.setKey(tree, midKey(okey, 0));
            } catch (Throwable e) {
                cleanupSplit(e, newNode, split);
                throw e;
            }

            mSplit = split;

            newNode.leftSegTail(TN_HEADER_SIZE);
            newNode.searchVecStart(TN_HEADER_SIZE);
            newNode.searchVecEnd(TN_HEADER_SIZE);

            int destLoc = pageSize(newPage) - encodedLen;
            newNode.copyToLeafEntry(okey, akey, vfrag, value, destLoc);
            DirectPageOps.p_shortPutLE(newPage, TN_HEADER_SIZE, destLoc);

            newNode.rightSegTail(destLoc - 1);
            newNode.releaseExclusive();

            return;
        }

        final int searchVecStart = searchVecStart();
        final int searchVecEnd = searchVecEnd();

        pos += searchVecStart;

        if (forInsert && pos == searchVecEnd + 2) {

            Split split = null;
            try {
                split = newSplitRight(newNode);
                split.setKey(tree, midKey(pos - searchVecStart - 2, okey));
            } catch (Throwable e) {
                cleanupSplit(e, newNode, split);
                throw e;
            }

            mSplit = split;

            newNode.rightSegTail(pageSize(newPage) - 1);
            int newSearchVecStart = pageSize(newPage) - 2;
            newNode.searchVecStart(newSearchVecStart);
            newNode.searchVecEnd(newSearchVecStart);

            newNode.copyToLeafEntry(okey, akey, vfrag, value, TN_HEADER_SIZE);
            DirectPageOps.p_shortPutLE(newPage, pageSize(newPage) - 2, TN_HEADER_SIZE);

            newNode.leftSegTail(TN_HEADER_SIZE + encodedLen);
            newNode.releaseExclusive();

            return;
        }

        int avail = availableLeafBytes();

        int garbageAccum = 0;
        int newLoc = 0;
        int newAvail = pageSize(newPage) - TN_HEADER_SIZE;

        if ((pos - searchVecStart) < (searchVecEnd - pos)) {
            int destLoc = pageSize(newPage);
            int newSearchVecLoc = TN_HEADER_SIZE;

            byte[] fv = null;

            int searchVecLoc = searchVecStart;
            for (; newAvail > avail; searchVecLoc += 2, newSearchVecLoc += 2) {
                int entryLoc = DirectPageOps.p_ushortGetLE(page, searchVecLoc);
                int entryLen = leafEntryLengthAtLoc(page, entryLoc);

                if (searchVecLoc == pos) {
                    if ((newAvail -= encodedLen + 2) < 0) {
                        if (vfrag != 0) {
                            break;
                        }

                        newAvail += encodedLen + 2; // undo

                        FragParams params = new FragParams();
                        params.value = value;
                        params.encodedLen = encodedLen;
                        params.available = newAvail;

                        try {
                            fragmentValueForSplit(tree, params);
                        } catch (Throwable e) {
                            cleanupSplit(e, newNode, null);
                            throw e;
                        }

                        vfrag = ENTRY_FRAGMENTED;
                        fv = value = params.value;
                        encodedLen = params.encodedLen;
                        newAvail = params.available;
                    }

                    newLoc = newSearchVecLoc;

                    if (forInsert) {
                        newSearchVecLoc += 2;
                        if (newAvail <= avail) {
                            break;
                        }
                    } else {
                        garbageAccum += entryLen;
                        avail += entryLen;
                        continue;
                    }
                }

                if (searchVecLoc == searchVecEnd) {
                    break;
                }

                if ((newAvail -= entryLen + 2) < 0) {
                    break;
                }

                destLoc -= entryLen;
                DirectPageOps.p_copy(page, entryLoc, newPage, destLoc, entryLen);
                DirectPageOps.p_shortPutLE(newPage, newSearchVecLoc, destLoc);

                garbageAccum += entryLen;
                avail += entryLen + 2;
            }

            newNode.leftSegTail(TN_HEADER_SIZE);
            newNode.searchVecStart(TN_HEADER_SIZE);
            newNode.searchVecEnd(newSearchVecLoc - 2);

            final int originalStart = searchVecStart();
            final int originalGarbage = garbage();
            searchVecStart(searchVecLoc);
            garbage(originalGarbage + garbageAccum);

            try {
                mSplit = newSplitLeft(newNode);

                if (newLoc == 0) {
                    fv = storeIntoSplitLeaf(tree, okey, akey, vfrag, value, encodedLen, forInsert);
                } else {
                    destLoc -= encodedLen;
                    newNode.copyToLeafEntry(okey, akey, vfrag, value, destLoc);
                    DirectPageOps.p_shortPutLE(newPage, newLoc, destLoc);
                }

                mSplit.setKey(tree, newNode.midKey(newNode.highestKeyPos(), this, 0));

                newNode.rightSegTail(destLoc - 1);
                newNode.releaseExclusive();
            } catch (Throwable e) {
                searchVecStart(originalStart);
                garbage(originalGarbage);
                cleanupFragments(e, fv);
                cleanupSplit(e, newNode, mSplit);
                mSplit = null;
                throw e;
            }
        } else {

            int destLoc = TN_HEADER_SIZE;
            int newSearchVecLoc = pageSize(newPage) - 2;

            byte[] fv = null;

            int searchVecLoc = searchVecEnd;
            for (; newAvail > avail; searchVecLoc -= 2, newSearchVecLoc -= 2) {
                int entryLoc = DirectPageOps.p_ushortGetLE(page, searchVecLoc);
                int entryLen = leafEntryLengthAtLoc(page, entryLoc);

                if (forInsert) {
                    if (searchVecLoc + 2 == pos) {
                        if ((newAvail -= encodedLen + 2) < 0) {
                            if (vfrag != 0) {
                                break;
                            }

                            newAvail += encodedLen + 2; // undo

                            FragParams params = new FragParams();
                            params.value = value;
                            params.encodedLen = encodedLen;
                            params.available = newAvail;

                            try {
                                fragmentValueForSplit(tree, params);
                            } catch (Throwable e) {
                                cleanupSplit(e, newNode, null);
                                throw e;
                            }

                            vfrag = ENTRY_FRAGMENTED;
                            fv = value = params.value;
                            encodedLen = params.encodedLen;
                            newAvail = params.available;
                        }

                        newLoc = newSearchVecLoc;
                        newSearchVecLoc -= 2;
                        if (newAvail <= avail) {
                            break;
                        }
                    }
                } else {
                    if (searchVecLoc == pos) {
                        if ((newAvail -= encodedLen + 2) < 0) {
                            if (vfrag != 0) {
                                break;
                            }

                            newAvail += encodedLen + 2;

                            FragParams params = new FragParams();
                            params.value = value;
                            params.encodedLen = encodedLen;
                            params.available = newAvail;

                            try {
                                fragmentValueForSplit(tree, params);
                            } catch (Throwable e) {
                                cleanupSplit(e, newNode, null);
                                throw e;
                            }

                            vfrag = ENTRY_FRAGMENTED;
                            fv = value = params.value;
                            encodedLen = params.encodedLen;
                            newAvail = params.available;
                        }

                        newLoc = newSearchVecLoc;
                        garbageAccum += entryLen;
                        avail += entryLen;
                        continue;
                    }
                }

                if (searchVecLoc == searchVecStart) {
                    break;
                }

                if ((newAvail -= entryLen + 2) < 0) {
                    break;
                }

                DirectPageOps.p_copy(page, entryLoc, newPage, destLoc, entryLen);
                DirectPageOps.p_shortPutLE(newPage, newSearchVecLoc, destLoc);
                destLoc += entryLen;

                garbageAccum += entryLen;
                avail += entryLen + 2;
            }

            newNode.rightSegTail(pageSize(newPage) - 1);
            newNode.searchVecStart(newSearchVecLoc + 2);
            newNode.searchVecEnd(pageSize(newPage) - 2);

            final int originalEnd = searchVecEnd();
            final int originalGarbage = garbage();
            searchVecEnd(searchVecLoc);
            garbage(originalGarbage + garbageAccum);

            try {
                mSplit = newSplitRight(newNode);

                if (newLoc == 0) {
                    fv = storeIntoSplitLeaf(tree, okey, akey, vfrag, value, encodedLen, forInsert);
                } else {
                    newNode.copyToLeafEntry(okey, akey, vfrag, value, destLoc);
                    DirectPageOps.p_shortPutLE(newPage, newLoc, destLoc);
                    destLoc += encodedLen;
                }

                mSplit.setKey(tree, this.midKey(this.highestKeyPos(), newNode, 0));

                newNode.leftSegTail(destLoc);
                newNode.releaseExclusive();
            } catch (Throwable e) {
                searchVecEnd(originalEnd);
                garbage(originalGarbage);
                cleanupFragments(e, fv);
                cleanupSplit(e, newNode, mSplit);
                mSplit = null;
                throw e;
            }
        }
    }

    private static final class FragParams {
        byte[] value;   // in: unfragmented value;  out: fragmented value
        int encodedLen; // in: entry encoded length;  out: updated entry encoded length
        int available;  // in: available bytes in the target leaf node;  out: updated
    }

    private static void fragmentValueForSplit(Tree tree, FragParams params) throws IOException {
        byte[] value = params.value;

        int encodedKeyLen = params.encodedLen - calculateLeafValueLength(value);

        LocalDatabase db = tree.mDatabase;

        int max = Math.min(params.available - 2, db.mMaxFragmentedEntrySize) - encodedKeyLen;

        value = db.fragment(value, value.length, max);

        if (value == null) {
            throw new AssertionError("Frag max: " + max);
        }

        params.value = value;
        params.encodedLen = encodedKeyLen + calculateFragmentedValueLength(value);

        if ((params.available -= params.encodedLen + 2) < 0) {
            throw new AssertionError();
        }
    }

    private byte[] storeIntoSplitLeaf(Tree tree, byte[] okey, byte[] akey,
                                      int vfrag, byte[] value,
                                      int encodedLen, boolean forInsert)
            throws IOException {
        int pos = binarySearch(okey);
        if (!forInsert) {
            if (pos < 0) {
                throw new AssertionError("Key not found");
            }
            updateLeafValue(null, tree, pos, vfrag, value);
            return null;
        }

        if (pos >= 0) {
            throw new AssertionError("Key exists");
        }

        int entryLoc = createLeafEntry(null, tree, ~pos, encodedLen);
        byte[] result = null;

        while (entryLoc < 0) {
            if (vfrag != 0) {
                // TODO: 此处会发生吗？
                throw new DatabaseException("Fragmented entry doesn't fit");
            }

            FragParams params = new FragParams();
            params.value = value;
            params.encodedLen = encodedLen;
            params.available = ~entryLoc;

            fragmentValueForSplit(tree, params);

            vfrag = ENTRY_FRAGMENTED;
            result = value = params.value;
            encodedLen = params.encodedLen;

            entryLoc = createLeafEntry(null, tree, ~pos, encodedLen);
        }

        copyToLeafEntry(okey, akey, vfrag, value, entryLoc);
        return result;
    }

    private void splitInternal(final InResult result,
                               final Tree tree, final int encodedLen,
                               final int keyPos, final int newChildPos)
            throws IOException {
        if (mSplit != null) {
            throw new AssertionError("Node is already split");
        }

        final LocalDatabase db = getDatabase();

        Node newNode;
        try {
            newNode = db.allocDirtyNode(NodeContext.MODE_UNEVICTABLE);
        } catch (DatabaseFullException e) {
            db.capacityLimitOverride(-1);
            try {
                newNode = db.allocDirtyNode(NodeContext.MODE_UNEVICTABLE);
            } finally {
                db.capacityLimitOverride(0);
            }
        }

        db.nodeMapPut(newNode);

        final long newPage = newNode.mPage;

        // newNode.garbage(0);
        DirectPageOps.p_intPutLE(newPage, 0, 0); // set type (fixed later), reserved byte, and garbage

        final long page = mPage;

        final int searchVecStart = searchVecStart();
        final int searchVecEnd = searchVecEnd();

        if ((searchVecEnd - searchVecStart) == 2 && keyPos == 2) {
            Split split;
            try {
                split = newSplitLeft(newNode);
            } catch (Throwable e) {
                cleanupSplit(e, newNode, null);
                throw e;
            }

            result.mEntryLoc = -1;

            int leftKeyLoc = DirectPageOps.p_ushortGetLE(page, searchVecStart);
            int leftKeyLen = keyLengthAtLoc(page, leftKeyLoc);

            DirectPageOps.p_copy(page, leftKeyLoc, newPage, TN_HEADER_SIZE, leftKeyLen);
            int leftSearchVecStart = pageSize(newPage) - (2 + 8 + 8);
            DirectPageOps.p_shortPutLE(newPage, leftSearchVecStart, TN_HEADER_SIZE);

            if (newChildPos == 8) {
                result.mPage = newPage;
                result.mNewChildLoc = leftSearchVecStart + (2 + 8);
            } else {
                if (newChildPos != 16) {
                    throw new AssertionError();
                }
                result.mPage = page;
                result.mNewChildLoc = searchVecEnd + (2 + 8);
            }

            DirectPageOps.p_copy(page, searchVecEnd + 2, newPage, leftSearchVecStart + 2, newChildPos);

            newNode.leftSegTail(TN_HEADER_SIZE + leftKeyLen);
            newNode.rightSegTail(leftSearchVecStart + (2 + 8 + 8 - 1));
            newNode.searchVecStart(leftSearchVecStart);
            newNode.searchVecEnd(leftSearchVecStart);
            newNode.releaseExclusive();

            DirectPageOps.p_copy(page, searchVecEnd, page, searchVecEnd + 8, 2);
            int newSearchVecStart = searchVecEnd + 8;
            searchVecStart(newSearchVecStart);
            searchVecEnd(newSearchVecStart);

            garbage(garbage() + leftKeyLen);

            mSplit = split;

            return;
        }

        result.mPage = newPage;
        final int keyLoc = keyPos + searchVecStart;

        int garbageAccum;
        int newKeyLoc;

        // -2: left
        // -1: guess left
        // +1: guess right
        // +2: right
        int splitSide = (keyPos < (searchVecEnd - searchVecStart - keyPos)) ? -1 : 1;

        Split split = null;
        doSplit:
        while (true) {
            garbageAccum = 0;
            newKeyLoc = 0;

            int size = 5 * (searchVecEnd - searchVecStart) + (1 + 8 + 8)
                    + leftSegTail() + pageSize(page) - rightSegTail() - garbage();

            int newSize = TN_HEADER_SIZE;

            size -= 8;
            newSize += 8;

            if (splitSide < 0) {
                int destLoc = pageSize(newPage);
                int newSearchVecLoc = TN_HEADER_SIZE;

                int searchVecLoc = searchVecStart;
                while (true) {
                    if (searchVecLoc == keyLoc) {
                        newKeyLoc = newSearchVecLoc;
                        newSearchVecLoc += 2;
                        newSize += encodedLen + (2 + 8);
                        if (newSize > pageSize(newPage)) {
                            if (splitSide == -1) {
                                splitSide = 2;
                                continue doSplit;
                            }
                            throw new AssertionError();
                        }
                    }

                    int entryLoc = DirectPageOps.p_ushortGetLE(page, searchVecLoc);
                    int entryLen = keyLengthAtLoc(page, entryLoc);

                    int sizeChange = entryLen + (2 + 8);
                    size -= sizeChange;
                    newSize += sizeChange;

                    searchVecLoc += 2;

                    garbageAccum += entryLen;

                    boolean full = size < TN_HEADER_SIZE | newSize > pageSize(newPage);

                    if (full || newSize >= size) {
                        if (newKeyLoc != 0) {
                            try {
                                split = newSplitLeft(newNode);
                                split.setKey(tree, retrieveKeyAtLoc(page, entryLoc));
                            } catch (Throwable e) {
                                cleanupSplit(e, newNode, split);
                                throw e;
                            }
                            break;
                        }

                        if (splitSide == -1) {
                            splitSide = 2;
                            continue doSplit;
                        }

                        if (full || splitSide != -2) {
                            throw new AssertionError();
                        }
                    }

                    destLoc -= entryLen;
                    DirectPageOps.p_copy(page, entryLoc, newPage, destLoc, entryLen);
                    DirectPageOps.p_shortPutLE(newPage, newSearchVecLoc, destLoc);
                    newSearchVecLoc += 2;
                }

                result.mEntryLoc = destLoc - encodedLen;

                {
                    DirectPageOps.p_copy(page, searchVecEnd + 2, newPage, newSearchVecLoc, newChildPos);

                    result.mNewChildLoc = newSearchVecLoc + newChildPos;

                    int tailChildIdsLen = ((searchVecLoc - searchVecStart) << 2) - newChildPos;
                    DirectPageOps.p_copy(page, searchVecEnd + 2 + newChildPos,
                            newPage, newSearchVecLoc + newChildPos + 8, tailChildIdsLen);
                }

                newNode.leftSegTail(TN_HEADER_SIZE);
                newNode.rightSegTail(destLoc - encodedLen - 1);
                newNode.searchVecStart(TN_HEADER_SIZE);
                newNode.searchVecEnd(newSearchVecLoc - 2);
                newNode.releaseExclusive();

                int shift = (searchVecLoc - searchVecStart) << 2;
                int len = searchVecEnd - searchVecLoc + 2;
                int newSearchVecStart = searchVecLoc + shift;
                DirectPageOps.p_copy(page, searchVecLoc, page, newSearchVecStart, len);
                searchVecStart(newSearchVecStart);
                searchVecEnd(searchVecEnd + shift);
            } else {

                int destLoc = TN_HEADER_SIZE;
                int newSearchVecLoc = pageSize(newPage);

                int searchVecLoc = searchVecEnd + 2;
                moveEntries:
                while (true) {
                    if (searchVecLoc == keyLoc) {
                        newSearchVecLoc -= 2;
                        newKeyLoc = newSearchVecLoc;
                        newSize += encodedLen + (2 + 8);
                        if (newSize > pageSize(newPage)) {
                            if (splitSide == 1) {
                                splitSide = -2;
                                continue doSplit;
                            }
                            throw new AssertionError();
                        }
                    }

                    searchVecLoc -= 2;

                    int entryLoc = DirectPageOps.p_ushortGetLE(page, searchVecLoc);
                    int entryLen = keyLengthAtLoc(page, entryLoc);

                    int sizeChange = entryLen + (2 + 8);
                    size -= sizeChange;
                    newSize += sizeChange;

                    garbageAccum += entryLen;

                    boolean full = size < TN_HEADER_SIZE | newSize > pageSize(newPage);

                    if (full || newSize >= size) {

                        if (newKeyLoc != 0) {
                            try {
                                split = newSplitRight(newNode);
                                split.setKey(tree, retrieveKeyAtLoc(page, entryLoc));
                            } catch (Throwable e) {
                                cleanupSplit(e, newNode, split);
                                throw e;
                            }
                            break moveEntries;
                        }

                        if (splitSide == 1) {
                            splitSide = -2;
                            continue doSplit;
                        }

                        if (full || splitSide != 2) {
                            throw new AssertionError();
                        }
                    }

                    DirectPageOps.p_copy(page, entryLoc, newPage, destLoc, entryLen);
                    newSearchVecLoc -= 2;
                    DirectPageOps.p_shortPutLE(newPage, newSearchVecLoc, destLoc);
                    destLoc += entryLen;
                }

                result.mEntryLoc = destLoc;

                int newVecLen = pageSize(page) - newSearchVecLoc;
                {
                    int highestLoc = pageSize(newPage) - (5 * newVecLen) - 8;
                    int midLoc = ((destLoc + encodedLen + highestLoc + 1) >> 1) & ~1;
                    DirectPageOps.p_copy(newPage, newSearchVecLoc, newPage, midLoc, newVecLen);
                    newKeyLoc -= newSearchVecLoc - midLoc;
                    newSearchVecLoc = midLoc;
                }

                int newSearchVecEnd = newSearchVecLoc + newVecLen - 2;

                {
                    int headChildIdsLen = newChildPos - ((searchVecLoc - searchVecStart + 2) << 2);
                    int newDestLoc = newSearchVecEnd + 2;
                    DirectPageOps.p_copy(page, searchVecEnd + 2 + newChildPos - headChildIdsLen,
                            newPage, newDestLoc, headChildIdsLen);

                    newDestLoc += headChildIdsLen;
                    result.mNewChildLoc = newDestLoc;

                    int tailChildIdsLen =
                            ((searchVecEnd - searchVecStart) << 2) + 16 - newChildPos;
                    DirectPageOps.p_copy(page, searchVecEnd + 2 + newChildPos,
                            newPage, newDestLoc + 8, tailChildIdsLen);
                }

                newNode.leftSegTail(destLoc + encodedLen);
                newNode.rightSegTail(pageSize(newPage) - 1);
                newNode.searchVecStart(newSearchVecLoc);
                newNode.searchVecEnd(newSearchVecEnd);
                newNode.releaseExclusive();

                int len = searchVecLoc - searchVecStart;
                int newSearchVecStart = searchVecEnd + 2 - len;
                DirectPageOps.p_copy(page, searchVecStart, page, newSearchVecStart, len);
                searchVecStart(newSearchVecStart);
            }

            break;
        } // end doSplit

        garbage(garbage() + garbageAccum);
        mSplit = split;

        DirectPageOps.p_shortPutLE(newPage, newKeyLoc, result.mEntryLoc);
    }

    private void compactInternal(InResult result, int encodedLen, int keyPos, int childPos) {
        long page = mPage;

        int searchVecLoc = searchVecStart();
        keyPos += searchVecLoc;
        int newSearchVecSize = searchVecEnd() - searchVecLoc + (2 + 2) + (childPos >> 30);

        int newSearchVecStart;
        int searchVecCap = garbage() + rightSegTail() + 1 - leftSegTail() - encodedLen;
        newSearchVecStart = pageSize(page) -
                (((searchVecCap + newSearchVecSize + ((newSearchVecSize + 2) << 2)) >> 1) & ~1);

        int destLoc = TN_HEADER_SIZE;
        int newSearchVecLoc = newSearchVecStart;
        int newLoc = 0;
        final int searchVecEnd = searchVecEnd();

        LocalDatabase db = getDatabase();
        long dest = db.removeSparePage();

        DirectPageOps.p_intPutLE(dest, 0, type() & 0xff);

        for (; searchVecLoc <= searchVecEnd; searchVecLoc += 2, newSearchVecLoc += 2) {
            if (searchVecLoc == keyPos) {
                newLoc = newSearchVecLoc;
                if (childPos >= 0) {
                    newSearchVecLoc += 2;
                } else {
                    continue;
                }
            }
            DirectPageOps.p_shortPutLE(dest, newSearchVecLoc, destLoc);
            int sourceLoc = DirectPageOps.p_ushortGetLE(page, searchVecLoc);
            int len = keyLengthAtLoc(page, sourceLoc);
            DirectPageOps.p_copy(page, sourceLoc, dest, destLoc, len);
            destLoc += len;
        }

        if (childPos >= 0) {
            if (newLoc == 0) {
                newLoc = newSearchVecLoc;
                newSearchVecLoc += 2;
            }

            DirectPageOps.p_copy(page, searchVecEnd() + 2, dest, newSearchVecLoc, childPos);
            DirectPageOps.p_copy(page, searchVecEnd() + 2 + childPos,
                    dest, newSearchVecLoc + childPos + 8,
                    (newSearchVecSize << 2) - childPos);
        } else {
            if (newLoc == 0) {
                newLoc = newSearchVecLoc;
            }

            DirectPageOps.p_copy(page, searchVecEnd() + 2, dest, newSearchVecLoc, (newSearchVecSize << 2) + 8);
        }

        // db.addSparePage(page);
        // mPage = dest;
        // garbage(0);
        if (db.mFullyMapped) {
            DirectPageOps.p_copy(dest, 0, page, 0, pageSize(page));
            db.addSparePage(dest);
            dest = page;
        } else {
            db.addSparePage(page);
            mPage = dest;
        }
        DirectPageOps.p_shortPutLE(dest, newLoc, destLoc);

        leftSegTail(destLoc + encodedLen);
        rightSegTail(pageSize(dest) - 1);
        searchVecStart(newSearchVecStart);
        searchVecEnd(newSearchVecLoc - 2);

        result.mPage = dest;
        result.mNewChildLoc = newSearchVecLoc + childPos;
        result.mEntryLoc = destLoc;
    }

    static final class InResult {
        long mPage;
        int mNewChildLoc; // location of child pointer
        int mEntryLoc;    // location of key entry, referenced by search vector
    }

    private Split newSplitLeft(Node newNode) {
        Split split = new Split(false, newNode);
        newNode.type((byte) (type() & ~HIGH_EXTREMITY));
        type((byte) (type() & ~LOW_EXTREMITY));
        return split;
    }

    private Split newSplitRight(Node newNode) {
        Split split = new Split(true, newNode);
        newNode.type((byte) (type() & ~LOW_EXTREMITY));
        type((byte) (type() & ~HIGH_EXTREMITY));
        return split;
    }

    @FunctionalInterface
    static interface Supplier {
        Node newNode() throws IOException;
    }

    static Node appendToSortLeaf(Node node, LocalDatabase db,
                                 byte[] okey, byte[] value, Supplier supplier)
            throws IOException {
        byte[] akey = okey;
        int encodedKeyLen = calculateAllowedKeyLength(db, okey);

        if (encodedKeyLen < 0) {
            akey = db.fragmentKey(okey);
            encodedKeyLen = 2 + akey.length;
        }

        try {
            int encodedLen = encodedKeyLen + calculateLeafValueLength(value);

            int vfrag;
            if (encodedLen <= db.mMaxEntrySize) {
                vfrag = 0;
            } else {
                value = db.fragment(value, value.length,
                        db.mMaxFragmentedEntrySize - encodedKeyLen);
                if (value == null) {
                    throw new AssertionError();
                }
                encodedLen = encodedKeyLen + calculateFragmentedValueLength(value);
                vfrag = ENTRY_FRAGMENTED;
            }

            try {
                while (true) {
                    long page = node.mPage;
                    int tail = node.leftSegTail();

                    int start;
                    if (tail == TN_HEADER_SIZE) {
                        if (page == DirectPageOps.p_closedTreePage()) {
                            throw new DatabaseException("Closed");
                        }
                        start = node.pageSize(page) - 2;
                        node.searchVecEnd(start);
                    } else {
                        start = node.searchVecStart() - 2;
                        if (encodedLen > (start - tail)) {
                            node.releaseExclusive();
                            node = supplier.newNode();
                            continue;
                        }
                    }

                    node.copyToLeafEntry(okey, akey, vfrag, value, tail);
                    node.leftSegTail(tail + encodedLen);

                    DirectPageOps.p_shortPutLE(page, start, tail);
                    node.searchVecStart(start);
                    return node;
                }
            } catch (Throwable e) {
                if (vfrag == ENTRY_FRAGMENTED) {
                    node.cleanupFragments(e, value);
                }
                throw e;
            }
        } catch (Throwable e) {
            if (okey != akey) {
                node.cleanupFragments(e, akey);
            }
            throw e;
        }
    }

    void sortLeaf() throws IOException {
        final int len = searchVecEnd() + 2 - searchVecStart();
        if (len <= 2) {
            return;
        }

        final int halfPos = (len >>> 1) & ~1;
        for (int pos = halfPos; (pos -= 2) >= 0; ) {
            siftDownLeaf(pos, len, halfPos);
        }

        final long page = mPage;
        final int start = searchVecStart();

        int lastHighLoc = -1;
        int vecPos = start + len;
        int pos = len - 2;
        do {
            int highLoc = DirectPageOps.p_ushortGetLE(page, start);
            DirectPageOps.p_shortPutLE(page, start, DirectPageOps.p_ushortGetLE(page, start + pos));
            if (highLoc != lastHighLoc) {
                DirectPageOps.p_shortPutLE(page, vecPos -= 2, highLoc);
                lastHighLoc = highLoc;
            }
            if (pos > 2) {
                siftDownLeaf(0, pos, (pos >>> 1) & ~1);
            }
        } while ((pos -= 2) >= 0);

        searchVecStart(vecPos);
    }

    private void siftDownLeaf(int pos, int endPos, int halfPos) throws IOException {
        final long page = mPage;
        final int start = searchVecStart();
        int loc = DirectPageOps.p_ushortGetLE(page, start + pos);

        do {
            int childPos = (pos << 1) + 2;
            int childLoc = DirectPageOps.p_ushortGetLE(page, start + childPos);
            int rightPos = childPos + 2;
            if (rightPos < endPos) {
                int rightLoc = DirectPageOps.p_ushortGetLE(page, start + rightPos);
                int compare = compareKeys(this, childLoc, this, rightLoc);
                if (compare < 0) {
                    childPos = rightPos;
                    childLoc = rightLoc;
                } else if (compare == 0) {
                    if (childLoc < rightLoc) {
                        replaceDuplicateLeafEntry(page, childLoc, rightLoc);
                        if (loc == childLoc) {
                            return;
                        }
                        childLoc = rightLoc;
                    } else if (childLoc > rightLoc) {
                        replaceDuplicateLeafEntry(page, rightLoc, childLoc);
                        if (loc == rightLoc) {
                            return;
                        }
                    }
                }
            }
            int compare = compareKeys(this, loc, this, childLoc);
            if (compare < 0) {
                DirectPageOps.p_shortPutLE(page, start + pos, childLoc);
                pos = childPos;
            } else {
                if (compare == 0) {
                    if (loc < childLoc) {
                        replaceDuplicateLeafEntry(page, loc, childLoc);
                        loc = childLoc;
                    } else if (loc > childLoc) {
                        replaceDuplicateLeafEntry(page, childLoc, loc);
                    }
                }
                break;
            }
        } while (pos < halfPos);

        DirectPageOps.p_shortPutLE(page, start + pos, loc);
    }

    private void replaceDuplicateLeafEntry(long page, int loc, int newLoc)
            throws IOException {
        int entryLen = doDeleteLeafEntry(page, loc) - loc;

        garbage(garbage() + entryLen);

        DirectPageOps.p_shortPutLE(page, loc, 0x8000); // encoding for an empty key
        DirectPageOps.p_bytePut(page, loc + 2, -1); // encoding for a ghost value

        int pos = searchVecStart();
        int endPos = searchVecEnd();
        for (; pos <= endPos; pos += 2) {
            if (DirectPageOps.p_ushortGetLE(page, pos) == loc) {
                DirectPageOps.p_shortPutLE(page, pos, newLoc);
            }
        }
    }

    void deleteFirstSortLeafEntry() throws IOException {
        long page = mPage;
        int start = searchVecStart();
        doDeleteLeafEntry(page, DirectPageOps.p_ushortGetLE(page, start));
        searchVecStart(start + 2);
    }

    long countCursors() {
        if (tryAcquireExclusive()) {
            long count = 0;
            try {
                CursorFrame frame = mLastCursorFrame;
                while (frame != null) {
                    if (!(frame instanceof GhostFrame)) {
                        count++;
                    }
                    frame = frame.getPrevCousin();
                }
            } finally {
                releaseExclusive();
            }
            return count;
        }

        acquireShared();
        try {
            CursorFrame frame = mLastCursorFrame;

            if (frame == null) {
                return 0;
            }

            CursorFrame lock = new CursorFrame();
            CursorFrame lockResult;

            while (true) {
                lockResult = frame.tryLock(lock);
                if (lockResult != null) {
                    break;
                }
                frame = frame.getPrevCousin();
                if (frame == null) {
                    return 0;
                }
            }

            long count = 0;

            while (true) {
                if (!(frame instanceof GhostFrame)) {
                    count++;
                }
                CursorFrame prev = frame.tryLockPrevious(lock);
                frame.unlock(lockResult);
                if (prev == null) {
                    return count;
                }
                lockResult = frame;
                frame = prev;
            }
        } finally {
            releaseShared();
        }
    }

    @Override
    @SuppressWarnings("fallthrough")
    public String toString() {
        String prefix;

        switch (type()) {
            case TYPE_UNDO_LOG:
                return "UndoNode: {id=" + mId +
                        ", cachedState=" + mCachedState +
                        ", topEntry=" + garbage() +
                        ", lowerNodeId=" + +DirectPageOps.p_longGetLE(mPage, 4) +
                        ", latchState=" + super.toString() +
                        '}';
            // case TYPE_FRAGMENT:
            // return "FragmentNode: {id=" + mId +
            // ", cachedState=" + mCachedState +
            // ", latchState=" + super.toString() +
            // '}';
            case TYPE_TN_IN:
            case (TYPE_TN_IN | LOW_EXTREMITY):
            case (TYPE_TN_IN | HIGH_EXTREMITY):
            case (TYPE_TN_IN | LOW_EXTREMITY | HIGH_EXTREMITY):
                prefix = "Internal";
                break;

            case TYPE_TN_BIN:
            case (TYPE_TN_BIN | LOW_EXTREMITY):
            case (TYPE_TN_BIN | HIGH_EXTREMITY):
            case (TYPE_TN_BIN | LOW_EXTREMITY | HIGH_EXTREMITY):
                prefix = "BottomInternal";
                break;
            default:
                if (!isLeaf()) {
                    return "Node: {id=" + mId +
                            ", cachedState=" + mCachedState +
                            ", latchState=" + super.toString() +
                            '}';
                }
            case TYPE_TN_LEAF:
                prefix = "Leaf";
                break;
        }

        char[] extremity = {'_', '_'};

        if ((type() & LOW_EXTREMITY) != 0) {
            extremity[0] = 'L';
        }
        if ((type() & HIGH_EXTREMITY) != 0) {
            extremity[1] = 'H';
        }

        return prefix + "Node: {id=" + mId +
                ", cachedState=" + mCachedState +
                ", isSplit=" + (mSplit != null) +
                ", availableBytes=" + availableBytes() +
                ", extremity=" + new String(extremity) +
                ", latchState=" + super.toString() +
                '}';
    }

    boolean verifyTreeNode(int level, VerificationObserver observer) throws IOException {
        return verifyTreeNode(level, observer, false);
    }

    private boolean verifyTreeNode(int level, VerificationObserver observer, boolean fix)
            throws IOException {
        int type = type() & ~(LOW_EXTREMITY | HIGH_EXTREMITY);
        if (type != TYPE_TN_IN && type != TYPE_TN_BIN && !isLeaf()) {
            return verifyFailed(level, observer, "Not a tree node: " + type);
        }

        final long page = mPage;

        if (!fix) {
            if (leftSegTail() < TN_HEADER_SIZE) {
                return verifyFailed(level, observer, "Left segment tail: " + leftSegTail());
            }

            if (searchVecStart() < leftSegTail()) {
                return verifyFailed(level, observer, "Search vector start: " + searchVecStart());
            }

            if (searchVecEnd() < (searchVecStart() - 2)) {
                return verifyFailed(level, observer, "Search vector end: " + searchVecEnd());
            }

            if (rightSegTail() < searchVecEnd() || rightSegTail() > (pageSize(page) - 1)) {
                return verifyFailed(level, observer, "Right segment tail: " + rightSegTail());
            }
        }

        if (!isLeaf()) {
            int childIdsStart = searchVecEnd() + 2;
            int childIdsEnd = childIdsStart + ((childIdsStart - searchVecStart()) << 2) + 8;
            if (childIdsEnd > (rightSegTail() + 1)) {
                return verifyFailed(level, observer, "Child ids end: " + childIdsEnd);
            }

            LHashTable.Int childIds = new LHashTable.Int(512);

            for (int i = childIdsStart; i < childIdsEnd; i += 8) {
                long childId = DirectPageOps.p_uint48GetLE(page, i);
                if (mId > 1 && childId <= 1) { // stubs don't have a valid child id
                    return verifyFailed(level, observer, "Illegal child id: " + childId);
                }
                LHashTable.IntEntry e = childIds.insert(childId);
                if (e.value != 0) {
                    return verifyFailed(level, observer, "Duplicate child id: " + childId);
                }
                e.value = 1;
            }
        }

        int used = TN_HEADER_SIZE;
        int leftTail = TN_HEADER_SIZE;
        int rightTail = pageSize(page); // compute as inclusive
        int largeValueCount = 0;
        int lastKeyLoc = 0;

        for (int i = searchVecStart(); i <= searchVecEnd(); i += 2) {
            final int keyLoc = DirectPageOps.p_ushortGetLE(page, i);
            int loc = keyLoc;

            if (loc < TN_HEADER_SIZE || loc >= pageSize(page) ||
                    (!fix && loc >= leftSegTail() && loc <= rightSegTail())) {
                return verifyFailed(level, observer, "Entry location: " + loc);
            }

            if (isLeaf()) {
                used += leafEntryLengthAtLoc(page, loc);
            } else {
                used += keyLengthAtLoc(page, loc);
            }

            if (loc > searchVecEnd()) {
                rightTail = Math.min(loc, rightTail);
            }

            int keyLen;
            try {
                keyLen = DirectPageOps.p_byteGet(page, loc++);
                keyLen = keyLen >= 0 ? (keyLen + 1)
                        : (((keyLen & 0x3f) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
            } catch (IndexOutOfBoundsException e) {
                return verifyFailed(level, observer, "Key location out of bounds");
            }

            loc += keyLen;

            if (loc > pageSize(page)) {
                return verifyFailed(level, observer, "Key end location: " + loc);
            }

            if (lastKeyLoc != 0) {
                int result = compareKeys(this, lastKeyLoc, this, keyLoc);
                if (result >= 0) {
                    return verifyFailed(level, observer, "Key order: " + result);
                }
            }

            lastKeyLoc = keyLoc;

            if (isLeaf()) value:{
                int len;
                try {
                    int header = DirectPageOps.p_byteGet(page, loc++);
                    if (header >= 0) {
                        len = header;
                    } else {
                        if ((header & 0x20) == 0) {
                            len = 1 + (((header & 0x1f) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
                        } else if (header != -1) {
                            len = 1 + (((header & 0x0f) << 16)
                                    | (DirectPageOps.p_ubyteGet(page, loc++) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
                        } else {
                            break value;
                        }
                        if ((header & ENTRY_FRAGMENTED) != 0) {
                            largeValueCount++;
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    return verifyFailed(level, observer, "Value location out of bounds");
                }
                loc += len;
                if (loc > pageSize(page)) {
                    return verifyFailed(level, observer, "Value end location: " + loc);
                }
            }

            if (loc <= searchVecStart()) {
                leftTail = Math.max(leftTail, loc);
            }
        }

        if (fix) {
            int garbage = pageSize(page) - (used + rightTail - leftTail);
            garbage(garbage);
            leftSegTail(leftTail);
            rightSegTail(rightTail - 1);
        } else {
            used += rightSegTail() + 1 - leftSegTail();
            int garbage = pageSize(page) - used;
            if (garbage() != garbage && mId > 1) {
                return verifyFailed(level, observer, "Garbage: " + garbage() + " != " + garbage);
            }
        }

        if (observer == null) {
            return true;
        }

        int entryCount = numKeys();
        int freeBytes = availableBytes();

        long id = mId;
        releaseShared();

        return observer.indexNodePassed(id, level, entryCount, freeBytes, largeValueCount);
    }

    private boolean verifyFailed(int level, VerificationObserver observer, String message)
            throws CorruptDatabaseException {
        if (observer == null) {
            throw new CorruptDatabaseException(message);
        }
        long id = mId;
        releaseShared();
        observer.setFailed(true);
        return observer.indexNodeFailed(id, level, message);
    }
}
