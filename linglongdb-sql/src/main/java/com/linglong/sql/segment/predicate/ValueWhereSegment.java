package com.linglong.sql.segment.predicate;

import com.linglong.sql.segment.LinglongSQLSegment;

/**
 * @author Stereo on 2019/10/9.
 */
public class ValueWhereSegment implements LinglongSQLSegment, PredicateRightValue {

    private final String operator;

    private final ExpressionSegment expression;

    public ValueWhereSegment(String operator, ExpressionSegment expression) {
        this.operator = operator;
        this.expression = expression;
    }

    @Override
    public int getStartIndex() {
        return 0;
    }

    @Override
    public int getStopIndex() {
        return 0;
    }

    public String getOperator() {
        return operator;
    }

    public ExpressionSegment getExpression() {
        return expression;
    }
}
