package com.glodon.linglong.engine.event;

import com.glodon.linglong.engine.util.Utils;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Stereo
 */
public final class EventLogger implements EventListener {
    private final Logger mLogger;

    public EventLogger() {
        this(Logger.getGlobal());
    }

    public EventLogger(Logger logger) {
        if (logger == null) {
            throw null;
        }
        mLogger = logger;
    }

    @Override
    public void notify(EventType type, String message, Object... args) {
        try {
            if (mLogger.isLoggable(type.level)) {
                String msg = type.category + ": " + String.format(message, args);
                LogRecord record = new LogRecord(type.level, msg);
                record.setSourceClassName(null);
                record.setSourceMethodName(null);

                for (Object obj : args) {
                    if (obj instanceof Throwable) {
                        Throwable thrown = record.getThrown();
                        if (thrown != null) {
                            Utils.suppress(thrown, (Throwable) obj);
                        } else {
                            record.setThrown((Throwable) obj);
                        }
                    }
                }

                mLogger.log(record);
            }
        } catch (Throwable e) {
            // Ignore.
        }
    }
}
