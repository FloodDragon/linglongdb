package com.glodon.linglong.engine.core.repl;

import com.glodon.linglong.base.common.Utils;
import com.glodon.linglong.engine.core.LocalDatabase;
import com.glodon.linglong.engine.core.tx.PendingTxn;
import com.glodon.linglong.engine.event.EventListener;
import com.glodon.linglong.engine.event.EventType;

import java.io.IOException;

/**
 * @author Stereo
 */
public final class PendingTxnWaiter extends Thread {
    private static final int TIMEOUT_MILLIS = 60000;

    static final int PENDING = 1, DO_COMMIT = 2, DO_ROLLBACK = 3, EXITED = 4;

    private final ReplRedoWriter mWriter;

    private PendingTxn mBehind;
    private PendingTxn mAhead;

    private long mFlipPos;
    private boolean mExited;

    public PendingTxnWaiter(ReplRedoWriter writer) {
        mFlipPos = -1;
        mWriter = writer;
    }

    public synchronized int add(PendingTxn pending) {
        long flipPos = mFlipPos;
        if (flipPos >= 0) {
            return pending.getCommitPos() <= flipPos ? DO_COMMIT : DO_ROLLBACK;
        }

        if (mExited) {
            return EXITED;
        }

        PendingTxn behind = mBehind;
        if (behind == null) {
            mBehind = pending;
            notify();
        } else {
            long commitPos = pending.getCommitPos();
            if (commitPos <= behind.getCommitPos()) {
                pending.setPrev(behind.getPrev());
                behind.setPrev(pending);
            } else {
                PendingTxn ahead = mAhead;
                if (ahead != null && commitPos <= ahead.getCommitPos()) {
                    pending.setPrev(ahead.getPrev());
                    ahead.setPrev(pending);
                } else {
                    pending.setPrev(ahead);
                    mAhead = pending;
                }
            }
        }
        return PENDING;
    }

    public synchronized void flipped(long commitPos) {
        if (commitPos < 0) {
            throw new IllegalArgumentException();
        }
        mFlipPos = commitPos;
    }

    void finishAll() {
        long commitPos;
        PendingTxn behind, ahead;
        synchronized (this) {
            commitPos = mFlipPos;
            if (commitPos < 0) {
                throw new IllegalStateException();
            }
            behind = mBehind;
            mBehind = null;
            ahead = mAhead;
            mAhead = null;
            notify();
        }
        LocalDatabase db = mWriter.mEngine.mDatabase;
        finishAll(behind, db, commitPos);
        finishAll(ahead, db, commitPos);
    }

    @Override
    public void run() {
        try {
            doRun();
        } catch (Throwable e) {
            synchronized (this) {
                mExited = true;
            }
            throw e;
        }
    }

    private void doRun() {
        while (true) {
            PendingTxn behind;
            synchronized (this) {
                if ((behind = mBehind) == null) {
                    try {
                        wait(TIMEOUT_MILLIS);
                    } catch (InterruptedException e) {
                    }
                    if ((behind = mBehind) == null) {
                        mExited = true;
                        return;
                    }
                }
            }

            if (!mWriter.confirm(behind)) {
                return;
            }

            synchronized (this) {
                if (mBehind == null) {
                    mExited = true;
                    return;
                }
                mBehind = mAhead;
                mAhead = null;
            }

            LocalDatabase db = mWriter.mEngine.mDatabase;
            do {
                try {
                    behind.commit(db);
                } catch (IOException e) {
                    uncaught(db, e);
                }
            } while ((behind = behind.getPrev()) != null);
        }
    }

    private static void finishAll(PendingTxn pending, LocalDatabase db, long commitPos) {
        while (pending != null) {
            try {
                if (pending.getCommitPos() <= commitPos) {
                    pending.commit(db);
                } else {
                    pending.rollback(db);
                }
            } catch (IOException e) {
                uncaught(db, e);
            }
            pending = pending.getPrev();
        }
    }

    private static void uncaught(LocalDatabase db, Throwable e) {
        EventListener listener = db.eventListener();
        if (listener != null) {
            listener.notify(EventType.REPLICATION_PANIC,
                    "Unexpected transaction exception: %1$s", e);
        } else {
            Utils.uncaught(e);
        }
    }
}
