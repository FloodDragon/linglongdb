package com.linglong.sql.segment.order;

import com.linglong.sql.segment.LinglongSQLSegment;

import java.util.Collection;

/**
 * @author Stereo on 2019/10/11.
 */
public class OrderBySegment implements LinglongSQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final Collection<OrderByItemSegment> orderByItems;

    public OrderBySegment(int startIndex, int stopIndex, Collection<OrderByItemSegment> orderByItems) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.orderByItems = orderByItems;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    public Collection<OrderByItemSegment> getOrderByItems() {
        return orderByItems;
    }
}
