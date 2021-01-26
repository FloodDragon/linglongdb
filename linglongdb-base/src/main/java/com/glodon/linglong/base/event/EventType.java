package com.glodon.linglong.base.event;

import java.util.logging.Level;

/**
 * 监听事件类型
 *
 * @author Stereo
 */
public enum EventType {
    DEBUG(Category.DEBUG, Level.FINE),

    CACHE_INIT_BEGIN(Category.CACHE_INIT, Level.INFO),

    CACHE_INIT_COMPLETE(Category.CACHE_INIT, Level.INFO),

    RECOVERY_BEGIN(Category.RECOVERY, Level.INFO),

    RECOVERY_CACHE_PRIMING(Category.RECOVERY, Level.INFO),

    RECOVERY_PROGRESS(Category.RECOVERY, Level.INFO),

    RECOVERY_LOAD_UNDO_LOGS(Category.RECOVERY, Level.INFO),

    RECOVERY_APPLY_REDO_LOG(Category.RECOVERY, Level.INFO),

    RECOVERY_REDO_LOG_CORRUPTION(Category.RECOVERY, Level.WARNING),

    RECOVERY_PROCESS_REMAINING(Category.RECOVERY, Level.INFO),

    RECOVERY_NO_HANDLER(Category.RECOVERY, Level.SEVERE),

    RECOVERY_HANDLER_UNCAUGHT(Category.RECOVERY, Level.SEVERE),

    RECOVERY_DELETE_FRAGMENTS(Category.RECOVERY, Level.INFO),

    RECOVERY_COMPLETE(Category.RECOVERY, Level.INFO),

    DELETION_BEGIN(Category.DELETION, Level.INFO),

    DELETION_FAILED(Category.DELETION, Level.WARNING),

    DELETION_COMPLETE(Category.DELETION, Level.INFO),

    REPLICATION_RESTORE(Category.REPLICATION, Level.INFO),

    REPLICATION_DEBUG(Category.REPLICATION, Level.FINE),

    REPLICATION_INFO(Category.REPLICATION, Level.INFO),

    REPLICATION_WARNING(Category.REPLICATION, Level.WARNING),

    REPLICATION_PANIC(Category.REPLICATION, Level.SEVERE),

    CHECKPOINT_BEGIN(Category.CHECKPOINT, Level.INFO),

    CHECKPOINT_FLUSH(Category.CHECKPOINT, Level.INFO),

    CHECKPOINT_SYNC(Category.CHECKPOINT, Level.INFO),

    CHECKPOINT_FAILED(Category.CHECKPOINT, Level.WARNING),

    CHECKPOINT_COMPLETE(Category.CHECKPOINT, Level.INFO),

    PANIC_UNHANDLED_EXCEPTION(Category.PANIC, Level.SEVERE);

    public final Category category;
    public final Level level;

    EventType(Category category, Level level) {
        this.category = category;
        this.level = level;
    }

    /**
     * 事件类别
     */
    public enum Category {
        DEBUG,

        CACHE_INIT,

        RECOVERY,

        DELETION,

        REPLICATION,

        CHECKPOINT,

        PANIC;
    }
}
