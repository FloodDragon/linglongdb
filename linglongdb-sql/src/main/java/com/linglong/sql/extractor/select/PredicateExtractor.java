package com.linglong.sql.extractor.select;

import com.linglong.sql.extractor.OptionalSQLSegmentExtractor;
import com.linglong.sql.segment.predicate.*;
import com.linglong.sql.util.ExtractorUtils;
import com.linglong.sql.util.LogicalOperator;
import com.linglong.sql.util.Paren;
import com.linglong.sql.util.RuleName;
import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.ParserRuleContext;
import com.google.common.base.Optional;
import java.util.*;

/**
 * @author Stereo on 2019/10/9.
 */
public final class PredicateExtractor implements OptionalSQLSegmentExtractor {
    private final ColumnExtractor columnExtractor = new ColumnExtractor();
    private final ExpressionExtractor expressionExtractor = new ExpressionExtractor();

    @Override
    public Optional<OrPredicateSegment> extract(ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> whereNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.WHERE_CLAUSE);
        if (!whereNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> exprNode = ExtractorUtils.findFirstChildNode((ParserRuleContext) whereNode.get().getChild(1), RuleName.EXPR);
        Preconditions.checkState(exprNode.isPresent());
        return extractRecursiveWithLogicalOperation(exprNode.get());
    }

    private Optional<OrPredicateSegment> extractRecursiveWithLogicalOperation(final ParserRuleContext exprNode) {
        Optional<ParserRuleContext> logicalOperatorNode = ExtractorUtils.findFirstChildNodeNoneRecursive(exprNode, RuleName.LOGICAL_OPERATOR);
        if (!logicalOperatorNode.isPresent()) {
            return extractRecursiveWithParen(exprNode);
        }
        Optional<OrPredicateSegment> leftPredicate = extractRecursiveWithLogicalOperation((ParserRuleContext) exprNode.getChild(0));
        Optional<OrPredicateSegment> rightPredicate = extractRecursiveWithLogicalOperation((ParserRuleContext) exprNode.getChild(2));
        if (leftPredicate.isPresent() && rightPredicate.isPresent()) {
            return Optional.of(mergePredicate(leftPredicate.get(), rightPredicate.get(), logicalOperatorNode.get().getText()));
        }
        return leftPredicate.isPresent() ? leftPredicate : rightPredicate;
    }

    private Optional<OrPredicateSegment> extractRecursiveWithParen(final ParserRuleContext exprNode) {
        if (1 == exprNode.getChild(0).getText().length() && Paren.isLeftParen(exprNode.getChild(0).getText().charAt(0))) {
            return extractRecursiveWithLogicalOperation((ParserRuleContext) exprNode.getChild(1));
        }
        Optional<PredicateSegment> predicate = extractPredicate(exprNode);
        return predicate.isPresent() ? Optional.of(getOrPredicateSegment(predicate.get())) : Optional.absent();
    }

    private Optional<PredicateSegment> extractPredicate(final ParserRuleContext exprNode) {
        if (ExtractorUtils.findFirstChildNode(exprNode, RuleName.SUBQUERY).isPresent()) {
            return Optional.absent();
        }
        Optional<PredicateSegment> result = extractComparisonPredicate(exprNode);
        if (result.isPresent()) {
            return result;
        }
        Optional<ParserRuleContext> predicateNode = ExtractorUtils.findFirstChildNode(exprNode, RuleName.PREDICATE);
        if (!predicateNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ColumnWhereSegment> column = columnExtractor.extract((ParserRuleContext) predicateNode.get().getChild(0));
        if (!column.isPresent()) {
            return Optional.absent();
        }

        if (predicateNode.get().getChildCount() >= 5 && "IN".equalsIgnoreCase(predicateNode.get().getChild(1).getText())) {
            result = extractInPredicate(predicateNode.get(), column.get());
            if (result.isPresent()) {
                return result;
            }
        }

        /*
        if (5 == predicateNode.get().getChildCount() && "BETWEEN".equalsIgnoreCase(predicateNode.get().getChild(1).getText())) {
            // BETWEEN
        }
        */
        return Optional.absent();
    }

    private Optional<PredicateSegment> extractInPredicate(final ParserRuleContext predicateNode, final ColumnWhereSegment column) {
        Collection<ExpressionSegment> sqlExpressions = extractInExpressionSegments(predicateNode);
        return sqlExpressions.isEmpty() ? Optional.<PredicateSegment>absent()
                : Optional.of(new PredicateSegment(predicateNode.getStart().getStartIndex(), predicateNode.getStop().getStopIndex(), column, new PredicateInRightValueSegment(sqlExpressions)));
    }

    private Collection<ExpressionSegment> extractInExpressionSegments(final ParserRuleContext predicateNode) {
        List<ExpressionSegment> result = new LinkedList<>();
        for (int i = 3; i < predicateNode.getChildCount(); i++) {
            if (RuleName.EXPR.getName().equals(predicateNode.getChild(i).getClass().getSimpleName())) {
                Optional<? extends ExpressionSegment> expression = expressionExtractor.extract((ParserRuleContext) predicateNode.getChild(i));
                // 如果expr的某些部分不受支持，清除in子句中的所有expr
                if (!expression.isPresent()) {
                    return Collections.emptyList();
                }
                result.add(expression.get());
            }
        }
        return result;
    }

    private Optional<PredicateSegment> extractComparisonPredicate(final ParserRuleContext exprNode) {
        Optional<ParserRuleContext> comparisonOperatorNode = ExtractorUtils.findFirstChildNode(exprNode, RuleName.COMPARISON_OPERATOR);
        if (!comparisonOperatorNode.isPresent()) {
            return Optional.absent();
        }
        ParserRuleContext booleanPrimaryNode = comparisonOperatorNode.get().getParent();
        Optional<ParserRuleContext> leftColumnNode = ExtractorUtils.findSingleNodeFromFirstDescendant((ParserRuleContext) booleanPrimaryNode.getChild(0), RuleName.COLUMN_NAME);
        Optional<ParserRuleContext> rightColumnNode = ExtractorUtils.findSingleNodeFromFirstDescendant((ParserRuleContext) booleanPrimaryNode.getChild(2), RuleName.COLUMN_NAME);
        if (!leftColumnNode.isPresent() && !rightColumnNode.isPresent()) {
            return Optional.absent();
        }
        if (leftColumnNode.isPresent() && rightColumnNode.isPresent()) {
            Optional<ColumnWhereSegment> leftColumn = columnExtractor.extract(leftColumnNode.get());
            Optional<ColumnWhereSegment> rightColumn = columnExtractor.extract(rightColumnNode.get());
            Preconditions.checkState(leftColumn.isPresent() && rightColumn.isPresent());
            return Optional.of(new PredicateSegment(booleanPrimaryNode.getStart().getStartIndex(), booleanPrimaryNode.getStop().getStopIndex(), leftColumn.get(), rightColumn.get().operator(comparisonOperatorNode.get().getText())));
        }
        Optional<ColumnWhereSegment> column = columnExtractor.extract(exprNode);
        Preconditions.checkState(column.isPresent());
        ParserRuleContext valueNode = leftColumnNode.isPresent()
                ? (ParserRuleContext) comparisonOperatorNode.get().getParent().getChild(2) : (ParserRuleContext) comparisonOperatorNode.get().getParent().getChild(0);
        Optional<? extends ExpressionSegment> sqlExpression = expressionExtractor.extract(valueNode);
        return sqlExpression.isPresent() ? Optional.of(new PredicateSegment(booleanPrimaryNode.getStart().getStartIndex(), booleanPrimaryNode.getStop().getStopIndex(), column.get(),
                new ValueWhereSegment(comparisonOperatorNode.get().getText(), sqlExpression.get()))) : Optional.absent();
    }

    private OrPredicateSegment getOrPredicateSegment(final PredicateSegment predicate) {
        OrPredicateSegment result = new OrPredicateSegment();
        AndPredicateSegment andPredicate = new AndPredicateSegment();
        andPredicate.getPredicates().add(predicate);
        result.getAndPredicates().add(andPredicate);
        return result;
    }

    private OrPredicateSegment mergePredicate(final OrPredicateSegment leftPredicate, final OrPredicateSegment rightPredicate, final String operator) {
        Optional<LogicalOperator> logicalOperator = LogicalOperator.valueFrom(operator);
        Preconditions.checkState(logicalOperator.isPresent());
        if (LogicalOperator.OR == logicalOperator.get()) {
            leftPredicate.getAndPredicates().addAll(rightPredicate.getAndPredicates());
            return leftPredicate;
        }
        OrPredicateSegment result = new OrPredicateSegment();
        for (AndPredicateSegment eachLeftPredicate : leftPredicate.getAndPredicates()) {
            for (AndPredicateSegment eachRightPredicate : rightPredicate.getAndPredicates()) {
                result.getAndPredicates().add(getAndPredicate(eachLeftPredicate, eachRightPredicate));
            }
        }
        return result;
    }

    private AndPredicateSegment getAndPredicate(final AndPredicateSegment leftPredicate, final AndPredicateSegment rightPredicate) {
        AndPredicateSegment result = new AndPredicateSegment();
        result.getPredicates().addAll(leftPredicate.getPredicates());
        result.getPredicates().addAll(rightPredicate.getPredicates());
        return result;
    }
}
