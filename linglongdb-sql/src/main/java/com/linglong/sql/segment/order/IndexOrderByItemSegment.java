package com.linglong.sql.segment.order;

import com.linglong.sql.util.OrderDirection;

/**
 * @author Stereo on 2019/10/11.
 */
public class IndexOrderByItemSegment extends OrderByItemSegment {

    private final String columnIndex;

    public IndexOrderByItemSegment(int startIndex, int stopIndex, String columnIndex, OrderDirection orderDirection) {
        super(startIndex, stopIndex, orderDirection);
        this.columnIndex = columnIndex;
    }

    @Override
    public String getText() {
        return columnIndex;
    }
}
