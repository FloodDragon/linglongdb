package com.linglong.engine.event;

import java.util.function.BiConsumer;
import java.util.logging.Level;

/**
 * @author Stereo
 */
public final class ReplicationEventListener implements BiConsumer<Level, String> {
    private final EventListener mListener;

    public ReplicationEventListener(EventListener listener) {
        mListener = listener;
    }

    @Override
    public void accept(Level level, String message) {
        EventType type;
        int value = level.intValue();

        if (value <= Level.FINE.intValue()) {
            type = EventType.REPLICATION_DEBUG;
        } else if (value <= Level.INFO.intValue()) {
            type = EventType.REPLICATION_INFO;
        } else if (value <= Level.WARNING.intValue()) {
            type = EventType.REPLICATION_WARNING;
        } else if (value < Level.OFF.intValue()) {
            type = EventType.REPLICATION_PANIC;
        } else {
            return;
        }

        mListener.notify(type, message);
    }
}
