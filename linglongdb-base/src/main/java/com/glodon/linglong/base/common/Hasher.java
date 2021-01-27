package com.glodon.linglong.base.common;

import java.nio.ByteOrder;

/**
 * 快速的非加密哈希函数
 *
 * @author Stereo
 */
public class Hasher {
    private static final Hasher INSTANCE;

    static {
        Hasher instance = null;

        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            String arch = System.getProperty("os.arch");
            if (arch.equals("i386") || arch.equals("x86")
                    || arch.equals("amd64") || arch.equals("x86_64")) {
                try {
                    instance = new UnsafeLE();
                } catch (Throwable e) {
                    // Not allowed.
                }
            }
        }

        INSTANCE = instance == null ? new Hasher() : instance;
    }

    public static long hash(long hash, byte[] b) {
        return INSTANCE.doHash(hash, b);
    }

    @SuppressWarnings("fallthrough")
    long doHash(long hash, byte[] b) {
        int len = b.length;
        hash ^= len;

        if (len < 8) {
            long v = 0;
            switch (len) {
                case 7:
                    v = (v << 8) | (b[6] & 0xffL);
                case 6:
                    v = (v << 8) | (b[5] & 0xffL);
                case 5:
                    v = (v << 8) | (b[4] & 0xffL);
                case 4:
                    v = (v << 8) | (b[3] & 0xffL);
                case 3:
                    v = (v << 8) | (b[2] & 0xffL);
                case 2:
                    v = (v << 8) | (b[1] & 0xffL);
                case 1:
                    v = (v << 8) | (b[0] & 0xffL);
            }
            hash = (hash << 5) - hash ^ Utils.scramble(v);
            return hash;
        }

        int end = len & ~7;
        int i = 0;

        while (i < end) {
            hash = (hash << 5) - hash ^ Utils.scramble(Utils.decodeLongLE(b, i));
            i += 8;
        }

        if ((len & 7) != 0) {
            hash = (hash << 5) - hash ^ Utils.scramble(Utils.decodeLongLE(b, len - 8));
        }

        return hash;
    }

    @SuppressWarnings("restriction")
    private static class UnsafeLE extends Hasher {
        private static final sun.misc.Unsafe UNSAFE;
        private static final long BYTE_ARRAY_OFFSET;

        static {
            try {
                UNSAFE = UnsafeAccess.tryObtain();
                BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
            } catch (Throwable e) {
                throw new ExceptionInInitializerError();
            }
        }

        @Override
        @SuppressWarnings("fallthrough")
        long doHash(long hash, byte[] b) {
            int len = b.length;
            hash ^= len;

            if (len < 8) {
                long v = 0;
                switch (len) {
                    case 7:
                        v = (v << 8) | (b[6] & 0xffL);
                    case 6:
                        v = (v << 8) | (b[5] & 0xffL);
                    case 5:
                        v = (v << 8) | (b[4] & 0xffL);
                    case 4:
                        v = (v << 8) | (b[3] & 0xffL);
                    case 3:
                        v = (v << 8) | (b[2] & 0xffL);
                    case 2:
                        v = (v << 8) | (b[1] & 0xffL);
                    case 1:
                        v = (v << 8) | (b[0] & 0xffL);
                }
                hash = ((hash << 5) - hash) ^ Utils.scramble(v);
                return hash;
            }

            int end = len & ~7;
            int i = 0;

            while (i < end) {
                hash = ((hash << 5) - hash) ^
                        Utils.scramble(UNSAFE.getLong(b, BYTE_ARRAY_OFFSET + i));
                i += 8;
            }

            if ((len & 7) != 0) {
                hash = ((hash << 5) - hash) ^
                        Utils.scramble(UNSAFE.getLong(b, BYTE_ARRAY_OFFSET + len - 8));
            }

            return hash;
        }
    }
}
