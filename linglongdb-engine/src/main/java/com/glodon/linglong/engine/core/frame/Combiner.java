package com.glodon.linglong.engine.core.frame;

import com.glodon.linglong.engine.core.View;
import com.glodon.linglong.engine.core.tx.Transaction;

import java.io.IOException;

/**
 * @author Stereo
 */
@FunctionalInterface
public interface Combiner {
    public static Combiner first() {
        return SelectCombiner.First.THE;
    }

    public static Combiner second() {
        return SelectCombiner.Second.THE;
    }

    public static Combiner discard() {
        return SelectCombiner.Discard.THE;
    }

    public byte[] combine(byte[] key, byte[] first, byte[] second) throws IOException;

    public default boolean requireValues() {
        return true;
    }

    public default boolean combineLocks() {
        return false;
    }

    public default byte[] loadUnion(Transaction txn, byte[] key, View first, View second)
            throws IOException {
        byte[] v1 = first.load(txn, key);
        byte[] v2 = second.load(txn, key);
        return v1 == null ? v2 : (v2 == null ? v1 : combine(key, v1, v2));
    }

    public default byte[] loadIntersection(Transaction txn, byte[] key, View first, View second)
            throws IOException {
        byte[] v1 = first.load(txn, key);
        if (v1 == null) {
            // Always need to lock the second entry too, for consistency and to avoid any odd
            // deadlocks if the store method is called.
            second.touch(txn, key);
            return null;
        }
        byte[] v2 = second.load(txn, key);
        return v2 == null ? null : combine(key, v1, v2);
    }

    public default byte[] loadDifference(Transaction txn, byte[] key, View first, View second)
            throws IOException {
        byte[] v1 = first.load(txn, key);
        if (v1 == null) {
            // Always need to lock the second entry too, for consistency and to avoid any odd
            // deadlocks if the store method is called.
            second.touch(txn, key);
            return null;
        }
        byte[] v2 = second.load(txn, key);
        return v2 == null ? v1 : combine(key, v1, v2);
    }

    public default byte[][] separate(byte[] key, byte[] value) throws IOException {
        return null;
    }
}
