package com.glodon.linglong.engine.core.tx;


import com.glodon.linglong.base.common.IntegerRef;
import com.glodon.linglong.base.common.LHashTable;
import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.exception.ClosedIndexException;
import com.glodon.linglong.base.exception.CorruptDatabaseException;
import com.glodon.linglong.base.exception.DatabaseException;
import com.glodon.linglong.base.exception.LockFailureException;
import com.glodon.linglong.engine.core.*;
import com.glodon.linglong.engine.core.frame.GhostFrame;
import com.glodon.linglong.engine.core.lock.CommitLock;
import com.glodon.linglong.engine.core.lock.Lock;
import com.glodon.linglong.engine.core.lock.LockManager;
import com.glodon.linglong.engine.core.lock.LockMode;
import com.glodon.linglong.engine.core.page.DirectPageOps;
import com.glodon.linglong.engine.event.EventListener;
import com.glodon.linglong.engine.event.EventType;
import com.glodon.linglong.engine.extend.TransactionHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import static com.glodon.linglong.base.common.IOUtils.*;
import static java.lang.System.arraycopy;

/**
 * @author Stereo
 */
public final class UndoLog implements DatabaseAccess {
    UndoLog mPrev;
    UndoLog mNext;

    /*
      UndoLog is persisted in Nodes. All multibyte types are little endian encoded.

      +----------------------------------------+
      | byte:   node type                      |  header
      | byte:   reserved (must be 0)           |
      | ushort: pointer to top entry           |
      | ulong:  lower node id                  |
      +----------------------------------------+
      | free space                             |
      -                                        -
      |                                        |
      +----------------------------------------+
      | log stack entries                      |
      -                                        -
      |                                        |
      +----------------------------------------+

      Stack entries are encoded from the tail end of the node towards the
      header. Entries without payloads are encoded with an opcode less than 16.
      All other types of entries are composed of three sections:

      +----------------------------------------+
      | byte:   opcode                         |
      | varint: payload length                 |
      | n:      payload                        |
      +----------------------------------------+

      Popping entries off the stack involves reading the opcode and moving
      forwards. Payloads which don't fit into the node spill over into the
      lower node(s).
    */

    static final int I_LOWERNode_ID = 4;
    private static final int HEADER_SIZE = 12;

    private static final int INITIAL_BUFFER_SIZE = 128;

    private static final byte OP_SCOPE_ENTER = (byte) 1;
    private static final byte OP_SCOPE_COMMIT = (byte) 2;

    static final byte OP_COMMIT = (byte) 4;

    static final byte OP_COMMIT_TRUNCATE = (byte) 5;

    static final byte OP_PREPARE = (byte) 6;

    static final byte OP_UNCREATE = (byte) 12;

    private static final byte PAYLOAD_OP = (byte) 16;

    private static final byte OP_LOG_COPY = (byte) 16;

    private static final byte OP_LOG_REF = (byte) 17;

    private static final byte OP_INDEX = (byte) 18;

    static final byte OP_UNINSERT = (byte) 19;

    public static final byte OP_UNUPDATE = (byte) 20;

    public static final byte OP_UNDELETE = (byte) 21;

    static final byte OP_UNDELETE_FRAGMENTED = (byte) 22;

    static final byte OP_ACTIVE_KEY = (byte) 23;

    static final byte OP_CUSTOM = (byte) 24;

    private static final int LK_ADJUST = 5;

    static final byte OP_UNUPDATE_LK = (byte) (OP_UNUPDATE + LK_ADJUST); //25

    static final byte OP_UNDELETE_LK = (byte) (OP_UNDELETE + LK_ADJUST); //26

    static final byte OP_UNDELETE_LK_FRAGMENTED = (byte) (OP_UNDELETE_FRAGMENTED + LK_ADJUST); //27

    static final byte OP_UNEXTEND = (byte) 29;

    static final byte OP_UNALLOC = (byte) 30;

    static final byte OP_UNWRITE = (byte) 31;

    private final LocalDatabase mDatabase;
    private final long mTxnId;

    private long mLength;

    private byte[] mBuffer;
    private int mBufferPos;

    private Node mNode;
    private int mNodeTopPos;

    private long mActiveIndexId;

    private byte[] mActiveKey;

    public UndoLog(LocalDatabase db, long txnId) {
        mDatabase = db;
        mTxnId = txnId;
    }

    @Override
    public LocalDatabase getDatabase() {
        return mDatabase;
    }

    public long persistReady() throws IOException {
        Node node = mNode;

        if (node != null) {
            node.acquireExclusive();
            try {
                mDatabase.markUnmappedDirty(node);
            } catch (Throwable e) {
                node.releaseExclusive();
                throw e;
            }
        } else if (mLength == 0) {
            return 0;
        } else {
            mNode = node = allocUnevictableNode(0);

            byte[] buffer = mBuffer;
            if (buffer == null) {
                mNodeTopPos = pageSize(node.getPage());
            } else {
                int pos = mBufferPos;
                int size = buffer.length - pos;
                long page = node.getPage();
                int newPos = pageSize(page) - size;
                DirectPageOps.p_copyFromArray(buffer, pos, page, newPos, size);
                mNodeTopPos = newPos;
                mBuffer = null;
                mBufferPos = 0;
            }
        }

        node.undoTop(mNodeTopPos);
        node.releaseExclusive();

        return mNode.getId();
    }

    public long topNodeId() throws IOException {
        return mNode == null ? 0 : mNode.getId();
    }

    private int pageSize(long page) {
        // return page.length;
        return mDatabase.pageSize();
    }

    long txnId() {
        return mTxnId;
    }

    void delete() {
        Node node = mNode;
        if (node != null) {
            mNode = null;
            node.delete(mDatabase);
        }
    }

    final void pushUninsert(final long indexId, byte[] key) throws IOException {
        setActiveIndexId(indexId);
        doPush(OP_UNINSERT, key);
    }

    final void pushNodeEncoded(final long indexId, byte op, byte[] payload, int off, int len)
            throws IOException {
        setActiveIndexId(indexId);

        if ((payload[off] & 0xc0) == 0xc0) {
            long copy = DirectPageOps.p_transfer(payload, false);
            try {
                payload = Node.expandKeyAtLoc(this, copy, off, len, op != OP_UNDELETE_FRAGMENTED);
            } finally {
                DirectPageOps.p_delete(copy);
            }
            off = 0;
            len = payload.length;
            op += LK_ADJUST;
        }

        doPush(op, payload, off, len);
    }

    final void pushNodeEncoded(final long indexId, byte op, long payloadPtr, int off, int len)
            throws IOException {
        setActiveIndexId(indexId);

        byte[] payload;
        if ((DirectPageOps.p_byteGet(payloadPtr, off) & 0xc0) == 0xc0) {
            // throw new AssertionError(); // 不应该使用直接页面访问
            payload = Node.expandKeyAtLoc
                    (this, payloadPtr, off, len, op != OP_UNDELETE_FRAGMENTED);
            op += LK_ADJUST;
        } else {
            payload = new byte[len];
            DirectPageOps.p_copyToArray(payloadPtr, off, payload, 0, len);
        }

        doPush(op, payload);
    }

    private void setActiveIndexId(long indexId) throws IOException {
        long activeIndexId = mActiveIndexId;
        if (indexId != activeIndexId) {
            if (activeIndexId != 0) {
                byte[] payload = new byte[8];
                encodeLongLE(payload, 0, activeIndexId);
                doPush(OP_INDEX, payload, 0, 8, 1);
            }
            mActiveIndexId = indexId;
        }
    }

    void pushCommit() throws IOException {
        doPush(OP_COMMIT);
    }

    void pushPrepare() throws IOException {
        doPush(OP_PREPARE);
    }

    void pushCustom(byte[] message) throws IOException {
        doPush(OP_CUSTOM, message);
    }

    void pushUncreate(long indexId, byte[] key) throws IOException {
        setActiveIndexIdAndKey(indexId, key);
        doPush(OP_UNCREATE);
    }

    void pushUnextend(long savepoint, long indexId, byte[] key, long length) throws IOException {
        if (setActiveIndexIdAndKey(indexId, key) && savepoint < mLength) discardCheck:{
            long unlen;

            Node node = mNode;
            if (node == null) {
                byte op = mBuffer[mBufferPos];
                if (op == OP_UNCREATE) {
                    return;
                }
                if (op != OP_UNEXTEND) {
                    break discardCheck;
                }
                int pos = mBufferPos + 1;
                int payloadLen = Utils.decodeUnsignedVarInt(mBuffer, pos);
                pos += Utils.calcUnsignedVarIntLength(payloadLen);
                IntegerRef.Value offsetRef = new IntegerRef.Value();
                offsetRef.set(pos);
                unlen = Utils.decodeUnsignedVarLong(mBuffer, offsetRef);
            } else {
                byte op = DirectPageOps.p_byteGet(mNode.getPage(), mNodeTopPos);
                if (op == OP_UNCREATE) {
                    return;
                }
                if (op != OP_UNEXTEND) {
                    break discardCheck;
                }
                int pos = mNodeTopPos + 1;
                int payloadLen = DirectPageOps.p_uintGetVar(mNode.getPage(), pos);
                pos += Utils.calcUnsignedVarIntLength(payloadLen);
                if (pos + payloadLen > pageSize(mNode.getPage())) {
                    break discardCheck;
                }
                IntegerRef.Value offsetRef = new IntegerRef.Value();
                offsetRef.set(pos);
                unlen = DirectPageOps.p_ulongGetVar(mNode.getPage(), offsetRef);
            }

            if (unlen <= length) {
                return;
            }
        }

        byte[] payload = new byte[9];
        int off = Utils.encodeUnsignedVarLong(payload, 0, length);
        doPush(OP_UNEXTEND, payload, 0, off);
    }

    void pushUnalloc(long indexId, byte[] key, long pos, long length) throws IOException {
        setActiveIndexIdAndKey(indexId, key);
        byte[] payload = new byte[9 + 9];
        int off = Utils.encodeUnsignedVarLong(payload, 0, length);
        off = Utils.encodeUnsignedVarLong(payload, off, pos);
        doPush(OP_UNALLOC, payload, 0, off);
    }

    void pushUnwrite(long indexId, byte[] key, long pos, byte[] b, int off, int len)
            throws IOException {
        setActiveIndexIdAndKey(indexId, key);
        int pLen = Utils.calcUnsignedVarLongLength(pos);
        int varIntLen = Utils.calcUnsignedVarIntLength(pLen + len);
        doPush(OP_UNWRITE, b, off, len, varIntLen, pLen);

        Node node = mNode;
        int posOff = 1 + varIntLen;
        if (node != null) {
            DirectPageOps.p_ulongPutVar(node.getPage(), mNodeTopPos + posOff, pos);
        } else {
            Utils.encodeUnsignedVarLong(mBuffer, mBufferPos + posOff, pos);
        }
    }

    void pushUnwrite(long indexId, byte[] key, long pos, long ptr, int off, int len)
            throws IOException {
        byte[] b = new byte[len];
        DirectPageOps.p_copyToArray(ptr, off, b, 0, len);
        pushUnwrite(indexId, key, pos, b, 0, len);
    }

    private boolean setActiveIndexIdAndKey(long indexId, byte[] key) throws IOException {
        boolean result = true;

        long activeIndexId = mActiveIndexId;
        if (indexId != activeIndexId) {
            if (activeIndexId != 0) {
                byte[] payload = new byte[8];
                encodeLongLE(payload, 0, activeIndexId);
                doPush(OP_INDEX, payload, 0, 8, 1);
            }
            mActiveIndexId = indexId;
            result = false;
        }

        byte[] activeKey = mActiveKey;
        if (!Arrays.equals(key, activeKey)) {
            if (activeKey != null) {
                doPush(OP_ACTIVE_KEY, mActiveKey);
            }
            mActiveKey = key;
            result = false;
        }

        return result;
    }

    private void doPush(final byte op) throws IOException {
        doPush(op, Utils.EMPTY_BYTES, 0, 0, 0);
    }

    private void doPush(final byte op, final byte[] payload) throws IOException {
        doPush(op, payload, 0, payload.length);
    }

    private void doPush(final byte op, final byte[] payload, final int off, final int len)
            throws IOException {
        doPush(op, payload, off, len, Utils.calcUnsignedVarIntLength(len), 0);
    }

    private void doPush(final byte op, final byte[] payload, final int off, final int len,
                        final int varIntLen)
            throws IOException {
        doPush(op, payload, off, len, varIntLen, 0);
    }

    private void doPush(final byte op, final byte[] payload, final int off, final int len,
                        final int varIntLen, final int pLen)
            throws IOException {
        final int encodedLen = 1 + varIntLen + pLen + len;

        Node node = mNode;
        if (node != null) {
            node.acquireExclusive();
            try {
                mDatabase.markUnmappedDirty(node);
            } catch (Throwable e) {
                node.releaseExclusive();
                throw e;
            }
        } else quick:{
            byte[] buffer = mBuffer;
            int pos;
            if (buffer == null) {
                int newCap = Math.max(INITIAL_BUFFER_SIZE, Utils.roundUpPower2(encodedLen));
                int pageSize = mDatabase.pageSize();
                if (newCap <= (pageSize >> 1)) {
                    mBuffer = buffer = new byte[newCap];
                    mBufferPos = pos = newCap;
                } else {
                    mNode = node = allocUnevictableNode(0);
                    mNodeTopPos = pageSize;
                    break quick;
                }
            } else {
                pos = mBufferPos;
                if (pos < encodedLen) {
                    final int size = buffer.length - pos;
                    int newCap = Utils.roundUpPower2(encodedLen + size);
                    if (newCap < 0) {
                        newCap = Integer.MAX_VALUE;
                    } else {
                        newCap = Math.max(buffer.length << 1, newCap);
                    }
                    if (newCap <= (mDatabase.pageSize() >> 1)) {
                        byte[] newBuf = new byte[newCap];
                        int newPos = newCap - size;
                        arraycopy(buffer, pos, newBuf, newPos, size);
                        mBuffer = buffer = newBuf;
                        mBufferPos = pos = newPos;
                    } else {
                        mNode = node = allocUnevictableNode(0);
                        long page = node.getPage();
                        int newPos = pageSize(page) - size;
                        DirectPageOps.p_copyFromArray(buffer, pos, page, newPos, size);
                        mNodeTopPos = newPos;
                        mBuffer = null;
                        mBufferPos = 0;
                        break quick;
                    }
                }
            }

            pos -= encodedLen;
            buffer[pos] = op;
            if (op >= PAYLOAD_OP) {
                int payloadPos = Utils.encodeUnsignedVarInt(buffer, pos + 1, pLen + len) + pLen;
                arraycopy(payload, off, buffer, payloadPos, len);
            }
            mBufferPos = pos;
            mLength += encodedLen;
            return;
        }

        int pos = mNodeTopPos;
        int available = pos - HEADER_SIZE;
        if (available >= encodedLen) {
            pos -= encodedLen;
            long page = node.getPage();
            DirectPageOps.p_bytePut(page, pos, op);
            if (op >= PAYLOAD_OP) {
                int payloadPos = DirectPageOps.p_uintPutVar(page, pos + 1, pLen + len) + pLen;
                DirectPageOps.p_copyFromArray(payload, off, page, payloadPos, len);
            }
            node.releaseExclusive();
            mNodeTopPos = pos;
            mLength += encodedLen;
            return;
        }

        int remaining = len;

        while (true) {
            int amt = Math.min(available, remaining);
            pos -= amt;
            available -= amt;
            remaining -= amt;
            long page = node.getPage();
            DirectPageOps.p_copyFromArray(payload, off + remaining, page, pos, amt);

            if (remaining <= 0 && available >= (encodedLen - len)) {
                if (varIntLen > 0) {
                    DirectPageOps.p_uintPutVar(page, pos -= varIntLen + pLen, pLen + len);
                }
                DirectPageOps.p_bytePut(page, --pos, op);
                node.releaseExclusive();
                break;
            }

            Node newNode;
            try {
                newNode = allocUnevictableNode(node.getId());
            } catch (Throwable e) {
                while (node != mNode) {
                    node = popNode(node, true);
                }
                node.releaseExclusive();
                throw e;
            }

            node.undoTop(pos);
            mDatabase.nodeMapPut(node);
            node.releaseExclusive();
            node.makeEvictable();

            node = newNode;
            pos = pageSize(page);
            available = pos - HEADER_SIZE;
        }

        mNode = node;
        mNodeTopPos = pos;
        mLength += encodedLen;
    }

    final long scopeEnter() throws IOException {
        final CommitLock.Shared shared = mDatabase.commitLock().acquireShared();
        try {
            long savepoint = mLength;
            doScopeEnter();
            return savepoint;
        } finally {
            shared.release();
        }
    }

    final void doScopeEnter() throws IOException {
        doPush(OP_SCOPE_ENTER);
    }

    final long scopeCommit() throws IOException {
        final CommitLock.Shared shared = mDatabase.commitLock().acquireShared();
        try {
            doPush(OP_SCOPE_COMMIT);
            return mLength;
        } finally {
            shared.release();
        }
    }

    final void scopeRollback(long savepoint) throws IOException {
        final CommitLock.Shared shared = mDatabase.commitLock().acquireShared();
        try {
            if (savepoint < mLength) {
                doRollback(savepoint);
            }
        } finally {
            shared.release();
        }
    }

    final void truncate(boolean commit) throws IOException {
        final CommitLock commitLock = mDatabase.commitLock();
        CommitLock.Shared shared = commitLock.acquireShared();
        try {
            shared = doTruncate(commitLock, shared, commit);
        } finally {
            shared.release();
        }
    }

    public final CommitLock.Shared doTruncate(CommitLock commitLock, CommitLock.Shared shared,
                                              boolean commit)
            throws IOException {
        if (mLength > 0) {
            Node node = mNode;
            if (node == null) {
                mBufferPos = mBuffer.length;
            } else {
                node.acquireExclusive();
                while ((node = popNode(node, true)) != null) {
                    if (commit) {
                        mDatabase.prepareToDelete(node);
                        mDatabase.redirty(node);
                        long page = node.getPage();
                        int end = pageSize(page) - 1;
                        node.undoTop(end);
                        DirectPageOps.p_bytePut(page, end, OP_COMMIT_TRUNCATE);
                    }
                    if (commitLock.hasQueuedThreads()) {
                        shared.release();
                        shared = commitLock.acquireShared();
                    }
                }
            }
            mLength = 0;
            mActiveIndexId = 0;
            mActiveKey = null;
        }

        return shared;
    }

    final void rollback() throws IOException {
        if (mLength == 0) {
            return;
        }

        final CommitLock.Shared shared = mDatabase.commitLock().acquireShared();
        try {
            doRollback(0);
        } finally {
            shared.release();
        }
    }

    private void doRollback(long savepoint) throws IOException {
        byte[] opRef = new byte[1];
        Index activeIndex = null;
        do {
            byte[] entry = pop(opRef, true);
            if (entry == null) {
                break;
            }
            byte op = opRef[0];
            activeIndex = undo(activeIndex, op, entry);
        } while (savepoint < mLength);
    }

    final void deleteGhosts() throws IOException {
        if (mLength <= 0) {
            return;
        }

        byte[] opRef = new byte[1];
        Index activeIndex = null;
        do {
            byte[] entry = pop(opRef, true);
            if (entry == null) {
                break;
            }

            byte op = opRef[0];
            switch (op) {
                default:
                    throw new DatabaseException("Unknown undo log entry type: " + op);

                case OP_SCOPE_ENTER:
                case OP_SCOPE_COMMIT:
                case OP_COMMIT:
                case OP_COMMIT_TRUNCATE:
                case OP_PREPARE:
                case OP_UNCREATE:
                case OP_UNINSERT:
                case OP_UNUPDATE:
                case OP_ACTIVE_KEY:
                case OP_CUSTOM:
                case OP_UNUPDATE_LK:
                case OP_UNEXTEND:
                case OP_UNALLOC:
                case OP_UNWRITE:
                    // Ignore.
                    break;

                case OP_INDEX:
                    mActiveIndexId = decodeLongLE(entry, 0);
                    activeIndex = null;
                    break;

                case OP_UNDELETE:
                case OP_UNDELETE_FRAGMENTED:
                    if ((activeIndex = findIndex(activeIndex)) != null) {
                        byte[] key = decodeNodeKey(entry);

                        do {
                            TreeCursor cursor = new TreeCursor((Tree) activeIndex, null);
                            try {
                                cursor.deleteGhost(key);
                                break;
                            } catch (ClosedIndexException e) {
                                activeIndex = findIndex(null);
                            } catch (Throwable e) {
                                throw closeOnFailure(cursor, e);
                            }
                        } while (activeIndex != null);
                    }
                    break;

                case OP_UNDELETE_LK:
                case OP_UNDELETE_LK_FRAGMENTED:
                    if ((activeIndex = findIndex(activeIndex)) != null) {
                        byte[] key = new byte[Utils.decodeUnsignedVarInt(entry, 0)];
                        arraycopy(entry, Utils.calcUnsignedVarIntLength(key.length), key, 0, key.length);

                        do {
                            TreeCursor cursor = new TreeCursor((Tree) activeIndex, null);
                            try {
                                cursor.deleteGhost(key);
                                break;
                            } catch (ClosedIndexException e) {
                                activeIndex = findIndex(null);
                            } catch (Throwable e) {
                                throw closeOnFailure(cursor, e);
                            }
                        } while (activeIndex != null);
                    }
                    break;
            }
        } while (mLength > 0);
    }

    private Index undo(Index activeIndex, byte op, byte[] entry) throws IOException {
        switch (op) {
            default:
                throw new DatabaseException("Unknown undo log entry type: " + op);

            case OP_SCOPE_ENTER:
            case OP_SCOPE_COMMIT:
            case OP_COMMIT:
            case OP_COMMIT_TRUNCATE:
            case OP_PREPARE:
                break;

            case OP_INDEX:
                mActiveIndexId = decodeLongLE(entry, 0);
                activeIndex = null;
                break;

            case OP_UNCREATE:
                while ((activeIndex = findIndex(activeIndex)) != null) {
                    try {
                        activeIndex.delete(Transaction.BOGUS, mActiveKey);
                        break;
                    } catch (ClosedIndexException e) {
                        activeIndex = null;
                    }
                }
                break;

            case OP_UNINSERT:
                while ((activeIndex = findIndex(activeIndex)) != null) {
                    try {
                        activeIndex.delete(Transaction.BOGUS, entry);
                        break;
                    } catch (ClosedIndexException e) {
                        activeIndex = null;
                    }
                }
                break;

            case OP_UNUPDATE:
            case OP_UNDELETE: {
                if ((activeIndex = findIndex(activeIndex)) != null) {
                    byte[][] pair = decodeNodeKeyValuePair(entry);

                    do {
                        try {
                            activeIndex.store(Transaction.BOGUS, pair[0], pair[1]);
                            break;
                        } catch (ClosedIndexException e) {
                            activeIndex = findIndex(null);
                        }
                    } while (activeIndex != null);
                }
                break;
            }

            case OP_UNUPDATE_LK:
            case OP_UNDELETE_LK:
                if ((activeIndex = findIndex(activeIndex)) != null) {
                    byte[] key = new byte[Utils.decodeUnsignedVarInt(entry, 0)];
                    int keyLoc = Utils.calcUnsignedVarIntLength(key.length);
                    arraycopy(entry, keyLoc, key, 0, key.length);

                    int valueLoc = keyLoc + key.length;
                    byte[] value = new byte[entry.length - valueLoc];
                    arraycopy(entry, valueLoc, value, 0, value.length);

                    do {
                        try {
                            activeIndex.store(Transaction.BOGUS, key, value);
                            break;
                        } catch (ClosedIndexException e) {
                            activeIndex = findIndex(null);
                        }
                    } while (activeIndex != null);
                }
                break;

            case OP_UNDELETE_FRAGMENTED:
                while (true) {
                    try {
                        activeIndex = findIndex(activeIndex);
                        mDatabase.fragmentedTrash().remove(mTxnId, (Tree) activeIndex, entry);
                        break;
                    } catch (ClosedIndexException e) {
                        activeIndex = null;
                    }
                }
                break;

            case OP_UNDELETE_LK_FRAGMENTED:
                if ((activeIndex = findIndex(activeIndex)) != null) {
                    byte[] key = new byte[Utils.decodeUnsignedVarInt(entry, 0)];
                    int keyLoc = Utils.calcUnsignedVarIntLength(key.length);
                    arraycopy(entry, keyLoc, key, 0, key.length);

                    int tidLoc = keyLoc + key.length;
                    int tidLen = entry.length - tidLoc;
                    byte[] trashKey = new byte[8 + tidLen];
                    encodeLongBE(trashKey, 0, mTxnId);
                    arraycopy(entry, tidLoc, trashKey, 8, tidLen);

                    do {
                        try {
                            activeIndex = findIndex(activeIndex);
                            mDatabase.fragmentedTrash().remove((Tree) activeIndex, key, trashKey);
                            break;
                        } catch (ClosedIndexException e) {
                            activeIndex = findIndex(null);
                        }
                    } while (activeIndex != null);
                }
                break;

            case OP_CUSTOM:
                LocalDatabase db = mDatabase;
                TransactionHandler handler = db.getCustomTxnHandler();
                if (handler == null) {
                    throw new DatabaseException("Custom transaction handler is not installed");
                }
                handler.undo(db, entry);
                break;

            case OP_ACTIVE_KEY:
                mActiveKey = entry;
                break;

            case OP_UNEXTEND:
                long length = Utils.decodeUnsignedVarLong(entry, new IntegerRef.Value());
                while ((activeIndex = findIndex(activeIndex)) != null) {
                    try (Cursor c = activeIndex.newAccessor(Transaction.BOGUS, mActiveKey)) {
                        c.valueLength(length);
                        break;
                    } catch (ClosedIndexException e) {
                        activeIndex = null;
                    }
                }
                break;

            case OP_UNALLOC:
                IntegerRef offsetRef = new IntegerRef.Value();
                length = Utils.decodeUnsignedVarLong(entry, offsetRef);
                long pos = Utils.decodeUnsignedVarLong(entry, offsetRef);
                while ((activeIndex = findIndex(activeIndex)) != null) {
                    try (Cursor c = activeIndex.newAccessor(Transaction.BOGUS, mActiveKey)) {
                        c.valueClear(pos, length);
                        break;
                    } catch (ClosedIndexException e) {
                        activeIndex = null;
                    }
                }
                break;

            case OP_UNWRITE:
                offsetRef = new IntegerRef.Value();
                pos = Utils.decodeUnsignedVarLong(entry, offsetRef);
                int off = offsetRef.get();
                while ((activeIndex = findIndex(activeIndex)) != null) {
                    try (Cursor c = activeIndex.newAccessor(Transaction.BOGUS, mActiveKey)) {
                        c.valueWrite(pos, entry, off, entry.length - off);
                        break;
                    } catch (ClosedIndexException e) {
                        activeIndex = null;
                    }
                }
                break;
        }

        return activeIndex;
    }

    private byte[] decodeNodeKey(byte[] entry) throws IOException {
        byte[] key;
        long pentry = DirectPageOps.p_transfer(entry, false);
        try {
            key = Node.retrieveKeyAtLoc(this, pentry, 0);
        } finally {
            DirectPageOps.p_delete(pentry);
        }
        return key;
    }

    private byte[][] decodeNodeKeyValuePair(byte[] entry) throws IOException {
        byte[][] pair;
        long pentry = DirectPageOps.p_transfer(entry, false);
        try {
            pair = Node.retrieveKeyValueAtLoc(this, pentry, 0);
        } finally {
            DirectPageOps.p_delete(pentry);
        }
        return pair;
    }

    private Index findIndex(Index activeIndex) throws IOException {
        if (activeIndex == null || activeIndex.isClosed()) {
            activeIndex = mDatabase.anyIndexById(mActiveIndexId);
        }
        return activeIndex;
    }

    final byte peek(boolean delete) throws IOException {
        Node node = mNode;
        if (node == null) {
            return (mBuffer == null || mBufferPos >= mBuffer.length) ? 0 : mBuffer[mBufferPos];
        }

        node.acquireExclusive();
        while (true) {
            long page = node.getPage();
            if (mNodeTopPos < pageSize(page)) {
                byte op = DirectPageOps.p_byteGet(page, mNodeTopPos);
                node.releaseExclusive();
                return op;
            }
            if ((node = popNode(node, delete)) == null) {
                return 0;
            }
        }
    }

    private final byte[] pop(byte[] opRef, boolean delete) throws IOException {
        Node node = mNode;
        if (node == null) {
            byte[] buffer = mBuffer;
            if (buffer == null) {
                opRef[0] = 0;
                mLength = 0;
                return null;
            }
            int pos = mBufferPos;
            if (pos >= buffer.length) {
                opRef[0] = 0;
                mLength = 0;
                return null;
            }
            if ((opRef[0] = buffer[pos++]) < PAYLOAD_OP) {
                mBufferPos = pos;
                mLength -= 1;
                return Utils.EMPTY_BYTES;
            }
            int payloadLen = Utils.decodeUnsignedVarInt(buffer, pos);
            int varIntLen = Utils.calcUnsignedVarIntLength(payloadLen);
            pos += varIntLen;
            byte[] entry = new byte[payloadLen];
            arraycopy(buffer, pos, entry, 0, payloadLen);
            mBufferPos = pos += payloadLen;
            mLength -= 1 + varIntLen + payloadLen;
            return entry;
        }

        node.acquireExclusive();
        long page;
        while (true) {
            page = node.getPage();
            if (mNodeTopPos < pageSize(page)) {
                break;
            }
            if ((node = popNode(node, delete)) == null) {
                mLength = 0;
                return null;
            }
        }

        if ((opRef[0] = DirectPageOps.p_byteGet(page, mNodeTopPos++)) < PAYLOAD_OP) {
            mLength -= 1;
            if (mNodeTopPos >= pageSize(page)) {
                node = popNode(node, delete);
            }
            if (node != null) {
                node.releaseExclusive();
            }
            return Utils.EMPTY_BYTES;
        }

        int payloadLen;
        {
            payloadLen = DirectPageOps.p_uintGetVar(page, mNodeTopPos);
            int varIntLen = DirectPageOps.p_uintVarSize(payloadLen);
            mNodeTopPos += varIntLen;
            mLength -= 1 + varIntLen + payloadLen;
        }

        byte[] entry = new byte[payloadLen];
        int entryPos = 0;

        while (true) {
            int avail = Math.min(payloadLen, pageSize(page) - mNodeTopPos);
            DirectPageOps.p_copyToArray(page, mNodeTopPos, entry, entryPos, avail);
            payloadLen -= avail;
            mNodeTopPos += avail;

            if (mNodeTopPos >= pageSize(page)) {
                node = popNode(node, delete);
            }

            if (payloadLen <= 0) {
                if (node != null) {
                    node.releaseExclusive();
                }
                return entry;
            }

            if (node == null) {
                throw new CorruptDatabaseException("Remainder of undo log is missing");
            }

            page = node.getPage();

            if (mNodeTopPos == pageSize(page) - 1 &&
                    DirectPageOps.p_byteGet(page, mNodeTopPos) == OP_COMMIT_TRUNCATE) {
                node.releaseExclusive();
                return entry;
            }

            entryPos += avail;
        }
    }

    private Node popNode(Node parent, boolean delete) throws IOException {
        Node lowerNode = null;
        long lowerNodeId = DirectPageOps.p_longGetLE(parent.getPage(), I_LOWERNode_ID);
        if (lowerNodeId != 0) {
            lowerNode = mDatabase.nodeMapGetAndRemove(lowerNodeId);
            if (lowerNode != null) {
                lowerNode.makeUnevictable();
            } else {
                try {
                    lowerNode = readUndoLogNode(mDatabase, lowerNodeId);
                } catch (Throwable e) {
                    parent.releaseExclusive();
                    throw e;
                }
            }
        }

        parent.makeEvictable();

        if (delete) {
            LocalDatabase db = mDatabase;
            db.deleteNode(parent, false);
        } else {
            parent.releaseExclusive();
        }

        mNode = lowerNode;
        mNodeTopPos = lowerNode == null ? 0 : lowerNode.undoTop();

        return lowerNode;
    }

    private Node allocUnevictableNode(long lowerNodeId) throws IOException {
        Node node = mDatabase.allocDirtyNode(NodeContext.MODE_UNEVICTABLE);
        node.type(Node.TYPE_UNDO_LOG);
        DirectPageOps.p_longPutLE(node.getPage(), I_LOWERNode_ID, lowerNodeId);
        return node;
    }

    final byte[] writeToMaster(UndoLog master, byte[] workspace) throws IOException {
        if (mActiveKey != null) {
            doPush(OP_ACTIVE_KEY, mActiveKey);
            mActiveKey = null;
        }

        Node node = mNode;
        if (node == null) {
            byte[] buffer = mBuffer;
            if (buffer == null) {
                return workspace;
            }
            int pos = mBufferPos;
            int bsize = buffer.length - pos;
            if (bsize == 0) {
                return workspace;
            }
            // TODO: 如果下次UndoLog仍在缓冲区中，请考虑调用persistReady。
            final int psize = (8 + 8 + 2) + bsize;
            if (workspace == null || workspace.length < psize) {
                workspace = new byte[Math.max(INITIAL_BUFFER_SIZE, Utils.roundUpPower2(psize))];
            }
            writeHeaderToMaster(workspace);
            encodeShortLE(workspace, (8 + 8), bsize);
            arraycopy(buffer, pos, workspace, (8 + 8 + 2), bsize);
            master.doPush(OP_LOG_COPY, workspace, 0, psize);
        } else {
            if (workspace == null) {
                workspace = new byte[INITIAL_BUFFER_SIZE];
            }
            writeHeaderToMaster(workspace);
            encodeLongLE(workspace, (8 + 8), mLength);
            encodeLongLE(workspace, (8 + 8 + 8), node.getId());
            encodeShortLE(workspace, (8 + 8 + 8 + 8), mNodeTopPos);
            master.doPush(OP_LOG_REF, workspace, 0, (8 + 8 + 8 + 8 + 2), 1);
        }
        return workspace;
    }

    private void writeHeaderToMaster(byte[] workspace) {
        encodeLongLE(workspace, 0, mTxnId);
        encodeLongLE(workspace, 8, mActiveIndexId);
    }

    public static UndoLog recoverMasterUndoLog(LocalDatabase db, long nodeId) throws IOException {
        UndoLog log = new UndoLog(db, 0);
        // Length is not recoverable.
        log.mLength = Long.MAX_VALUE;
        log.mNode = readUndoLogNode(db, nodeId);
        log.mNodeTopPos = log.mNode.undoTop();
        log.mNode.releaseExclusive();
        return log;
    }

    public void recoverTransactions(EventListener debugListener, boolean trace,
                                    LHashTable.Obj<LocalTransaction> txns,
                                    LockMode lockMode, long timeoutNanos)
            throws IOException {
        byte[] opRef = new byte[1];
        byte[] entry;
        while ((entry = pop(opRef, true)) != null) {
            UndoLog log = recoverUndoLog(opRef[0], entry);

            if (debugListener != null) {
                debugListener.notify
                        (EventType.DEBUG,
                                "Recovered transaction undo log: " +
                                        "txnId=%1$d, length=%2$d, bufferPos=%3$d, " +
                                        "nodeId=%4$d, nodeTopPos=%5$d, activeIndexId=%6$s",
                                log.mTxnId, log.mLength, log.mBufferPos,
                                log.mNode == null ? 0 : log.mNode.getId(), log.mNodeTopPos, log.mActiveIndexId);
            }

            LocalTransaction txn = log.recoverTransaction
                    (debugListener, trace, lockMode, timeoutNanos);

            txn.recoveredUndoLog(recoverUndoLog(opRef[0], entry));
            txn.attach("recovery");

            txns.insert(log.mTxnId).value = txn;
        }
    }

    private final LocalTransaction recoverTransaction(EventListener debugListener, boolean trace,
                                                      LockMode lockMode, long timeoutNanos)
            throws IOException {
        byte[] opRef = new byte[1];
        Scope scope = new Scope();

        Deque<Scope> scopes = new ArrayDeque<>();
        scopes.addFirst(scope);

        boolean acquireLocks = true;
        int depth = 1;

        int hasState = LocalTransaction.HAS_TRASH;

        loop:
        while (mLength > 0) {
            byte[] entry = pop(opRef, false);
            if (entry == null) {
                break;
            }

            byte op = opRef[0];

            if (trace) {
                traceOp(debugListener, op, entry);
            }

            switch (op) {
                default:
                    throw new DatabaseException("Unknown undo log entry type: " + op);

                case OP_COMMIT:
                    acquireLocks = false;
                    break;

                case OP_COMMIT_TRUNCATE:
                    if (mNode != null) {
                        mNode.makeEvictable();
                        mNode = null;
                        mNodeTopPos = 0;
                    }
                    break loop;

                case OP_PREPARE:
                    hasState |= LocalTransaction.HAS_PREPARE;
                    break;

                case OP_SCOPE_ENTER:
                    depth++;
                    if (depth > scopes.size()) {
                        scope.mSavepoint = mLength;
                        scope = new Scope();
                        scopes.addFirst(scope);
                    }
                    break;

                case OP_SCOPE_COMMIT:
                    depth--;
                    break;

                case OP_INDEX:
                    mActiveIndexId = decodeLongLE(entry, 0);
                    break;

                case OP_UNINSERT:
                    if (lockMode != LockMode.UNSAFE) {
                        scope.addLock(mActiveIndexId, entry);
                    }
                    break;

                case OP_UNUPDATE:
                case OP_UNDELETE:
                case OP_UNDELETE_FRAGMENTED:
                    if (lockMode != LockMode.UNSAFE) {
                        byte[] key = decodeNodeKey(entry);

                        scope.addLock(mActiveIndexId, key).setGhostFrame(new GhostFrame());
                    }
                    break;

                case OP_UNUPDATE_LK:
                case OP_UNDELETE_LK:
                case OP_UNDELETE_LK_FRAGMENTED:
                    if (lockMode != LockMode.UNSAFE) {
                        byte[] key = new byte[Utils.decodeUnsignedVarInt(entry, 0)];
                        arraycopy(entry, Utils.calcUnsignedVarIntLength(key.length), key, 0, key.length);

                        scope.addLock(mActiveIndexId, key)
                                .setGhostFrame(new GhostFrame());
                    }
                    break;

                case OP_CUSTOM:
                    break;

                case OP_ACTIVE_KEY:
                    if (lockMode != LockMode.UNSAFE) {
                        mActiveKey = entry;
                    }
                    break;

                case OP_UNCREATE:
                case OP_UNEXTEND:
                case OP_UNALLOC:
                case OP_UNWRITE:
                    if (mActiveKey != null) {
                        scope.addLock(mActiveIndexId, mActiveKey);
                        mActiveKey = null;
                    }
                    break;
            }
        }

        LocalTransaction txn = new LocalTransaction
                (mDatabase, mTxnId, lockMode, timeoutNanos, hasState);

        scope = scopes.pollFirst();
        if (acquireLocks) {
            scope.acquireLocks(txn);
        }

        while ((scope = scopes.pollFirst()) != null) {
            txn.recoveredScope(scope.mSavepoint, LocalTransaction.HAS_TRASH);
            if (acquireLocks) {
                scope.acquireLocks(txn);
            }
        }

        return txn;
    }

    private void traceOp(EventListener debugListener, byte op, byte[] entry) throws IOException {
        String opStr;
        String payloadStr = null;

        switch (op) {
            default:
                opStr = "UNKNOWN";
                payloadStr = "op=" + (op & 0xff) + ", entry=0x" + Utils.toHex(entry);
                break;

            case OP_SCOPE_ENTER:
                opStr = "SCOPE_ENTER";
                break;

            case OP_SCOPE_COMMIT:
                opStr = "SCOPE_COMMIT";
                break;

            case OP_COMMIT:
                opStr = "COMMIT";
                break;

            case OP_COMMIT_TRUNCATE:
                opStr = "COMMIT_TRUNCATE";
                break;

            case OP_PREPARE:
                opStr = "PREPARE";
                break;

            case OP_UNCREATE:
                opStr = "UNCREATE";
                break;

            case OP_LOG_COPY:
                opStr = "LOG_COPY";
                break;

            case OP_LOG_REF:
                opStr = "LOG_REF";
                break;

            case OP_INDEX:
                opStr = "INDEX";
                payloadStr = "indexId=" + decodeLongLE(entry, 0);
                break;

            case OP_UNINSERT:
                opStr = "UNINSERT";
                payloadStr = "key=0x" + Utils.toHex(entry) + " (" +
                        new String(entry, StandardCharsets.UTF_8) + ')';
                break;

            case OP_UNUPDATE:
            case OP_UNDELETE:
                opStr = op == OP_UNUPDATE ? "UNUPDATE" : "UNDELETE";
                byte[][] pair = decodeNodeKeyValuePair(entry);
                payloadStr = "key=0x" + Utils.toHex(pair[0]) + " (" +
                        new String(pair[0], StandardCharsets.UTF_8) + ") value=0x" + Utils.toHex(pair[1]);
                break;

            case OP_UNDELETE_FRAGMENTED:
                opStr = "UNDELETE_FRAGMENTED";
                byte[] key = decodeNodeKey(entry);
                payloadStr = "key=0x" + Utils.toHex(key) + " (" +
                        new String(key, StandardCharsets.UTF_8) + ')';
                break;

            case OP_ACTIVE_KEY:
                opStr = "ACTIVE_KEY";
                payloadStr = "key=0x" + Utils.toHex(entry) + " (" +
                        new String(entry, StandardCharsets.UTF_8) + ')';
                break;

            case OP_CUSTOM:
                opStr = "CUSTOM";
                payloadStr = "entry=0x" + Utils.toHex(entry);
                break;

            case OP_UNUPDATE_LK:
            case OP_UNDELETE_LK:
                opStr = op == OP_UNUPDATE ? "UNUPDATE_LK" : "UNDELETE_LK";

                int keyLen = Utils.decodeUnsignedVarInt(entry, 0);
                int keyLoc = Utils.calcUnsignedVarIntLength(keyLen);
                int valueLoc = keyLoc + keyLen;
                int valueLen = entry.length - valueLoc;

                payloadStr = "key=0x" + Utils.toHex(entry, keyLoc, keyLen) + " (" +
                        new String(entry, keyLoc, keyLen, StandardCharsets.UTF_8) + ") value=0x" +
                        Utils.toHex(entry, valueLoc, valueLen);

                break;

            case OP_UNDELETE_LK_FRAGMENTED:
                opStr = "UNDELETE_LK_FRAGMENTED";

                keyLen = Utils.decodeUnsignedVarInt(entry, 0);
                keyLoc = Utils.calcUnsignedVarIntLength(keyLen);

                payloadStr = "key=0x" + Utils.toHex(entry, keyLoc, keyLen) + " (" +
                        new String(entry, keyLoc, keyLen, StandardCharsets.UTF_8) + ')';

                break;

            case OP_UNEXTEND:
                opStr = "UNEXTEND";
                payloadStr = "length=" + Utils.decodeUnsignedVarLong(entry, new IntegerRef.Value());
                break;

            case OP_UNALLOC:
                opStr = "UNALLOC";
                IntegerRef offsetRef = new IntegerRef.Value();
                long length = Utils.decodeUnsignedVarLong(entry, offsetRef);
                long pos = Utils.decodeUnsignedVarLong(entry, offsetRef);
                payloadStr = "pos=" + pos + ", length=" + length;
                break;

            case OP_UNWRITE:
                opStr = "UNWRITE";
                offsetRef = new IntegerRef.Value();
                pos = Utils.decodeUnsignedVarLong(entry, offsetRef);
                int off = offsetRef.get();
                payloadStr = "pos=" + pos + ", value=0x" + Utils.toHex(entry, off, entry.length - off);
                break;
        }

        if (payloadStr == null) {
            debugListener.notify(EventType.DEBUG, "Undo recover %1$s", opStr);
        } else {
            debugListener.notify(EventType.DEBUG, "Undo recover %1$s %2$s", opStr, payloadStr);
        }
    }

    static class Scope {
        long mSavepoint;

        Lock mTopLock;

        Scope() {
        }

        public Lock addLock(long indexId, byte[] key) {
            Lock lock = new Lock();
            lock.setIndexId(indexId);
            lock.setKey(key);
            lock.setHashCode(LockManager.hash(indexId, key));
            lock.setLockManagerNext(mTopLock);
            mTopLock = lock;
            return lock;
        }

        void acquireLocks(LocalTransaction txn) throws LockFailureException {
            Lock lock = mTopLock;
            if (lock != null) while (true) {
                Lock next = lock.getLockManagerNext();
                txn.lockExclusive(lock);
                if (next == null) {
                    break;
                }
                mTopLock = lock = next;
            }
        }
    }

    private UndoLog recoverUndoLog(byte masterLogOp, byte[] masterLogEntry)
            throws IOException {
        if (masterLogOp != OP_LOG_COPY && masterLogOp != OP_LOG_REF) {
            throw new DatabaseException("Unknown undo log entry type: " + masterLogOp);
        }

        long txnId = decodeLongLE(masterLogEntry, 0);
        UndoLog log = new UndoLog(mDatabase, txnId);
        log.mActiveIndexId = decodeLongLE(masterLogEntry, 8);

        if (masterLogOp == OP_LOG_COPY) {
            int bsize = decodeUnsignedShortLE(masterLogEntry, (8 + 8));
            log.mLength = bsize;
            byte[] buffer = new byte[bsize];
            arraycopy(masterLogEntry, (8 + 8 + 2), buffer, 0, bsize);
            log.mBuffer = buffer;
            log.mBufferPos = 0;
        } else {
            log.mLength = decodeLongLE(masterLogEntry, (8 + 8));
            long nodeId = decodeLongLE(masterLogEntry, (8 + 8 + 8));
            int topEntry = decodeUnsignedShortLE(masterLogEntry, (8 + 8 + 8 + 8));
            log.mNode = readUndoLogNode(mDatabase, nodeId);
            log.mNodeTopPos = topEntry;

            if (log.mNode.undoTop() == pageSize(log.mNode.getPage()) - 1 &&
                    DirectPageOps.p_byteGet(log.mNode.getPage(), log.mNode.undoTop()) == OP_COMMIT_TRUNCATE) {
                log.mNodeTopPos = log.mNode.undoTop();
            }
            log.mNode.releaseExclusive();
        }
        return log;
    }

    private static Node readUndoLogNode(LocalDatabase db, long nodeId) throws IOException {
        Node node = db.allocLatchedNode(nodeId, NodeContext.MODE_UNEVICTABLE);
        try {
            node.read(db, nodeId);
            if (node.type() != Node.TYPE_UNDO_LOG) {
                throw new CorruptDatabaseException
                        ("Not an undo log node type: " + node.type() + ", id: " + nodeId);
            }
            return node;
        } catch (Throwable e) {
            node.makeEvictableNow();
            node.releaseExclusive();
            throw e;
        }
    }
}
