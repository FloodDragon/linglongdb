package com.linglong.server.database.process;

import com.linglong.base.concurrent.RWLock;
import com.linglong.engine.core.frame.Index;

/**
 * Created by liuj-ai on 2021/4/27.
 */
public class _IndexEntry {
    private final Index index;
    private final RWLock rwLock = new RWLock();

    _IndexEntry(Index index) {
        this.index = index;
    }

    void sharedLock() {
        rwLock.acquireShared();
    }

    void sharedUnLock() {
        rwLock.releaseShared();
    }

    void exclusiveLock() {
        rwLock.acquireExclusive();
    }

    void exclusiveUnLock() {
        rwLock.releaseExclusive();
    }

    Index getIndex() {
        return index;
    }
}
