
package com.glodon.linglong.replication;

import com.glodon.linglong.base.common.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Checksum;

/**
 * @author Stereo
 */
class CheckedOutputStream extends OutputStream {
    private final OutputStream mDest;
    private final Checksum mChecksum;

    CheckedOutputStream(OutputStream dest, Checksum checksum) {
        mDest = dest;
        mChecksum = checksum;
    }

    @Override
    public void write(int b) throws IOException {
        mDest.write(b);
        mChecksum.update(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        mDest.write(buf, off, len);
        mChecksum.update(buf, off, len);
    }

    void writeChecksum() throws IOException {
        byte[] buf = new byte[4];
        Utils.encodeIntLE(buf, 0, (int) mChecksum.getValue());
        mDest.write(buf);
    }

    @Override
    public void close() throws IOException {
        mDest.close();
    }
}
