package com.glodon.linglong.base.common;

import com.glodon.linglong.base.exception.DatabaseException;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.lang.System.arraycopy;

/**
 * 常用工具
 *
 * @author Stereo
 */
public class Utils extends IOUtils {
    public static final byte[] EMPTY_BYTES = new byte[0];

    public static long toNanos(long timeout, TimeUnit unit) {
        return timeout < 0 ? -1 :
                (timeout == 0 ? 0 : (((timeout = unit.toNanos(timeout)) < 0) ? 0 : timeout));
    }

    public static int roundUpPower2(int i) {
        // Hacker's Delight figure 3-3.
        i--;
        i |= i >> 1;
        i |= i >> 2;
        i |= i >> 4;
        i |= i >> 8;
        return (i | (i >> 16)) + 1;
    }

    public static long roundUpPower2(long i) {
        i--;
        i |= i >> 1;
        i |= i >> 2;
        i |= i >> 4;
        i |= i >> 8;
        i |= i >> 16;
        return (i | (i >> 32)) + 1;
    }

    public static int randomSeed() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int seed;
        do {
            seed = rnd.nextInt();
        } while (seed == 0);
        return seed;
    }

    public static int nextRandom(int seed) {
        seed ^= seed << 13;
        seed ^= seed >>> 17;
        seed ^= seed << 5;
        return seed;
    }

    public static int nzHash(long v) {
        int h = hash64to32(v);
        // n is -1 if h is 0; n is 0 for all other cases
        int n = ((h & -h) - 1) >> 31;
        return h + n;
    }

    public static long scramble(long v) {
        v = (v << 21) - v - 1;
        v = v ^ (v >>> 24);
        v = (v + (v << 3)) + (v << 8); // v * 265
        v = v ^ (v >>> 14);
        v = (v + (v << 2)) + (v << 4); // v * 21
        v = v ^ (v >>> 28);
        return v + (v << 31);
    }

    public static int hash64to32(long v) {
        v = (v << 18) - v - 1; // (~v) + (v << 18)
        v = v ^ (v >>> 31);
        v = (v + (v << 2)) + (v << 4); // v * 21
        v = v ^ (v >>> 11);
        v = v + (v << 6);
        v = v ^ (v >>> 22);
        return (int) v;
    }

    /*
    public static long unscramble(long v) {
        // http://naml.us/blog/2012/03/inverse-of-a-hash-function

        long tmp;

        // Invert v = v + (v << 31)
        tmp = v - (v << 31);
        v = v - (tmp << 31);

        // Invert v = v ^ (v >>> 28)
        tmp = v ^ v >>> 28;
        v = v ^ tmp >>> 28;

        // Invert v *= 21
        //v *= 14933078535860113213u;
        v *= -3513665537849438403L;

        // Invert v = v ^ (v >>> 14)
        tmp = v ^ v >>> 14;
        tmp = v ^ tmp >>> 14;
        tmp = v ^ tmp >>> 14;
        v = v ^ tmp >>> 14;

        // Invert v *= 265
        //v *= 15244667743933553977u;
        v *= -3202076329775997639L;

        // Invert v = v ^ (v >>> 24)
        tmp = v ^ v >>> 24;
        v = v ^ tmp >>> 24;

        // Invert v = (~v) + (v << 21)
        tmp = ~v;
        tmp = ~(v - (tmp << 21));
        tmp = ~(v - (tmp << 21));
        v = ~(v - (tmp << 21));

        return v;
    }
    */

    public static TimeUnit inferUnit(TimeUnit unit, long value) {
        infer:
        {
            if (value == 0) break infer;
            if ((value - (value /= 1000) * 1000) != 0) break infer;
            unit = TimeUnit.MICROSECONDS;
            if ((value - (value /= 1000) * 1000) != 0) break infer;
            unit = TimeUnit.MILLISECONDS;
            if ((value - (value /= 1000) * 1000) != 0) break infer;
            unit = TimeUnit.SECONDS;
            if ((value - (value /= 60) * 60) != 0) break infer;
            unit = TimeUnit.MINUTES;
            if ((value - (value /= 60) * 60) != 0) break infer;
            unit = TimeUnit.HOURS;
            if ((value - (value / 24) * 24) != 0) break infer;
            unit = TimeUnit.DAYS;
        }

        return unit;
    }

    public static String timeoutMessage(long nanosTimeout, DatabaseException ex) {
        String msg;
        if (nanosTimeout == 0) {
            msg = "Never waited";
        } else if (nanosTimeout < 0) {
            msg = "Infinite wait";
        } else {
            StringBuilder b = new StringBuilder("Waited ");
            appendTimeout(b, ex.getTimeout(), ex.getUnit());
            Object att = ex.getOwnerAttachment();
            if (att != null) {
                appendAttachment(b, att);
            }
            return b.toString();
        }

        Object att = ex.getOwnerAttachment();
        if (att != null) {
            StringBuilder b = new StringBuilder(msg);
            appendAttachment(b, att);
            msg = b.toString();
        }

        return msg;
    }

    public static void appendTimeout(StringBuilder b, long timeout, TimeUnit unit) {
        if (timeout == 0) {
            b.append('0');
        } else if (timeout < 0) {
            b.append("infinite");
        } else {
            b.append(timeout);
            b.append(' ');
            String unitStr = unit.toString().toLowerCase();
            if (timeout == 1) {
                unitStr = unitStr.substring(0, unitStr.length() - 1);
            }
            b.append(unitStr);
        }
    }

    private static void appendAttachment(StringBuilder b, Object att) {
        b.append("; owner attachment: ").append(att);
    }

    public static byte[] cloneArray(byte[] bytes) {
        return (bytes == null || bytes.length == 0) ? bytes : bytes.clone();
    }

    public static void arrayCopyOrFill(byte[] src, int srcPos, byte[] dest, int destPos, int length) {
        if (src == null) {
            Arrays.fill(dest, destPos, destPos + length, (byte) 0);
        } else {
            arraycopy(src, srcPos, dest, destPos, length);
        }
    }


    public static void keyCheck(byte[] key) {
        if (key == null) {
            throw new NullPointerException("Key is null");
        }
    }

    public static byte[] midKey(byte[] low, byte[] high) {
        return midKey(low, 0, low.length, high, 0);
    }

    public static byte[] midKey(byte[] low, int lowOff, int lowLen, byte[] high, int highOff) {
        for (int i = 0; i < lowLen; i++) {
            byte lo = low[lowOff + i];
            byte hi = high[highOff + i];
            if (lo != hi) {
                byte[] mid = new byte[i + 1];
                System.arraycopy(low, lowOff, mid, 0, i);
                mid[i] = (byte) (((lo & 0xff) + (hi & 0xff) + 1) >> 1);
                return mid;
            }
        }
        byte[] mid = new byte[lowLen + 1];
        System.arraycopy(high, highOff, mid, 0, mid.length);
        return mid;
    }

    public static int decodeUnsignedVarInt(byte[] b, int offset) {
        int v = b[offset];
        if (v >= 0) {
            return v;
        }
        switch ((v >> 4) & 0x07) {
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
                return (1 << 7)
                        + (((v & 0x3f) << 8)
                        | (b[offset + 1] & 0xff));
            case 0x04:
            case 0x05:
                return ((1 << 14) + (1 << 7))
                        + (((v & 0x1f) << 16)
                        | ((b[++offset] & 0xff) << 8)
                        | (b[offset + 1] & 0xff));
            case 0x06:
                return ((1 << 21) + (1 << 14) + (1 << 7))
                        + (((v & 0x0f) << 24)
                        | ((b[++offset] & 0xff) << 16)
                        | ((b[++offset] & 0xff) << 8)
                        | (b[offset + 1] & 0xff));
            default:
                return ((1 << 28) + (1 << 21) + (1 << 14) + (1 << 7))
                        + ((b[++offset] << 24)
                        | ((b[++offset] & 0xff) << 16)
                        | ((b[++offset] & 0xff) << 8)
                        | (b[offset + 1] & 0xff));
        }
    }

    public static int decodeUnsignedVarInt(byte[] b, int start, int end) throws EOFException {
        if (start >= end) {
            throw new EOFException();
        }
        int v = b[start];
        if (v >= 0) {
            return v;
        }
        switch ((v >> 4) & 0x07) {
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
                if (++start >= end) {
                    throw new EOFException();
                }
                return (1 << 7)
                        + (((v & 0x3f) << 8)
                        | (b[start] & 0xff));
            case 0x04:
            case 0x05:
                if (start + 2 >= end) {
                    throw new EOFException();
                }
                return ((1 << 14) + (1 << 7))
                        + (((v & 0x1f) << 16)
                        | ((b[++start] & 0xff) << 8)
                        | (b[start + 1] & 0xff));
            case 0x06:
                if (start + 3 >= end) {
                    throw new EOFException();
                }
                return ((1 << 21) + (1 << 14) + (1 << 7))
                        + (((v & 0x0f) << 24)
                        | ((b[++start] & 0xff) << 16)
                        | ((b[++start] & 0xff) << 8)
                        | (b[start + 1] & 0xff));
            default:
                if (start + 4 >= end) {
                    throw new EOFException();
                }
                return ((1 << 28) + (1 << 21) + (1 << 14) + (1 << 7))
                        + ((b[++start] << 24)
                        | ((b[++start] & 0xff) << 16)
                        | ((b[++start] & 0xff) << 8)
                        | (b[start + 1] & 0xff));
        }
    }

    public static int decodeSignedVarInt(byte[] b, int offset) {
        int v = decodeUnsignedVarInt(b, offset);
        return ((v & 1) != 0) ? ((~(v >> 1)) | (1 << 31)) : (v >>> 1);
    }

    public static long decodeSignedVarLong(byte[] b, IntegerRef offsetRef) {
        long v = decodeUnsignedVarLong(b, offsetRef);
        return ((v & 1) != 0) ? ((~(v >> 1)) | (1L << 63)) : (v >>> 1);
    }

    public static long decodeUnsignedVarLong(byte[] b, IntegerRef offsetRef) {
        int offset = offsetRef.get();
        int val = b[offset++];
        if (val >= 0) {
            offsetRef.set(offset);
            return val;
        }
        long decoded;
        switch ((val >> 4) & 0x07) {
            case 0x00:
            case 0x01:
            case 0x02:
            case 0x03:
                decoded = (1L << 7) +
                        (((val & 0x3f) << 8)
                                | (b[offset++] & 0xff));
                break;
            case 0x04:
            case 0x05:
                decoded = ((1L << 14) + (1L << 7))
                        + (((val & 0x1f) << 16)
                        | ((b[offset++] & 0xff) << 8)
                        | (b[offset++] & 0xff));
                break;
            case 0x06:
                decoded = ((1L << 21) + (1L << 14) + (1L << 7))
                        + (((val & 0x0f) << 24)
                        | ((b[offset++] & 0xff) << 16)
                        | ((b[offset++] & 0xff) << 8)
                        | (b[offset++] & 0xff));
                break;
            default:
                switch (val & 0x0f) {
                    default:
                        decoded = ((1L << 28) + (1L << 21) + (1L << 14) + (1L << 7))
                                + (((val & 0x07L) << 32)
                                | (((long) (b[offset++] & 0xff)) << 24)
                                | (((long) (b[offset++] & 0xff)) << 16)
                                | (((long) (b[offset++] & 0xff)) << 8)
                                | ((long) (b[offset++] & 0xff)));
                        break;
                    case 0x08:
                    case 0x09:
                    case 0x0a:
                    case 0x0b:
                        decoded = ((1L << 35)
                                + (1L << 28) + (1L << 21) + (1L << 14) + (1L << 7))
                                + (((val & 0x03L) << 40)
                                | (((long) (b[offset++] & 0xff)) << 32)
                                | (((long) (b[offset++] & 0xff)) << 24)
                                | (((long) (b[offset++] & 0xff)) << 16)
                                | (((long) (b[offset++] & 0xff)) << 8)
                                | ((long) (b[offset++] & 0xff)));
                        break;
                    case 0x0c:
                    case 0x0d:
                        decoded = ((1L << 42) + (1L << 35)
                                + (1L << 28) + (1L << 21) + (1L << 14) + (1L << 7))
                                + (((val & 0x01L) << 48)
                                | (((long) (b[offset++] & 0xff)) << 40)
                                | (((long) (b[offset++] & 0xff)) << 32)
                                | (((long) (b[offset++] & 0xff)) << 24)
                                | (((long) (b[offset++] & 0xff)) << 16)
                                | (((long) (b[offset++] & 0xff)) << 8)
                                | ((long) (b[offset++] & 0xff)));
                        break;
                    case 0x0e:
                        decoded = ((1L << 49) + (1L << 42) + (1L << 35)
                                + (1L << 28) + (1L << 21) + (1L << 14) + (1L << 7))
                                + ((((long) (b[offset++] & 0xff)) << 48)
                                | (((long) (b[offset++] & 0xff)) << 40)
                                | (((long) (b[offset++] & 0xff)) << 32)
                                | (((long) (b[offset++] & 0xff)) << 24)
                                | (((long) (b[offset++] & 0xff)) << 16)
                                | (((long) (b[offset++] & 0xff)) << 8)
                                | ((long) (b[offset++] & 0xff)));
                        break;
                    case 0x0f:
                        decoded = ((1L << 56) + (1L << 49) + (1L << 42) + (1L << 35)
                                + (1L << 28) + (1L << 21) + (1L << 14) + (1L << 7))
                                + ((((long) b[offset++]) << 56)
                                | (((long) (b[offset++] & 0xff)) << 48)
                                | (((long) (b[offset++] & 0xff)) << 40)
                                | (((long) (b[offset++] & 0xff)) << 32)
                                | (((long) (b[offset++] & 0xff)) << 24)
                                | (((long) (b[offset++] & 0xff)) << 16)
                                | (((long) (b[offset++] & 0xff)) << 8L)
                                | ((long) (b[offset++] & 0xff)));
                        break;
                }
                break;
        }

        offsetRef.set(offset);
        return decoded;
    }

    public static int calcUnsignedVarIntLength(int v) {
        if (v < (1 << 7)) {
            return v < 0 ? 5 : 1;
        }
        v -= (1 << 7);
        if (v < (1 << 14)) {
            return 2;
        }
        v -= (1 << 14);
        if (v < (1 << 21)) {
            return 3;
        }
        v -= (1 << 21);
        if (v < (1 << 28)) {
            return 4;
        }
        return 5;
    }

    public static int encodeUnsignedVarInt(byte[] b, int offset, int v) {
        if (v < (1 << 7)) {
            if (v < 0) {
                v -= (1 << 28) + (1 << 21) + (1 << 14) + (1 << 7);
                b[offset++] = (byte) (0xff);
                b[offset++] = (byte) (v >> 24);
                b[offset++] = (byte) (v >> 16);
                b[offset++] = (byte) (v >> 8);
            }
        } else {
            v -= (1 << 7);
            if (v < (1 << 14)) {
                b[offset++] = (byte) (0x80 | (v >> 8));
            } else {
                v -= (1 << 14);
                if (v < (1 << 21)) {
                    b[offset++] = (byte) (0xc0 | (v >> 16));
                } else {
                    v -= (1 << 21);
                    if (v < (1 << 28)) {
                        b[offset++] = (byte) (0xe0 | (v >> 24));
                    } else {
                        v -= (1 << 28);
                        b[offset++] = (byte) (0xf0);
                        b[offset++] = (byte) (v >> 24);
                    }
                    b[offset++] = (byte) (v >> 16);
                }
                b[offset++] = (byte) (v >> 8);
            }
        }
        b[offset++] = (byte) v;
        return offset;
    }

    public static int convertSignedVarInt(int v) {
        if (v < 0) {
            // Complement negative value to turn all the ones to zeros, which
            // can be compacted. Shift and put sign bit at LSB.
            v = ((~v) << 1) | 1;
        } else {
            // Shift and put sign bit at LSB.
            v <<= 1;
        }
        return v;
    }

    public static int encodeSignedVarInt(byte[] b, int offset, int v) {
        return encodeUnsignedVarInt(b, offset, convertSignedVarInt(v));
    }

    public static long convertSignedVarLong(long v) {
        if (v < 0) {
            // Complement negative value to turn all the ones to zeros, which
            // can be compacted. Shift and put sign bit at LSB.
            v = ((~v) << 1) | 1;
        } else {
            // Shift and put sign bit at LSB.
            v <<= 1;
        }
        return v;
    }

    public static int encodeSignedVarLong(byte[] b, int offset, long v) {
        return encodeUnsignedVarLong(b, offset, convertSignedVarLong(v));
    }

    public static int calcUnsignedVarLongLength(long v) {
        if (v < (1L << 7)) {
            return v < 0 ? 9 : 1;
        }
        v -= (1L << 7);
        if (v < (1L << 14)) {
            return 2;
        }
        v -= (1L << 14);
        if (v < (1L << 21)) {
            return 3;
        }
        v -= (1L << 21);
        if (v < (1L << 28)) {
            return 4;
        }
        v -= (1L << 28);
        if (v < (1L << 35)) {
            return 5;
        }
        v -= (1L << 35);
        if (v < (1L << 42)) {
            return 6;
        }
        v -= (1L << 42);
        if (v < (1L << 49)) {
            return 7;
        }
        v -= (1L << 49);
        if (v < (1L << 56)) {
            return 8;
        }
        return 9;
    }

    public static int encodeUnsignedVarLong(byte[] b, int offset, long v) {
        if (v < (1L << 7)) {
            if (v < 0) {
                v -= (1L << 56) + (1L << 49) + (1L << 42) + (1L << 35)
                        + (1L << 28) + (1L << 21) + (1L << 14) + (1L << 7);
                b[offset++] = (byte) (0xff);
                b[offset++] = (byte) (v >> 56);
                b[offset++] = (byte) (v >> 48);
                b[offset++] = (byte) (v >> 40);
                b[offset++] = (byte) (v >> 32);
                b[offset++] = (byte) (v >> 24);
                b[offset++] = (byte) (v >> 16);
                b[offset++] = (byte) (v >> 8);
            }
        } else {
            v -= (1L << 7);
            if (v < (1L << 14)) {
                b[offset++] = (byte) (0x80 | (int) (v >> 8));
            } else {
                v -= (1L << 14);
                if (v < (1L << 21)) {
                    b[offset++] = (byte) (0xc0 | (int) (v >> 16));
                } else {
                    v -= (1L << 21);
                    if (v < (1L << 28)) {
                        b[offset++] = (byte) (0xe0 | (int) (v >> 24));
                    } else {
                        v -= (1L << 28);
                        if (v < (1L << 35)) {
                            b[offset++] = (byte) (0xf0 | (int) (v >> 32));
                        } else {
                            v -= (1L << 35);
                            if (v < (1L << 42)) {
                                b[offset++] = (byte) (0xf8 | (int) (v >> 40));
                            } else {
                                v -= (1L << 42);
                                if (v < (1L << 49)) {
                                    b[offset++] = (byte) (0xfc | (int) (v >> 48));
                                } else {
                                    v -= (1L << 49);
                                    if (v < (1L << 56)) {
                                        b[offset++] = (byte) (0xfe | (int) (v >> 56));
                                    } else {
                                        v -= (1L << 56);
                                        b[offset++] = (byte) (0xff);
                                        b[offset++] = (byte) (v >> 56);
                                    }
                                    b[offset++] = (byte) (v >> 48);
                                }
                                b[offset++] = (byte) (v >> 40);
                            }
                            b[offset++] = (byte) (v >> 32);
                        }
                        b[offset++] = (byte) (v >> 24);
                    }
                    b[offset++] = (byte) (v >> 16);
                }
                b[offset++] = (byte) (v >> 8);
            }
        }
        b[offset++] = (byte) v;
        return offset;
    }

    public static byte[] decrementReverseUnsignedVar(byte[] b, int offset) {
        int len = decodeReverseUnsignedLength(b, offset);
        decrement(b, offset, offset + len);
        if (len != decodeReverseUnsignedLength(b, offset)) {
            byte[] copy = new byte[b.length + 1];
            arraycopy(b, 0, copy, 0, b.length);
            copy[copy.length - 1] = (byte) 0xff;
            b = copy;
        }
        return b;
    }

    private static int decodeReverseUnsignedLength(byte[] b, int offset) {
        int g = 23;
        int h;
        while ((h = Integer.numberOfLeadingZeros(b[offset] & 0xff)) == 32) {
            g -= 8;
            offset++;
        }
        return h - g;
    }

    public static String toHex(byte[] key) {
        return key == null ? "null" : toHex(key, 0, key.length);
    }

    public static String toHex(byte[] key, int offset, int length) {
        if (key == null) {
            return "null";
        }
        char[] chars = new char[length << 1];
        int end = offset + length;
        for (int bi = offset, ci = 0; bi < end; bi++) {
            int b = key[bi] & 0xff;
            chars[ci++] = toHexChar(b >> 4);
            chars[ci++] = toHexChar(b & 0xf);
        }
        return new String(chars);
    }

    private static char toHexChar(int b) {
        return (char) ((b < 10) ? ('0' + b) : ('a' + b - 10));
    }

    public static String toHexDump(byte[] b) {
        return toHexDump(b, 0, b.length);
    }

    public static String toHexDump(byte[] b, int offset, int length) {
        StringBuilder bob = new StringBuilder();

        for (int i = 0; i < length; i += 16) {
            if (i > 0) {
                bob.append('\n');
            }

            String prefix = "0000000".concat(Integer.toHexString(i));
            prefix = prefix.substring(prefix.length() - 8);
            bob.append(prefix);
            bob.append(": ");

            for (int j = 0; j < 16; j += 2) {
                int pos = i + j;
                if (pos >= length - 1) {
                    if (pos >= length) {
                        bob.append("     ");
                    } else {
                        int v = b[offset + pos] & 0xff;
                        if (v < 0x10) {
                            bob.append('0');
                        }
                        bob.append(Integer.toHexString(v));
                        bob.append("   ");
                    }
                } else {
                    String pair = "000".concat
                            (Integer.toHexString(decodeUnsignedShortBE(b, offset + pos)));
                    pair = pair.substring(pair.length() - 4);
                    bob.append(pair);
                    bob.append(' ');
                }
            }

            bob.append(' ');

            for (int j = 0; j < 16; j++) {
                int pos = i + j;
                if (pos >= length) {
                    break;
                }
                char c = (char) (b[offset + pos] & 0xff);
                bob.append(Character.isISOControl(c) ? '.' : c);
            }
        }

        return bob.toString();
    }

    public static void deleteNumberedFiles(File baseFile, String pattern) throws IOException {
        deleteNumberedFiles(baseFile, pattern, 0);
    }

    public static void deleteNumberedFiles(File baseFile, String pattern, long min) throws IOException {
        File parentFile = baseFile.getParentFile();
        File[] files;
        if (parentFile == null || (files = parentFile.listFiles()) == null) {
            return;
        }

        String prefix = baseFile.getName() + pattern;

        for (File file : files) {
            String name = file.getName();
            if (name.startsWith(prefix)) {
                String suffix = name.substring(prefix.length());
                long num;
                try {
                    num = Long.parseLong(suffix);
                } catch (NumberFormatException e) {
                    continue;
                }
                if (num >= min) {
                    file.delete();
                }
            }
        }
    }

    public static void initCause(Throwable e, Throwable cause) {
        if (e != null && cause != null && !cycleCheck(e, cause) && !cycleCheck(cause, e)) {
            try {
                e.initCause(cause);
            } catch (Throwable e2) {
            }
        }
    }

    private static boolean cycleCheck(Throwable e, Throwable cause) {
        for (int i = 0; i < 100; i++) {
            if (e == cause) {
                return true;
            }
            e = e.getCause();
            if (e == null) {
                return false;
            }
        }
        // Cause chain is quite long, and so it probably has a cycle.
        return true;
    }

    public static void addLocalTrace(Throwable e) {
        String message = "--- thread transfer ---";

        StackTraceElement[] original = e.getStackTrace();
        StackTraceElement[] local = new Exception().getStackTrace();
        if (local.length == 0) {
            return;
        }

        StackTraceElement[] merged = new StackTraceElement[local.length + original.length];

        // Append original.
        System.arraycopy(original, 0, merged, 0, original.length);

        // Append separator.
        merged[original.length] = new StackTraceElement(message, "", null, -1);

        // Append local trace and omit this method.
        System.arraycopy(local, 1, merged, original.length + 1, local.length - 1);

        e.setStackTrace(merged);
    }

    public static String toMiniString(Object obj) {
        StringBuilder b = new StringBuilder();
        appendMiniString(b, obj);
        return b.toString();
    }

    public static void appendMiniString(StringBuilder b, Object obj) {
        if (obj == null) {
            b.append("null");
            return;
        }
        b.append(obj.getClass().getName()).append('@').append(Integer.toHexString(obj.hashCode()));
    }
}