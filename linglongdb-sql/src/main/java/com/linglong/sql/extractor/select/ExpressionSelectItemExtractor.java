package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.item.ExpressionSelectItemSegment;
import com.linglong.sql.util.ExtractorUtils;
import com.linglong.sql.util.RuleName;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo on 2019/10/9.
 */
public final class ExpressionSelectItemExtractor implements OptionalSQLSegmentExtractor {
    @Override
    public Optional<ExpressionSelectItemSegment> extract(ParserRuleContext expressionNode) {
        ExpressionSelectItemSegment result = new ExpressionSelectItemSegment(expressionNode.getStart().getStartIndex(), expressionNode.getStop().getStopIndex(), expressionNode.getText());
        Optional<ParserRuleContext> aliasNode = ExtractorUtils.findFirstChildNodeNoneRecursive(expressionNode, RuleName.ALIAS);
        if (aliasNode.isPresent()) {
            result.setAlias(aliasNode.get().getText());
        }
        return Optional.of(result);
    }
}
