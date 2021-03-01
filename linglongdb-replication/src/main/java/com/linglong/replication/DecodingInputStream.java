package com.linglong.replication;

import com.linglong.base.common.Utils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Stereo
 */
class DecodingInputStream extends ByteArrayInputStream {
    DecodingInputStream(byte[] data) {
        super(data);
    }

    public int decodeIntLE() {
        int value = Utils.decodeIntLE(buf, pos);
        pos += 4;
        return value;
    }

    public long decodeLongLE() {
        long value = Utils.decodeLongLE(buf, pos);
        pos += 8;
        return value;
    }

    public String decodeStr() {
        int len = decodeIntLE();
        byte[] bytes = Arrays.copyOfRange(buf, pos, pos += len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public Map<String, String> decodeMap() {
        int size = decodeIntLE();
        if (size == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>();
        while (--size >= 0) {
            map.put(decodeStr(), decodeStr());
        }
        return map;
    }
}
