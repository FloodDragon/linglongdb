package com.linglong.sql.segment.item;

import com.linglong.sql.segment.predicate.ExpressionSegment;

/**
 * @author Stereo on 2019/10/9.
 */
public class LiteralExpressionSegment implements ExpressionSegment {

    private final int startIndex;

    private final int stopIndex;

    private final Object literals;

    public LiteralExpressionSegment(int startIndex, int stopIndex, Object literals) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.literals = literals;
    }


    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    public Object getLiterals() {
        return literals;
    }
}
