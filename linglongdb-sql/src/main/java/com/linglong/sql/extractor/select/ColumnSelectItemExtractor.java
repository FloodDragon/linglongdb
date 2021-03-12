package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.item.ColumnSelectItemSegment;
import com.linglong.sql.util.ExtractorUtils;
import com.linglong.sql.util.RuleName;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo on 2019/10/8.
 */
public final class ColumnSelectItemExtractor implements OptionalSQLSegmentExtractor {
    @Override
    public Optional<ColumnSelectItemSegment> extract(ParserRuleContext expressionNode) {
        /**
         * 寻找当前列
         */
        Optional<ParserRuleContext> columnNode = ExtractorUtils.findFirstChildNodeNoneRecursive(expressionNode, RuleName.COLUMN_NAME);
        if (!columnNode.isPresent()) {
            return Optional.absent();
        }
        ParserRuleContext columnRuleNode = columnNode.get();
        /**
         * 寻找列名
         */
        Optional<ParserRuleContext> nameNode = ExtractorUtils.findFirstChildNodeNoneRecursive(columnNode.get(), RuleName.NAME);
        if (!columnNode.isPresent()) {
            return Optional.absent();
        }
        ParserRuleContext nameRuleNode = nameNode.get();

        ColumnSelectItemSegment columnSelectItemSegment = new ColumnSelectItemSegment(
                columnRuleNode.getStart().getStartIndex(),
                columnRuleNode.getStop().getStopIndex(),
                columnRuleNode.getText(),
                nameRuleNode.getText());
        Optional<ParserRuleContext> aliasNode = ExtractorUtils.findFirstChildNodeNoneRecursive(expressionNode, RuleName.ALIAS);

        /**
         * 寻找别名
         */
        if (aliasNode.isPresent()) {
            columnSelectItemSegment.setAlias(aliasNode.get().getText());
        }

        /**
         * 递归寻找下级列
         */
        Optional<ParserRuleContext> childNode = ExtractorUtils.findFirstChildNodeNoneRecursive(columnRuleNode, RuleName.COLUMN_NAME, true);
        if (childNode.isPresent()) {
            Optional<? extends ColumnSelectItemSegment> childColumnSelectItemSegmentOptional = extract(childNode.get());
            if (childColumnSelectItemSegmentOptional.isPresent()) {
                columnSelectItemSegment.setColumnSelectItemSegment(childColumnSelectItemSegmentOptional.get());
            }
        }
        return Optional.of(columnSelectItemSegment);
    }
}
