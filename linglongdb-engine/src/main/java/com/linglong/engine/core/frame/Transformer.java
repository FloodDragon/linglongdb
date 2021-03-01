package com.linglong.engine.core.frame;

import com.linglong.base.common.Ordering;
import com.linglong.base.common.Utils;
import com.linglong.base.exception.ViewConstraintException;

import java.io.IOException;
import java.util.Comparator;

/**
 * @author Stereo
 */
@FunctionalInterface
public interface Transformer {
    default Boolean requireValue() {
        return Boolean.TRUE;
    }

    byte[] transformValue(byte[] value, byte[] key, byte[] tkey)
            throws IOException;

    default byte[] transformValue(Cursor cursor, byte[] tkey) throws IOException {
        byte[] value = cursor.value();
        if (value == Cursor.NOT_LOADED && requireValue() != Boolean.FALSE) {
            cursor.load();
            value = cursor.value();
        }
        return transformValue(value, cursor.key(), tkey);
    }

    default byte[] inverseTransformValue(byte[] tvalue, byte[] key, byte[] tkey)
            throws ViewConstraintException, IOException {
        throw new ViewConstraintException("Inverse transform isn't supported");
    }

    default byte[] transformKey(Cursor cursor) throws IOException {
        return cursor.key();
    }

    default byte[] inverseTransformKey(byte[] tkey) {
        return tkey;
    }

    default byte[] inverseTransformKeyGt(byte[] tkey) {
        tkey = tkey.clone();
        return Utils.increment(tkey, 0, tkey.length) ? inverseTransformKey(tkey) : null;
    }

    default byte[] inverseTransformKeyLt(byte[] tkey) {
        tkey = tkey.clone();
        return Utils.decrement(tkey, 0, tkey.length) ? inverseTransformKey(tkey) : null;
    }

    default Ordering transformedOrdering(Ordering original) {
        return original;
    }

    default Comparator<byte[]> transformedComparator(Comparator<byte[]> original) {
        return original;
    }
}
