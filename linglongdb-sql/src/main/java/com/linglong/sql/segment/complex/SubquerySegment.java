package com.linglong.sql.segment.complex;


import com.linglong.sql.segment.generic.TablesSegment;
import com.linglong.sql.segment.item.SelectItemsSegment;
import com.linglong.sql.segment.limit.LimitSegment;
import com.linglong.sql.segment.order.GroupBySegment;
import com.linglong.sql.segment.order.OrderBySegment;
import com.linglong.sql.segment.predicate.WhereSegment;

/**
 * @author Stereo on 2019/10/9.
 */
public class SubquerySegment extends SelectSegment {

    public SubquerySegment(SelectSegment selectSegment) {
        this(selectSegment.getStartIndex(), selectSegment.getStopIndex(), selectSegment.getSelectItemsSegment(), selectSegment.getTablesSegment(), selectSegment.getWhereSegment(), selectSegment.getGroupBySegment(), selectSegment.getOrderBySegment(), selectSegment.getLimitSegment());
    }

    public SubquerySegment(int startIndex, int stopIndex, SelectItemsSegment selectItemsSegment, TablesSegment tablesSegment, WhereSegment whereSegment, GroupBySegment groupBySegment, OrderBySegment orderBySegment, LimitSegment limitSegment) {
        super(startIndex, stopIndex, selectItemsSegment, tablesSegment, whereSegment, groupBySegment, orderBySegment, limitSegment);
    }
}
