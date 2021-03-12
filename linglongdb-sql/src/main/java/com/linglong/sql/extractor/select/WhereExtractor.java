package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.predicate.OrPredicateSegment;
import com.linglong.sql.segment.predicate.WhereSegment;
import com.linglong.sql.util.ExtractorUtils;
import com.linglong.sql.util.RuleName;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo on 2019/10/9.
 */
public final class WhereExtractor implements OptionalSQLSegmentExtractor {

    private final PredicateExtractor predicateExtractor = new PredicateExtractor();

    @Override
    public Optional<WhereSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> whereNode = ExtractorUtils.findFirstChildNodeNoneRecursive(ancestorNode, RuleName.WHERE_CLAUSE);
        if (!whereNode.isPresent()) {
            return Optional.absent();
        }
        WhereSegment result = new WhereSegment(whereNode.get().getStart().getStartIndex(), whereNode.get().getStop().getStopIndex());
        Optional<OrPredicateSegment> orPredicateSegment = predicateExtractor.extract(whereNode.get());
        if (orPredicateSegment.isPresent()) {
            result.getAndPredicateSegments().addAll(orPredicateSegment.get().getAndPredicates());
        }
        return Optional.of(result);
    }
}