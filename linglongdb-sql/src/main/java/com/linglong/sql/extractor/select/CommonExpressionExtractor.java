package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.predicate.CommonExpressionSegment;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo on 2019/10/9.
 */
public final class CommonExpressionExtractor implements OptionalSQLSegmentExtractor {

    @Override
    public Optional<CommonExpressionSegment> extract(final ParserRuleContext expressionNode) {
        return Optional.of(new CommonExpressionSegment(expressionNode.getStart().getStartIndex(), expressionNode.getStop().getStopIndex(), expressionNode.getText()));
    }
}