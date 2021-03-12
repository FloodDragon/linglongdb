package com.linglong.sql.antlr4;// Generated from D:/IdeaProjects/linglongdb/linglongdb-sql/src/main/antlr4\LinglongSQLStatement.g4 by ANTLR 4.7.2
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class LinglongSQLStatementParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		AND_=1, OR_=2, NOT_=3, TILDE_=4, VERTICAL_BAR_=5, AMPERSAND_=6, SIGNED_LEFT_SHIFT_=7, 
		SIGNED_RIGHT_SHIFT_=8, CARET_=9, MOD_=10, COLON_=11, PLUS_=12, MINUS_=13, 
		ASTERISK_=14, SLASH_=15, BACKSLASH_=16, DOT_=17, DOT_ASTERISK_=18, SAFE_EQ_=19, 
		DEQ_=20, EQ_=21, NEQ_=22, GT_=23, GTE_=24, LT_=25, LTE_=26, POUND_=27, 
		LP_=28, RP_=29, LBE_=30, RBE_=31, LBT_=32, RBT_=33, COMMA_=34, DQ_=35, 
		SQ_=36, BQ_=37, QUESTION_=38, AT_=39, SEMI_=40, WS=41, SELECT=42, INSERT=43, 
		UPDATE=44, DELETE=45, CREATE=46, ALTER=47, DROP=48, TRUNCATE=49, SCHEMA=50, 
		GRANT=51, REVOKE=52, ADD=53, SET=54, TABLE=55, COLUMN=56, INDEX=57, CONSTRAINT=58, 
		PRIMARY=59, UNIQUE=60, FOREIGN=61, KEY=62, POSITION=63, PRECISION=64, 
		FUNCTION=65, TRIGGER=66, PROCEDURE=67, VIEW=68, INTO=69, VALUES=70, WITH=71, 
		UNION=72, DISTINCT=73, CASE=74, WHEN=75, CAST=76, TRIM=77, SUBSTRING=78, 
		FROM=79, NATURAL=80, JOIN=81, FULL=82, INNER=83, OUTER=84, LEFT=85, RIGHT=86, 
		CROSS=87, USING=88, WHERE=89, AS=90, ON=91, IF=92, ELSE=93, THEN=94, FOR=95, 
		TO=96, AND=97, OR=98, IS=99, NOT=100, NULL=101, TRUE=102, FALSE=103, EXISTS=104, 
		BETWEEN=105, IN=106, ALL=107, ANY=108, LIKE=109, ORDER=110, GROUP=111, 
		BY=112, ASC=113, DESC=114, HAVING=115, LIMIT=116, OFFSET=117, BEGIN=118, 
		COMMIT=119, ROLLBACK=120, SAVEPOINT=121, BOOLEAN=122, DOUBLE=123, CHAR=124, 
		CHARACTER=125, ARRAY=126, INTERVAL=127, DATE=128, TIME=129, TIMESTAMP=130, 
		LOCALTIME=131, LOCALTIMESTAMP=132, YEAR=133, QUARTER=134, MONTH=135, WEEK=136, 
		DAY=137, HOUR=138, MINUTE=139, SECOND=140, MICROSECOND=141, MAX=142, MIN=143, 
		SUM=144, COUNT=145, AVG=146, DEFAULT=147, CURRENT=148, ENABLE=149, DISABLE=150, 
		CALL=151, INSTANCE=152, PRESERVE=153, DO=154, DEFINER=155, CURRENT_USER=156, 
		SQL=157, CASCADED=158, LOCAL=159, CLOSE=160, OPEN=161, NEXT=162, NAME=163, 
		FOR_GENERATOR=164, CONTINUOUS=165, DATABASE=166, DATABASES=167, DESTINATIONS=168, 
		DIAGNOSTICS=169, DURATION=170, END=171, EVERY=172, EXPLAIN=173, FIELD=174, 
		GRANTS=175, GROUPS=176, INF=177, KEYS=178, KILL=179, SHOW=180, MEASUREMENT=181, 
		MEASUREMENTS=182, PASSWORD=183, POLICY=184, POLICIES=185, PRIVILEGES=186, 
		QUERIES=187, QUERY=188, READ=189, REPLICATION=190, RESAMPLE=191, RETENTION=192, 
		SERIES=193, SHARD=194, SHARDS=195, SLIMIT=196, STATS=197, SUBSCRIPTION=198, 
		TAG=199, USER=200, USERS=201, WRITE=202, INTEGRAL=203, MEAN=204, MEDIAN=205, 
		MODE=206, SPREAD=207, STDDEV=208, BOTTOM=209, FIRST=210, LAST=211, PERCENTILE=212, 
		SAMPLE=213, TOP=214, ABS=215, ACOS=216, ASIN=217, ATAN=218, ATAN2=219, 
		CEIL=220, COS=221, CUMULATIVE_SUM=222, DERIVATIVE=223, DIFFERENCE=224, 
		ELAPSED=225, EXP=226, FLOOR=227, HISTOGRAM=228, LN=229, LOG=230, LOG2=231, 
		LOG10=232, MOVING_AVERAGE=233, NON_NEGATIVE_DERIVATIVE=234, NON_NEGATIVE_DIFFERENCE=235, 
		POW=236, ROUND=237, SIN=238, SQRT=239, TAN=240, HOLT_WINTERS=241, CHANDE_MOMENTUM_OSCILLATOR=242, 
		EXPONENTIAL_MOVING_AVERAGE=243, DOUBLE_EXPONENTIAL_MOVING_AVERAGE=244, 
		KAUFMANS_EFFICIENCY_RATIO=245, KAUFMANS_ADAPTIVE_MOVING_AVERAGE=246, TRIPLE_EXPONENTIAL_MOVING_AVERAGE=247, 
		RELATIVE_STRENGTH_INDEX=248, USE=249, DESCRIBE=250, SCHEMAS=251, TABLES=252, 
		TABLESPACE=253, COLUMNS=254, FIELDS=255, INDEXES=256, STATUS=257, REPLACE=258, 
		MODIFY=259, DISTINCTROW=260, VALUE=261, DUPLICATE=262, AFTER=263, OJ=264, 
		WINDOW=265, MOD=266, DIV=267, XOR=268, REGEXP=269, RLIKE=270, ACCOUNT=271, 
		ROLE=272, START=273, TRANSACTION=274, ROW=275, ROWS=276, WITHOUT=277, 
		BINARY=278, ESCAPE=279, GENERATED=280, PARTITION=281, SUBPARTITION=282, 
		STORAGE=283, STORED=284, SUPER=285, SUBSTR=286, TEMPORARY=287, THAN=288, 
		TRAILING=289, UNBOUNDED=290, UNLOCK=291, UNSIGNED=292, UPGRADE=293, USAGE=294, 
		VALIDATION=295, VIRTUAL=296, ROLLUP=297, SOUNDS=298, UNKNOWN=299, OFF=300, 
		ALWAYS=301, CASCADE=302, CHECK=303, COMMITTED=304, LEVEL=305, NO=306, 
		OPTION=307, REFERENCES=308, ACTION=309, ALGORITHM=310, ANALYZE=311, AUTOCOMMIT=312, 
		MAXVALUE=313, BOTH=314, BTREE=315, CHAIN=316, CHANGE=317, CHARSET=318, 
		CHECKSUM=319, CIPHER=320, CLIENT=321, COALESCE=322, COLLATE=323, COMMENT=324, 
		COMPACT=325, COMPRESSED=326, COMPRESSION=327, CONNECTION=328, CONSISTENT=329, 
		CONVERT=330, COPY=331, DATA=332, DELAYED=333, DIRECTORY=334, DISCARD=335, 
		DISK=336, DYNAMIC=337, ENCRYPTION=338, ENGINE=339, EVENT=340, EXCEPT=341, 
		EXCHANGE=342, EXCLUSIVE=343, EXECUTE=344, EXTRACT=345, FILE=346, FIXED=347, 
		FOLLOWING=348, FORCE=349, FULLTEXT=350, GLOBAL=351, HASH=352, IDENTIFIED=353, 
		IGNORE=354, IMPORT_=355, INPLACE=356, LEADING=357, LESS=358, LINEAR=359, 
		LOCK=360, MATCH=361, MEMORY=362, NONE=363, NOW=364, OFFLINE=365, ONLINE=366, 
		OPTIMIZE=367, OVER=368, PARSER=369, PARTIAL=370, PARTITIONING=371, PERSIST=372, 
		PRECEDING=373, PROCESS=374, PROXY=375, QUICK=376, RANGE=377, REBUILD=378, 
		RECURSIVE=379, REDUNDANT=380, RELEASE=381, RELOAD=382, REMOVE=383, RENAME=384, 
		REORGANIZE=385, REPAIR=386, REQUIRE=387, RESTRICT=388, REVERSE=389, ROUTINE=390, 
		SEPARATOR=391, SESSION=392, SHARED=393, SHUTDOWN=394, SIMPLE=395, SLAVE=396, 
		SPATIAL=397, ZEROFILL=398, VISIBLE=399, INVISIBLE=400, INSTANT=401, ENFORCED=402, 
		AGAINST=403, LANGUAGE=404, EXTENDED=405, EXPANSION=406, VARIANCE=407, 
		MAX_ROWS=408, MIN_ROWS=409, HIGH_PRIORITY=410, LOW_PRIORITY=411, SQL_BIG_RESULT=412, 
		SQL_BUFFER_RESULT=413, SQL_CACHE=414, SQL_CALC_FOUND_ROWS=415, SQL_NO_CACHE=416, 
		SQL_SMALL_RESULT=417, STATS_AUTO_RECALC=418, STATS_PERSISTENT=419, STATS_SAMPLE_PAGES=420, 
		ROLE_ADMIN=421, ROW_FORMAT=422, SET_USER_ID=423, REPLICATION_SLAVE_ADMIN=424, 
		GROUP_REPLICATION_ADMIN=425, STRAIGHT_JOIN=426, WEIGHT_STRING=427, COLUMN_FORMAT=428, 
		CONNECTION_ADMIN=429, FIREWALL_ADMIN=430, FIREWALL_USER=431, INSERT_METHOD=432, 
		KEY_BLOCK_SIZE=433, PACK_KEYS=434, PERSIST_ONLY=435, BIT_AND=436, BIT_OR=437, 
		BIT_XOR=438, GROUP_CONCAT=439, JSON_ARRAYAGG=440, JSON_OBJECTAGG=441, 
		STD=442, STDDEV_POP=443, STDDEV_SAMP=444, VAR_POP=445, VAR_SAMP=446, AUDIT_ADMIN=447, 
		AUTO_INCREMENT=448, AVG_ROW_LENGTH=449, BINLOG_ADMIN=450, DELAY_KEY_WRITE=451, 
		ENCRYPTION_KEY_ADMIN=452, SYSTEM_VARIABLES_ADMIN=453, VERSION_TOKEN_ADMIN=454, 
		CURRENT_TIMESTAMP=455, YEAR_MONTH=456, DAY_HOUR=457, DAY_MINUTE=458, DAY_SECOND=459, 
		DAY_MICROSECOND=460, HOUR_MINUTE=461, HOUR_SECOND=462, HOUR_MICROSECOND=463, 
		MINUTE_SECOND=464, MINUTE_MICROSECOND=465, SECOND_MICROSECOND=466, UL_BINARY=467, 
		ROTATE=468, MASTER=469, BINLOG=470, ERROR=471, SCHEDULE=472, COMPLETION=473, 
		STARTS=474, ENDS=475, HOST=476, SOCKET=477, PORT=478, SERVER=479, WRAPPER=480, 
		OPTIONS=481, OWNER=482, DETERMINISTIC=483, RETURNS=484, CONTAINS=485, 
		READS=486, MODIFIES=487, SECURITY=488, INVOKER=489, OUT=490, TEMPTABLE=491, 
		MERGE=492, UNDEFINED=493, DATAFILE=494, FILE_BLOCK_SIZE=495, EXTENT_SIZE=496, 
		INITIAL_SIZE=497, AUTOEXTEND_SIZE=498, MAX_SIZE=499, NODEGROUP=500, WAIT=501, 
		LOGFILE=502, UNDOFILE=503, UNDO_BUFFER_SIZE=504, REDO_BUFFER_SIZE=505, 
		HANDLER=506, PREV=507, ORGANIZATION=508, DEFINITION=509, DESCRIPTION=510, 
		REFERENCE=511, FOLLOWS=512, PRECEDES=513, IMPORT=514, LOAD=515, CONCURRENT=516, 
		INFILE=517, LINES=518, STARTING=519, TERMINATED=520, OPTIONALLY=521, ENCLOSED=522, 
		ESCAPED=523, XML=524, UNDO=525, DUMPFILE=526, OUTFILE=527, SHARE=528, 
		IDENTIFIER_=529, STRING_=530, NUMBER_=531, HEX_DIGIT_=532, BIT_NUM_=533;
	public static final int
		RULE_execute = 0, RULE_selectClause = 1, RULE_selectItems = 2, RULE_selectItem = 3, 
		RULE_alias = 4, RULE_unqualifiedShorthand = 5, RULE_qualifiedShorthand = 6, 
		RULE_fromClause = 7, RULE_tableReferences = 8, RULE_escapedTableReference_ = 9, 
		RULE_tableReference = 10, RULE_tableFactor = 11, RULE_whereClause = 12, 
		RULE_groupByClause = 13, RULE_limitClause = 14, RULE_limitRowCount = 15, 
		RULE_limitOffset = 16, RULE_subquery = 17, RULE_parameterMarker = 18, 
		RULE_literals = 19, RULE_stringLiterals = 20, RULE_numberLiterals = 21, 
		RULE_dateTimeLiterals = 22, RULE_hexadecimalLiterals = 23, RULE_bitValueLiterals = 24, 
		RULE_booleanLiterals = 25, RULE_nullValueLiterals = 26, RULE_identifier_ = 27, 
		RULE_variable_ = 28, RULE_unreservedWord_ = 29, RULE_tableName = 30, RULE_dbName = 31, 
		RULE_rpName = 32, RULE_columnName = 33, RULE_owner = 34, RULE_name = 35, 
		RULE_columnNames = 36, RULE_characterSetName_ = 37, RULE_expr = 38, RULE_logicalOperator = 39, 
		RULE_notOperator_ = 40, RULE_booleanPrimary_ = 41, RULE_comparisonOperator = 42, 
		RULE_predicate = 43, RULE_bitExpr = 44, RULE_simpleExpr = 45, RULE_functionCall = 46, 
		RULE_aggregationFunction = 47, RULE_aggregationFunctionName_ = 48, RULE_selectorFunction_ = 49, 
		RULE_selectorFunctionName_ = 50, RULE_transformationFunction_ = 51, RULE_transformationFunctionName_ = 52, 
		RULE_distinct = 53, RULE_overClause_ = 54, RULE_windowSpecification_ = 55, 
		RULE_partitionClause_ = 56, RULE_frameClause_ = 57, RULE_frameStart_ = 58, 
		RULE_frameEnd_ = 59, RULE_frameBetween_ = 60, RULE_specialFunction_ = 61, 
		RULE_groupConcatFunction_ = 62, RULE_windowFunction_ = 63, RULE_castFunction_ = 64, 
		RULE_convertFunction_ = 65, RULE_positionFunction_ = 66, RULE_substringFunction_ = 67, 
		RULE_extractFunction_ = 68, RULE_charFunction_ = 69, RULE_trimFunction_ = 70, 
		RULE_weightStringFunction_ = 71, RULE_levelClause_ = 72, RULE_levelInWeightListElement_ = 73, 
		RULE_regularFunction_ = 74, RULE_regularFunctionName_ = 75, RULE_matchExpression_ = 76, 
		RULE_matchSearchModifier_ = 77, RULE_caseExpression_ = 78, RULE_caseWhen_ = 79, 
		RULE_caseElse_ = 80, RULE_intervalExpression_ = 81, RULE_intervalUnit_ = 82, 
		RULE_orderByClause = 83, RULE_orderByItem = 84, RULE_dataType = 85, RULE_dataTypeName_ = 86, 
		RULE_dataTypeLength = 87, RULE_characterSet_ = 88, RULE_collateClause_ = 89, 
		RULE_ignoredIdentifier_ = 90;
	private static String[] makeRuleNames() {
		return new String[] {
			"execute", "selectClause", "selectItems", "selectItem", "alias", "unqualifiedShorthand", 
			"qualifiedShorthand", "fromClause", "tableReferences", "escapedTableReference_", 
			"tableReference", "tableFactor", "whereClause", "groupByClause", "limitClause", 
			"limitRowCount", "limitOffset", "subquery", "parameterMarker", "literals", 
			"stringLiterals", "numberLiterals", "dateTimeLiterals", "hexadecimalLiterals", 
			"bitValueLiterals", "booleanLiterals", "nullValueLiterals", "identifier_", 
			"variable_", "unreservedWord_", "tableName", "dbName", "rpName", "columnName", 
			"owner", "name", "columnNames", "characterSetName_", "expr", "logicalOperator", 
			"notOperator_", "booleanPrimary_", "comparisonOperator", "predicate", 
			"bitExpr", "simpleExpr", "functionCall", "aggregationFunction", "aggregationFunctionName_", 
			"selectorFunction_", "selectorFunctionName_", "transformationFunction_", 
			"transformationFunctionName_", "distinct", "overClause_", "windowSpecification_", 
			"partitionClause_", "frameClause_", "frameStart_", "frameEnd_", "frameBetween_", 
			"specialFunction_", "groupConcatFunction_", "windowFunction_", "castFunction_", 
			"convertFunction_", "positionFunction_", "substringFunction_", "extractFunction_", 
			"charFunction_", "trimFunction_", "weightStringFunction_", "levelClause_", 
			"levelInWeightListElement_", "regularFunction_", "regularFunctionName_", 
			"matchExpression_", "matchSearchModifier_", "caseExpression_", "caseWhen_", 
			"caseElse_", "intervalExpression_", "intervalUnit_", "orderByClause", 
			"orderByItem", "dataType", "dataTypeName_", "dataTypeLength", "characterSet_", 
			"collateClause_", "ignoredIdentifier_"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'&&'", "'||'", "'!'", "'~'", "'|'", "'&'", "'<<'", "'>>'", "'^'", 
			"'%'", "':'", "'+'", "'-'", "'*'", "'/'", "'\\'", "'.'", "'.*'", "'<=>'", 
			"'=='", "'='", null, "'>'", "'>='", "'<'", "'<='", "'#'", "'('", "')'", 
			"'{'", "'}'", "'['", "']'", "','", "'\"'", "'''", "'`'", "'?'", "'@'", 
			"';'", null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, "'DO NOT MATCH ANY THING, JUST FOR GENERATOR'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "AND_", "OR_", "NOT_", "TILDE_", "VERTICAL_BAR_", "AMPERSAND_", 
			"SIGNED_LEFT_SHIFT_", "SIGNED_RIGHT_SHIFT_", "CARET_", "MOD_", "COLON_", 
			"PLUS_", "MINUS_", "ASTERISK_", "SLASH_", "BACKSLASH_", "DOT_", "DOT_ASTERISK_", 
			"SAFE_EQ_", "DEQ_", "EQ_", "NEQ_", "GT_", "GTE_", "LT_", "LTE_", "POUND_", 
			"LP_", "RP_", "LBE_", "RBE_", "LBT_", "RBT_", "COMMA_", "DQ_", "SQ_", 
			"BQ_", "QUESTION_", "AT_", "SEMI_", "WS", "SELECT", "INSERT", "UPDATE", 
			"DELETE", "CREATE", "ALTER", "DROP", "TRUNCATE", "SCHEMA", "GRANT", "REVOKE", 
			"ADD", "SET", "TABLE", "COLUMN", "INDEX", "CONSTRAINT", "PRIMARY", "UNIQUE", 
			"FOREIGN", "KEY", "POSITION", "PRECISION", "FUNCTION", "TRIGGER", "PROCEDURE", 
			"VIEW", "INTO", "VALUES", "WITH", "UNION", "DISTINCT", "CASE", "WHEN", 
			"CAST", "TRIM", "SUBSTRING", "FROM", "NATURAL", "JOIN", "FULL", "INNER", 
			"OUTER", "LEFT", "RIGHT", "CROSS", "USING", "WHERE", "AS", "ON", "IF", 
			"ELSE", "THEN", "FOR", "TO", "AND", "OR", "IS", "NOT", "NULL", "TRUE", 
			"FALSE", "EXISTS", "BETWEEN", "IN", "ALL", "ANY", "LIKE", "ORDER", "GROUP", 
			"BY", "ASC", "DESC", "HAVING", "LIMIT", "OFFSET", "BEGIN", "COMMIT", 
			"ROLLBACK", "SAVEPOINT", "BOOLEAN", "DOUBLE", "CHAR", "CHARACTER", "ARRAY", 
			"INTERVAL", "DATE", "TIME", "TIMESTAMP", "LOCALTIME", "LOCALTIMESTAMP", 
			"YEAR", "QUARTER", "MONTH", "WEEK", "DAY", "HOUR", "MINUTE", "SECOND", 
			"MICROSECOND", "MAX", "MIN", "SUM", "COUNT", "AVG", "DEFAULT", "CURRENT", 
			"ENABLE", "DISABLE", "CALL", "INSTANCE", "PRESERVE", "DO", "DEFINER", 
			"CURRENT_USER", "SQL", "CASCADED", "LOCAL", "CLOSE", "OPEN", "NEXT", 
			"NAME", "FOR_GENERATOR", "CONTINUOUS", "DATABASE", "DATABASES", "DESTINATIONS", 
			"DIAGNOSTICS", "DURATION", "END", "EVERY", "EXPLAIN", "FIELD", "GRANTS", 
			"GROUPS", "INF", "KEYS", "KILL", "SHOW", "MEASUREMENT", "MEASUREMENTS", 
			"PASSWORD", "POLICY", "POLICIES", "PRIVILEGES", "QUERIES", "QUERY", "READ", 
			"REPLICATION", "RESAMPLE", "RETENTION", "SERIES", "SHARD", "SHARDS", 
			"SLIMIT", "STATS", "SUBSCRIPTION", "TAG", "USER", "USERS", "WRITE", "INTEGRAL", 
			"MEAN", "MEDIAN", "MODE", "SPREAD", "STDDEV", "BOTTOM", "FIRST", "LAST", 
			"PERCENTILE", "SAMPLE", "TOP", "ABS", "ACOS", "ASIN", "ATAN", "ATAN2", 
			"CEIL", "COS", "CUMULATIVE_SUM", "DERIVATIVE", "DIFFERENCE", "ELAPSED", 
			"EXP", "FLOOR", "HISTOGRAM", "LN", "LOG", "LOG2", "LOG10", "MOVING_AVERAGE", 
			"NON_NEGATIVE_DERIVATIVE", "NON_NEGATIVE_DIFFERENCE", "POW", "ROUND", 
			"SIN", "SQRT", "TAN", "HOLT_WINTERS", "CHANDE_MOMENTUM_OSCILLATOR", "EXPONENTIAL_MOVING_AVERAGE", 
			"DOUBLE_EXPONENTIAL_MOVING_AVERAGE", "KAUFMANS_EFFICIENCY_RATIO", "KAUFMANS_ADAPTIVE_MOVING_AVERAGE", 
			"TRIPLE_EXPONENTIAL_MOVING_AVERAGE", "RELATIVE_STRENGTH_INDEX", "USE", 
			"DESCRIBE", "SCHEMAS", "TABLES", "TABLESPACE", "COLUMNS", "FIELDS", "INDEXES", 
			"STATUS", "REPLACE", "MODIFY", "DISTINCTROW", "VALUE", "DUPLICATE", "AFTER", 
			"OJ", "WINDOW", "MOD", "DIV", "XOR", "REGEXP", "RLIKE", "ACCOUNT", "ROLE", 
			"START", "TRANSACTION", "ROW", "ROWS", "WITHOUT", "BINARY", "ESCAPE", 
			"GENERATED", "PARTITION", "SUBPARTITION", "STORAGE", "STORED", "SUPER", 
			"SUBSTR", "TEMPORARY", "THAN", "TRAILING", "UNBOUNDED", "UNLOCK", "UNSIGNED", 
			"UPGRADE", "USAGE", "VALIDATION", "VIRTUAL", "ROLLUP", "SOUNDS", "UNKNOWN", 
			"OFF", "ALWAYS", "CASCADE", "CHECK", "COMMITTED", "LEVEL", "NO", "OPTION", 
			"REFERENCES", "ACTION", "ALGORITHM", "ANALYZE", "AUTOCOMMIT", "MAXVALUE", 
			"BOTH", "BTREE", "CHAIN", "CHANGE", "CHARSET", "CHECKSUM", "CIPHER", 
			"CLIENT", "COALESCE", "COLLATE", "COMMENT", "COMPACT", "COMPRESSED", 
			"COMPRESSION", "CONNECTION", "CONSISTENT", "CONVERT", "COPY", "DATA", 
			"DELAYED", "DIRECTORY", "DISCARD", "DISK", "DYNAMIC", "ENCRYPTION", "ENGINE", 
			"EVENT", "EXCEPT", "EXCHANGE", "EXCLUSIVE", "EXECUTE", "EXTRACT", "FILE", 
			"FIXED", "FOLLOWING", "FORCE", "FULLTEXT", "GLOBAL", "HASH", "IDENTIFIED", 
			"IGNORE", "IMPORT_", "INPLACE", "LEADING", "LESS", "LINEAR", "LOCK", 
			"MATCH", "MEMORY", "NONE", "NOW", "OFFLINE", "ONLINE", "OPTIMIZE", "OVER", 
			"PARSER", "PARTIAL", "PARTITIONING", "PERSIST", "PRECEDING", "PROCESS", 
			"PROXY", "QUICK", "RANGE", "REBUILD", "RECURSIVE", "REDUNDANT", "RELEASE", 
			"RELOAD", "REMOVE", "RENAME", "REORGANIZE", "REPAIR", "REQUIRE", "RESTRICT", 
			"REVERSE", "ROUTINE", "SEPARATOR", "SESSION", "SHARED", "SHUTDOWN", "SIMPLE", 
			"SLAVE", "SPATIAL", "ZEROFILL", "VISIBLE", "INVISIBLE", "INSTANT", "ENFORCED", 
			"AGAINST", "LANGUAGE", "EXTENDED", "EXPANSION", "VARIANCE", "MAX_ROWS", 
			"MIN_ROWS", "HIGH_PRIORITY", "LOW_PRIORITY", "SQL_BIG_RESULT", "SQL_BUFFER_RESULT", 
			"SQL_CACHE", "SQL_CALC_FOUND_ROWS", "SQL_NO_CACHE", "SQL_SMALL_RESULT", 
			"STATS_AUTO_RECALC", "STATS_PERSISTENT", "STATS_SAMPLE_PAGES", "ROLE_ADMIN", 
			"ROW_FORMAT", "SET_USER_ID", "REPLICATION_SLAVE_ADMIN", "GROUP_REPLICATION_ADMIN", 
			"STRAIGHT_JOIN", "WEIGHT_STRING", "COLUMN_FORMAT", "CONNECTION_ADMIN", 
			"FIREWALL_ADMIN", "FIREWALL_USER", "INSERT_METHOD", "KEY_BLOCK_SIZE", 
			"PACK_KEYS", "PERSIST_ONLY", "BIT_AND", "BIT_OR", "BIT_XOR", "GROUP_CONCAT", 
			"JSON_ARRAYAGG", "JSON_OBJECTAGG", "STD", "STDDEV_POP", "STDDEV_SAMP", 
			"VAR_POP", "VAR_SAMP", "AUDIT_ADMIN", "AUTO_INCREMENT", "AVG_ROW_LENGTH", 
			"BINLOG_ADMIN", "DELAY_KEY_WRITE", "ENCRYPTION_KEY_ADMIN", "SYSTEM_VARIABLES_ADMIN", 
			"VERSION_TOKEN_ADMIN", "CURRENT_TIMESTAMP", "YEAR_MONTH", "DAY_HOUR", 
			"DAY_MINUTE", "DAY_SECOND", "DAY_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", 
			"HOUR_MICROSECOND", "MINUTE_SECOND", "MINUTE_MICROSECOND", "SECOND_MICROSECOND", 
			"UL_BINARY", "ROTATE", "MASTER", "BINLOG", "ERROR", "SCHEDULE", "COMPLETION", 
			"STARTS", "ENDS", "HOST", "SOCKET", "PORT", "SERVER", "WRAPPER", "OPTIONS", 
			"OWNER", "DETERMINISTIC", "RETURNS", "CONTAINS", "READS", "MODIFIES", 
			"SECURITY", "INVOKER", "OUT", "TEMPTABLE", "MERGE", "UNDEFINED", "DATAFILE", 
			"FILE_BLOCK_SIZE", "EXTENT_SIZE", "INITIAL_SIZE", "AUTOEXTEND_SIZE", 
			"MAX_SIZE", "NODEGROUP", "WAIT", "LOGFILE", "UNDOFILE", "UNDO_BUFFER_SIZE", 
			"REDO_BUFFER_SIZE", "HANDLER", "PREV", "ORGANIZATION", "DEFINITION", 
			"DESCRIPTION", "REFERENCE", "FOLLOWS", "PRECEDES", "IMPORT", "LOAD", 
			"CONCURRENT", "INFILE", "LINES", "STARTING", "TERMINATED", "OPTIONALLY", 
			"ENCLOSED", "ESCAPED", "XML", "UNDO", "DUMPFILE", "OUTFILE", "SHARE", 
			"IDENTIFIER_", "STRING_", "NUMBER_", "HEX_DIGIT_", "BIT_NUM_"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "LinglongSQLStatement.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public LinglongSQLStatementParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class ExecuteContext extends ParserRuleContext {
		public SelectClauseContext selectClause() {
			return getRuleContext(SelectClauseContext.class,0);
		}
		public ExecuteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_execute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterExecute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitExecute(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitExecute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExecuteContext execute() throws RecognitionException {
		ExecuteContext _localctx = new ExecuteContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_execute);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(182);
			selectClause();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectClauseContext extends ParserRuleContext {
		public TerminalNode SELECT() { return getToken(LinglongSQLStatementParser.SELECT, 0); }
		public SelectItemsContext selectItems() {
			return getRuleContext(SelectItemsContext.class,0);
		}
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public GroupByClauseContext groupByClause() {
			return getRuleContext(GroupByClauseContext.class,0);
		}
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public LimitClauseContext limitClause() {
			return getRuleContext(LimitClauseContext.class,0);
		}
		public SelectClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterSelectClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitSelectClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitSelectClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectClauseContext selectClause() throws RecognitionException {
		SelectClauseContext _localctx = new SelectClauseContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_selectClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(184);
			match(SELECT);
			setState(185);
			selectItems();
			setState(186);
			fromClause();
			setState(188);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(187);
				whereClause();
				}
			}

			setState(191);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==GROUP) {
				{
				setState(190);
				groupByClause();
				}
			}

			setState(194);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(193);
				orderByClause();
				}
			}

			setState(197);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LIMIT) {
				{
				setState(196);
				limitClause();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectItemsContext extends ParserRuleContext {
		public UnqualifiedShorthandContext unqualifiedShorthand() {
			return getRuleContext(UnqualifiedShorthandContext.class,0);
		}
		public List<SelectItemContext> selectItem() {
			return getRuleContexts(SelectItemContext.class);
		}
		public SelectItemContext selectItem(int i) {
			return getRuleContext(SelectItemContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public SelectItemsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectItems; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterSelectItems(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitSelectItems(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitSelectItems(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectItemsContext selectItems() throws RecognitionException {
		SelectItemsContext _localctx = new SelectItemsContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_selectItems);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(201);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ASTERISK_:
				{
				setState(199);
				unqualifiedShorthand();
				}
				break;
			case NOT_:
			case TILDE_:
			case PLUS_:
			case MINUS_:
			case DOT_:
			case LP_:
			case LBE_:
			case QUESTION_:
			case AT_:
			case TRUNCATE:
			case POSITION:
			case VIEW:
			case DISTINCT:
			case CASE:
			case SUBSTRING:
			case IF:
			case NOT:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case ANY:
			case OFFSET:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case INTERVAL:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case YEAR:
			case QUARTER:
			case MONTH:
			case WEEK:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case INSTANCE:
			case DO:
			case DEFINER:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case OPEN:
			case NEXT:
			case NAME:
			case END:
			case EVERY:
			case PASSWORD:
			case PRIVILEGES:
			case QUERY:
			case USER:
			case INTEGRAL:
			case MEAN:
			case MEDIAN:
			case MODE:
			case SPREAD:
			case STDDEV:
			case BOTTOM:
			case FIRST:
			case LAST:
			case PERCENTILE:
			case SAMPLE:
			case TOP:
			case ABS:
			case ACOS:
			case ASIN:
			case ATAN:
			case ATAN2:
			case CEIL:
			case COS:
			case CUMULATIVE_SUM:
			case DERIVATIVE:
			case DIFFERENCE:
			case EXP:
			case FLOOR:
			case HISTOGRAM:
			case LN:
			case LOG:
			case LOG2:
			case LOG10:
			case MOVING_AVERAGE:
			case NON_NEGATIVE_DERIVATIVE:
			case NON_NEGATIVE_DIFFERENCE:
			case POW:
			case ROUND:
			case SIN:
			case SQRT:
			case TAN:
			case TABLES:
			case TABLESPACE:
			case COLUMNS:
			case FIELDS:
			case INDEXES:
			case STATUS:
			case REPLACE:
			case MODIFY:
			case VALUE:
			case DUPLICATE:
			case AFTER:
			case OJ:
			case ACCOUNT:
			case ROLE:
			case START:
			case TRANSACTION:
			case ROW:
			case WITHOUT:
			case BINARY:
			case ESCAPE:
			case SUBPARTITION:
			case STORAGE:
			case SUPER:
			case TEMPORARY:
			case THAN:
			case UNBOUNDED:
			case UPGRADE:
			case VALIDATION:
			case ROLLUP:
			case SOUNDS:
			case UNKNOWN:
			case OFF:
			case ALWAYS:
			case COMMITTED:
			case LEVEL:
			case NO:
			case ACTION:
			case ALGORITHM:
			case AUTOCOMMIT:
			case BTREE:
			case CHAIN:
			case CHARSET:
			case CHECKSUM:
			case CIPHER:
			case CLIENT:
			case COALESCE:
			case COMMENT:
			case COMPACT:
			case COMPRESSED:
			case COMPRESSION:
			case CONNECTION:
			case CONSISTENT:
			case DATA:
			case DISCARD:
			case DISK:
			case ENCRYPTION:
			case ENGINE:
			case EVENT:
			case EXCHANGE:
			case EXECUTE:
			case FILE:
			case FIXED:
			case FOLLOWING:
			case GLOBAL:
			case HASH:
			case IMPORT_:
			case LESS:
			case MATCH:
			case MEMORY:
			case NONE:
			case NOW:
			case PARSER:
			case PARTIAL:
			case PARTITIONING:
			case PERSIST:
			case PRECEDING:
			case PROCESS:
			case PROXY:
			case QUICK:
			case REBUILD:
			case REDUNDANT:
			case RELOAD:
			case REMOVE:
			case REORGANIZE:
			case REPAIR:
			case REVERSE:
			case SESSION:
			case SHUTDOWN:
			case SIMPLE:
			case SLAVE:
			case VISIBLE:
			case INVISIBLE:
			case ENFORCED:
			case AGAINST:
			case LANGUAGE:
			case EXTENDED:
			case EXPANSION:
			case VARIANCE:
			case MAX_ROWS:
			case MIN_ROWS:
			case SQL_BIG_RESULT:
			case SQL_BUFFER_RESULT:
			case SQL_CACHE:
			case SQL_NO_CACHE:
			case STATS_AUTO_RECALC:
			case STATS_PERSISTENT:
			case STATS_SAMPLE_PAGES:
			case ROW_FORMAT:
			case WEIGHT_STRING:
			case COLUMN_FORMAT:
			case INSERT_METHOD:
			case KEY_BLOCK_SIZE:
			case PACK_KEYS:
			case PERSIST_ONLY:
			case BIT_AND:
			case BIT_OR:
			case BIT_XOR:
			case GROUP_CONCAT:
			case JSON_ARRAYAGG:
			case JSON_OBJECTAGG:
			case STD:
			case STDDEV_POP:
			case STDDEV_SAMP:
			case VAR_POP:
			case VAR_SAMP:
			case AUTO_INCREMENT:
			case AVG_ROW_LENGTH:
			case DELAY_KEY_WRITE:
			case CURRENT_TIMESTAMP:
			case ROTATE:
			case MASTER:
			case BINLOG:
			case ERROR:
			case SCHEDULE:
			case COMPLETION:
			case HOST:
			case SOCKET:
			case PORT:
			case SERVER:
			case WRAPPER:
			case OPTIONS:
			case OWNER:
			case RETURNS:
			case CONTAINS:
			case SECURITY:
			case INVOKER:
			case TEMPTABLE:
			case MERGE:
			case UNDEFINED:
			case DATAFILE:
			case FILE_BLOCK_SIZE:
			case EXTENT_SIZE:
			case INITIAL_SIZE:
			case AUTOEXTEND_SIZE:
			case MAX_SIZE:
			case NODEGROUP:
			case WAIT:
			case LOGFILE:
			case UNDOFILE:
			case UNDO_BUFFER_SIZE:
			case REDO_BUFFER_SIZE:
			case HANDLER:
			case PREV:
			case ORGANIZATION:
			case DEFINITION:
			case DESCRIPTION:
			case REFERENCE:
			case FOLLOWS:
			case PRECEDES:
			case IMPORT:
			case CONCURRENT:
			case XML:
			case DUMPFILE:
			case SHARE:
			case IDENTIFIER_:
			case STRING_:
			case NUMBER_:
			case HEX_DIGIT_:
			case BIT_NUM_:
				{
				setState(200);
				selectItem();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(207);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(203);
				match(COMMA_);
				setState(204);
				selectItem();
				}
				}
				setState(209);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectItemContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public TerminalNode AS() { return getToken(LinglongSQLStatementParser.AS, 0); }
		public QualifiedShorthandContext qualifiedShorthand() {
			return getRuleContext(QualifiedShorthandContext.class,0);
		}
		public SelectItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterSelectItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitSelectItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitSelectItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectItemContext selectItem() throws RecognitionException {
		SelectItemContext _localctx = new SelectItemContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_selectItem);
		int _la;
		try {
			setState(221);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(212);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
				case 1:
					{
					setState(210);
					columnName();
					}
					break;
				case 2:
					{
					setState(211);
					expr(0);
					}
					break;
				}
				setState(218);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TRUNCATE || _la==POSITION || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & ((1L << (VIEW - 68)) | (1L << (AS - 68)) | (1L << (ANY - 68)) | (1L << (OFFSET - 68)) | (1L << (BEGIN - 68)) | (1L << (COMMIT - 68)) | (1L << (ROLLBACK - 68)) | (1L << (SAVEPOINT - 68)) | (1L << (BOOLEAN - 68)) | (1L << (DATE - 68)) | (1L << (TIME - 68)) | (1L << (TIMESTAMP - 68)))) != 0) || ((((_la - 133)) & ~0x3f) == 0 && ((1L << (_la - 133)) & ((1L << (YEAR - 133)) | (1L << (QUARTER - 133)) | (1L << (MONTH - 133)) | (1L << (WEEK - 133)) | (1L << (DAY - 133)) | (1L << (HOUR - 133)) | (1L << (MINUTE - 133)) | (1L << (SECOND - 133)) | (1L << (MICROSECOND - 133)) | (1L << (MAX - 133)) | (1L << (MIN - 133)) | (1L << (SUM - 133)) | (1L << (COUNT - 133)) | (1L << (AVG - 133)) | (1L << (CURRENT - 133)) | (1L << (ENABLE - 133)) | (1L << (DISABLE - 133)) | (1L << (INSTANCE - 133)) | (1L << (DO - 133)) | (1L << (DEFINER - 133)) | (1L << (CASCADED - 133)) | (1L << (LOCAL - 133)) | (1L << (CLOSE - 133)) | (1L << (OPEN - 133)) | (1L << (NEXT - 133)) | (1L << (NAME - 133)) | (1L << (END - 133)) | (1L << (EVERY - 133)) | (1L << (PASSWORD - 133)) | (1L << (PRIVILEGES - 133)) | (1L << (QUERY - 133)))) != 0) || ((((_la - 200)) & ~0x3f) == 0 && ((1L << (_la - 200)) & ((1L << (USER - 200)) | (1L << (MODE - 200)) | (1L << (STDDEV - 200)) | (1L << (BOTTOM - 200)) | (1L << (FIRST - 200)) | (1L << (LAST - 200)) | (1L << (TABLES - 200)) | (1L << (TABLESPACE - 200)) | (1L << (COLUMNS - 200)) | (1L << (FIELDS - 200)) | (1L << (INDEXES - 200)) | (1L << (STATUS - 200)) | (1L << (MODIFY - 200)) | (1L << (VALUE - 200)) | (1L << (DUPLICATE - 200)) | (1L << (AFTER - 200)))) != 0) || ((((_la - 264)) & ~0x3f) == 0 && ((1L << (_la - 264)) & ((1L << (OJ - 264)) | (1L << (ACCOUNT - 264)) | (1L << (ROLE - 264)) | (1L << (START - 264)) | (1L << (TRANSACTION - 264)) | (1L << (WITHOUT - 264)) | (1L << (ESCAPE - 264)) | (1L << (SUBPARTITION - 264)) | (1L << (STORAGE - 264)) | (1L << (SUPER - 264)) | (1L << (TEMPORARY - 264)) | (1L << (THAN - 264)) | (1L << (UNBOUNDED - 264)) | (1L << (UPGRADE - 264)) | (1L << (VALIDATION - 264)) | (1L << (ROLLUP - 264)) | (1L << (SOUNDS - 264)) | (1L << (UNKNOWN - 264)) | (1L << (OFF - 264)) | (1L << (ALWAYS - 264)) | (1L << (COMMITTED - 264)) | (1L << (LEVEL - 264)) | (1L << (NO - 264)) | (1L << (ACTION - 264)) | (1L << (ALGORITHM - 264)) | (1L << (AUTOCOMMIT - 264)) | (1L << (BTREE - 264)) | (1L << (CHAIN - 264)) | (1L << (CHARSET - 264)) | (1L << (CHECKSUM - 264)) | (1L << (CIPHER - 264)) | (1L << (CLIENT - 264)) | (1L << (COALESCE - 264)) | (1L << (COMMENT - 264)) | (1L << (COMPACT - 264)) | (1L << (COMPRESSED - 264)) | (1L << (COMPRESSION - 264)))) != 0) || ((((_la - 328)) & ~0x3f) == 0 && ((1L << (_la - 328)) & ((1L << (CONNECTION - 328)) | (1L << (CONSISTENT - 328)) | (1L << (DATA - 328)) | (1L << (DISCARD - 328)) | (1L << (DISK - 328)) | (1L << (ENCRYPTION - 328)) | (1L << (ENGINE - 328)) | (1L << (EVENT - 328)) | (1L << (EXCHANGE - 328)) | (1L << (EXECUTE - 328)) | (1L << (FILE - 328)) | (1L << (FIXED - 328)) | (1L << (FOLLOWING - 328)) | (1L << (GLOBAL - 328)) | (1L << (HASH - 328)) | (1L << (IMPORT_ - 328)) | (1L << (LESS - 328)) | (1L << (MEMORY - 328)) | (1L << (NONE - 328)) | (1L << (PARSER - 328)) | (1L << (PARTIAL - 328)) | (1L << (PARTITIONING - 328)) | (1L << (PERSIST - 328)) | (1L << (PRECEDING - 328)) | (1L << (PROCESS - 328)) | (1L << (PROXY - 328)) | (1L << (QUICK - 328)) | (1L << (REBUILD - 328)) | (1L << (REDUNDANT - 328)) | (1L << (RELOAD - 328)) | (1L << (REMOVE - 328)) | (1L << (REORGANIZE - 328)) | (1L << (REPAIR - 328)) | (1L << (REVERSE - 328)))) != 0) || ((((_la - 392)) & ~0x3f) == 0 && ((1L << (_la - 392)) & ((1L << (SESSION - 392)) | (1L << (SHUTDOWN - 392)) | (1L << (SIMPLE - 392)) | (1L << (SLAVE - 392)) | (1L << (VISIBLE - 392)) | (1L << (INVISIBLE - 392)) | (1L << (ENFORCED - 392)) | (1L << (AGAINST - 392)) | (1L << (LANGUAGE - 392)) | (1L << (EXTENDED - 392)) | (1L << (EXPANSION - 392)) | (1L << (VARIANCE - 392)) | (1L << (MAX_ROWS - 392)) | (1L << (MIN_ROWS - 392)) | (1L << (SQL_BIG_RESULT - 392)) | (1L << (SQL_BUFFER_RESULT - 392)) | (1L << (SQL_CACHE - 392)) | (1L << (SQL_NO_CACHE - 392)) | (1L << (STATS_AUTO_RECALC - 392)) | (1L << (STATS_PERSISTENT - 392)) | (1L << (STATS_SAMPLE_PAGES - 392)) | (1L << (ROW_FORMAT - 392)) | (1L << (WEIGHT_STRING - 392)) | (1L << (COLUMN_FORMAT - 392)) | (1L << (INSERT_METHOD - 392)) | (1L << (KEY_BLOCK_SIZE - 392)) | (1L << (PACK_KEYS - 392)) | (1L << (PERSIST_ONLY - 392)) | (1L << (BIT_AND - 392)) | (1L << (BIT_OR - 392)) | (1L << (BIT_XOR - 392)) | (1L << (GROUP_CONCAT - 392)) | (1L << (JSON_ARRAYAGG - 392)) | (1L << (JSON_OBJECTAGG - 392)) | (1L << (STD - 392)) | (1L << (STDDEV_POP - 392)) | (1L << (STDDEV_SAMP - 392)) | (1L << (VAR_POP - 392)) | (1L << (VAR_SAMP - 392)) | (1L << (AUTO_INCREMENT - 392)) | (1L << (AVG_ROW_LENGTH - 392)) | (1L << (DELAY_KEY_WRITE - 392)))) != 0) || ((((_la - 468)) & ~0x3f) == 0 && ((1L << (_la - 468)) & ((1L << (ROTATE - 468)) | (1L << (MASTER - 468)) | (1L << (BINLOG - 468)) | (1L << (ERROR - 468)) | (1L << (SCHEDULE - 468)) | (1L << (COMPLETION - 468)) | (1L << (HOST - 468)) | (1L << (SOCKET - 468)) | (1L << (PORT - 468)) | (1L << (SERVER - 468)) | (1L << (WRAPPER - 468)) | (1L << (OPTIONS - 468)) | (1L << (OWNER - 468)) | (1L << (RETURNS - 468)) | (1L << (CONTAINS - 468)) | (1L << (SECURITY - 468)) | (1L << (INVOKER - 468)) | (1L << (TEMPTABLE - 468)) | (1L << (MERGE - 468)) | (1L << (UNDEFINED - 468)) | (1L << (DATAFILE - 468)) | (1L << (FILE_BLOCK_SIZE - 468)) | (1L << (EXTENT_SIZE - 468)) | (1L << (INITIAL_SIZE - 468)) | (1L << (AUTOEXTEND_SIZE - 468)) | (1L << (MAX_SIZE - 468)) | (1L << (NODEGROUP - 468)) | (1L << (WAIT - 468)) | (1L << (LOGFILE - 468)) | (1L << (UNDOFILE - 468)) | (1L << (UNDO_BUFFER_SIZE - 468)) | (1L << (REDO_BUFFER_SIZE - 468)) | (1L << (HANDLER - 468)) | (1L << (PREV - 468)) | (1L << (ORGANIZATION - 468)) | (1L << (DEFINITION - 468)) | (1L << (DESCRIPTION - 468)) | (1L << (REFERENCE - 468)) | (1L << (FOLLOWS - 468)) | (1L << (PRECEDES - 468)) | (1L << (IMPORT - 468)) | (1L << (CONCURRENT - 468)) | (1L << (XML - 468)) | (1L << (DUMPFILE - 468)) | (1L << (SHARE - 468)) | (1L << (IDENTIFIER_ - 468)) | (1L << (STRING_ - 468)))) != 0)) {
					{
					setState(215);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(214);
						match(AS);
						}
					}

					setState(217);
					alias();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(220);
				qualifiedShorthand();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AliasContext extends ParserRuleContext {
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public TerminalNode STRING_() { return getToken(LinglongSQLStatementParser.STRING_, 0); }
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitAlias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitAlias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_alias);
		try {
			setState(225);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TRUNCATE:
			case POSITION:
			case VIEW:
			case ANY:
			case OFFSET:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case YEAR:
			case QUARTER:
			case MONTH:
			case WEEK:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case INSTANCE:
			case DO:
			case DEFINER:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case OPEN:
			case NEXT:
			case NAME:
			case END:
			case EVERY:
			case PASSWORD:
			case PRIVILEGES:
			case QUERY:
			case USER:
			case MODE:
			case STDDEV:
			case BOTTOM:
			case FIRST:
			case LAST:
			case TABLES:
			case TABLESPACE:
			case COLUMNS:
			case FIELDS:
			case INDEXES:
			case STATUS:
			case MODIFY:
			case VALUE:
			case DUPLICATE:
			case AFTER:
			case OJ:
			case ACCOUNT:
			case ROLE:
			case START:
			case TRANSACTION:
			case WITHOUT:
			case ESCAPE:
			case SUBPARTITION:
			case STORAGE:
			case SUPER:
			case TEMPORARY:
			case THAN:
			case UNBOUNDED:
			case UPGRADE:
			case VALIDATION:
			case ROLLUP:
			case SOUNDS:
			case UNKNOWN:
			case OFF:
			case ALWAYS:
			case COMMITTED:
			case LEVEL:
			case NO:
			case ACTION:
			case ALGORITHM:
			case AUTOCOMMIT:
			case BTREE:
			case CHAIN:
			case CHARSET:
			case CHECKSUM:
			case CIPHER:
			case CLIENT:
			case COALESCE:
			case COMMENT:
			case COMPACT:
			case COMPRESSED:
			case COMPRESSION:
			case CONNECTION:
			case CONSISTENT:
			case DATA:
			case DISCARD:
			case DISK:
			case ENCRYPTION:
			case ENGINE:
			case EVENT:
			case EXCHANGE:
			case EXECUTE:
			case FILE:
			case FIXED:
			case FOLLOWING:
			case GLOBAL:
			case HASH:
			case IMPORT_:
			case LESS:
			case MEMORY:
			case NONE:
			case PARSER:
			case PARTIAL:
			case PARTITIONING:
			case PERSIST:
			case PRECEDING:
			case PROCESS:
			case PROXY:
			case QUICK:
			case REBUILD:
			case REDUNDANT:
			case RELOAD:
			case REMOVE:
			case REORGANIZE:
			case REPAIR:
			case REVERSE:
			case SESSION:
			case SHUTDOWN:
			case SIMPLE:
			case SLAVE:
			case VISIBLE:
			case INVISIBLE:
			case ENFORCED:
			case AGAINST:
			case LANGUAGE:
			case EXTENDED:
			case EXPANSION:
			case VARIANCE:
			case MAX_ROWS:
			case MIN_ROWS:
			case SQL_BIG_RESULT:
			case SQL_BUFFER_RESULT:
			case SQL_CACHE:
			case SQL_NO_CACHE:
			case STATS_AUTO_RECALC:
			case STATS_PERSISTENT:
			case STATS_SAMPLE_PAGES:
			case ROW_FORMAT:
			case WEIGHT_STRING:
			case COLUMN_FORMAT:
			case INSERT_METHOD:
			case KEY_BLOCK_SIZE:
			case PACK_KEYS:
			case PERSIST_ONLY:
			case BIT_AND:
			case BIT_OR:
			case BIT_XOR:
			case GROUP_CONCAT:
			case JSON_ARRAYAGG:
			case JSON_OBJECTAGG:
			case STD:
			case STDDEV_POP:
			case STDDEV_SAMP:
			case VAR_POP:
			case VAR_SAMP:
			case AUTO_INCREMENT:
			case AVG_ROW_LENGTH:
			case DELAY_KEY_WRITE:
			case ROTATE:
			case MASTER:
			case BINLOG:
			case ERROR:
			case SCHEDULE:
			case COMPLETION:
			case HOST:
			case SOCKET:
			case PORT:
			case SERVER:
			case WRAPPER:
			case OPTIONS:
			case OWNER:
			case RETURNS:
			case CONTAINS:
			case SECURITY:
			case INVOKER:
			case TEMPTABLE:
			case MERGE:
			case UNDEFINED:
			case DATAFILE:
			case FILE_BLOCK_SIZE:
			case EXTENT_SIZE:
			case INITIAL_SIZE:
			case AUTOEXTEND_SIZE:
			case MAX_SIZE:
			case NODEGROUP:
			case WAIT:
			case LOGFILE:
			case UNDOFILE:
			case UNDO_BUFFER_SIZE:
			case REDO_BUFFER_SIZE:
			case HANDLER:
			case PREV:
			case ORGANIZATION:
			case DEFINITION:
			case DESCRIPTION:
			case REFERENCE:
			case FOLLOWS:
			case PRECEDES:
			case IMPORT:
			case CONCURRENT:
			case XML:
			case DUMPFILE:
			case SHARE:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(223);
				identifier_();
				}
				break;
			case STRING_:
				enterOuterAlt(_localctx, 2);
				{
				setState(224);
				match(STRING_);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnqualifiedShorthandContext extends ParserRuleContext {
		public TerminalNode ASTERISK_() { return getToken(LinglongSQLStatementParser.ASTERISK_, 0); }
		public UnqualifiedShorthandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unqualifiedShorthand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterUnqualifiedShorthand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitUnqualifiedShorthand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitUnqualifiedShorthand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnqualifiedShorthandContext unqualifiedShorthand() throws RecognitionException {
		UnqualifiedShorthandContext _localctx = new UnqualifiedShorthandContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_unqualifiedShorthand);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(227);
			match(ASTERISK_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QualifiedShorthandContext extends ParserRuleContext {
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public TerminalNode DOT_ASTERISK_() { return getToken(LinglongSQLStatementParser.DOT_ASTERISK_, 0); }
		public QualifiedShorthandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedShorthand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterQualifiedShorthand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitQualifiedShorthand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitQualifiedShorthand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedShorthandContext qualifiedShorthand() throws RecognitionException {
		QualifiedShorthandContext _localctx = new QualifiedShorthandContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_qualifiedShorthand);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
			identifier_();
			setState(230);
			match(DOT_ASTERISK_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FromClauseContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(LinglongSQLStatementParser.FROM, 0); }
		public TableReferencesContext tableReferences() {
			return getRuleContext(TableReferencesContext.class,0);
		}
		public FromClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterFromClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitFromClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitFromClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromClauseContext fromClause() throws RecognitionException {
		FromClauseContext _localctx = new FromClauseContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_fromClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(232);
			match(FROM);
			setState(233);
			tableReferences();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TableReferencesContext extends ParserRuleContext {
		public List<EscapedTableReference_Context> escapedTableReference_() {
			return getRuleContexts(EscapedTableReference_Context.class);
		}
		public EscapedTableReference_Context escapedTableReference_(int i) {
			return getRuleContext(EscapedTableReference_Context.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public TableReferencesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableReferences; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterTableReferences(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitTableReferences(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitTableReferences(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableReferencesContext tableReferences() throws RecognitionException {
		TableReferencesContext _localctx = new TableReferencesContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_tableReferences);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(235);
			escapedTableReference_();
			setState(240);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(236);
				match(COMMA_);
				setState(237);
				escapedTableReference_();
				}
				}
				setState(242);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EscapedTableReference_Context extends ParserRuleContext {
		public TableReferenceContext tableReference() {
			return getRuleContext(TableReferenceContext.class,0);
		}
		public EscapedTableReference_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_escapedTableReference_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterEscapedTableReference_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitEscapedTableReference_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitEscapedTableReference_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EscapedTableReference_Context escapedTableReference_() throws RecognitionException {
		EscapedTableReference_Context _localctx = new EscapedTableReference_Context(_ctx, getState());
		enterRule(_localctx, 18, RULE_escapedTableReference_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(243);
			tableReference();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TableReferenceContext extends ParserRuleContext {
		public TableFactorContext tableFactor() {
			return getRuleContext(TableFactorContext.class,0);
		}
		public TableReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableReference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterTableReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitTableReference(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitTableReference(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableReferenceContext tableReference() throws RecognitionException {
		TableReferenceContext _localctx = new TableReferenceContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_tableReference);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(245);
			tableFactor();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TableFactorContext extends ParserRuleContext {
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public TerminalNode AS() { return getToken(LinglongSQLStatementParser.AS, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public ColumnNamesContext columnNames() {
			return getRuleContext(ColumnNamesContext.class,0);
		}
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public TableReferencesContext tableReferences() {
			return getRuleContext(TableReferencesContext.class,0);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public TableFactorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableFactor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterTableFactor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitTableFactor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitTableFactor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableFactorContext tableFactor() throws RecognitionException {
		TableFactorContext _localctx = new TableFactorContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_tableFactor);
		int _la;
		try {
			setState(262);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(247);
				tableName();
				setState(252);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TRUNCATE || _la==POSITION || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & ((1L << (VIEW - 68)) | (1L << (AS - 68)) | (1L << (ANY - 68)) | (1L << (OFFSET - 68)) | (1L << (BEGIN - 68)) | (1L << (COMMIT - 68)) | (1L << (ROLLBACK - 68)) | (1L << (SAVEPOINT - 68)) | (1L << (BOOLEAN - 68)) | (1L << (DATE - 68)) | (1L << (TIME - 68)) | (1L << (TIMESTAMP - 68)))) != 0) || ((((_la - 133)) & ~0x3f) == 0 && ((1L << (_la - 133)) & ((1L << (YEAR - 133)) | (1L << (QUARTER - 133)) | (1L << (MONTH - 133)) | (1L << (WEEK - 133)) | (1L << (DAY - 133)) | (1L << (HOUR - 133)) | (1L << (MINUTE - 133)) | (1L << (SECOND - 133)) | (1L << (MICROSECOND - 133)) | (1L << (MAX - 133)) | (1L << (MIN - 133)) | (1L << (SUM - 133)) | (1L << (COUNT - 133)) | (1L << (AVG - 133)) | (1L << (CURRENT - 133)) | (1L << (ENABLE - 133)) | (1L << (DISABLE - 133)) | (1L << (INSTANCE - 133)) | (1L << (DO - 133)) | (1L << (DEFINER - 133)) | (1L << (CASCADED - 133)) | (1L << (LOCAL - 133)) | (1L << (CLOSE - 133)) | (1L << (OPEN - 133)) | (1L << (NEXT - 133)) | (1L << (NAME - 133)) | (1L << (END - 133)) | (1L << (EVERY - 133)) | (1L << (PASSWORD - 133)) | (1L << (PRIVILEGES - 133)) | (1L << (QUERY - 133)))) != 0) || ((((_la - 200)) & ~0x3f) == 0 && ((1L << (_la - 200)) & ((1L << (USER - 200)) | (1L << (MODE - 200)) | (1L << (STDDEV - 200)) | (1L << (BOTTOM - 200)) | (1L << (FIRST - 200)) | (1L << (LAST - 200)) | (1L << (TABLES - 200)) | (1L << (TABLESPACE - 200)) | (1L << (COLUMNS - 200)) | (1L << (FIELDS - 200)) | (1L << (INDEXES - 200)) | (1L << (STATUS - 200)) | (1L << (MODIFY - 200)) | (1L << (VALUE - 200)) | (1L << (DUPLICATE - 200)) | (1L << (AFTER - 200)))) != 0) || ((((_la - 264)) & ~0x3f) == 0 && ((1L << (_la - 264)) & ((1L << (OJ - 264)) | (1L << (ACCOUNT - 264)) | (1L << (ROLE - 264)) | (1L << (START - 264)) | (1L << (TRANSACTION - 264)) | (1L << (WITHOUT - 264)) | (1L << (ESCAPE - 264)) | (1L << (SUBPARTITION - 264)) | (1L << (STORAGE - 264)) | (1L << (SUPER - 264)) | (1L << (TEMPORARY - 264)) | (1L << (THAN - 264)) | (1L << (UNBOUNDED - 264)) | (1L << (UPGRADE - 264)) | (1L << (VALIDATION - 264)) | (1L << (ROLLUP - 264)) | (1L << (SOUNDS - 264)) | (1L << (UNKNOWN - 264)) | (1L << (OFF - 264)) | (1L << (ALWAYS - 264)) | (1L << (COMMITTED - 264)) | (1L << (LEVEL - 264)) | (1L << (NO - 264)) | (1L << (ACTION - 264)) | (1L << (ALGORITHM - 264)) | (1L << (AUTOCOMMIT - 264)) | (1L << (BTREE - 264)) | (1L << (CHAIN - 264)) | (1L << (CHARSET - 264)) | (1L << (CHECKSUM - 264)) | (1L << (CIPHER - 264)) | (1L << (CLIENT - 264)) | (1L << (COALESCE - 264)) | (1L << (COMMENT - 264)) | (1L << (COMPACT - 264)) | (1L << (COMPRESSED - 264)) | (1L << (COMPRESSION - 264)))) != 0) || ((((_la - 328)) & ~0x3f) == 0 && ((1L << (_la - 328)) & ((1L << (CONNECTION - 328)) | (1L << (CONSISTENT - 328)) | (1L << (DATA - 328)) | (1L << (DISCARD - 328)) | (1L << (DISK - 328)) | (1L << (ENCRYPTION - 328)) | (1L << (ENGINE - 328)) | (1L << (EVENT - 328)) | (1L << (EXCHANGE - 328)) | (1L << (EXECUTE - 328)) | (1L << (FILE - 328)) | (1L << (FIXED - 328)) | (1L << (FOLLOWING - 328)) | (1L << (GLOBAL - 328)) | (1L << (HASH - 328)) | (1L << (IMPORT_ - 328)) | (1L << (LESS - 328)) | (1L << (MEMORY - 328)) | (1L << (NONE - 328)) | (1L << (PARSER - 328)) | (1L << (PARTIAL - 328)) | (1L << (PARTITIONING - 328)) | (1L << (PERSIST - 328)) | (1L << (PRECEDING - 328)) | (1L << (PROCESS - 328)) | (1L << (PROXY - 328)) | (1L << (QUICK - 328)) | (1L << (REBUILD - 328)) | (1L << (REDUNDANT - 328)) | (1L << (RELOAD - 328)) | (1L << (REMOVE - 328)) | (1L << (REORGANIZE - 328)) | (1L << (REPAIR - 328)) | (1L << (REVERSE - 328)))) != 0) || ((((_la - 392)) & ~0x3f) == 0 && ((1L << (_la - 392)) & ((1L << (SESSION - 392)) | (1L << (SHUTDOWN - 392)) | (1L << (SIMPLE - 392)) | (1L << (SLAVE - 392)) | (1L << (VISIBLE - 392)) | (1L << (INVISIBLE - 392)) | (1L << (ENFORCED - 392)) | (1L << (AGAINST - 392)) | (1L << (LANGUAGE - 392)) | (1L << (EXTENDED - 392)) | (1L << (EXPANSION - 392)) | (1L << (VARIANCE - 392)) | (1L << (MAX_ROWS - 392)) | (1L << (MIN_ROWS - 392)) | (1L << (SQL_BIG_RESULT - 392)) | (1L << (SQL_BUFFER_RESULT - 392)) | (1L << (SQL_CACHE - 392)) | (1L << (SQL_NO_CACHE - 392)) | (1L << (STATS_AUTO_RECALC - 392)) | (1L << (STATS_PERSISTENT - 392)) | (1L << (STATS_SAMPLE_PAGES - 392)) | (1L << (ROW_FORMAT - 392)) | (1L << (WEIGHT_STRING - 392)) | (1L << (COLUMN_FORMAT - 392)) | (1L << (INSERT_METHOD - 392)) | (1L << (KEY_BLOCK_SIZE - 392)) | (1L << (PACK_KEYS - 392)) | (1L << (PERSIST_ONLY - 392)) | (1L << (BIT_AND - 392)) | (1L << (BIT_OR - 392)) | (1L << (BIT_XOR - 392)) | (1L << (GROUP_CONCAT - 392)) | (1L << (JSON_ARRAYAGG - 392)) | (1L << (JSON_OBJECTAGG - 392)) | (1L << (STD - 392)) | (1L << (STDDEV_POP - 392)) | (1L << (STDDEV_SAMP - 392)) | (1L << (VAR_POP - 392)) | (1L << (VAR_SAMP - 392)) | (1L << (AUTO_INCREMENT - 392)) | (1L << (AVG_ROW_LENGTH - 392)) | (1L << (DELAY_KEY_WRITE - 392)))) != 0) || ((((_la - 468)) & ~0x3f) == 0 && ((1L << (_la - 468)) & ((1L << (ROTATE - 468)) | (1L << (MASTER - 468)) | (1L << (BINLOG - 468)) | (1L << (ERROR - 468)) | (1L << (SCHEDULE - 468)) | (1L << (COMPLETION - 468)) | (1L << (HOST - 468)) | (1L << (SOCKET - 468)) | (1L << (PORT - 468)) | (1L << (SERVER - 468)) | (1L << (WRAPPER - 468)) | (1L << (OPTIONS - 468)) | (1L << (OWNER - 468)) | (1L << (RETURNS - 468)) | (1L << (CONTAINS - 468)) | (1L << (SECURITY - 468)) | (1L << (INVOKER - 468)) | (1L << (TEMPTABLE - 468)) | (1L << (MERGE - 468)) | (1L << (UNDEFINED - 468)) | (1L << (DATAFILE - 468)) | (1L << (FILE_BLOCK_SIZE - 468)) | (1L << (EXTENT_SIZE - 468)) | (1L << (INITIAL_SIZE - 468)) | (1L << (AUTOEXTEND_SIZE - 468)) | (1L << (MAX_SIZE - 468)) | (1L << (NODEGROUP - 468)) | (1L << (WAIT - 468)) | (1L << (LOGFILE - 468)) | (1L << (UNDOFILE - 468)) | (1L << (UNDO_BUFFER_SIZE - 468)) | (1L << (REDO_BUFFER_SIZE - 468)) | (1L << (HANDLER - 468)) | (1L << (PREV - 468)) | (1L << (ORGANIZATION - 468)) | (1L << (DEFINITION - 468)) | (1L << (DESCRIPTION - 468)) | (1L << (REFERENCE - 468)) | (1L << (FOLLOWS - 468)) | (1L << (PRECEDES - 468)) | (1L << (IMPORT - 468)) | (1L << (CONCURRENT - 468)) | (1L << (XML - 468)) | (1L << (DUMPFILE - 468)) | (1L << (SHARE - 468)) | (1L << (IDENTIFIER_ - 468)) | (1L << (STRING_ - 468)))) != 0)) {
					{
					setState(249);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(248);
						match(AS);
						}
					}

					setState(251);
					alias();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(254);
				subquery();
				setState(256);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LP_) | (1L << TRUNCATE) | (1L << POSITION))) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & ((1L << (VIEW - 68)) | (1L << (ANY - 68)) | (1L << (OFFSET - 68)) | (1L << (BEGIN - 68)) | (1L << (COMMIT - 68)) | (1L << (ROLLBACK - 68)) | (1L << (SAVEPOINT - 68)) | (1L << (BOOLEAN - 68)) | (1L << (DATE - 68)) | (1L << (TIME - 68)) | (1L << (TIMESTAMP - 68)))) != 0) || ((((_la - 133)) & ~0x3f) == 0 && ((1L << (_la - 133)) & ((1L << (YEAR - 133)) | (1L << (QUARTER - 133)) | (1L << (MONTH - 133)) | (1L << (WEEK - 133)) | (1L << (DAY - 133)) | (1L << (HOUR - 133)) | (1L << (MINUTE - 133)) | (1L << (SECOND - 133)) | (1L << (MICROSECOND - 133)) | (1L << (MAX - 133)) | (1L << (MIN - 133)) | (1L << (SUM - 133)) | (1L << (COUNT - 133)) | (1L << (AVG - 133)) | (1L << (CURRENT - 133)) | (1L << (ENABLE - 133)) | (1L << (DISABLE - 133)) | (1L << (INSTANCE - 133)) | (1L << (DO - 133)) | (1L << (DEFINER - 133)) | (1L << (CASCADED - 133)) | (1L << (LOCAL - 133)) | (1L << (CLOSE - 133)) | (1L << (OPEN - 133)) | (1L << (NEXT - 133)) | (1L << (NAME - 133)) | (1L << (END - 133)) | (1L << (EVERY - 133)) | (1L << (PASSWORD - 133)) | (1L << (PRIVILEGES - 133)) | (1L << (QUERY - 133)))) != 0) || ((((_la - 200)) & ~0x3f) == 0 && ((1L << (_la - 200)) & ((1L << (USER - 200)) | (1L << (MODE - 200)) | (1L << (STDDEV - 200)) | (1L << (BOTTOM - 200)) | (1L << (FIRST - 200)) | (1L << (LAST - 200)) | (1L << (TABLES - 200)) | (1L << (TABLESPACE - 200)) | (1L << (COLUMNS - 200)) | (1L << (FIELDS - 200)) | (1L << (INDEXES - 200)) | (1L << (STATUS - 200)) | (1L << (MODIFY - 200)) | (1L << (VALUE - 200)) | (1L << (DUPLICATE - 200)) | (1L << (AFTER - 200)))) != 0) || ((((_la - 264)) & ~0x3f) == 0 && ((1L << (_la - 264)) & ((1L << (OJ - 264)) | (1L << (ACCOUNT - 264)) | (1L << (ROLE - 264)) | (1L << (START - 264)) | (1L << (TRANSACTION - 264)) | (1L << (WITHOUT - 264)) | (1L << (ESCAPE - 264)) | (1L << (SUBPARTITION - 264)) | (1L << (STORAGE - 264)) | (1L << (SUPER - 264)) | (1L << (TEMPORARY - 264)) | (1L << (THAN - 264)) | (1L << (UNBOUNDED - 264)) | (1L << (UPGRADE - 264)) | (1L << (VALIDATION - 264)) | (1L << (ROLLUP - 264)) | (1L << (SOUNDS - 264)) | (1L << (UNKNOWN - 264)) | (1L << (OFF - 264)) | (1L << (ALWAYS - 264)) | (1L << (COMMITTED - 264)) | (1L << (LEVEL - 264)) | (1L << (NO - 264)) | (1L << (ACTION - 264)) | (1L << (ALGORITHM - 264)) | (1L << (AUTOCOMMIT - 264)) | (1L << (BTREE - 264)) | (1L << (CHAIN - 264)) | (1L << (CHARSET - 264)) | (1L << (CHECKSUM - 264)) | (1L << (CIPHER - 264)) | (1L << (CLIENT - 264)) | (1L << (COALESCE - 264)) | (1L << (COMMENT - 264)) | (1L << (COMPACT - 264)) | (1L << (COMPRESSED - 264)) | (1L << (COMPRESSION - 264)))) != 0) || ((((_la - 328)) & ~0x3f) == 0 && ((1L << (_la - 328)) & ((1L << (CONNECTION - 328)) | (1L << (CONSISTENT - 328)) | (1L << (DATA - 328)) | (1L << (DISCARD - 328)) | (1L << (DISK - 328)) | (1L << (ENCRYPTION - 328)) | (1L << (ENGINE - 328)) | (1L << (EVENT - 328)) | (1L << (EXCHANGE - 328)) | (1L << (EXECUTE - 328)) | (1L << (FILE - 328)) | (1L << (FIXED - 328)) | (1L << (FOLLOWING - 328)) | (1L << (GLOBAL - 328)) | (1L << (HASH - 328)) | (1L << (IMPORT_ - 328)) | (1L << (LESS - 328)) | (1L << (MEMORY - 328)) | (1L << (NONE - 328)) | (1L << (PARSER - 328)) | (1L << (PARTIAL - 328)) | (1L << (PARTITIONING - 328)) | (1L << (PERSIST - 328)) | (1L << (PRECEDING - 328)) | (1L << (PROCESS - 328)) | (1L << (PROXY - 328)) | (1L << (QUICK - 328)) | (1L << (REBUILD - 328)) | (1L << (REDUNDANT - 328)) | (1L << (RELOAD - 328)) | (1L << (REMOVE - 328)) | (1L << (REORGANIZE - 328)) | (1L << (REPAIR - 328)) | (1L << (REVERSE - 328)))) != 0) || ((((_la - 392)) & ~0x3f) == 0 && ((1L << (_la - 392)) & ((1L << (SESSION - 392)) | (1L << (SHUTDOWN - 392)) | (1L << (SIMPLE - 392)) | (1L << (SLAVE - 392)) | (1L << (VISIBLE - 392)) | (1L << (INVISIBLE - 392)) | (1L << (ENFORCED - 392)) | (1L << (AGAINST - 392)) | (1L << (LANGUAGE - 392)) | (1L << (EXTENDED - 392)) | (1L << (EXPANSION - 392)) | (1L << (VARIANCE - 392)) | (1L << (MAX_ROWS - 392)) | (1L << (MIN_ROWS - 392)) | (1L << (SQL_BIG_RESULT - 392)) | (1L << (SQL_BUFFER_RESULT - 392)) | (1L << (SQL_CACHE - 392)) | (1L << (SQL_NO_CACHE - 392)) | (1L << (STATS_AUTO_RECALC - 392)) | (1L << (STATS_PERSISTENT - 392)) | (1L << (STATS_SAMPLE_PAGES - 392)) | (1L << (ROW_FORMAT - 392)) | (1L << (WEIGHT_STRING - 392)) | (1L << (COLUMN_FORMAT - 392)) | (1L << (INSERT_METHOD - 392)) | (1L << (KEY_BLOCK_SIZE - 392)) | (1L << (PACK_KEYS - 392)) | (1L << (PERSIST_ONLY - 392)) | (1L << (BIT_AND - 392)) | (1L << (BIT_OR - 392)) | (1L << (BIT_XOR - 392)) | (1L << (GROUP_CONCAT - 392)) | (1L << (JSON_ARRAYAGG - 392)) | (1L << (JSON_OBJECTAGG - 392)) | (1L << (STD - 392)) | (1L << (STDDEV_POP - 392)) | (1L << (STDDEV_SAMP - 392)) | (1L << (VAR_POP - 392)) | (1L << (VAR_SAMP - 392)) | (1L << (AUTO_INCREMENT - 392)) | (1L << (AVG_ROW_LENGTH - 392)) | (1L << (DELAY_KEY_WRITE - 392)))) != 0) || ((((_la - 468)) & ~0x3f) == 0 && ((1L << (_la - 468)) & ((1L << (ROTATE - 468)) | (1L << (MASTER - 468)) | (1L << (BINLOG - 468)) | (1L << (ERROR - 468)) | (1L << (SCHEDULE - 468)) | (1L << (COMPLETION - 468)) | (1L << (HOST - 468)) | (1L << (SOCKET - 468)) | (1L << (PORT - 468)) | (1L << (SERVER - 468)) | (1L << (WRAPPER - 468)) | (1L << (OPTIONS - 468)) | (1L << (OWNER - 468)) | (1L << (RETURNS - 468)) | (1L << (CONTAINS - 468)) | (1L << (SECURITY - 468)) | (1L << (INVOKER - 468)) | (1L << (TEMPTABLE - 468)) | (1L << (MERGE - 468)) | (1L << (UNDEFINED - 468)) | (1L << (DATAFILE - 468)) | (1L << (FILE_BLOCK_SIZE - 468)) | (1L << (EXTENT_SIZE - 468)) | (1L << (INITIAL_SIZE - 468)) | (1L << (AUTOEXTEND_SIZE - 468)) | (1L << (MAX_SIZE - 468)) | (1L << (NODEGROUP - 468)) | (1L << (WAIT - 468)) | (1L << (LOGFILE - 468)) | (1L << (UNDOFILE - 468)) | (1L << (UNDO_BUFFER_SIZE - 468)) | (1L << (REDO_BUFFER_SIZE - 468)) | (1L << (HANDLER - 468)) | (1L << (PREV - 468)) | (1L << (ORGANIZATION - 468)) | (1L << (DEFINITION - 468)) | (1L << (DESCRIPTION - 468)) | (1L << (REFERENCE - 468)) | (1L << (FOLLOWS - 468)) | (1L << (PRECEDES - 468)) | (1L << (IMPORT - 468)) | (1L << (CONCURRENT - 468)) | (1L << (XML - 468)) | (1L << (DUMPFILE - 468)) | (1L << (SHARE - 468)) | (1L << (IDENTIFIER_ - 468)))) != 0)) {
					{
					setState(255);
					columnNames();
					}
				}

				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(258);
				match(LP_);
				setState(259);
				tableReferences();
				setState(260);
				match(RP_);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WhereClauseContext extends ParserRuleContext {
		public TerminalNode WHERE() { return getToken(LinglongSQLStatementParser.WHERE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public WhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterWhereClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitWhereClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitWhereClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhereClauseContext whereClause() throws RecognitionException {
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_whereClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(264);
			match(WHERE);
			setState(265);
			expr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GroupByClauseContext extends ParserRuleContext {
		public TerminalNode GROUP() { return getToken(LinglongSQLStatementParser.GROUP, 0); }
		public TerminalNode BY() { return getToken(LinglongSQLStatementParser.BY, 0); }
		public List<OrderByItemContext> orderByItem() {
			return getRuleContexts(OrderByItemContext.class);
		}
		public OrderByItemContext orderByItem(int i) {
			return getRuleContext(OrderByItemContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public TerminalNode WITH() { return getToken(LinglongSQLStatementParser.WITH, 0); }
		public TerminalNode ROLLUP() { return getToken(LinglongSQLStatementParser.ROLLUP, 0); }
		public GroupByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupByClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterGroupByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitGroupByClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitGroupByClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupByClauseContext groupByClause() throws RecognitionException {
		GroupByClauseContext _localctx = new GroupByClauseContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_groupByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(267);
			match(GROUP);
			setState(268);
			match(BY);
			setState(269);
			orderByItem();
			setState(274);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(270);
				match(COMMA_);
				setState(271);
				orderByItem();
				}
				}
				setState(276);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(279);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(277);
				match(WITH);
				setState(278);
				match(ROLLUP);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LimitClauseContext extends ParserRuleContext {
		public TerminalNode LIMIT() { return getToken(LinglongSQLStatementParser.LIMIT, 0); }
		public LimitRowCountContext limitRowCount() {
			return getRuleContext(LimitRowCountContext.class,0);
		}
		public TerminalNode OFFSET() { return getToken(LinglongSQLStatementParser.OFFSET, 0); }
		public LimitOffsetContext limitOffset() {
			return getRuleContext(LimitOffsetContext.class,0);
		}
		public TerminalNode COMMA_() { return getToken(LinglongSQLStatementParser.COMMA_, 0); }
		public LimitClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limitClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterLimitClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitLimitClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitLimitClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LimitClauseContext limitClause() throws RecognitionException {
		LimitClauseContext _localctx = new LimitClauseContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_limitClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(281);
			match(LIMIT);
			setState(292);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				setState(285);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
				case 1:
					{
					setState(282);
					limitOffset();
					setState(283);
					match(COMMA_);
					}
					break;
				}
				setState(287);
				limitRowCount();
				}
				break;
			case 2:
				{
				setState(288);
				limitRowCount();
				setState(289);
				match(OFFSET);
				setState(290);
				limitOffset();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LimitRowCountContext extends ParserRuleContext {
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public ParameterMarkerContext parameterMarker() {
			return getRuleContext(ParameterMarkerContext.class,0);
		}
		public LimitRowCountContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limitRowCount; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterLimitRowCount(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitLimitRowCount(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitLimitRowCount(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LimitRowCountContext limitRowCount() throws RecognitionException {
		LimitRowCountContext _localctx = new LimitRowCountContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_limitRowCount);
		try {
			setState(296);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MINUS_:
			case NUMBER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(294);
				numberLiterals();
				}
				break;
			case QUESTION_:
				enterOuterAlt(_localctx, 2);
				{
				setState(295);
				parameterMarker();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LimitOffsetContext extends ParserRuleContext {
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public ParameterMarkerContext parameterMarker() {
			return getRuleContext(ParameterMarkerContext.class,0);
		}
		public LimitOffsetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limitOffset; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterLimitOffset(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitLimitOffset(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitLimitOffset(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LimitOffsetContext limitOffset() throws RecognitionException {
		LimitOffsetContext _localctx = new LimitOffsetContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_limitOffset);
		try {
			setState(300);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MINUS_:
			case NUMBER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(298);
				numberLiterals();
				}
				break;
			case QUESTION_:
				enterOuterAlt(_localctx, 2);
				{
				setState(299);
				parameterMarker();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SubqueryContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public SelectClauseContext selectClause() {
			return getRuleContext(SelectClauseContext.class,0);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public TerminalNode AS() { return getToken(LinglongSQLStatementParser.AS, 0); }
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public SubqueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subquery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterSubquery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitSubquery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitSubquery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubqueryContext subquery() throws RecognitionException {
		SubqueryContext _localctx = new SubqueryContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_subquery);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(302);
			match(LP_);
			setState(303);
			selectClause();
			setState(304);
			match(RP_);
			setState(306);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				{
				setState(305);
				match(AS);
				}
				break;
			}
			setState(309);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				{
				setState(308);
				alias();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParameterMarkerContext extends ParserRuleContext {
		public TerminalNode QUESTION_() { return getToken(LinglongSQLStatementParser.QUESTION_, 0); }
		public ParameterMarkerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterMarker; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterParameterMarker(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitParameterMarker(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitParameterMarker(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterMarkerContext parameterMarker() throws RecognitionException {
		ParameterMarkerContext _localctx = new ParameterMarkerContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_parameterMarker);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(311);
			match(QUESTION_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralsContext extends ParserRuleContext {
		public StringLiteralsContext stringLiterals() {
			return getRuleContext(StringLiteralsContext.class,0);
		}
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public DateTimeLiteralsContext dateTimeLiterals() {
			return getRuleContext(DateTimeLiteralsContext.class,0);
		}
		public HexadecimalLiteralsContext hexadecimalLiterals() {
			return getRuleContext(HexadecimalLiteralsContext.class,0);
		}
		public BitValueLiteralsContext bitValueLiterals() {
			return getRuleContext(BitValueLiteralsContext.class,0);
		}
		public BooleanLiteralsContext booleanLiterals() {
			return getRuleContext(BooleanLiteralsContext.class,0);
		}
		public NullValueLiteralsContext nullValueLiterals() {
			return getRuleContext(NullValueLiteralsContext.class,0);
		}
		public LiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralsContext literals() throws RecognitionException {
		LiteralsContext _localctx = new LiteralsContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_literals);
		try {
			setState(320);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(313);
				stringLiterals();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(314);
				numberLiterals();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(315);
				dateTimeLiterals();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(316);
				hexadecimalLiterals();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(317);
				bitValueLiterals();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(318);
				booleanLiterals();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(319);
				nullValueLiterals();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringLiteralsContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(LinglongSQLStatementParser.STRING_, 0); }
		public CharacterSetName_Context characterSetName_() {
			return getRuleContext(CharacterSetName_Context.class,0);
		}
		public CollateClause_Context collateClause_() {
			return getRuleContext(CollateClause_Context.class,0);
		}
		public StringLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterStringLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitStringLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitStringLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringLiteralsContext stringLiterals() throws RecognitionException {
		StringLiteralsContext _localctx = new StringLiteralsContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_stringLiterals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(323);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER_) {
				{
				setState(322);
				characterSetName_();
				}
			}

			setState(325);
			match(STRING_);
			setState(327);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				{
				setState(326);
				collateClause_();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumberLiteralsContext extends ParserRuleContext {
		public TerminalNode NUMBER_() { return getToken(LinglongSQLStatementParser.NUMBER_, 0); }
		public TerminalNode MINUS_() { return getToken(LinglongSQLStatementParser.MINUS_, 0); }
		public NumberLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numberLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterNumberLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitNumberLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitNumberLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberLiteralsContext numberLiterals() throws RecognitionException {
		NumberLiteralsContext _localctx = new NumberLiteralsContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_numberLiterals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(330);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MINUS_) {
				{
				setState(329);
				match(MINUS_);
				}
			}

			setState(332);
			match(NUMBER_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DateTimeLiteralsContext extends ParserRuleContext {
		public TerminalNode STRING_() { return getToken(LinglongSQLStatementParser.STRING_, 0); }
		public TerminalNode DATE() { return getToken(LinglongSQLStatementParser.DATE, 0); }
		public TerminalNode TIME() { return getToken(LinglongSQLStatementParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(LinglongSQLStatementParser.TIMESTAMP, 0); }
		public TerminalNode LBE_() { return getToken(LinglongSQLStatementParser.LBE_, 0); }
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public TerminalNode RBE_() { return getToken(LinglongSQLStatementParser.RBE_, 0); }
		public DateTimeLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateTimeLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterDateTimeLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitDateTimeLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitDateTimeLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateTimeLiteralsContext dateTimeLiterals() throws RecognitionException {
		DateTimeLiteralsContext _localctx = new DateTimeLiteralsContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_dateTimeLiterals);
		int _la;
		try {
			setState(341);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DATE:
			case TIME:
			case TIMESTAMP:
				enterOuterAlt(_localctx, 1);
				{
				setState(334);
				_la = _input.LA(1);
				if ( !(((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (DATE - 128)) | (1L << (TIME - 128)) | (1L << (TIMESTAMP - 128)))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(335);
				match(STRING_);
				}
				break;
			case LBE_:
				enterOuterAlt(_localctx, 2);
				{
				setState(336);
				match(LBE_);
				setState(337);
				identifier_();
				setState(338);
				match(STRING_);
				setState(339);
				match(RBE_);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class HexadecimalLiteralsContext extends ParserRuleContext {
		public TerminalNode HEX_DIGIT_() { return getToken(LinglongSQLStatementParser.HEX_DIGIT_, 0); }
		public CharacterSetName_Context characterSetName_() {
			return getRuleContext(CharacterSetName_Context.class,0);
		}
		public CollateClause_Context collateClause_() {
			return getRuleContext(CollateClause_Context.class,0);
		}
		public HexadecimalLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hexadecimalLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterHexadecimalLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitHexadecimalLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitHexadecimalLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HexadecimalLiteralsContext hexadecimalLiterals() throws RecognitionException {
		HexadecimalLiteralsContext _localctx = new HexadecimalLiteralsContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_hexadecimalLiterals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(344);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER_) {
				{
				setState(343);
				characterSetName_();
				}
			}

			setState(346);
			match(HEX_DIGIT_);
			setState(348);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				{
				setState(347);
				collateClause_();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BitValueLiteralsContext extends ParserRuleContext {
		public TerminalNode BIT_NUM_() { return getToken(LinglongSQLStatementParser.BIT_NUM_, 0); }
		public CharacterSetName_Context characterSetName_() {
			return getRuleContext(CharacterSetName_Context.class,0);
		}
		public CollateClause_Context collateClause_() {
			return getRuleContext(CollateClause_Context.class,0);
		}
		public BitValueLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bitValueLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterBitValueLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitBitValueLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitBitValueLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BitValueLiteralsContext bitValueLiterals() throws RecognitionException {
		BitValueLiteralsContext _localctx = new BitValueLiteralsContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_bitValueLiterals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(351);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER_) {
				{
				setState(350);
				characterSetName_();
				}
			}

			setState(353);
			match(BIT_NUM_);
			setState(355);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				{
				setState(354);
				collateClause_();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanLiteralsContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(LinglongSQLStatementParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(LinglongSQLStatementParser.FALSE, 0); }
		public BooleanLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterBooleanLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitBooleanLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitBooleanLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanLiteralsContext booleanLiterals() throws RecognitionException {
		BooleanLiteralsContext _localctx = new BooleanLiteralsContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_booleanLiterals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(357);
			_la = _input.LA(1);
			if ( !(_la==TRUE || _la==FALSE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NullValueLiteralsContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(LinglongSQLStatementParser.NULL, 0); }
		public NullValueLiteralsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nullValueLiterals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterNullValueLiterals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitNullValueLiterals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitNullValueLiterals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NullValueLiteralsContext nullValueLiterals() throws RecognitionException {
		NullValueLiteralsContext _localctx = new NullValueLiteralsContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_nullValueLiterals);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(359);
			match(NULL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Identifier_Context extends ParserRuleContext {
		public TerminalNode IDENTIFIER_() { return getToken(LinglongSQLStatementParser.IDENTIFIER_, 0); }
		public UnreservedWord_Context unreservedWord_() {
			return getRuleContext(UnreservedWord_Context.class,0);
		}
		public Identifier_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterIdentifier_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitIdentifier_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitIdentifier_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Identifier_Context identifier_() throws RecognitionException {
		Identifier_Context _localctx = new Identifier_Context(_ctx, getState());
		enterRule(_localctx, 54, RULE_identifier_);
		try {
			setState(363);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(361);
				match(IDENTIFIER_);
				}
				break;
			case TRUNCATE:
			case POSITION:
			case VIEW:
			case ANY:
			case OFFSET:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case YEAR:
			case QUARTER:
			case MONTH:
			case WEEK:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case INSTANCE:
			case DO:
			case DEFINER:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case OPEN:
			case NEXT:
			case NAME:
			case END:
			case EVERY:
			case PASSWORD:
			case PRIVILEGES:
			case QUERY:
			case USER:
			case MODE:
			case STDDEV:
			case BOTTOM:
			case FIRST:
			case LAST:
			case TABLES:
			case TABLESPACE:
			case COLUMNS:
			case FIELDS:
			case INDEXES:
			case STATUS:
			case MODIFY:
			case VALUE:
			case DUPLICATE:
			case AFTER:
			case OJ:
			case ACCOUNT:
			case ROLE:
			case START:
			case TRANSACTION:
			case WITHOUT:
			case ESCAPE:
			case SUBPARTITION:
			case STORAGE:
			case SUPER:
			case TEMPORARY:
			case THAN:
			case UNBOUNDED:
			case UPGRADE:
			case VALIDATION:
			case ROLLUP:
			case SOUNDS:
			case UNKNOWN:
			case OFF:
			case ALWAYS:
			case COMMITTED:
			case LEVEL:
			case NO:
			case ACTION:
			case ALGORITHM:
			case AUTOCOMMIT:
			case BTREE:
			case CHAIN:
			case CHARSET:
			case CHECKSUM:
			case CIPHER:
			case CLIENT:
			case COALESCE:
			case COMMENT:
			case COMPACT:
			case COMPRESSED:
			case COMPRESSION:
			case CONNECTION:
			case CONSISTENT:
			case DATA:
			case DISCARD:
			case DISK:
			case ENCRYPTION:
			case ENGINE:
			case EVENT:
			case EXCHANGE:
			case EXECUTE:
			case FILE:
			case FIXED:
			case FOLLOWING:
			case GLOBAL:
			case HASH:
			case IMPORT_:
			case LESS:
			case MEMORY:
			case NONE:
			case PARSER:
			case PARTIAL:
			case PARTITIONING:
			case PERSIST:
			case PRECEDING:
			case PROCESS:
			case PROXY:
			case QUICK:
			case REBUILD:
			case REDUNDANT:
			case RELOAD:
			case REMOVE:
			case REORGANIZE:
			case REPAIR:
			case REVERSE:
			case SESSION:
			case SHUTDOWN:
			case SIMPLE:
			case SLAVE:
			case VISIBLE:
			case INVISIBLE:
			case ENFORCED:
			case AGAINST:
			case LANGUAGE:
			case EXTENDED:
			case EXPANSION:
			case VARIANCE:
			case MAX_ROWS:
			case MIN_ROWS:
			case SQL_BIG_RESULT:
			case SQL_BUFFER_RESULT:
			case SQL_CACHE:
			case SQL_NO_CACHE:
			case STATS_AUTO_RECALC:
			case STATS_PERSISTENT:
			case STATS_SAMPLE_PAGES:
			case ROW_FORMAT:
			case WEIGHT_STRING:
			case COLUMN_FORMAT:
			case INSERT_METHOD:
			case KEY_BLOCK_SIZE:
			case PACK_KEYS:
			case PERSIST_ONLY:
			case BIT_AND:
			case BIT_OR:
			case BIT_XOR:
			case GROUP_CONCAT:
			case JSON_ARRAYAGG:
			case JSON_OBJECTAGG:
			case STD:
			case STDDEV_POP:
			case STDDEV_SAMP:
			case VAR_POP:
			case VAR_SAMP:
			case AUTO_INCREMENT:
			case AVG_ROW_LENGTH:
			case DELAY_KEY_WRITE:
			case ROTATE:
			case MASTER:
			case BINLOG:
			case ERROR:
			case SCHEDULE:
			case COMPLETION:
			case HOST:
			case SOCKET:
			case PORT:
			case SERVER:
			case WRAPPER:
			case OPTIONS:
			case OWNER:
			case RETURNS:
			case CONTAINS:
			case SECURITY:
			case INVOKER:
			case TEMPTABLE:
			case MERGE:
			case UNDEFINED:
			case DATAFILE:
			case FILE_BLOCK_SIZE:
			case EXTENT_SIZE:
			case INITIAL_SIZE:
			case AUTOEXTEND_SIZE:
			case MAX_SIZE:
			case NODEGROUP:
			case WAIT:
			case LOGFILE:
			case UNDOFILE:
			case UNDO_BUFFER_SIZE:
			case REDO_BUFFER_SIZE:
			case HANDLER:
			case PREV:
			case ORGANIZATION:
			case DEFINITION:
			case DESCRIPTION:
			case REFERENCE:
			case FOLLOWS:
			case PRECEDES:
			case IMPORT:
			case CONCURRENT:
			case XML:
			case DUMPFILE:
			case SHARE:
				enterOuterAlt(_localctx, 2);
				{
				setState(362);
				unreservedWord_();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Variable_Context extends ParserRuleContext {
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public List<TerminalNode> AT_() { return getTokens(LinglongSQLStatementParser.AT_); }
		public TerminalNode AT_(int i) {
			return getToken(LinglongSQLStatementParser.AT_, i);
		}
		public TerminalNode DOT_() { return getToken(LinglongSQLStatementParser.DOT_, 0); }
		public TerminalNode GLOBAL() { return getToken(LinglongSQLStatementParser.GLOBAL, 0); }
		public TerminalNode PERSIST() { return getToken(LinglongSQLStatementParser.PERSIST, 0); }
		public TerminalNode PERSIST_ONLY() { return getToken(LinglongSQLStatementParser.PERSIST_ONLY, 0); }
		public TerminalNode SESSION() { return getToken(LinglongSQLStatementParser.SESSION, 0); }
		public Variable_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterVariable_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitVariable_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitVariable_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Variable_Context variable_() throws RecognitionException {
		Variable_Context _localctx = new Variable_Context(_ctx, getState());
		enterRule(_localctx, 56, RULE_variable_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(369);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AT_) {
				{
				setState(366);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
				case 1:
					{
					setState(365);
					match(AT_);
					}
					break;
				}
				setState(368);
				match(AT_);
				}
			}

			setState(372);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				{
				setState(371);
				_la = _input.LA(1);
				if ( !(((((_la - 351)) & ~0x3f) == 0 && ((1L << (_la - 351)) & ((1L << (GLOBAL - 351)) | (1L << (PERSIST - 351)) | (1L << (SESSION - 351)))) != 0) || _la==PERSIST_ONLY) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			}
			setState(375);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DOT_) {
				{
				setState(374);
				match(DOT_);
				}
			}

			setState(377);
			identifier_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnreservedWord_Context extends ParserRuleContext {
		public TerminalNode ACCOUNT() { return getToken(LinglongSQLStatementParser.ACCOUNT, 0); }
		public TerminalNode ACTION() { return getToken(LinglongSQLStatementParser.ACTION, 0); }
		public TerminalNode AFTER() { return getToken(LinglongSQLStatementParser.AFTER, 0); }
		public TerminalNode ALGORITHM() { return getToken(LinglongSQLStatementParser.ALGORITHM, 0); }
		public TerminalNode ALWAYS() { return getToken(LinglongSQLStatementParser.ALWAYS, 0); }
		public TerminalNode ANY() { return getToken(LinglongSQLStatementParser.ANY, 0); }
		public TerminalNode AUTO_INCREMENT() { return getToken(LinglongSQLStatementParser.AUTO_INCREMENT, 0); }
		public TerminalNode AVG_ROW_LENGTH() { return getToken(LinglongSQLStatementParser.AVG_ROW_LENGTH, 0); }
		public TerminalNode BEGIN() { return getToken(LinglongSQLStatementParser.BEGIN, 0); }
		public TerminalNode BTREE() { return getToken(LinglongSQLStatementParser.BTREE, 0); }
		public TerminalNode CHAIN() { return getToken(LinglongSQLStatementParser.CHAIN, 0); }
		public TerminalNode CHARSET() { return getToken(LinglongSQLStatementParser.CHARSET, 0); }
		public TerminalNode CHECKSUM() { return getToken(LinglongSQLStatementParser.CHECKSUM, 0); }
		public TerminalNode CIPHER() { return getToken(LinglongSQLStatementParser.CIPHER, 0); }
		public TerminalNode CLIENT() { return getToken(LinglongSQLStatementParser.CLIENT, 0); }
		public TerminalNode COALESCE() { return getToken(LinglongSQLStatementParser.COALESCE, 0); }
		public TerminalNode COLUMNS() { return getToken(LinglongSQLStatementParser.COLUMNS, 0); }
		public TerminalNode COLUMN_FORMAT() { return getToken(LinglongSQLStatementParser.COLUMN_FORMAT, 0); }
		public TerminalNode COMMENT() { return getToken(LinglongSQLStatementParser.COMMENT, 0); }
		public TerminalNode COMMIT() { return getToken(LinglongSQLStatementParser.COMMIT, 0); }
		public TerminalNode COMMITTED() { return getToken(LinglongSQLStatementParser.COMMITTED, 0); }
		public TerminalNode COMPACT() { return getToken(LinglongSQLStatementParser.COMPACT, 0); }
		public TerminalNode COMPRESSED() { return getToken(LinglongSQLStatementParser.COMPRESSED, 0); }
		public TerminalNode COMPRESSION() { return getToken(LinglongSQLStatementParser.COMPRESSION, 0); }
		public TerminalNode CONNECTION() { return getToken(LinglongSQLStatementParser.CONNECTION, 0); }
		public TerminalNode CONSISTENT() { return getToken(LinglongSQLStatementParser.CONSISTENT, 0); }
		public TerminalNode CURRENT() { return getToken(LinglongSQLStatementParser.CURRENT, 0); }
		public TerminalNode DATA() { return getToken(LinglongSQLStatementParser.DATA, 0); }
		public TerminalNode DATE() { return getToken(LinglongSQLStatementParser.DATE, 0); }
		public TerminalNode DELAY_KEY_WRITE() { return getToken(LinglongSQLStatementParser.DELAY_KEY_WRITE, 0); }
		public TerminalNode DISABLE() { return getToken(LinglongSQLStatementParser.DISABLE, 0); }
		public TerminalNode DISCARD() { return getToken(LinglongSQLStatementParser.DISCARD, 0); }
		public TerminalNode DISK() { return getToken(LinglongSQLStatementParser.DISK, 0); }
		public TerminalNode DUPLICATE() { return getToken(LinglongSQLStatementParser.DUPLICATE, 0); }
		public TerminalNode ENABLE() { return getToken(LinglongSQLStatementParser.ENABLE, 0); }
		public TerminalNode ENCRYPTION() { return getToken(LinglongSQLStatementParser.ENCRYPTION, 0); }
		public TerminalNode ENFORCED() { return getToken(LinglongSQLStatementParser.ENFORCED, 0); }
		public TerminalNode END() { return getToken(LinglongSQLStatementParser.END, 0); }
		public TerminalNode ENGINE() { return getToken(LinglongSQLStatementParser.ENGINE, 0); }
		public TerminalNode ESCAPE() { return getToken(LinglongSQLStatementParser.ESCAPE, 0); }
		public TerminalNode EVENT() { return getToken(LinglongSQLStatementParser.EVENT, 0); }
		public TerminalNode EXCHANGE() { return getToken(LinglongSQLStatementParser.EXCHANGE, 0); }
		public TerminalNode EXECUTE() { return getToken(LinglongSQLStatementParser.EXECUTE, 0); }
		public TerminalNode FILE() { return getToken(LinglongSQLStatementParser.FILE, 0); }
		public TerminalNode FIRST() { return getToken(LinglongSQLStatementParser.FIRST, 0); }
		public TerminalNode FIXED() { return getToken(LinglongSQLStatementParser.FIXED, 0); }
		public TerminalNode FOLLOWING() { return getToken(LinglongSQLStatementParser.FOLLOWING, 0); }
		public TerminalNode GLOBAL() { return getToken(LinglongSQLStatementParser.GLOBAL, 0); }
		public TerminalNode HASH() { return getToken(LinglongSQLStatementParser.HASH, 0); }
		public TerminalNode IMPORT_() { return getToken(LinglongSQLStatementParser.IMPORT_, 0); }
		public TerminalNode INSERT_METHOD() { return getToken(LinglongSQLStatementParser.INSERT_METHOD, 0); }
		public TerminalNode INVISIBLE() { return getToken(LinglongSQLStatementParser.INVISIBLE, 0); }
		public TerminalNode KEY_BLOCK_SIZE() { return getToken(LinglongSQLStatementParser.KEY_BLOCK_SIZE, 0); }
		public TerminalNode LAST() { return getToken(LinglongSQLStatementParser.LAST, 0); }
		public TerminalNode LESS() { return getToken(LinglongSQLStatementParser.LESS, 0); }
		public TerminalNode LEVEL() { return getToken(LinglongSQLStatementParser.LEVEL, 0); }
		public TerminalNode MAX_ROWS() { return getToken(LinglongSQLStatementParser.MAX_ROWS, 0); }
		public TerminalNode MEMORY() { return getToken(LinglongSQLStatementParser.MEMORY, 0); }
		public TerminalNode MIN_ROWS() { return getToken(LinglongSQLStatementParser.MIN_ROWS, 0); }
		public TerminalNode MODIFY() { return getToken(LinglongSQLStatementParser.MODIFY, 0); }
		public TerminalNode NO() { return getToken(LinglongSQLStatementParser.NO, 0); }
		public TerminalNode NONE() { return getToken(LinglongSQLStatementParser.NONE, 0); }
		public TerminalNode OFFSET() { return getToken(LinglongSQLStatementParser.OFFSET, 0); }
		public TerminalNode PACK_KEYS() { return getToken(LinglongSQLStatementParser.PACK_KEYS, 0); }
		public TerminalNode PARSER() { return getToken(LinglongSQLStatementParser.PARSER, 0); }
		public TerminalNode PARTIAL() { return getToken(LinglongSQLStatementParser.PARTIAL, 0); }
		public TerminalNode PARTITIONING() { return getToken(LinglongSQLStatementParser.PARTITIONING, 0); }
		public TerminalNode PASSWORD() { return getToken(LinglongSQLStatementParser.PASSWORD, 0); }
		public TerminalNode PERSIST() { return getToken(LinglongSQLStatementParser.PERSIST, 0); }
		public TerminalNode PERSIST_ONLY() { return getToken(LinglongSQLStatementParser.PERSIST_ONLY, 0); }
		public TerminalNode PRECEDING() { return getToken(LinglongSQLStatementParser.PRECEDING, 0); }
		public TerminalNode PRIVILEGES() { return getToken(LinglongSQLStatementParser.PRIVILEGES, 0); }
		public TerminalNode PROCESS() { return getToken(LinglongSQLStatementParser.PROCESS, 0); }
		public TerminalNode PROXY() { return getToken(LinglongSQLStatementParser.PROXY, 0); }
		public TerminalNode QUICK() { return getToken(LinglongSQLStatementParser.QUICK, 0); }
		public TerminalNode REBUILD() { return getToken(LinglongSQLStatementParser.REBUILD, 0); }
		public TerminalNode REDUNDANT() { return getToken(LinglongSQLStatementParser.REDUNDANT, 0); }
		public TerminalNode RELOAD() { return getToken(LinglongSQLStatementParser.RELOAD, 0); }
		public TerminalNode REMOVE() { return getToken(LinglongSQLStatementParser.REMOVE, 0); }
		public TerminalNode REORGANIZE() { return getToken(LinglongSQLStatementParser.REORGANIZE, 0); }
		public TerminalNode REPAIR() { return getToken(LinglongSQLStatementParser.REPAIR, 0); }
		public TerminalNode REVERSE() { return getToken(LinglongSQLStatementParser.REVERSE, 0); }
		public TerminalNode ROLLBACK() { return getToken(LinglongSQLStatementParser.ROLLBACK, 0); }
		public TerminalNode ROLLUP() { return getToken(LinglongSQLStatementParser.ROLLUP, 0); }
		public TerminalNode ROW_FORMAT() { return getToken(LinglongSQLStatementParser.ROW_FORMAT, 0); }
		public TerminalNode SAVEPOINT() { return getToken(LinglongSQLStatementParser.SAVEPOINT, 0); }
		public TerminalNode SESSION() { return getToken(LinglongSQLStatementParser.SESSION, 0); }
		public TerminalNode SHUTDOWN() { return getToken(LinglongSQLStatementParser.SHUTDOWN, 0); }
		public TerminalNode SIMPLE() { return getToken(LinglongSQLStatementParser.SIMPLE, 0); }
		public TerminalNode SLAVE() { return getToken(LinglongSQLStatementParser.SLAVE, 0); }
		public TerminalNode SOUNDS() { return getToken(LinglongSQLStatementParser.SOUNDS, 0); }
		public TerminalNode SQL_BIG_RESULT() { return getToken(LinglongSQLStatementParser.SQL_BIG_RESULT, 0); }
		public TerminalNode SQL_BUFFER_RESULT() { return getToken(LinglongSQLStatementParser.SQL_BUFFER_RESULT, 0); }
		public TerminalNode SQL_CACHE() { return getToken(LinglongSQLStatementParser.SQL_CACHE, 0); }
		public TerminalNode SQL_NO_CACHE() { return getToken(LinglongSQLStatementParser.SQL_NO_CACHE, 0); }
		public TerminalNode START() { return getToken(LinglongSQLStatementParser.START, 0); }
		public TerminalNode STATS_AUTO_RECALC() { return getToken(LinglongSQLStatementParser.STATS_AUTO_RECALC, 0); }
		public TerminalNode STATS_PERSISTENT() { return getToken(LinglongSQLStatementParser.STATS_PERSISTENT, 0); }
		public TerminalNode STATS_SAMPLE_PAGES() { return getToken(LinglongSQLStatementParser.STATS_SAMPLE_PAGES, 0); }
		public TerminalNode STORAGE() { return getToken(LinglongSQLStatementParser.STORAGE, 0); }
		public TerminalNode SUBPARTITION() { return getToken(LinglongSQLStatementParser.SUBPARTITION, 0); }
		public TerminalNode SUPER() { return getToken(LinglongSQLStatementParser.SUPER, 0); }
		public TerminalNode TABLES() { return getToken(LinglongSQLStatementParser.TABLES, 0); }
		public TerminalNode TABLESPACE() { return getToken(LinglongSQLStatementParser.TABLESPACE, 0); }
		public TerminalNode TEMPORARY() { return getToken(LinglongSQLStatementParser.TEMPORARY, 0); }
		public TerminalNode THAN() { return getToken(LinglongSQLStatementParser.THAN, 0); }
		public TerminalNode TIME() { return getToken(LinglongSQLStatementParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(LinglongSQLStatementParser.TIMESTAMP, 0); }
		public TerminalNode TRANSACTION() { return getToken(LinglongSQLStatementParser.TRANSACTION, 0); }
		public TerminalNode TRUNCATE() { return getToken(LinglongSQLStatementParser.TRUNCATE, 0); }
		public TerminalNode UNBOUNDED() { return getToken(LinglongSQLStatementParser.UNBOUNDED, 0); }
		public TerminalNode UNKNOWN() { return getToken(LinglongSQLStatementParser.UNKNOWN, 0); }
		public TerminalNode UPGRADE() { return getToken(LinglongSQLStatementParser.UPGRADE, 0); }
		public TerminalNode VALIDATION() { return getToken(LinglongSQLStatementParser.VALIDATION, 0); }
		public TerminalNode VALUE() { return getToken(LinglongSQLStatementParser.VALUE, 0); }
		public TerminalNode VIEW() { return getToken(LinglongSQLStatementParser.VIEW, 0); }
		public TerminalNode VISIBLE() { return getToken(LinglongSQLStatementParser.VISIBLE, 0); }
		public TerminalNode WEIGHT_STRING() { return getToken(LinglongSQLStatementParser.WEIGHT_STRING, 0); }
		public TerminalNode WITHOUT() { return getToken(LinglongSQLStatementParser.WITHOUT, 0); }
		public TerminalNode MICROSECOND() { return getToken(LinglongSQLStatementParser.MICROSECOND, 0); }
		public TerminalNode SECOND() { return getToken(LinglongSQLStatementParser.SECOND, 0); }
		public TerminalNode MINUTE() { return getToken(LinglongSQLStatementParser.MINUTE, 0); }
		public TerminalNode HOUR() { return getToken(LinglongSQLStatementParser.HOUR, 0); }
		public TerminalNode DAY() { return getToken(LinglongSQLStatementParser.DAY, 0); }
		public TerminalNode WEEK() { return getToken(LinglongSQLStatementParser.WEEK, 0); }
		public TerminalNode MONTH() { return getToken(LinglongSQLStatementParser.MONTH, 0); }
		public TerminalNode QUARTER() { return getToken(LinglongSQLStatementParser.QUARTER, 0); }
		public TerminalNode YEAR() { return getToken(LinglongSQLStatementParser.YEAR, 0); }
		public TerminalNode AGAINST() { return getToken(LinglongSQLStatementParser.AGAINST, 0); }
		public TerminalNode LANGUAGE() { return getToken(LinglongSQLStatementParser.LANGUAGE, 0); }
		public TerminalNode MODE() { return getToken(LinglongSQLStatementParser.MODE, 0); }
		public TerminalNode QUERY() { return getToken(LinglongSQLStatementParser.QUERY, 0); }
		public TerminalNode EXPANSION() { return getToken(LinglongSQLStatementParser.EXPANSION, 0); }
		public TerminalNode BOOLEAN() { return getToken(LinglongSQLStatementParser.BOOLEAN, 0); }
		public TerminalNode MAX() { return getToken(LinglongSQLStatementParser.MAX, 0); }
		public TerminalNode MIN() { return getToken(LinglongSQLStatementParser.MIN, 0); }
		public TerminalNode SUM() { return getToken(LinglongSQLStatementParser.SUM, 0); }
		public TerminalNode COUNT() { return getToken(LinglongSQLStatementParser.COUNT, 0); }
		public TerminalNode AVG() { return getToken(LinglongSQLStatementParser.AVG, 0); }
		public TerminalNode BIT_AND() { return getToken(LinglongSQLStatementParser.BIT_AND, 0); }
		public TerminalNode BIT_OR() { return getToken(LinglongSQLStatementParser.BIT_OR, 0); }
		public TerminalNode BIT_XOR() { return getToken(LinglongSQLStatementParser.BIT_XOR, 0); }
		public TerminalNode GROUP_CONCAT() { return getToken(LinglongSQLStatementParser.GROUP_CONCAT, 0); }
		public TerminalNode JSON_ARRAYAGG() { return getToken(LinglongSQLStatementParser.JSON_ARRAYAGG, 0); }
		public TerminalNode JSON_OBJECTAGG() { return getToken(LinglongSQLStatementParser.JSON_OBJECTAGG, 0); }
		public TerminalNode STD() { return getToken(LinglongSQLStatementParser.STD, 0); }
		public TerminalNode STDDEV() { return getToken(LinglongSQLStatementParser.STDDEV, 0); }
		public TerminalNode STDDEV_POP() { return getToken(LinglongSQLStatementParser.STDDEV_POP, 0); }
		public TerminalNode STDDEV_SAMP() { return getToken(LinglongSQLStatementParser.STDDEV_SAMP, 0); }
		public TerminalNode VAR_POP() { return getToken(LinglongSQLStatementParser.VAR_POP, 0); }
		public TerminalNode VAR_SAMP() { return getToken(LinglongSQLStatementParser.VAR_SAMP, 0); }
		public TerminalNode VARIANCE() { return getToken(LinglongSQLStatementParser.VARIANCE, 0); }
		public TerminalNode EXTENDED() { return getToken(LinglongSQLStatementParser.EXTENDED, 0); }
		public TerminalNode STATUS() { return getToken(LinglongSQLStatementParser.STATUS, 0); }
		public TerminalNode FIELDS() { return getToken(LinglongSQLStatementParser.FIELDS, 0); }
		public TerminalNode INDEXES() { return getToken(LinglongSQLStatementParser.INDEXES, 0); }
		public TerminalNode USER() { return getToken(LinglongSQLStatementParser.USER, 0); }
		public TerminalNode ROLE() { return getToken(LinglongSQLStatementParser.ROLE, 0); }
		public TerminalNode OJ() { return getToken(LinglongSQLStatementParser.OJ, 0); }
		public TerminalNode AUTOCOMMIT() { return getToken(LinglongSQLStatementParser.AUTOCOMMIT, 0); }
		public TerminalNode OFF() { return getToken(LinglongSQLStatementParser.OFF, 0); }
		public TerminalNode ROTATE() { return getToken(LinglongSQLStatementParser.ROTATE, 0); }
		public TerminalNode INSTANCE() { return getToken(LinglongSQLStatementParser.INSTANCE, 0); }
		public TerminalNode MASTER() { return getToken(LinglongSQLStatementParser.MASTER, 0); }
		public TerminalNode BINLOG() { return getToken(LinglongSQLStatementParser.BINLOG, 0); }
		public TerminalNode ERROR() { return getToken(LinglongSQLStatementParser.ERROR, 0); }
		public TerminalNode SCHEDULE() { return getToken(LinglongSQLStatementParser.SCHEDULE, 0); }
		public TerminalNode COMPLETION() { return getToken(LinglongSQLStatementParser.COMPLETION, 0); }
		public TerminalNode DO() { return getToken(LinglongSQLStatementParser.DO, 0); }
		public TerminalNode DEFINER() { return getToken(LinglongSQLStatementParser.DEFINER, 0); }
		public TerminalNode EVERY() { return getToken(LinglongSQLStatementParser.EVERY, 0); }
		public TerminalNode HOST() { return getToken(LinglongSQLStatementParser.HOST, 0); }
		public TerminalNode SOCKET() { return getToken(LinglongSQLStatementParser.SOCKET, 0); }
		public TerminalNode OWNER() { return getToken(LinglongSQLStatementParser.OWNER, 0); }
		public TerminalNode PORT() { return getToken(LinglongSQLStatementParser.PORT, 0); }
		public TerminalNode RETURNS() { return getToken(LinglongSQLStatementParser.RETURNS, 0); }
		public TerminalNode CONTAINS() { return getToken(LinglongSQLStatementParser.CONTAINS, 0); }
		public TerminalNode SECURITY() { return getToken(LinglongSQLStatementParser.SECURITY, 0); }
		public TerminalNode INVOKER() { return getToken(LinglongSQLStatementParser.INVOKER, 0); }
		public TerminalNode UNDEFINED() { return getToken(LinglongSQLStatementParser.UNDEFINED, 0); }
		public TerminalNode MERGE() { return getToken(LinglongSQLStatementParser.MERGE, 0); }
		public TerminalNode TEMPTABLE() { return getToken(LinglongSQLStatementParser.TEMPTABLE, 0); }
		public TerminalNode CASCADED() { return getToken(LinglongSQLStatementParser.CASCADED, 0); }
		public TerminalNode LOCAL() { return getToken(LinglongSQLStatementParser.LOCAL, 0); }
		public TerminalNode SERVER() { return getToken(LinglongSQLStatementParser.SERVER, 0); }
		public TerminalNode WRAPPER() { return getToken(LinglongSQLStatementParser.WRAPPER, 0); }
		public TerminalNode OPTIONS() { return getToken(LinglongSQLStatementParser.OPTIONS, 0); }
		public TerminalNode DATAFILE() { return getToken(LinglongSQLStatementParser.DATAFILE, 0); }
		public TerminalNode FILE_BLOCK_SIZE() { return getToken(LinglongSQLStatementParser.FILE_BLOCK_SIZE, 0); }
		public TerminalNode EXTENT_SIZE() { return getToken(LinglongSQLStatementParser.EXTENT_SIZE, 0); }
		public TerminalNode INITIAL_SIZE() { return getToken(LinglongSQLStatementParser.INITIAL_SIZE, 0); }
		public TerminalNode AUTOEXTEND_SIZE() { return getToken(LinglongSQLStatementParser.AUTOEXTEND_SIZE, 0); }
		public TerminalNode MAX_SIZE() { return getToken(LinglongSQLStatementParser.MAX_SIZE, 0); }
		public TerminalNode NODEGROUP() { return getToken(LinglongSQLStatementParser.NODEGROUP, 0); }
		public TerminalNode WAIT() { return getToken(LinglongSQLStatementParser.WAIT, 0); }
		public TerminalNode LOGFILE() { return getToken(LinglongSQLStatementParser.LOGFILE, 0); }
		public TerminalNode UNDOFILE() { return getToken(LinglongSQLStatementParser.UNDOFILE, 0); }
		public TerminalNode UNDO_BUFFER_SIZE() { return getToken(LinglongSQLStatementParser.UNDO_BUFFER_SIZE, 0); }
		public TerminalNode REDO_BUFFER_SIZE() { return getToken(LinglongSQLStatementParser.REDO_BUFFER_SIZE, 0); }
		public TerminalNode DEFINITION() { return getToken(LinglongSQLStatementParser.DEFINITION, 0); }
		public TerminalNode ORGANIZATION() { return getToken(LinglongSQLStatementParser.ORGANIZATION, 0); }
		public TerminalNode DESCRIPTION() { return getToken(LinglongSQLStatementParser.DESCRIPTION, 0); }
		public TerminalNode REFERENCE() { return getToken(LinglongSQLStatementParser.REFERENCE, 0); }
		public TerminalNode FOLLOWS() { return getToken(LinglongSQLStatementParser.FOLLOWS, 0); }
		public TerminalNode PRECEDES() { return getToken(LinglongSQLStatementParser.PRECEDES, 0); }
		public TerminalNode NAME() { return getToken(LinglongSQLStatementParser.NAME, 0); }
		public TerminalNode CLOSE() { return getToken(LinglongSQLStatementParser.CLOSE, 0); }
		public TerminalNode OPEN() { return getToken(LinglongSQLStatementParser.OPEN, 0); }
		public TerminalNode NEXT() { return getToken(LinglongSQLStatementParser.NEXT, 0); }
		public TerminalNode HANDLER() { return getToken(LinglongSQLStatementParser.HANDLER, 0); }
		public TerminalNode PREV() { return getToken(LinglongSQLStatementParser.PREV, 0); }
		public TerminalNode IMPORT() { return getToken(LinglongSQLStatementParser.IMPORT, 0); }
		public TerminalNode CONCURRENT() { return getToken(LinglongSQLStatementParser.CONCURRENT, 0); }
		public TerminalNode XML() { return getToken(LinglongSQLStatementParser.XML, 0); }
		public TerminalNode POSITION() { return getToken(LinglongSQLStatementParser.POSITION, 0); }
		public TerminalNode SHARE() { return getToken(LinglongSQLStatementParser.SHARE, 0); }
		public TerminalNode DUMPFILE() { return getToken(LinglongSQLStatementParser.DUMPFILE, 0); }
		public TerminalNode BOTTOM() { return getToken(LinglongSQLStatementParser.BOTTOM, 0); }
		public UnreservedWord_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unreservedWord_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterUnreservedWord_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitUnreservedWord_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitUnreservedWord_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnreservedWord_Context unreservedWord_() throws RecognitionException {
		UnreservedWord_Context _localctx = new UnreservedWord_Context(_ctx, getState());
		enterRule(_localctx, 58, RULE_unreservedWord_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(379);
			_la = _input.LA(1);
			if ( !(_la==TRUNCATE || _la==POSITION || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & ((1L << (VIEW - 68)) | (1L << (ANY - 68)) | (1L << (OFFSET - 68)) | (1L << (BEGIN - 68)) | (1L << (COMMIT - 68)) | (1L << (ROLLBACK - 68)) | (1L << (SAVEPOINT - 68)) | (1L << (BOOLEAN - 68)) | (1L << (DATE - 68)) | (1L << (TIME - 68)) | (1L << (TIMESTAMP - 68)))) != 0) || ((((_la - 133)) & ~0x3f) == 0 && ((1L << (_la - 133)) & ((1L << (YEAR - 133)) | (1L << (QUARTER - 133)) | (1L << (MONTH - 133)) | (1L << (WEEK - 133)) | (1L << (DAY - 133)) | (1L << (HOUR - 133)) | (1L << (MINUTE - 133)) | (1L << (SECOND - 133)) | (1L << (MICROSECOND - 133)) | (1L << (MAX - 133)) | (1L << (MIN - 133)) | (1L << (SUM - 133)) | (1L << (COUNT - 133)) | (1L << (AVG - 133)) | (1L << (CURRENT - 133)) | (1L << (ENABLE - 133)) | (1L << (DISABLE - 133)) | (1L << (INSTANCE - 133)) | (1L << (DO - 133)) | (1L << (DEFINER - 133)) | (1L << (CASCADED - 133)) | (1L << (LOCAL - 133)) | (1L << (CLOSE - 133)) | (1L << (OPEN - 133)) | (1L << (NEXT - 133)) | (1L << (NAME - 133)) | (1L << (END - 133)) | (1L << (EVERY - 133)) | (1L << (PASSWORD - 133)) | (1L << (PRIVILEGES - 133)) | (1L << (QUERY - 133)))) != 0) || ((((_la - 200)) & ~0x3f) == 0 && ((1L << (_la - 200)) & ((1L << (USER - 200)) | (1L << (MODE - 200)) | (1L << (STDDEV - 200)) | (1L << (BOTTOM - 200)) | (1L << (FIRST - 200)) | (1L << (LAST - 200)) | (1L << (TABLES - 200)) | (1L << (TABLESPACE - 200)) | (1L << (COLUMNS - 200)) | (1L << (FIELDS - 200)) | (1L << (INDEXES - 200)) | (1L << (STATUS - 200)) | (1L << (MODIFY - 200)) | (1L << (VALUE - 200)) | (1L << (DUPLICATE - 200)) | (1L << (AFTER - 200)))) != 0) || ((((_la - 264)) & ~0x3f) == 0 && ((1L << (_la - 264)) & ((1L << (OJ - 264)) | (1L << (ACCOUNT - 264)) | (1L << (ROLE - 264)) | (1L << (START - 264)) | (1L << (TRANSACTION - 264)) | (1L << (WITHOUT - 264)) | (1L << (ESCAPE - 264)) | (1L << (SUBPARTITION - 264)) | (1L << (STORAGE - 264)) | (1L << (SUPER - 264)) | (1L << (TEMPORARY - 264)) | (1L << (THAN - 264)) | (1L << (UNBOUNDED - 264)) | (1L << (UPGRADE - 264)) | (1L << (VALIDATION - 264)) | (1L << (ROLLUP - 264)) | (1L << (SOUNDS - 264)) | (1L << (UNKNOWN - 264)) | (1L << (OFF - 264)) | (1L << (ALWAYS - 264)) | (1L << (COMMITTED - 264)) | (1L << (LEVEL - 264)) | (1L << (NO - 264)) | (1L << (ACTION - 264)) | (1L << (ALGORITHM - 264)) | (1L << (AUTOCOMMIT - 264)) | (1L << (BTREE - 264)) | (1L << (CHAIN - 264)) | (1L << (CHARSET - 264)) | (1L << (CHECKSUM - 264)) | (1L << (CIPHER - 264)) | (1L << (CLIENT - 264)) | (1L << (COALESCE - 264)) | (1L << (COMMENT - 264)) | (1L << (COMPACT - 264)) | (1L << (COMPRESSED - 264)) | (1L << (COMPRESSION - 264)))) != 0) || ((((_la - 328)) & ~0x3f) == 0 && ((1L << (_la - 328)) & ((1L << (CONNECTION - 328)) | (1L << (CONSISTENT - 328)) | (1L << (DATA - 328)) | (1L << (DISCARD - 328)) | (1L << (DISK - 328)) | (1L << (ENCRYPTION - 328)) | (1L << (ENGINE - 328)) | (1L << (EVENT - 328)) | (1L << (EXCHANGE - 328)) | (1L << (EXECUTE - 328)) | (1L << (FILE - 328)) | (1L << (FIXED - 328)) | (1L << (FOLLOWING - 328)) | (1L << (GLOBAL - 328)) | (1L << (HASH - 328)) | (1L << (IMPORT_ - 328)) | (1L << (LESS - 328)) | (1L << (MEMORY - 328)) | (1L << (NONE - 328)) | (1L << (PARSER - 328)) | (1L << (PARTIAL - 328)) | (1L << (PARTITIONING - 328)) | (1L << (PERSIST - 328)) | (1L << (PRECEDING - 328)) | (1L << (PROCESS - 328)) | (1L << (PROXY - 328)) | (1L << (QUICK - 328)) | (1L << (REBUILD - 328)) | (1L << (REDUNDANT - 328)) | (1L << (RELOAD - 328)) | (1L << (REMOVE - 328)) | (1L << (REORGANIZE - 328)) | (1L << (REPAIR - 328)) | (1L << (REVERSE - 328)))) != 0) || ((((_la - 392)) & ~0x3f) == 0 && ((1L << (_la - 392)) & ((1L << (SESSION - 392)) | (1L << (SHUTDOWN - 392)) | (1L << (SIMPLE - 392)) | (1L << (SLAVE - 392)) | (1L << (VISIBLE - 392)) | (1L << (INVISIBLE - 392)) | (1L << (ENFORCED - 392)) | (1L << (AGAINST - 392)) | (1L << (LANGUAGE - 392)) | (1L << (EXTENDED - 392)) | (1L << (EXPANSION - 392)) | (1L << (VARIANCE - 392)) | (1L << (MAX_ROWS - 392)) | (1L << (MIN_ROWS - 392)) | (1L << (SQL_BIG_RESULT - 392)) | (1L << (SQL_BUFFER_RESULT - 392)) | (1L << (SQL_CACHE - 392)) | (1L << (SQL_NO_CACHE - 392)) | (1L << (STATS_AUTO_RECALC - 392)) | (1L << (STATS_PERSISTENT - 392)) | (1L << (STATS_SAMPLE_PAGES - 392)) | (1L << (ROW_FORMAT - 392)) | (1L << (WEIGHT_STRING - 392)) | (1L << (COLUMN_FORMAT - 392)) | (1L << (INSERT_METHOD - 392)) | (1L << (KEY_BLOCK_SIZE - 392)) | (1L << (PACK_KEYS - 392)) | (1L << (PERSIST_ONLY - 392)) | (1L << (BIT_AND - 392)) | (1L << (BIT_OR - 392)) | (1L << (BIT_XOR - 392)) | (1L << (GROUP_CONCAT - 392)) | (1L << (JSON_ARRAYAGG - 392)) | (1L << (JSON_OBJECTAGG - 392)) | (1L << (STD - 392)) | (1L << (STDDEV_POP - 392)) | (1L << (STDDEV_SAMP - 392)) | (1L << (VAR_POP - 392)) | (1L << (VAR_SAMP - 392)) | (1L << (AUTO_INCREMENT - 392)) | (1L << (AVG_ROW_LENGTH - 392)) | (1L << (DELAY_KEY_WRITE - 392)))) != 0) || ((((_la - 468)) & ~0x3f) == 0 && ((1L << (_la - 468)) & ((1L << (ROTATE - 468)) | (1L << (MASTER - 468)) | (1L << (BINLOG - 468)) | (1L << (ERROR - 468)) | (1L << (SCHEDULE - 468)) | (1L << (COMPLETION - 468)) | (1L << (HOST - 468)) | (1L << (SOCKET - 468)) | (1L << (PORT - 468)) | (1L << (SERVER - 468)) | (1L << (WRAPPER - 468)) | (1L << (OPTIONS - 468)) | (1L << (OWNER - 468)) | (1L << (RETURNS - 468)) | (1L << (CONTAINS - 468)) | (1L << (SECURITY - 468)) | (1L << (INVOKER - 468)) | (1L << (TEMPTABLE - 468)) | (1L << (MERGE - 468)) | (1L << (UNDEFINED - 468)) | (1L << (DATAFILE - 468)) | (1L << (FILE_BLOCK_SIZE - 468)) | (1L << (EXTENT_SIZE - 468)) | (1L << (INITIAL_SIZE - 468)) | (1L << (AUTOEXTEND_SIZE - 468)) | (1L << (MAX_SIZE - 468)) | (1L << (NODEGROUP - 468)) | (1L << (WAIT - 468)) | (1L << (LOGFILE - 468)) | (1L << (UNDOFILE - 468)) | (1L << (UNDO_BUFFER_SIZE - 468)) | (1L << (REDO_BUFFER_SIZE - 468)) | (1L << (HANDLER - 468)) | (1L << (PREV - 468)) | (1L << (ORGANIZATION - 468)) | (1L << (DEFINITION - 468)) | (1L << (DESCRIPTION - 468)) | (1L << (REFERENCE - 468)) | (1L << (FOLLOWS - 468)) | (1L << (PRECEDES - 468)) | (1L << (IMPORT - 468)) | (1L << (CONCURRENT - 468)) | (1L << (XML - 468)) | (1L << (DUMPFILE - 468)) | (1L << (SHARE - 468)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TableNameContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public DbNameContext dbName() {
			return getRuleContext(DbNameContext.class,0);
		}
		public List<TerminalNode> DOT_() { return getTokens(LinglongSQLStatementParser.DOT_); }
		public TerminalNode DOT_(int i) {
			return getToken(LinglongSQLStatementParser.DOT_, i);
		}
		public RpNameContext rpName() {
			return getRuleContext(RpNameContext.class,0);
		}
		public TableNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterTableName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitTableName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitTableName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableNameContext tableName() throws RecognitionException {
		TableNameContext _localctx = new TableNameContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_tableName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(390);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				{
				{
				setState(381);
				dbName();
				setState(382);
				match(DOT_);
				}
				{
				setState(384);
				rpName();
				setState(385);
				match(DOT_);
				}
				}
				break;
			case 2:
				{
				{
				setState(387);
				rpName();
				setState(388);
				match(DOT_);
				}
				}
				break;
			}
			setState(392);
			name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DbNameContext extends ParserRuleContext {
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public DbNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dbName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterDbName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitDbName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitDbName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DbNameContext dbName() throws RecognitionException {
		DbNameContext _localctx = new DbNameContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_dbName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(394);
			identifier_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RpNameContext extends ParserRuleContext {
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public RpNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rpName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterRpName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitRpName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitRpName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RpNameContext rpName() throws RecognitionException {
		RpNameContext _localctx = new RpNameContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_rpName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(396);
			identifier_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColumnNameContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TerminalNode DOT_() { return getToken(LinglongSQLStatementParser.DOT_, 0); }
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public ColumnNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterColumnName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitColumnName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitColumnName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnNameContext columnName() throws RecognitionException {
		ColumnNameContext _localctx = new ColumnNameContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_columnName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(398);
			name();
			setState(401);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				{
				setState(399);
				match(DOT_);
				setState(400);
				columnName();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OwnerContext extends ParserRuleContext {
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public OwnerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_owner; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterOwner(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitOwner(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitOwner(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OwnerContext owner() throws RecognitionException {
		OwnerContext _localctx = new OwnerContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_owner);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(403);
			identifier_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NameContext extends ParserRuleContext {
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public NameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NameContext name() throws RecognitionException {
		NameContext _localctx = new NameContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(405);
			identifier_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColumnNamesContext extends ParserRuleContext {
		public List<ColumnNameContext> columnName() {
			return getRuleContexts(ColumnNameContext.class);
		}
		public ColumnNameContext columnName(int i) {
			return getRuleContext(ColumnNameContext.class,i);
		}
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public ColumnNamesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnNames; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterColumnNames(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitColumnNames(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitColumnNames(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnNamesContext columnNames() throws RecognitionException {
		ColumnNamesContext _localctx = new ColumnNamesContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_columnNames);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(408);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LP_) {
				{
				setState(407);
				match(LP_);
				}
			}

			setState(410);
			columnName();
			setState(415);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,41,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(411);
					match(COMMA_);
					setState(412);
					columnName();
					}
					} 
				}
				setState(417);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,41,_ctx);
			}
			setState(419);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				{
				setState(418);
				match(RP_);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CharacterSetName_Context extends ParserRuleContext {
		public TerminalNode IDENTIFIER_() { return getToken(LinglongSQLStatementParser.IDENTIFIER_, 0); }
		public CharacterSetName_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterSetName_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterCharacterSetName_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitCharacterSetName_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitCharacterSetName_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CharacterSetName_Context characterSetName_() throws RecognitionException {
		CharacterSetName_Context _localctx = new CharacterSetName_Context(_ctx, getState());
		enterRule(_localctx, 74, RULE_characterSetName_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(421);
			match(IDENTIFIER_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprContext extends ParserRuleContext {
		public NotOperator_Context notOperator_() {
			return getRuleContext(NotOperator_Context.class,0);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public BooleanPrimary_Context booleanPrimary_() {
			return getRuleContext(BooleanPrimary_Context.class,0);
		}
		public LogicalOperatorContext logicalOperator() {
			return getRuleContext(LogicalOperatorContext.class,0);
		}
		public TerminalNode XOR() { return getToken(LinglongSQLStatementParser.XOR, 0); }
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 76;
		enterRecursionRule(_localctx, 76, RULE_expr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(432);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
			case 1:
				{
				setState(424);
				notOperator_();
				setState(425);
				expr(3);
				}
				break;
			case 2:
				{
				setState(427);
				match(LP_);
				setState(428);
				expr(0);
				setState(429);
				match(RP_);
				}
				break;
			case 3:
				{
				setState(431);
				booleanPrimary_(0);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(443);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,45,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(441);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
					case 1:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(434);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(435);
						logicalOperator();
						setState(436);
						expr(6);
						}
						break;
					case 2:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(438);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(439);
						match(XOR);
						setState(440);
						expr(5);
						}
						break;
					}
					} 
				}
				setState(445);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,45,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class LogicalOperatorContext extends ParserRuleContext {
		public TerminalNode OR() { return getToken(LinglongSQLStatementParser.OR, 0); }
		public TerminalNode OR_() { return getToken(LinglongSQLStatementParser.OR_, 0); }
		public TerminalNode AND() { return getToken(LinglongSQLStatementParser.AND, 0); }
		public TerminalNode AND_() { return getToken(LinglongSQLStatementParser.AND_, 0); }
		public LogicalOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterLogicalOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitLogicalOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitLogicalOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalOperatorContext logicalOperator() throws RecognitionException {
		LogicalOperatorContext _localctx = new LogicalOperatorContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_logicalOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(446);
			_la = _input.LA(1);
			if ( !(_la==AND_ || _la==OR_ || _la==AND || _la==OR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NotOperator_Context extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(LinglongSQLStatementParser.NOT, 0); }
		public TerminalNode NOT_() { return getToken(LinglongSQLStatementParser.NOT_, 0); }
		public NotOperator_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_notOperator_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterNotOperator_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitNotOperator_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitNotOperator_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NotOperator_Context notOperator_() throws RecognitionException {
		NotOperator_Context _localctx = new NotOperator_Context(_ctx, getState());
		enterRule(_localctx, 80, RULE_notOperator_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(448);
			_la = _input.LA(1);
			if ( !(_la==NOT_ || _la==NOT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanPrimary_Context extends ParserRuleContext {
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public BooleanPrimary_Context booleanPrimary_() {
			return getRuleContext(BooleanPrimary_Context.class,0);
		}
		public TerminalNode IS() { return getToken(LinglongSQLStatementParser.IS, 0); }
		public TerminalNode TRUE() { return getToken(LinglongSQLStatementParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(LinglongSQLStatementParser.FALSE, 0); }
		public TerminalNode UNKNOWN() { return getToken(LinglongSQLStatementParser.UNKNOWN, 0); }
		public TerminalNode NULL() { return getToken(LinglongSQLStatementParser.NULL, 0); }
		public TerminalNode NOT() { return getToken(LinglongSQLStatementParser.NOT, 0); }
		public TerminalNode SAFE_EQ_() { return getToken(LinglongSQLStatementParser.SAFE_EQ_, 0); }
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode ALL() { return getToken(LinglongSQLStatementParser.ALL, 0); }
		public TerminalNode ANY() { return getToken(LinglongSQLStatementParser.ANY, 0); }
		public BooleanPrimary_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanPrimary_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterBooleanPrimary_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitBooleanPrimary_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitBooleanPrimary_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanPrimary_Context booleanPrimary_() throws RecognitionException {
		return booleanPrimary_(0);
	}

	private BooleanPrimary_Context booleanPrimary_(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		BooleanPrimary_Context _localctx = new BooleanPrimary_Context(_ctx, _parentState);
		BooleanPrimary_Context _prevctx = _localctx;
		int _startState = 82;
		enterRecursionRule(_localctx, 82, RULE_booleanPrimary_, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(451);
			predicate();
			}
			_ctx.stop = _input.LT(-1);
			setState(473);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,48,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(471);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
					case 1:
						{
						_localctx = new BooleanPrimary_Context(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary_);
						setState(453);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(454);
						match(IS);
						setState(456);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(455);
							match(NOT);
							}
						}

						setState(458);
						_la = _input.LA(1);
						if ( !(((((_la - 101)) & ~0x3f) == 0 && ((1L << (_la - 101)) & ((1L << (NULL - 101)) | (1L << (TRUE - 101)) | (1L << (FALSE - 101)))) != 0) || _la==UNKNOWN) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					case 2:
						{
						_localctx = new BooleanPrimary_Context(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary_);
						setState(459);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(460);
						match(SAFE_EQ_);
						setState(461);
						predicate();
						}
						break;
					case 3:
						{
						_localctx = new BooleanPrimary_Context(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary_);
						setState(462);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(463);
						comparisonOperator();
						setState(464);
						predicate();
						}
						break;
					case 4:
						{
						_localctx = new BooleanPrimary_Context(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_booleanPrimary_);
						setState(466);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(467);
						comparisonOperator();
						setState(468);
						_la = _input.LA(1);
						if ( !(_la==ALL || _la==ANY) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(469);
						subquery();
						}
						break;
					}
					} 
				}
				setState(475);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,48,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class ComparisonOperatorContext extends ParserRuleContext {
		public TerminalNode EQ_() { return getToken(LinglongSQLStatementParser.EQ_, 0); }
		public TerminalNode GTE_() { return getToken(LinglongSQLStatementParser.GTE_, 0); }
		public TerminalNode GT_() { return getToken(LinglongSQLStatementParser.GT_, 0); }
		public TerminalNode LTE_() { return getToken(LinglongSQLStatementParser.LTE_, 0); }
		public TerminalNode LT_() { return getToken(LinglongSQLStatementParser.LT_, 0); }
		public TerminalNode NEQ_() { return getToken(LinglongSQLStatementParser.NEQ_, 0); }
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterComparisonOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitComparisonOperator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitComparisonOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(476);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQ_) | (1L << NEQ_) | (1L << GT_) | (1L << GTE_) | (1L << LT_) | (1L << LTE_))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PredicateContext extends ParserRuleContext {
		public List<BitExprContext> bitExpr() {
			return getRuleContexts(BitExprContext.class);
		}
		public BitExprContext bitExpr(int i) {
			return getRuleContext(BitExprContext.class,i);
		}
		public TerminalNode IN() { return getToken(LinglongSQLStatementParser.IN, 0); }
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode NOT() { return getToken(LinglongSQLStatementParser.NOT, 0); }
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public TerminalNode BETWEEN() { return getToken(LinglongSQLStatementParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(LinglongSQLStatementParser.AND, 0); }
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public TerminalNode SOUNDS() { return getToken(LinglongSQLStatementParser.SOUNDS, 0); }
		public TerminalNode LIKE() { return getToken(LinglongSQLStatementParser.LIKE, 0); }
		public List<SimpleExprContext> simpleExpr() {
			return getRuleContexts(SimpleExprContext.class);
		}
		public SimpleExprContext simpleExpr(int i) {
			return getRuleContext(SimpleExprContext.class,i);
		}
		public TerminalNode ESCAPE() { return getToken(LinglongSQLStatementParser.ESCAPE, 0); }
		public TerminalNode REGEXP() { return getToken(LinglongSQLStatementParser.REGEXP, 0); }
		public TerminalNode RLIKE() { return getToken(LinglongSQLStatementParser.RLIKE, 0); }
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitPredicate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_predicate);
		int _la;
		try {
			setState(533);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(478);
				bitExpr(0);
				setState(480);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(479);
					match(NOT);
					}
				}

				setState(482);
				match(IN);
				setState(483);
				subquery();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(485);
				bitExpr(0);
				setState(487);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(486);
					match(NOT);
					}
				}

				setState(489);
				match(IN);
				setState(490);
				match(LP_);
				setState(491);
				expr(0);
				setState(496);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(492);
					match(COMMA_);
					setState(493);
					expr(0);
					}
					}
					setState(498);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(499);
				match(RP_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(501);
				bitExpr(0);
				setState(503);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(502);
					match(NOT);
					}
				}

				setState(505);
				match(BETWEEN);
				setState(506);
				bitExpr(0);
				setState(507);
				match(AND);
				setState(508);
				predicate();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(510);
				bitExpr(0);
				setState(511);
				match(SOUNDS);
				setState(512);
				match(LIKE);
				setState(513);
				bitExpr(0);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(515);
				bitExpr(0);
				setState(517);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(516);
					match(NOT);
					}
				}

				setState(519);
				match(LIKE);
				setState(520);
				simpleExpr(0);
				setState(523);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
				case 1:
					{
					setState(521);
					match(ESCAPE);
					setState(522);
					simpleExpr(0);
					}
					break;
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(525);
				bitExpr(0);
				setState(527);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(526);
					match(NOT);
					}
				}

				setState(529);
				_la = _input.LA(1);
				if ( !(_la==REGEXP || _la==RLIKE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(530);
				bitExpr(0);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(532);
				bitExpr(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BitExprContext extends ParserRuleContext {
		public SimpleExprContext simpleExpr() {
			return getRuleContext(SimpleExprContext.class,0);
		}
		public List<BitExprContext> bitExpr() {
			return getRuleContexts(BitExprContext.class);
		}
		public BitExprContext bitExpr(int i) {
			return getRuleContext(BitExprContext.class,i);
		}
		public TerminalNode VERTICAL_BAR_() { return getToken(LinglongSQLStatementParser.VERTICAL_BAR_, 0); }
		public TerminalNode AMPERSAND_() { return getToken(LinglongSQLStatementParser.AMPERSAND_, 0); }
		public TerminalNode SIGNED_LEFT_SHIFT_() { return getToken(LinglongSQLStatementParser.SIGNED_LEFT_SHIFT_, 0); }
		public TerminalNode SIGNED_RIGHT_SHIFT_() { return getToken(LinglongSQLStatementParser.SIGNED_RIGHT_SHIFT_, 0); }
		public TerminalNode PLUS_() { return getToken(LinglongSQLStatementParser.PLUS_, 0); }
		public TerminalNode MINUS_() { return getToken(LinglongSQLStatementParser.MINUS_, 0); }
		public TerminalNode ASTERISK_() { return getToken(LinglongSQLStatementParser.ASTERISK_, 0); }
		public TerminalNode SLASH_() { return getToken(LinglongSQLStatementParser.SLASH_, 0); }
		public TerminalNode DIV() { return getToken(LinglongSQLStatementParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(LinglongSQLStatementParser.MOD, 0); }
		public TerminalNode MOD_() { return getToken(LinglongSQLStatementParser.MOD_, 0); }
		public TerminalNode CARET_() { return getToken(LinglongSQLStatementParser.CARET_, 0); }
		public IntervalExpression_Context intervalExpression_() {
			return getRuleContext(IntervalExpression_Context.class,0);
		}
		public BitExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bitExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterBitExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitBitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitBitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BitExprContext bitExpr() throws RecognitionException {
		return bitExpr(0);
	}

	private BitExprContext bitExpr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		BitExprContext _localctx = new BitExprContext(_ctx, _parentState);
		BitExprContext _prevctx = _localctx;
		int _startState = 88;
		enterRecursionRule(_localctx, 88, RULE_bitExpr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(536);
			simpleExpr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(582);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,58,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(580);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
					case 1:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(538);
						if (!(precpred(_ctx, 15))) throw new FailedPredicateException(this, "precpred(_ctx, 15)");
						setState(539);
						match(VERTICAL_BAR_);
						setState(540);
						bitExpr(16);
						}
						break;
					case 2:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(541);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(542);
						match(AMPERSAND_);
						setState(543);
						bitExpr(15);
						}
						break;
					case 3:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(544);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(545);
						match(SIGNED_LEFT_SHIFT_);
						setState(546);
						bitExpr(14);
						}
						break;
					case 4:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(547);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(548);
						match(SIGNED_RIGHT_SHIFT_);
						setState(549);
						bitExpr(13);
						}
						break;
					case 5:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(550);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(551);
						match(PLUS_);
						setState(552);
						bitExpr(12);
						}
						break;
					case 6:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(553);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(554);
						match(MINUS_);
						setState(555);
						bitExpr(11);
						}
						break;
					case 7:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(556);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(557);
						match(ASTERISK_);
						setState(558);
						bitExpr(10);
						}
						break;
					case 8:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(559);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(560);
						match(SLASH_);
						setState(561);
						bitExpr(9);
						}
						break;
					case 9:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(562);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(563);
						match(DIV);
						setState(564);
						bitExpr(8);
						}
						break;
					case 10:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(565);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(566);
						match(MOD);
						setState(567);
						bitExpr(7);
						}
						break;
					case 11:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(568);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(569);
						match(MOD_);
						setState(570);
						bitExpr(6);
						}
						break;
					case 12:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(571);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(572);
						match(CARET_);
						setState(573);
						bitExpr(5);
						}
						break;
					case 13:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(574);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(575);
						match(PLUS_);
						setState(576);
						intervalExpression_();
						}
						break;
					case 14:
						{
						_localctx = new BitExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bitExpr);
						setState(577);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(578);
						match(MINUS_);
						setState(579);
						intervalExpression_();
						}
						break;
					}
					} 
				}
				setState(584);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,58,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class SimpleExprContext extends ParserRuleContext {
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public ParameterMarkerContext parameterMarker() {
			return getRuleContext(ParameterMarkerContext.class,0);
		}
		public LiteralsContext literals() {
			return getRuleContext(LiteralsContext.class,0);
		}
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public Variable_Context variable_() {
			return getRuleContext(Variable_Context.class,0);
		}
		public List<SimpleExprContext> simpleExpr() {
			return getRuleContexts(SimpleExprContext.class);
		}
		public SimpleExprContext simpleExpr(int i) {
			return getRuleContext(SimpleExprContext.class,i);
		}
		public TerminalNode PLUS_() { return getToken(LinglongSQLStatementParser.PLUS_, 0); }
		public TerminalNode MINUS_() { return getToken(LinglongSQLStatementParser.MINUS_, 0); }
		public TerminalNode TILDE_() { return getToken(LinglongSQLStatementParser.TILDE_, 0); }
		public TerminalNode NOT_() { return getToken(LinglongSQLStatementParser.NOT_, 0); }
		public TerminalNode BINARY() { return getToken(LinglongSQLStatementParser.BINARY, 0); }
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public TerminalNode ROW() { return getToken(LinglongSQLStatementParser.ROW, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public SubqueryContext subquery() {
			return getRuleContext(SubqueryContext.class,0);
		}
		public TerminalNode EXISTS() { return getToken(LinglongSQLStatementParser.EXISTS, 0); }
		public TerminalNode LBE_() { return getToken(LinglongSQLStatementParser.LBE_, 0); }
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public TerminalNode RBE_() { return getToken(LinglongSQLStatementParser.RBE_, 0); }
		public MatchExpression_Context matchExpression_() {
			return getRuleContext(MatchExpression_Context.class,0);
		}
		public CaseExpression_Context caseExpression_() {
			return getRuleContext(CaseExpression_Context.class,0);
		}
		public IntervalExpression_Context intervalExpression_() {
			return getRuleContext(IntervalExpression_Context.class,0);
		}
		public TerminalNode OR_() { return getToken(LinglongSQLStatementParser.OR_, 0); }
		public TerminalNode COLLATE() { return getToken(LinglongSQLStatementParser.COLLATE, 0); }
		public TerminalNode STRING_() { return getToken(LinglongSQLStatementParser.STRING_, 0); }
		public SimpleExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterSimpleExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitSimpleExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitSimpleExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SimpleExprContext simpleExpr() throws RecognitionException {
		return simpleExpr(0);
	}

	private SimpleExprContext simpleExpr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		SimpleExprContext _localctx = new SimpleExprContext(_ctx, _parentState);
		SimpleExprContext _prevctx = _localctx;
		int _startState = 90;
		enterRecursionRule(_localctx, 90, RULE_simpleExpr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(619);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,62,_ctx) ) {
			case 1:
				{
				setState(586);
				functionCall();
				}
				break;
			case 2:
				{
				setState(587);
				parameterMarker();
				}
				break;
			case 3:
				{
				setState(588);
				literals();
				}
				break;
			case 4:
				{
				setState(589);
				columnName();
				}
				break;
			case 5:
				{
				setState(590);
				variable_();
				}
				break;
			case 6:
				{
				setState(591);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NOT_) | (1L << TILDE_) | (1L << PLUS_) | (1L << MINUS_))) != 0) || _la==BINARY) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(592);
				simpleExpr(7);
				}
				break;
			case 7:
				{
				setState(594);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ROW) {
					{
					setState(593);
					match(ROW);
					}
				}

				setState(596);
				match(LP_);
				setState(597);
				expr(0);
				setState(602);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(598);
					match(COMMA_);
					setState(599);
					expr(0);
					}
					}
					setState(604);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(605);
				match(RP_);
				}
				break;
			case 8:
				{
				setState(608);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXISTS) {
					{
					setState(607);
					match(EXISTS);
					}
				}

				setState(610);
				subquery();
				}
				break;
			case 9:
				{
				setState(611);
				match(LBE_);
				setState(612);
				identifier_();
				setState(613);
				expr(0);
				setState(614);
				match(RBE_);
				}
				break;
			case 10:
				{
				setState(616);
				matchExpression_();
				}
				break;
			case 11:
				{
				setState(617);
				caseExpression_();
				}
				break;
			case 12:
				{
				setState(618);
				intervalExpression_();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(632);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(630);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,64,_ctx) ) {
					case 1:
						{
						_localctx = new SimpleExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_simpleExpr);
						setState(621);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(622);
						match(OR_);
						setState(623);
						simpleExpr(9);
						}
						break;
					case 2:
						{
						_localctx = new SimpleExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_simpleExpr);
						setState(624);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(625);
						match(COLLATE);
						setState(628);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case STRING_:
							{
							setState(626);
							match(STRING_);
							}
							break;
						case TRUNCATE:
						case POSITION:
						case VIEW:
						case ANY:
						case OFFSET:
						case BEGIN:
						case COMMIT:
						case ROLLBACK:
						case SAVEPOINT:
						case BOOLEAN:
						case DATE:
						case TIME:
						case TIMESTAMP:
						case YEAR:
						case QUARTER:
						case MONTH:
						case WEEK:
						case DAY:
						case HOUR:
						case MINUTE:
						case SECOND:
						case MICROSECOND:
						case MAX:
						case MIN:
						case SUM:
						case COUNT:
						case AVG:
						case CURRENT:
						case ENABLE:
						case DISABLE:
						case INSTANCE:
						case DO:
						case DEFINER:
						case CASCADED:
						case LOCAL:
						case CLOSE:
						case OPEN:
						case NEXT:
						case NAME:
						case END:
						case EVERY:
						case PASSWORD:
						case PRIVILEGES:
						case QUERY:
						case USER:
						case MODE:
						case STDDEV:
						case BOTTOM:
						case FIRST:
						case LAST:
						case TABLES:
						case TABLESPACE:
						case COLUMNS:
						case FIELDS:
						case INDEXES:
						case STATUS:
						case MODIFY:
						case VALUE:
						case DUPLICATE:
						case AFTER:
						case OJ:
						case ACCOUNT:
						case ROLE:
						case START:
						case TRANSACTION:
						case WITHOUT:
						case ESCAPE:
						case SUBPARTITION:
						case STORAGE:
						case SUPER:
						case TEMPORARY:
						case THAN:
						case UNBOUNDED:
						case UPGRADE:
						case VALIDATION:
						case ROLLUP:
						case SOUNDS:
						case UNKNOWN:
						case OFF:
						case ALWAYS:
						case COMMITTED:
						case LEVEL:
						case NO:
						case ACTION:
						case ALGORITHM:
						case AUTOCOMMIT:
						case BTREE:
						case CHAIN:
						case CHARSET:
						case CHECKSUM:
						case CIPHER:
						case CLIENT:
						case COALESCE:
						case COMMENT:
						case COMPACT:
						case COMPRESSED:
						case COMPRESSION:
						case CONNECTION:
						case CONSISTENT:
						case DATA:
						case DISCARD:
						case DISK:
						case ENCRYPTION:
						case ENGINE:
						case EVENT:
						case EXCHANGE:
						case EXECUTE:
						case FILE:
						case FIXED:
						case FOLLOWING:
						case GLOBAL:
						case HASH:
						case IMPORT_:
						case LESS:
						case MEMORY:
						case NONE:
						case PARSER:
						case PARTIAL:
						case PARTITIONING:
						case PERSIST:
						case PRECEDING:
						case PROCESS:
						case PROXY:
						case QUICK:
						case REBUILD:
						case REDUNDANT:
						case RELOAD:
						case REMOVE:
						case REORGANIZE:
						case REPAIR:
						case REVERSE:
						case SESSION:
						case SHUTDOWN:
						case SIMPLE:
						case SLAVE:
						case VISIBLE:
						case INVISIBLE:
						case ENFORCED:
						case AGAINST:
						case LANGUAGE:
						case EXTENDED:
						case EXPANSION:
						case VARIANCE:
						case MAX_ROWS:
						case MIN_ROWS:
						case SQL_BIG_RESULT:
						case SQL_BUFFER_RESULT:
						case SQL_CACHE:
						case SQL_NO_CACHE:
						case STATS_AUTO_RECALC:
						case STATS_PERSISTENT:
						case STATS_SAMPLE_PAGES:
						case ROW_FORMAT:
						case WEIGHT_STRING:
						case COLUMN_FORMAT:
						case INSERT_METHOD:
						case KEY_BLOCK_SIZE:
						case PACK_KEYS:
						case PERSIST_ONLY:
						case BIT_AND:
						case BIT_OR:
						case BIT_XOR:
						case GROUP_CONCAT:
						case JSON_ARRAYAGG:
						case JSON_OBJECTAGG:
						case STD:
						case STDDEV_POP:
						case STDDEV_SAMP:
						case VAR_POP:
						case VAR_SAMP:
						case AUTO_INCREMENT:
						case AVG_ROW_LENGTH:
						case DELAY_KEY_WRITE:
						case ROTATE:
						case MASTER:
						case BINLOG:
						case ERROR:
						case SCHEDULE:
						case COMPLETION:
						case HOST:
						case SOCKET:
						case PORT:
						case SERVER:
						case WRAPPER:
						case OPTIONS:
						case OWNER:
						case RETURNS:
						case CONTAINS:
						case SECURITY:
						case INVOKER:
						case TEMPTABLE:
						case MERGE:
						case UNDEFINED:
						case DATAFILE:
						case FILE_BLOCK_SIZE:
						case EXTENT_SIZE:
						case INITIAL_SIZE:
						case AUTOEXTEND_SIZE:
						case MAX_SIZE:
						case NODEGROUP:
						case WAIT:
						case LOGFILE:
						case UNDOFILE:
						case UNDO_BUFFER_SIZE:
						case REDO_BUFFER_SIZE:
						case HANDLER:
						case PREV:
						case ORGANIZATION:
						case DEFINITION:
						case DESCRIPTION:
						case REFERENCE:
						case FOLLOWS:
						case PRECEDES:
						case IMPORT:
						case CONCURRENT:
						case XML:
						case DUMPFILE:
						case SHARE:
						case IDENTIFIER_:
							{
							setState(627);
							identifier_();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						}
						break;
					}
					} 
				}
				setState(634);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class FunctionCallContext extends ParserRuleContext {
		public AggregationFunctionContext aggregationFunction() {
			return getRuleContext(AggregationFunctionContext.class,0);
		}
		public SelectorFunction_Context selectorFunction_() {
			return getRuleContext(SelectorFunction_Context.class,0);
		}
		public TransformationFunction_Context transformationFunction_() {
			return getRuleContext(TransformationFunction_Context.class,0);
		}
		public RegularFunction_Context regularFunction_() {
			return getRuleContext(RegularFunction_Context.class,0);
		}
		public FunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitFunctionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_functionCall);
		try {
			setState(639);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,66,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(635);
				aggregationFunction();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(636);
				selectorFunction_();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(637);
				transformationFunction_();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(638);
				regularFunction_();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AggregationFunctionContext extends ParserRuleContext {
		public AggregationFunctionName_Context aggregationFunctionName_() {
			return getRuleContext(AggregationFunctionName_Context.class,0);
		}
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public DistinctContext distinct() {
			return getRuleContext(DistinctContext.class,0);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ASTERISK_() { return getToken(LinglongSQLStatementParser.ASTERISK_, 0); }
		public OverClause_Context overClause_() {
			return getRuleContext(OverClause_Context.class,0);
		}
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public AggregationFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregationFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterAggregationFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitAggregationFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitAggregationFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregationFunctionContext aggregationFunction() throws RecognitionException {
		AggregationFunctionContext _localctx = new AggregationFunctionContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_aggregationFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(641);
			aggregationFunctionName_();
			setState(642);
			match(LP_);
			setState(644);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,67,_ctx) ) {
			case 1:
				{
				setState(643);
				distinct();
				}
				break;
			}
			setState(658);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
			case 1:
				{
				setState(646);
				expr(0);
				setState(651);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(647);
					match(COMMA_);
					setState(648);
					expr(0);
					}
					}
					setState(653);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				{
				setState(654);
				match(ASTERISK_);
				}
				break;
			case 3:
				{
				setState(655);
				match(ASTERISK_);
				{
				setState(656);
				match(COMMA_);
				setState(657);
				expr(0);
				}
				}
				break;
			}
			setState(660);
			match(RP_);
			setState(662);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
			case 1:
				{
				setState(661);
				overClause_();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AggregationFunctionName_Context extends ParserRuleContext {
		public TerminalNode COUNT() { return getToken(LinglongSQLStatementParser.COUNT, 0); }
		public TerminalNode DISTINCT() { return getToken(LinglongSQLStatementParser.DISTINCT, 0); }
		public TerminalNode INTEGRAL() { return getToken(LinglongSQLStatementParser.INTEGRAL, 0); }
		public TerminalNode MEAN() { return getToken(LinglongSQLStatementParser.MEAN, 0); }
		public TerminalNode MEDIAN() { return getToken(LinglongSQLStatementParser.MEDIAN, 0); }
		public TerminalNode MODE() { return getToken(LinglongSQLStatementParser.MODE, 0); }
		public TerminalNode SPREAD() { return getToken(LinglongSQLStatementParser.SPREAD, 0); }
		public TerminalNode STDDEV() { return getToken(LinglongSQLStatementParser.STDDEV, 0); }
		public TerminalNode SUM() { return getToken(LinglongSQLStatementParser.SUM, 0); }
		public AggregationFunctionName_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregationFunctionName_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterAggregationFunctionName_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitAggregationFunctionName_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitAggregationFunctionName_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregationFunctionName_Context aggregationFunctionName_() throws RecognitionException {
		AggregationFunctionName_Context _localctx = new AggregationFunctionName_Context(_ctx, getState());
		enterRule(_localctx, 96, RULE_aggregationFunctionName_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(664);
			_la = _input.LA(1);
			if ( !(_la==DISTINCT || ((((_la - 144)) & ~0x3f) == 0 && ((1L << (_la - 144)) & ((1L << (SUM - 144)) | (1L << (COUNT - 144)) | (1L << (INTEGRAL - 144)) | (1L << (MEAN - 144)) | (1L << (MEDIAN - 144)) | (1L << (MODE - 144)) | (1L << (SPREAD - 144)))) != 0) || _la==STDDEV) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectorFunction_Context extends ParserRuleContext {
		public SelectorFunctionName_Context selectorFunctionName_() {
			return getRuleContext(SelectorFunctionName_Context.class,0);
		}
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ASTERISK_() { return getToken(LinglongSQLStatementParser.ASTERISK_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public SelectorFunction_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectorFunction_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterSelectorFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitSelectorFunction_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitSelectorFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectorFunction_Context selectorFunction_() throws RecognitionException {
		SelectorFunction_Context _localctx = new SelectorFunction_Context(_ctx, getState());
		enterRule(_localctx, 98, RULE_selectorFunction_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(666);
			selectorFunctionName_();
			setState(667);
			match(LP_);
			setState(677);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT_:
			case TILDE_:
			case PLUS_:
			case MINUS_:
			case DOT_:
			case LP_:
			case LBE_:
			case QUESTION_:
			case AT_:
			case TRUNCATE:
			case POSITION:
			case VIEW:
			case DISTINCT:
			case CASE:
			case SUBSTRING:
			case IF:
			case NOT:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case ANY:
			case OFFSET:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case INTERVAL:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case YEAR:
			case QUARTER:
			case MONTH:
			case WEEK:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case INSTANCE:
			case DO:
			case DEFINER:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case OPEN:
			case NEXT:
			case NAME:
			case END:
			case EVERY:
			case PASSWORD:
			case PRIVILEGES:
			case QUERY:
			case USER:
			case INTEGRAL:
			case MEAN:
			case MEDIAN:
			case MODE:
			case SPREAD:
			case STDDEV:
			case BOTTOM:
			case FIRST:
			case LAST:
			case PERCENTILE:
			case SAMPLE:
			case TOP:
			case ABS:
			case ACOS:
			case ASIN:
			case ATAN:
			case ATAN2:
			case CEIL:
			case COS:
			case CUMULATIVE_SUM:
			case DERIVATIVE:
			case DIFFERENCE:
			case EXP:
			case FLOOR:
			case HISTOGRAM:
			case LN:
			case LOG:
			case LOG2:
			case LOG10:
			case MOVING_AVERAGE:
			case NON_NEGATIVE_DERIVATIVE:
			case NON_NEGATIVE_DIFFERENCE:
			case POW:
			case ROUND:
			case SIN:
			case SQRT:
			case TAN:
			case TABLES:
			case TABLESPACE:
			case COLUMNS:
			case FIELDS:
			case INDEXES:
			case STATUS:
			case REPLACE:
			case MODIFY:
			case VALUE:
			case DUPLICATE:
			case AFTER:
			case OJ:
			case ACCOUNT:
			case ROLE:
			case START:
			case TRANSACTION:
			case ROW:
			case WITHOUT:
			case BINARY:
			case ESCAPE:
			case SUBPARTITION:
			case STORAGE:
			case SUPER:
			case TEMPORARY:
			case THAN:
			case UNBOUNDED:
			case UPGRADE:
			case VALIDATION:
			case ROLLUP:
			case SOUNDS:
			case UNKNOWN:
			case OFF:
			case ALWAYS:
			case COMMITTED:
			case LEVEL:
			case NO:
			case ACTION:
			case ALGORITHM:
			case AUTOCOMMIT:
			case BTREE:
			case CHAIN:
			case CHARSET:
			case CHECKSUM:
			case CIPHER:
			case CLIENT:
			case COALESCE:
			case COMMENT:
			case COMPACT:
			case COMPRESSED:
			case COMPRESSION:
			case CONNECTION:
			case CONSISTENT:
			case DATA:
			case DISCARD:
			case DISK:
			case ENCRYPTION:
			case ENGINE:
			case EVENT:
			case EXCHANGE:
			case EXECUTE:
			case FILE:
			case FIXED:
			case FOLLOWING:
			case GLOBAL:
			case HASH:
			case IMPORT_:
			case LESS:
			case MATCH:
			case MEMORY:
			case NONE:
			case NOW:
			case PARSER:
			case PARTIAL:
			case PARTITIONING:
			case PERSIST:
			case PRECEDING:
			case PROCESS:
			case PROXY:
			case QUICK:
			case REBUILD:
			case REDUNDANT:
			case RELOAD:
			case REMOVE:
			case REORGANIZE:
			case REPAIR:
			case REVERSE:
			case SESSION:
			case SHUTDOWN:
			case SIMPLE:
			case SLAVE:
			case VISIBLE:
			case INVISIBLE:
			case ENFORCED:
			case AGAINST:
			case LANGUAGE:
			case EXTENDED:
			case EXPANSION:
			case VARIANCE:
			case MAX_ROWS:
			case MIN_ROWS:
			case SQL_BIG_RESULT:
			case SQL_BUFFER_RESULT:
			case SQL_CACHE:
			case SQL_NO_CACHE:
			case STATS_AUTO_RECALC:
			case STATS_PERSISTENT:
			case STATS_SAMPLE_PAGES:
			case ROW_FORMAT:
			case WEIGHT_STRING:
			case COLUMN_FORMAT:
			case INSERT_METHOD:
			case KEY_BLOCK_SIZE:
			case PACK_KEYS:
			case PERSIST_ONLY:
			case BIT_AND:
			case BIT_OR:
			case BIT_XOR:
			case GROUP_CONCAT:
			case JSON_ARRAYAGG:
			case JSON_OBJECTAGG:
			case STD:
			case STDDEV_POP:
			case STDDEV_SAMP:
			case VAR_POP:
			case VAR_SAMP:
			case AUTO_INCREMENT:
			case AVG_ROW_LENGTH:
			case DELAY_KEY_WRITE:
			case CURRENT_TIMESTAMP:
			case ROTATE:
			case MASTER:
			case BINLOG:
			case ERROR:
			case SCHEDULE:
			case COMPLETION:
			case HOST:
			case SOCKET:
			case PORT:
			case SERVER:
			case WRAPPER:
			case OPTIONS:
			case OWNER:
			case RETURNS:
			case CONTAINS:
			case SECURITY:
			case INVOKER:
			case TEMPTABLE:
			case MERGE:
			case UNDEFINED:
			case DATAFILE:
			case FILE_BLOCK_SIZE:
			case EXTENT_SIZE:
			case INITIAL_SIZE:
			case AUTOEXTEND_SIZE:
			case MAX_SIZE:
			case NODEGROUP:
			case WAIT:
			case LOGFILE:
			case UNDOFILE:
			case UNDO_BUFFER_SIZE:
			case REDO_BUFFER_SIZE:
			case HANDLER:
			case PREV:
			case ORGANIZATION:
			case DEFINITION:
			case DESCRIPTION:
			case REFERENCE:
			case FOLLOWS:
			case PRECEDES:
			case IMPORT:
			case CONCURRENT:
			case XML:
			case DUMPFILE:
			case SHARE:
			case IDENTIFIER_:
			case STRING_:
			case NUMBER_:
			case HEX_DIGIT_:
			case BIT_NUM_:
				{
				setState(668);
				expr(0);
				setState(673);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(669);
					match(COMMA_);
					setState(670);
					expr(0);
					}
					}
					setState(675);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case ASTERISK_:
				{
				setState(676);
				match(ASTERISK_);
				}
				break;
			case RP_:
				break;
			default:
				break;
			}
			setState(679);
			match(RP_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectorFunctionName_Context extends ParserRuleContext {
		public TerminalNode BOTTOM() { return getToken(LinglongSQLStatementParser.BOTTOM, 0); }
		public TerminalNode FIRST() { return getToken(LinglongSQLStatementParser.FIRST, 0); }
		public TerminalNode LAST() { return getToken(LinglongSQLStatementParser.LAST, 0); }
		public TerminalNode MAX() { return getToken(LinglongSQLStatementParser.MAX, 0); }
		public TerminalNode MIN() { return getToken(LinglongSQLStatementParser.MIN, 0); }
		public TerminalNode PERCENTILE() { return getToken(LinglongSQLStatementParser.PERCENTILE, 0); }
		public TerminalNode SAMPLE() { return getToken(LinglongSQLStatementParser.SAMPLE, 0); }
		public TerminalNode TOP() { return getToken(LinglongSQLStatementParser.TOP, 0); }
		public SelectorFunctionName_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectorFunctionName_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterSelectorFunctionName_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitSelectorFunctionName_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitSelectorFunctionName_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectorFunctionName_Context selectorFunctionName_() throws RecognitionException {
		SelectorFunctionName_Context _localctx = new SelectorFunctionName_Context(_ctx, getState());
		enterRule(_localctx, 100, RULE_selectorFunctionName_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(681);
			_la = _input.LA(1);
			if ( !(_la==MAX || _la==MIN || ((((_la - 209)) & ~0x3f) == 0 && ((1L << (_la - 209)) & ((1L << (BOTTOM - 209)) | (1L << (FIRST - 209)) | (1L << (LAST - 209)) | (1L << (PERCENTILE - 209)) | (1L << (SAMPLE - 209)) | (1L << (TOP - 209)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TransformationFunction_Context extends ParserRuleContext {
		public TransformationFunctionName_Context transformationFunctionName_() {
			return getRuleContext(TransformationFunctionName_Context.class,0);
		}
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ASTERISK_() { return getToken(LinglongSQLStatementParser.ASTERISK_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public TransformationFunction_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_transformationFunction_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterTransformationFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitTransformationFunction_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitTransformationFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransformationFunction_Context transformationFunction_() throws RecognitionException {
		TransformationFunction_Context _localctx = new TransformationFunction_Context(_ctx, getState());
		enterRule(_localctx, 102, RULE_transformationFunction_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(683);
			transformationFunctionName_();
			setState(684);
			match(LP_);
			setState(697);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,74,_ctx) ) {
			case 1:
				{
				setState(685);
				expr(0);
				setState(690);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(686);
					match(COMMA_);
					setState(687);
					expr(0);
					}
					}
					setState(692);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				{
				setState(693);
				match(ASTERISK_);
				}
				break;
			case 3:
				{
				setState(694);
				match(ASTERISK_);
				{
				setState(695);
				match(COMMA_);
				setState(696);
				expr(0);
				}
				}
				break;
			}
			setState(699);
			match(RP_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TransformationFunctionName_Context extends ParserRuleContext {
		public TerminalNode ABS() { return getToken(LinglongSQLStatementParser.ABS, 0); }
		public TerminalNode ACOS() { return getToken(LinglongSQLStatementParser.ACOS, 0); }
		public TerminalNode ASIN() { return getToken(LinglongSQLStatementParser.ASIN, 0); }
		public TerminalNode ATAN() { return getToken(LinglongSQLStatementParser.ATAN, 0); }
		public TerminalNode ATAN2() { return getToken(LinglongSQLStatementParser.ATAN2, 0); }
		public TerminalNode CEIL() { return getToken(LinglongSQLStatementParser.CEIL, 0); }
		public TerminalNode COS() { return getToken(LinglongSQLStatementParser.COS, 0); }
		public TerminalNode CUMULATIVE_SUM() { return getToken(LinglongSQLStatementParser.CUMULATIVE_SUM, 0); }
		public TerminalNode DERIVATIVE() { return getToken(LinglongSQLStatementParser.DERIVATIVE, 0); }
		public TerminalNode DIFFERENCE() { return getToken(LinglongSQLStatementParser.DIFFERENCE, 0); }
		public TerminalNode ELAPSED() { return getToken(LinglongSQLStatementParser.ELAPSED, 0); }
		public TerminalNode EXP() { return getToken(LinglongSQLStatementParser.EXP, 0); }
		public TerminalNode FLOOR() { return getToken(LinglongSQLStatementParser.FLOOR, 0); }
		public TerminalNode HISTOGRAM() { return getToken(LinglongSQLStatementParser.HISTOGRAM, 0); }
		public TerminalNode LN() { return getToken(LinglongSQLStatementParser.LN, 0); }
		public TerminalNode LOG() { return getToken(LinglongSQLStatementParser.LOG, 0); }
		public TerminalNode LOG2() { return getToken(LinglongSQLStatementParser.LOG2, 0); }
		public TerminalNode LOG10() { return getToken(LinglongSQLStatementParser.LOG10, 0); }
		public TerminalNode MOVING_AVERAGE() { return getToken(LinglongSQLStatementParser.MOVING_AVERAGE, 0); }
		public TerminalNode NON_NEGATIVE_DERIVATIVE() { return getToken(LinglongSQLStatementParser.NON_NEGATIVE_DERIVATIVE, 0); }
		public TerminalNode NON_NEGATIVE_DIFFERENCE() { return getToken(LinglongSQLStatementParser.NON_NEGATIVE_DIFFERENCE, 0); }
		public TerminalNode POW() { return getToken(LinglongSQLStatementParser.POW, 0); }
		public TerminalNode ROUND() { return getToken(LinglongSQLStatementParser.ROUND, 0); }
		public TerminalNode SIN() { return getToken(LinglongSQLStatementParser.SIN, 0); }
		public TerminalNode SQRT() { return getToken(LinglongSQLStatementParser.SQRT, 0); }
		public TerminalNode TAN() { return getToken(LinglongSQLStatementParser.TAN, 0); }
		public TransformationFunctionName_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_transformationFunctionName_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterTransformationFunctionName_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitTransformationFunctionName_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitTransformationFunctionName_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TransformationFunctionName_Context transformationFunctionName_() throws RecognitionException {
		TransformationFunctionName_Context _localctx = new TransformationFunctionName_Context(_ctx, getState());
		enterRule(_localctx, 104, RULE_transformationFunctionName_);
		try {
			setState(727);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABS:
				enterOuterAlt(_localctx, 1);
				{
				setState(701);
				match(ABS);
				}
				break;
			case ACOS:
				enterOuterAlt(_localctx, 2);
				{
				setState(702);
				match(ACOS);
				}
				break;
			case ASIN:
				enterOuterAlt(_localctx, 3);
				{
				setState(703);
				match(ASIN);
				}
				break;
			case ATAN:
				enterOuterAlt(_localctx, 4);
				{
				setState(704);
				match(ATAN);
				}
				break;
			case ATAN2:
				enterOuterAlt(_localctx, 5);
				{
				setState(705);
				match(ATAN2);
				}
				break;
			case CEIL:
				enterOuterAlt(_localctx, 6);
				{
				setState(706);
				match(CEIL);
				}
				break;
			case COS:
				enterOuterAlt(_localctx, 7);
				{
				setState(707);
				match(COS);
				}
				break;
			case CUMULATIVE_SUM:
				enterOuterAlt(_localctx, 8);
				{
				setState(708);
				match(CUMULATIVE_SUM);
				}
				break;
			case DERIVATIVE:
				enterOuterAlt(_localctx, 9);
				{
				setState(709);
				match(DERIVATIVE);
				}
				break;
			case DIFFERENCE:
				enterOuterAlt(_localctx, 10);
				{
				setState(710);
				match(DIFFERENCE);
				setState(711);
				match(ELAPSED);
				}
				break;
			case EXP:
				enterOuterAlt(_localctx, 11);
				{
				setState(712);
				match(EXP);
				}
				break;
			case FLOOR:
				enterOuterAlt(_localctx, 12);
				{
				setState(713);
				match(FLOOR);
				}
				break;
			case HISTOGRAM:
				enterOuterAlt(_localctx, 13);
				{
				setState(714);
				match(HISTOGRAM);
				}
				break;
			case LN:
				enterOuterAlt(_localctx, 14);
				{
				setState(715);
				match(LN);
				}
				break;
			case LOG:
				enterOuterAlt(_localctx, 15);
				{
				setState(716);
				match(LOG);
				}
				break;
			case LOG2:
				enterOuterAlt(_localctx, 16);
				{
				setState(717);
				match(LOG2);
				}
				break;
			case LOG10:
				enterOuterAlt(_localctx, 17);
				{
				setState(718);
				match(LOG10);
				}
				break;
			case MOVING_AVERAGE:
				enterOuterAlt(_localctx, 18);
				{
				setState(719);
				match(MOVING_AVERAGE);
				}
				break;
			case NON_NEGATIVE_DERIVATIVE:
				enterOuterAlt(_localctx, 19);
				{
				setState(720);
				match(NON_NEGATIVE_DERIVATIVE);
				}
				break;
			case NON_NEGATIVE_DIFFERENCE:
				enterOuterAlt(_localctx, 20);
				{
				setState(721);
				match(NON_NEGATIVE_DIFFERENCE);
				}
				break;
			case POW:
				enterOuterAlt(_localctx, 21);
				{
				setState(722);
				match(POW);
				}
				break;
			case ROUND:
				enterOuterAlt(_localctx, 22);
				{
				setState(723);
				match(ROUND);
				}
				break;
			case SIN:
				enterOuterAlt(_localctx, 23);
				{
				setState(724);
				match(SIN);
				}
				break;
			case SQRT:
				enterOuterAlt(_localctx, 24);
				{
				setState(725);
				match(SQRT);
				}
				break;
			case TAN:
				enterOuterAlt(_localctx, 25);
				{
				setState(726);
				match(TAN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DistinctContext extends ParserRuleContext {
		public TerminalNode DISTINCT() { return getToken(LinglongSQLStatementParser.DISTINCT, 0); }
		public DistinctContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_distinct; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterDistinct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitDistinct(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitDistinct(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DistinctContext distinct() throws RecognitionException {
		DistinctContext _localctx = new DistinctContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_distinct);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(729);
			match(DISTINCT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OverClause_Context extends ParserRuleContext {
		public TerminalNode OVER() { return getToken(LinglongSQLStatementParser.OVER, 0); }
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public WindowSpecification_Context windowSpecification_() {
			return getRuleContext(WindowSpecification_Context.class,0);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public OverClause_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_overClause_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterOverClause_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitOverClause_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitOverClause_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OverClause_Context overClause_() throws RecognitionException {
		OverClause_Context _localctx = new OverClause_Context(_ctx, getState());
		enterRule(_localctx, 108, RULE_overClause_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(731);
			match(OVER);
			setState(737);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LP_:
				{
				setState(732);
				match(LP_);
				setState(733);
				windowSpecification_();
				setState(734);
				match(RP_);
				}
				break;
			case TRUNCATE:
			case POSITION:
			case VIEW:
			case ANY:
			case OFFSET:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case YEAR:
			case QUARTER:
			case MONTH:
			case WEEK:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case INSTANCE:
			case DO:
			case DEFINER:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case OPEN:
			case NEXT:
			case NAME:
			case END:
			case EVERY:
			case PASSWORD:
			case PRIVILEGES:
			case QUERY:
			case USER:
			case MODE:
			case STDDEV:
			case BOTTOM:
			case FIRST:
			case LAST:
			case TABLES:
			case TABLESPACE:
			case COLUMNS:
			case FIELDS:
			case INDEXES:
			case STATUS:
			case MODIFY:
			case VALUE:
			case DUPLICATE:
			case AFTER:
			case OJ:
			case ACCOUNT:
			case ROLE:
			case START:
			case TRANSACTION:
			case WITHOUT:
			case ESCAPE:
			case SUBPARTITION:
			case STORAGE:
			case SUPER:
			case TEMPORARY:
			case THAN:
			case UNBOUNDED:
			case UPGRADE:
			case VALIDATION:
			case ROLLUP:
			case SOUNDS:
			case UNKNOWN:
			case OFF:
			case ALWAYS:
			case COMMITTED:
			case LEVEL:
			case NO:
			case ACTION:
			case ALGORITHM:
			case AUTOCOMMIT:
			case BTREE:
			case CHAIN:
			case CHARSET:
			case CHECKSUM:
			case CIPHER:
			case CLIENT:
			case COALESCE:
			case COMMENT:
			case COMPACT:
			case COMPRESSED:
			case COMPRESSION:
			case CONNECTION:
			case CONSISTENT:
			case DATA:
			case DISCARD:
			case DISK:
			case ENCRYPTION:
			case ENGINE:
			case EVENT:
			case EXCHANGE:
			case EXECUTE:
			case FILE:
			case FIXED:
			case FOLLOWING:
			case GLOBAL:
			case HASH:
			case IMPORT_:
			case LESS:
			case MEMORY:
			case NONE:
			case PARSER:
			case PARTIAL:
			case PARTITIONING:
			case PERSIST:
			case PRECEDING:
			case PROCESS:
			case PROXY:
			case QUICK:
			case REBUILD:
			case REDUNDANT:
			case RELOAD:
			case REMOVE:
			case REORGANIZE:
			case REPAIR:
			case REVERSE:
			case SESSION:
			case SHUTDOWN:
			case SIMPLE:
			case SLAVE:
			case VISIBLE:
			case INVISIBLE:
			case ENFORCED:
			case AGAINST:
			case LANGUAGE:
			case EXTENDED:
			case EXPANSION:
			case VARIANCE:
			case MAX_ROWS:
			case MIN_ROWS:
			case SQL_BIG_RESULT:
			case SQL_BUFFER_RESULT:
			case SQL_CACHE:
			case SQL_NO_CACHE:
			case STATS_AUTO_RECALC:
			case STATS_PERSISTENT:
			case STATS_SAMPLE_PAGES:
			case ROW_FORMAT:
			case WEIGHT_STRING:
			case COLUMN_FORMAT:
			case INSERT_METHOD:
			case KEY_BLOCK_SIZE:
			case PACK_KEYS:
			case PERSIST_ONLY:
			case BIT_AND:
			case BIT_OR:
			case BIT_XOR:
			case GROUP_CONCAT:
			case JSON_ARRAYAGG:
			case JSON_OBJECTAGG:
			case STD:
			case STDDEV_POP:
			case STDDEV_SAMP:
			case VAR_POP:
			case VAR_SAMP:
			case AUTO_INCREMENT:
			case AVG_ROW_LENGTH:
			case DELAY_KEY_WRITE:
			case ROTATE:
			case MASTER:
			case BINLOG:
			case ERROR:
			case SCHEDULE:
			case COMPLETION:
			case HOST:
			case SOCKET:
			case PORT:
			case SERVER:
			case WRAPPER:
			case OPTIONS:
			case OWNER:
			case RETURNS:
			case CONTAINS:
			case SECURITY:
			case INVOKER:
			case TEMPTABLE:
			case MERGE:
			case UNDEFINED:
			case DATAFILE:
			case FILE_BLOCK_SIZE:
			case EXTENT_SIZE:
			case INITIAL_SIZE:
			case AUTOEXTEND_SIZE:
			case MAX_SIZE:
			case NODEGROUP:
			case WAIT:
			case LOGFILE:
			case UNDOFILE:
			case UNDO_BUFFER_SIZE:
			case REDO_BUFFER_SIZE:
			case HANDLER:
			case PREV:
			case ORGANIZATION:
			case DEFINITION:
			case DESCRIPTION:
			case REFERENCE:
			case FOLLOWS:
			case PRECEDES:
			case IMPORT:
			case CONCURRENT:
			case XML:
			case DUMPFILE:
			case SHARE:
			case IDENTIFIER_:
				{
				setState(736);
				identifier_();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WindowSpecification_Context extends ParserRuleContext {
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public PartitionClause_Context partitionClause_() {
			return getRuleContext(PartitionClause_Context.class,0);
		}
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public FrameClause_Context frameClause_() {
			return getRuleContext(FrameClause_Context.class,0);
		}
		public WindowSpecification_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowSpecification_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterWindowSpecification_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitWindowSpecification_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitWindowSpecification_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowSpecification_Context windowSpecification_() throws RecognitionException {
		WindowSpecification_Context _localctx = new WindowSpecification_Context(_ctx, getState());
		enterRule(_localctx, 110, RULE_windowSpecification_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(740);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TRUNCATE || _la==POSITION || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & ((1L << (VIEW - 68)) | (1L << (ANY - 68)) | (1L << (OFFSET - 68)) | (1L << (BEGIN - 68)) | (1L << (COMMIT - 68)) | (1L << (ROLLBACK - 68)) | (1L << (SAVEPOINT - 68)) | (1L << (BOOLEAN - 68)) | (1L << (DATE - 68)) | (1L << (TIME - 68)) | (1L << (TIMESTAMP - 68)))) != 0) || ((((_la - 133)) & ~0x3f) == 0 && ((1L << (_la - 133)) & ((1L << (YEAR - 133)) | (1L << (QUARTER - 133)) | (1L << (MONTH - 133)) | (1L << (WEEK - 133)) | (1L << (DAY - 133)) | (1L << (HOUR - 133)) | (1L << (MINUTE - 133)) | (1L << (SECOND - 133)) | (1L << (MICROSECOND - 133)) | (1L << (MAX - 133)) | (1L << (MIN - 133)) | (1L << (SUM - 133)) | (1L << (COUNT - 133)) | (1L << (AVG - 133)) | (1L << (CURRENT - 133)) | (1L << (ENABLE - 133)) | (1L << (DISABLE - 133)) | (1L << (INSTANCE - 133)) | (1L << (DO - 133)) | (1L << (DEFINER - 133)) | (1L << (CASCADED - 133)) | (1L << (LOCAL - 133)) | (1L << (CLOSE - 133)) | (1L << (OPEN - 133)) | (1L << (NEXT - 133)) | (1L << (NAME - 133)) | (1L << (END - 133)) | (1L << (EVERY - 133)) | (1L << (PASSWORD - 133)) | (1L << (PRIVILEGES - 133)) | (1L << (QUERY - 133)))) != 0) || ((((_la - 200)) & ~0x3f) == 0 && ((1L << (_la - 200)) & ((1L << (USER - 200)) | (1L << (MODE - 200)) | (1L << (STDDEV - 200)) | (1L << (BOTTOM - 200)) | (1L << (FIRST - 200)) | (1L << (LAST - 200)) | (1L << (TABLES - 200)) | (1L << (TABLESPACE - 200)) | (1L << (COLUMNS - 200)) | (1L << (FIELDS - 200)) | (1L << (INDEXES - 200)) | (1L << (STATUS - 200)) | (1L << (MODIFY - 200)) | (1L << (VALUE - 200)) | (1L << (DUPLICATE - 200)) | (1L << (AFTER - 200)))) != 0) || ((((_la - 264)) & ~0x3f) == 0 && ((1L << (_la - 264)) & ((1L << (OJ - 264)) | (1L << (ACCOUNT - 264)) | (1L << (ROLE - 264)) | (1L << (START - 264)) | (1L << (TRANSACTION - 264)) | (1L << (WITHOUT - 264)) | (1L << (ESCAPE - 264)) | (1L << (SUBPARTITION - 264)) | (1L << (STORAGE - 264)) | (1L << (SUPER - 264)) | (1L << (TEMPORARY - 264)) | (1L << (THAN - 264)) | (1L << (UNBOUNDED - 264)) | (1L << (UPGRADE - 264)) | (1L << (VALIDATION - 264)) | (1L << (ROLLUP - 264)) | (1L << (SOUNDS - 264)) | (1L << (UNKNOWN - 264)) | (1L << (OFF - 264)) | (1L << (ALWAYS - 264)) | (1L << (COMMITTED - 264)) | (1L << (LEVEL - 264)) | (1L << (NO - 264)) | (1L << (ACTION - 264)) | (1L << (ALGORITHM - 264)) | (1L << (AUTOCOMMIT - 264)) | (1L << (BTREE - 264)) | (1L << (CHAIN - 264)) | (1L << (CHARSET - 264)) | (1L << (CHECKSUM - 264)) | (1L << (CIPHER - 264)) | (1L << (CLIENT - 264)) | (1L << (COALESCE - 264)) | (1L << (COMMENT - 264)) | (1L << (COMPACT - 264)) | (1L << (COMPRESSED - 264)) | (1L << (COMPRESSION - 264)))) != 0) || ((((_la - 328)) & ~0x3f) == 0 && ((1L << (_la - 328)) & ((1L << (CONNECTION - 328)) | (1L << (CONSISTENT - 328)) | (1L << (DATA - 328)) | (1L << (DISCARD - 328)) | (1L << (DISK - 328)) | (1L << (ENCRYPTION - 328)) | (1L << (ENGINE - 328)) | (1L << (EVENT - 328)) | (1L << (EXCHANGE - 328)) | (1L << (EXECUTE - 328)) | (1L << (FILE - 328)) | (1L << (FIXED - 328)) | (1L << (FOLLOWING - 328)) | (1L << (GLOBAL - 328)) | (1L << (HASH - 328)) | (1L << (IMPORT_ - 328)) | (1L << (LESS - 328)) | (1L << (MEMORY - 328)) | (1L << (NONE - 328)) | (1L << (PARSER - 328)) | (1L << (PARTIAL - 328)) | (1L << (PARTITIONING - 328)) | (1L << (PERSIST - 328)) | (1L << (PRECEDING - 328)) | (1L << (PROCESS - 328)) | (1L << (PROXY - 328)) | (1L << (QUICK - 328)) | (1L << (REBUILD - 328)) | (1L << (REDUNDANT - 328)) | (1L << (RELOAD - 328)) | (1L << (REMOVE - 328)) | (1L << (REORGANIZE - 328)) | (1L << (REPAIR - 328)) | (1L << (REVERSE - 328)))) != 0) || ((((_la - 392)) & ~0x3f) == 0 && ((1L << (_la - 392)) & ((1L << (SESSION - 392)) | (1L << (SHUTDOWN - 392)) | (1L << (SIMPLE - 392)) | (1L << (SLAVE - 392)) | (1L << (VISIBLE - 392)) | (1L << (INVISIBLE - 392)) | (1L << (ENFORCED - 392)) | (1L << (AGAINST - 392)) | (1L << (LANGUAGE - 392)) | (1L << (EXTENDED - 392)) | (1L << (EXPANSION - 392)) | (1L << (VARIANCE - 392)) | (1L << (MAX_ROWS - 392)) | (1L << (MIN_ROWS - 392)) | (1L << (SQL_BIG_RESULT - 392)) | (1L << (SQL_BUFFER_RESULT - 392)) | (1L << (SQL_CACHE - 392)) | (1L << (SQL_NO_CACHE - 392)) | (1L << (STATS_AUTO_RECALC - 392)) | (1L << (STATS_PERSISTENT - 392)) | (1L << (STATS_SAMPLE_PAGES - 392)) | (1L << (ROW_FORMAT - 392)) | (1L << (WEIGHT_STRING - 392)) | (1L << (COLUMN_FORMAT - 392)) | (1L << (INSERT_METHOD - 392)) | (1L << (KEY_BLOCK_SIZE - 392)) | (1L << (PACK_KEYS - 392)) | (1L << (PERSIST_ONLY - 392)) | (1L << (BIT_AND - 392)) | (1L << (BIT_OR - 392)) | (1L << (BIT_XOR - 392)) | (1L << (GROUP_CONCAT - 392)) | (1L << (JSON_ARRAYAGG - 392)) | (1L << (JSON_OBJECTAGG - 392)) | (1L << (STD - 392)) | (1L << (STDDEV_POP - 392)) | (1L << (STDDEV_SAMP - 392)) | (1L << (VAR_POP - 392)) | (1L << (VAR_SAMP - 392)) | (1L << (AUTO_INCREMENT - 392)) | (1L << (AVG_ROW_LENGTH - 392)) | (1L << (DELAY_KEY_WRITE - 392)))) != 0) || ((((_la - 468)) & ~0x3f) == 0 && ((1L << (_la - 468)) & ((1L << (ROTATE - 468)) | (1L << (MASTER - 468)) | (1L << (BINLOG - 468)) | (1L << (ERROR - 468)) | (1L << (SCHEDULE - 468)) | (1L << (COMPLETION - 468)) | (1L << (HOST - 468)) | (1L << (SOCKET - 468)) | (1L << (PORT - 468)) | (1L << (SERVER - 468)) | (1L << (WRAPPER - 468)) | (1L << (OPTIONS - 468)) | (1L << (OWNER - 468)) | (1L << (RETURNS - 468)) | (1L << (CONTAINS - 468)) | (1L << (SECURITY - 468)) | (1L << (INVOKER - 468)) | (1L << (TEMPTABLE - 468)) | (1L << (MERGE - 468)) | (1L << (UNDEFINED - 468)) | (1L << (DATAFILE - 468)) | (1L << (FILE_BLOCK_SIZE - 468)) | (1L << (EXTENT_SIZE - 468)) | (1L << (INITIAL_SIZE - 468)) | (1L << (AUTOEXTEND_SIZE - 468)) | (1L << (MAX_SIZE - 468)) | (1L << (NODEGROUP - 468)) | (1L << (WAIT - 468)) | (1L << (LOGFILE - 468)) | (1L << (UNDOFILE - 468)) | (1L << (UNDO_BUFFER_SIZE - 468)) | (1L << (REDO_BUFFER_SIZE - 468)) | (1L << (HANDLER - 468)) | (1L << (PREV - 468)) | (1L << (ORGANIZATION - 468)) | (1L << (DEFINITION - 468)) | (1L << (DESCRIPTION - 468)) | (1L << (REFERENCE - 468)) | (1L << (FOLLOWS - 468)) | (1L << (PRECEDES - 468)) | (1L << (IMPORT - 468)) | (1L << (CONCURRENT - 468)) | (1L << (XML - 468)) | (1L << (DUMPFILE - 468)) | (1L << (SHARE - 468)) | (1L << (IDENTIFIER_ - 468)))) != 0)) {
				{
				setState(739);
				identifier_();
				}
			}

			setState(743);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PARTITION) {
				{
				setState(742);
				partitionClause_();
				}
			}

			setState(746);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(745);
				orderByClause();
				}
			}

			setState(749);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ROWS || _la==RANGE) {
				{
				setState(748);
				frameClause_();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PartitionClause_Context extends ParserRuleContext {
		public TerminalNode PARTITION() { return getToken(LinglongSQLStatementParser.PARTITION, 0); }
		public TerminalNode BY() { return getToken(LinglongSQLStatementParser.BY, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public PartitionClause_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partitionClause_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterPartitionClause_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitPartitionClause_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitPartitionClause_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PartitionClause_Context partitionClause_() throws RecognitionException {
		PartitionClause_Context _localctx = new PartitionClause_Context(_ctx, getState());
		enterRule(_localctx, 112, RULE_partitionClause_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(751);
			match(PARTITION);
			setState(752);
			match(BY);
			setState(753);
			expr(0);
			setState(758);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(754);
				match(COMMA_);
				setState(755);
				expr(0);
				}
				}
				setState(760);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FrameClause_Context extends ParserRuleContext {
		public TerminalNode ROWS() { return getToken(LinglongSQLStatementParser.ROWS, 0); }
		public TerminalNode RANGE() { return getToken(LinglongSQLStatementParser.RANGE, 0); }
		public FrameStart_Context frameStart_() {
			return getRuleContext(FrameStart_Context.class,0);
		}
		public FrameBetween_Context frameBetween_() {
			return getRuleContext(FrameBetween_Context.class,0);
		}
		public FrameClause_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frameClause_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterFrameClause_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitFrameClause_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitFrameClause_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FrameClause_Context frameClause_() throws RecognitionException {
		FrameClause_Context _localctx = new FrameClause_Context(_ctx, getState());
		enterRule(_localctx, 114, RULE_frameClause_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(761);
			_la = _input.LA(1);
			if ( !(_la==ROWS || _la==RANGE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(764);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT_:
			case TILDE_:
			case PLUS_:
			case MINUS_:
			case DOT_:
			case LP_:
			case LBE_:
			case QUESTION_:
			case AT_:
			case TRUNCATE:
			case POSITION:
			case VIEW:
			case DISTINCT:
			case CASE:
			case SUBSTRING:
			case IF:
			case NOT:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case ANY:
			case OFFSET:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case INTERVAL:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case YEAR:
			case QUARTER:
			case MONTH:
			case WEEK:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case INSTANCE:
			case DO:
			case DEFINER:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case OPEN:
			case NEXT:
			case NAME:
			case END:
			case EVERY:
			case PASSWORD:
			case PRIVILEGES:
			case QUERY:
			case USER:
			case INTEGRAL:
			case MEAN:
			case MEDIAN:
			case MODE:
			case SPREAD:
			case STDDEV:
			case BOTTOM:
			case FIRST:
			case LAST:
			case PERCENTILE:
			case SAMPLE:
			case TOP:
			case ABS:
			case ACOS:
			case ASIN:
			case ATAN:
			case ATAN2:
			case CEIL:
			case COS:
			case CUMULATIVE_SUM:
			case DERIVATIVE:
			case DIFFERENCE:
			case EXP:
			case FLOOR:
			case HISTOGRAM:
			case LN:
			case LOG:
			case LOG2:
			case LOG10:
			case MOVING_AVERAGE:
			case NON_NEGATIVE_DERIVATIVE:
			case NON_NEGATIVE_DIFFERENCE:
			case POW:
			case ROUND:
			case SIN:
			case SQRT:
			case TAN:
			case TABLES:
			case TABLESPACE:
			case COLUMNS:
			case FIELDS:
			case INDEXES:
			case STATUS:
			case REPLACE:
			case MODIFY:
			case VALUE:
			case DUPLICATE:
			case AFTER:
			case OJ:
			case ACCOUNT:
			case ROLE:
			case START:
			case TRANSACTION:
			case ROW:
			case WITHOUT:
			case BINARY:
			case ESCAPE:
			case SUBPARTITION:
			case STORAGE:
			case SUPER:
			case TEMPORARY:
			case THAN:
			case UNBOUNDED:
			case UPGRADE:
			case VALIDATION:
			case ROLLUP:
			case SOUNDS:
			case UNKNOWN:
			case OFF:
			case ALWAYS:
			case COMMITTED:
			case LEVEL:
			case NO:
			case ACTION:
			case ALGORITHM:
			case AUTOCOMMIT:
			case BTREE:
			case CHAIN:
			case CHARSET:
			case CHECKSUM:
			case CIPHER:
			case CLIENT:
			case COALESCE:
			case COMMENT:
			case COMPACT:
			case COMPRESSED:
			case COMPRESSION:
			case CONNECTION:
			case CONSISTENT:
			case DATA:
			case DISCARD:
			case DISK:
			case ENCRYPTION:
			case ENGINE:
			case EVENT:
			case EXCHANGE:
			case EXECUTE:
			case FILE:
			case FIXED:
			case FOLLOWING:
			case GLOBAL:
			case HASH:
			case IMPORT_:
			case LESS:
			case MATCH:
			case MEMORY:
			case NONE:
			case NOW:
			case PARSER:
			case PARTIAL:
			case PARTITIONING:
			case PERSIST:
			case PRECEDING:
			case PROCESS:
			case PROXY:
			case QUICK:
			case REBUILD:
			case REDUNDANT:
			case RELOAD:
			case REMOVE:
			case REORGANIZE:
			case REPAIR:
			case REVERSE:
			case SESSION:
			case SHUTDOWN:
			case SIMPLE:
			case SLAVE:
			case VISIBLE:
			case INVISIBLE:
			case ENFORCED:
			case AGAINST:
			case LANGUAGE:
			case EXTENDED:
			case EXPANSION:
			case VARIANCE:
			case MAX_ROWS:
			case MIN_ROWS:
			case SQL_BIG_RESULT:
			case SQL_BUFFER_RESULT:
			case SQL_CACHE:
			case SQL_NO_CACHE:
			case STATS_AUTO_RECALC:
			case STATS_PERSISTENT:
			case STATS_SAMPLE_PAGES:
			case ROW_FORMAT:
			case WEIGHT_STRING:
			case COLUMN_FORMAT:
			case INSERT_METHOD:
			case KEY_BLOCK_SIZE:
			case PACK_KEYS:
			case PERSIST_ONLY:
			case BIT_AND:
			case BIT_OR:
			case BIT_XOR:
			case GROUP_CONCAT:
			case JSON_ARRAYAGG:
			case JSON_OBJECTAGG:
			case STD:
			case STDDEV_POP:
			case STDDEV_SAMP:
			case VAR_POP:
			case VAR_SAMP:
			case AUTO_INCREMENT:
			case AVG_ROW_LENGTH:
			case DELAY_KEY_WRITE:
			case CURRENT_TIMESTAMP:
			case ROTATE:
			case MASTER:
			case BINLOG:
			case ERROR:
			case SCHEDULE:
			case COMPLETION:
			case HOST:
			case SOCKET:
			case PORT:
			case SERVER:
			case WRAPPER:
			case OPTIONS:
			case OWNER:
			case RETURNS:
			case CONTAINS:
			case SECURITY:
			case INVOKER:
			case TEMPTABLE:
			case MERGE:
			case UNDEFINED:
			case DATAFILE:
			case FILE_BLOCK_SIZE:
			case EXTENT_SIZE:
			case INITIAL_SIZE:
			case AUTOEXTEND_SIZE:
			case MAX_SIZE:
			case NODEGROUP:
			case WAIT:
			case LOGFILE:
			case UNDOFILE:
			case UNDO_BUFFER_SIZE:
			case REDO_BUFFER_SIZE:
			case HANDLER:
			case PREV:
			case ORGANIZATION:
			case DEFINITION:
			case DESCRIPTION:
			case REFERENCE:
			case FOLLOWS:
			case PRECEDES:
			case IMPORT:
			case CONCURRENT:
			case XML:
			case DUMPFILE:
			case SHARE:
			case IDENTIFIER_:
			case STRING_:
			case NUMBER_:
			case HEX_DIGIT_:
			case BIT_NUM_:
				{
				setState(762);
				frameStart_();
				}
				break;
			case BETWEEN:
				{
				setState(763);
				frameBetween_();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FrameStart_Context extends ParserRuleContext {
		public TerminalNode CURRENT() { return getToken(LinglongSQLStatementParser.CURRENT, 0); }
		public TerminalNode ROW() { return getToken(LinglongSQLStatementParser.ROW, 0); }
		public TerminalNode UNBOUNDED() { return getToken(LinglongSQLStatementParser.UNBOUNDED, 0); }
		public TerminalNode PRECEDING() { return getToken(LinglongSQLStatementParser.PRECEDING, 0); }
		public TerminalNode FOLLOWING() { return getToken(LinglongSQLStatementParser.FOLLOWING, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public FrameStart_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frameStart_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterFrameStart_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitFrameStart_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitFrameStart_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FrameStart_Context frameStart_() throws RecognitionException {
		FrameStart_Context _localctx = new FrameStart_Context(_ctx, getState());
		enterRule(_localctx, 116, RULE_frameStart_);
		try {
			setState(778);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(766);
				match(CURRENT);
				setState(767);
				match(ROW);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(768);
				match(UNBOUNDED);
				setState(769);
				match(PRECEDING);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(770);
				match(UNBOUNDED);
				setState(771);
				match(FOLLOWING);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(772);
				expr(0);
				setState(773);
				match(PRECEDING);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(775);
				expr(0);
				setState(776);
				match(FOLLOWING);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FrameEnd_Context extends ParserRuleContext {
		public FrameStart_Context frameStart_() {
			return getRuleContext(FrameStart_Context.class,0);
		}
		public FrameEnd_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frameEnd_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterFrameEnd_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitFrameEnd_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitFrameEnd_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FrameEnd_Context frameEnd_() throws RecognitionException {
		FrameEnd_Context _localctx = new FrameEnd_Context(_ctx, getState());
		enterRule(_localctx, 118, RULE_frameEnd_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(780);
			frameStart_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FrameBetween_Context extends ParserRuleContext {
		public TerminalNode BETWEEN() { return getToken(LinglongSQLStatementParser.BETWEEN, 0); }
		public FrameStart_Context frameStart_() {
			return getRuleContext(FrameStart_Context.class,0);
		}
		public TerminalNode AND() { return getToken(LinglongSQLStatementParser.AND, 0); }
		public FrameEnd_Context frameEnd_() {
			return getRuleContext(FrameEnd_Context.class,0);
		}
		public FrameBetween_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frameBetween_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterFrameBetween_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitFrameBetween_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitFrameBetween_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FrameBetween_Context frameBetween_() throws RecognitionException {
		FrameBetween_Context _localctx = new FrameBetween_Context(_ctx, getState());
		enterRule(_localctx, 120, RULE_frameBetween_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(782);
			match(BETWEEN);
			setState(783);
			frameStart_();
			setState(784);
			match(AND);
			setState(785);
			frameEnd_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SpecialFunction_Context extends ParserRuleContext {
		public GroupConcatFunction_Context groupConcatFunction_() {
			return getRuleContext(GroupConcatFunction_Context.class,0);
		}
		public WindowFunction_Context windowFunction_() {
			return getRuleContext(WindowFunction_Context.class,0);
		}
		public CastFunction_Context castFunction_() {
			return getRuleContext(CastFunction_Context.class,0);
		}
		public ConvertFunction_Context convertFunction_() {
			return getRuleContext(ConvertFunction_Context.class,0);
		}
		public PositionFunction_Context positionFunction_() {
			return getRuleContext(PositionFunction_Context.class,0);
		}
		public SubstringFunction_Context substringFunction_() {
			return getRuleContext(SubstringFunction_Context.class,0);
		}
		public ExtractFunction_Context extractFunction_() {
			return getRuleContext(ExtractFunction_Context.class,0);
		}
		public CharFunction_Context charFunction_() {
			return getRuleContext(CharFunction_Context.class,0);
		}
		public TrimFunction_Context trimFunction_() {
			return getRuleContext(TrimFunction_Context.class,0);
		}
		public WeightStringFunction_Context weightStringFunction_() {
			return getRuleContext(WeightStringFunction_Context.class,0);
		}
		public SpecialFunction_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specialFunction_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterSpecialFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitSpecialFunction_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitSpecialFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SpecialFunction_Context specialFunction_() throws RecognitionException {
		SpecialFunction_Context _localctx = new SpecialFunction_Context(_ctx, getState());
		enterRule(_localctx, 122, RULE_specialFunction_);
		try {
			setState(797);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,84,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(787);
				groupConcatFunction_();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(788);
				windowFunction_();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(789);
				castFunction_();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(790);
				convertFunction_();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(791);
				positionFunction_();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(792);
				substringFunction_();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(793);
				extractFunction_();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(794);
				charFunction_();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(795);
				trimFunction_();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(796);
				weightStringFunction_();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GroupConcatFunction_Context extends ParserRuleContext {
		public TerminalNode GROUP_CONCAT() { return getToken(LinglongSQLStatementParser.GROUP_CONCAT, 0); }
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public DistinctContext distinct() {
			return getRuleContext(DistinctContext.class,0);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ASTERISK_() { return getToken(LinglongSQLStatementParser.ASTERISK_, 0); }
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public TerminalNode SEPARATOR() { return getToken(LinglongSQLStatementParser.SEPARATOR, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public GroupConcatFunction_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupConcatFunction_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterGroupConcatFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitGroupConcatFunction_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitGroupConcatFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupConcatFunction_Context groupConcatFunction_() throws RecognitionException {
		GroupConcatFunction_Context _localctx = new GroupConcatFunction_Context(_ctx, getState());
		enterRule(_localctx, 124, RULE_groupConcatFunction_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(799);
			match(GROUP_CONCAT);
			setState(800);
			match(LP_);
			setState(802);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,85,_ctx) ) {
			case 1:
				{
				setState(801);
				distinct();
				}
				break;
			}
			setState(813);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT_:
			case TILDE_:
			case PLUS_:
			case MINUS_:
			case DOT_:
			case LP_:
			case LBE_:
			case QUESTION_:
			case AT_:
			case TRUNCATE:
			case POSITION:
			case VIEW:
			case DISTINCT:
			case CASE:
			case SUBSTRING:
			case IF:
			case NOT:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case ANY:
			case OFFSET:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case INTERVAL:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case YEAR:
			case QUARTER:
			case MONTH:
			case WEEK:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case INSTANCE:
			case DO:
			case DEFINER:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case OPEN:
			case NEXT:
			case NAME:
			case END:
			case EVERY:
			case PASSWORD:
			case PRIVILEGES:
			case QUERY:
			case USER:
			case INTEGRAL:
			case MEAN:
			case MEDIAN:
			case MODE:
			case SPREAD:
			case STDDEV:
			case BOTTOM:
			case FIRST:
			case LAST:
			case PERCENTILE:
			case SAMPLE:
			case TOP:
			case ABS:
			case ACOS:
			case ASIN:
			case ATAN:
			case ATAN2:
			case CEIL:
			case COS:
			case CUMULATIVE_SUM:
			case DERIVATIVE:
			case DIFFERENCE:
			case EXP:
			case FLOOR:
			case HISTOGRAM:
			case LN:
			case LOG:
			case LOG2:
			case LOG10:
			case MOVING_AVERAGE:
			case NON_NEGATIVE_DERIVATIVE:
			case NON_NEGATIVE_DIFFERENCE:
			case POW:
			case ROUND:
			case SIN:
			case SQRT:
			case TAN:
			case TABLES:
			case TABLESPACE:
			case COLUMNS:
			case FIELDS:
			case INDEXES:
			case STATUS:
			case REPLACE:
			case MODIFY:
			case VALUE:
			case DUPLICATE:
			case AFTER:
			case OJ:
			case ACCOUNT:
			case ROLE:
			case START:
			case TRANSACTION:
			case ROW:
			case WITHOUT:
			case BINARY:
			case ESCAPE:
			case SUBPARTITION:
			case STORAGE:
			case SUPER:
			case TEMPORARY:
			case THAN:
			case UNBOUNDED:
			case UPGRADE:
			case VALIDATION:
			case ROLLUP:
			case SOUNDS:
			case UNKNOWN:
			case OFF:
			case ALWAYS:
			case COMMITTED:
			case LEVEL:
			case NO:
			case ACTION:
			case ALGORITHM:
			case AUTOCOMMIT:
			case BTREE:
			case CHAIN:
			case CHARSET:
			case CHECKSUM:
			case CIPHER:
			case CLIENT:
			case COALESCE:
			case COMMENT:
			case COMPACT:
			case COMPRESSED:
			case COMPRESSION:
			case CONNECTION:
			case CONSISTENT:
			case DATA:
			case DISCARD:
			case DISK:
			case ENCRYPTION:
			case ENGINE:
			case EVENT:
			case EXCHANGE:
			case EXECUTE:
			case FILE:
			case FIXED:
			case FOLLOWING:
			case GLOBAL:
			case HASH:
			case IMPORT_:
			case LESS:
			case MATCH:
			case MEMORY:
			case NONE:
			case NOW:
			case PARSER:
			case PARTIAL:
			case PARTITIONING:
			case PERSIST:
			case PRECEDING:
			case PROCESS:
			case PROXY:
			case QUICK:
			case REBUILD:
			case REDUNDANT:
			case RELOAD:
			case REMOVE:
			case REORGANIZE:
			case REPAIR:
			case REVERSE:
			case SESSION:
			case SHUTDOWN:
			case SIMPLE:
			case SLAVE:
			case VISIBLE:
			case INVISIBLE:
			case ENFORCED:
			case AGAINST:
			case LANGUAGE:
			case EXTENDED:
			case EXPANSION:
			case VARIANCE:
			case MAX_ROWS:
			case MIN_ROWS:
			case SQL_BIG_RESULT:
			case SQL_BUFFER_RESULT:
			case SQL_CACHE:
			case SQL_NO_CACHE:
			case STATS_AUTO_RECALC:
			case STATS_PERSISTENT:
			case STATS_SAMPLE_PAGES:
			case ROW_FORMAT:
			case WEIGHT_STRING:
			case COLUMN_FORMAT:
			case INSERT_METHOD:
			case KEY_BLOCK_SIZE:
			case PACK_KEYS:
			case PERSIST_ONLY:
			case BIT_AND:
			case BIT_OR:
			case BIT_XOR:
			case GROUP_CONCAT:
			case JSON_ARRAYAGG:
			case JSON_OBJECTAGG:
			case STD:
			case STDDEV_POP:
			case STDDEV_SAMP:
			case VAR_POP:
			case VAR_SAMP:
			case AUTO_INCREMENT:
			case AVG_ROW_LENGTH:
			case DELAY_KEY_WRITE:
			case CURRENT_TIMESTAMP:
			case ROTATE:
			case MASTER:
			case BINLOG:
			case ERROR:
			case SCHEDULE:
			case COMPLETION:
			case HOST:
			case SOCKET:
			case PORT:
			case SERVER:
			case WRAPPER:
			case OPTIONS:
			case OWNER:
			case RETURNS:
			case CONTAINS:
			case SECURITY:
			case INVOKER:
			case TEMPTABLE:
			case MERGE:
			case UNDEFINED:
			case DATAFILE:
			case FILE_BLOCK_SIZE:
			case EXTENT_SIZE:
			case INITIAL_SIZE:
			case AUTOEXTEND_SIZE:
			case MAX_SIZE:
			case NODEGROUP:
			case WAIT:
			case LOGFILE:
			case UNDOFILE:
			case UNDO_BUFFER_SIZE:
			case REDO_BUFFER_SIZE:
			case HANDLER:
			case PREV:
			case ORGANIZATION:
			case DEFINITION:
			case DESCRIPTION:
			case REFERENCE:
			case FOLLOWS:
			case PRECEDES:
			case IMPORT:
			case CONCURRENT:
			case XML:
			case DUMPFILE:
			case SHARE:
			case IDENTIFIER_:
			case STRING_:
			case NUMBER_:
			case HEX_DIGIT_:
			case BIT_NUM_:
				{
				setState(804);
				expr(0);
				setState(809);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(805);
					match(COMMA_);
					setState(806);
					expr(0);
					}
					}
					setState(811);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case ASTERISK_:
				{
				setState(812);
				match(ASTERISK_);
				}
				break;
			case RP_:
			case ORDER:
			case SEPARATOR:
				break;
			default:
				break;
			}
			setState(816);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(815);
				orderByClause();
				}
			}

			setState(820);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEPARATOR) {
				{
				setState(818);
				match(SEPARATOR);
				setState(819);
				expr(0);
				}
			}

			setState(822);
			match(RP_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WindowFunction_Context extends ParserRuleContext {
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public OverClause_Context overClause_() {
			return getRuleContext(OverClause_Context.class,0);
		}
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public WindowFunction_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowFunction_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterWindowFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitWindowFunction_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitWindowFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowFunction_Context windowFunction_() throws RecognitionException {
		WindowFunction_Context _localctx = new WindowFunction_Context(_ctx, getState());
		enterRule(_localctx, 126, RULE_windowFunction_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(824);
			identifier_();
			setState(825);
			match(LP_);
			setState(826);
			expr(0);
			setState(831);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(827);
				match(COMMA_);
				setState(828);
				expr(0);
				}
				}
				setState(833);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(834);
			match(RP_);
			setState(835);
			overClause_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CastFunction_Context extends ParserRuleContext {
		public TerminalNode CAST() { return getToken(LinglongSQLStatementParser.CAST, 0); }
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode AS() { return getToken(LinglongSQLStatementParser.AS, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public CastFunction_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_castFunction_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterCastFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitCastFunction_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitCastFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CastFunction_Context castFunction_() throws RecognitionException {
		CastFunction_Context _localctx = new CastFunction_Context(_ctx, getState());
		enterRule(_localctx, 128, RULE_castFunction_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(837);
			match(CAST);
			setState(838);
			match(LP_);
			setState(839);
			expr(0);
			setState(840);
			match(AS);
			setState(841);
			dataType();
			setState(842);
			match(RP_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConvertFunction_Context extends ParserRuleContext {
		public TerminalNode CONVERT() { return getToken(LinglongSQLStatementParser.CONVERT, 0); }
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode COMMA_() { return getToken(LinglongSQLStatementParser.COMMA_, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public TerminalNode USING() { return getToken(LinglongSQLStatementParser.USING, 0); }
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public ConvertFunction_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_convertFunction_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterConvertFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitConvertFunction_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitConvertFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConvertFunction_Context convertFunction_() throws RecognitionException {
		ConvertFunction_Context _localctx = new ConvertFunction_Context(_ctx, getState());
		enterRule(_localctx, 130, RULE_convertFunction_);
		try {
			setState(858);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(844);
				match(CONVERT);
				setState(845);
				match(LP_);
				setState(846);
				expr(0);
				setState(847);
				match(COMMA_);
				setState(848);
				dataType();
				setState(849);
				match(RP_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(851);
				match(CONVERT);
				setState(852);
				match(LP_);
				setState(853);
				expr(0);
				setState(854);
				match(USING);
				setState(855);
				identifier_();
				setState(856);
				match(RP_);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PositionFunction_Context extends ParserRuleContext {
		public TerminalNode POSITION() { return getToken(LinglongSQLStatementParser.POSITION, 0); }
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode IN() { return getToken(LinglongSQLStatementParser.IN, 0); }
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public PositionFunction_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_positionFunction_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterPositionFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitPositionFunction_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitPositionFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PositionFunction_Context positionFunction_() throws RecognitionException {
		PositionFunction_Context _localctx = new PositionFunction_Context(_ctx, getState());
		enterRule(_localctx, 132, RULE_positionFunction_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(860);
			match(POSITION);
			setState(861);
			match(LP_);
			setState(862);
			expr(0);
			setState(863);
			match(IN);
			setState(864);
			expr(0);
			setState(865);
			match(RP_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SubstringFunction_Context extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode FROM() { return getToken(LinglongSQLStatementParser.FROM, 0); }
		public List<TerminalNode> NUMBER_() { return getTokens(LinglongSQLStatementParser.NUMBER_); }
		public TerminalNode NUMBER_(int i) {
			return getToken(LinglongSQLStatementParser.NUMBER_, i);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public TerminalNode SUBSTRING() { return getToken(LinglongSQLStatementParser.SUBSTRING, 0); }
		public TerminalNode SUBSTR() { return getToken(LinglongSQLStatementParser.SUBSTR, 0); }
		public TerminalNode FOR() { return getToken(LinglongSQLStatementParser.FOR, 0); }
		public SubstringFunction_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_substringFunction_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterSubstringFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitSubstringFunction_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitSubstringFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubstringFunction_Context substringFunction_() throws RecognitionException {
		SubstringFunction_Context _localctx = new SubstringFunction_Context(_ctx, getState());
		enterRule(_localctx, 134, RULE_substringFunction_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(867);
			_la = _input.LA(1);
			if ( !(_la==SUBSTRING || _la==SUBSTR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(868);
			match(LP_);
			setState(869);
			expr(0);
			setState(870);
			match(FROM);
			setState(871);
			match(NUMBER_);
			setState(874);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FOR) {
				{
				setState(872);
				match(FOR);
				setState(873);
				match(NUMBER_);
				}
			}

			setState(876);
			match(RP_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtractFunction_Context extends ParserRuleContext {
		public TerminalNode EXTRACT() { return getToken(LinglongSQLStatementParser.EXTRACT, 0); }
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public TerminalNode FROM() { return getToken(LinglongSQLStatementParser.FROM, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public ExtractFunction_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extractFunction_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterExtractFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitExtractFunction_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitExtractFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExtractFunction_Context extractFunction_() throws RecognitionException {
		ExtractFunction_Context _localctx = new ExtractFunction_Context(_ctx, getState());
		enterRule(_localctx, 136, RULE_extractFunction_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(878);
			match(EXTRACT);
			setState(879);
			match(LP_);
			setState(880);
			identifier_();
			setState(881);
			match(FROM);
			setState(882);
			expr(0);
			setState(883);
			match(RP_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CharFunction_Context extends ParserRuleContext {
		public TerminalNode CHAR() { return getToken(LinglongSQLStatementParser.CHAR, 0); }
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public TerminalNode USING() { return getToken(LinglongSQLStatementParser.USING, 0); }
		public IgnoredIdentifier_Context ignoredIdentifier_() {
			return getRuleContext(IgnoredIdentifier_Context.class,0);
		}
		public CharFunction_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_charFunction_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterCharFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitCharFunction_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitCharFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CharFunction_Context charFunction_() throws RecognitionException {
		CharFunction_Context _localctx = new CharFunction_Context(_ctx, getState());
		enterRule(_localctx, 138, RULE_charFunction_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(885);
			match(CHAR);
			setState(886);
			match(LP_);
			setState(887);
			expr(0);
			setState(892);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(888);
				match(COMMA_);
				setState(889);
				expr(0);
				}
				}
				setState(894);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(897);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USING) {
				{
				setState(895);
				match(USING);
				setState(896);
				ignoredIdentifier_();
				}
			}

			setState(899);
			match(RP_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TrimFunction_Context extends ParserRuleContext {
		public TerminalNode TRIM() { return getToken(LinglongSQLStatementParser.TRIM, 0); }
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public List<TerminalNode> STRING_() { return getTokens(LinglongSQLStatementParser.STRING_); }
		public TerminalNode STRING_(int i) {
			return getToken(LinglongSQLStatementParser.STRING_, i);
		}
		public TerminalNode FROM() { return getToken(LinglongSQLStatementParser.FROM, 0); }
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public TerminalNode LEADING() { return getToken(LinglongSQLStatementParser.LEADING, 0); }
		public TerminalNode BOTH() { return getToken(LinglongSQLStatementParser.BOTH, 0); }
		public TerminalNode TRAILING() { return getToken(LinglongSQLStatementParser.TRAILING, 0); }
		public TrimFunction_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trimFunction_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterTrimFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitTrimFunction_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitTrimFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TrimFunction_Context trimFunction_() throws RecognitionException {
		TrimFunction_Context _localctx = new TrimFunction_Context(_ctx, getState());
		enterRule(_localctx, 140, RULE_trimFunction_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(901);
			match(TRIM);
			setState(902);
			match(LP_);
			setState(903);
			_la = _input.LA(1);
			if ( !(_la==TRAILING || _la==BOTH || _la==LEADING) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(904);
			match(STRING_);
			setState(905);
			match(FROM);
			setState(906);
			match(STRING_);
			setState(907);
			match(RP_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WeightStringFunction_Context extends ParserRuleContext {
		public TerminalNode WEIGHT_STRING() { return getToken(LinglongSQLStatementParser.WEIGHT_STRING, 0); }
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public TerminalNode AS() { return getToken(LinglongSQLStatementParser.AS, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public LevelClause_Context levelClause_() {
			return getRuleContext(LevelClause_Context.class,0);
		}
		public WeightStringFunction_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_weightStringFunction_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterWeightStringFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitWeightStringFunction_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitWeightStringFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WeightStringFunction_Context weightStringFunction_() throws RecognitionException {
		WeightStringFunction_Context _localctx = new WeightStringFunction_Context(_ctx, getState());
		enterRule(_localctx, 142, RULE_weightStringFunction_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(909);
			match(WEIGHT_STRING);
			setState(910);
			match(LP_);
			setState(911);
			expr(0);
			setState(914);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(912);
				match(AS);
				setState(913);
				dataType();
				}
			}

			setState(917);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LEVEL) {
				{
				setState(916);
				levelClause_();
				}
			}

			setState(919);
			match(RP_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LevelClause_Context extends ParserRuleContext {
		public TerminalNode LEVEL() { return getToken(LinglongSQLStatementParser.LEVEL, 0); }
		public List<LevelInWeightListElement_Context> levelInWeightListElement_() {
			return getRuleContexts(LevelInWeightListElement_Context.class);
		}
		public LevelInWeightListElement_Context levelInWeightListElement_(int i) {
			return getRuleContext(LevelInWeightListElement_Context.class,i);
		}
		public List<TerminalNode> NUMBER_() { return getTokens(LinglongSQLStatementParser.NUMBER_); }
		public TerminalNode NUMBER_(int i) {
			return getToken(LinglongSQLStatementParser.NUMBER_, i);
		}
		public TerminalNode MINUS_() { return getToken(LinglongSQLStatementParser.MINUS_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public LevelClause_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_levelClause_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterLevelClause_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitLevelClause_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitLevelClause_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LevelClause_Context levelClause_() throws RecognitionException {
		LevelClause_Context _localctx = new LevelClause_Context(_ctx, getState());
		enterRule(_localctx, 144, RULE_levelClause_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(921);
			match(LEVEL);
			setState(933);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
			case 1:
				{
				setState(922);
				levelInWeightListElement_();
				setState(927);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(923);
					match(COMMA_);
					setState(924);
					levelInWeightListElement_();
					}
					}
					setState(929);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				{
				setState(930);
				match(NUMBER_);
				setState(931);
				match(MINUS_);
				setState(932);
				match(NUMBER_);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LevelInWeightListElement_Context extends ParserRuleContext {
		public TerminalNode NUMBER_() { return getToken(LinglongSQLStatementParser.NUMBER_, 0); }
		public TerminalNode REVERSE() { return getToken(LinglongSQLStatementParser.REVERSE, 0); }
		public TerminalNode ASC() { return getToken(LinglongSQLStatementParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(LinglongSQLStatementParser.DESC, 0); }
		public LevelInWeightListElement_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_levelInWeightListElement_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterLevelInWeightListElement_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitLevelInWeightListElement_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitLevelInWeightListElement_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LevelInWeightListElement_Context levelInWeightListElement_() throws RecognitionException {
		LevelInWeightListElement_Context _localctx = new LevelInWeightListElement_Context(_ctx, getState());
		enterRule(_localctx, 146, RULE_levelInWeightListElement_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(935);
			match(NUMBER_);
			setState(937);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(936);
				_la = _input.LA(1);
				if ( !(_la==ASC || _la==DESC) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(940);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==REVERSE) {
				{
				setState(939);
				match(REVERSE);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RegularFunction_Context extends ParserRuleContext {
		public RegularFunctionName_Context regularFunctionName_() {
			return getRuleContext(RegularFunctionName_Context.class,0);
		}
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ASTERISK_() { return getToken(LinglongSQLStatementParser.ASTERISK_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public RegularFunction_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_regularFunction_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterRegularFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitRegularFunction_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitRegularFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RegularFunction_Context regularFunction_() throws RecognitionException {
		RegularFunction_Context _localctx = new RegularFunction_Context(_ctx, getState());
		enterRule(_localctx, 148, RULE_regularFunction_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(942);
			regularFunctionName_();
			setState(943);
			match(LP_);
			setState(953);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT_:
			case TILDE_:
			case PLUS_:
			case MINUS_:
			case DOT_:
			case LP_:
			case LBE_:
			case QUESTION_:
			case AT_:
			case TRUNCATE:
			case POSITION:
			case VIEW:
			case DISTINCT:
			case CASE:
			case SUBSTRING:
			case IF:
			case NOT:
			case NULL:
			case TRUE:
			case FALSE:
			case EXISTS:
			case ANY:
			case OFFSET:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case INTERVAL:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case LOCALTIME:
			case LOCALTIMESTAMP:
			case YEAR:
			case QUARTER:
			case MONTH:
			case WEEK:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case INSTANCE:
			case DO:
			case DEFINER:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case OPEN:
			case NEXT:
			case NAME:
			case END:
			case EVERY:
			case PASSWORD:
			case PRIVILEGES:
			case QUERY:
			case USER:
			case INTEGRAL:
			case MEAN:
			case MEDIAN:
			case MODE:
			case SPREAD:
			case STDDEV:
			case BOTTOM:
			case FIRST:
			case LAST:
			case PERCENTILE:
			case SAMPLE:
			case TOP:
			case ABS:
			case ACOS:
			case ASIN:
			case ATAN:
			case ATAN2:
			case CEIL:
			case COS:
			case CUMULATIVE_SUM:
			case DERIVATIVE:
			case DIFFERENCE:
			case EXP:
			case FLOOR:
			case HISTOGRAM:
			case LN:
			case LOG:
			case LOG2:
			case LOG10:
			case MOVING_AVERAGE:
			case NON_NEGATIVE_DERIVATIVE:
			case NON_NEGATIVE_DIFFERENCE:
			case POW:
			case ROUND:
			case SIN:
			case SQRT:
			case TAN:
			case TABLES:
			case TABLESPACE:
			case COLUMNS:
			case FIELDS:
			case INDEXES:
			case STATUS:
			case REPLACE:
			case MODIFY:
			case VALUE:
			case DUPLICATE:
			case AFTER:
			case OJ:
			case ACCOUNT:
			case ROLE:
			case START:
			case TRANSACTION:
			case ROW:
			case WITHOUT:
			case BINARY:
			case ESCAPE:
			case SUBPARTITION:
			case STORAGE:
			case SUPER:
			case TEMPORARY:
			case THAN:
			case UNBOUNDED:
			case UPGRADE:
			case VALIDATION:
			case ROLLUP:
			case SOUNDS:
			case UNKNOWN:
			case OFF:
			case ALWAYS:
			case COMMITTED:
			case LEVEL:
			case NO:
			case ACTION:
			case ALGORITHM:
			case AUTOCOMMIT:
			case BTREE:
			case CHAIN:
			case CHARSET:
			case CHECKSUM:
			case CIPHER:
			case CLIENT:
			case COALESCE:
			case COMMENT:
			case COMPACT:
			case COMPRESSED:
			case COMPRESSION:
			case CONNECTION:
			case CONSISTENT:
			case DATA:
			case DISCARD:
			case DISK:
			case ENCRYPTION:
			case ENGINE:
			case EVENT:
			case EXCHANGE:
			case EXECUTE:
			case FILE:
			case FIXED:
			case FOLLOWING:
			case GLOBAL:
			case HASH:
			case IMPORT_:
			case LESS:
			case MATCH:
			case MEMORY:
			case NONE:
			case NOW:
			case PARSER:
			case PARTIAL:
			case PARTITIONING:
			case PERSIST:
			case PRECEDING:
			case PROCESS:
			case PROXY:
			case QUICK:
			case REBUILD:
			case REDUNDANT:
			case RELOAD:
			case REMOVE:
			case REORGANIZE:
			case REPAIR:
			case REVERSE:
			case SESSION:
			case SHUTDOWN:
			case SIMPLE:
			case SLAVE:
			case VISIBLE:
			case INVISIBLE:
			case ENFORCED:
			case AGAINST:
			case LANGUAGE:
			case EXTENDED:
			case EXPANSION:
			case VARIANCE:
			case MAX_ROWS:
			case MIN_ROWS:
			case SQL_BIG_RESULT:
			case SQL_BUFFER_RESULT:
			case SQL_CACHE:
			case SQL_NO_CACHE:
			case STATS_AUTO_RECALC:
			case STATS_PERSISTENT:
			case STATS_SAMPLE_PAGES:
			case ROW_FORMAT:
			case WEIGHT_STRING:
			case COLUMN_FORMAT:
			case INSERT_METHOD:
			case KEY_BLOCK_SIZE:
			case PACK_KEYS:
			case PERSIST_ONLY:
			case BIT_AND:
			case BIT_OR:
			case BIT_XOR:
			case GROUP_CONCAT:
			case JSON_ARRAYAGG:
			case JSON_OBJECTAGG:
			case STD:
			case STDDEV_POP:
			case STDDEV_SAMP:
			case VAR_POP:
			case VAR_SAMP:
			case AUTO_INCREMENT:
			case AVG_ROW_LENGTH:
			case DELAY_KEY_WRITE:
			case CURRENT_TIMESTAMP:
			case ROTATE:
			case MASTER:
			case BINLOG:
			case ERROR:
			case SCHEDULE:
			case COMPLETION:
			case HOST:
			case SOCKET:
			case PORT:
			case SERVER:
			case WRAPPER:
			case OPTIONS:
			case OWNER:
			case RETURNS:
			case CONTAINS:
			case SECURITY:
			case INVOKER:
			case TEMPTABLE:
			case MERGE:
			case UNDEFINED:
			case DATAFILE:
			case FILE_BLOCK_SIZE:
			case EXTENT_SIZE:
			case INITIAL_SIZE:
			case AUTOEXTEND_SIZE:
			case MAX_SIZE:
			case NODEGROUP:
			case WAIT:
			case LOGFILE:
			case UNDOFILE:
			case UNDO_BUFFER_SIZE:
			case REDO_BUFFER_SIZE:
			case HANDLER:
			case PREV:
			case ORGANIZATION:
			case DEFINITION:
			case DESCRIPTION:
			case REFERENCE:
			case FOLLOWS:
			case PRECEDES:
			case IMPORT:
			case CONCURRENT:
			case XML:
			case DUMPFILE:
			case SHARE:
			case IDENTIFIER_:
			case STRING_:
			case NUMBER_:
			case HEX_DIGIT_:
			case BIT_NUM_:
				{
				setState(944);
				expr(0);
				setState(949);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(945);
					match(COMMA_);
					setState(946);
					expr(0);
					}
					}
					setState(951);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case ASTERISK_:
				{
				setState(952);
				match(ASTERISK_);
				}
				break;
			case RP_:
				break;
			default:
				break;
			}
			setState(955);
			match(RP_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RegularFunctionName_Context extends ParserRuleContext {
		public Identifier_Context identifier_() {
			return getRuleContext(Identifier_Context.class,0);
		}
		public TerminalNode IF() { return getToken(LinglongSQLStatementParser.IF, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(LinglongSQLStatementParser.CURRENT_TIMESTAMP, 0); }
		public TerminalNode LOCALTIME() { return getToken(LinglongSQLStatementParser.LOCALTIME, 0); }
		public TerminalNode LOCALTIMESTAMP() { return getToken(LinglongSQLStatementParser.LOCALTIMESTAMP, 0); }
		public TerminalNode NOW() { return getToken(LinglongSQLStatementParser.NOW, 0); }
		public TerminalNode REPLACE() { return getToken(LinglongSQLStatementParser.REPLACE, 0); }
		public TerminalNode INTERVAL() { return getToken(LinglongSQLStatementParser.INTERVAL, 0); }
		public TerminalNode SUBSTRING() { return getToken(LinglongSQLStatementParser.SUBSTRING, 0); }
		public RegularFunctionName_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_regularFunctionName_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterRegularFunctionName_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitRegularFunctionName_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitRegularFunctionName_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RegularFunctionName_Context regularFunctionName_() throws RecognitionException {
		RegularFunctionName_Context _localctx = new RegularFunctionName_Context(_ctx, getState());
		enterRule(_localctx, 150, RULE_regularFunctionName_);
		try {
			setState(966);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TRUNCATE:
			case POSITION:
			case VIEW:
			case ANY:
			case OFFSET:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case YEAR:
			case QUARTER:
			case MONTH:
			case WEEK:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case INSTANCE:
			case DO:
			case DEFINER:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case OPEN:
			case NEXT:
			case NAME:
			case END:
			case EVERY:
			case PASSWORD:
			case PRIVILEGES:
			case QUERY:
			case USER:
			case MODE:
			case STDDEV:
			case BOTTOM:
			case FIRST:
			case LAST:
			case TABLES:
			case TABLESPACE:
			case COLUMNS:
			case FIELDS:
			case INDEXES:
			case STATUS:
			case MODIFY:
			case VALUE:
			case DUPLICATE:
			case AFTER:
			case OJ:
			case ACCOUNT:
			case ROLE:
			case START:
			case TRANSACTION:
			case WITHOUT:
			case ESCAPE:
			case SUBPARTITION:
			case STORAGE:
			case SUPER:
			case TEMPORARY:
			case THAN:
			case UNBOUNDED:
			case UPGRADE:
			case VALIDATION:
			case ROLLUP:
			case SOUNDS:
			case UNKNOWN:
			case OFF:
			case ALWAYS:
			case COMMITTED:
			case LEVEL:
			case NO:
			case ACTION:
			case ALGORITHM:
			case AUTOCOMMIT:
			case BTREE:
			case CHAIN:
			case CHARSET:
			case CHECKSUM:
			case CIPHER:
			case CLIENT:
			case COALESCE:
			case COMMENT:
			case COMPACT:
			case COMPRESSED:
			case COMPRESSION:
			case CONNECTION:
			case CONSISTENT:
			case DATA:
			case DISCARD:
			case DISK:
			case ENCRYPTION:
			case ENGINE:
			case EVENT:
			case EXCHANGE:
			case EXECUTE:
			case FILE:
			case FIXED:
			case FOLLOWING:
			case GLOBAL:
			case HASH:
			case IMPORT_:
			case LESS:
			case MEMORY:
			case NONE:
			case PARSER:
			case PARTIAL:
			case PARTITIONING:
			case PERSIST:
			case PRECEDING:
			case PROCESS:
			case PROXY:
			case QUICK:
			case REBUILD:
			case REDUNDANT:
			case RELOAD:
			case REMOVE:
			case REORGANIZE:
			case REPAIR:
			case REVERSE:
			case SESSION:
			case SHUTDOWN:
			case SIMPLE:
			case SLAVE:
			case VISIBLE:
			case INVISIBLE:
			case ENFORCED:
			case AGAINST:
			case LANGUAGE:
			case EXTENDED:
			case EXPANSION:
			case VARIANCE:
			case MAX_ROWS:
			case MIN_ROWS:
			case SQL_BIG_RESULT:
			case SQL_BUFFER_RESULT:
			case SQL_CACHE:
			case SQL_NO_CACHE:
			case STATS_AUTO_RECALC:
			case STATS_PERSISTENT:
			case STATS_SAMPLE_PAGES:
			case ROW_FORMAT:
			case WEIGHT_STRING:
			case COLUMN_FORMAT:
			case INSERT_METHOD:
			case KEY_BLOCK_SIZE:
			case PACK_KEYS:
			case PERSIST_ONLY:
			case BIT_AND:
			case BIT_OR:
			case BIT_XOR:
			case GROUP_CONCAT:
			case JSON_ARRAYAGG:
			case JSON_OBJECTAGG:
			case STD:
			case STDDEV_POP:
			case STDDEV_SAMP:
			case VAR_POP:
			case VAR_SAMP:
			case AUTO_INCREMENT:
			case AVG_ROW_LENGTH:
			case DELAY_KEY_WRITE:
			case ROTATE:
			case MASTER:
			case BINLOG:
			case ERROR:
			case SCHEDULE:
			case COMPLETION:
			case HOST:
			case SOCKET:
			case PORT:
			case SERVER:
			case WRAPPER:
			case OPTIONS:
			case OWNER:
			case RETURNS:
			case CONTAINS:
			case SECURITY:
			case INVOKER:
			case TEMPTABLE:
			case MERGE:
			case UNDEFINED:
			case DATAFILE:
			case FILE_BLOCK_SIZE:
			case EXTENT_SIZE:
			case INITIAL_SIZE:
			case AUTOEXTEND_SIZE:
			case MAX_SIZE:
			case NODEGROUP:
			case WAIT:
			case LOGFILE:
			case UNDOFILE:
			case UNDO_BUFFER_SIZE:
			case REDO_BUFFER_SIZE:
			case HANDLER:
			case PREV:
			case ORGANIZATION:
			case DEFINITION:
			case DESCRIPTION:
			case REFERENCE:
			case FOLLOWS:
			case PRECEDES:
			case IMPORT:
			case CONCURRENT:
			case XML:
			case DUMPFILE:
			case SHARE:
			case IDENTIFIER_:
				enterOuterAlt(_localctx, 1);
				{
				setState(957);
				identifier_();
				}
				break;
			case IF:
				enterOuterAlt(_localctx, 2);
				{
				setState(958);
				match(IF);
				}
				break;
			case CURRENT_TIMESTAMP:
				enterOuterAlt(_localctx, 3);
				{
				setState(959);
				match(CURRENT_TIMESTAMP);
				}
				break;
			case LOCALTIME:
				enterOuterAlt(_localctx, 4);
				{
				setState(960);
				match(LOCALTIME);
				}
				break;
			case LOCALTIMESTAMP:
				enterOuterAlt(_localctx, 5);
				{
				setState(961);
				match(LOCALTIMESTAMP);
				}
				break;
			case NOW:
				enterOuterAlt(_localctx, 6);
				{
				setState(962);
				match(NOW);
				}
				break;
			case REPLACE:
				enterOuterAlt(_localctx, 7);
				{
				setState(963);
				match(REPLACE);
				}
				break;
			case INTERVAL:
				enterOuterAlt(_localctx, 8);
				{
				setState(964);
				match(INTERVAL);
				}
				break;
			case SUBSTRING:
				enterOuterAlt(_localctx, 9);
				{
				setState(965);
				match(SUBSTRING);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MatchExpression_Context extends ParserRuleContext {
		public TerminalNode MATCH() { return getToken(LinglongSQLStatementParser.MATCH, 0); }
		public ColumnNamesContext columnNames() {
			return getRuleContext(ColumnNamesContext.class,0);
		}
		public TerminalNode AGAINST() { return getToken(LinglongSQLStatementParser.AGAINST, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public MatchSearchModifier_Context matchSearchModifier_() {
			return getRuleContext(MatchSearchModifier_Context.class,0);
		}
		public MatchExpression_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchExpression_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterMatchExpression_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitMatchExpression_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitMatchExpression_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchExpression_Context matchExpression_() throws RecognitionException {
		MatchExpression_Context _localctx = new MatchExpression_Context(_ctx, getState());
		enterRule(_localctx, 152, RULE_matchExpression_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(968);
			match(MATCH);
			setState(969);
			columnNames();
			setState(970);
			match(AGAINST);
			{
			setState(971);
			expr(0);
			setState(973);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,104,_ctx) ) {
			case 1:
				{
				setState(972);
				matchSearchModifier_();
				}
				break;
			}
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MatchSearchModifier_Context extends ParserRuleContext {
		public TerminalNode IN() { return getToken(LinglongSQLStatementParser.IN, 0); }
		public TerminalNode NATURAL() { return getToken(LinglongSQLStatementParser.NATURAL, 0); }
		public TerminalNode LANGUAGE() { return getToken(LinglongSQLStatementParser.LANGUAGE, 0); }
		public TerminalNode MODE() { return getToken(LinglongSQLStatementParser.MODE, 0); }
		public TerminalNode WITH() { return getToken(LinglongSQLStatementParser.WITH, 0); }
		public TerminalNode QUERY() { return getToken(LinglongSQLStatementParser.QUERY, 0); }
		public TerminalNode EXPANSION() { return getToken(LinglongSQLStatementParser.EXPANSION, 0); }
		public TerminalNode BOOLEAN() { return getToken(LinglongSQLStatementParser.BOOLEAN, 0); }
		public MatchSearchModifier_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchSearchModifier_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterMatchSearchModifier_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitMatchSearchModifier_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitMatchSearchModifier_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchSearchModifier_Context matchSearchModifier_() throws RecognitionException {
		MatchSearchModifier_Context _localctx = new MatchSearchModifier_Context(_ctx, getState());
		enterRule(_localctx, 154, RULE_matchSearchModifier_);
		try {
			setState(992);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,105,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(975);
				match(IN);
				setState(976);
				match(NATURAL);
				setState(977);
				match(LANGUAGE);
				setState(978);
				match(MODE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(979);
				match(IN);
				setState(980);
				match(NATURAL);
				setState(981);
				match(LANGUAGE);
				setState(982);
				match(MODE);
				setState(983);
				match(WITH);
				setState(984);
				match(QUERY);
				setState(985);
				match(EXPANSION);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(986);
				match(IN);
				setState(987);
				match(BOOLEAN);
				setState(988);
				match(MODE);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(989);
				match(WITH);
				setState(990);
				match(QUERY);
				setState(991);
				match(EXPANSION);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CaseExpression_Context extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(LinglongSQLStatementParser.CASE, 0); }
		public TerminalNode END() { return getToken(LinglongSQLStatementParser.END, 0); }
		public SimpleExprContext simpleExpr() {
			return getRuleContext(SimpleExprContext.class,0);
		}
		public List<CaseWhen_Context> caseWhen_() {
			return getRuleContexts(CaseWhen_Context.class);
		}
		public CaseWhen_Context caseWhen_(int i) {
			return getRuleContext(CaseWhen_Context.class,i);
		}
		public CaseElse_Context caseElse_() {
			return getRuleContext(CaseElse_Context.class,0);
		}
		public CaseExpression_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseExpression_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterCaseExpression_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitCaseExpression_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitCaseExpression_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseExpression_Context caseExpression_() throws RecognitionException {
		CaseExpression_Context _localctx = new CaseExpression_Context(_ctx, getState());
		enterRule(_localctx, 156, RULE_caseExpression_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(994);
			match(CASE);
			setState(996);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NOT_) | (1L << TILDE_) | (1L << PLUS_) | (1L << MINUS_) | (1L << DOT_) | (1L << LP_) | (1L << LBE_) | (1L << QUESTION_) | (1L << AT_) | (1L << TRUNCATE) | (1L << POSITION))) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & ((1L << (VIEW - 68)) | (1L << (DISTINCT - 68)) | (1L << (CASE - 68)) | (1L << (SUBSTRING - 68)) | (1L << (IF - 68)) | (1L << (NULL - 68)) | (1L << (TRUE - 68)) | (1L << (FALSE - 68)) | (1L << (EXISTS - 68)) | (1L << (ANY - 68)) | (1L << (OFFSET - 68)) | (1L << (BEGIN - 68)) | (1L << (COMMIT - 68)) | (1L << (ROLLBACK - 68)) | (1L << (SAVEPOINT - 68)) | (1L << (BOOLEAN - 68)) | (1L << (INTERVAL - 68)) | (1L << (DATE - 68)) | (1L << (TIME - 68)) | (1L << (TIMESTAMP - 68)) | (1L << (LOCALTIME - 68)))) != 0) || ((((_la - 132)) & ~0x3f) == 0 && ((1L << (_la - 132)) & ((1L << (LOCALTIMESTAMP - 132)) | (1L << (YEAR - 132)) | (1L << (QUARTER - 132)) | (1L << (MONTH - 132)) | (1L << (WEEK - 132)) | (1L << (DAY - 132)) | (1L << (HOUR - 132)) | (1L << (MINUTE - 132)) | (1L << (SECOND - 132)) | (1L << (MICROSECOND - 132)) | (1L << (MAX - 132)) | (1L << (MIN - 132)) | (1L << (SUM - 132)) | (1L << (COUNT - 132)) | (1L << (AVG - 132)) | (1L << (CURRENT - 132)) | (1L << (ENABLE - 132)) | (1L << (DISABLE - 132)) | (1L << (INSTANCE - 132)) | (1L << (DO - 132)) | (1L << (DEFINER - 132)) | (1L << (CASCADED - 132)) | (1L << (LOCAL - 132)) | (1L << (CLOSE - 132)) | (1L << (OPEN - 132)) | (1L << (NEXT - 132)) | (1L << (NAME - 132)) | (1L << (END - 132)) | (1L << (EVERY - 132)) | (1L << (PASSWORD - 132)) | (1L << (PRIVILEGES - 132)) | (1L << (QUERY - 132)))) != 0) || ((((_la - 200)) & ~0x3f) == 0 && ((1L << (_la - 200)) & ((1L << (USER - 200)) | (1L << (INTEGRAL - 200)) | (1L << (MEAN - 200)) | (1L << (MEDIAN - 200)) | (1L << (MODE - 200)) | (1L << (SPREAD - 200)) | (1L << (STDDEV - 200)) | (1L << (BOTTOM - 200)) | (1L << (FIRST - 200)) | (1L << (LAST - 200)) | (1L << (PERCENTILE - 200)) | (1L << (SAMPLE - 200)) | (1L << (TOP - 200)) | (1L << (ABS - 200)) | (1L << (ACOS - 200)) | (1L << (ASIN - 200)) | (1L << (ATAN - 200)) | (1L << (ATAN2 - 200)) | (1L << (CEIL - 200)) | (1L << (COS - 200)) | (1L << (CUMULATIVE_SUM - 200)) | (1L << (DERIVATIVE - 200)) | (1L << (DIFFERENCE - 200)) | (1L << (EXP - 200)) | (1L << (FLOOR - 200)) | (1L << (HISTOGRAM - 200)) | (1L << (LN - 200)) | (1L << (LOG - 200)) | (1L << (LOG2 - 200)) | (1L << (LOG10 - 200)) | (1L << (MOVING_AVERAGE - 200)) | (1L << (NON_NEGATIVE_DERIVATIVE - 200)) | (1L << (NON_NEGATIVE_DIFFERENCE - 200)) | (1L << (POW - 200)) | (1L << (ROUND - 200)) | (1L << (SIN - 200)) | (1L << (SQRT - 200)) | (1L << (TAN - 200)) | (1L << (TABLES - 200)) | (1L << (TABLESPACE - 200)) | (1L << (COLUMNS - 200)) | (1L << (FIELDS - 200)) | (1L << (INDEXES - 200)) | (1L << (STATUS - 200)) | (1L << (REPLACE - 200)) | (1L << (MODIFY - 200)) | (1L << (VALUE - 200)) | (1L << (DUPLICATE - 200)) | (1L << (AFTER - 200)))) != 0) || ((((_la - 264)) & ~0x3f) == 0 && ((1L << (_la - 264)) & ((1L << (OJ - 264)) | (1L << (ACCOUNT - 264)) | (1L << (ROLE - 264)) | (1L << (START - 264)) | (1L << (TRANSACTION - 264)) | (1L << (ROW - 264)) | (1L << (WITHOUT - 264)) | (1L << (BINARY - 264)) | (1L << (ESCAPE - 264)) | (1L << (SUBPARTITION - 264)) | (1L << (STORAGE - 264)) | (1L << (SUPER - 264)) | (1L << (TEMPORARY - 264)) | (1L << (THAN - 264)) | (1L << (UNBOUNDED - 264)) | (1L << (UPGRADE - 264)) | (1L << (VALIDATION - 264)) | (1L << (ROLLUP - 264)) | (1L << (SOUNDS - 264)) | (1L << (UNKNOWN - 264)) | (1L << (OFF - 264)) | (1L << (ALWAYS - 264)) | (1L << (COMMITTED - 264)) | (1L << (LEVEL - 264)) | (1L << (NO - 264)) | (1L << (ACTION - 264)) | (1L << (ALGORITHM - 264)) | (1L << (AUTOCOMMIT - 264)) | (1L << (BTREE - 264)) | (1L << (CHAIN - 264)) | (1L << (CHARSET - 264)) | (1L << (CHECKSUM - 264)) | (1L << (CIPHER - 264)) | (1L << (CLIENT - 264)) | (1L << (COALESCE - 264)) | (1L << (COMMENT - 264)) | (1L << (COMPACT - 264)) | (1L << (COMPRESSED - 264)) | (1L << (COMPRESSION - 264)))) != 0) || ((((_la - 328)) & ~0x3f) == 0 && ((1L << (_la - 328)) & ((1L << (CONNECTION - 328)) | (1L << (CONSISTENT - 328)) | (1L << (DATA - 328)) | (1L << (DISCARD - 328)) | (1L << (DISK - 328)) | (1L << (ENCRYPTION - 328)) | (1L << (ENGINE - 328)) | (1L << (EVENT - 328)) | (1L << (EXCHANGE - 328)) | (1L << (EXECUTE - 328)) | (1L << (FILE - 328)) | (1L << (FIXED - 328)) | (1L << (FOLLOWING - 328)) | (1L << (GLOBAL - 328)) | (1L << (HASH - 328)) | (1L << (IMPORT_ - 328)) | (1L << (LESS - 328)) | (1L << (MATCH - 328)) | (1L << (MEMORY - 328)) | (1L << (NONE - 328)) | (1L << (NOW - 328)) | (1L << (PARSER - 328)) | (1L << (PARTIAL - 328)) | (1L << (PARTITIONING - 328)) | (1L << (PERSIST - 328)) | (1L << (PRECEDING - 328)) | (1L << (PROCESS - 328)) | (1L << (PROXY - 328)) | (1L << (QUICK - 328)) | (1L << (REBUILD - 328)) | (1L << (REDUNDANT - 328)) | (1L << (RELOAD - 328)) | (1L << (REMOVE - 328)) | (1L << (REORGANIZE - 328)) | (1L << (REPAIR - 328)) | (1L << (REVERSE - 328)))) != 0) || ((((_la - 392)) & ~0x3f) == 0 && ((1L << (_la - 392)) & ((1L << (SESSION - 392)) | (1L << (SHUTDOWN - 392)) | (1L << (SIMPLE - 392)) | (1L << (SLAVE - 392)) | (1L << (VISIBLE - 392)) | (1L << (INVISIBLE - 392)) | (1L << (ENFORCED - 392)) | (1L << (AGAINST - 392)) | (1L << (LANGUAGE - 392)) | (1L << (EXTENDED - 392)) | (1L << (EXPANSION - 392)) | (1L << (VARIANCE - 392)) | (1L << (MAX_ROWS - 392)) | (1L << (MIN_ROWS - 392)) | (1L << (SQL_BIG_RESULT - 392)) | (1L << (SQL_BUFFER_RESULT - 392)) | (1L << (SQL_CACHE - 392)) | (1L << (SQL_NO_CACHE - 392)) | (1L << (STATS_AUTO_RECALC - 392)) | (1L << (STATS_PERSISTENT - 392)) | (1L << (STATS_SAMPLE_PAGES - 392)) | (1L << (ROW_FORMAT - 392)) | (1L << (WEIGHT_STRING - 392)) | (1L << (COLUMN_FORMAT - 392)) | (1L << (INSERT_METHOD - 392)) | (1L << (KEY_BLOCK_SIZE - 392)) | (1L << (PACK_KEYS - 392)) | (1L << (PERSIST_ONLY - 392)) | (1L << (BIT_AND - 392)) | (1L << (BIT_OR - 392)) | (1L << (BIT_XOR - 392)) | (1L << (GROUP_CONCAT - 392)) | (1L << (JSON_ARRAYAGG - 392)) | (1L << (JSON_OBJECTAGG - 392)) | (1L << (STD - 392)) | (1L << (STDDEV_POP - 392)) | (1L << (STDDEV_SAMP - 392)) | (1L << (VAR_POP - 392)) | (1L << (VAR_SAMP - 392)) | (1L << (AUTO_INCREMENT - 392)) | (1L << (AVG_ROW_LENGTH - 392)) | (1L << (DELAY_KEY_WRITE - 392)) | (1L << (CURRENT_TIMESTAMP - 392)))) != 0) || ((((_la - 468)) & ~0x3f) == 0 && ((1L << (_la - 468)) & ((1L << (ROTATE - 468)) | (1L << (MASTER - 468)) | (1L << (BINLOG - 468)) | (1L << (ERROR - 468)) | (1L << (SCHEDULE - 468)) | (1L << (COMPLETION - 468)) | (1L << (HOST - 468)) | (1L << (SOCKET - 468)) | (1L << (PORT - 468)) | (1L << (SERVER - 468)) | (1L << (WRAPPER - 468)) | (1L << (OPTIONS - 468)) | (1L << (OWNER - 468)) | (1L << (RETURNS - 468)) | (1L << (CONTAINS - 468)) | (1L << (SECURITY - 468)) | (1L << (INVOKER - 468)) | (1L << (TEMPTABLE - 468)) | (1L << (MERGE - 468)) | (1L << (UNDEFINED - 468)) | (1L << (DATAFILE - 468)) | (1L << (FILE_BLOCK_SIZE - 468)) | (1L << (EXTENT_SIZE - 468)) | (1L << (INITIAL_SIZE - 468)) | (1L << (AUTOEXTEND_SIZE - 468)) | (1L << (MAX_SIZE - 468)) | (1L << (NODEGROUP - 468)) | (1L << (WAIT - 468)) | (1L << (LOGFILE - 468)) | (1L << (UNDOFILE - 468)) | (1L << (UNDO_BUFFER_SIZE - 468)) | (1L << (REDO_BUFFER_SIZE - 468)) | (1L << (HANDLER - 468)) | (1L << (PREV - 468)) | (1L << (ORGANIZATION - 468)) | (1L << (DEFINITION - 468)) | (1L << (DESCRIPTION - 468)) | (1L << (REFERENCE - 468)) | (1L << (FOLLOWS - 468)) | (1L << (PRECEDES - 468)) | (1L << (IMPORT - 468)) | (1L << (CONCURRENT - 468)) | (1L << (XML - 468)) | (1L << (DUMPFILE - 468)) | (1L << (SHARE - 468)) | (1L << (IDENTIFIER_ - 468)) | (1L << (STRING_ - 468)) | (1L << (NUMBER_ - 468)))) != 0) || _la==HEX_DIGIT_ || _la==BIT_NUM_) {
				{
				setState(995);
				simpleExpr(0);
				}
			}

			setState(999); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(998);
				caseWhen_();
				}
				}
				setState(1001); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WHEN );
			setState(1004);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(1003);
				caseElse_();
				}
			}

			setState(1006);
			match(END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CaseWhen_Context extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(LinglongSQLStatementParser.WHEN, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode THEN() { return getToken(LinglongSQLStatementParser.THEN, 0); }
		public CaseWhen_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseWhen_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterCaseWhen_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitCaseWhen_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitCaseWhen_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseWhen_Context caseWhen_() throws RecognitionException {
		CaseWhen_Context _localctx = new CaseWhen_Context(_ctx, getState());
		enterRule(_localctx, 158, RULE_caseWhen_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1008);
			match(WHEN);
			setState(1009);
			expr(0);
			setState(1010);
			match(THEN);
			setState(1011);
			expr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CaseElse_Context extends ParserRuleContext {
		public TerminalNode ELSE() { return getToken(LinglongSQLStatementParser.ELSE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public CaseElse_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseElse_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterCaseElse_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitCaseElse_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitCaseElse_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseElse_Context caseElse_() throws RecognitionException {
		CaseElse_Context _localctx = new CaseElse_Context(_ctx, getState());
		enterRule(_localctx, 160, RULE_caseElse_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1013);
			match(ELSE);
			setState(1014);
			expr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IntervalExpression_Context extends ParserRuleContext {
		public TerminalNode INTERVAL() { return getToken(LinglongSQLStatementParser.INTERVAL, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public IntervalUnit_Context intervalUnit_() {
			return getRuleContext(IntervalUnit_Context.class,0);
		}
		public IntervalExpression_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intervalExpression_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterIntervalExpression_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitIntervalExpression_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitIntervalExpression_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalExpression_Context intervalExpression_() throws RecognitionException {
		IntervalExpression_Context _localctx = new IntervalExpression_Context(_ctx, getState());
		enterRule(_localctx, 162, RULE_intervalExpression_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1016);
			match(INTERVAL);
			setState(1017);
			expr(0);
			setState(1018);
			intervalUnit_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IntervalUnit_Context extends ParserRuleContext {
		public TerminalNode MICROSECOND() { return getToken(LinglongSQLStatementParser.MICROSECOND, 0); }
		public TerminalNode SECOND() { return getToken(LinglongSQLStatementParser.SECOND, 0); }
		public TerminalNode MINUTE() { return getToken(LinglongSQLStatementParser.MINUTE, 0); }
		public TerminalNode HOUR() { return getToken(LinglongSQLStatementParser.HOUR, 0); }
		public TerminalNode DAY() { return getToken(LinglongSQLStatementParser.DAY, 0); }
		public TerminalNode WEEK() { return getToken(LinglongSQLStatementParser.WEEK, 0); }
		public TerminalNode MONTH() { return getToken(LinglongSQLStatementParser.MONTH, 0); }
		public TerminalNode QUARTER() { return getToken(LinglongSQLStatementParser.QUARTER, 0); }
		public TerminalNode YEAR() { return getToken(LinglongSQLStatementParser.YEAR, 0); }
		public TerminalNode SECOND_MICROSECOND() { return getToken(LinglongSQLStatementParser.SECOND_MICROSECOND, 0); }
		public TerminalNode MINUTE_MICROSECOND() { return getToken(LinglongSQLStatementParser.MINUTE_MICROSECOND, 0); }
		public TerminalNode MINUTE_SECOND() { return getToken(LinglongSQLStatementParser.MINUTE_SECOND, 0); }
		public TerminalNode HOUR_MICROSECOND() { return getToken(LinglongSQLStatementParser.HOUR_MICROSECOND, 0); }
		public TerminalNode HOUR_SECOND() { return getToken(LinglongSQLStatementParser.HOUR_SECOND, 0); }
		public TerminalNode HOUR_MINUTE() { return getToken(LinglongSQLStatementParser.HOUR_MINUTE, 0); }
		public TerminalNode DAY_MICROSECOND() { return getToken(LinglongSQLStatementParser.DAY_MICROSECOND, 0); }
		public TerminalNode DAY_SECOND() { return getToken(LinglongSQLStatementParser.DAY_SECOND, 0); }
		public TerminalNode DAY_MINUTE() { return getToken(LinglongSQLStatementParser.DAY_MINUTE, 0); }
		public TerminalNode DAY_HOUR() { return getToken(LinglongSQLStatementParser.DAY_HOUR, 0); }
		public TerminalNode YEAR_MONTH() { return getToken(LinglongSQLStatementParser.YEAR_MONTH, 0); }
		public IntervalUnit_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intervalUnit_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterIntervalUnit_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitIntervalUnit_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitIntervalUnit_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalUnit_Context intervalUnit_() throws RecognitionException {
		IntervalUnit_Context _localctx = new IntervalUnit_Context(_ctx, getState());
		enterRule(_localctx, 164, RULE_intervalUnit_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1020);
			_la = _input.LA(1);
			if ( !(((((_la - 133)) & ~0x3f) == 0 && ((1L << (_la - 133)) & ((1L << (YEAR - 133)) | (1L << (QUARTER - 133)) | (1L << (MONTH - 133)) | (1L << (WEEK - 133)) | (1L << (DAY - 133)) | (1L << (HOUR - 133)) | (1L << (MINUTE - 133)) | (1L << (SECOND - 133)) | (1L << (MICROSECOND - 133)))) != 0) || ((((_la - 456)) & ~0x3f) == 0 && ((1L << (_la - 456)) & ((1L << (YEAR_MONTH - 456)) | (1L << (DAY_HOUR - 456)) | (1L << (DAY_MINUTE - 456)) | (1L << (DAY_SECOND - 456)) | (1L << (DAY_MICROSECOND - 456)) | (1L << (HOUR_MINUTE - 456)) | (1L << (HOUR_SECOND - 456)) | (1L << (HOUR_MICROSECOND - 456)) | (1L << (MINUTE_SECOND - 456)) | (1L << (MINUTE_MICROSECOND - 456)) | (1L << (SECOND_MICROSECOND - 456)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OrderByClauseContext extends ParserRuleContext {
		public TerminalNode ORDER() { return getToken(LinglongSQLStatementParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(LinglongSQLStatementParser.BY, 0); }
		public List<OrderByItemContext> orderByItem() {
			return getRuleContexts(OrderByItemContext.class);
		}
		public OrderByItemContext orderByItem(int i) {
			return getRuleContext(OrderByItemContext.class,i);
		}
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public OrderByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderByClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterOrderByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitOrderByClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitOrderByClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderByClauseContext orderByClause() throws RecognitionException {
		OrderByClauseContext _localctx = new OrderByClauseContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_orderByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1022);
			match(ORDER);
			setState(1023);
			match(BY);
			setState(1024);
			orderByItem();
			setState(1029);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA_) {
				{
				{
				setState(1025);
				match(COMMA_);
				setState(1026);
				orderByItem();
				}
				}
				setState(1031);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OrderByItemContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public NumberLiteralsContext numberLiterals() {
			return getRuleContext(NumberLiteralsContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode ASC() { return getToken(LinglongSQLStatementParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(LinglongSQLStatementParser.DESC, 0); }
		public OrderByItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderByItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterOrderByItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitOrderByItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitOrderByItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderByItemContext orderByItem() throws RecognitionException {
		OrderByItemContext _localctx = new OrderByItemContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_orderByItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1035);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,110,_ctx) ) {
			case 1:
				{
				setState(1032);
				columnName();
				}
				break;
			case 2:
				{
				setState(1033);
				numberLiterals();
				}
				break;
			case 3:
				{
				setState(1034);
				expr(0);
				}
				break;
			}
			setState(1038);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(1037);
				_la = _input.LA(1);
				if ( !(_la==ASC || _la==DESC) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DataTypeContext extends ParserRuleContext {
		public DataTypeName_Context dataTypeName_() {
			return getRuleContext(DataTypeName_Context.class,0);
		}
		public DataTypeLengthContext dataTypeLength() {
			return getRuleContext(DataTypeLengthContext.class,0);
		}
		public CharacterSet_Context characterSet_() {
			return getRuleContext(CharacterSet_Context.class,0);
		}
		public CollateClause_Context collateClause_() {
			return getRuleContext(CollateClause_Context.class,0);
		}
		public TerminalNode UNSIGNED() { return getToken(LinglongSQLStatementParser.UNSIGNED, 0); }
		public TerminalNode ZEROFILL() { return getToken(LinglongSQLStatementParser.ZEROFILL, 0); }
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public List<TerminalNode> STRING_() { return getTokens(LinglongSQLStatementParser.STRING_); }
		public TerminalNode STRING_(int i) {
			return getToken(LinglongSQLStatementParser.STRING_, i);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public List<TerminalNode> COMMA_() { return getTokens(LinglongSQLStatementParser.COMMA_); }
		public TerminalNode COMMA_(int i) {
			return getToken(LinglongSQLStatementParser.COMMA_, i);
		}
		public DataTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitDataType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeContext dataType() throws RecognitionException {
		DataTypeContext _localctx = new DataTypeContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_dataType);
		int _la;
		try {
			setState(1073);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,120,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1040);
				dataTypeName_();
				setState(1042);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LP_) {
					{
					setState(1041);
					dataTypeLength();
					}
				}

				setState(1045);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CHAR || _la==CHARACTER) {
					{
					setState(1044);
					characterSet_();
					}
				}

				setState(1048);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COLLATE) {
					{
					setState(1047);
					collateClause_();
					}
				}

				setState(1051);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==UNSIGNED) {
					{
					setState(1050);
					match(UNSIGNED);
					}
				}

				setState(1054);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ZEROFILL) {
					{
					setState(1053);
					match(ZEROFILL);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1056);
				dataTypeName_();
				setState(1057);
				match(LP_);
				setState(1058);
				match(STRING_);
				setState(1063);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA_) {
					{
					{
					setState(1059);
					match(COMMA_);
					setState(1060);
					match(STRING_);
					}
					}
					setState(1065);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1066);
				match(RP_);
				setState(1068);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CHAR || _la==CHARACTER) {
					{
					setState(1067);
					characterSet_();
					}
				}

				setState(1071);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COLLATE) {
					{
					setState(1070);
					collateClause_();
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DataTypeName_Context extends ParserRuleContext {
		public List<Identifier_Context> identifier_() {
			return getRuleContexts(Identifier_Context.class);
		}
		public Identifier_Context identifier_(int i) {
			return getRuleContext(Identifier_Context.class,i);
		}
		public DataTypeName_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataTypeName_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterDataTypeName_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitDataTypeName_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitDataTypeName_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeName_Context dataTypeName_() throws RecognitionException {
		DataTypeName_Context _localctx = new DataTypeName_Context(_ctx, getState());
		enterRule(_localctx, 172, RULE_dataTypeName_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1075);
			identifier_();
			setState(1077);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,121,_ctx) ) {
			case 1:
				{
				setState(1076);
				identifier_();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DataTypeLengthContext extends ParserRuleContext {
		public TerminalNode LP_() { return getToken(LinglongSQLStatementParser.LP_, 0); }
		public List<TerminalNode> NUMBER_() { return getTokens(LinglongSQLStatementParser.NUMBER_); }
		public TerminalNode NUMBER_(int i) {
			return getToken(LinglongSQLStatementParser.NUMBER_, i);
		}
		public TerminalNode RP_() { return getToken(LinglongSQLStatementParser.RP_, 0); }
		public TerminalNode COMMA_() { return getToken(LinglongSQLStatementParser.COMMA_, 0); }
		public DataTypeLengthContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataTypeLength; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterDataTypeLength(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitDataTypeLength(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitDataTypeLength(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeLengthContext dataTypeLength() throws RecognitionException {
		DataTypeLengthContext _localctx = new DataTypeLengthContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_dataTypeLength);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1079);
			match(LP_);
			setState(1080);
			match(NUMBER_);
			setState(1083);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA_) {
				{
				setState(1081);
				match(COMMA_);
				setState(1082);
				match(NUMBER_);
				}
			}

			setState(1085);
			match(RP_);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CharacterSet_Context extends ParserRuleContext {
		public TerminalNode SET() { return getToken(LinglongSQLStatementParser.SET, 0); }
		public IgnoredIdentifier_Context ignoredIdentifier_() {
			return getRuleContext(IgnoredIdentifier_Context.class,0);
		}
		public TerminalNode CHARACTER() { return getToken(LinglongSQLStatementParser.CHARACTER, 0); }
		public TerminalNode CHAR() { return getToken(LinglongSQLStatementParser.CHAR, 0); }
		public TerminalNode EQ_() { return getToken(LinglongSQLStatementParser.EQ_, 0); }
		public CharacterSet_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_characterSet_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterCharacterSet_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitCharacterSet_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitCharacterSet_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CharacterSet_Context characterSet_() throws RecognitionException {
		CharacterSet_Context _localctx = new CharacterSet_Context(_ctx, getState());
		enterRule(_localctx, 176, RULE_characterSet_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1087);
			_la = _input.LA(1);
			if ( !(_la==CHAR || _la==CHARACTER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(1088);
			match(SET);
			setState(1090);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ_) {
				{
				setState(1089);
				match(EQ_);
				}
			}

			setState(1092);
			ignoredIdentifier_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CollateClause_Context extends ParserRuleContext {
		public TerminalNode COLLATE() { return getToken(LinglongSQLStatementParser.COLLATE, 0); }
		public TerminalNode STRING_() { return getToken(LinglongSQLStatementParser.STRING_, 0); }
		public IgnoredIdentifier_Context ignoredIdentifier_() {
			return getRuleContext(IgnoredIdentifier_Context.class,0);
		}
		public TerminalNode EQ_() { return getToken(LinglongSQLStatementParser.EQ_, 0); }
		public CollateClause_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_collateClause_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterCollateClause_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitCollateClause_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitCollateClause_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CollateClause_Context collateClause_() throws RecognitionException {
		CollateClause_Context _localctx = new CollateClause_Context(_ctx, getState());
		enterRule(_localctx, 178, RULE_collateClause_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1094);
			match(COLLATE);
			setState(1096);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ_) {
				{
				setState(1095);
				match(EQ_);
				}
			}

			setState(1100);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING_:
				{
				setState(1098);
				match(STRING_);
				}
				break;
			case TRUNCATE:
			case POSITION:
			case VIEW:
			case ANY:
			case OFFSET:
			case BEGIN:
			case COMMIT:
			case ROLLBACK:
			case SAVEPOINT:
			case BOOLEAN:
			case DATE:
			case TIME:
			case TIMESTAMP:
			case YEAR:
			case QUARTER:
			case MONTH:
			case WEEK:
			case DAY:
			case HOUR:
			case MINUTE:
			case SECOND:
			case MICROSECOND:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case AVG:
			case CURRENT:
			case ENABLE:
			case DISABLE:
			case INSTANCE:
			case DO:
			case DEFINER:
			case CASCADED:
			case LOCAL:
			case CLOSE:
			case OPEN:
			case NEXT:
			case NAME:
			case END:
			case EVERY:
			case PASSWORD:
			case PRIVILEGES:
			case QUERY:
			case USER:
			case MODE:
			case STDDEV:
			case BOTTOM:
			case FIRST:
			case LAST:
			case TABLES:
			case TABLESPACE:
			case COLUMNS:
			case FIELDS:
			case INDEXES:
			case STATUS:
			case MODIFY:
			case VALUE:
			case DUPLICATE:
			case AFTER:
			case OJ:
			case ACCOUNT:
			case ROLE:
			case START:
			case TRANSACTION:
			case WITHOUT:
			case ESCAPE:
			case SUBPARTITION:
			case STORAGE:
			case SUPER:
			case TEMPORARY:
			case THAN:
			case UNBOUNDED:
			case UPGRADE:
			case VALIDATION:
			case ROLLUP:
			case SOUNDS:
			case UNKNOWN:
			case OFF:
			case ALWAYS:
			case COMMITTED:
			case LEVEL:
			case NO:
			case ACTION:
			case ALGORITHM:
			case AUTOCOMMIT:
			case BTREE:
			case CHAIN:
			case CHARSET:
			case CHECKSUM:
			case CIPHER:
			case CLIENT:
			case COALESCE:
			case COMMENT:
			case COMPACT:
			case COMPRESSED:
			case COMPRESSION:
			case CONNECTION:
			case CONSISTENT:
			case DATA:
			case DISCARD:
			case DISK:
			case ENCRYPTION:
			case ENGINE:
			case EVENT:
			case EXCHANGE:
			case EXECUTE:
			case FILE:
			case FIXED:
			case FOLLOWING:
			case GLOBAL:
			case HASH:
			case IMPORT_:
			case LESS:
			case MEMORY:
			case NONE:
			case PARSER:
			case PARTIAL:
			case PARTITIONING:
			case PERSIST:
			case PRECEDING:
			case PROCESS:
			case PROXY:
			case QUICK:
			case REBUILD:
			case REDUNDANT:
			case RELOAD:
			case REMOVE:
			case REORGANIZE:
			case REPAIR:
			case REVERSE:
			case SESSION:
			case SHUTDOWN:
			case SIMPLE:
			case SLAVE:
			case VISIBLE:
			case INVISIBLE:
			case ENFORCED:
			case AGAINST:
			case LANGUAGE:
			case EXTENDED:
			case EXPANSION:
			case VARIANCE:
			case MAX_ROWS:
			case MIN_ROWS:
			case SQL_BIG_RESULT:
			case SQL_BUFFER_RESULT:
			case SQL_CACHE:
			case SQL_NO_CACHE:
			case STATS_AUTO_RECALC:
			case STATS_PERSISTENT:
			case STATS_SAMPLE_PAGES:
			case ROW_FORMAT:
			case WEIGHT_STRING:
			case COLUMN_FORMAT:
			case INSERT_METHOD:
			case KEY_BLOCK_SIZE:
			case PACK_KEYS:
			case PERSIST_ONLY:
			case BIT_AND:
			case BIT_OR:
			case BIT_XOR:
			case GROUP_CONCAT:
			case JSON_ARRAYAGG:
			case JSON_OBJECTAGG:
			case STD:
			case STDDEV_POP:
			case STDDEV_SAMP:
			case VAR_POP:
			case VAR_SAMP:
			case AUTO_INCREMENT:
			case AVG_ROW_LENGTH:
			case DELAY_KEY_WRITE:
			case ROTATE:
			case MASTER:
			case BINLOG:
			case ERROR:
			case SCHEDULE:
			case COMPLETION:
			case HOST:
			case SOCKET:
			case PORT:
			case SERVER:
			case WRAPPER:
			case OPTIONS:
			case OWNER:
			case RETURNS:
			case CONTAINS:
			case SECURITY:
			case INVOKER:
			case TEMPTABLE:
			case MERGE:
			case UNDEFINED:
			case DATAFILE:
			case FILE_BLOCK_SIZE:
			case EXTENT_SIZE:
			case INITIAL_SIZE:
			case AUTOEXTEND_SIZE:
			case MAX_SIZE:
			case NODEGROUP:
			case WAIT:
			case LOGFILE:
			case UNDOFILE:
			case UNDO_BUFFER_SIZE:
			case REDO_BUFFER_SIZE:
			case HANDLER:
			case PREV:
			case ORGANIZATION:
			case DEFINITION:
			case DESCRIPTION:
			case REFERENCE:
			case FOLLOWS:
			case PRECEDES:
			case IMPORT:
			case CONCURRENT:
			case XML:
			case DUMPFILE:
			case SHARE:
			case IDENTIFIER_:
				{
				setState(1099);
				ignoredIdentifier_();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IgnoredIdentifier_Context extends ParserRuleContext {
		public List<Identifier_Context> identifier_() {
			return getRuleContexts(Identifier_Context.class);
		}
		public Identifier_Context identifier_(int i) {
			return getRuleContext(Identifier_Context.class,i);
		}
		public TerminalNode DOT_() { return getToken(LinglongSQLStatementParser.DOT_, 0); }
		public IgnoredIdentifier_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ignoredIdentifier_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).enterIgnoredIdentifier_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LinglongSQLStatementListener ) ((LinglongSQLStatementListener)listener).exitIgnoredIdentifier_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LinglongSQLStatementVisitor ) return ((LinglongSQLStatementVisitor<? extends T>)visitor).visitIgnoredIdentifier_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IgnoredIdentifier_Context ignoredIdentifier_() throws RecognitionException {
		IgnoredIdentifier_Context _localctx = new IgnoredIdentifier_Context(_ctx, getState());
		enterRule(_localctx, 180, RULE_ignoredIdentifier_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1102);
			identifier_();
			setState(1105);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,126,_ctx) ) {
			case 1:
				{
				setState(1103);
				match(DOT_);
				setState(1104);
				identifier_();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 38:
			return expr_sempred((ExprContext)_localctx, predIndex);
		case 41:
			return booleanPrimary__sempred((BooleanPrimary_Context)_localctx, predIndex);
		case 44:
			return bitExpr_sempred((BitExprContext)_localctx, predIndex);
		case 45:
			return simpleExpr_sempred((SimpleExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 5);
		case 1:
			return precpred(_ctx, 4);
		}
		return true;
	}
	private boolean booleanPrimary__sempred(BooleanPrimary_Context _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 5);
		case 3:
			return precpred(_ctx, 4);
		case 4:
			return precpred(_ctx, 3);
		case 5:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean bitExpr_sempred(BitExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 6:
			return precpred(_ctx, 15);
		case 7:
			return precpred(_ctx, 14);
		case 8:
			return precpred(_ctx, 13);
		case 9:
			return precpred(_ctx, 12);
		case 10:
			return precpred(_ctx, 11);
		case 11:
			return precpred(_ctx, 10);
		case 12:
			return precpred(_ctx, 9);
		case 13:
			return precpred(_ctx, 8);
		case 14:
			return precpred(_ctx, 7);
		case 15:
			return precpred(_ctx, 6);
		case 16:
			return precpred(_ctx, 5);
		case 17:
			return precpred(_ctx, 4);
		case 18:
			return precpred(_ctx, 3);
		case 19:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean simpleExpr_sempred(SimpleExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 20:
			return precpred(_ctx, 8);
		case 21:
			return precpred(_ctx, 10);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0217\u0456\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"+
		"\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t[\4\\\t\\\3\2\3\2\3\3\3\3\3\3"+
		"\3\3\5\3\u00bf\n\3\3\3\5\3\u00c2\n\3\3\3\5\3\u00c5\n\3\3\3\5\3\u00c8\n"+
		"\3\3\4\3\4\5\4\u00cc\n\4\3\4\3\4\7\4\u00d0\n\4\f\4\16\4\u00d3\13\4\3\5"+
		"\3\5\5\5\u00d7\n\5\3\5\5\5\u00da\n\5\3\5\5\5\u00dd\n\5\3\5\5\5\u00e0\n"+
		"\5\3\6\3\6\5\6\u00e4\n\6\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\n\7"+
		"\n\u00f1\n\n\f\n\16\n\u00f4\13\n\3\13\3\13\3\f\3\f\3\r\3\r\5\r\u00fc\n"+
		"\r\3\r\5\r\u00ff\n\r\3\r\3\r\5\r\u0103\n\r\3\r\3\r\3\r\3\r\5\r\u0109\n"+
		"\r\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\7\17\u0113\n\17\f\17\16\17"+
		"\u0116\13\17\3\17\3\17\5\17\u011a\n\17\3\20\3\20\3\20\3\20\5\20\u0120"+
		"\n\20\3\20\3\20\3\20\3\20\3\20\5\20\u0127\n\20\3\21\3\21\5\21\u012b\n"+
		"\21\3\22\3\22\5\22\u012f\n\22\3\23\3\23\3\23\3\23\5\23\u0135\n\23\3\23"+
		"\5\23\u0138\n\23\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25\3\25\5\25\u0143"+
		"\n\25\3\26\5\26\u0146\n\26\3\26\3\26\5\26\u014a\n\26\3\27\5\27\u014d\n"+
		"\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\30\3\30\5\30\u0158\n\30\3\31"+
		"\5\31\u015b\n\31\3\31\3\31\5\31\u015f\n\31\3\32\5\32\u0162\n\32\3\32\3"+
		"\32\5\32\u0166\n\32\3\33\3\33\3\34\3\34\3\35\3\35\5\35\u016e\n\35\3\36"+
		"\5\36\u0171\n\36\3\36\5\36\u0174\n\36\3\36\5\36\u0177\n\36\3\36\5\36\u017a"+
		"\n\36\3\36\3\36\3\37\3\37\3 \3 \3 \3 \3 \3 \3 \3 \3 \5 \u0189\n \3 \3"+
		" \3!\3!\3\"\3\"\3#\3#\3#\5#\u0194\n#\3$\3$\3%\3%\3&\5&\u019b\n&\3&\3&"+
		"\3&\7&\u01a0\n&\f&\16&\u01a3\13&\3&\5&\u01a6\n&\3\'\3\'\3(\3(\3(\3(\3"+
		"(\3(\3(\3(\3(\5(\u01b3\n(\3(\3(\3(\3(\3(\3(\3(\7(\u01bc\n(\f(\16(\u01bf"+
		"\13(\3)\3)\3*\3*\3+\3+\3+\3+\3+\3+\5+\u01cb\n+\3+\3+\3+\3+\3+\3+\3+\3"+
		"+\3+\3+\3+\3+\3+\7+\u01da\n+\f+\16+\u01dd\13+\3,\3,\3-\3-\5-\u01e3\n-"+
		"\3-\3-\3-\3-\3-\5-\u01ea\n-\3-\3-\3-\3-\3-\7-\u01f1\n-\f-\16-\u01f4\13"+
		"-\3-\3-\3-\3-\5-\u01fa\n-\3-\3-\3-\3-\3-\3-\3-\3-\3-\3-\3-\3-\5-\u0208"+
		"\n-\3-\3-\3-\3-\5-\u020e\n-\3-\3-\5-\u0212\n-\3-\3-\3-\3-\5-\u0218\n-"+
		"\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3."+
		"\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\7."+
		"\u0247\n.\f.\16.\u024a\13.\3/\3/\3/\3/\3/\3/\3/\3/\3/\5/\u0255\n/\3/\3"+
		"/\3/\3/\7/\u025b\n/\f/\16/\u025e\13/\3/\3/\3/\5/\u0263\n/\3/\3/\3/\3/"+
		"\3/\3/\3/\3/\3/\5/\u026e\n/\3/\3/\3/\3/\3/\3/\3/\5/\u0277\n/\7/\u0279"+
		"\n/\f/\16/\u027c\13/\3\60\3\60\3\60\3\60\5\60\u0282\n\60\3\61\3\61\3\61"+
		"\5\61\u0287\n\61\3\61\3\61\3\61\7\61\u028c\n\61\f\61\16\61\u028f\13\61"+
		"\3\61\3\61\3\61\3\61\5\61\u0295\n\61\3\61\3\61\5\61\u0299\n\61\3\62\3"+
		"\62\3\63\3\63\3\63\3\63\3\63\7\63\u02a2\n\63\f\63\16\63\u02a5\13\63\3"+
		"\63\5\63\u02a8\n\63\3\63\3\63\3\64\3\64\3\65\3\65\3\65\3\65\3\65\7\65"+
		"\u02b3\n\65\f\65\16\65\u02b6\13\65\3\65\3\65\3\65\3\65\5\65\u02bc\n\65"+
		"\3\65\3\65\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66"+
		"\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66"+
		"\5\66\u02da\n\66\3\67\3\67\38\38\38\38\38\38\58\u02e4\n8\39\59\u02e7\n"+
		"9\39\59\u02ea\n9\39\59\u02ed\n9\39\59\u02f0\n9\3:\3:\3:\3:\3:\7:\u02f7"+
		"\n:\f:\16:\u02fa\13:\3;\3;\3;\5;\u02ff\n;\3<\3<\3<\3<\3<\3<\3<\3<\3<\3"+
		"<\3<\3<\5<\u030d\n<\3=\3=\3>\3>\3>\3>\3>\3?\3?\3?\3?\3?\3?\3?\3?\3?\3"+
		"?\5?\u0320\n?\3@\3@\3@\5@\u0325\n@\3@\3@\3@\7@\u032a\n@\f@\16@\u032d\13"+
		"@\3@\5@\u0330\n@\3@\5@\u0333\n@\3@\3@\5@\u0337\n@\3@\3@\3A\3A\3A\3A\3"+
		"A\7A\u0340\nA\fA\16A\u0343\13A\3A\3A\3A\3B\3B\3B\3B\3B\3B\3B\3C\3C\3C"+
		"\3C\3C\3C\3C\3C\3C\3C\3C\3C\3C\3C\5C\u035d\nC\3D\3D\3D\3D\3D\3D\3D\3E"+
		"\3E\3E\3E\3E\3E\3E\5E\u036d\nE\3E\3E\3F\3F\3F\3F\3F\3F\3F\3G\3G\3G\3G"+
		"\3G\7G\u037d\nG\fG\16G\u0380\13G\3G\3G\5G\u0384\nG\3G\3G\3H\3H\3H\3H\3"+
		"H\3H\3H\3H\3I\3I\3I\3I\3I\5I\u0395\nI\3I\5I\u0398\nI\3I\3I\3J\3J\3J\3"+
		"J\7J\u03a0\nJ\fJ\16J\u03a3\13J\3J\3J\3J\5J\u03a8\nJ\3K\3K\5K\u03ac\nK"+
		"\3K\5K\u03af\nK\3L\3L\3L\3L\3L\7L\u03b6\nL\fL\16L\u03b9\13L\3L\5L\u03bc"+
		"\nL\3L\3L\3M\3M\3M\3M\3M\3M\3M\3M\3M\5M\u03c9\nM\3N\3N\3N\3N\3N\5N\u03d0"+
		"\nN\3O\3O\3O\3O\3O\3O\3O\3O\3O\3O\3O\3O\3O\3O\3O\3O\3O\5O\u03e3\nO\3P"+
		"\3P\5P\u03e7\nP\3P\6P\u03ea\nP\rP\16P\u03eb\3P\5P\u03ef\nP\3P\3P\3Q\3"+
		"Q\3Q\3Q\3Q\3R\3R\3R\3S\3S\3S\3S\3T\3T\3U\3U\3U\3U\3U\7U\u0406\nU\fU\16"+
		"U\u0409\13U\3V\3V\3V\5V\u040e\nV\3V\5V\u0411\nV\3W\3W\5W\u0415\nW\3W\5"+
		"W\u0418\nW\3W\5W\u041b\nW\3W\5W\u041e\nW\3W\5W\u0421\nW\3W\3W\3W\3W\3"+
		"W\7W\u0428\nW\fW\16W\u042b\13W\3W\3W\5W\u042f\nW\3W\5W\u0432\nW\5W\u0434"+
		"\nW\3X\3X\5X\u0438\nX\3Y\3Y\3Y\3Y\5Y\u043e\nY\3Y\3Y\3Z\3Z\3Z\5Z\u0445"+
		"\nZ\3Z\3Z\3[\3[\5[\u044b\n[\3[\3[\5[\u044f\n[\3\\\3\\\3\\\5\\\u0454\n"+
		"\\\3\\\2\6NTZ\\]\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62"+
		"\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088"+
		"\u008a\u008c\u008e\u0090\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0"+
		"\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\2\25"+
		"\3\2\u0082\u0084\3\2hi\6\2\u0161\u0161\u0176\u0176\u018a\u018a\u01b5\u01b5"+
		"L\2\63\63AAFFnnw|\u0082\u0084\u0087\u0094\u0096\u0098\u009a\u009a\u009c"+
		"\u009d\u00a0\u00a5\u00ad\u00ae\u00b9\u00b9\u00bc\u00bc\u00be\u00be\u00ca"+
		"\u00ca\u00d0\u00d0\u00d2\u00d5\u00fe\u0103\u0105\u0105\u0107\u010a\u0111"+
		"\u0114\u0117\u0117\u0119\u0119\u011c\u011d\u011f\u011f\u0121\u0122\u0124"+
		"\u0124\u0127\u0127\u0129\u0129\u012b\u012f\u0132\u0134\u0137\u0138\u013a"+
		"\u013a\u013d\u013e\u0140\u0144\u0146\u014b\u014e\u014e\u0151\u0152\u0154"+
		"\u0156\u0158\u0158\u015a\u015a\u015c\u015e\u0161\u0162\u0165\u0165\u0168"+
		"\u0168\u016c\u016d\u0173\u017a\u017c\u017c\u017e\u017e\u0180\u0181\u0183"+
		"\u0184\u0187\u0187\u018a\u018a\u018c\u018e\u0191\u0192\u0194\u019b\u019e"+
		"\u01a0\u01a2\u01a2\u01a4\u01a6\u01a8\u01a8\u01ad\u01ae\u01b2\u01c0\u01c2"+
		"\u01c3\u01c5\u01c5\u01d6\u01db\u01de\u01e4\u01e6\u01e7\u01ea\u01eb\u01ed"+
		"\u0204\u0206\u0206\u020e\u020e\u0210\u0210\u0212\u0212\4\2\3\4cd\4\2\5"+
		"\5ff\4\2gi\u012d\u012d\3\2mn\3\2\27\34\3\2\u010f\u0110\5\2\5\6\16\17\u0118"+
		"\u0118\5\2KK\u0092\u0093\u00cd\u00d2\4\2\u0090\u0091\u00d3\u00d8\4\2\u0116"+
		"\u0116\u017b\u017b\4\2PP\u0120\u0120\5\2\u0123\u0123\u013c\u013c\u0167"+
		"\u0167\3\2st\4\2\u0087\u008f\u01ca\u01d4\3\2~\177\2\u04d3\2\u00b8\3\2"+
		"\2\2\4\u00ba\3\2\2\2\6\u00cb\3\2\2\2\b\u00df\3\2\2\2\n\u00e3\3\2\2\2\f"+
		"\u00e5\3\2\2\2\16\u00e7\3\2\2\2\20\u00ea\3\2\2\2\22\u00ed\3\2\2\2\24\u00f5"+
		"\3\2\2\2\26\u00f7\3\2\2\2\30\u0108\3\2\2\2\32\u010a\3\2\2\2\34\u010d\3"+
		"\2\2\2\36\u011b\3\2\2\2 \u012a\3\2\2\2\"\u012e\3\2\2\2$\u0130\3\2\2\2"+
		"&\u0139\3\2\2\2(\u0142\3\2\2\2*\u0145\3\2\2\2,\u014c\3\2\2\2.\u0157\3"+
		"\2\2\2\60\u015a\3\2\2\2\62\u0161\3\2\2\2\64\u0167\3\2\2\2\66\u0169\3\2"+
		"\2\28\u016d\3\2\2\2:\u0173\3\2\2\2<\u017d\3\2\2\2>\u0188\3\2\2\2@\u018c"+
		"\3\2\2\2B\u018e\3\2\2\2D\u0190\3\2\2\2F\u0195\3\2\2\2H\u0197\3\2\2\2J"+
		"\u019a\3\2\2\2L\u01a7\3\2\2\2N\u01b2\3\2\2\2P\u01c0\3\2\2\2R\u01c2\3\2"+
		"\2\2T\u01c4\3\2\2\2V\u01de\3\2\2\2X\u0217\3\2\2\2Z\u0219\3\2\2\2\\\u026d"+
		"\3\2\2\2^\u0281\3\2\2\2`\u0283\3\2\2\2b\u029a\3\2\2\2d\u029c\3\2\2\2f"+
		"\u02ab\3\2\2\2h\u02ad\3\2\2\2j\u02d9\3\2\2\2l\u02db\3\2\2\2n\u02dd\3\2"+
		"\2\2p\u02e6\3\2\2\2r\u02f1\3\2\2\2t\u02fb\3\2\2\2v\u030c\3\2\2\2x\u030e"+
		"\3\2\2\2z\u0310\3\2\2\2|\u031f\3\2\2\2~\u0321\3\2\2\2\u0080\u033a\3\2"+
		"\2\2\u0082\u0347\3\2\2\2\u0084\u035c\3\2\2\2\u0086\u035e\3\2\2\2\u0088"+
		"\u0365\3\2\2\2\u008a\u0370\3\2\2\2\u008c\u0377\3\2\2\2\u008e\u0387\3\2"+
		"\2\2\u0090\u038f\3\2\2\2\u0092\u039b\3\2\2\2\u0094\u03a9\3\2\2\2\u0096"+
		"\u03b0\3\2\2\2\u0098\u03c8\3\2\2\2\u009a\u03ca\3\2\2\2\u009c\u03e2\3\2"+
		"\2\2\u009e\u03e4\3\2\2\2\u00a0\u03f2\3\2\2\2\u00a2\u03f7\3\2\2\2\u00a4"+
		"\u03fa\3\2\2\2\u00a6\u03fe\3\2\2\2\u00a8\u0400\3\2\2\2\u00aa\u040d\3\2"+
		"\2\2\u00ac\u0433\3\2\2\2\u00ae\u0435\3\2\2\2\u00b0\u0439\3\2\2\2\u00b2"+
		"\u0441\3\2\2\2\u00b4\u0448\3\2\2\2\u00b6\u0450\3\2\2\2\u00b8\u00b9\5\4"+
		"\3\2\u00b9\3\3\2\2\2\u00ba\u00bb\7,\2\2\u00bb\u00bc\5\6\4\2\u00bc\u00be"+
		"\5\20\t\2\u00bd\u00bf\5\32\16\2\u00be\u00bd\3\2\2\2\u00be\u00bf\3\2\2"+
		"\2\u00bf\u00c1\3\2\2\2\u00c0\u00c2\5\34\17\2\u00c1\u00c0\3\2\2\2\u00c1"+
		"\u00c2\3\2\2\2\u00c2\u00c4\3\2\2\2\u00c3\u00c5\5\u00a8U\2\u00c4\u00c3"+
		"\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c5\u00c7\3\2\2\2\u00c6\u00c8\5\36\20\2"+
		"\u00c7\u00c6\3\2\2\2\u00c7\u00c8\3\2\2\2\u00c8\5\3\2\2\2\u00c9\u00cc\5"+
		"\f\7\2\u00ca\u00cc\5\b\5\2\u00cb\u00c9\3\2\2\2\u00cb\u00ca\3\2\2\2\u00cc"+
		"\u00d1\3\2\2\2\u00cd\u00ce\7$\2\2\u00ce\u00d0\5\b\5\2\u00cf\u00cd\3\2"+
		"\2\2\u00d0\u00d3\3\2\2\2\u00d1\u00cf\3\2\2\2\u00d1\u00d2\3\2\2\2\u00d2"+
		"\7\3\2\2\2\u00d3\u00d1\3\2\2\2\u00d4\u00d7\5D#\2\u00d5\u00d7\5N(\2\u00d6"+
		"\u00d4\3\2\2\2\u00d6\u00d5\3\2\2\2\u00d7\u00dc\3\2\2\2\u00d8\u00da\7\\"+
		"\2\2\u00d9\u00d8\3\2\2\2\u00d9\u00da\3\2\2\2\u00da\u00db\3\2\2\2\u00db"+
		"\u00dd\5\n\6\2\u00dc\u00d9\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dd\u00e0\3\2"+
		"\2\2\u00de\u00e0\5\16\b\2\u00df\u00d6\3\2\2\2\u00df\u00de\3\2\2\2\u00e0"+
		"\t\3\2\2\2\u00e1\u00e4\58\35\2\u00e2\u00e4\7\u0214\2\2\u00e3\u00e1\3\2"+
		"\2\2\u00e3\u00e2\3\2\2\2\u00e4\13\3\2\2\2\u00e5\u00e6\7\20\2\2\u00e6\r"+
		"\3\2\2\2\u00e7\u00e8\58\35\2\u00e8\u00e9\7\24\2\2\u00e9\17\3\2\2\2\u00ea"+
		"\u00eb\7Q\2\2\u00eb\u00ec\5\22\n\2\u00ec\21\3\2\2\2\u00ed\u00f2\5\24\13"+
		"\2\u00ee\u00ef\7$\2\2\u00ef\u00f1\5\24\13\2\u00f0\u00ee\3\2\2\2\u00f1"+
		"\u00f4\3\2\2\2\u00f2\u00f0\3\2\2\2\u00f2\u00f3\3\2\2\2\u00f3\23\3\2\2"+
		"\2\u00f4\u00f2\3\2\2\2\u00f5\u00f6\5\26\f\2\u00f6\25\3\2\2\2\u00f7\u00f8"+
		"\5\30\r\2\u00f8\27\3\2\2\2\u00f9\u00fe\5> \2\u00fa\u00fc\7\\\2\2\u00fb"+
		"\u00fa\3\2\2\2\u00fb\u00fc\3\2\2\2\u00fc\u00fd\3\2\2\2\u00fd\u00ff\5\n"+
		"\6\2\u00fe\u00fb\3\2\2\2\u00fe\u00ff\3\2\2\2\u00ff\u0109\3\2\2\2\u0100"+
		"\u0102\5$\23\2\u0101\u0103\5J&\2\u0102\u0101\3\2\2\2\u0102\u0103\3\2\2"+
		"\2\u0103\u0109\3\2\2\2\u0104\u0105\7\36\2\2\u0105\u0106\5\22\n\2\u0106"+
		"\u0107\7\37\2\2\u0107\u0109\3\2\2\2\u0108\u00f9\3\2\2\2\u0108\u0100\3"+
		"\2\2\2\u0108\u0104\3\2\2\2\u0109\31\3\2\2\2\u010a\u010b\7[\2\2\u010b\u010c"+
		"\5N(\2\u010c\33\3\2\2\2\u010d\u010e\7q\2\2\u010e\u010f\7r\2\2\u010f\u0114"+
		"\5\u00aaV\2\u0110\u0111\7$\2\2\u0111\u0113\5\u00aaV\2\u0112\u0110\3\2"+
		"\2\2\u0113\u0116\3\2\2\2\u0114\u0112\3\2\2\2\u0114\u0115\3\2\2\2\u0115"+
		"\u0119\3\2\2\2\u0116\u0114\3\2\2\2\u0117\u0118\7I\2\2\u0118\u011a\7\u012b"+
		"\2\2\u0119\u0117\3\2\2\2\u0119\u011a\3\2\2\2\u011a\35\3\2\2\2\u011b\u0126"+
		"\7v\2\2\u011c\u011d\5\"\22\2\u011d\u011e\7$\2\2\u011e\u0120\3\2\2\2\u011f"+
		"\u011c\3\2\2\2\u011f\u0120\3\2\2\2\u0120\u0121\3\2\2\2\u0121\u0127\5 "+
		"\21\2\u0122\u0123\5 \21\2\u0123\u0124\7w\2\2\u0124\u0125\5\"\22\2\u0125"+
		"\u0127\3\2\2\2\u0126\u011f\3\2\2\2\u0126\u0122\3\2\2\2\u0127\37\3\2\2"+
		"\2\u0128\u012b\5,\27\2\u0129\u012b\5&\24\2\u012a\u0128\3\2\2\2\u012a\u0129"+
		"\3\2\2\2\u012b!\3\2\2\2\u012c\u012f\5,\27\2\u012d\u012f\5&\24\2\u012e"+
		"\u012c\3\2\2\2\u012e\u012d\3\2\2\2\u012f#\3\2\2\2\u0130\u0131\7\36\2\2"+
		"\u0131\u0132\5\4\3\2\u0132\u0134\7\37\2\2\u0133\u0135\7\\\2\2\u0134\u0133"+
		"\3\2\2\2\u0134\u0135\3\2\2\2\u0135\u0137\3\2\2\2\u0136\u0138\5\n\6\2\u0137"+
		"\u0136\3\2\2\2\u0137\u0138\3\2\2\2\u0138%\3\2\2\2\u0139\u013a\7(\2\2\u013a"+
		"\'\3\2\2\2\u013b\u0143\5*\26\2\u013c\u0143\5,\27\2\u013d\u0143\5.\30\2"+
		"\u013e\u0143\5\60\31\2\u013f\u0143\5\62\32\2\u0140\u0143\5\64\33\2\u0141"+
		"\u0143\5\66\34\2\u0142\u013b\3\2\2\2\u0142\u013c\3\2\2\2\u0142\u013d\3"+
		"\2\2\2\u0142\u013e\3\2\2\2\u0142\u013f\3\2\2\2\u0142\u0140\3\2\2\2\u0142"+
		"\u0141\3\2\2\2\u0143)\3\2\2\2\u0144\u0146\5L\'\2\u0145\u0144\3\2\2\2\u0145"+
		"\u0146\3\2\2\2\u0146\u0147\3\2\2\2\u0147\u0149\7\u0214\2\2\u0148\u014a"+
		"\5\u00b4[\2\u0149\u0148\3\2\2\2\u0149\u014a\3\2\2\2\u014a+\3\2\2\2\u014b"+
		"\u014d\7\17\2\2\u014c\u014b\3\2\2\2\u014c\u014d\3\2\2\2\u014d\u014e\3"+
		"\2\2\2\u014e\u014f\7\u0215\2\2\u014f-\3\2\2\2\u0150\u0151\t\2\2\2\u0151"+
		"\u0158\7\u0214\2\2\u0152\u0153\7 \2\2\u0153\u0154\58\35\2\u0154\u0155"+
		"\7\u0214\2\2\u0155\u0156\7!\2\2\u0156\u0158\3\2\2\2\u0157\u0150\3\2\2"+
		"\2\u0157\u0152\3\2\2\2\u0158/\3\2\2\2\u0159\u015b\5L\'\2\u015a\u0159\3"+
		"\2\2\2\u015a\u015b\3\2\2\2\u015b\u015c\3\2\2\2\u015c\u015e\7\u0216\2\2"+
		"\u015d\u015f\5\u00b4[\2\u015e\u015d\3\2\2\2\u015e\u015f\3\2\2\2\u015f"+
		"\61\3\2\2\2\u0160\u0162\5L\'\2\u0161\u0160\3\2\2\2\u0161\u0162\3\2\2\2"+
		"\u0162\u0163\3\2\2\2\u0163\u0165\7\u0217\2\2\u0164\u0166\5\u00b4[\2\u0165"+
		"\u0164\3\2\2\2\u0165\u0166\3\2\2\2\u0166\63\3\2\2\2\u0167\u0168\t\3\2"+
		"\2\u0168\65\3\2\2\2\u0169\u016a\7g\2\2\u016a\67\3\2\2\2\u016b\u016e\7"+
		"\u0213\2\2\u016c\u016e\5<\37\2\u016d\u016b\3\2\2\2\u016d\u016c\3\2\2\2"+
		"\u016e9\3\2\2\2\u016f\u0171\7)\2\2\u0170\u016f\3\2\2\2\u0170\u0171\3\2"+
		"\2\2\u0171\u0172\3\2\2\2\u0172\u0174\7)\2\2\u0173\u0170\3\2\2\2\u0173"+
		"\u0174\3\2\2\2\u0174\u0176\3\2\2\2\u0175\u0177\t\4\2\2\u0176\u0175\3\2"+
		"\2\2\u0176\u0177\3\2\2\2\u0177\u0179\3\2\2\2\u0178\u017a\7\23\2\2\u0179"+
		"\u0178\3\2\2\2\u0179\u017a\3\2\2\2\u017a\u017b\3\2\2\2\u017b\u017c\58"+
		"\35\2\u017c;\3\2\2\2\u017d\u017e\t\5\2\2\u017e=\3\2\2\2\u017f\u0180\5"+
		"@!\2\u0180\u0181\7\23\2\2\u0181\u0182\3\2\2\2\u0182\u0183\5B\"\2\u0183"+
		"\u0184\7\23\2\2\u0184\u0189\3\2\2\2\u0185\u0186\5B\"\2\u0186\u0187\7\23"+
		"\2\2\u0187\u0189\3\2\2\2\u0188\u017f\3\2\2\2\u0188\u0185\3\2\2\2\u0188"+
		"\u0189\3\2\2\2\u0189\u018a\3\2\2\2\u018a\u018b\5H%\2\u018b?\3\2\2\2\u018c"+
		"\u018d\58\35\2\u018dA\3\2\2\2\u018e\u018f\58\35\2\u018fC\3\2\2\2\u0190"+
		"\u0193\5H%\2\u0191\u0192\7\23\2\2\u0192\u0194\5D#\2\u0193\u0191\3\2\2"+
		"\2\u0193\u0194\3\2\2\2\u0194E\3\2\2\2\u0195\u0196\58\35\2\u0196G\3\2\2"+
		"\2\u0197\u0198\58\35\2\u0198I\3\2\2\2\u0199\u019b\7\36\2\2\u019a\u0199"+
		"\3\2\2\2\u019a\u019b\3\2\2\2\u019b\u019c\3\2\2\2\u019c\u01a1\5D#\2\u019d"+
		"\u019e\7$\2\2\u019e\u01a0\5D#\2\u019f\u019d\3\2\2\2\u01a0\u01a3\3\2\2"+
		"\2\u01a1\u019f\3\2\2\2\u01a1\u01a2\3\2\2\2\u01a2\u01a5\3\2\2\2\u01a3\u01a1"+
		"\3\2\2\2\u01a4\u01a6\7\37\2\2\u01a5\u01a4\3\2\2\2\u01a5\u01a6\3\2\2\2"+
		"\u01a6K\3\2\2\2\u01a7\u01a8\7\u0213\2\2\u01a8M\3\2\2\2\u01a9\u01aa\b("+
		"\1\2\u01aa\u01ab\5R*\2\u01ab\u01ac\5N(\5\u01ac\u01b3\3\2\2\2\u01ad\u01ae"+
		"\7\36\2\2\u01ae\u01af\5N(\2\u01af\u01b0\7\37\2\2\u01b0\u01b3\3\2\2\2\u01b1"+
		"\u01b3\5T+\2\u01b2\u01a9\3\2\2\2\u01b2\u01ad\3\2\2\2\u01b2\u01b1\3\2\2"+
		"\2\u01b3\u01bd\3\2\2\2\u01b4\u01b5\f\7\2\2\u01b5\u01b6\5P)\2\u01b6\u01b7"+
		"\5N(\b\u01b7\u01bc\3\2\2\2\u01b8\u01b9\f\6\2\2\u01b9\u01ba\7\u010e\2\2"+
		"\u01ba\u01bc\5N(\7\u01bb\u01b4\3\2\2\2\u01bb\u01b8\3\2\2\2\u01bc\u01bf"+
		"\3\2\2\2\u01bd\u01bb\3\2\2\2\u01bd\u01be\3\2\2\2\u01beO\3\2\2\2\u01bf"+
		"\u01bd\3\2\2\2\u01c0\u01c1\t\6\2\2\u01c1Q\3\2\2\2\u01c2\u01c3\t\7\2\2"+
		"\u01c3S\3\2\2\2\u01c4\u01c5\b+\1\2\u01c5\u01c6\5X-\2\u01c6\u01db\3\2\2"+
		"\2\u01c7\u01c8\f\7\2\2\u01c8\u01ca\7e\2\2\u01c9\u01cb\7f\2\2\u01ca\u01c9"+
		"\3\2\2\2\u01ca\u01cb\3\2\2\2\u01cb\u01cc\3\2\2\2\u01cc\u01da\t\b\2\2\u01cd"+
		"\u01ce\f\6\2\2\u01ce\u01cf\7\25\2\2\u01cf\u01da\5X-\2\u01d0\u01d1\f\5"+
		"\2\2\u01d1\u01d2\5V,\2\u01d2\u01d3\5X-\2\u01d3\u01da\3\2\2\2\u01d4\u01d5"+
		"\f\4\2\2\u01d5\u01d6\5V,\2\u01d6\u01d7\t\t\2\2\u01d7\u01d8\5$\23\2\u01d8"+
		"\u01da\3\2\2\2\u01d9\u01c7\3\2\2\2\u01d9\u01cd\3\2\2\2\u01d9\u01d0\3\2"+
		"\2\2\u01d9\u01d4\3\2\2\2\u01da\u01dd\3\2\2\2\u01db\u01d9\3\2\2\2\u01db"+
		"\u01dc\3\2\2\2\u01dcU\3\2\2\2\u01dd\u01db\3\2\2\2\u01de\u01df\t\n\2\2"+
		"\u01dfW\3\2\2\2\u01e0\u01e2\5Z.\2\u01e1\u01e3\7f\2\2\u01e2\u01e1\3\2\2"+
		"\2\u01e2\u01e3\3\2\2\2\u01e3\u01e4\3\2\2\2\u01e4\u01e5\7l\2\2\u01e5\u01e6"+
		"\5$\23\2\u01e6\u0218\3\2\2\2\u01e7\u01e9\5Z.\2\u01e8\u01ea\7f\2\2\u01e9"+
		"\u01e8\3\2\2\2\u01e9\u01ea\3\2\2\2\u01ea\u01eb\3\2\2\2\u01eb\u01ec\7l"+
		"\2\2\u01ec\u01ed\7\36\2\2\u01ed\u01f2\5N(\2\u01ee\u01ef\7$\2\2\u01ef\u01f1"+
		"\5N(\2\u01f0\u01ee\3\2\2\2\u01f1\u01f4\3\2\2\2\u01f2\u01f0\3\2\2\2\u01f2"+
		"\u01f3\3\2\2\2\u01f3\u01f5\3\2\2\2\u01f4\u01f2\3\2\2\2\u01f5\u01f6\7\37"+
		"\2\2\u01f6\u0218\3\2\2\2\u01f7\u01f9\5Z.\2\u01f8\u01fa\7f\2\2\u01f9\u01f8"+
		"\3\2\2\2\u01f9\u01fa\3\2\2\2\u01fa\u01fb\3\2\2\2\u01fb\u01fc\7k\2\2\u01fc"+
		"\u01fd\5Z.\2\u01fd\u01fe\7c\2\2\u01fe\u01ff\5X-\2\u01ff\u0218\3\2\2\2"+
		"\u0200\u0201\5Z.\2\u0201\u0202\7\u012c\2\2\u0202\u0203\7o\2\2\u0203\u0204"+
		"\5Z.\2\u0204\u0218\3\2\2\2\u0205\u0207\5Z.\2\u0206\u0208\7f\2\2\u0207"+
		"\u0206\3\2\2\2\u0207\u0208\3\2\2\2\u0208\u0209\3\2\2\2\u0209\u020a\7o"+
		"\2\2\u020a\u020d\5\\/\2\u020b\u020c\7\u0119\2\2\u020c\u020e\5\\/\2\u020d"+
		"\u020b\3\2\2\2\u020d\u020e\3\2\2\2\u020e\u0218\3\2\2\2\u020f\u0211\5Z"+
		".\2\u0210\u0212\7f\2\2\u0211\u0210\3\2\2\2\u0211\u0212\3\2\2\2\u0212\u0213"+
		"\3\2\2\2\u0213\u0214\t\13\2\2\u0214\u0215\5Z.\2\u0215\u0218\3\2\2\2\u0216"+
		"\u0218\5Z.\2\u0217\u01e0\3\2\2\2\u0217\u01e7\3\2\2\2\u0217\u01f7\3\2\2"+
		"\2\u0217\u0200\3\2\2\2\u0217\u0205\3\2\2\2\u0217\u020f\3\2\2\2\u0217\u0216"+
		"\3\2\2\2\u0218Y\3\2\2\2\u0219\u021a\b.\1\2\u021a\u021b\5\\/\2\u021b\u0248"+
		"\3\2\2\2\u021c\u021d\f\21\2\2\u021d\u021e\7\7\2\2\u021e\u0247\5Z.\22\u021f"+
		"\u0220\f\20\2\2\u0220\u0221\7\b\2\2\u0221\u0247\5Z.\21\u0222\u0223\f\17"+
		"\2\2\u0223\u0224\7\t\2\2\u0224\u0247\5Z.\20\u0225\u0226\f\16\2\2\u0226"+
		"\u0227\7\n\2\2\u0227\u0247\5Z.\17\u0228\u0229\f\r\2\2\u0229\u022a\7\16"+
		"\2\2\u022a\u0247\5Z.\16\u022b\u022c\f\f\2\2\u022c\u022d\7\17\2\2\u022d"+
		"\u0247\5Z.\r\u022e\u022f\f\13\2\2\u022f\u0230\7\20\2\2\u0230\u0247\5Z"+
		".\f\u0231\u0232\f\n\2\2\u0232\u0233\7\21\2\2\u0233\u0247\5Z.\13\u0234"+
		"\u0235\f\t\2\2\u0235\u0236\7\u010d\2\2\u0236\u0247\5Z.\n\u0237\u0238\f"+
		"\b\2\2\u0238\u0239\7\u010c\2\2\u0239\u0247\5Z.\t\u023a\u023b\f\7\2\2\u023b"+
		"\u023c\7\f\2\2\u023c\u0247\5Z.\b\u023d\u023e\f\6\2\2\u023e\u023f\7\13"+
		"\2\2\u023f\u0247\5Z.\7\u0240\u0241\f\5\2\2\u0241\u0242\7\16\2\2\u0242"+
		"\u0247\5\u00a4S\2\u0243\u0244\f\4\2\2\u0244\u0245\7\17\2\2\u0245\u0247"+
		"\5\u00a4S\2\u0246\u021c\3\2\2\2\u0246\u021f\3\2\2\2\u0246\u0222\3\2\2"+
		"\2\u0246\u0225\3\2\2\2\u0246\u0228\3\2\2\2\u0246\u022b\3\2\2\2\u0246\u022e"+
		"\3\2\2\2\u0246\u0231\3\2\2\2\u0246\u0234\3\2\2\2\u0246\u0237\3\2\2\2\u0246"+
		"\u023a\3\2\2\2\u0246\u023d\3\2\2\2\u0246\u0240\3\2\2\2\u0246\u0243\3\2"+
		"\2\2\u0247\u024a\3\2\2\2\u0248\u0246\3\2\2\2\u0248\u0249\3\2\2\2\u0249"+
		"[\3\2\2\2\u024a\u0248\3\2\2\2\u024b\u024c\b/\1\2\u024c\u026e\5^\60\2\u024d"+
		"\u026e\5&\24\2\u024e\u026e\5(\25\2\u024f\u026e\5D#\2\u0250\u026e\5:\36"+
		"\2\u0251\u0252\t\f\2\2\u0252\u026e\5\\/\t\u0253\u0255\7\u0115\2\2\u0254"+
		"\u0253\3\2\2\2\u0254\u0255\3\2\2\2\u0255\u0256\3\2\2\2\u0256\u0257\7\36"+
		"\2\2\u0257\u025c\5N(\2\u0258\u0259\7$\2\2\u0259\u025b\5N(\2\u025a\u0258"+
		"\3\2\2\2\u025b\u025e\3\2\2\2\u025c\u025a\3\2\2\2\u025c\u025d\3\2\2\2\u025d"+
		"\u025f\3\2\2\2\u025e\u025c\3\2\2\2\u025f\u0260\7\37\2\2\u0260\u026e\3"+
		"\2\2\2\u0261\u0263\7j\2\2\u0262\u0261\3\2\2\2\u0262\u0263\3\2\2\2\u0263"+
		"\u0264\3\2\2\2\u0264\u026e\5$\23\2\u0265\u0266\7 \2\2\u0266\u0267\58\35"+
		"\2\u0267\u0268\5N(\2\u0268\u0269\7!\2\2\u0269\u026e\3\2\2\2\u026a\u026e"+
		"\5\u009aN\2\u026b\u026e\5\u009eP\2\u026c\u026e\5\u00a4S\2\u026d\u024b"+
		"\3\2\2\2\u026d\u024d\3\2\2\2\u026d\u024e\3\2\2\2\u026d\u024f\3\2\2\2\u026d"+
		"\u0250\3\2\2\2\u026d\u0251\3\2\2\2\u026d\u0254\3\2\2\2\u026d\u0262\3\2"+
		"\2\2\u026d\u0265\3\2\2\2\u026d\u026a\3\2\2\2\u026d\u026b\3\2\2\2\u026d"+
		"\u026c\3\2\2\2\u026e\u027a\3\2\2\2\u026f\u0270\f\n\2\2\u0270\u0271\7\4"+
		"\2\2\u0271\u0279\5\\/\13\u0272\u0273\f\f\2\2\u0273\u0276\7\u0145\2\2\u0274"+
		"\u0277\7\u0214\2\2\u0275\u0277\58\35\2\u0276\u0274\3\2\2\2\u0276\u0275"+
		"\3\2\2\2\u0277\u0279\3\2\2\2\u0278\u026f\3\2\2\2\u0278\u0272\3\2\2\2\u0279"+
		"\u027c\3\2\2\2\u027a\u0278\3\2\2\2\u027a\u027b\3\2\2\2\u027b]\3\2\2\2"+
		"\u027c\u027a\3\2\2\2\u027d\u0282\5`\61\2\u027e\u0282\5d\63\2\u027f\u0282"+
		"\5h\65\2\u0280\u0282\5\u0096L\2\u0281\u027d\3\2\2\2\u0281\u027e\3\2\2"+
		"\2\u0281\u027f\3\2\2\2\u0281\u0280\3\2\2\2\u0282_\3\2\2\2\u0283\u0284"+
		"\5b\62\2\u0284\u0286\7\36\2\2\u0285\u0287\5l\67\2\u0286\u0285\3\2\2\2"+
		"\u0286\u0287\3\2\2\2\u0287\u0294\3\2\2\2\u0288\u028d\5N(\2\u0289\u028a"+
		"\7$\2\2\u028a\u028c\5N(\2\u028b\u0289\3\2\2\2\u028c\u028f\3\2\2\2\u028d"+
		"\u028b\3\2\2\2\u028d\u028e\3\2\2\2\u028e\u0295\3\2\2\2\u028f\u028d\3\2"+
		"\2\2\u0290\u0295\7\20\2\2\u0291\u0292\7\20\2\2\u0292\u0293\7$\2\2\u0293"+
		"\u0295\5N(\2\u0294\u0288\3\2\2\2\u0294\u0290\3\2\2\2\u0294\u0291\3\2\2"+
		"\2\u0294\u0295\3\2\2\2\u0295\u0296\3\2\2\2\u0296\u0298\7\37\2\2\u0297"+
		"\u0299\5n8\2\u0298\u0297\3\2\2\2\u0298\u0299\3\2\2\2\u0299a\3\2\2\2\u029a"+
		"\u029b\t\r\2\2\u029bc\3\2\2\2\u029c\u029d\5f\64\2\u029d\u02a7\7\36\2\2"+
		"\u029e\u02a3\5N(\2\u029f\u02a0\7$\2\2\u02a0\u02a2\5N(\2\u02a1\u029f\3"+
		"\2\2\2\u02a2\u02a5\3\2\2\2\u02a3\u02a1\3\2\2\2\u02a3\u02a4\3\2\2\2\u02a4"+
		"\u02a8\3\2\2\2\u02a5\u02a3\3\2\2\2\u02a6\u02a8\7\20\2\2\u02a7\u029e\3"+
		"\2\2\2\u02a7\u02a6\3\2\2\2\u02a7\u02a8\3\2\2\2\u02a8\u02a9\3\2\2\2\u02a9"+
		"\u02aa\7\37\2\2\u02aae\3\2\2\2\u02ab\u02ac\t\16\2\2\u02acg\3\2\2\2\u02ad"+
		"\u02ae\5j\66\2\u02ae\u02bb\7\36\2\2\u02af\u02b4\5N(\2\u02b0\u02b1\7$\2"+
		"\2\u02b1\u02b3\5N(\2\u02b2\u02b0\3\2\2\2\u02b3\u02b6\3\2\2\2\u02b4\u02b2"+
		"\3\2\2\2\u02b4\u02b5\3\2\2\2\u02b5\u02bc\3\2\2\2\u02b6\u02b4\3\2\2\2\u02b7"+
		"\u02bc\7\20\2\2\u02b8\u02b9\7\20\2\2\u02b9\u02ba\7$\2\2\u02ba\u02bc\5"+
		"N(\2\u02bb\u02af\3\2\2\2\u02bb\u02b7\3\2\2\2\u02bb\u02b8\3\2\2\2\u02bb"+
		"\u02bc\3\2\2\2\u02bc\u02bd\3\2\2\2\u02bd\u02be\7\37\2\2\u02bei\3\2\2\2"+
		"\u02bf\u02da\7\u00d9\2\2\u02c0\u02da\7\u00da\2\2\u02c1\u02da\7\u00db\2"+
		"\2\u02c2\u02da\7\u00dc\2\2\u02c3\u02da\7\u00dd\2\2\u02c4\u02da\7\u00de"+
		"\2\2\u02c5\u02da\7\u00df\2\2\u02c6\u02da\7\u00e0\2\2\u02c7\u02da\7\u00e1"+
		"\2\2\u02c8\u02c9\7\u00e2\2\2\u02c9\u02da\7\u00e3\2\2\u02ca\u02da\7\u00e4"+
		"\2\2\u02cb\u02da\7\u00e5\2\2\u02cc\u02da\7\u00e6\2\2\u02cd\u02da\7\u00e7"+
		"\2\2\u02ce\u02da\7\u00e8\2\2\u02cf\u02da\7\u00e9\2\2\u02d0\u02da\7\u00ea"+
		"\2\2\u02d1\u02da\7\u00eb\2\2\u02d2\u02da\7\u00ec\2\2\u02d3\u02da\7\u00ed"+
		"\2\2\u02d4\u02da\7\u00ee\2\2\u02d5\u02da\7\u00ef\2\2\u02d6\u02da\7\u00f0"+
		"\2\2\u02d7\u02da\7\u00f1\2\2\u02d8\u02da\7\u00f2\2\2\u02d9\u02bf\3\2\2"+
		"\2\u02d9\u02c0\3\2\2\2\u02d9\u02c1\3\2\2\2\u02d9\u02c2\3\2\2\2\u02d9\u02c3"+
		"\3\2\2\2\u02d9\u02c4\3\2\2\2\u02d9\u02c5\3\2\2\2\u02d9\u02c6\3\2\2\2\u02d9"+
		"\u02c7\3\2\2\2\u02d9\u02c8\3\2\2\2\u02d9\u02ca\3\2\2\2\u02d9\u02cb\3\2"+
		"\2\2\u02d9\u02cc\3\2\2\2\u02d9\u02cd\3\2\2\2\u02d9\u02ce\3\2\2\2\u02d9"+
		"\u02cf\3\2\2\2\u02d9\u02d0\3\2\2\2\u02d9\u02d1\3\2\2\2\u02d9\u02d2\3\2"+
		"\2\2\u02d9\u02d3\3\2\2\2\u02d9\u02d4\3\2\2\2\u02d9\u02d5\3\2\2\2\u02d9"+
		"\u02d6\3\2\2\2\u02d9\u02d7\3\2\2\2\u02d9\u02d8\3\2\2\2\u02dak\3\2\2\2"+
		"\u02db\u02dc\7K\2\2\u02dcm\3\2\2\2\u02dd\u02e3\7\u0172\2\2\u02de\u02df"+
		"\7\36\2\2\u02df\u02e0\5p9\2\u02e0\u02e1\7\37\2\2\u02e1\u02e4\3\2\2\2\u02e2"+
		"\u02e4\58\35\2\u02e3\u02de\3\2\2\2\u02e3\u02e2\3\2\2\2\u02e4o\3\2\2\2"+
		"\u02e5\u02e7\58\35\2\u02e6\u02e5\3\2\2\2\u02e6\u02e7\3\2\2\2\u02e7\u02e9"+
		"\3\2\2\2\u02e8\u02ea\5r:\2\u02e9\u02e8\3\2\2\2\u02e9\u02ea\3\2\2\2\u02ea"+
		"\u02ec\3\2\2\2\u02eb\u02ed\5\u00a8U\2\u02ec\u02eb\3\2\2\2\u02ec\u02ed"+
		"\3\2\2\2\u02ed\u02ef\3\2\2\2\u02ee\u02f0\5t;\2\u02ef\u02ee\3\2\2\2\u02ef"+
		"\u02f0\3\2\2\2\u02f0q\3\2\2\2\u02f1\u02f2\7\u011b\2\2\u02f2\u02f3\7r\2"+
		"\2\u02f3\u02f8\5N(\2\u02f4\u02f5\7$\2\2\u02f5\u02f7\5N(\2\u02f6\u02f4"+
		"\3\2\2\2\u02f7\u02fa\3\2\2\2\u02f8\u02f6\3\2\2\2\u02f8\u02f9\3\2\2\2\u02f9"+
		"s\3\2\2\2\u02fa\u02f8\3\2\2\2\u02fb\u02fe\t\17\2\2\u02fc\u02ff\5v<\2\u02fd"+
		"\u02ff\5z>\2\u02fe\u02fc\3\2\2\2\u02fe\u02fd\3\2\2\2\u02ffu\3\2\2\2\u0300"+
		"\u0301\7\u0096\2\2\u0301\u030d\7\u0115\2\2\u0302\u0303\7\u0124\2\2\u0303"+
		"\u030d\7\u0177\2\2\u0304\u0305\7\u0124\2\2\u0305\u030d\7\u015e\2\2\u0306"+
		"\u0307\5N(\2\u0307\u0308\7\u0177\2\2\u0308\u030d\3\2\2\2\u0309\u030a\5"+
		"N(\2\u030a\u030b\7\u015e\2\2\u030b\u030d\3\2\2\2\u030c\u0300\3\2\2\2\u030c"+
		"\u0302\3\2\2\2\u030c\u0304\3\2\2\2\u030c\u0306\3\2\2\2\u030c\u0309\3\2"+
		"\2\2\u030dw\3\2\2\2\u030e\u030f\5v<\2\u030fy\3\2\2\2\u0310\u0311\7k\2"+
		"\2\u0311\u0312\5v<\2\u0312\u0313\7c\2\2\u0313\u0314\5x=\2\u0314{\3\2\2"+
		"\2\u0315\u0320\5~@\2\u0316\u0320\5\u0080A\2\u0317\u0320\5\u0082B\2\u0318"+
		"\u0320\5\u0084C\2\u0319\u0320\5\u0086D\2\u031a\u0320\5\u0088E\2\u031b"+
		"\u0320\5\u008aF\2\u031c\u0320\5\u008cG\2\u031d\u0320\5\u008eH\2\u031e"+
		"\u0320\5\u0090I\2\u031f\u0315\3\2\2\2\u031f\u0316\3\2\2\2\u031f\u0317"+
		"\3\2\2\2\u031f\u0318\3\2\2\2\u031f\u0319\3\2\2\2\u031f\u031a\3\2\2\2\u031f"+
		"\u031b\3\2\2\2\u031f\u031c\3\2\2\2\u031f\u031d\3\2\2\2\u031f\u031e\3\2"+
		"\2\2\u0320}\3\2\2\2\u0321\u0322\7\u01b9\2\2\u0322\u0324\7\36\2\2\u0323"+
		"\u0325\5l\67\2\u0324\u0323\3\2\2\2\u0324\u0325\3\2\2\2\u0325\u032f\3\2"+
		"\2\2\u0326\u032b\5N(\2\u0327\u0328\7$\2\2\u0328\u032a\5N(\2\u0329\u0327"+
		"\3\2\2\2\u032a\u032d\3\2\2\2\u032b\u0329\3\2\2\2\u032b\u032c\3\2\2\2\u032c"+
		"\u0330\3\2\2\2\u032d\u032b\3\2\2\2\u032e\u0330\7\20\2\2\u032f\u0326\3"+
		"\2\2\2\u032f\u032e\3\2\2\2\u032f\u0330\3\2\2\2\u0330\u0332\3\2\2\2\u0331"+
		"\u0333\5\u00a8U\2\u0332\u0331\3\2\2\2\u0332\u0333\3\2\2\2\u0333\u0336"+
		"\3\2\2\2\u0334\u0335\7\u0189\2\2\u0335\u0337\5N(\2\u0336\u0334\3\2\2\2"+
		"\u0336\u0337\3\2\2\2\u0337\u0338\3\2\2\2\u0338\u0339\7\37\2\2\u0339\177"+
		"\3\2\2\2\u033a\u033b\58\35\2\u033b\u033c\7\36\2\2\u033c\u0341\5N(\2\u033d"+
		"\u033e\7$\2\2\u033e\u0340\5N(\2\u033f\u033d\3\2\2\2\u0340\u0343\3\2\2"+
		"\2\u0341\u033f\3\2\2\2\u0341\u0342\3\2\2\2\u0342\u0344\3\2\2\2\u0343\u0341"+
		"\3\2\2\2\u0344\u0345\7\37\2\2\u0345\u0346\5n8\2\u0346\u0081\3\2\2\2\u0347"+
		"\u0348\7N\2\2\u0348\u0349\7\36\2\2\u0349\u034a\5N(\2\u034a\u034b\7\\\2"+
		"\2\u034b\u034c\5\u00acW\2\u034c\u034d\7\37\2\2\u034d\u0083\3\2\2\2\u034e"+
		"\u034f\7\u014c\2\2\u034f\u0350\7\36\2\2\u0350\u0351\5N(\2\u0351\u0352"+
		"\7$\2\2\u0352\u0353\5\u00acW\2\u0353\u0354\7\37\2\2\u0354\u035d\3\2\2"+
		"\2\u0355\u0356\7\u014c\2\2\u0356\u0357\7\36\2\2\u0357\u0358\5N(\2\u0358"+
		"\u0359\7Z\2\2\u0359\u035a\58\35\2\u035a\u035b\7\37\2\2\u035b\u035d\3\2"+
		"\2\2\u035c\u034e\3\2\2\2\u035c\u0355\3\2\2\2\u035d\u0085\3\2\2\2\u035e"+
		"\u035f\7A\2\2\u035f\u0360\7\36\2\2\u0360\u0361\5N(\2\u0361\u0362\7l\2"+
		"\2\u0362\u0363\5N(\2\u0363\u0364\7\37\2\2\u0364\u0087\3\2\2\2\u0365\u0366"+
		"\t\20\2\2\u0366\u0367\7\36\2\2\u0367\u0368\5N(\2\u0368\u0369\7Q\2\2\u0369"+
		"\u036c\7\u0215\2\2\u036a\u036b\7a\2\2\u036b\u036d\7\u0215\2\2\u036c\u036a"+
		"\3\2\2\2\u036c\u036d\3\2\2\2\u036d\u036e\3\2\2\2\u036e\u036f\7\37\2\2"+
		"\u036f\u0089\3\2\2\2\u0370\u0371\7\u015b\2\2\u0371\u0372\7\36\2\2\u0372"+
		"\u0373\58\35\2\u0373\u0374\7Q\2\2\u0374\u0375\5N(\2\u0375\u0376\7\37\2"+
		"\2\u0376\u008b\3\2\2\2\u0377\u0378\7~\2\2\u0378\u0379\7\36\2\2\u0379\u037e"+
		"\5N(\2\u037a\u037b\7$\2\2\u037b\u037d\5N(\2\u037c\u037a\3\2\2\2\u037d"+
		"\u0380\3\2\2\2\u037e\u037c\3\2\2\2\u037e\u037f\3\2\2\2\u037f\u0383\3\2"+
		"\2\2\u0380\u037e\3\2\2\2\u0381\u0382\7Z\2\2\u0382\u0384\5\u00b6\\\2\u0383"+
		"\u0381\3\2\2\2\u0383\u0384\3\2\2\2\u0384\u0385\3\2\2\2\u0385\u0386\7\37"+
		"\2\2\u0386\u008d\3\2\2\2\u0387\u0388\7O\2\2\u0388\u0389\7\36\2\2\u0389"+
		"\u038a\t\21\2\2\u038a\u038b\7\u0214\2\2\u038b\u038c\7Q\2\2\u038c\u038d"+
		"\7\u0214\2\2\u038d\u038e\7\37\2\2\u038e\u008f\3\2\2\2\u038f\u0390\7\u01ad"+
		"\2\2\u0390\u0391\7\36\2\2\u0391\u0394\5N(\2\u0392\u0393\7\\\2\2\u0393"+
		"\u0395\5\u00acW\2\u0394\u0392\3\2\2\2\u0394\u0395\3\2\2\2\u0395\u0397"+
		"\3\2\2\2\u0396\u0398\5\u0092J\2\u0397\u0396\3\2\2\2\u0397\u0398\3\2\2"+
		"\2\u0398\u0399\3\2\2\2\u0399\u039a\7\37\2\2\u039a\u0091\3\2\2\2\u039b"+
		"\u03a7\7\u0133\2\2\u039c\u03a1\5\u0094K\2\u039d\u039e\7$\2\2\u039e\u03a0"+
		"\5\u0094K\2\u039f\u039d\3\2\2\2\u03a0\u03a3\3\2\2\2\u03a1\u039f\3\2\2"+
		"\2\u03a1\u03a2\3\2\2\2\u03a2\u03a8\3\2\2\2\u03a3\u03a1\3\2\2\2\u03a4\u03a5"+
		"\7\u0215\2\2\u03a5\u03a6\7\17\2\2\u03a6\u03a8\7\u0215\2\2\u03a7\u039c"+
		"\3\2\2\2\u03a7\u03a4\3\2\2\2\u03a8\u0093\3\2\2\2\u03a9\u03ab\7\u0215\2"+
		"\2\u03aa\u03ac\t\22\2\2\u03ab\u03aa\3\2\2\2\u03ab\u03ac\3\2\2\2\u03ac"+
		"\u03ae\3\2\2\2\u03ad\u03af\7\u0187\2\2\u03ae\u03ad\3\2\2\2\u03ae\u03af"+
		"\3\2\2\2\u03af\u0095\3\2\2\2\u03b0\u03b1\5\u0098M\2\u03b1\u03bb\7\36\2"+
		"\2\u03b2\u03b7\5N(\2\u03b3\u03b4\7$\2\2\u03b4\u03b6\5N(\2\u03b5\u03b3"+
		"\3\2\2\2\u03b6\u03b9\3\2\2\2\u03b7\u03b5\3\2\2\2\u03b7\u03b8\3\2\2\2\u03b8"+
		"\u03bc\3\2\2\2\u03b9\u03b7\3\2\2\2\u03ba\u03bc\7\20\2\2\u03bb\u03b2\3"+
		"\2\2\2\u03bb\u03ba\3\2\2\2\u03bb\u03bc\3\2\2\2\u03bc\u03bd\3\2\2\2\u03bd"+
		"\u03be\7\37\2\2\u03be\u0097\3\2\2\2\u03bf\u03c9\58\35\2\u03c0\u03c9\7"+
		"^\2\2\u03c1\u03c9\7\u01c9\2\2\u03c2\u03c9\7\u0085\2\2\u03c3\u03c9\7\u0086"+
		"\2\2\u03c4\u03c9\7\u016e\2\2\u03c5\u03c9\7\u0104\2\2\u03c6\u03c9\7\u0081"+
		"\2\2\u03c7\u03c9\7P\2\2\u03c8\u03bf\3\2\2\2\u03c8\u03c0\3\2\2\2\u03c8"+
		"\u03c1\3\2\2\2\u03c8\u03c2\3\2\2\2\u03c8\u03c3\3\2\2\2\u03c8\u03c4\3\2"+
		"\2\2\u03c8\u03c5\3\2\2\2\u03c8\u03c6\3\2\2\2\u03c8\u03c7\3\2\2\2\u03c9"+
		"\u0099\3\2\2\2\u03ca\u03cb\7\u016b\2\2\u03cb\u03cc\5J&\2\u03cc\u03cd\7"+
		"\u0195\2\2\u03cd\u03cf\5N(\2\u03ce\u03d0\5\u009cO\2\u03cf\u03ce\3\2\2"+
		"\2\u03cf\u03d0\3\2\2\2\u03d0\u009b\3\2\2\2\u03d1\u03d2\7l\2\2\u03d2\u03d3"+
		"\7R\2\2\u03d3\u03d4\7\u0196\2\2\u03d4\u03e3\7\u00d0\2\2\u03d5\u03d6\7"+
		"l\2\2\u03d6\u03d7\7R\2\2\u03d7\u03d8\7\u0196\2\2\u03d8\u03d9\7\u00d0\2"+
		"\2\u03d9\u03da\7I\2\2\u03da\u03db\7\u00be\2\2\u03db\u03e3\7\u0198\2\2"+
		"\u03dc\u03dd\7l\2\2\u03dd\u03de\7|\2\2\u03de\u03e3\7\u00d0\2\2\u03df\u03e0"+
		"\7I\2\2\u03e0\u03e1\7\u00be\2\2\u03e1\u03e3\7\u0198\2\2\u03e2\u03d1\3"+
		"\2\2\2\u03e2\u03d5\3\2\2\2\u03e2\u03dc\3\2\2\2\u03e2\u03df\3\2\2\2\u03e3"+
		"\u009d\3\2\2\2\u03e4\u03e6\7L\2\2\u03e5\u03e7\5\\/\2\u03e6\u03e5\3\2\2"+
		"\2\u03e6\u03e7\3\2\2\2\u03e7\u03e9\3\2\2\2\u03e8\u03ea\5\u00a0Q\2\u03e9"+
		"\u03e8\3\2\2\2\u03ea\u03eb\3\2\2\2\u03eb\u03e9\3\2\2\2\u03eb\u03ec\3\2"+
		"\2\2\u03ec\u03ee\3\2\2\2\u03ed\u03ef\5\u00a2R\2\u03ee\u03ed\3\2\2\2\u03ee"+
		"\u03ef\3\2\2\2\u03ef\u03f0\3\2\2\2\u03f0\u03f1\7\u00ad\2\2\u03f1\u009f"+
		"\3\2\2\2\u03f2\u03f3\7M\2\2\u03f3\u03f4\5N(\2\u03f4\u03f5\7`\2\2\u03f5"+
		"\u03f6\5N(\2\u03f6\u00a1\3\2\2\2\u03f7\u03f8\7_\2\2\u03f8\u03f9\5N(\2"+
		"\u03f9\u00a3\3\2\2\2\u03fa\u03fb\7\u0081\2\2\u03fb\u03fc\5N(\2\u03fc\u03fd"+
		"\5\u00a6T\2\u03fd\u00a5\3\2\2\2\u03fe\u03ff\t\23\2\2\u03ff\u00a7\3\2\2"+
		"\2\u0400\u0401\7p\2\2\u0401\u0402\7r\2\2\u0402\u0407\5\u00aaV\2\u0403"+
		"\u0404\7$\2\2\u0404\u0406\5\u00aaV\2\u0405\u0403\3\2\2\2\u0406\u0409\3"+
		"\2\2\2\u0407\u0405\3\2\2\2\u0407\u0408\3\2\2\2\u0408\u00a9\3\2\2\2\u0409"+
		"\u0407\3\2\2\2\u040a\u040e\5D#\2\u040b\u040e\5,\27\2\u040c\u040e\5N(\2"+
		"\u040d\u040a\3\2\2\2\u040d\u040b\3\2\2\2\u040d\u040c\3\2\2\2\u040e\u0410"+
		"\3\2\2\2\u040f\u0411\t\22\2\2\u0410\u040f\3\2\2\2\u0410\u0411\3\2\2\2"+
		"\u0411\u00ab\3\2\2\2\u0412\u0414\5\u00aeX\2\u0413\u0415\5\u00b0Y\2\u0414"+
		"\u0413\3\2\2\2\u0414\u0415\3\2\2\2\u0415\u0417\3\2\2\2\u0416\u0418\5\u00b2"+
		"Z\2\u0417\u0416\3\2\2\2\u0417\u0418\3\2\2\2\u0418\u041a\3\2\2\2\u0419"+
		"\u041b\5\u00b4[\2\u041a\u0419\3\2\2\2\u041a\u041b\3\2\2\2\u041b\u041d"+
		"\3\2\2\2\u041c\u041e\7\u0126\2\2\u041d\u041c\3\2\2\2\u041d\u041e\3\2\2"+
		"\2\u041e\u0420\3\2\2\2\u041f\u0421\7\u0190\2\2\u0420\u041f\3\2\2\2\u0420"+
		"\u0421\3\2\2\2\u0421\u0434\3\2\2\2\u0422\u0423\5\u00aeX\2\u0423\u0424"+
		"\7\36\2\2\u0424\u0429\7\u0214\2\2\u0425\u0426\7$\2\2\u0426\u0428\7\u0214"+
		"\2\2\u0427\u0425\3\2\2\2\u0428\u042b\3\2\2\2\u0429\u0427\3\2\2\2\u0429"+
		"\u042a\3\2\2\2\u042a\u042c\3\2\2\2\u042b\u0429\3\2\2\2\u042c\u042e\7\37"+
		"\2\2\u042d\u042f\5\u00b2Z\2\u042e\u042d\3\2\2\2\u042e\u042f\3\2\2\2\u042f"+
		"\u0431\3\2\2\2\u0430\u0432\5\u00b4[\2\u0431\u0430\3\2\2\2\u0431\u0432"+
		"\3\2\2\2\u0432\u0434\3\2\2\2\u0433\u0412\3\2\2\2\u0433\u0422\3\2\2\2\u0434"+
		"\u00ad\3\2\2\2\u0435\u0437\58\35\2\u0436\u0438\58\35\2\u0437\u0436\3\2"+
		"\2\2\u0437\u0438\3\2\2\2\u0438\u00af\3\2\2\2\u0439\u043a\7\36\2\2\u043a"+
		"\u043d\7\u0215\2\2\u043b\u043c\7$\2\2\u043c\u043e\7\u0215\2\2\u043d\u043b"+
		"\3\2\2\2\u043d\u043e\3\2\2\2\u043e\u043f\3\2\2\2\u043f\u0440\7\37\2\2"+
		"\u0440\u00b1\3\2\2\2\u0441\u0442\t\24\2\2\u0442\u0444\78\2\2\u0443\u0445"+
		"\7\27\2\2\u0444\u0443\3\2\2\2\u0444\u0445\3\2\2\2\u0445\u0446\3\2\2\2"+
		"\u0446\u0447\5\u00b6\\\2\u0447\u00b3\3\2\2\2\u0448\u044a\7\u0145\2\2\u0449"+
		"\u044b\7\27\2\2\u044a\u0449\3\2\2\2\u044a\u044b\3\2\2\2\u044b\u044e\3"+
		"\2\2\2\u044c\u044f\7\u0214\2\2\u044d\u044f\5\u00b6\\\2\u044e\u044c\3\2"+
		"\2\2\u044e\u044d\3\2\2\2\u044f\u00b5\3\2\2\2\u0450\u0453\58\35\2\u0451"+
		"\u0452\7\23\2\2\u0452\u0454\58\35\2\u0453\u0451\3\2\2\2\u0453\u0454\3"+
		"\2\2\2\u0454\u00b7\3\2\2\2\u0081\u00be\u00c1\u00c4\u00c7\u00cb\u00d1\u00d6"+
		"\u00d9\u00dc\u00df\u00e3\u00f2\u00fb\u00fe\u0102\u0108\u0114\u0119\u011f"+
		"\u0126\u012a\u012e\u0134\u0137\u0142\u0145\u0149\u014c\u0157\u015a\u015e"+
		"\u0161\u0165\u016d\u0170\u0173\u0176\u0179\u0188\u0193\u019a\u01a1\u01a5"+
		"\u01b2\u01bb\u01bd\u01ca\u01d9\u01db\u01e2\u01e9\u01f2\u01f9\u0207\u020d"+
		"\u0211\u0217\u0246\u0248\u0254\u025c\u0262\u026d\u0276\u0278\u027a\u0281"+
		"\u0286\u028d\u0294\u0298\u02a3\u02a7\u02b4\u02bb\u02d9\u02e3\u02e6\u02e9"+
		"\u02ec\u02ef\u02f8\u02fe\u030c\u031f\u0324\u032b\u032f\u0332\u0336\u0341"+
		"\u035c\u036c\u037e\u0383\u0394\u0397\u03a1\u03a7\u03ab\u03ae\u03b7\u03bb"+
		"\u03c8\u03cf\u03e2\u03e6\u03eb\u03ee\u0407\u040d\u0410\u0414\u0417\u041a"+
		"\u041d\u0420\u0429\u042e\u0431\u0433\u0437\u043d\u0444\u044a\u044e\u0453";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}