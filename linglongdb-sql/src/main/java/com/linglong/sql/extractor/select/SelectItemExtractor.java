package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.item.SelectItemSegment;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo on 2019/10/8.
 */
public final class SelectItemExtractor implements OptionalSQLSegmentExtractor {

    private final ShorthandSelectItemExtractor shorthandSelectItemExtractor = new ShorthandSelectItemExtractor();
    private final ColumnSelectItemExtractor columnSelectItemExtractor = new ColumnSelectItemExtractor();
    private final FunctionSelectItemExtractor functionSelectItemExtractor = new FunctionSelectItemExtractor();
    private final ExpressionSelectItemExtractor expressionSelectItemExtractor = new ExpressionSelectItemExtractor();

    @Override
    public Optional<? extends SelectItemSegment> extract(ParserRuleContext expressionNode) {
        Optional<? extends SelectItemSegment> result;
        result = shorthandSelectItemExtractor.extract(expressionNode);
        if (result.isPresent()) {
            return result;
        }
        result = columnSelectItemExtractor.extract(expressionNode);
        if (result.isPresent()) {
            return result;
        }

        result = functionSelectItemExtractor.extract(expressionNode);
        if (result.isPresent()) {
            return result;
        }
        return expressionSelectItemExtractor.extract(expressionNode);
    }
}
