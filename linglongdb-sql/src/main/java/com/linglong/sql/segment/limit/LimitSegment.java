package com.linglong.sql.segment.limit;

import com.linglong.sql.segment.LinglongSQLSegment;

/**
 * @author Stereo on 2019/10/11.
 */
public class LimitSegment implements LinglongSQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final LimitValueSegment offset;

    private final LimitValueSegment rowCount;

    public LimitSegment(int startIndex, int stopIndex, LimitValueSegment offset, LimitValueSegment rowCount) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.offset = offset;
        this.rowCount = rowCount;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    public LimitValueSegment getOffset() {
        return offset;
    }

    public LimitValueSegment getRowCount() {
        return rowCount;
    }
}
