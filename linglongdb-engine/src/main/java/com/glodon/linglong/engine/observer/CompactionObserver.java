package com.glodon.linglong.engine.observer;

import com.glodon.linglong.engine.core.Index;

/**
 * @author Stereo
 */
public class CompactionObserver {
    protected Index index;

    public boolean indexBegin(Index index) {
        this.index = index;
        return true;
    }

    public boolean indexComplete(Index index) {
        this.index = null;
        return true;
    }

    public boolean indexNodeVisited(long id) {
        return true;
    }
}
