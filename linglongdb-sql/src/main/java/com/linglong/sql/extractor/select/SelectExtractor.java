package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.complex.SelectSegment;
import com.linglong.sql.segment.generic.TablesSegment;
import com.linglong.sql.segment.item.SelectItemsSegment;
import com.linglong.sql.segment.limit.LimitSegment;
import com.linglong.sql.segment.order.GroupBySegment;
import com.linglong.sql.segment.order.OrderBySegment;
import com.linglong.sql.segment.predicate.WhereSegment;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo on 2019/10/11.
 */
public final class SelectExtractor implements OptionalSQLSegmentExtractor {
    private final SelectItemsExtractor selectItemsExtractor = new SelectItemsExtractor();
    private final TableExtractor tableExtractor = new TableExtractor();
    private final WhereExtractor whereExtractor = new WhereExtractor();
    private final GroupByExtractor groupByExtractor = new GroupByExtractor();
    private final OrderByExtractor orderByExtractor = new OrderByExtractor();
    private final LimitExtractor limitExtractor = new LimitExtractor();

    @Override
    public Optional<SelectSegment> extract(ParserRuleContext ancestorNode) {

        //select items
        Optional<SelectItemsSegment> selectItemsSegmentOptional = selectItemsExtractor.extract(ancestorNode);
        SelectItemsSegment selectItemsSegment = null;
        if (selectItemsSegmentOptional.isPresent()) {
            selectItemsSegment = selectItemsSegmentOptional.get();
        }

        //table /sub query
        TablesSegment tablesSegment = null;
        Optional<TablesSegment> tablesSegmentOptional = tableExtractor.extract(ancestorNode);
        if (tablesSegmentOptional.isPresent()) {
            tablesSegment = tablesSegmentOptional.get();
        }

        //where
        WhereSegment whereSegment = null;
        Optional<WhereSegment> whereSegmentOptional = whereExtractor.extract(ancestorNode);
        if (whereSegmentOptional.isPresent()) {
            whereSegment = whereSegmentOptional.get();
        }

        //group by
        GroupBySegment groupBySegment = null;
        Optional<GroupBySegment> groupBySegmentOptional = groupByExtractor.extract(ancestorNode);
        if (groupBySegmentOptional.isPresent()) {
            groupBySegment = groupBySegmentOptional.get();
        }

        //order by
        OrderBySegment orderBySegment = null;
        Optional<OrderBySegment> orderBySegmentOptional = orderByExtractor.extract(ancestorNode);
        if (orderBySegmentOptional.isPresent()) {
            orderBySegment = orderBySegmentOptional.get();
        }

        //limit
        LimitSegment limitSegment = null;
        Optional<LimitSegment> limitSegmentOptional = limitExtractor.extract(ancestorNode);
        if (limitSegmentOptional.isPresent()) {
            limitSegment = limitSegmentOptional.get();
        }
        return Optional.of(new SelectSegment(ancestorNode.getStart().getStartIndex(), ancestorNode.getStop().getStopIndex(), selectItemsSegment, tablesSegment, whereSegment, groupBySegment, orderBySegment, limitSegment));
    }
}
