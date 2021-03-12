package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.item.SelectItemSegment;
import com.linglong.sql.segment.item.SelectItemsSegment;
import com.linglong.sql.util.ExtractorUtils;
import com.linglong.sql.util.RuleName;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;


/**
 * @author Stereo on 2019/10/8.
 */
public final class SelectItemsExtractor implements OptionalSQLSegmentExtractor {

    private SelectItemExtractor selectItemExtractor = new SelectItemExtractor();

    @Override
    public Optional<SelectItemsSegment> extract(ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> selectItemsNode = ExtractorUtils.findFirstChildNodeNoneRecursive(ancestorNode, RuleName.SELECT_ITEMS);
        if (!selectItemsNode.isPresent()) {
            return Optional.absent();
        }
        ParserRuleContext selectItemsRuleNode = selectItemsNode.get();
        SelectItemsSegment result = new SelectItemsSegment(selectItemsRuleNode.getStart().getStartIndex(), selectItemsRuleNode.getStop().getStopIndex());
        Optional<ParserRuleContext> unqualifiedShorthandNode = ExtractorUtils.findFirstChildNode(selectItemsRuleNode, RuleName.UNQUALIFIED_SHORTHAND);
        if (unqualifiedShorthandNode.isPresent()) {
            setUnqualifiedShorthandSelectItemSegment(unqualifiedShorthandNode.get(), result);
        }
        setSelectItemSegment(selectItemsRuleNode, result);
        return Optional.of(result);
    }

    private void setUnqualifiedShorthandSelectItemSegment(final ParserRuleContext unqualifiedShorthandNode,
                                                          final SelectItemsSegment selectItemsSegment) {
        Optional<? extends SelectItemSegment> unqualifiedShorthandSelectItemSegment = selectItemExtractor.extract(unqualifiedShorthandNode);
        if (unqualifiedShorthandSelectItemSegment.isPresent()) {
            selectItemsSegment.getSelectItems().add(unqualifiedShorthandSelectItemSegment.get());
        }
    }

    private void setSelectItemSegment(
            final ParserRuleContext selectItemsNode,
            final SelectItemsSegment selectItemsSegment) {
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(selectItemsNode, RuleName.SELECT_ITEM)) {
            Optional<? extends SelectItemSegment> selectItemSegment = selectItemExtractor.extract(each);
            if (selectItemSegment.isPresent()) {
                selectItemsSegment.getSelectItems().add(selectItemSegment.get());
            }
        }
    }
}
