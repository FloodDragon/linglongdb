package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.limit.LimitSegment;
import com.linglong.sql.segment.limit.LimitValueSegment;
import com.linglong.sql.util.ExtractorUtils;
import com.linglong.sql.util.RuleName;
import com.linglong.sql.util.SQLUtil;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo on 2019/10/11.
 */
public final class LimitExtractor implements OptionalSQLSegmentExtractor {
    @Override
    public Optional<LimitSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> limitNode = ExtractorUtils.findFirstChildNodeNoneRecursive(ancestorNode, RuleName.LIMIT_CLAUSE);
        return limitNode.isPresent()
                ? Optional.of(new LimitSegment(limitNode.get().getStart().getStartIndex(), limitNode.get().getStop().getStopIndex(),
                extractOffset(limitNode.get()).orNull(), extractRowCount(limitNode.get()).orNull())) : Optional.<LimitSegment>absent();
    }

    private Optional<LimitValueSegment> extractOffset(final ParserRuleContext limitNode) {
        Optional<ParserRuleContext> offsetNode = ExtractorUtils.findFirstChildNode(limitNode, RuleName.LIMIT_OFFSET);
        return offsetNode.isPresent() ? Optional.of(extractLimitValue(offsetNode.get())) : Optional.<LimitValueSegment>absent();
    }

    private Optional<LimitValueSegment> extractRowCount(final ParserRuleContext limitNode) {
        Optional<ParserRuleContext> rowCountNode = ExtractorUtils.findFirstChildNode(limitNode, RuleName.LIMIT_ROW_COUNT);
        return rowCountNode.isPresent() ? Optional.of(extractLimitValue(rowCountNode.get())) : Optional.<LimitValueSegment>absent();
    }

    private LimitValueSegment extractLimitValue(final ParserRuleContext limitValueNode) {
        Optional<ParserRuleContext> numberLiteralsNode = ExtractorUtils.findFirstChildNode(limitValueNode, RuleName.NUMBER_LITERALS);
        Preconditions.checkState(numberLiteralsNode.isPresent());
        return new LimitValueSegment(
                limitValueNode.getStart().getStartIndex(), limitValueNode.getStop().getStopIndex(), SQLUtil.getExactlyNumber(numberLiteralsNode.get().getText(), 10).longValue());
    }
}
