package com.linglong.engine.core.page;


import com.linglong.base.common.IntegerRef;
import com.linglong.base.exception.CorruptDatabaseException;
import com.linglong.io.*;

import java.io.IOException;
import java.util.function.LongConsumer;

import static com.linglong.engine.core.page.PageQueue.*;

/**
 * @author Stereo
 */
public class PageQueueScanner {

    public static void scan(PageArray array, long headerId, int headerOffset, LongConsumer dst)
            throws IOException {
        byte[] buf = new byte[array.pageSize()];
        array.readPage(headerId, buf);

        final long pageCount = PageOps.p_longGetLE(buf, headerOffset + I_REMOVE_PAGE_COUNT);
        final long nodeCount = PageOps.p_longGetLE(buf, headerOffset + I_REMOVE_NODE_COUNT);
        long nodeId = PageOps.p_longGetLE(buf, headerOffset + I_REMOVE_HEAD_ID);
        int nodeOffset = PageOps.p_intGetLE(buf, headerOffset + I_REMOVE_HEAD_OFFSET);
        long pageId = PageOps.p_longGetLE(buf, headerOffset + I_REMOVE_HEAD_FIRST_PAGE_ID);
        final long tailId = PageOps.p_longGetLE(buf, headerOffset + I_APPEND_HEAD_ID);

        if (nodeId == 0) {
            if (pageCount != 0 || nodeCount != 0) {
                throw new CorruptDatabaseException
                        ("Invalid empty page queue: " + pageCount + ", " + nodeCount);
            }
            return;
        }

        array.readPage(nodeId, buf);

        if (pageId == 0) {
            if (nodeOffset != I_NODE_START) {
                throw new CorruptDatabaseException("Invalid node offset: " + nodeOffset);
            }
            pageId = PageOps.p_longGetBE(buf, I_FIRST_PAGE_ID);
        }

        long actualPageCount = 0;
        long actualNodeCount = 1;

        IntegerRef.Value nodeOffsetRef = new IntegerRef.Value();
        nodeOffsetRef.set(nodeOffset);

        while (true) {
            actualPageCount++;
            dst.accept(pageId);

            if (nodeOffsetRef.get() < buf.length) {
                long delta = PageOps.p_ulongGetVar(buf, nodeOffsetRef);
                if (delta > 0) {
                    pageId += delta;
                    continue;
                }
            }

            dst.accept(nodeId);

            nodeId = PageOps.p_longGetBE(buf, I_NEXT_NODE_ID);
            if (nodeId == tailId) {
                break;
            }

            array.readPage(nodeId, buf);

            actualNodeCount++;
            pageId = PageOps.p_longGetBE(buf, I_FIRST_PAGE_ID);
            nodeOffsetRef.set(I_NODE_START);
        }

        if (pageCount != actualPageCount) {
            throw new CorruptDatabaseException
                    ("Mismatched page count: " + pageCount + " != " + actualPageCount);
        }

        if (nodeCount != actualNodeCount) {
            throw new CorruptDatabaseException
                    ("Mismatched node count: " + nodeCount + " != " + actualNodeCount);
        }
    }
}
