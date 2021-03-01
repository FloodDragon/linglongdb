package com.linglong.replication;

import java.util.function.LongConsumer;

/**
 * @author Stereo
 */
abstract class LogWriter extends LogInfo implements StreamReplicator.Writer {
    abstract long prevTerm();

    @Override
    public void uponCommit(long index, LongConsumer task) {
        uponCommit(new Delayed(index) {
            @Override
            protected void doRun(long counter) {
                task.accept(counter);
            }
        });
    }

    abstract void uponCommit(Delayed task);

    abstract void release();
}
