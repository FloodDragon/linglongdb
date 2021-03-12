package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.generic.TableSegment;
import com.linglong.sql.segment.generic.TablesSegment;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;

/**
 * @author Stereo on 2019/10/11.
 */
public final class TableExtractor implements OptionalSQLSegmentExtractor {

    private final TableReferencesExtractor tableReferencesExtractor = new TableReferencesExtractor();

    @Override
    public Optional<TablesSegment> extract(ParserRuleContext ancestorNode) {
        Collection<TableSegment> tableSegments = tableReferencesExtractor.extract(ancestorNode);
        return tableSegments != null && tableSegments.size() > 0 ? Optional.of(new TablesSegment(ancestorNode.getStart().getStartIndex(), ancestorNode.getStop().getStopIndex(), tableSegments)) : Optional.absent();
    }
}
