package com.linglong.engine.core.lock;

import com.linglong.base.common.Utils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * @author Stereo
 */
public final class DeadlockSet implements Serializable {
    private static final long serialVersionUID = 1L;

    private final OwnerInfo[] mInfoSet;

    DeadlockSet(OwnerInfo[] infoSet) {
        mInfoSet = infoSet;
    }

    public int size() {
        return mInfoSet.length;
    }

    public long getIndexId(int pos) {
        return mInfoSet[pos].mIndexId;
    }

    public byte[] getIndexName(int pos) {
        return mInfoSet[pos].mIndexName;
    }

    public String getIndexNameString(int pos) {
        return indexNameString(getIndexName(pos));
    }

    private static String indexNameString(byte[] name) {
        if (name == null) {
            return null;
        }
        return new String(name, StandardCharsets.UTF_8);
    }

    public byte[] getKey(int pos) {
        return mInfoSet[pos].mKey;
    }

    public Object getOwnerAttachment(int pos) {
        return mInfoSet[pos].mAttachment;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append('[');
        appendMembers(b);
        return b.append(']').toString();
    }

    public void appendMembers(StringBuilder b) {
        for (int i=0; i<mInfoSet.length; i++) {
            OwnerInfo info = mInfoSet[i];
            if (i > 0) {
                b.append(", ");
            }
            b.append('{');
            b.append("indexId").append(": ").append(info.mIndexId);
            b.append(", ");

            String name = indexNameString(info.mIndexName);
            if (name != null) {
                b.append("indexName").append(": ").append(name);
                b.append(", ");
            }

            b.append("key").append(": ").append(Utils.toHex(info.mKey));

            Object att = info.mAttachment;
            if (att != null) {
                b.append(", ");
                b.append("attachment").append(": ").append(att);
            }

            b.append('}');
        }
    }

    static class OwnerInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        long mIndexId;
        byte[] mIndexName;
        byte[] mKey;
        Object mAttachment;
    }
}
