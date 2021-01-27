package com.glodon.linglong.engine.event;

import java.io.PrintStream;

/**
 * @author Stereo
 */
public class EventPrinter implements EventListener {
    private final PrintStream mOut;

    public EventPrinter() {
        this(System.out);
    }

    public EventPrinter(PrintStream out) {
        if (out == null) {
            throw null;
        }
        mOut = out;
    }

    @Override
    public void notify(EventType type, String message, Object... args) {
        try {
            mOut.println(type.category + ": " + String.format(message, args));

            for (Object obj : args) {
                if (obj instanceof Throwable) {
                    ((Throwable) obj).printStackTrace(mOut);
                }
            }
        } catch (Throwable e) {
            // Ignore.
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof EventPrinter) {
            return mOut.equals(((EventPrinter) obj).mOut);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mOut.hashCode();
    }
}
