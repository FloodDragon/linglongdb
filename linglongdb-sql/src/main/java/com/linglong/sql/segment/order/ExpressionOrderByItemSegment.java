package com.linglong.sql.segment.order;

import com.linglong.sql.util.OrderDirection;

/**
 * @author Stereo on 2019/10/11.
 */
public class ExpressionOrderByItemSegment extends OrderByItemSegment {
    private final String expression;

    public ExpressionOrderByItemSegment(int startIndex, int stopIndex, String expression, OrderDirection orderDirection) {
        super(startIndex, stopIndex, orderDirection);
        this.expression = expression;
    }

    @Override
    public String getText() {
        return expression;
    }
}