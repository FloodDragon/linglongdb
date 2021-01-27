package com.glodon.linglong.engine.event;

import java.util.logging.Level;

/**
 * 监听器接收有关正在执行的操作的通知数据库。
 *
 * @author Stereo
 */
public interface EventListener {
    /**
     * @param message event message format
     * @param args    arguments for message
     */
    void notify(EventType type, String message, Object... args);

    default boolean isObserved(EventType type) {
        return isObserved(type.category) && isObserved(type.level);
    }

    default boolean isObserved(EventType.Category category) {
        return true;
    }

    default boolean isObserved(Level level) {
        return true;
    }

    /**
     * Returns a filtered listener which only observes the given event categories.
     */
    default EventListener observe(EventType.Category... categories) {
        return AllowEventListener.make(this, categories);
    }

    /**
     * Returns a filtered listener which only observes the given event levels.
     */
    default EventListener observe(Level... levels) {
        return AllowEventListener.make(this, levels);
    }

    /**
     * Returns a filtered listener which never observes the given event categories.
     */
    default EventListener ignore(EventType.Category... categories) {
        return DisallowEventListener.make(this, categories);
    }

    /**
     * Returns a filtered listener which never observes the given event levels.
     */
    default EventListener ignore(Level... levels) {
        return DisallowEventListener.make(this, levels);
    }
}
