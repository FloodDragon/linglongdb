package com.linglong.sql.antlr4;// Generated from D:/IdeaProjects/linglongdb/linglongdb-sql/src/main/antlr4\LinglongSQLStatement.g4 by ANTLR 4.7.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link LinglongSQLStatementParser}.
 */
public interface LinglongSQLStatementListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#execute}.
	 * @param ctx the parse tree
	 */
	void enterExecute(LinglongSQLStatementParser.ExecuteContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#execute}.
	 * @param ctx the parse tree
	 */
	void exitExecute(LinglongSQLStatementParser.ExecuteContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void enterSelectClause(LinglongSQLStatementParser.SelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void exitSelectClause(LinglongSQLStatementParser.SelectClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#selectItems}.
	 * @param ctx the parse tree
	 */
	void enterSelectItems(LinglongSQLStatementParser.SelectItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#selectItems}.
	 * @param ctx the parse tree
	 */
	void exitSelectItems(LinglongSQLStatementParser.SelectItemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#selectItem}.
	 * @param ctx the parse tree
	 */
	void enterSelectItem(LinglongSQLStatementParser.SelectItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#selectItem}.
	 * @param ctx the parse tree
	 */
	void exitSelectItem(LinglongSQLStatementParser.SelectItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(LinglongSQLStatementParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(LinglongSQLStatementParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#unqualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void enterUnqualifiedShorthand(LinglongSQLStatementParser.UnqualifiedShorthandContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#unqualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void exitUnqualifiedShorthand(LinglongSQLStatementParser.UnqualifiedShorthandContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#qualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedShorthand(LinglongSQLStatementParser.QualifiedShorthandContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#qualifiedShorthand}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedShorthand(LinglongSQLStatementParser.QualifiedShorthandContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void enterFromClause(LinglongSQLStatementParser.FromClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void exitFromClause(LinglongSQLStatementParser.FromClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#tableReferences}.
	 * @param ctx the parse tree
	 */
	void enterTableReferences(LinglongSQLStatementParser.TableReferencesContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#tableReferences}.
	 * @param ctx the parse tree
	 */
	void exitTableReferences(LinglongSQLStatementParser.TableReferencesContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#escapedTableReference_}.
	 * @param ctx the parse tree
	 */
	void enterEscapedTableReference_(LinglongSQLStatementParser.EscapedTableReference_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#escapedTableReference_}.
	 * @param ctx the parse tree
	 */
	void exitEscapedTableReference_(LinglongSQLStatementParser.EscapedTableReference_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#tableReference}.
	 * @param ctx the parse tree
	 */
	void enterTableReference(LinglongSQLStatementParser.TableReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#tableReference}.
	 * @param ctx the parse tree
	 */
	void exitTableReference(LinglongSQLStatementParser.TableReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#tableFactor}.
	 * @param ctx the parse tree
	 */
	void enterTableFactor(LinglongSQLStatementParser.TableFactorContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#tableFactor}.
	 * @param ctx the parse tree
	 */
	void exitTableFactor(LinglongSQLStatementParser.TableFactorContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereClause(LinglongSQLStatementParser.WhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereClause(LinglongSQLStatementParser.WhereClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void enterGroupByClause(LinglongSQLStatementParser.GroupByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void exitGroupByClause(LinglongSQLStatementParser.GroupByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void enterLimitClause(LinglongSQLStatementParser.LimitClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void exitLimitClause(LinglongSQLStatementParser.LimitClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#limitRowCount}.
	 * @param ctx the parse tree
	 */
	void enterLimitRowCount(LinglongSQLStatementParser.LimitRowCountContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#limitRowCount}.
	 * @param ctx the parse tree
	 */
	void exitLimitRowCount(LinglongSQLStatementParser.LimitRowCountContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#limitOffset}.
	 * @param ctx the parse tree
	 */
	void enterLimitOffset(LinglongSQLStatementParser.LimitOffsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#limitOffset}.
	 * @param ctx the parse tree
	 */
	void exitLimitOffset(LinglongSQLStatementParser.LimitOffsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#subquery}.
	 * @param ctx the parse tree
	 */
	void enterSubquery(LinglongSQLStatementParser.SubqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#subquery}.
	 * @param ctx the parse tree
	 */
	void exitSubquery(LinglongSQLStatementParser.SubqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 */
	void enterParameterMarker(LinglongSQLStatementParser.ParameterMarkerContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#parameterMarker}.
	 * @param ctx the parse tree
	 */
	void exitParameterMarker(LinglongSQLStatementParser.ParameterMarkerContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#literals}.
	 * @param ctx the parse tree
	 */
	void enterLiterals(LinglongSQLStatementParser.LiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#literals}.
	 * @param ctx the parse tree
	 */
	void exitLiterals(LinglongSQLStatementParser.LiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#stringLiterals}.
	 * @param ctx the parse tree
	 */
	void enterStringLiterals(LinglongSQLStatementParser.StringLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#stringLiterals}.
	 * @param ctx the parse tree
	 */
	void exitStringLiterals(LinglongSQLStatementParser.StringLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 */
	void enterNumberLiterals(LinglongSQLStatementParser.NumberLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#numberLiterals}.
	 * @param ctx the parse tree
	 */
	void exitNumberLiterals(LinglongSQLStatementParser.NumberLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#dateTimeLiterals}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeLiterals(LinglongSQLStatementParser.DateTimeLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#dateTimeLiterals}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeLiterals(LinglongSQLStatementParser.DateTimeLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 */
	void enterHexadecimalLiterals(LinglongSQLStatementParser.HexadecimalLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#hexadecimalLiterals}.
	 * @param ctx the parse tree
	 */
	void exitHexadecimalLiterals(LinglongSQLStatementParser.HexadecimalLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 */
	void enterBitValueLiterals(LinglongSQLStatementParser.BitValueLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#bitValueLiterals}.
	 * @param ctx the parse tree
	 */
	void exitBitValueLiterals(LinglongSQLStatementParser.BitValueLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#booleanLiterals}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiterals(LinglongSQLStatementParser.BooleanLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#booleanLiterals}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiterals(LinglongSQLStatementParser.BooleanLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 */
	void enterNullValueLiterals(LinglongSQLStatementParser.NullValueLiteralsContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#nullValueLiterals}.
	 * @param ctx the parse tree
	 */
	void exitNullValueLiterals(LinglongSQLStatementParser.NullValueLiteralsContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#identifier_}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier_(LinglongSQLStatementParser.Identifier_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#identifier_}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier_(LinglongSQLStatementParser.Identifier_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#variable_}.
	 * @param ctx the parse tree
	 */
	void enterVariable_(LinglongSQLStatementParser.Variable_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#variable_}.
	 * @param ctx the parse tree
	 */
	void exitVariable_(LinglongSQLStatementParser.Variable_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#unreservedWord_}.
	 * @param ctx the parse tree
	 */
	void enterUnreservedWord_(LinglongSQLStatementParser.UnreservedWord_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#unreservedWord_}.
	 * @param ctx the parse tree
	 */
	void exitUnreservedWord_(LinglongSQLStatementParser.UnreservedWord_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(LinglongSQLStatementParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(LinglongSQLStatementParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#dbName}.
	 * @param ctx the parse tree
	 */
	void enterDbName(LinglongSQLStatementParser.DbNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#dbName}.
	 * @param ctx the parse tree
	 */
	void exitDbName(LinglongSQLStatementParser.DbNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#rpName}.
	 * @param ctx the parse tree
	 */
	void enterRpName(LinglongSQLStatementParser.RpNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#rpName}.
	 * @param ctx the parse tree
	 */
	void exitRpName(LinglongSQLStatementParser.RpNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#columnName}.
	 * @param ctx the parse tree
	 */
	void enterColumnName(LinglongSQLStatementParser.ColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#columnName}.
	 * @param ctx the parse tree
	 */
	void exitColumnName(LinglongSQLStatementParser.ColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#owner}.
	 * @param ctx the parse tree
	 */
	void enterOwner(LinglongSQLStatementParser.OwnerContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#owner}.
	 * @param ctx the parse tree
	 */
	void exitOwner(LinglongSQLStatementParser.OwnerContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(LinglongSQLStatementParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(LinglongSQLStatementParser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#columnNames}.
	 * @param ctx the parse tree
	 */
	void enterColumnNames(LinglongSQLStatementParser.ColumnNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#columnNames}.
	 * @param ctx the parse tree
	 */
	void exitColumnNames(LinglongSQLStatementParser.ColumnNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#characterSetName_}.
	 * @param ctx the parse tree
	 */
	void enterCharacterSetName_(LinglongSQLStatementParser.CharacterSetName_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#characterSetName_}.
	 * @param ctx the parse tree
	 */
	void exitCharacterSetName_(LinglongSQLStatementParser.CharacterSetName_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(LinglongSQLStatementParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(LinglongSQLStatementParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOperator(LinglongSQLStatementParser.LogicalOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOperator(LinglongSQLStatementParser.LogicalOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#notOperator_}.
	 * @param ctx the parse tree
	 */
	void enterNotOperator_(LinglongSQLStatementParser.NotOperator_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#notOperator_}.
	 * @param ctx the parse tree
	 */
	void exitNotOperator_(LinglongSQLStatementParser.NotOperator_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#booleanPrimary_}.
	 * @param ctx the parse tree
	 */
	void enterBooleanPrimary_(LinglongSQLStatementParser.BooleanPrimary_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#booleanPrimary_}.
	 * @param ctx the parse tree
	 */
	void exitBooleanPrimary_(LinglongSQLStatementParser.BooleanPrimary_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(LinglongSQLStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(LinglongSQLStatementParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(LinglongSQLStatementParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(LinglongSQLStatementParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#bitExpr}.
	 * @param ctx the parse tree
	 */
	void enterBitExpr(LinglongSQLStatementParser.BitExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#bitExpr}.
	 * @param ctx the parse tree
	 */
	void exitBitExpr(LinglongSQLStatementParser.BitExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#simpleExpr}.
	 * @param ctx the parse tree
	 */
	void enterSimpleExpr(LinglongSQLStatementParser.SimpleExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#simpleExpr}.
	 * @param ctx the parse tree
	 */
	void exitSimpleExpr(LinglongSQLStatementParser.SimpleExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(LinglongSQLStatementParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(LinglongSQLStatementParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#aggregationFunction}.
	 * @param ctx the parse tree
	 */
	void enterAggregationFunction(LinglongSQLStatementParser.AggregationFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#aggregationFunction}.
	 * @param ctx the parse tree
	 */
	void exitAggregationFunction(LinglongSQLStatementParser.AggregationFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#aggregationFunctionName_}.
	 * @param ctx the parse tree
	 */
	void enterAggregationFunctionName_(LinglongSQLStatementParser.AggregationFunctionName_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#aggregationFunctionName_}.
	 * @param ctx the parse tree
	 */
	void exitAggregationFunctionName_(LinglongSQLStatementParser.AggregationFunctionName_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#selectorFunction_}.
	 * @param ctx the parse tree
	 */
	void enterSelectorFunction_(LinglongSQLStatementParser.SelectorFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#selectorFunction_}.
	 * @param ctx the parse tree
	 */
	void exitSelectorFunction_(LinglongSQLStatementParser.SelectorFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#selectorFunctionName_}.
	 * @param ctx the parse tree
	 */
	void enterSelectorFunctionName_(LinglongSQLStatementParser.SelectorFunctionName_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#selectorFunctionName_}.
	 * @param ctx the parse tree
	 */
	void exitSelectorFunctionName_(LinglongSQLStatementParser.SelectorFunctionName_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#transformationFunction_}.
	 * @param ctx the parse tree
	 */
	void enterTransformationFunction_(LinglongSQLStatementParser.TransformationFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#transformationFunction_}.
	 * @param ctx the parse tree
	 */
	void exitTransformationFunction_(LinglongSQLStatementParser.TransformationFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#transformationFunctionName_}.
	 * @param ctx the parse tree
	 */
	void enterTransformationFunctionName_(LinglongSQLStatementParser.TransformationFunctionName_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#transformationFunctionName_}.
	 * @param ctx the parse tree
	 */
	void exitTransformationFunctionName_(LinglongSQLStatementParser.TransformationFunctionName_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#distinct}.
	 * @param ctx the parse tree
	 */
	void enterDistinct(LinglongSQLStatementParser.DistinctContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#distinct}.
	 * @param ctx the parse tree
	 */
	void exitDistinct(LinglongSQLStatementParser.DistinctContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#overClause_}.
	 * @param ctx the parse tree
	 */
	void enterOverClause_(LinglongSQLStatementParser.OverClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#overClause_}.
	 * @param ctx the parse tree
	 */
	void exitOverClause_(LinglongSQLStatementParser.OverClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#windowSpecification_}.
	 * @param ctx the parse tree
	 */
	void enterWindowSpecification_(LinglongSQLStatementParser.WindowSpecification_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#windowSpecification_}.
	 * @param ctx the parse tree
	 */
	void exitWindowSpecification_(LinglongSQLStatementParser.WindowSpecification_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#partitionClause_}.
	 * @param ctx the parse tree
	 */
	void enterPartitionClause_(LinglongSQLStatementParser.PartitionClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#partitionClause_}.
	 * @param ctx the parse tree
	 */
	void exitPartitionClause_(LinglongSQLStatementParser.PartitionClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#frameClause_}.
	 * @param ctx the parse tree
	 */
	void enterFrameClause_(LinglongSQLStatementParser.FrameClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#frameClause_}.
	 * @param ctx the parse tree
	 */
	void exitFrameClause_(LinglongSQLStatementParser.FrameClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#frameStart_}.
	 * @param ctx the parse tree
	 */
	void enterFrameStart_(LinglongSQLStatementParser.FrameStart_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#frameStart_}.
	 * @param ctx the parse tree
	 */
	void exitFrameStart_(LinglongSQLStatementParser.FrameStart_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#frameEnd_}.
	 * @param ctx the parse tree
	 */
	void enterFrameEnd_(LinglongSQLStatementParser.FrameEnd_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#frameEnd_}.
	 * @param ctx the parse tree
	 */
	void exitFrameEnd_(LinglongSQLStatementParser.FrameEnd_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#frameBetween_}.
	 * @param ctx the parse tree
	 */
	void enterFrameBetween_(LinglongSQLStatementParser.FrameBetween_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#frameBetween_}.
	 * @param ctx the parse tree
	 */
	void exitFrameBetween_(LinglongSQLStatementParser.FrameBetween_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#specialFunction_}.
	 * @param ctx the parse tree
	 */
	void enterSpecialFunction_(LinglongSQLStatementParser.SpecialFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#specialFunction_}.
	 * @param ctx the parse tree
	 */
	void exitSpecialFunction_(LinglongSQLStatementParser.SpecialFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#groupConcatFunction_}.
	 * @param ctx the parse tree
	 */
	void enterGroupConcatFunction_(LinglongSQLStatementParser.GroupConcatFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#groupConcatFunction_}.
	 * @param ctx the parse tree
	 */
	void exitGroupConcatFunction_(LinglongSQLStatementParser.GroupConcatFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#windowFunction_}.
	 * @param ctx the parse tree
	 */
	void enterWindowFunction_(LinglongSQLStatementParser.WindowFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#windowFunction_}.
	 * @param ctx the parse tree
	 */
	void exitWindowFunction_(LinglongSQLStatementParser.WindowFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#castFunction_}.
	 * @param ctx the parse tree
	 */
	void enterCastFunction_(LinglongSQLStatementParser.CastFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#castFunction_}.
	 * @param ctx the parse tree
	 */
	void exitCastFunction_(LinglongSQLStatementParser.CastFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#convertFunction_}.
	 * @param ctx the parse tree
	 */
	void enterConvertFunction_(LinglongSQLStatementParser.ConvertFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#convertFunction_}.
	 * @param ctx the parse tree
	 */
	void exitConvertFunction_(LinglongSQLStatementParser.ConvertFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#positionFunction_}.
	 * @param ctx the parse tree
	 */
	void enterPositionFunction_(LinglongSQLStatementParser.PositionFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#positionFunction_}.
	 * @param ctx the parse tree
	 */
	void exitPositionFunction_(LinglongSQLStatementParser.PositionFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#substringFunction_}.
	 * @param ctx the parse tree
	 */
	void enterSubstringFunction_(LinglongSQLStatementParser.SubstringFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#substringFunction_}.
	 * @param ctx the parse tree
	 */
	void exitSubstringFunction_(LinglongSQLStatementParser.SubstringFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#extractFunction_}.
	 * @param ctx the parse tree
	 */
	void enterExtractFunction_(LinglongSQLStatementParser.ExtractFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#extractFunction_}.
	 * @param ctx the parse tree
	 */
	void exitExtractFunction_(LinglongSQLStatementParser.ExtractFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#charFunction_}.
	 * @param ctx the parse tree
	 */
	void enterCharFunction_(LinglongSQLStatementParser.CharFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#charFunction_}.
	 * @param ctx the parse tree
	 */
	void exitCharFunction_(LinglongSQLStatementParser.CharFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#trimFunction_}.
	 * @param ctx the parse tree
	 */
	void enterTrimFunction_(LinglongSQLStatementParser.TrimFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#trimFunction_}.
	 * @param ctx the parse tree
	 */
	void exitTrimFunction_(LinglongSQLStatementParser.TrimFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#weightStringFunction_}.
	 * @param ctx the parse tree
	 */
	void enterWeightStringFunction_(LinglongSQLStatementParser.WeightStringFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#weightStringFunction_}.
	 * @param ctx the parse tree
	 */
	void exitWeightStringFunction_(LinglongSQLStatementParser.WeightStringFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#levelClause_}.
	 * @param ctx the parse tree
	 */
	void enterLevelClause_(LinglongSQLStatementParser.LevelClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#levelClause_}.
	 * @param ctx the parse tree
	 */
	void exitLevelClause_(LinglongSQLStatementParser.LevelClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#levelInWeightListElement_}.
	 * @param ctx the parse tree
	 */
	void enterLevelInWeightListElement_(LinglongSQLStatementParser.LevelInWeightListElement_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#levelInWeightListElement_}.
	 * @param ctx the parse tree
	 */
	void exitLevelInWeightListElement_(LinglongSQLStatementParser.LevelInWeightListElement_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#regularFunction_}.
	 * @param ctx the parse tree
	 */
	void enterRegularFunction_(LinglongSQLStatementParser.RegularFunction_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#regularFunction_}.
	 * @param ctx the parse tree
	 */
	void exitRegularFunction_(LinglongSQLStatementParser.RegularFunction_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#regularFunctionName_}.
	 * @param ctx the parse tree
	 */
	void enterRegularFunctionName_(LinglongSQLStatementParser.RegularFunctionName_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#regularFunctionName_}.
	 * @param ctx the parse tree
	 */
	void exitRegularFunctionName_(LinglongSQLStatementParser.RegularFunctionName_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#matchExpression_}.
	 * @param ctx the parse tree
	 */
	void enterMatchExpression_(LinglongSQLStatementParser.MatchExpression_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#matchExpression_}.
	 * @param ctx the parse tree
	 */
	void exitMatchExpression_(LinglongSQLStatementParser.MatchExpression_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#matchSearchModifier_}.
	 * @param ctx the parse tree
	 */
	void enterMatchSearchModifier_(LinglongSQLStatementParser.MatchSearchModifier_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#matchSearchModifier_}.
	 * @param ctx the parse tree
	 */
	void exitMatchSearchModifier_(LinglongSQLStatementParser.MatchSearchModifier_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#caseExpression_}.
	 * @param ctx the parse tree
	 */
	void enterCaseExpression_(LinglongSQLStatementParser.CaseExpression_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#caseExpression_}.
	 * @param ctx the parse tree
	 */
	void exitCaseExpression_(LinglongSQLStatementParser.CaseExpression_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#caseWhen_}.
	 * @param ctx the parse tree
	 */
	void enterCaseWhen_(LinglongSQLStatementParser.CaseWhen_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#caseWhen_}.
	 * @param ctx the parse tree
	 */
	void exitCaseWhen_(LinglongSQLStatementParser.CaseWhen_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#caseElse_}.
	 * @param ctx the parse tree
	 */
	void enterCaseElse_(LinglongSQLStatementParser.CaseElse_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#caseElse_}.
	 * @param ctx the parse tree
	 */
	void exitCaseElse_(LinglongSQLStatementParser.CaseElse_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#intervalExpression_}.
	 * @param ctx the parse tree
	 */
	void enterIntervalExpression_(LinglongSQLStatementParser.IntervalExpression_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#intervalExpression_}.
	 * @param ctx the parse tree
	 */
	void exitIntervalExpression_(LinglongSQLStatementParser.IntervalExpression_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#intervalUnit_}.
	 * @param ctx the parse tree
	 */
	void enterIntervalUnit_(LinglongSQLStatementParser.IntervalUnit_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#intervalUnit_}.
	 * @param ctx the parse tree
	 */
	void exitIntervalUnit_(LinglongSQLStatementParser.IntervalUnit_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void enterOrderByClause(LinglongSQLStatementParser.OrderByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void exitOrderByClause(LinglongSQLStatementParser.OrderByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#orderByItem}.
	 * @param ctx the parse tree
	 */
	void enterOrderByItem(LinglongSQLStatementParser.OrderByItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#orderByItem}.
	 * @param ctx the parse tree
	 */
	void exitOrderByItem(LinglongSQLStatementParser.OrderByItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDataType(LinglongSQLStatementParser.DataTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDataType(LinglongSQLStatementParser.DataTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#dataTypeName_}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeName_(LinglongSQLStatementParser.DataTypeName_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#dataTypeName_}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeName_(LinglongSQLStatementParser.DataTypeName_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeLength(LinglongSQLStatementParser.DataTypeLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#dataTypeLength}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeLength(LinglongSQLStatementParser.DataTypeLengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#characterSet_}.
	 * @param ctx the parse tree
	 */
	void enterCharacterSet_(LinglongSQLStatementParser.CharacterSet_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#characterSet_}.
	 * @param ctx the parse tree
	 */
	void exitCharacterSet_(LinglongSQLStatementParser.CharacterSet_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#collateClause_}.
	 * @param ctx the parse tree
	 */
	void enterCollateClause_(LinglongSQLStatementParser.CollateClause_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#collateClause_}.
	 * @param ctx the parse tree
	 */
	void exitCollateClause_(LinglongSQLStatementParser.CollateClause_Context ctx);
	/**
	 * Enter a parse tree produced by {@link LinglongSQLStatementParser#ignoredIdentifier_}.
	 * @param ctx the parse tree
	 */
	void enterIgnoredIdentifier_(LinglongSQLStatementParser.IgnoredIdentifier_Context ctx);
	/**
	 * Exit a parse tree produced by {@link LinglongSQLStatementParser#ignoredIdentifier_}.
	 * @param ctx the parse tree
	 */
	void exitIgnoredIdentifier_(LinglongSQLStatementParser.IgnoredIdentifier_Context ctx);
}