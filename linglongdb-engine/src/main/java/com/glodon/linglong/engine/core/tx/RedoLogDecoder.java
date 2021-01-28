package com.glodon.linglong.engine.core.tx;

import java.io.EOFException;
import java.io.IOException;

/**
 * @author Stereo
 */
final class RedoLogDecoder extends RedoDecoder {
    private final RedoLog mLog;
    private final EventListener mListener;

    RedoLogDecoder(RedoLog log, DataIn in, EventListener listener) {
        super(true, 0, in, new Latch());
        mLog = log;
        mListener = listener;
    }

    @Override
    boolean verifyTerminator(DataIn in) throws IOException {
        try {
            int term = in.readIntLE();
            if (term == mLog.nextTermRnd() || term == Utils.nzHash(mTxnId)) {
                return true;
            }
            if (mListener != null) {
                mListener.notify(EventType.RECOVERY_REDO_LOG_CORRUPTION,
                        "Invalid message terminator");
            }
            return false;
        } catch (EOFException e) {
            if (mListener != null) {
                mListener.notify(EventType.RECOVERY_REDO_LOG_CORRUPTION,
                        "Unexpected end of file");
            }
            return false;
        }
    }
}
