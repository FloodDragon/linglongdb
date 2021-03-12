package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.order.GroupBySegment;
import com.linglong.sql.util.ExtractorUtils;
import com.linglong.sql.util.RuleName;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo on 2019/10/11.
 */
public final class GroupByExtractor implements OptionalSQLSegmentExtractor {

    private final OrderByItemExtractor orderByItemExtractor = new OrderByItemExtractor();

    @Override
    public Optional<GroupBySegment> extract(ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> groupByNode = ExtractorUtils.findFirstChildNodeNoneRecursive(ancestorNode, RuleName.GROUP_BY_CLAUSE);
        return groupByNode.isPresent() ? Optional.of(
                new GroupBySegment(groupByNode.get().getStart().getStartIndex(), groupByNode.get().getStop().getStopIndex(), orderByItemExtractor.extract(groupByNode.get())))
                : Optional.absent();
    }
}
