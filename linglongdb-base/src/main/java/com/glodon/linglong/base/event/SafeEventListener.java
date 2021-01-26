package com.glodon.linglong.base.event;

import java.util.logging.Level;

/**
 * @author Stereo
 */
class SafeEventListener implements EventListener {
    static EventListener makeSafe(EventListener listener) {
        return (listener == null
                || listener instanceof SafeEventListener
                || listener.getClass() == EventLogger.class
                || listener.getClass() == EventPrinter.class) ? listener
                : new SafeEventListener(listener);
    }

    final EventListener mListener;

    SafeEventListener(EventListener listener) {
        mListener = listener;
    }

    @Override
    public void notify(EventType type, String message, Object... args) {
        try {
            if (shouldNotify(type)) {
                mListener.notify(type, message, args);
            }
        } catch (Throwable e) {
            // Ignore.
        }
    }

    @Override
    public boolean isObserved(EventType type) {
        return mListener.isObserved(type);
    }

    @Override
    public boolean isObserved(EventType.Category category) {
        return mListener.isObserved(category);
    }

    @Override
    public boolean isObserved(Level level) {
        return mListener.isObserved(level);
    }

    boolean shouldNotify(EventType type) {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == SafeEventListener.class) {
            return mListener.equals(((SafeEventListener) obj).mListener);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mListener.hashCode() + getClass().hashCode();
    }
}
