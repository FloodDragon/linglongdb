package com.linglong.io;

import com.linglong.base.common.IOUtils;
import com.linglong.base.common.UnsafeAccess;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.Checksum;

/**
 * 摘自Netty工具
 *
 * @author Stereo
 */
public class CRC32C {
    private static final MethodHandle INSTANCE_CTOR;
    private static final MethodHandle UPDATE_BYTE_BUFFER;

    static {
        MethodHandle ctor = null;
        MethodType voidType = MethodType.methodType(void.class);

        Class clazz;
        try {
            clazz = Class.forName("java.util.zip.CRC32C");
            ctor = MethodHandles.publicLookup().findConstructor(clazz, voidType);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            // Use default implementation.
            clazz = Impl.class;
        }

        if (ctor == null) {
            try {
                ctor = MethodHandles.lookup().findConstructor(clazz, voidType);
            } catch (Throwable e) {
                throw IOUtils.rethrow(e);
            }
        }

        INSTANCE_CTOR = ctor;

        try {
            UPDATE_BYTE_BUFFER = MethodHandles.lookup().findVirtual
                    (clazz, "update", MethodType.methodType(void.class, ByteBuffer.class));
        } catch (Throwable e) {
            throw IOUtils.rethrow(e);
        }
    }

    public static Checksum newInstance() {
        try {
            return (Checksum) INSTANCE_CTOR.invoke();
        } catch (Throwable e) {
            throw IOUtils.rethrow(e);
        }
    }

    public static void update(Checksum crc, ByteBuffer buffer) {
        try {
            UPDATE_BYTE_BUFFER.invoke(crc, buffer);
        } catch (Throwable e) {
            throw IOUtils.rethrow(e);
        }
    }

    private CRC32C() {
    }

    static class Impl implements Checksum {
        private static final int CRC32C_POLY = 0x1EDC6F41;
        private static final int REVERSED_CRC32C_POLY = Integer.reverse(CRC32C_POLY);

        private static final sun.misc.Unsafe UNSAFE = UnsafeAccess.tryObtain();

        private static final int ADDRESS_SIZE;
        private static final int ARRAY_BYTE_BASE_OFFSET;
        private static final int ARRAY_BYTE_INDEX_SCALE;

        private static final int[] byteTable;
        private static final int[][] byteTables = new int[8][256];
        private static final int[] byteTable0 = byteTables[0];
        private static final int[] byteTable1 = byteTables[1];
        private static final int[] byteTable2 = byteTables[2];
        private static final int[] byteTable3 = byteTables[3];
        private static final int[] byteTable4 = byteTables[4];
        private static final int[] byteTable5 = byteTables[5];
        private static final int[] byteTable6 = byteTables[6];
        private static final int[] byteTable7 = byteTables[7];

        static {
            ADDRESS_SIZE = UNSAFE.addressSize();
            ARRAY_BYTE_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
            ARRAY_BYTE_INDEX_SCALE = UNSAFE.arrayIndexScale(byte[].class);

            for (int index = 0; index < byteTables[0].length; index++) {
                int r = index;
                for (int i = 0; i < Byte.SIZE; i++) {
                    if ((r & 1) != 0) {
                        r = (r >>> 1) ^ REVERSED_CRC32C_POLY;
                    } else {
                        r >>>= 1;
                    }
                }
                byteTables[0][index] = r;
            }

            for (int index = 0; index < byteTables[0].length; index++) {
                int r = byteTables[0][index];

                for (int k = 1; k < byteTables.length; k++) {
                    r = byteTables[0][r & 0xFF] ^ (r >>> 8);
                    byteTables[k][index] = r;
                }
            }

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                byteTable = byteTables[0];
            } else { // ByteOrder.BIG_ENDIAN
                byteTable = new int[byteTable0.length];
                System.arraycopy(byteTable0, 0, byteTable, 0, byteTable0.length);
                for (int[] table : byteTables) {
                    for (int index = 0; index < table.length; index++) {
                        table[index] = Integer.reverseBytes(table[index]);
                    }
                }
            }
        }

        private int crc = 0xFFFFFFFF;

        Impl() {
        }

        @Override
        public void update(int b) {
            crc = (crc >>> 8) ^ byteTable[(crc ^ (b & 0xFF)) & 0xFF];
        }

        @Override
        public void update(byte[] b, int off, int len) {
            if (b == null) {
                throw new NullPointerException();
            }
            if (off < 0 || len < 0 || off > b.length - len) {
                throw new ArrayIndexOutOfBoundsException();
            }
            crc = updateBytes(crc, b, off, (off + len));
        }

        public void update(ByteBuffer buffer) {
            int pos = buffer.position();
            int limit = buffer.limit();
            assert (pos <= limit);
            int rem = limit - pos;
            if (rem <= 0) {
                return;
            }

            if (buffer.isDirect()) {
                long address;
                try {
                    address = UNSAFE.getLong(buffer, DirectAccess.cDirectAddressOffset);
                    crc = updateDirectByteBuffer(crc, address, pos, limit);
                } catch (Exception e) {
                    throw new UnsupportedOperationException(e);
                }
            } else if (buffer.hasArray()) {
                crc = updateBytes(crc, buffer.array(), pos + buffer.arrayOffset(),
                        limit + buffer.arrayOffset());
            } else {
                byte[] b = new byte[Math.min(buffer.remaining(), 4096)];
                while (buffer.hasRemaining()) {
                    int length = Math.min(buffer.remaining(), b.length);
                    buffer.get(b, 0, length);
                    update(b, 0, length);
                }
            }
            buffer.position(limit);
        }

        @Override
        public void reset() {
            crc = 0xFFFFFFFF;
        }

        @Override
        public long getValue() {
            return (~crc) & 0xFFFFFFFFL;
        }

        private static int updateBytes(int crc, byte[] b, int off, int end) {

            if (end - off >= 8 && ARRAY_BYTE_INDEX_SCALE == 1) {

                int alignLength
                        = (8 - ((ARRAY_BYTE_BASE_OFFSET + off) & 0x7)) & 0x7;
                for (int alignEnd = off + alignLength; off < alignEnd; off++) {
                    crc = (crc >>> 8) ^ byteTable[(crc ^ b[off]) & 0xFF];
                }

                if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
                    crc = Integer.reverseBytes(crc);
                }

                for (; off < (end - Long.BYTES); off += Long.BYTES) {
                    int firstHalf;
                    int secondHalf;
                    if (ADDRESS_SIZE == 4) {
                        firstHalf = UNSAFE.getInt(b, (long) ARRAY_BYTE_BASE_OFFSET + off);
                        secondHalf = UNSAFE.getInt(b, (long) ARRAY_BYTE_BASE_OFFSET + off
                                + Integer.BYTES);
                    } else {
                        long value = UNSAFE.getLong(b, (long) ARRAY_BYTE_BASE_OFFSET + off);
                        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                            firstHalf = (int) value;
                            secondHalf = (int) (value >>> 32);
                        } else { // ByteOrder.BIG_ENDIAN
                            firstHalf = (int) (value >>> 32);
                            secondHalf = (int) value;
                        }
                    }
                    crc ^= firstHalf;
                    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                        crc = byteTable7[crc & 0xFF]
                                ^ byteTable6[(crc >>> 8) & 0xFF]
                                ^ byteTable5[(crc >>> 16) & 0xFF]
                                ^ byteTable4[crc >>> 24]
                                ^ byteTable3[secondHalf & 0xFF]
                                ^ byteTable2[(secondHalf >>> 8) & 0xFF]
                                ^ byteTable1[(secondHalf >>> 16) & 0xFF]
                                ^ byteTable0[secondHalf >>> 24];
                    } else { // ByteOrder.BIG_ENDIAN
                        crc = byteTable0[secondHalf & 0xFF]
                                ^ byteTable1[(secondHalf >>> 8) & 0xFF]
                                ^ byteTable2[(secondHalf >>> 16) & 0xFF]
                                ^ byteTable3[secondHalf >>> 24]
                                ^ byteTable4[crc & 0xFF]
                                ^ byteTable5[(crc >>> 8) & 0xFF]
                                ^ byteTable6[(crc >>> 16) & 0xFF]
                                ^ byteTable7[crc >>> 24];
                    }
                }

                if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
                    crc = Integer.reverseBytes(crc);
                }
            }

            for (; off < end; off++) {
                crc = (crc >>> 8) ^ byteTable[(crc ^ b[off]) & 0xFF];
            }

            return crc;
        }

        private static int updateDirectByteBuffer(int crc, long address,
                                                  int off, int end) {

            if (end - off >= 8) {

                int alignLength = (8 - (int) ((address + off) & 0x7)) & 0x7;
                for (int alignEnd = off + alignLength; off < alignEnd; off++) {
                    crc = (crc >>> 8)
                            ^ byteTable[(crc ^ UNSAFE.getByte(address + off)) & 0xFF];
                }

                if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
                    crc = Integer.reverseBytes(crc);
                }

                for (; off <= (end - Long.BYTES); off += Long.BYTES) {
                    int firstHalf = UNSAFE.getInt(address + off);
                    int secondHalf = UNSAFE.getInt(address + off + Integer.BYTES);
                    crc ^= firstHalf;
                    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                        crc = byteTable7[crc & 0xFF]
                                ^ byteTable6[(crc >>> 8) & 0xFF]
                                ^ byteTable5[(crc >>> 16) & 0xFF]
                                ^ byteTable4[crc >>> 24]
                                ^ byteTable3[secondHalf & 0xFF]
                                ^ byteTable2[(secondHalf >>> 8) & 0xFF]
                                ^ byteTable1[(secondHalf >>> 16) & 0xFF]
                                ^ byteTable0[secondHalf >>> 24];
                    } else { // ByteOrder.BIG_ENDIAN
                        crc = byteTable0[secondHalf & 0xFF]
                                ^ byteTable1[(secondHalf >>> 8) & 0xFF]
                                ^ byteTable2[(secondHalf >>> 16) & 0xFF]
                                ^ byteTable3[secondHalf >>> 24]
                                ^ byteTable4[crc & 0xFF]
                                ^ byteTable5[(crc >>> 8) & 0xFF]
                                ^ byteTable6[(crc >>> 16) & 0xFF]
                                ^ byteTable7[crc >>> 24];
                    }
                }

                if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
                    crc = Integer.reverseBytes(crc);
                }
            }

            // Tail
            for (; off < end; off++) {
                crc = (crc >>> 8)
                        ^ byteTable[(crc ^ UNSAFE.getByte(address + off)) & 0xFF];
            }

            return crc;
        }
    }
}
