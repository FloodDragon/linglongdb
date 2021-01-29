package com.glodon.linglong.engine.core.page;

import com.glodon.linglong.base.common.IntegerRef;
import com.glodon.linglong.engine.core.Node;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

import static com.glodon.linglong.base.common.IOUtils.*;
import static com.glodon.linglong.base.common.Utils.*;

/**
 * 对数据库页进行操作的低级操作。
 *
 * @author Stereo
 */
final class PageOps {
    static final int NODE_OVERHEAD = 100;

    private static final byte[] CLOSED_TREE_PAGE;
    private static final byte[] STUB_TREE_PAGE;

    static {
        CLOSED_TREE_PAGE = newEmptyTreeLeafPage();
        STUB_TREE_PAGE = newEmptyTreePage(Node.TN_HEADER_SIZE + 8, Node.TYPE_TN_IN);
    }

    private static byte[] newEmptyTreeLeafPage() {
        return newEmptyTreePage
                (Node.TN_HEADER_SIZE, Node.TYPE_TN_LEAF | Node.LOW_EXTREMITY | Node.HIGH_EXTREMITY);
    }

    private static byte[] newEmptyTreePage(int pageSize, int type) {
        byte[] empty = new byte[pageSize];

        empty[0] = (byte) type;

        p_shortPutLE(empty, 4, Node.TN_HEADER_SIZE);     // leftSegTail
        p_shortPutLE(empty, 6, pageSize - 1);            // rightSegTail
        p_shortPutLE(empty, 8, Node.TN_HEADER_SIZE);     // searchVecStart
        p_shortPutLE(empty, 10, Node.TN_HEADER_SIZE - 2); // searchVecEnd

        return empty;
    }

    static byte[] p_null() {
        return null;
    }

    static byte[] p_closedTreePage() {
        return CLOSED_TREE_PAGE;
    }

    static byte[] p_stubTreePage() {
        return STUB_TREE_PAGE;
    }

    static byte[] p_alloc(int size, boolean aligned) {
        return new byte[size];
    }

    static byte[] p_calloc(int size, boolean aligned) {
        return new byte[size];
    }

    static byte[][] p_allocArray(int size) {
        return new byte[size][];
    }

    static void p_delete(byte[] page) {
    }

    static Object p_arenaAlloc(int pageSize, long pageCount) throws IOException {
        return null;
    }

    static void p_arenaDelete(Object arena) throws IOException {
        if (arena != null) {
            throw new IllegalArgumentException();
        }
    }

    static byte[] p_calloc(Object arena, int size, boolean aligned) {
        return p_calloc(size, aligned);
    }

    static byte[] p_clone(byte[] page, int length, boolean aligned) {
        return page.clone();
    }

    static byte[] p_transfer(byte[] array, boolean aligned) {
        return array;
    }

    static byte[] p_transferTo(byte[] array, byte[] page) {
        return array;
    }

    static byte p_byteGet(byte[] page, int index) {
        return page[index];
    }

    static int p_ubyteGet(byte[] page, int index) {
        return page[index] & 0xff;
    }

    static void p_bytePut(byte[] page, int index, byte v) {
        page[index] = v;
    }

    static void p_bytePut(byte[] page, int index, int v) {
        page[index] = (byte) v;
    }

    static int p_ushortGetLE(byte[] page, int index) {
        return decodeUnsignedShortLE(page, index);
    }

    static void p_shortPutLE(byte[] page, int index, int v) {
        encodeShortLE(page, index, v);
    }

    static int p_intGetLE(byte[] page, int index) {
        return decodeIntLE(page, index);
    }

    static void p_intPutLE(byte[] page, int index, int v) {
        encodeIntLE(page, index, v);
    }

    static int p_uintGetVar(byte[] page, int index) {
        return decodeUnsignedVarInt(page, index);
    }

    static int p_uintPutVar(byte[] page, int index, int v) {
        return encodeUnsignedVarInt(page, index, v);
    }

    static int p_uintVarSize(int v) {
        return calcUnsignedVarIntLength(v);
    }

    static long p_uint48GetLE(byte[] page, int index) {
        return decodeUnsignedInt48LE(page, index);
    }

    static void p_int48PutLE(byte[] page, int index, long v) {
        encodeInt48LE(page, index, v);
    }

    static long p_longGetLE(byte[] page, int index) {
        return decodeLongLE(page, index);
    }

    static void p_longPutLE(byte[] page, int index, long v) {
        encodeLongLE(page, index, v);
    }

    static long p_longGetBE(byte[] page, int index) {
        return decodeLongBE(page, index);
    }

    static void p_longPutBE(byte[] page, int index, long v) {
        encodeLongBE(page, index, v);
    }

    static long p_ulongGetVar(byte[] page, IntegerRef ref) {
        return decodeUnsignedVarLong(page, ref);
    }

    static int p_ulongPutVar(byte[] page, int index, long v) {
        return encodeUnsignedVarLong(page, index, v);
    }

    static int p_ulongVarSize(long v) {
        return calcUnsignedVarLongLength(v);
    }

    static void p_clear(byte[] page, int fromIndex, int toIndex) {
        java.util.Arrays.fill(page, fromIndex, toIndex, (byte) 0);
    }

    static byte[] p_copyIfNotArray(byte[] page, byte[] array) {
        return page;
    }

    static void p_copyFromArray(byte[] src, int srcStart,
                                byte[] dstPage, int dstStart, int len) {
        System.arraycopy(src, srcStart, dstPage, dstStart, len);
    }

    static void p_copyToArray(byte[] srcPage, int srcStart,
                              byte[] dst, int dstStart, int len) {
        System.arraycopy(srcPage, srcStart, dst, dstStart, len);
    }

    static void p_copyFromBB(ByteBuffer src, byte[] dstPage, int dstStart, int len) {
        src.get(dstPage, dstStart, len);
    }

    static void p_copyToBB(byte[] srcPage, int srcStart, ByteBuffer dst, int len) {
        dst.put(srcPage, srcStart, len);
    }

    public static void p_copy(byte[] srcPage, int srcStart,
                       byte[] dstPage, int dstStart, int len) {
        System.arraycopy(srcPage, srcStart, dstPage, dstStart, len);
    }

    static void p_copies(byte[] page,
                         int start1, int dest1, int length1,
                         int start2, int dest2, int length2) {
        if (dest1 < start1) {
            p_copy(page, start1, page, dest1, length1);
            p_copy(page, start2, page, dest2, length2);
        } else {
            p_copy(page, start2, page, dest2, length2);
            p_copy(page, start1, page, dest1, length1);
        }
    }

    static void p_copies(byte[] page,
                         int start1, int dest1, int length1,
                         int start2, int dest2, int length2,
                         int start3, int dest3, int length3) {
        if (dest1 < start1) {
            p_copy(page, start1, page, dest1, length1);
            p_copies(page, start2, dest2, length2, start3, dest3, length3);
        } else {
            p_copies(page, start2, dest2, length2, start3, dest3, length3);
            p_copy(page, start1, page, dest1, length1);
        }
    }

    static int p_compareKeysPageToArray(byte[] apage, int aoff, int alen,
                                        byte[] b, int boff, int blen) {
        return compareUnsigned(apage, aoff, alen, b, boff, blen);
    }

    static int p_compareKeysPageToPage(byte[] apage, int aoff, int alen,
                                       byte[] bpage, int boff, int blen) {
        return compareUnsigned(apage, aoff, alen, bpage, boff, blen);
    }

    static byte[] p_midKeyLowPage(byte[] lowPage, int lowOff, int lowLen,
                                  byte[] high, int highOff) {
        return midKey(lowPage, lowOff, lowLen, high, highOff);
    }

    static byte[] p_midKeyHighPage(byte[] low, int lowOff, int lowLen,
                                   byte[] highPage, int highOff) {
        return midKey(low, lowOff, lowLen, highPage, highOff);
    }

    static byte[] p_midKeyLowHighPage(byte[] lowPage, int lowOff, int lowLen,
                                      byte[] highPage, int highOff) {
        return midKey(lowPage, lowOff, lowLen, highPage, highOff);
    }

    static int p_crc32(byte[] srcPage, int srcStart, int len) {
        CRC32 crc = new CRC32();
        crc.update(srcPage, srcStart, len);
        return (int) crc.getValue();
    }
}
