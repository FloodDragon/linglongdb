package com.glodon.linglong.engine.core;

import com.glodon.linglong.engine.event.EventListener;

import java.io.IOException;

/**
 *
 * @author Stereo
 */
abstract class AbstractDatabase implements Database {
    /**
     * @return null if none
     */
    abstract EventListener eventListener();

    /**
     * Called by Checkpointer task.
     */
    abstract void checkpoint(boolean force, long sizeThreshold, long delayThresholdNanos)
        throws IOException;
}
