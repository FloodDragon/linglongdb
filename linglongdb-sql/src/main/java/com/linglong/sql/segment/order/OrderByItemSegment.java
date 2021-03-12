package com.linglong.sql.segment.order;

import com.linglong.sql.segment.LinglongSQLSegment;
import com.linglong.sql.util.OrderDirection;

/**
 * @author Stereo on 2019/10/10.
 */
public abstract class OrderByItemSegment implements LinglongSQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final OrderDirection orderDirection;

    public OrderByItemSegment(int startIndex, int stopIndex, OrderDirection orderDirection) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.orderDirection = orderDirection;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }


    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    public OrderDirection getOrderDirection() {
        return orderDirection;
    }

    public abstract String getText();
}
