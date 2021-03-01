package com.linglong.replication;

import com.linglong.base.common.Utils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Stereo
 */
final class OptionsEncoder extends EncodingOutputStream {
    OptionsEncoder() {
        super(64);
        count = 4;
    }

    @Override
    public void writeTo(OutputStream out) {
        try {
            if (mWriter != null) {
                mWriter.flush();
            }
            Utils.encodeIntLE(buf, 0, count);
            defaultWriteTo(out);
        } catch (IOException e) {
            // Not expected.
            throw Utils.rethrow(e);
        }
    }
}
