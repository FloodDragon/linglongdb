package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.item.ShorthandSelectItemSegment;
import com.linglong.sql.util.ExtractorUtils;
import com.linglong.sql.util.RuleName;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo on 2019/10/8.
 */
public final class ShorthandSelectItemExtractor implements OptionalSQLSegmentExtractor {
    @Override
    public Optional<ShorthandSelectItemSegment> extract(final ParserRuleContext expressionNode) {
        Optional<ParserRuleContext> unqualifiedShorthandNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.UNQUALIFIED_SHORTHAND);
        if (unqualifiedShorthandNode.isPresent()) {
            return Optional.of(new ShorthandSelectItemSegment(
                    unqualifiedShorthandNode.get().getStart().getStartIndex(), unqualifiedShorthandNode.get().getStop().getStopIndex(), unqualifiedShorthandNode.get().getText()));
        }
        return Optional.absent();
    }
}
