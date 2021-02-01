package com.glodon.linglong.engine.core.frame;

import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.engine.core.tx.Transaction;
import com.glodon.linglong.engine.observer.VerificationObserver;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;

/**
 * 索引
 *
 * @author Stereo
 */
public interface Index extends View, Closeable {
    long getId();

    byte[] getName();

    String getNameString();

    long evict(Transaction txn, byte[] lowKey, byte[] highKey,
               Filter evictionFilter, boolean autoload)
            throws IOException;

    Stats analyze(byte[] lowKey, byte[] highKey) throws IOException;

    class Stats implements Cloneable, Serializable {
        private static final long serialVersionUID = 3L;

        public double entryCount;
        public double keyBytes;
        public double valueBytes;
        public double freeBytes;
        public double totalBytes;

        public Stats(double entryCount,
                     double keyBytes,
                     double valueBytes,
                     double freeBytes,
                     double totalBytes) {
            this.entryCount = entryCount;
            this.keyBytes = keyBytes;
            this.valueBytes = valueBytes;
            this.freeBytes = freeBytes;
            this.totalBytes = totalBytes;
        }

        public double entryCount() {
            return entryCount;
        }

        public double keyBytes() {
            return keyBytes;
        }

        public double valueBytes() {
            return valueBytes;
        }

        public double freeBytes() {
            return freeBytes;
        }

        public double totalBytes() {
            return totalBytes;
        }

        public Stats add(Stats augend) {
            return new Stats(entryCount + augend.entryCount,
                    keyBytes + augend.keyBytes,
                    valueBytes + augend.valueBytes,
                    freeBytes + augend.freeBytes,
                    totalBytes + augend.totalBytes);
        }

        public Stats subtract(Stats subtrahend) {
            return new Stats(entryCount - subtrahend.entryCount,
                    keyBytes - subtrahend.keyBytes,
                    valueBytes - subtrahend.valueBytes,
                    freeBytes - subtrahend.freeBytes,
                    totalBytes - subtrahend.totalBytes);
        }

        public Stats divide(double scalar) {
            return new Stats(entryCount / scalar,
                    keyBytes / scalar,
                    valueBytes / scalar,
                    freeBytes / scalar,
                    totalBytes / scalar);
        }

        public Stats round() {
            return new Stats(Math.round(entryCount),
                    Math.round(keyBytes),
                    Math.round(valueBytes),
                    Math.round(freeBytes),
                    Math.round(totalBytes));
        }

        public Stats divideAndRound(double scalar) {
            return new Stats(Math.round(entryCount / scalar),
                    Math.round(keyBytes / scalar),
                    Math.round(valueBytes / scalar),
                    Math.round(freeBytes / scalar),
                    Math.round(totalBytes / scalar));
        }

        @Override
        public Stats clone() {
            try {
                return (Stats) super.clone();
            } catch (CloneNotSupportedException e) {
                throw Utils.rethrow(e);
            }
        }

        @Override
        public int hashCode() {
            long hash = Double.doubleToLongBits(entryCount);
            hash = hash * 31 + Double.doubleToLongBits(keyBytes);
            hash = hash * 31 + Double.doubleToLongBits(valueBytes);
            hash = hash * 31 + Double.doubleToLongBits(freeBytes);
            hash = hash * 31 + Double.doubleToLongBits(totalBytes);
            return (int) Utils.scramble(hash);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj != null && obj.getClass() == Stats.class) {
                Stats other = (Stats) obj;
                return entryCount == other.entryCount
                        && keyBytes == other.keyBytes
                        && valueBytes == other.valueBytes
                        && freeBytes == other.freeBytes
                        && totalBytes == other.totalBytes;
            }
            return false;
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder("Index.Stats {");

            boolean any = false;
            any = append(b, any, "entryCount", entryCount);
            any = append(b, any, "keyBytes", keyBytes);
            any = append(b, any, "valueBytes", valueBytes);
            any = append(b, any, "freeBytes", freeBytes);
            any = append(b, any, "totalBytes", totalBytes);

            b.append('}');
            return b.toString();
        }

        private static boolean append(StringBuilder b, boolean any, String name, double value) {
            if (!Double.isNaN(value)) {
                if (any) {
                    b.append(", ");
                }

                b.append(name).append('=');

                long v = (long) value;
                if (v == value) {
                    b.append(v);
                } else {
                    b.append(value);
                }

                any = true;
            }

            return any;
        }
    }

    boolean verify(VerificationObserver observer) throws IOException;

    @Override
    void close() throws IOException;

    boolean isClosed();

    void drop() throws IOException;
}
