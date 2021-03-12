package com.linglong.sql.segment.complex;

import com.linglong.sql.segment.LinglongSQLSegment;
import com.linglong.sql.segment.generic.TablesSegment;
import com.linglong.sql.segment.item.SelectItemsSegment;
import com.linglong.sql.segment.limit.LimitSegment;
import com.linglong.sql.segment.order.GroupBySegment;
import com.linglong.sql.segment.order.OrderBySegment;
import com.linglong.sql.segment.predicate.WhereSegment;

/**
 * @author Stereo on 2019/10/11.
 */
public class SelectSegment implements LinglongSQLSegment {
    private final int startIndex;

    private final int stopIndex;

    private final SelectItemsSegment selectItemsSegment;

    private final TablesSegment tablesSegment;

    private final WhereSegment whereSegment;

    private final GroupBySegment groupBySegment;

    private final OrderBySegment orderBySegment;

    private final LimitSegment limitSegment;

    public SelectSegment(int startIndex,
                         int stopIndex,
                         SelectItemsSegment selectItemsSegment,
                         TablesSegment tablesSegment,
                         WhereSegment whereSegment,
                         GroupBySegment groupBySegment,
                         OrderBySegment orderBySegment,
                         LimitSegment limitSegment) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.selectItemsSegment = selectItemsSegment;
        this.tablesSegment = tablesSegment;
        this.whereSegment = whereSegment;
        this.groupBySegment = groupBySegment;
        this.orderBySegment = orderBySegment;
        this.limitSegment = limitSegment;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    public SelectItemsSegment getSelectItemsSegment() {
        return selectItemsSegment;
    }

    public TablesSegment getTablesSegment() {
        return tablesSegment;
    }

    public WhereSegment getWhereSegment() {
        return whereSegment;
    }

    public GroupBySegment getGroupBySegment() {
        return groupBySegment;
    }

    public OrderBySegment getOrderBySegment() {
        return orderBySegment;
    }

    public LimitSegment getLimitSegment() {
        return limitSegment;
    }
}
