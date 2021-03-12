package com.linglong.sql.antlr4;// Generated from D:/IdeaProjects/linglongdb/linglongdb-sql/src/main/antlr4\LinglongSQLStatement.g4 by ANTLR 4.7.2
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link LinglongSQLStatementParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface LinglongSQLStatementVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#execute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecute(LinglongSQLStatementParser.ExecuteContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#selectClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectClause(LinglongSQLStatementParser.SelectClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#selectItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectItems(LinglongSQLStatementParser.SelectItemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#selectItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectItem(LinglongSQLStatementParser.SelectItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(LinglongSQLStatementParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#unqualifiedShorthand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnqualifiedShorthand(LinglongSQLStatementParser.UnqualifiedShorthandContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#qualifiedShorthand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedShorthand(LinglongSQLStatementParser.QualifiedShorthandContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(LinglongSQLStatementParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#tableReferences}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableReferences(LinglongSQLStatementParser.TableReferencesContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#escapedTableReference_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEscapedTableReference_(LinglongSQLStatementParser.EscapedTableReference_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#tableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableReference(LinglongSQLStatementParser.TableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#tableFactor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableFactor(LinglongSQLStatementParser.TableFactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(LinglongSQLStatementParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#groupByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByClause(LinglongSQLStatementParser.GroupByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#limitClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitClause(LinglongSQLStatementParser.LimitClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#limitRowCount}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitRowCount(LinglongSQLStatementParser.LimitRowCountContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#limitOffset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitOffset(LinglongSQLStatementParser.LimitOffsetContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#subquery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubquery(LinglongSQLStatementParser.SubqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterMarker(LinglongSQLStatementParser.ParameterMarkerContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#literals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiterals(LinglongSQLStatementParser.LiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#stringLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiterals(LinglongSQLStatementParser.StringLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberLiterals(LinglongSQLStatementParser.NumberLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#dateTimeLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeLiterals(LinglongSQLStatementParser.DateTimeLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHexadecimalLiterals(LinglongSQLStatementParser.HexadecimalLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitValueLiterals(LinglongSQLStatementParser.BitValueLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#booleanLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiterals(LinglongSQLStatementParser.BooleanLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullValueLiterals(LinglongSQLStatementParser.NullValueLiteralsContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#identifier_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier_(LinglongSQLStatementParser.Identifier_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#variable_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable_(LinglongSQLStatementParser.Variable_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#unreservedWord_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnreservedWord_(LinglongSQLStatementParser.UnreservedWord_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(LinglongSQLStatementParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#dbName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDbName(LinglongSQLStatementParser.DbNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#rpName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRpName(LinglongSQLStatementParser.RpNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#columnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnName(LinglongSQLStatementParser.ColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#owner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOwner(LinglongSQLStatementParser.OwnerContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(LinglongSQLStatementParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#columnNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNames(LinglongSQLStatementParser.ColumnNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#characterSetName_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterSetName_(LinglongSQLStatementParser.CharacterSetName_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(LinglongSQLStatementParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOperator(LinglongSQLStatementParser.LogicalOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#notOperator_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotOperator_(LinglongSQLStatementParser.NotOperator_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#booleanPrimary_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanPrimary_(LinglongSQLStatementParser.BooleanPrimary_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(LinglongSQLStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(LinglongSQLStatementParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#bitExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitExpr(LinglongSQLStatementParser.BitExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#simpleExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleExpr(LinglongSQLStatementParser.SimpleExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(LinglongSQLStatementParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#aggregationFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregationFunction(LinglongSQLStatementParser.AggregationFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#aggregationFunctionName_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregationFunctionName_(LinglongSQLStatementParser.AggregationFunctionName_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#selectorFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectorFunction_(LinglongSQLStatementParser.SelectorFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#selectorFunctionName_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectorFunctionName_(LinglongSQLStatementParser.SelectorFunctionName_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#transformationFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformationFunction_(LinglongSQLStatementParser.TransformationFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#transformationFunctionName_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformationFunctionName_(LinglongSQLStatementParser.TransformationFunctionName_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#distinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinct(LinglongSQLStatementParser.DistinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#overClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverClause_(LinglongSQLStatementParser.OverClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#windowSpecification_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowSpecification_(LinglongSQLStatementParser.WindowSpecification_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#partitionClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionClause_(LinglongSQLStatementParser.PartitionClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#frameClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameClause_(LinglongSQLStatementParser.FrameClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#frameStart_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameStart_(LinglongSQLStatementParser.FrameStart_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#frameEnd_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameEnd_(LinglongSQLStatementParser.FrameEnd_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#frameBetween_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrameBetween_(LinglongSQLStatementParser.FrameBetween_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#specialFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecialFunction_(LinglongSQLStatementParser.SpecialFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#groupConcatFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupConcatFunction_(LinglongSQLStatementParser.GroupConcatFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#windowFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowFunction_(LinglongSQLStatementParser.WindowFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#castFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastFunction_(LinglongSQLStatementParser.CastFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#convertFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConvertFunction_(LinglongSQLStatementParser.ConvertFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#positionFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionFunction_(LinglongSQLStatementParser.PositionFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#substringFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstringFunction_(LinglongSQLStatementParser.SubstringFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#extractFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtractFunction_(LinglongSQLStatementParser.ExtractFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#charFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharFunction_(LinglongSQLStatementParser.CharFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#trimFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrimFunction_(LinglongSQLStatementParser.TrimFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#weightStringFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeightStringFunction_(LinglongSQLStatementParser.WeightStringFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#levelClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLevelClause_(LinglongSQLStatementParser.LevelClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#levelInWeightListElement_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLevelInWeightListElement_(LinglongSQLStatementParser.LevelInWeightListElement_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#regularFunction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegularFunction_(LinglongSQLStatementParser.RegularFunction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#regularFunctionName_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegularFunctionName_(LinglongSQLStatementParser.RegularFunctionName_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#matchExpression_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchExpression_(LinglongSQLStatementParser.MatchExpression_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#matchSearchModifier_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchSearchModifier_(LinglongSQLStatementParser.MatchSearchModifier_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#caseExpression_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseExpression_(LinglongSQLStatementParser.CaseExpression_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#caseWhen_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseWhen_(LinglongSQLStatementParser.CaseWhen_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#caseElse_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseElse_(LinglongSQLStatementParser.CaseElse_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#intervalExpression_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalExpression_(LinglongSQLStatementParser.IntervalExpression_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#intervalUnit_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalUnit_(LinglongSQLStatementParser.IntervalUnit_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#orderByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByClause(LinglongSQLStatementParser.OrderByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#orderByItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByItem(LinglongSQLStatementParser.OrderByItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataType(LinglongSQLStatementParser.DataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#dataTypeName_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeName_(LinglongSQLStatementParser.DataTypeName_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeLength(LinglongSQLStatementParser.DataTypeLengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#characterSet_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacterSet_(LinglongSQLStatementParser.CharacterSet_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#collateClause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollateClause_(LinglongSQLStatementParser.CollateClause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link LinglongSQLStatementParser#ignoredIdentifier_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIgnoredIdentifier_(LinglongSQLStatementParser.IgnoredIdentifier_Context ctx);
}