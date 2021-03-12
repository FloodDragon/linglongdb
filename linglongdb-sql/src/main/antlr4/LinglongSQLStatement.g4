grammar LinglongSQLStatement;

import Symbol, Keyword, LinglongSQLKeyword, Literals, BaseRule;

execute
    : selectClause
    ;

selectClause
    : SELECT selectItems fromClause whereClause? groupByClause? orderByClause? limitClause?
    ;

selectItems
    : (unqualifiedShorthand | selectItem) (COMMA_ selectItem)*
    ;

selectItem
    : (columnName | expr) (AS? alias)? | qualifiedShorthand
    ;

alias
    : identifier_ | STRING_
    ;

unqualifiedShorthand
    : ASTERISK_
    ;

qualifiedShorthand
    : identifier_ DOT_ASTERISK_
    ;

fromClause
    : FROM tableReferences
    ;

tableReferences
    : escapedTableReference_ (COMMA_ escapedTableReference_)*
    ;

escapedTableReference_
    : tableReference
    ;

tableReference
    : tableFactor
    ;

tableFactor
    : tableName (AS? alias)? | subquery columnNames? | LP_ tableReferences RP_
    ;

whereClause
    : WHERE expr
    ;

groupByClause
    : GROUP BY orderByItem (COMMA_ orderByItem)* (WITH ROLLUP)?
    ;

limitClause
    : LIMIT ((limitOffset COMMA_)? limitRowCount | limitRowCount OFFSET limitOffset)
    ;

limitRowCount
    : numberLiterals | parameterMarker
    ;

limitOffset
    : numberLiterals | parameterMarker
    ;

subquery
    : LP_ selectClause RP_ AS? alias?
    ;

