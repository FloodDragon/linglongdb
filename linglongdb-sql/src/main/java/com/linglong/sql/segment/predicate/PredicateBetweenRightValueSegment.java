package com.linglong.sql.segment.predicate;

import com.linglong.sql.segment.LinglongSQLSegment;

/**
 * @author Stereo on 2021/3/12.
 */
public class PredicateBetweenRightValueSegment implements LinglongSQLSegment, PredicateRightValue {

    private final ExpressionSegment betweenExpression;

    private final ExpressionSegment andExpression;

    public PredicateBetweenRightValueSegment(ExpressionSegment betweenExpression, ExpressionSegment andExpression) {
        this.betweenExpression = betweenExpression;
        this.andExpression = andExpression;
    }

    public ExpressionSegment getBetweenExpression() {
        return betweenExpression;
    }

    public ExpressionSegment getAndExpression() {
        return andExpression;
    }

    @Override
    public int getStartIndex() {
        return 0;
    }

    @Override
    public int getStopIndex() {
        return 0;
    }
}
