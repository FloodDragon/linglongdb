package com.linglong.server.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stereo on 2019/11/14.
 */
public abstract class Actor implements Runnable {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Actor.class);

    Thread thread;
    volatile boolean stopped = false;

    public void stop() throws InterruptedException {
        stopped = true;
        if (thread != null) {
            thread.interrupt();
            thread.join();
        }
    }

    @Override
    public void run() {
        thread = Thread.currentThread();
        while (!stopped && !Thread.currentThread().isInterrupted()) {
            try {
                doAct();
            } catch (InterruptedException e) {
                if (!stopped) {
                    LOGGER.info("NodeActor thread interrupted");
                }
                return;
            }
        }
    }

    protected abstract void doAct() throws InterruptedException;
}
