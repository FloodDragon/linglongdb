package com.linglong.rpc.server.skeleton.liveliness;

public class MonotonicClock implements Clock {
    public long getTime() {
        final long NANOSECONDS_PER_MILLISECOND = 1000000;
        return System.nanoTime() / NANOSECONDS_PER_MILLISECOND;
    }
}
