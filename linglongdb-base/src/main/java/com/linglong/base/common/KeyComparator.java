package com.linglong.base.common;

import java.util.Comparator;

/**
 * @author Stereo
 */
public final class KeyComparator implements Comparator<byte[]> {
    public static final KeyComparator THE = new KeyComparator();

    private KeyComparator() {
    }

    @Override
    public int compare(byte[] a, byte[] b) {
        return Utils.compareUnsigned(a, 0, a.length, b, 0, b.length);
    }
}
