package com.glodon.linglong.engine.core;

import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.base.exception.LargeValueException;
import com.glodon.linglong.engine.core.frame.CursorFrame;
import com.glodon.linglong.engine.core.page.DirectPageOps;
import com.glodon.linglong.engine.core.tx.LocalTransaction;
import com.glodon.linglong.engine.core.tx.UndoLog;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * @author Stereo
 */
final public class TreeValue {
    public static final int OP_LENGTH = 0, OP_READ = 1, OP_CLEAR = 2, OP_SET_LENGTH = 3, OP_WRITE = 4;

    public static final byte[] TOUCH_VALUE = new byte[0];

    private TreeValue() {
    }

    static int compactCheck(final CursorFrame frame, long pos, final long highestNodeId)
            throws IOException {
        final Node node = frame.getNode();

        int nodePos = frame.getNodePos();
        if (nodePos < 0) {
            return -1;
        }

        final long page = node.mPage;
        int loc = DirectPageOps.p_ushortGetLE(page, node.searchVecStart() + nodePos);
        loc += Node.keyLengthAtLoc(page, loc);

        int vHeader = DirectPageOps.p_byteGet(page, loc++);
        if (vHeader >= 0) {
            return pos >= vHeader ? -1 : 0;
        }

        int len;
        if ((vHeader & 0x20) == 0) {
            len = 1 + (((vHeader & 0x1f) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
        } else if (vHeader != -1) {
            len = 1 + (((vHeader & 0x0f) << 16)
                    | (DirectPageOps.p_ubyteGet(page, loc++) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
        } else {
            return -1;
        }

        if ((vHeader & Node.ENTRY_FRAGMENTED) == 0) {
            return pos >= len ? -1 : 0;
        }

        final int fHeader = DirectPageOps.p_byteGet(page, loc++);
        final long fLen = LocalDatabase.decodeFullFragmentedValueLength(fHeader, page, loc);

        if (pos >= fLen) {
            return -1;
        }

        loc = skipFragmentedLengthField(loc, fHeader);

        if ((fHeader & 0x02) != 0) {
            final int fInlineLen = DirectPageOps.p_ushortGetLE(page, loc);
            if (pos < fInlineLen) {
                return 0;
            }
            pos -= fInlineLen;
            loc = loc + 2 + fInlineLen;
        }

        LocalDatabase db = node.getDatabase();

        if ((fHeader & 0x01) == 0) {
            loc += (((int) pos) / pageSize(db, page)) * 6;
            final long fNodeId = DirectPageOps.p_uint48GetLE(page, loc);
            return fNodeId > highestNodeId ? 1 : 0;
        }

        final long inodeId = DirectPageOps.p_uint48GetLE(page, loc);
        if (inodeId == 0) {
            return 0;
        }

        Node inode = db.nodeMapLoadFragment(inodeId);
        int level = db.calculateInodeLevels(fLen);

        while (true) {
            level--;
            long levelCap = db.levelCap(level);
            long childNodeId = DirectPageOps.p_uint48GetLE(inode.mPage, ((int) (pos / levelCap)) * 6);
            inode.releaseShared();
            if (childNodeId > highestNodeId) {
                return 1;
            }
            if (level <= 0 || childNodeId == 0) {
                return 0;
            }
            inode = db.nodeMapLoadFragment(childNodeId);
            pos %= levelCap;
        }
    }

    @SuppressWarnings("fallthrough")
    static long action(LocalTransaction txn, TreeCursor cursor, CursorFrame frame,
                       int op, long pos, byte[] b, int bOff, long bLen)
            throws IOException {
        while (true) {
            Node node = frame.getNode();

            int nodePos = frame.getNodePos();
            if (nodePos < 0) {

                if (op <= OP_CLEAR) {
                    return -1;
                }

                if (b == TOUCH_VALUE) {
                    return 0;
                }

                if (txn != null) {
                    txn.pushUncreate(cursor.mTree.mId, cursor.mKey);
                    txn = null;
                }

                node = cursor.insertBlank(frame, node, pos + bLen);

                if (bLen <= 0) {
                    return 0;
                }

                nodePos = frame.getNodePos();

                if (nodePos < 0) {
                    return 0;
                }
            }

            long page = node.mPage;
            int loc = DirectPageOps.p_ushortGetLE(page, node.searchVecStart() + nodePos);

            final int kHeaderLoc = loc;

            loc += Node.keyLengthAtLoc(page, loc);

            final int vHeaderLoc = loc; // location of raw value header
            final int vLen;             // length of raw value sans header

            nonFrag:
            {
                final int vHeader = DirectPageOps.p_byteGet(page, loc++); // header byte of raw value

                decodeLen:
                if (vHeader >= 0) {
                    vLen = vHeader;
                } else {
                    if ((vHeader & 0x20) == 0) {
                        vLen = 1 + (((vHeader & 0x1f) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
                    } else if (vHeader != -1) {
                        vLen = 1 + (((vHeader & 0x0f) << 16)
                                | (DirectPageOps.p_ubyteGet(page, loc++) << 8) | DirectPageOps.p_ubyteGet(page, loc++));
                    } else {
                        if (op <= OP_CLEAR) {
                            return -1;
                        }

                        if (b == TOUCH_VALUE) {
                            return 0;
                        }

                        DirectPageOps.p_bytePut(page, vHeaderLoc, 0);
                        vLen = 0;
                        break decodeLen;
                    }

                    if ((vHeader & Node.ENTRY_FRAGMENTED) != 0) {
                        break nonFrag;
                    }
                }

                switch (op) {
                    case OP_LENGTH:
                    default:
                        return vLen;

                    case OP_READ:
                        if (bLen <= 0 || pos >= vLen) {
                            bLen = 0;
                        } else {
                            bLen = Math.min((int) (vLen - pos), bLen);
                            DirectPageOps.p_copyToArray(page, (int) (loc + pos), b, bOff, (int) bLen);
                        }
                        return bLen;

                    case OP_CLEAR:
                        if (pos < vLen) {
                            int iLoc = (int) (loc + pos);
                            int iLen = (int) Math.min(bLen, vLen - pos);
                            if (txn != null) {
                                txn.pushUnwrite(cursor.mTree.mId, cursor.mKey, pos, page, iLoc, iLen);
                            }
                            DirectPageOps.p_clear(page, iLoc, iLoc + iLen);
                        }
                        return 0;

                    case OP_SET_LENGTH:
                        if (pos <= vLen) {
                            if (pos == vLen) {
                                return 0;
                            }

                            // Truncate non-fragmented value.

                            int newLen = (int) pos;
                            int oldLen = (int) vLen;
                            int garbageAccum = oldLen - newLen;

                            if (txn != null) {
                                txn.pushUnwrite(cursor.mTree.mId, cursor.mKey,
                                        pos, page, loc + newLen, garbageAccum);
                            }

                            shift:
                            {
                                final int vShift;

                                if (newLen <= 127) {
                                    DirectPageOps.p_bytePut(page, vHeaderLoc, newLen);
                                    vShift = loc - (vHeaderLoc + 1);
                                } else if (newLen <= 8192) {
                                    DirectPageOps.p_bytePut(page, vHeaderLoc, 0x80 | ((newLen - 1) >> 8));
                                    DirectPageOps.p_bytePut(page, vHeaderLoc + 1, newLen - 1);
                                    vShift = loc - (vHeaderLoc + 2);
                                } else {
                                    DirectPageOps.p_bytePut(page, vHeaderLoc, 0xa0 | ((newLen - 1) >> 16));
                                    DirectPageOps.p_bytePut(page, vHeaderLoc + 1, (newLen - 1) >> 8);
                                    DirectPageOps.p_bytePut(page, vHeaderLoc + 2, newLen - 1);
                                    break shift;
                                }

                                if (vShift > 0) {
                                    garbageAccum += vShift;
                                    DirectPageOps.p_copy(page, loc, page, loc - vShift, newLen);
                                }
                            }

                            node.garbage(node.garbage() + garbageAccum);
                            return 0;
                        }

                        break;

                    case OP_WRITE:
                        if (b == TOUCH_VALUE) {
                            return 0;
                        }

                        if (pos < vLen) {
                            final long end = pos + bLen;
                            if (end <= vLen) {
                                int iLoc = (int) (loc + pos);
                                int iLen = (int) bLen;
                                if (txn != null) {
                                    txn.pushUnwrite(cursor.mTree.mId, cursor.mKey,
                                            pos, page, iLoc, iLen);
                                }
                                DirectPageOps.p_copyFromArray(b, bOff, page, iLoc, iLen);
                                return 0;
                            } else if (pos == 0 && bOff == 0 && bLen == b.length) {
                                try {
                                    Tree tree = cursor.mTree;
                                    if (txn != null) {
                                        txn.pushUndoStore(tree.mId, UndoLog.OP_UNUPDATE,
                                                page, kHeaderLoc, loc + vLen - kHeaderLoc);
                                    }
                                    node.updateLeafValue(frame, tree, nodePos, 0, b);
                                } catch (Throwable e) {
                                    node.releaseExclusive();
                                    throw e;
                                }
                                if (node.mSplit != null) {
                                    node = cursor.mTree.finishSplit(frame, node);
                                }
                                return 0;
                            } else {
                                int iLoc = (int) (loc + pos);
                                int iLen = (int) (vLen - pos);
                                if (txn != null) {
                                    txn.pushUnwrite(cursor.mTree.mId, cursor.mKey,
                                            pos, page, iLoc, iLen);
                                }
                                DirectPageOps.p_copyFromArray(b, bOff, page, iLoc, iLen);
                                pos = vLen;
                                bOff += iLen;
                                bLen -= iLen;
                            }
                        }

                        break;
                }

                if (txn != null) {
                    txn.pushUnextend(cursor.mTree.mId, cursor.mKey, vLen);
                    txn = null;
                }

                byte[] oldValue = new byte[vLen];
                DirectPageOps.p_copyToArray(page, loc, oldValue, 0, oldValue.length);

                node.deleteLeafEntry(nodePos);
                node.postDelete(nodePos, cursor.mKey);

                cursor.insertBlank(frame, node, pos + bLen);

                op = OP_WRITE;

                if (bLen <= 0) {
                    pos = 0;
                    b = oldValue;
                    bOff = 0;
                    bLen = oldValue.length;
                } else {
                    action(null, cursor, frame, OP_WRITE, 0, oldValue, 0, oldValue.length);
                }

                continue;
            }

            int fHeaderLoc; // location of fragmented value header
            int fHeader;    // header byte of fragmented value
            long fLen;      // length of fragmented value (when fully reconstructed)

            fHeaderLoc = loc;
            fHeader = DirectPageOps.p_byteGet(page, loc++);

            switch ((fHeader >> 2) & 0x03) {
                default:
                    fLen = DirectPageOps.p_ushortGetLE(page, loc);
                    break;
                case 1:
                    fLen = DirectPageOps.p_intGetLE(page, loc) & 0xffffffffL;
                    break;
                case 2:
                    fLen = DirectPageOps.p_uint48GetLE(page, loc);
                    break;
                case 3:
                    fLen = DirectPageOps.p_longGetLE(page, loc);
                    if (fLen < 0) {
                        node.release(op > OP_READ);
                        throw new LargeValueException(fLen);
                    }
                    break;
            }

            loc = skipFragmentedLengthField(loc, fHeader);

            switch (op) {
                case OP_LENGTH:
                default:
                    return fLen;

                case OP_READ:
                    try {
                        if (bLen <= 0 || pos >= fLen) {
                            return 0;
                        }

                        bLen = (int) Math.min(fLen - pos, bLen);
                        final int total = (int) bLen;

                        if ((fHeader & 0x02) != 0) {
                            final int fInlineLen = DirectPageOps.p_ushortGetLE(page, loc);
                            loc += 2;
                            final int amt = (int) (fInlineLen - pos);
                            if (amt <= 0) {
                                pos -= fInlineLen;
                            } else if (bLen <= amt) {
                                DirectPageOps.p_copyToArray(page, (int) (loc + pos), b, bOff, (int) bLen);
                                return bLen;
                            } else {
                                DirectPageOps.p_copyToArray(page, (int) (loc + pos), b, bOff, amt);
                                bLen -= amt;
                                bOff += amt;
                                pos = 0;
                            }
                            loc += fInlineLen;
                        }

                        final LocalDatabase db = node.getDatabase();

                        if ((fHeader & 0x01) == 0) {
                            final int ipos = (int) pos;
                            final int pageSize = pageSize(db, page);
                            loc += (ipos / pageSize) * 6;
                            int fNodeOff = ipos % pageSize;
                            while (true) {
                                final int amt = Math.min((int) bLen, pageSize - fNodeOff);
                                final long fNodeId = DirectPageOps.p_uint48GetLE(page, loc);
                                if (fNodeId == 0) {
                                    Arrays.fill(b, bOff, bOff + amt, (byte) 0);
                                } else {
                                    final Node fNode = db.nodeMapLoadFragment(fNodeId);
                                    DirectPageOps.p_copyToArray(fNode.mPage, fNodeOff, b, bOff, amt);
                                    fNode.releaseShared();
                                }
                                bLen -= amt;
                                if (bLen <= 0) {
                                    return total;
                                }
                                bOff += amt;
                                loc += 6;
                                fNodeOff = 0;
                            }
                        }

                        final long inodeId = DirectPageOps.p_uint48GetLE(page, loc);
                        if (inodeId == 0) {
                            Arrays.fill(b, bOff, bOff + (int) bLen, (byte) 0);
                        } else {
                            final int levels = db.calculateInodeLevels(fLen);
                            final Node inode = db.nodeMapLoadFragment(inodeId);
                            readMultilevelFragments(pos, levels, inode, b, bOff, (int) bLen);
                        }

                        return total;
                    } catch (Throwable e) {
                        node.releaseShared();
                        throw e;
                    }

                case OP_CLEAR:
                case OP_SET_LENGTH:
                    clearOrTruncate:
                    {
                        if (op == OP_CLEAR) {
                            bLen = Math.min(bLen, fLen - pos);
                            if (bLen <= 0) {
                                return 0;
                            }
                        } else {
                            if (pos >= fLen) {
                                break clearOrTruncate;
                            }
                            bLen = fLen - pos;
                        }

                        final long finalLength = pos;
                        int fInlineLoc = loc;
                        int fInlineLen = 0;

                        if ((fHeader & 0x02) != 0) {
                            fInlineLen = DirectPageOps.p_ushortGetLE(page, loc);
                            fInlineLoc += 2;
                            loc += 2;

                            final long amt = fInlineLen - pos;
                            if (amt > 0) {
                                int iLoc = (int) (loc + pos);
                                if (bLen <= amt) {
                                    int iLen = (int) bLen;
                                    if (txn != null) {
                                        txn.pushUnwrite(cursor.mTree.mId, cursor.mKey,
                                                pos, page, iLoc, iLen);
                                    }
                                    DirectPageOps.p_clear(page, iLoc, iLoc + iLen);
                                    if (op == OP_SET_LENGTH) {
                                        fHeaderLoc = truncateFragmented
                                                (node, page, vHeaderLoc, vLen, iLen);
                                        updateLengthField(page, fHeaderLoc, finalLength);
                                    }
                                    return 0;
                                }
                                int iLen = (int) amt;
                                if (txn != null) {
                                    txn.pushUnwrite(cursor.mTree.mId, cursor.mKey, pos, page, iLoc, iLen);
                                }
                                DirectPageOps.p_clear(page, iLoc, iLoc + iLen);
                                bLen -= amt;
                                pos = fInlineLen;
                            }

                            fLen -= fInlineLen;
                            loc += fInlineLen;
                        }

                        final boolean toEnd = (pos - fInlineLen + bLen) >= fLen;

                        if ((fHeader & 0x01) != 0) try {
                            long inodeId = DirectPageOps.p_uint48GetLE(page, loc);

                            if (inodeId == 0) {
                                if (op == OP_CLEAR) {
                                    return 0;
                                }
                            } else clearNodes:{
                                final LocalDatabase db = node.getDatabase();

                                Node inode = db.nodeMapLoadFragmentExclusive(inodeId, true);
                                try {
                                    if (db.markFragmentDirty(inode)) {
                                        DirectPageOps.p_int48PutLE(page, loc, inode.mId);
                                    }

                                    int levels = db.calculateInodeLevels(fLen - fInlineLen);

                                    clearMultilevelFragments
                                            (txn, cursor, pos, pos - fInlineLen, levels, inode, bLen, toEnd);

                                    if (op == OP_CLEAR) {
                                        return 0;
                                    }

                                    int newLevels = db.calculateInodeLevels(finalLength - fInlineLen);

                                    if (newLevels >= levels) {
                                        break clearNodes;
                                    }

                                    do {
                                        long childNodeId = DirectPageOps.p_uint48GetLE(inode.mPage, 0);

                                        if (childNodeId == 0) {
                                            inodeId = 0;
                                            break;
                                        }

                                        Node childNode;
                                        try {
                                            childNode = db.nodeMapLoadFragmentExclusive(childNodeId, true);
                                        } catch (Throwable e) {
                                            inode.releaseExclusive();
                                            db.close(e);
                                            throw e;
                                        }

                                        Node toDelete = inode;
                                        inode = childNode;
                                        db.deleteNode(toDelete);
                                        inodeId = inode.mId;
                                    } while (--levels > newLevels);

                                    DirectPageOps.p_int48PutLE(page, loc, inodeId);

                                    if (newLevels <= 0) {
                                        if (pos == 0) {
                                            DirectPageOps.p_bytePut(page, vHeaderLoc, 0);
                                            int garbageAccum = fHeaderLoc - vHeaderLoc + vLen - 1;
                                            node.garbage(node.garbage() + garbageAccum);
                                            db.deleteNode(inode);
                                            break clearNodes;
                                        }
                                        DirectPageOps.p_bytePut(page, fHeaderLoc, fHeader & ~0x01);
                                    }
                                } finally {
                                    inode.releaseExclusive();
                                }
                            }

                            updateLengthField(page, fHeaderLoc, finalLength);
                            return 0;
                        } catch (Throwable e) {
                            node.releaseExclusive();
                            throw e;
                        }

                        final LocalDatabase db = node.getDatabase();
                        final int ipos = (int) (pos - fInlineLen);
                        final int pageSize = pageSize(db, page);
                        loc += (ipos / pageSize) * 6;
                        int fNodeOff = ipos % pageSize;

                        int firstDeletedLoc = loc;

                        while (true) try {
                            final int amt = Math.min((int) bLen, pageSize - fNodeOff);
                            final long fNodeId = DirectPageOps.p_uint48GetLE(page, loc);

                            if (fNodeId != 0) {
                                if (amt >= pageSize || toEnd && fNodeOff <= 0) {
                                    if (txn == null) {
                                        db.deleteFragment(fNodeId);
                                    } else {
                                        Node fNode = db.nodeMapLoadFragmentExclusive(fNodeId, true);
                                        try {
                                            txn.pushUnwrite(cursor.mTree.mId, cursor.mKey,
                                                    pos, fNode.mPage, 0, amt);
                                        } catch (Throwable e) {
                                            fNode.releaseExclusive();
                                        }
                                        db.deleteNode(fNode);
                                    }
                                    DirectPageOps.p_int48PutLE(page, loc, 0);
                                } else {
                                    Node fNode = db.nodeMapLoadFragmentExclusive(fNodeId, true);
                                    try {
                                        if (db.markFragmentDirty(fNode)) {
                                            DirectPageOps.p_int48PutLE(page, loc, fNode.mId);
                                        }
                                        long fNodePage = fNode.mPage;
                                        if (txn != null) {
                                            txn.pushUnwrite(cursor.mTree.mId, cursor.mKey,
                                                    pos, fNodePage, fNodeOff, amt);
                                        }
                                        DirectPageOps.p_clear(fNode.mPage, fNodeOff, fNodeOff + amt);
                                    } finally {
                                        fNode.releaseExclusive();
                                    }
                                    firstDeletedLoc += 6;
                                }
                            }

                            bLen -= amt;
                            loc += 6;

                            if (bLen <= 0) {
                                if (op == OP_SET_LENGTH) {
                                    int shrinkage = loc - firstDeletedLoc;
                                    if (ipos <= 0) {
                                        int len = (int) finalLength;
                                        shrinkage = shrinkage - ipos + fInlineLen - len;
                                        fragmentedToNormal
                                                (node, page, vHeaderLoc, fInlineLoc, len, shrinkage);
                                    } else {
                                        fHeaderLoc = truncateFragmented
                                                (node, page, vHeaderLoc, vLen, shrinkage);
                                        updateLengthField(page, fHeaderLoc, finalLength);
                                    }
                                }

                                return 0;
                            }

                            pos += amt;
                            fNodeOff = 0;
                        } catch (Throwable e) {
                            node.releaseExclusive();
                            throw e;
                        }
                    }

                case OP_WRITE:
                    int fInlineLen = 0;
                    if ((fHeader & 0x02) != 0) {
                        fInlineLen = DirectPageOps.p_ushortGetLE(page, loc);
                        loc += 2;
                        final long amt = fInlineLen - pos;
                        if (amt > 0) {
                            int iLoc = (int) (loc + pos);
                            if (bLen <= amt) {
                                // Only writing inline content.
                                int iLen = (int) bLen;
                                if (txn != null) {
                                    txn.pushUnwrite(cursor.mTree.mId, cursor.mKey,
                                            pos, page, iLoc, iLen);
                                }
                                DirectPageOps.p_copyFromArray(b, bOff, page, iLoc, iLen);
                                return 0;
                            }
                            int iLen = (int) amt;
                            if (txn != null) {
                                txn.pushUnwrite(cursor.mTree.mId, cursor.mKey, pos, page, iLoc, iLen);
                            }
                            DirectPageOps.p_copyFromArray(b, bOff, page, iLoc, iLen);
                            bLen -= amt;
                            bOff += amt;
                            pos = fInlineLen;
                        }
                        loc += fInlineLen;
                    }

                    final long endPos = pos + bLen;
                    final LocalDatabase db;
                    final int pageSize;

                    if (endPos <= fLen) {
                        if (bLen == 0 && b != TOUCH_VALUE) {
                            return 0;
                        }

                        db = node.getDatabase();

                        if ((fHeader & 0x01) != 0) try {
                            // Indirect pointers.

                            final int levels = db.calculateInodeLevels(fLen - fInlineLen);
                            final Node inode = prepareMultilevelWrite(db, page, loc);
                            writeMultilevelFragments
                                    (txn, cursor, pos, pos - fInlineLen,
                                            levels, inode, b, bOff, (int) bLen);

                            return 0;
                        } catch (Throwable e) {
                            node.releaseExclusive();
                            throw e;
                        }

                        pageSize = pageSize(db, page);
                    } else {
                        if (b == TOUCH_VALUE) {
                            return 0;
                        }

                        int fieldGrowth = lengthFieldGrowth(fHeader, endPos);

                        if (fieldGrowth > 0) {
                            tryIncreaseLengthField(cursor, frame, kHeaderLoc, vHeaderLoc, vLen,
                                    fHeaderLoc, fieldGrowth);
                            continue;
                        }

                        db = node.getDatabase();

                        if ((fHeader & 0x01) != 0) try {
                            if (txn != null) {
                                txn.pushUnextend(cursor.mTree.mId, cursor.mKey, fLen);
                                if (pos >= fLen) {
                                    txn = null;
                                }
                            }

                            Node inode = prepareMultilevelWrite(db, page, loc);

                            int levels = db.calculateInodeLevels(fLen - fInlineLen);

                            long newLen = endPos - fInlineLen;

                            if (db.levelCap(levels) < newLen) {
                                int newLevels = db.calculateInodeLevels(newLen);
                                if (newLevels <= levels) {
                                    throw new AssertionError();
                                }

                                pageSize = pageSize(db, page);

                                Node[] newNodes = new Node[newLevels - levels];
                                for (int i = 0; i < newNodes.length; i++) {
                                    try {
                                        newNodes[i] = db.allocDirtyFragmentNode();
                                    } catch (Throwable e) {
                                        try {
                                            // Clean up the mess.
                                            while (--i >= 0) {
                                                db.deleteNode(newNodes[i], true);
                                            }
                                        } catch (Throwable e2) {
                                            Utils.suppress(e, e2);
                                            db.close(e);
                                        }
                                        throw e;
                                    }
                                }

                                for (Node upper : newNodes) {
                                    long upage = upper.mPage;
                                    DirectPageOps.p_int48PutLE(upage, 0, inode.mId);
                                    inode.releaseExclusive();
                                    DirectPageOps.p_clear(upage, 6, pageSize);
                                    inode = upper;
                                }

                                levels = newLevels;

                                DirectPageOps.p_int48PutLE(page, loc, inode.mId);
                            }

                            updateLengthField(page, fHeaderLoc, endPos);

                            writeMultilevelFragments
                                    (txn, cursor, pos, pos - fInlineLen,
                                            levels, inode, b, bOff, (int) bLen);

                            return 0;
                        } catch (Throwable e) {
                            node.releaseExclusive();
                            throw e;
                        }

                        pageSize = pageSize(db, page);

                        long ptrGrowth = pointerCount(pageSize, endPos - fInlineLen)
                                - pointerCount(pageSize, fLen - fInlineLen);

                        if (ptrGrowth > 0) {
                            int newLoc = tryExtendDirect(cursor, frame, kHeaderLoc, vHeaderLoc, vLen,
                                    fHeaderLoc, ptrGrowth * 6);

                            if (newLoc < 0) {
                                continue;
                            }

                            page = node.mPage;

                            int delta = newLoc - fHeaderLoc;
                            loc += delta;

                            fHeaderLoc = newLoc;
                        }

                        if (txn != null) {
                            txn.pushUnextend(cursor.mTree.mId, cursor.mKey, fLen);
                            if (pos >= fLen) {
                                txn = null;
                            }
                        }

                        updateLengthField(page, fHeaderLoc, endPos);
                    }

                    final int ipos = (int) (pos - fInlineLen);
                    loc += (ipos / pageSize) * 6;
                    int fNodeOff = ipos % pageSize;

                    while (true) try {
                        final int amt = Math.min((int) bLen, pageSize - fNodeOff);
                        final long fNodeId = DirectPageOps.p_uint48GetLE(page, loc);
                        if (fNodeId == 0) {
                            if (amt > 0) {
                                if (txn != null) {
                                    txn.pushUnalloc(cursor.mTree.mId, cursor.mKey, pos, amt);
                                }
                                final Node fNode = db.allocDirtyFragmentNode();
                                try {
                                    DirectPageOps.p_int48PutLE(page, loc, fNode.mId);

                                    long fNodePage = fNode.mPage;
                                    DirectPageOps.p_clear(fNodePage, 0, fNodeOff);
                                    DirectPageOps.p_copyFromArray(b, bOff, fNodePage, fNodeOff, amt);
                                    DirectPageOps.p_clear(fNodePage, fNodeOff + amt, pageSize);
                                } finally {
                                    fNode.releaseExclusive();
                                }
                            }
                        } else {
                            if (amt > 0 || b == TOUCH_VALUE) {
                                if (txn == null) {
                                    final Node fNode = db
                                            .nodeMapLoadFragmentExclusive(fNodeId, amt < pageSize);
                                    try {
                                        if (db.markFragmentDirty(fNode)) {
                                            DirectPageOps.p_int48PutLE(page, loc, fNode.mId);
                                        }
                                        DirectPageOps.p_copyFromArray(b, bOff, fNode.mPage, fNodeOff, amt);
                                    } finally {
                                        fNode.releaseExclusive();
                                    }
                                } else {
                                    final Node fNode = db.nodeMapLoadFragmentExclusive(fNodeId, true);
                                    try {
                                        txn.pushUnwrite(cursor.mTree.mId, cursor.mKey,
                                                pos, fNode.mPage, fNodeOff, amt);
                                        if (db.markFragmentDirty(fNode)) {
                                            DirectPageOps.p_int48PutLE(page, loc, fNode.mId);
                                        }
                                        DirectPageOps.p_copyFromArray(b, bOff, fNode.mPage, fNodeOff, amt);
                                    } finally {
                                        fNode.releaseExclusive();
                                    }
                                }
                            }
                        }
                        bLen -= amt;
                        if (bLen <= 0) {
                            return 0;
                        }
                        bOff += amt;
                        pos += pageSize;
                        loc += 6;
                        fNodeOff = 0;
                    } catch (Throwable e) {
                        node.releaseExclusive();
                        throw e;
                    }
            } // end switch(op)
        }
    }

    private static void readMultilevelFragments(long pos, int level, Node inode,
                                                final byte[] b, int bOff, int bLen)
            throws IOException {
        LocalDatabase db = inode.getDatabase();

        start:
        while (true) {
            long page = inode.mPage;
            level--;
            long levelCap = db.levelCap(level);

            int poffset = ((int) (pos / levelCap)) * 6;

            long ppos = pos % levelCap;

            while (true) {
                long childNodeId = DirectPageOps.p_uint48GetLE(page, poffset);
                int len = (int) Math.min(levelCap - ppos, bLen);

                bLen -= len;

                if (childNodeId == 0) {
                    Arrays.fill(b, bOff, bOff + len, (byte) 0);
                    if (bLen <= 0) {
                        inode.releaseShared();
                        return;
                    }
                } else {
                    Node childNode;
                    try {
                        childNode = db.nodeMapLoadFragment(childNodeId);
                    } catch (Throwable e) {
                        inode.releaseShared();
                        throw e;
                    }
                    if (level <= 0) {
                        DirectPageOps.p_copyToArray(childNode.mPage, (int) ppos, b, bOff, len);
                        childNode.releaseShared();
                        if (bLen <= 0) {
                            inode.releaseShared();
                            return;
                        }
                    } else {
                        if (bLen <= 0) {
                            inode.releaseShared();
                            pos = ppos;
                            inode = childNode;
                            bLen = len;
                            continue start;
                        }
                        try {
                            readMultilevelFragments(ppos, level, childNode, b, bOff, len);
                        } catch (Throwable e) {
                            inode.releaseShared();
                            throw e;
                        }
                    }
                }

                bOff += len;
                poffset += 6;

                ppos = 0;
            }
        }
    }

    private static Node prepareMultilevelWrite(LocalDatabase db, long page, int loc)
            throws IOException {
        final Node inode;
        final long inodeId = DirectPageOps.p_uint48GetLE(page, loc);

        if (inodeId == 0) {
            inode = db.allocDirtyFragmentNode();
            DirectPageOps.p_clear(inode.mPage, 0, pageSize(db, inode.mPage));
        } else {
            inode = db.nodeMapLoadFragmentExclusive(inodeId, true);
            try {
                if (!db.markFragmentDirty(inode)) {
                    return inode;
                }
            } catch (Throwable e) {
                inode.releaseExclusive();
                throw e;
            }
        }

        DirectPageOps.p_int48PutLE(page, loc, inode.mId);

        return inode;
    }

    private static void writeMultilevelFragments(LocalTransaction txn, TreeCursor cursor,
                                                 long pos, long ppos, int level, Node inode,
                                                 final byte[] b, int bOff, int bLen)
            throws IOException {
        LocalDatabase db = inode.getDatabase();

        start:
        while (true) {
            long page = inode.mPage;
            level--;
            long levelCap = db.levelCap(level);

            int poffset = ((int) (ppos / levelCap)) * 6;

            ppos %= levelCap;

            final int pageSize = pageSize(db, page);

            while (true) {
                long childNodeId = DirectPageOps.p_uint48GetLE(page, poffset);
                int len = (int) Math.min(levelCap - ppos, bLen);

                bLen -= len;

                if (level <= 0) {
                    final Node childNode;
                    setPtr:
                    {
                        try {
                            if (childNodeId == 0) {
                                if (txn != null) {
                                    txn.pushUnalloc(cursor.mTree.mId, cursor.mKey, pos, len);
                                }
                                childNode = db.allocDirtyFragmentNode();
                                if (ppos > 0 || len < pageSize) {
                                    DirectPageOps.p_clear(childNode.mPage, 0, pageSize);
                                }
                            } else {
                                if (txn == null) {
                                    childNode = db.nodeMapLoadFragmentExclusive
                                            (childNodeId, ppos > 0 | len < pageSize);
                                } else {
                                    childNode = db.nodeMapLoadFragmentExclusive(childNodeId, true);
                                    txn.pushUnwrite(cursor.mTree.mId, cursor.mKey,
                                            pos, childNode.mPage, (int) ppos, len);
                                }

                                try {
                                    if (!db.markFragmentDirty(childNode)) {
                                        break setPtr;
                                    }
                                } catch (Throwable e) {
                                    childNode.releaseExclusive();
                                    throw e;
                                }
                            }
                        } catch (Throwable e) {
                            inode.releaseExclusive();
                            throw e;
                        }

                        DirectPageOps.p_int48PutLE(page, poffset, childNode.mId);
                    }

                    DirectPageOps.p_copyFromArray(b, bOff, childNode.mPage, (int) ppos, len);
                    childNode.releaseExclusive();

                    if (bLen <= 0) {
                        inode.releaseExclusive();
                        return;
                    }
                } else {
                    final Node childNode;
                    setPtr:
                    {
                        try {
                            if (childNodeId == 0) {
                                childNode = db.allocDirtyFragmentNode();
                                DirectPageOps.p_clear(childNode.mPage, 0, pageSize);
                            } else {
                                childNode = db.nodeMapLoadFragmentExclusive(childNodeId, true);
                                try {
                                    if (!db.markFragmentDirty(childNode)) {
                                        break setPtr;
                                    }
                                } catch (Throwable e) {
                                    childNode.releaseExclusive();
                                    throw e;
                                }
                            }
                        } catch (Throwable e) {
                            inode.releaseExclusive();
                            throw e;
                        }

                        DirectPageOps.p_int48PutLE(page, poffset, childNode.mId);
                    }

                    if (bLen <= 0) {
                        inode.releaseExclusive(); // latch coupling release
                        inode = childNode;
                        bLen = len;
                        continue start;
                    }

                    try {
                        writeMultilevelFragments
                                (txn, cursor, pos, ppos, level, childNode, b, bOff, len);
                    } catch (Throwable e) {
                        inode.releaseExclusive();
                        throw e;
                    }
                }

                pos += len;
                bOff += len;
                poffset += 6;

                ppos = 0;
            }
        }
    }

    private static void clearMultilevelFragments(LocalTransaction txn, TreeCursor cursor,
                                                 long pos, long ppos, int level, final Node inode,
                                                 long clearLen, boolean toEnd)
            throws IOException {
        LocalDatabase db = inode.getDatabase();

        long page = inode.mPage;
        level--;
        long levelCap = db.levelCap(level);

        int poffset = ((int) (ppos / levelCap)) * 6;

        ppos %= levelCap;

        while (true) {
            long len = Math.min(levelCap - ppos, clearLen);
            long childNodeId = DirectPageOps.p_uint48GetLE(page, poffset);

            if (childNodeId != 0) {
                if (len >= levelCap || toEnd && ppos <= 0) {
                    full:
                    {
                        Node childNode = null;
                        try {
                            if (level <= 0) {
                                if (txn == null) {
                                    db.deleteFragment(childNodeId);
                                    break full;
                                }
                                childNode = db.nodeMapLoadFragmentExclusive(childNodeId, true);
                                txn.pushUnwrite(cursor.mTree.mId, cursor.mKey,
                                        pos, childNode.mPage, 0, (int) len);
                            } else {
                                childNode = db.nodeMapLoadFragmentExclusive(childNodeId, true);
                                clearMultilevelFragments
                                        (txn, cursor, pos, ppos, level, childNode, len, toEnd);
                            }
                        } catch (Throwable e) {
                            if (childNode != null) {
                                childNode.releaseExclusive();
                            }
                            throw e;
                        }

                        db.deleteNode(childNode);
                    }

                    DirectPageOps.p_int48PutLE(page, poffset, 0);
                } else {
                    Node childNode = db.nodeMapLoadFragmentExclusive(childNodeId, true);
                    try {
                        if (db.markFragmentDirty(childNode)) {
                            DirectPageOps.p_int48PutLE(page, poffset, childNode.mId);
                        }
                        if (level <= 0) {
                            long childPage = childNode.mPage;
                            if (txn != null) {
                                txn.pushUnwrite(cursor.mTree.mId, cursor.mKey,
                                        pos, childPage, (int) ppos, (int) len);
                            }
                            DirectPageOps.p_clear(childPage, (int) ppos, (int) (ppos + len));
                        } else {
                            clearMultilevelFragments
                                    (txn, cursor, pos, ppos, level, childNode, len, toEnd);
                        }
                    } finally {
                        childNode.releaseExclusive();
                    }
                }
            }

            clearLen -= len;
            if (clearLen <= 0) {
                break;
            }

            pos += len;
            poffset += 6;

            ppos = 0;
        }
    }

    private static int lengthFieldGrowth(int fHeader, long fLen) {
        int growth = 0;

        switch ((fHeader >> 2) & 0x03) {
            case 0: // (2 byte length field)
                if (fLen < (1L << (2 * 8))) {
                    break;
                }
                growth = 2;
            case 1: // (4 byte length field)
                if (fLen < (1L << (4 * 8))) {
                    break;
                }
                growth += 2;
            case 2: // (6 byte length field)
                if (fLen < (1L << (6 * 8))) {
                    break;
                }
                growth += 2;
        }

        return growth;
    }

    private static void updateLengthField(long page, int fHeaderLoc, long fLen) {
        int fHeader = DirectPageOps.p_byteGet(page, fHeaderLoc);

        switch ((fHeader >> 2) & 0x03) {
            case 0: // (2 byte length field)
                DirectPageOps.p_shortPutLE(page, fHeaderLoc + 1, (int) fLen);
                break;
            case 1: // (4 byte length field)
                DirectPageOps.p_intPutLE(page, fHeaderLoc + 1, (int) fLen);
                break;
            case 2: // (6 byte length field)
                DirectPageOps.p_int48PutLE(page, fHeaderLoc + 1, fLen);
                break;
            default: // (8 byte length field)
                DirectPageOps.p_longPutLE(page, fHeaderLoc + 1, fLen);
                break;
        }
    }

    private static int tryIncreaseLengthField(final TreeCursor cursor, final CursorFrame frame,
                                              final int kHeaderLoc,
                                              final int vHeaderLoc, final int vLen,
                                              final int fHeaderLoc, final long growth)
            throws IOException {
        final int fOffset = fHeaderLoc - kHeaderLoc;
        final long newEntryLen = fOffset + vLen + growth;
        final Node node = frame.getNode();

        if (newEntryLen > node.getDatabase().mMaxFragmentedEntrySize) {
            compactDirectFormat(cursor, frame, kHeaderLoc, vHeaderLoc, vLen, fHeaderLoc);
            return -1;
        }

        final Tree tree = cursor.mTree;

        try {
            final int igrowth = (int) growth;
            final byte[] newValue = new byte[vLen + igrowth];
            final long page = node.mPage;

            int fHeader = DirectPageOps.p_byteGet(page, fHeaderLoc);
            newValue[0] = (byte) (fHeader + (igrowth << 1));

            int srcLoc = fHeaderLoc + 1;
            int fieldLen = skipFragmentedLengthField(0, fHeader);
            DirectPageOps.p_copyToArray(page, srcLoc, newValue, 1, fieldLen);

            srcLoc += fieldLen;
            int dstLoc = 1 + fieldLen + igrowth;
            DirectPageOps.p_copyToArray(page, srcLoc, newValue, dstLoc, newValue.length - dstLoc);

            DirectPageOps.p_bytePut(page, vHeaderLoc, DirectPageOps.p_byteGet(page, vHeaderLoc) & ~Node.ENTRY_FRAGMENTED);

            node.updateLeafValue(frame, tree, frame.getNodePos(), Node.ENTRY_FRAGMENTED, newValue);
        } catch (Throwable e) {
            node.releaseExclusive();
            throw e;
        }

        if (node.mSplit != null) {
            tree.finishSplit(frame, node);
            return -2;
        }

        return 0;
    }

    private static long pointerCount(long pageSize, long len) {
        long count = (len + pageSize - 1) / pageSize;
        if (count < 0) {
            count = pointerCountOverflow(pageSize, len);
        }
        return count;
    }

    private static long pointerCountOverflow(long pageSize, long len) {
        return BigInteger.valueOf(len).add(BigInteger.valueOf(pageSize - 1))
                .subtract(BigInteger.ONE).divide(BigInteger.valueOf(pageSize)).longValue();
    }


    private static int tryExtendDirect(final TreeCursor cursor, final CursorFrame frame,
                                       final int kHeaderLoc,
                                       final int vHeaderLoc, final int vLen,
                                       final int fHeaderLoc, final long growth)
            throws IOException {
        final int fOffset = fHeaderLoc - kHeaderLoc;
        final long newEntryLen = fOffset + vLen + growth;
        final Node node = frame.getNode();

        if (newEntryLen > node.getDatabase().mMaxFragmentedEntrySize) {
            compactDirectFormat(cursor, frame, kHeaderLoc, vHeaderLoc, vLen, fHeaderLoc);
            return -1;
        }

        final Tree tree = cursor.mTree;

        try {
            final byte[] newValue = new byte[vLen + (int) growth];
            final long page = node.mPage;
            DirectPageOps.p_copyToArray(page, fHeaderLoc, newValue, 0, vLen);

            DirectPageOps.p_bytePut(page, vHeaderLoc, DirectPageOps.p_byteGet(page, vHeaderLoc) & ~Node.ENTRY_FRAGMENTED);

            node.updateLeafValue(frame, tree, frame.getNodePos(), Node.ENTRY_FRAGMENTED, newValue);
        } catch (Throwable e) {
            node.releaseExclusive();
            throw e;
        }

        if (node.mSplit != null) {
            tree.finishSplit(frame, node);
            return -2;
        }

        return DirectPageOps.p_ushortGetLE(node.mPage, node.searchVecStart() + frame.getNodePos()) + fOffset;
    }

    private static int skipFragmentedLengthField(int loc, int fHeader) {
        return loc + 2 + ((fHeader >> 1) & 0x06);
    }

    private static void compactDirectFormat(final TreeCursor cursor, final CursorFrame frame,
                                            final int kHeaderLoc,
                                            final int vHeaderLoc, final int vLen,
                                            final int fHeaderLoc)
            throws IOException {
        final Node node = frame.getNode();
        final long page = node.mPage;

        int loc = fHeaderLoc;
        final int fHeader = DirectPageOps.p_byteGet(page, loc++);
        final long fLen = LocalDatabase.decodeFullFragmentedValueLength(fHeader, page, loc);

        loc = skipFragmentedLengthField(loc, fHeader);

        final int fInlineLen;
        if ((fHeader & 0x02) == 0) {
            fInlineLen = 0;
        } else {
            fInlineLen = DirectPageOps.p_ushortGetLE(page, loc);
            loc = loc + 2 + fInlineLen;
        }


        final int tailLen = fHeaderLoc + vLen - loc; // length of all the direct pointers, in bytes

        final LocalDatabase db = node.getDatabase();
        final int pageSize = pageSize(db, page);
        final int shrinkage;

        if (fInlineLen > 0) {
            if (fInlineLen < 4) {
                byte[] newValue;
                try {
                    byte[] fullValue = db.reconstruct(page, fHeaderLoc, vLen);
                    int max = db.mMaxFragmentedEntrySize - (vHeaderLoc - kHeaderLoc);
                    // Encode it this time without any inline content.
                    newValue = db.fragment(fullValue, fullValue.length, max, 0);
                } catch (Throwable e) {
                    node.releaseExclusive();
                    throw e;
                }

                try {
                    node.updateLeafValue(frame, cursor.mTree, frame.getNodePos(),
                            Node.ENTRY_FRAGMENTED, newValue);
                } catch (Throwable e) {
                    node.releaseExclusive();
                    throw e;
                }

                if (node.mSplit != null) {
                    cursor.mTree.finishSplit(frame, node);
                }

                return;
            }

            Node leftNode;
            Node rightNode = null;

            try {
                if (pointerCount(pageSize, fLen) * 6 <= tailLen) {
                    shrinkage = 2 + fInlineLen;
                } else {
                    rightNode = db.allocDirtyFragmentNode();
                    DirectPageOps.p_clear(rightNode.mPage, fInlineLen, pageSize);
                    shrinkage = 2 + fInlineLen - 6;
                }
                leftNode = shiftDirectRight(db, page, loc, loc + tailLen, fInlineLen, rightNode);
            } catch (Throwable e) {
                node.releaseExclusive();
                try {
                    if (rightNode != null) {
                        db.deleteNode(rightNode, true);
                    }
                } catch (Throwable e2) {
                    Utils.suppress(e, e2);
                    db.close(e);
                }
                throw e;
            }

            DirectPageOps.p_copy(page, loc - fInlineLen, leftNode.mPage, 0, fInlineLen);
            leftNode.releaseExclusive();

            DirectPageOps.p_copy(page, loc, page, loc - fInlineLen - 2, tailLen);

            if (rightNode != null) {
                DirectPageOps.p_int48PutLE(page, loc - fInlineLen - 2 + tailLen, rightNode.mId);
            }

            DirectPageOps.p_bytePut(page, fHeaderLoc, fHeader & ~0x02);
        } else {
            if ((fLen - fInlineLen) > pageSize) {
                Node inode;
                try {
                    inode = db.allocDirtyFragmentNode();
                } catch (Throwable e) {
                    node.releaseExclusive();
                    throw e;
                }

                long ipage = inode.mPage;
                DirectPageOps.p_copy(page, loc, ipage, 0, tailLen);
                DirectPageOps.p_clear(ipage, tailLen, pageSize);

                DirectPageOps.p_int48PutLE(page, loc, inode.mId);
                inode.releaseExclusive();
            }

            DirectPageOps.p_bytePut(page, fHeaderLoc, fHeader | 0x01);

            shrinkage = tailLen - 6;
        }

        int newLen = vLen - shrinkage - 1; // minus one as required by field encoding
        int header = DirectPageOps.p_byteGet(page, vHeaderLoc);
        if ((header & 0x20) == 0) {
            DirectPageOps.p_bytePut(page, vHeaderLoc, (header & 0xe0) | (newLen >> 8));
            DirectPageOps.p_bytePut(page, vHeaderLoc + 1, newLen);
        } else {
            DirectPageOps.p_bytePut(page, vHeaderLoc, (header & 0xf0) | (newLen >> 16));
            DirectPageOps.p_bytePut(page, vHeaderLoc + 1, newLen >> 8);
            DirectPageOps.p_bytePut(page, vHeaderLoc + 2, newLen);
        }

        node.garbage(node.garbage() + shrinkage);

        if (node.shouldLeafMerge()) {
            cursor.mergeLeaf(frame, node);
            frame.acquireExclusive();
        }
    }

    private static Node shiftDirectRight(LocalDatabase db, final long page,
                                         int startLoc, int endLoc, int amount,
                                         Node dstNode)
            throws IOException {
        final Node[] fNodes = new Node[(endLoc - startLoc) / 6];
        final int pageSize = pageSize(db, page);

        try {
            boolean requireDest = true;
            for (int i = 0, loc = startLoc; loc < endLoc; i++, loc += 6) {
                long fNodeId = DirectPageOps.p_uint48GetLE(page, loc);
                if (fNodeId != 0) {
                    Node fNode = db.nodeMapLoadFragmentExclusive(fNodeId, true);
                    fNodes[i] = fNode;
                    if (db.markFragmentDirty(fNode)) {
                        DirectPageOps.p_int48PutLE(page, loc, fNode.mId);
                    }
                    requireDest = true;
                } else if (requireDest) {
                    Node fNode = db.allocDirtyFragmentNode();
                    DirectPageOps.p_clear(fNode.mPage, 0, pageSize);
                    fNodes[i] = fNode;
                    DirectPageOps.p_int48PutLE(page, loc, fNode.mId);
                    requireDest = false;
                }
            }
        } catch (Throwable e) {
            for (Node fNode : fNodes) {
                if (fNode != null) {
                    fNode.releaseExclusive();
                }
            }
            throw e;
        }

        for (int i = fNodes.length; --i >= 0; ) {
            Node fNode = fNodes[i];
            if (fNode == null) {
                if (dstNode != null) {
                    DirectPageOps.p_clear(dstNode.mPage, 0, amount);
                }
            } else {
                long fPage = fNode.mPage;
                if (dstNode != null) {
                    DirectPageOps.p_copy(fPage, pageSize - amount, dstNode.mPage, 0, amount);
                    dstNode.releaseExclusive();
                }
                DirectPageOps.p_copy(fPage, 0, fPage, amount, pageSize - amount);
            }
            dstNode = fNode;
        }

        return dstNode;
    }

    private static void fragmentedToNormal(final Node node, final long page,
                                           final int vHeaderLoc, final int fInlineLoc,
                                           final int fInlineLen, final int shrinkage) {
        int loc = vHeaderLoc;

        if (fInlineLen <= 127) {
            DirectPageOps.p_bytePut(page, loc++, fInlineLen);
        } else if (fInlineLen <= 8192) {
            DirectPageOps.p_bytePut(page, loc++, 0x80 | ((fInlineLen - 1) >> 8));
            DirectPageOps.p_bytePut(page, loc++, fInlineLen - 1);
        } else {
            DirectPageOps.p_bytePut(page, loc++, 0xa0 | ((fInlineLen - 1) >> 16));
            DirectPageOps.p_bytePut(page, loc++, (fInlineLen - 1) >> 8);
            DirectPageOps.p_bytePut(page, loc++, fInlineLen - 1);
        }

        DirectPageOps.p_copy(page, fInlineLoc, page, loc, fInlineLen);

        node.garbage(node.garbage() + shrinkage + (fInlineLoc - loc));
    }

    private static int truncateFragmented(final Node node, final long page,
                                          final int vHeaderLoc, final int vLen, int shrinkage) {
        final int newLen = vLen - shrinkage;
        int loc = vHeaderLoc;

        if (vLen <= 8192) {
            DirectPageOps.p_bytePut(page, loc++, 0xc0 | ((newLen - 1) >> 8));
            DirectPageOps.p_bytePut(page, loc++, newLen - 1);
        } else {
            DirectPageOps.p_bytePut(page, loc++, 0xe0 | ((newLen - 1) >> 16));
            DirectPageOps.p_bytePut(page, loc++, (newLen - 1) >> 8);
            DirectPageOps.p_bytePut(page, loc++, newLen - 1);
        }

        node.garbage(node.garbage() + shrinkage);

        return loc;
    }

    private static int pageSize(LocalDatabase db, long page) {
        // return page.length;
        return db.pageSize();
    }
}
