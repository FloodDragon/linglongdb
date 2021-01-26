package com.glodon.linglong.base.io;

import com.glodon.linglong.base.util.UnsafeAccess;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * java.nio.DirectByteBuffer的访问封装
 *
 * @author Stereo
 */
@SuppressWarnings("restriction")
public class DirectAccess {
    private static final sun.misc.Unsafe UNSAFE = UnsafeAccess.tryObtain();

    private static final Class<?> cDirectByteBufferClass;
    static final long cDirectAddressOffset;
    private static final long cDirectCapacityOffset;
    private static final ThreadLocal<ByteBuffer> cLocalBuffer;
    private static final ThreadLocal<ByteBuffer> cLocalBuffer2;

    static {
        Class<?> clazz;
        long addrOffset, capOffset;
        ThreadLocal<ByteBuffer> local;
        ThreadLocal<ByteBuffer> local2;

        try {
            clazz = Class.forName("java.nio.DirectByteBuffer");

            addrOffset = UNSAFE.objectFieldOffset(Buffer.class.getDeclaredField("address"));
            capOffset = UNSAFE.objectFieldOffset(Buffer.class.getDeclaredField("capacity"));

            local = new ThreadLocal<>();
            local2 = new ThreadLocal<>();
        } catch (Exception e) {
            clazz = null;
            addrOffset = 0;
            capOffset = 0;
            local = null;
            local2 = null;
        }

        cDirectByteBufferClass = clazz;
        cDirectAddressOffset = addrOffset;
        cDirectCapacityOffset = capOffset;
        cLocalBuffer = local;
        cLocalBuffer2 = local2;
    }

    private final ThreadLocal<ByteBuffer> mLocalBuffer;

    public DirectAccess() {
        if (!isSupported()) {
            throw new UnsupportedOperationException();
        }
        mLocalBuffer = new ThreadLocal<>();
    }

    public ByteBuffer prepare(long ptr, int length) {
        return ref(mLocalBuffer, ptr, length);
    }

    public static boolean isSupported() {
        return cLocalBuffer2 != null;
    }

    public static ByteBuffer ref(long ptr, int length) {
        return ref(cLocalBuffer, ptr, length);
    }

    public static ByteBuffer ref2(long ptr, int length) {
        return ref(cLocalBuffer2, ptr, length);
    }

    public static long getAddress(Buffer buf) {
        if (!buf.isDirect()) {
            throw new IllegalArgumentException("Not a direct buffer");
        }
        try {
            return UNSAFE.getLong(buf, cDirectAddressOffset);
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static ByteBuffer ref(ThreadLocal<ByteBuffer> local, long ptr, int length) {
        if (local == null) {
            throw new UnsupportedOperationException();
        }

        ByteBuffer bb = local.get();

        try {
            if (bb == null) {
                bb = (ByteBuffer) UNSAFE.allocateInstance(cDirectByteBufferClass);
                bb.clear();
                local.set(bb);
            }
            UNSAFE.putLong(bb, cDirectAddressOffset, ptr);
            UNSAFE.putInt(bb, cDirectCapacityOffset, length);
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }

        bb.position(0).limit(length);

        return bb;
    }

    public static void unref(ByteBuffer bb) {
        bb.position(0).limit(0);
        try {
            UNSAFE.putInt(bb, cDirectCapacityOffset, 0);
            UNSAFE.putLong(bb, cDirectAddressOffset, 0);
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
