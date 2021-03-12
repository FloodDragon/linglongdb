package com.linglong.sql.statement;

import com.linglong.sql.segment.complex.SelectSegment;
import com.linglong.sql.segment.generic.TablesSegment;
import com.linglong.sql.segment.item.SelectItemsSegment;
import com.linglong.sql.segment.limit.LimitSegment;
import com.linglong.sql.segment.order.GroupBySegment;
import com.linglong.sql.segment.order.OrderBySegment;
import com.linglong.sql.segment.predicate.WhereSegment;

/**
 * @author Stereo
 */
public class SelectStatement extends AbstractStatement {

    private final SelectSegment selectSegment;

    public SelectStatement(SelectSegment selectSegment) {
        this.selectSegment = selectSegment;
    }

    public SelectItemsSegment getSelectItemsSegment() {
        return selectSegment.getSelectItemsSegment();
    }

    public TablesSegment getTablesSegment() {
        return selectSegment.getTablesSegment();
    }

    public WhereSegment getWhereSegment() {
        return selectSegment.getWhereSegment();
    }

    public GroupBySegment getGroupBySegment() {
        return selectSegment.getGroupBySegment();
    }

    public OrderBySegment getOrderBySegment() {
        return selectSegment.getOrderBySegment();
    }

    public LimitSegment getLimitSegment() {
        return selectSegment.getLimitSegment();
    }

    public SelectSegment getSelectSegment() {
        return selectSegment;
    }
}
