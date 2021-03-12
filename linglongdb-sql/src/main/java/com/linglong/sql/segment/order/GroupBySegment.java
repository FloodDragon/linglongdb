package com.linglong.sql.segment.order;

import com.linglong.sql.segment.LinglongSQLSegment;

import java.util.Collection;

/**
 * @author Stereo on 2019/10/11.
 */
public class GroupBySegment implements LinglongSQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final Collection<OrderByItemSegment> groupByItems;

    public GroupBySegment(int startIndex, int stopIndex, Collection<OrderByItemSegment> groupByItems) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.groupByItems = groupByItems;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    public Collection<OrderByItemSegment> getGroupByItems() {
        return groupByItems;
    }
}
