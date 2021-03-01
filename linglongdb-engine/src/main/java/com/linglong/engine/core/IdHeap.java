package com.linglong.engine.core;

import com.linglong.base.common.IntegerRef;
import com.linglong.engine.core.page.DirectPageOps;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * @author Stereo
 */
public final class IdHeap {
    private final int mDrainSize;
    private long[] mIds;
    private int mSize;

    public IdHeap(int drainSize) {
        mDrainSize = drainSize;
        mIds = new long[drainSize + 1];
    }

    public int size() {
        return mSize;
    }

    public void add(long id) {
        long[] ids = mIds;
        int pos = mSize;
        if (pos >= ids.length) {
            mIds = ids = Arrays.copyOf(ids, ids.length + 1);
        }
        while (pos > 0) {
            int parentPos = (pos - 1) >>> 1;
            long parentId = ids[parentPos];
            if (id >= parentId) {
                break;
            }
            ids[pos] = parentId;
            pos = parentPos;
        }
        ids[pos] = id;
        mSize++;
    }

    /*
    public long peek() {
        if (mSize <= 0) {
            throw new NoSuchElementException();
        }
        return mIds[0];
    }
    */

    public long remove() {
        long id = tryRemove();
        if (id == 0) {
            throw new NoSuchElementException();
        }
        return id;
    }

    public long tryRemove() {
        final int size = mSize;
        if (size <= 0) {
            return 0;
        }
        int pos = size - 1;
        long[] ids = mIds;
        long result = ids[0];
        if (pos != 0) {
            long id = ids[pos];
            pos = 0;
            int half = size >>> 1;
            while (pos < half) {
                int childPos = (pos << 1) + 1;
                long child = ids[childPos];
                int rightPos = childPos + 1;
                if (rightPos < size && child > ids[rightPos]) {
                    child = ids[childPos = rightPos];
                }
                if (id <= child) {
                    break;
                }
                ids[pos] = child;
                pos = childPos;
            }
            ids[pos] = id;
        }
        mSize = size - 1;
        return result;
    }

    public void remove(long id) {
        long[] copy = new long[mIds.length];
        int pos = 0;
        while (true) {
            long removed = tryRemove();
            if (removed == 0) {
                break;
            }
            if (removed != id) {
                copy[pos++] = removed;
            }
        }
        while (--pos >= 0) {
            add(copy[pos]);
        }
    }

    public boolean shouldDrain() {
        return mSize >= mDrainSize;
    }

    public int drain(long prevId, long buffer, int offset, int length) {
        int end = offset + length;
        while (mSize > 0 && offset < end) {
            if (offset > (end - 9)) {
                long id = mIds[0];
                if (offset + DirectPageOps.p_ulongVarSize(id - prevId) > end) {
                    break;
                }
            }
            long id = remove();
            offset = DirectPageOps.p_ulongPutVar(buffer, offset, id - prevId);
            prevId = id;
        }
        return offset;
    }

    public void undrain(long id, long buffer, int offset, int endOffset) {
        add(id);
        IntegerRef offsetRef = new IntegerRef.Value();
        offsetRef.set(offset);
        while (offsetRef.get() < endOffset) {
            id += DirectPageOps.p_ulongGetVar(buffer, offsetRef);
            add(id);
        }
    }
}
