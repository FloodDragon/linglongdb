package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.item.LiteralExpressionSegment;
import com.linglong.sql.util.ExtractorUtils;
import com.linglong.sql.util.RuleName;
import com.linglong.sql.util.SQLUtil;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo on 2019/10/9.
 */
public final class LiteralExpressionExtractor implements OptionalSQLSegmentExtractor {

    @Override
    public Optional<LiteralExpressionSegment> extract(final ParserRuleContext expressionNode) {
        Optional<ParserRuleContext> literalsNode = ExtractorUtils.findSingleNodeFromFirstDescendant(expressionNode, RuleName.LITERALS);
        if (!literalsNode.isPresent()) {
            return Optional.absent();
        }
        Optional<?> literals = getLiterals(literalsNode.get());
        return literals.isPresent() ? Optional.of(new LiteralExpressionSegment(literalsNode.get().getStart().getStartIndex(), literalsNode.get().getStop().getStopIndex(), literals.get()))
                : Optional.<LiteralExpressionSegment>absent();
    }

    private Optional<?> getLiterals(final ParserRuleContext literalsNode) {
        Optional<Number> numberLiterals = getNumberLiterals(literalsNode);
        if (numberLiterals.isPresent()) {
            return numberLiterals;
        }
        return getStringLiterals(literalsNode);
    }

    private Optional<Number> getNumberLiterals(final ParserRuleContext literalsNode) {
        Optional<ParserRuleContext> numberLiteralsNode = ExtractorUtils.findFirstChildNode(literalsNode, RuleName.NUMBER_LITERALS);
        return numberLiteralsNode.isPresent() ? Optional.of(SQLUtil.getExactlyNumber(numberLiteralsNode.get().getText(), 10)) : Optional.<Number>absent();
    }

    private Optional<String> getStringLiterals(final ParserRuleContext literalsNode) {
        Optional<ParserRuleContext> stringLiteralsNode = ExtractorUtils.findFirstChildNode(literalsNode, RuleName.STRING_LITERALS);
        if (stringLiteralsNode.isPresent()) {
            String text = stringLiteralsNode.get().getText();
            return Optional.of(text.substring(1, text.length() - 1));
        }
        return Optional.absent();
    }
}