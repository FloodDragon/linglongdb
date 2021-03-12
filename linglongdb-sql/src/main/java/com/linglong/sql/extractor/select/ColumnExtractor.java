package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.predicate.ColumnWhereSegment;
import com.linglong.sql.util.ExtractorUtils;
import com.linglong.sql.util.RuleName;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo on 2019/10/9.
 */
public final class ColumnExtractor implements OptionalSQLSegmentExtractor {

    @Override
    public Optional<ColumnWhereSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> columnNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.COLUMN_NAME);
        return columnNode.isPresent() ? Optional.of(getColumnSegment(columnNode.get())) : Optional.absent();
    }

    private ColumnWhereSegment getColumnSegment(final ParserRuleContext columnNode) {
        ParserRuleContext nameNode = ExtractorUtils.getFirstChildNode(columnNode, RuleName.NAME);
        ColumnWhereSegment result = new ColumnWhereSegment(columnNode.getStart().getStartIndex(), columnNode.getStop().getStopIndex(), nameNode.getText());
        return result;
    }
}