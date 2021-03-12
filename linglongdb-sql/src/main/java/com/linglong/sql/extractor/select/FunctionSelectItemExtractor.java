package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.item.FunctionSelectItemSegment;
import com.linglong.sql.util.ExtractorUtils;
import com.linglong.sql.util.RuleName;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo on 2019/10/8.
 */
public final class FunctionSelectItemExtractor implements OptionalSQLSegmentExtractor {
    @Override
    public Optional<FunctionSelectItemSegment> extract(ParserRuleContext expressionNode) {
        Optional<ParserRuleContext> functionCallNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.FUNCTION_CALL);
        if (!functionCallNode.isPresent()) {
            return Optional.absent();
        }
        ParserRuleContext functionCallRuleNode = functionCallNode.get();
        FunctionSelectItemSegment functionSelectItemSegment = new FunctionSelectItemSegment(
                functionCallRuleNode.getStart().getStartIndex(),
                functionCallRuleNode.getStop().getStopIndex(), functionCallRuleNode.getText());

        Optional<ParserRuleContext> aliasNode = ExtractorUtils.findFirstChildNodeNoneRecursive(expressionNode, RuleName.ALIAS);

        /**
         * 寻找别名
         */
        if (aliasNode.isPresent()) {
            functionSelectItemSegment.setAlias(aliasNode.get().getText());
        }

        return Optional.of(functionSelectItemSegment);
    }
}
