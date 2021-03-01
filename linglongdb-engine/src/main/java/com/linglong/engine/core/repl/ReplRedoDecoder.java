package com.linglong.engine.core.repl;

import com.linglong.base.concurrent.Latch;
import com.linglong.engine.core.tx.DataIn;
import com.linglong.engine.core.tx.RedoDecoder;
import com.linglong.engine.extend.ReplicationManager;

import java.io.IOException;

/**
 * @author Stereo
 */
final public class ReplRedoDecoder extends RedoDecoder {
    volatile boolean mDeactivated;

    public ReplRedoDecoder(ReplicationManager manager,
                           long initialPosition, long initialTxnId,
                           Latch decodeLatch) {
        super(false, initialTxnId, new In(initialPosition, manager), decodeLatch);
    }

    @Override
    public boolean verifyTerminator(DataIn in) {
        return true;
    }

    static final class In extends DataIn {
        private final ReplicationManager mManager;

        In(long position, ReplicationManager manager) {
            this(position, manager, 64 << 10);
        }

        In(long position, ReplicationManager manager, int bufferSize) {
            super(position, bufferSize);
            mManager = manager;
        }

        @Override
        public int doRead(byte[] buf, int off, int len) throws IOException {
            return mManager.read(buf, off, len);
        }

        @Override
        public void close() throws IOException {
        }
    }
}
