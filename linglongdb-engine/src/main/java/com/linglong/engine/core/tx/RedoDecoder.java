package com.linglong.engine.core.tx;


import com.linglong.base.concurrent.Latch;
import com.linglong.base.exception.DatabaseException;

import java.io.EOFException;
import java.io.IOException;


/**
 * @author Stereo
 * @See ReplRedoDecoder 用于各个节点数据复制流程如下
 * 1.主节点将数据广播到各个节点的Log日志中
 * 2.各个节点会将数据进行解码
 * 3.将解码后的数据根据操作类型进行处理doRun()
 */
public abstract class RedoDecoder {
    private final boolean mLenient;
    final DataIn mIn;

    public DataIn getIn() {
        return mIn;
    }

    long mTxnId;

    final Latch mDecodeLatch;

    long mDecodePosition;

    public long getDecodePosition() {
        return mDecodePosition;
    }

    long mDecodeTransactionId;

    public long getDecodeTransactionId() {
        return mDecodeTransactionId;
    }

    public RedoDecoder(boolean lenient, long initialTxnId, DataIn in, Latch decodeLatch) {
        mLenient = lenient;
        mIn = in;

        mTxnId = initialTxnId;

        mDecodeLatch = decodeLatch;

        mDecodePosition = in.mPos;
        mDecodeTransactionId = initialTxnId;
    }

    public boolean run(RedoVisitor visitor) throws IOException {
        mDecodeLatch.acquireExclusive();
        try {
            return doRun(visitor, mIn);
        } finally {
            mDecodeLatch.releaseExclusive();
        }
    }

    @SuppressWarnings("fallthrough")
    private boolean doRun(RedoVisitor visitor, DataIn in) throws IOException {
        while (true) {
            mDecodePosition = in.mPos;
            mDecodeTransactionId = mTxnId;

            mDecodeLatch.releaseExclusive();

            int op;
            try {
                op = in.read();
            } finally {
                mDecodeLatch.acquireExclusive();
            }

            if (op < 0) {
                return true;
            }
            //****** 跟踪BUG后续删除 ******
            //System.out.println("Decoder 读取OP ---------> " + op);
            switch (op &= 0xff) {
                case 0:
                    if (mLenient) {
                        return true;
                    }

                default:
                    throw new DatabaseException
                            ("Unknown redo log operation: " + op + " at " + (in.mPos - 1));

                case RedoOps.OP_RESET:
                    mTxnId = 0;
                    if (!verifyTerminator(in)) {
                        return false;
                    }
                    if (!visitor.reset()) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TIMESTAMP:
                    long ts;
                    try {
                        ts = in.readLongLE();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.timestamp(ts)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_SHUTDOWN:
                    try {
                        ts = in.readLongLE();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.shutdown(ts)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_CLOSE:
                    try {
                        ts = in.readLongLE();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.close(ts)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_END_FILE:
                    try {
                        ts = in.readLongLE();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.endFile(ts)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_NOP_RANDOM:
                    try {
                        in.readLongLE();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_ID_RESET:
                    long txnId;
                    try {
                        txnId = in.readLongLE();
                    } catch (EOFException e) {
                        return true;
                    }
                    mTxnId = txnId;
                    if (!verifyTerminator(in)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_CONTROL:
                    byte[] message;
                    try {
                        message = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.control(message)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_PREPARE:
                    try {
                        txnId = readTxnId(in);
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnPrepare(txnId)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_ENTER:
                    try {
                        txnId = readTxnId(in);
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnEnter(txnId)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_ROLLBACK:
                    try {
                        txnId = readTxnId(in);
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnRollback(txnId)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_ROLLBACK_FINAL:
                    try {
                        txnId = readTxnId(in);
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnRollbackFinal(txnId)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_COMMIT:
                    try {
                        txnId = readTxnId(in);
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnCommit(txnId)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_COMMIT_FINAL:
                    try {
                        txnId = readTxnId(in);
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnCommitFinal(txnId)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_STORE:
                    long indexId;
                    byte[] key, value;
                    try {
                        indexId = in.readLongLE();
                        key = in.readBytes();
                        value = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.store(indexId, key, value)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_STORE_NO_LOCK:
                    try {
                        indexId = in.readLongLE();
                        key = in.readBytes();
                        value = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.storeNoLock(indexId, key, value)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_DELETE:
                    try {
                        indexId = in.readLongLE();
                        key = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.store(indexId, key, null)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_DELETE_NO_LOCK:
                    try {
                        indexId = in.readLongLE();
                        key = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.storeNoLock(indexId, key, null)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_RENAME_INDEX:
                    byte[] newName;
                    try {
                        txnId = readTxnId(in);
                        indexId = in.readLongLE();
                        newName = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.renameIndex(txnId, indexId, newName)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_DELETE_INDEX:
                    try {
                        txnId = readTxnId(in);
                        indexId = in.readLongLE();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.deleteIndex(txnId, indexId)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_ENTER_STORE:
                    try {
                        txnId = readTxnId(in);
                        indexId = in.readLongLE();
                        key = in.readBytes();
                        value = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnEnterStore(txnId, indexId, key, value)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_STORE:
                    try {
                        txnId = readTxnId(in);
                        indexId = in.readLongLE();
                        key = in.readBytes();
                        value = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnStore(txnId, indexId, key, value)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_STORE_COMMIT:
                    try {
                        txnId = readTxnId(in);
                        indexId = in.readLongLE();
                        key = in.readBytes();
                        value = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnStoreCommit(txnId, indexId, key, value)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_STORE_COMMIT_FINAL:
                    try {
                        txnId = readTxnId(in);
                        indexId = in.readLongLE();
                        key = in.readBytes();
                        value = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in)
                            || !visitor.txnStoreCommitFinal(txnId, indexId, key, value)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_ENTER_DELETE:
                    try {
                        txnId = readTxnId(in);
                        indexId = in.readLongLE();
                        key = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnEnterStore(txnId, indexId, key, null)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_DELETE:
                    try {
                        txnId = readTxnId(in);
                        indexId = in.readLongLE();
                        key = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnStore(txnId, indexId, key, null)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_DELETE_COMMIT:
                    try {
                        txnId = readTxnId(in);
                        indexId = in.readLongLE();
                        key = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnStoreCommit(txnId, indexId, key, null)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_DELETE_COMMIT_FINAL:
                    try {
                        txnId = readTxnId(in);
                        indexId = in.readLongLE();
                        key = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in)
                            || !visitor.txnStoreCommitFinal(txnId, indexId, key, null)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_CURSOR_REGISTER:
                    long cursorId;
                    try {
                        cursorId = readTxnId(in);
                        indexId = in.readLongLE();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.cursorRegister(cursorId, indexId)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_CURSOR_UNREGISTER:
                    try {
                        cursorId = readTxnId(in);
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.cursorUnregister(cursorId)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_CURSOR_STORE:
                    try {
                        cursorId = readTxnId(in);
                        txnId = readTxnId(in);
                        key = in.readBytes();
                        value = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in)
                            || !visitor.cursorStore(cursorId, txnId, key, value)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_CURSOR_DELETE:
                    try {
                        cursorId = readTxnId(in);
                        txnId = readTxnId(in);
                        key = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.cursorStore(cursorId, txnId, key, null)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_CURSOR_FIND:
                    try {
                        cursorId = readTxnId(in);
                        txnId = readTxnId(in);
                        key = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.cursorFind(cursorId, txnId, key)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_CURSOR_VALUE_SET_LENGTH:
                    long length;
                    try {
                        cursorId = readTxnId(in);
                        txnId = readTxnId(in);
                        length = in.readUnsignedVarLong();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in)
                            || !visitor.cursorValueSetLength(cursorId, txnId, length)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_CURSOR_VALUE_WRITE:
                    try {
                        cursorId = readTxnId(in);
                        txnId = readTxnId(in);
                        long pos = in.readUnsignedVarLong();
                        int amount = in.readUnsignedVarInt();
                        in.cursorValueWrite(visitor, cursorId, txnId, pos, amount);
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_CURSOR_VALUE_CLEAR:
                    long pos;
                    try {
                        cursorId = readTxnId(in);
                        txnId = readTxnId(in);
                        pos = in.readUnsignedVarLong();
                        length = in.readUnsignedVarLong();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in)
                            || !visitor.cursorValueClear(cursorId, txnId, pos, length)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_LOCK_SHARED:
                    try {
                        txnId = readTxnId(in);
                        indexId = in.readLongLE();
                        key = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnLockShared(txnId, indexId, key)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_LOCK_UPGRADABLE:
                    try {
                        txnId = readTxnId(in);
                        indexId = in.readLongLE();
                        key = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnLockUpgradable(txnId, indexId, key)) {
                        return false;
                    }
                    break;

                case RedoOps.OP_TXN_LOCK_EXCLUSIVE:
                    try {
                        txnId = readTxnId(in);
                        indexId = in.readLongLE();
                        key = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnLockExclusive(txnId, indexId, key)) {
                        return false;
                    }
                    break;

                case (RedoOps.OP_TXN_CUSTOM & 0xff):
                    try {
                        txnId = readTxnId(in);
                        message = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in) || !visitor.txnCustom(txnId, message)) {
                        return false;
                    }
                    break;

                case (RedoOps.OP_TXN_CUSTOM_LOCK & 0xff):
                    try {
                        txnId = readTxnId(in);
                        indexId = in.readLongLE();
                        key = in.readBytes();
                        message = in.readBytes();
                    } catch (EOFException e) {
                        return true;
                    }
                    if (!verifyTerminator(in)
                            || !visitor.txnCustomLock(txnId, message, indexId, key)) {
                        return false;
                    }
                    break;
            }
        }
    }

    private long readTxnId(DataIn in) throws IOException {
        return mTxnId += in.readSignedVarLong();
    }

    public abstract boolean verifyTerminator(DataIn in) throws IOException;
}
