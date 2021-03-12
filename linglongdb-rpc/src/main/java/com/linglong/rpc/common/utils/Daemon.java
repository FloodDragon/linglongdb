package com.linglong.rpc.common.utils;

import java.util.concurrent.ThreadFactory;

/**
 * @author Stereo on 2018/4/10.
 */
public class Daemon extends Thread {
    {
        setDaemon(true);
    }

    Runnable runnable = null;

    public Daemon() {
        super();
    }

    public Daemon(Runnable runnable) {
        super(runnable);
        this.runnable = runnable;
        this.setName(runnable.toString());
    }

    public Daemon(ThreadGroup group, Runnable runnable) {
        super(group, runnable);
        this.runnable = runnable;
        this.setName(runnable.toString());
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public static class DaemonFactory extends Daemon implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            SecurityManager securityManager = System.getSecurityManager();
            ThreadGroup group = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
            Daemon daemon = new Daemon(group, runnable);
            if (daemon.getPriority() != Thread.NORM_PRIORITY) {
                daemon.setPriority(Thread.NORM_PRIORITY);
            }
            return daemon;
        }
    }
}