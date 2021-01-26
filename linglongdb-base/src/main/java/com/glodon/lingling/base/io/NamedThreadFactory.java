package com.glodon.lingling.base.io;

import java.util.concurrent.ThreadFactory;

/**
 * @author Stereo
 */
final class NamedThreadFactory implements ThreadFactory {
    private static int cThreadCounter;

    private final String mPrefix;
    private final ThreadGroup mGroup;

    NamedThreadFactory(String prefix) {
        mPrefix = prefix == null ? "Thread" : prefix;
        SecurityManager sm = System.getSecurityManager();
        mGroup = (sm != null) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        int num;
        synchronized (NamedThreadFactory.class) {
            num = ++cThreadCounter;
        }
        Thread t = new Thread(mGroup, r, mPrefix + '-' + (num & 0xffffffffL));
        t.setDaemon(true);
        return t;
    }
}
