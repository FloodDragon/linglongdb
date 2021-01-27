package com.glodon.linglong.replication;

import java.io.IOException;

/**
 * @author Stereo
 */
public interface MessageReplicator extends DirectReplicator {
    static MessageReplicator open(ReplicatorConfig config) throws IOException {
        return new MessageStreamReplicator(StreamReplicator.open(config));
    }

    @Override
    Reader newReader(long index, boolean follow);

    @Override
    Writer newWriter();

    @Override
    Writer newWriter(long index);

    interface Reader extends DirectReplicator.Reader {
        byte[] readMessage() throws IOException;

        int readMessage(byte[] buf, int offset, int length) throws IOException;
    }

    interface Writer extends DirectReplicator.Writer {
        default boolean writeMessage(byte[] message) throws IOException {
            return writeMessage(message, 0, message.length, true);
        }

        default boolean writeMessage(byte[] message, int offset, int length) throws IOException {
            return writeMessage(message, offset, length, true);
        }

        boolean writeMessage(byte[] message, int offset, int length, boolean finished)
                throws IOException;
    }
}
