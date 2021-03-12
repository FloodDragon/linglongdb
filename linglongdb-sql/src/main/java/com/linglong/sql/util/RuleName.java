
package com.linglong.sql.util;

/**
 * 规则名字
 * @author Stereo
 */
public enum RuleName {

    OWNER("Owner"),

    NAME("Name"),

    SCHEMA_NAME("SchemaName"),

    TABLE_NAME("TableName"),

    RP_NAME("RpName"),

    DB_NAME("DbName"),

    COLUMN_DEFINITION("ColumnDefinition"),

    COLUMN_NAME("ColumnName"),

    DATA_TYPE("DataType"),

    DATA_TYPE_LENGTH("DataTypeLength"),

    FIRST_OR_AFTER_COLUMN("FirstOrAfterColumn"),

    PRIMARY_KEY("PrimaryKey"),

    COLUMN_NAMES("ColumnNames"),

    INDEX_NAME("IndexName"),

    ADD_COLUMN_SPECIFICATION("AddColumnSpecification"),

    CHANGE_COLUMN_SPECIFICATION("ChangeColumnSpecification"),

    DROP_COLUMN_SPECIFICATION("DropColumnSpecification"),

    MODIFY_COLUMN_SPECIFICATION("ModifyColumnSpecification"),

    RENAME_COLUMN_SPECIFICATION("RenameColumnSpecification"),

    DROP_PRIMARY_KEY_SPECIFICATION("DropPrimaryKeySpecification"),

    ADD_INDEX_SPECIFICATION("AddIndexSpecification"),

    RENAME_INDEX_SPECIFICATION("RenameIndexSpecification"),

    DROP_INDEX_SPECIFICATION("DropIndexSpecification"),

    ADD_CONSTRAINT_SPECIFICATION("AddConstraintSpecification"),

    DROP_CONSTRAINT_CLAUSE("DropConstraintClause"),

    MODIFY_COL_PROPERTIES("ModifyColProperties"),

    INSERT_VALUES_CLAUSE("InsertValuesClause"),

    ON_DUPLICATE_KEY_CLAUSE("OnDuplicateKeyClause"),

    SET_ASSIGNMENTS_CLAUSE("SetAssignmentsClause"),

    ASSIGNMENT_VALUES("AssignmentValues"),

    ASSIGNMENT_VALUE("AssignmentValue"),

    ASSIGNMENT("Assignment"),

    DUPLICATE_SPECIFICATION("DuplicateSpecification"),

    SELECT_ITEMS("SelectItems"),

    SELECT_ITEM("SelectItem"),

    SELECT_CLAUSE("SelectClause"),

    UNQUALIFIED_SHORTHAND("UnqualifiedShorthand"),

    QUALIFIED_SHORTHAND("QualifiedShorthand"),

    FUNCTION_CALL("FunctionCall"),

    AGGREGATION_FUNCTION("AggregationFunction"),

    DISTINCT("Distinct"),

    TABLE_CONSTRAINT("TableConstraint"),

    TABLE_FACTOR("TableFactor"),

    TABLE_REFERENCES("TableReferences"),

    TABLE_REFERENCE("TableReference"),

    ALIAS("Alias"),

    PARAMETER_MARKER("ParameterMarker"),

    LITERALS("Literals"),

    NUMBER_LITERALS("NumberLiterals"),

    STRING_LITERALS("StringLiterals"),

    EXPR("Expr"),

    SIMPLE_EXPR("SimpleExpr"),

    BIT_EXPR("BitExpr"),

    LOGICAL_OPERATOR("LogicalOperator"),

    FROM_CLAUSE("FromClause"),

    WHERE_CLAUSE("WhereClause"),

    GROUP_BY_CLAUSE("GroupByClause"),

    ORDER_BY_CLAUSE("OrderByClause"),

    ORDER_BY_ITEM("OrderByItem"),

    COMPARISON_OPERATOR("ComparisonOperator"),

    PREDICATE("Predicate"),

    LIMIT_CLAUSE("LimitClause"),

    LIMIT_ROW_COUNT("LimitRowCount"),

    LIMIT_OFFSET("LimitOffset"),

    SUBQUERY("Subquery"),

    AUTO_COMMIT_VALUE("AutoCommitValue"),

    IMPLICIT_TRANSACTIONS_VALUE("ImplicitTransactionsValue"),

    SHOW_LIKE("ShowLike"),

    FROM_SCHEMA("FromSchema"),

    TOP("Top");

    private final String name;

    public String getName() {
        return name + "Context";
    }

    RuleName(String name) {
        this.name = name;
    }
}
