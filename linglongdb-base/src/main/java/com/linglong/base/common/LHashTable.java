package com.linglong.base.common;

/**
 * 自定义项的简单哈希表
 *
 * @author Stereo
 */
public abstract class LHashTable<E extends LHashTable.Entry<E>> {
    public static final class ObjEntry<V> extends Entry<ObjEntry<V>> {
        public V value;
    }

    public static final class Obj<V> extends LHashTable<ObjEntry<V>> {
        public Obj(int capacity) {
            super(capacity);
        }

        public V getValue(long key) {
            ObjEntry<V> entry = get(key);
            return entry == null ? null : entry.value;
        }

        public V removeValue(long key) {
            ObjEntry<V> entry = remove(key);
            return entry == null ? null : entry.value;
        }

        protected ObjEntry<V> newEntry() {
            return new ObjEntry<>();
        }
    }

    public static final class IntEntry extends Entry<IntEntry> {
        public int value;
    }

    public static final class Int extends LHashTable<IntEntry> {
        public Int(int capacity) {
            super(capacity);
        }

        protected IntEntry newEntry() {
            return new IntEntry();
        }
    }

    private static final float LOAD_FACTOR = 0.75f;

    private E[] mEntries;
    private int mSize;
    private int mGrowThreshold;

    public LHashTable(int capacity) {
        clear(capacity);
    }

    public final int size() {
        return mSize;
    }

    @SuppressWarnings("unchecked")
    public final void clear(int capacity) {
        if (capacity <= 0) {
            capacity = 1;
        }
        capacity = Utils.roundUpPower2(capacity);
        E[] entries = mEntries;
        if (entries != null && entries.length == capacity) {
            java.util.Arrays.fill(entries, null);
        } else {
            mEntries = (E[]) new Entry[capacity];
            mGrowThreshold = (int) (capacity * LOAD_FACTOR);
        }
        mSize = 0;
    }

    public final E get(long key) {
        E[] entries = mEntries;
        for (E e = entries[((int) key) & (entries.length - 1)]; e != null; e = e.next) {
            if (e.key == key) {
                return e;
            }
        }
        return null;
    }

    public final E insert(long key) {
        E[] entries = mEntries;
        int index = ((int) key) & (entries.length - 1);
        for (E e = entries[index]; e != null; e = e.next) {
            if (e.key == key) {
                return e;
            }
        }
        if (grow()) {
            entries = mEntries;
            index = ((int) key) & (entries.length - 1);
        }
        mSize++;
        return entries[index] = newEntry(key, entries[index]);
    }

    public final E replace(long key) {
        E[] entries = mEntries;
        int index = ((int) key) & (entries.length - 1);
        for (E e = entries[index], prev = null; e != null; e = e.next) {
            if (e.key == key) {
                if (prev == null) {
                    entries[index] = e.next;
                } else {
                    prev.next = e.next;
                }
                return entries[index] = newEntry(key, entries[index]);
            } else {
                prev = e;
            }
        }
        if (grow()) {
            entries = mEntries;
            index = ((int) key) & (entries.length - 1);
        }
        mSize++;
        return entries[index] = newEntry(key, entries[index]);
    }

    public final E remove(long key) {
        E[] entries = mEntries;
        int index = ((int) key) & (entries.length - 1);
        for (E e = entries[index], prev = null; e != null; e = e.next) {
            if (e.key == key) {
                if (prev == null) {
                    entries[index] = e.next;
                } else {
                    prev.next = e.next;
                }
                mSize--;
                return e;
            } else {
                prev = e;
            }
        }
        return null;
    }

    public <X extends Exception> void traverse(Visitor<E, X> v) throws X {
        E[] entries = mEntries;
        for (int i = 0; i < entries.length; i++) {
            for (E e = entries[i], prev = null; e != null; ) {
                E next = e.next;
                if (v.visit(e)) {
                    if (prev == null) {
                        entries[i] = next;
                    } else {
                        prev.next = next;
                    }
                    mSize--;
                } else {
                    prev = e;
                }
                e = next;
            }
        }
    }

    protected abstract E newEntry();

    private E newEntry(long key, E next) {
        E e = newEntry();
        e.key = key;
        e.next = next;
        return e;
    }

    @SuppressWarnings("unchecked")
    private boolean grow() {
        if (mSize < mGrowThreshold) {
            return false;
        }

        E[] entries = mEntries;

        int capacity = entries.length << 1;
        if (capacity == 0) {
            capacity = 1;
        }
        E[] newEntries = (E[]) new Entry[capacity];
        int newMask = capacity - 1;

        for (int i = entries.length; --i >= 0; ) {
            for (E e = entries[i]; e != null; ) {
                E next = e.next;
                int ix = ((int) e.key) & newMask;
                e.next = newEntries[ix];
                newEntries[ix] = e;
                e = next;
            }
        }

        mEntries = newEntries;
        mGrowThreshold = (int) (capacity * LOAD_FACTOR);

        return true;
    }

    public static class Entry<E extends Entry<E>> {
        public long key;
        E next;
    }

    @FunctionalInterface
    public interface Visitor<E extends Entry<E>, X extends Exception> {
        boolean visit(E e) throws X;
    }
}
