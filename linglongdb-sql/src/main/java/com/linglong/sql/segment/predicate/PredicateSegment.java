package com.linglong.sql.segment.predicate;

import com.linglong.sql.segment.LinglongSQLSegment;

/**
 * @author Stereo on 2019/10/9.
 */
public class PredicateSegment implements LinglongSQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final ColumnWhereSegment column;

    private final PredicateRightValue rightValue;

    public PredicateSegment(int startIndex, int stopIndex, ColumnWhereSegment column, PredicateRightValue rightValue) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.column = column;
        this.rightValue = rightValue;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    public ColumnWhereSegment getColumn() {
        return column;
    }

    public PredicateRightValue getRightValue() {
        return rightValue;
    }
}
