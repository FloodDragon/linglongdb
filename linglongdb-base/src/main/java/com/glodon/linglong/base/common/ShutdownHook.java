package com.glodon.linglong.base.common;

import java.lang.ref.WeakReference;

/**
 * @author Stereo
 */
public interface ShutdownHook {
    void shutdown();

    abstract class Weak<A> extends WeakReference<A> implements ShutdownHook {
        public Weak(A obj) {
            super(obj);
        }

        @Override
        public final void shutdown() {
            A obj = get();
            if (obj != null) {
                doShutdown(obj);
            }
        }

        public abstract void doShutdown(A obj);
    }
}
