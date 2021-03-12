package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.CollectionSQLSegmentExtractor;
import com.linglong.sql.segment.order.ColumnOrderByItemSegment;
import com.linglong.sql.segment.order.ExpressionOrderByItemSegment;
import com.linglong.sql.segment.order.IndexOrderByItemSegment;
import com.linglong.sql.segment.order.OrderByItemSegment;
import com.linglong.sql.segment.predicate.ColumnWhereSegment;
import com.linglong.sql.util.ExtractorUtils;
import com.linglong.sql.util.OrderDirection;
import com.linglong.sql.util.RuleName;
import com.linglong.sql.util.SQLUtil;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Stereo on 2019/10/10.
 */
public final class OrderByItemExtractor implements CollectionSQLSegmentExtractor {

    private final ColumnExtractor columnExtractor = new ColumnExtractor();

    public Collection<OrderByItemSegment> extract(final ParserRuleContext ancestorNode) {
        Collection<OrderByItemSegment> result = new LinkedList<>();
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.ORDER_BY_ITEM)) {
            OrderDirection orderDirection = 2 == each.getChildCount() && OrderDirection.DESC.name().equalsIgnoreCase(each.getChild(1).getText()) ? OrderDirection.DESC : OrderDirection.ASC;
            Optional<ParserRuleContext> indexNode = ExtractorUtils.findFirstChildNode(each, RuleName.NUMBER_LITERALS);
            if (indexNode.isPresent()) {
                result.add(new IndexOrderByItemSegment(indexNode.get().getStart().getStartIndex(), indexNode.get().getStop().getStopIndex(),
                        String.valueOf(SQLUtil.getExactlyNumber(indexNode.get().getText(), 10).intValue()), orderDirection));
                continue;
            }
            Optional<ParserRuleContext> expressionNode = ExtractorUtils.findFirstChildNode(each, RuleName.EXPR);
            if (expressionNode.isPresent()) {
                result.add(new ExpressionOrderByItemSegment(expressionNode.get().getStart().getStartIndex(), expressionNode.get().getStop().getStopIndex(),
                        expressionNode.get().getText(), orderDirection));
                continue;
            }
            Optional<ColumnWhereSegment> columnSegment = columnExtractor.extract(each);
            if (columnSegment.isPresent()) {
                result.add(new ColumnOrderByItemSegment(columnSegment.get().getStartIndex(), columnSegment.get().getStopIndex(), columnSegment.get(), orderDirection));
            }
        }
        return result;
    }
}
