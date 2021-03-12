package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.CollectionSQLSegmentExtractor;
import com.linglong.sql.segment.complex.SelectSegment;
import com.linglong.sql.segment.complex.SubquerySegment;
import com.linglong.sql.segment.generic.TableSegment;
import com.linglong.sql.util.ExtractorUtils;
import com.linglong.sql.util.RuleName;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * @author Stereo on 2019/10/9.
 */
public final class TableReferencesExtractor implements CollectionSQLSegmentExtractor {

    @Override
    public Collection<TableSegment> extract(ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> tableReferencesNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.TABLE_REFERENCES);
        if (tableReferencesNode.isPresent()) {
            Collection<TableSegment> tableSegments = new LinkedList<>();
            for (ParserRuleContext childRuleNode : ExtractorUtils.getChildrenNodes(tableReferencesNode.get())) {
                Optional<ParserRuleContext> tableFactorNode = ExtractorUtils.findFirstChildNode(childRuleNode, RuleName.TABLE_FACTOR);
                if (tableFactorNode.isPresent()) {
                    addTableSegment(tableFactorNode, tableSegments);
                }
            }
            return tableSegments;
        } else
            return Collections.emptyList();
    }

    private void addTableSegment(Optional<ParserRuleContext> tableFactorNode, Collection<TableSegment> tableSegments) {
        ParserRuleContext tableFactorRuleNode = tableFactorNode.get();
        Optional<ParserRuleContext> nextRuleNode;
        Optional<TableSegment> tableSegmentOptional;
        nextRuleNode = ExtractorUtils.findFirstChildNodeNoneRecursive(tableFactorRuleNode, RuleName.TABLE_NAME, true);
        if (nextRuleNode.isPresent()) {
            tableSegmentOptional = getTableSegment(nextRuleNode.get());
            if (tableSegmentOptional.isPresent()) {
                tableSegments.add(tableSegmentOptional.get());
            }
            return;
        }
        nextRuleNode = ExtractorUtils.findFirstChildNodeNoneRecursive(tableFactorRuleNode, RuleName.SUBQUERY, true);
        if (nextRuleNode.isPresent()) {
            tableSegmentOptional = getSubquerySegment(nextRuleNode.get());
            if (tableSegmentOptional.isPresent()) {
                tableSegments.add(tableSegmentOptional.get());
            }
            return;
        }
    }

    private Optional<TableSegment> getTableSegment(final ParserRuleContext childRuleNode) {
        Optional<ParserRuleContext> tableNameNode = ExtractorUtils.findFirstChildNode(childRuleNode, RuleName.TABLE_NAME);
        if (tableNameNode.isPresent()) {
            ParserRuleContext nameNode = ExtractorUtils.getFirstChildNode(tableNameNode.get(), RuleName.NAME);
            TableSegment tableSegment = new TableSegment(nameNode.getStart().getStartIndex(), nameNode.getStop().getStopIndex(), nameNode.getText());
            Optional<ParserRuleContext> rpNameNode = ExtractorUtils.findFirstChildNode(tableNameNode.get(), RuleName.RP_NAME);
            if (rpNameNode.isPresent()) {
                tableSegment.setRpName(rpNameNode.get().getText());
            }
            Optional<ParserRuleContext> dbNameNode = ExtractorUtils.findFirstChildNode(tableNameNode.get(), RuleName.DB_NAME);
            if (dbNameNode.isPresent()) {
                tableSegment.setDbName(dbNameNode.get().getText());
            }
            return Optional.of(tableSegment);
        } else
            return Optional.absent();
    }

    private Optional<TableSegment> getSubquerySegment(final ParserRuleContext childRuleNode) {
        Optional<ParserRuleContext> subqueryNode = ExtractorUtils.findFirstChildNode(childRuleNode, RuleName.SUBQUERY);
        if (!subqueryNode.isPresent()) {
            return Optional.absent();
        }

        Optional<ParserRuleContext> selectClauseNode = ExtractorUtils.findFirstChildNode(subqueryNode.get(), RuleName.SELECT_CLAUSE);
        if (!selectClauseNode.isPresent()) {
            return Optional.absent();
        }
        SelectExtractor selectExtractor = new SelectExtractor();
        Optional<SelectSegment> selectSegmentOptional = selectExtractor.extract(selectClauseNode.get());
        if (!selectSegmentOptional.isPresent()) {
            return Optional.absent();
        }
        return Optional.of(new TableSegment(childRuleNode.getStart().getStartIndex(), childRuleNode.getStop().getStopIndex(), new SubquerySegment(selectSegmentOptional.get())));
    }
}
