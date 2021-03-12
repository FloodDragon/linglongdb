package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.order.OrderBySegment;
import com.linglong.sql.util.ExtractorUtils;
import com.linglong.sql.util.RuleName;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo on 2019/10/11.
 */
public final class OrderByExtractor implements OptionalSQLSegmentExtractor {

    private final OrderByItemExtractor orderByItemExtractor = new OrderByItemExtractor();

    @Override
    public final Optional<OrderBySegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> orderByNode = ExtractorUtils.findFirstChildNodeNoneRecursive(ancestorNode, RuleName.ORDER_BY_CLAUSE);
        return orderByNode.isPresent() ? Optional.of(new OrderBySegment(orderByNode.get().getStart().getStartIndex(), orderByNode.get().getStop().getStopIndex(),
                orderByItemExtractor.extract(orderByNode.get()))) : Optional.absent();
    }
}
