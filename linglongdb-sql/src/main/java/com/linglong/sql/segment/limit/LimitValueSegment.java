package com.linglong.sql.segment.limit;

import com.linglong.sql.segment.LinglongSQLSegment;

/**
 * @author Stereo on 2019/10/11.
 */
public class LimitValueSegment implements LinglongSQLSegment {

    private final long value;

    private final int startIndex;

    private final int stopIndex;

    public LimitValueSegment(int startIndex, int stopIndex, long value) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.value = value;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    public long getValue() {
        return value;
    }
}
