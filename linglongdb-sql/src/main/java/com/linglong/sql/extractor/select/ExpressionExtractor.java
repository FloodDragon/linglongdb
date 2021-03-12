package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.item.LiteralExpressionSegment;
import com.linglong.sql.segment.predicate.ExpressionSegment;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo on 2019/10/9.
 */
public final class ExpressionExtractor implements OptionalSQLSegmentExtractor {

    private final LiteralExpressionExtractor literalExpressionExtractor = new LiteralExpressionExtractor();
    private final CommonExpressionExtractor commonExpressionExtractor = new CommonExpressionExtractor();

    @Override
    public Optional<? extends ExpressionSegment> extract(final ParserRuleContext expressionNode) {

        Optional<LiteralExpressionSegment> literalExpressionSegment = literalExpressionExtractor.extract(expressionNode);
        if (literalExpressionSegment.isPresent()) {
            return literalExpressionSegment;
        }
        return commonExpressionExtractor.extract(expressionNode);
    }
}