package com.linglong.base.common;

import java.lang.reflect.Field;

/**
 * JDK Unsafe Access
 *
 * @author Stereo
 */
@SuppressWarnings("restriction")
public class UnsafeAccess {
    private static final sun.misc.Unsafe UNSAFE;
    private static final Throwable UNSUPPORTED;

    static {
        sun.misc.Unsafe unsafe = null;
        Throwable unsupported = null;

        try {
            Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (sun.misc.Unsafe) theUnsafe.get(null);
        } catch (Throwable e) {
            unsupported = e;
        }

        UNSAFE = unsafe;
        UNSUPPORTED = unsupported;
    }

    public static sun.misc.Unsafe tryObtain() {
        return UNSAFE;
    }

    public static sun.misc.Unsafe obtain() throws UnsupportedOperationException {
        sun.misc.Unsafe u = UNSAFE;
        if (u == null) {
            throw new UnsupportedOperationException(UNSUPPORTED);
        }
        return u;
    }
}
