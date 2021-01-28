package com.glodon.linglong.engine.observer;

import com.glodon.linglong.engine.core.Index;

/**
 * @author Stereo
 */
public class VerificationObserver {
    protected Index index;
    protected int height;

    boolean failed;

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean indexBegin(Index index, int height) {
        this.index = index;
        this.height = height;
        return true;
    }

    public boolean indexComplete(Index index, boolean passed, String message) {
        this.index = null;
        this.height = 0;
        return true;
    }

    public boolean indexNodePassed(long id, int level,
                                   int entryCount, int freeBytes, int largeValueCount) {
        return true;
    }

    public boolean indexNodeFailed(long id, int level, String message) {
        StringBuilder b = new StringBuilder("Verification failure: index=");

        Index index = this.index;
        if (index == null) {
            b.append("null");
        } else {
            b.append(index.getId());
        }

        b.append(", node=").append(id).append(", level=").append(level)
                .append(": ").append(message);

        reportFailure(b.toString());

        return true;
    }

    protected void reportFailure(String message) {
        System.out.println(message);
    }
}
