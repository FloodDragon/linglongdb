package com.glodon.linglong.replication;

import com.glodon.linglong.base.common.Utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Stereo
 */
final class OptionsDecoder extends DecodingInputStream {
    OptionsDecoder(InputStream in) throws IOException {
        super(decode(in));
    }

    private static byte[] decode(InputStream in) throws IOException {
        byte[] buf = new byte[4];
        Utils.readFully(in, buf, 0, buf.length);
        buf = new byte[Utils.decodeIntLE(buf, 0) - 4];
        Utils.readFully(in, buf, 0, buf.length);
        return buf;
    }
}
