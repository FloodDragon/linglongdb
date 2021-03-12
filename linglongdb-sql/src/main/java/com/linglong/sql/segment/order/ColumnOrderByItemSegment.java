package com.linglong.sql.segment.order;

import com.linglong.sql.segment.predicate.ColumnWhereSegment;
import com.linglong.sql.util.OrderDirection;

/**
 * @author Stereo on 2019/10/11.
 */
public class ColumnOrderByItemSegment extends OrderByItemSegment {

    private final ColumnWhereSegment columnWhereSegment;

    public ColumnOrderByItemSegment(int startIndex, int stopIndex, ColumnWhereSegment columnWhereSegment, OrderDirection orderDirection) {
        super(startIndex, stopIndex, orderDirection);
        this.columnWhereSegment = columnWhereSegment;
    }

    public ColumnWhereSegment getColumnWhereSegment() {
        return columnWhereSegment;
    }

    @Override
    public String getText() {
        return columnWhereSegment.getName();
    }
}
