
package com.linglong.engine.extend;

import com.linglong.base.exception.ConfirmationFailureException;
import com.linglong.engine.core.frame.Database;
import com.linglong.engine.core.frame.Index;
import com.linglong.engine.event.EventListener;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Stereo
 */
public interface ReplicationManager extends Closeable {
    long encoding();

    default InputStream restoreRequest(EventListener listener) throws IOException {
        return null;
    }

    void start(long position) throws IOException;

    default void ready(Accessor accessor) throws IOException {
    }

    interface Accessor extends EventListener {
        Database database();

        long control(byte[] message) throws IOException;
    }

    long readPosition();

    int read(byte[] b, int off, int len) throws IOException;

    Writer writer() throws IOException;

    interface Writer {

        long position();

        long confirmedPosition();

        boolean leaderNotify(Runnable callback);

        boolean write(byte[] b, int off, int len, long commitPos) throws IOException;

        default boolean confirm(long commitPos) throws IOException {
            return confirm(commitPos, -1);
        }

        boolean confirm(long commitPos, long timeoutNanos) throws IOException;

        default long confirmEnd() throws ConfirmationFailureException {
            return confirmEnd(-1);
        }

        long confirmEnd(long timeoutNanos) throws ConfirmationFailureException;
    }

    void sync() throws IOException;

    default void syncConfirm(long position) throws IOException {
        syncConfirm(position, -1);
    }

    void syncConfirm(long position, long timeoutNanos) throws IOException;

    void checkpointed(long position) throws IOException;

    default void control(long position, byte[] message) throws IOException {
    }

    default void notifyRename(Index index, byte[] oldName, byte[] newName) {
    }

    default void notifyDrop(Index index) {
    }

    //boolean forward(byte[] b, int off, int len) throws IOException;
}
