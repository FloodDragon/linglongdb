package com.glodon.linglong.replication;
import com.glodon.linglong.base.common.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Stereo
 */
class EncodingOutputStream extends ByteArrayOutputStream {
    protected Writer mWriter;

    EncodingOutputStream() {
    }

    EncodingOutputStream(int size) {
        super(size);
    }

    public void encodeIntLE(int value) {
        int pos = count;
        if (pos + 4 > buf.length) {
            buf = Arrays.copyOf(buf, buf.length << 1);
        }
        Utils.encodeIntLE(buf, pos, value);
        count = pos + 4;
    }

    public void encodeLongLE(long value) {
        int pos = count;
        if (pos + 8 > buf.length) {
            buf = Arrays.copyOf(buf, buf.length << 1);
        }
        Utils.encodeLongLE(buf, pos, value);
        count = pos + 8;
    }

    public void encodeStr(String str) {
        try {
            Writer writer = mWriter;
            if (writer == null) {
                mWriter = writer = new OutputStreamWriter(this, StandardCharsets.UTF_8);
            }
            encodeIntLE(str.length());
            writer.write(str);
            writer.flush();
        } catch (IOException e) {
            // Not expected.
            throw Utils.rethrow(e);
        }
    }

    public void encodeMap(Map<String, String> map) {
        encodeIntLE(map.size());
        for (Map.Entry<String, String> e : map.entrySet()) {
            encodeStr(e.getKey());
            encodeStr(e.getValue());
        }
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        if (mWriter != null) {
            mWriter.flush();
        }
        super.writeTo(out);
    }

    protected final void defaultWriteTo(OutputStream out) throws IOException {
        super.writeTo(out);
    }
}
