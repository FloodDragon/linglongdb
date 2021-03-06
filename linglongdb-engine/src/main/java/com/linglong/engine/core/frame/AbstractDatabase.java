package com.linglong.engine.core.frame;

import com.linglong.engine.event.EventListener;

import java.io.IOException;

/**
 * @author Stereo
 */
public abstract class AbstractDatabase implements Database {

    public abstract EventListener eventListener();

    public abstract void checkpoint(boolean force, long sizeThreshold, long delayThresholdNanos)
            throws IOException;
}
