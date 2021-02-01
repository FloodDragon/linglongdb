package com.glodon.linglong.engine.core.frame;

import com.glodon.linglong.engine.core.tx.Transaction;

import java.io.IOException;

/**
 * @author Stereo
 */
public abstract class SelectCombiner implements Combiner {
    public static final class First extends SelectCombiner {
        public static final Combiner THE = new First();

        @Override
        public byte[] combine(byte[] key, byte[] first, byte[] second) {
            return first;
        }

        @Override
        public byte[] loadUnion(Transaction txn, byte[] key, View first, View second)
                throws IOException {
            byte[] v1 = first.load(txn, key);
            if (v1 == null) {
                return second.load(txn, key);
            } else {
                second.touch(txn, key);
                return v1;
            }
        }

        @Override
        public byte[] loadIntersection(Transaction txn, byte[] key, View first, View second)
                throws IOException {
            byte[] v1 = first.load(txn, key);
            if (v1 == null) {
                second.touch(txn, key);
                return null;
            }
            return second.exists(txn, key) ? v1 : null;
        }

        @Override
        public byte[] loadDifference(Transaction txn, byte[] key, View first, View second)
                throws IOException {
            byte[] v1 = first.load(txn, key);
            if (v1 == null) {
                second.touch(txn, key);
                return null;
            }
            return v1;
        }
    }

    public static final class Second extends SelectCombiner {
        public static final Combiner THE = new Second();

        @Override
        public byte[] combine(byte[] key, byte[] first, byte[] second) {
            return second;
        }

        @Override
        public byte[] loadUnion(Transaction txn, byte[] key, View first, View second)
                throws IOException {
            first.touch(txn, key);
            byte[] v2 = second.load(txn, key);
            return v2 == null ? first.load(txn, key) : v2;
        }

        @Override
        public byte[] loadIntersection(Transaction txn, byte[] key, View first, View second)
                throws IOException {
            first.touch(txn, key);
            byte[] v2 = second.load(txn, key);
            return v2 == null ? null : first.exists(txn, key) ? v2 : null;
        }
    }

    public static final class Discard extends SelectCombiner {
        public static final Combiner THE = new Discard();

        @Override
        public byte[] combine(byte[] key, byte[] first, byte[] second) {
            return null;
        }

        @Override
        public byte[] loadUnion(Transaction txn, byte[] key, View first, View second)
                throws IOException {
            byte[] v1 = first.load(txn, key);
            if (v1 == null) {
                return second.load(txn, key);
            } else {
                return second.exists(txn, key) ? null : v1;
            }
        }

        @Override
        public byte[] loadIntersection(Transaction txn, byte[] key, View first, View second)
                throws IOException {
            first.touch(txn, key);
            second.touch(txn, key);
            return null;
        }

        @Override
        public byte[] loadDifference(Transaction txn, byte[] key, View first, View second)
                throws IOException {
            byte[] v1 = first.load(txn, key);
            if (v1 == null) {
                second.touch(txn, key);
                return null;
            }
            return second.exists(txn, key) ? null : v1;
        }
    }

    @Override
    public boolean requireValues() {
        return false;
    }
}
