package com.linglong.sql;

import com.linglong.sql.exception.LinglongSQLParsingException;
import com.linglong.sql.rule.StatementRule;
import com.linglong.sql.rule.StatementRuleFactory;
import org.antlr.v4.runtime.ParserRuleContext;


/**
 * @author Stereo
 */
public final class LinglongSQLAST {

    private final String sql;
    private final StatementRule statementRule;
    private final ParserRuleContext parserRuleContext;

    public LinglongSQLAST(
            String sql,
            ParserRuleContext parserRuleContext) {
        this.sql = sql;
        this.parserRuleContext = parserRuleContext;
        String contextName = parserRuleContext.getClass().getSimpleName();
        this.statementRule = StatementRuleFactory.getStatementRule(contextName);
        if (null == statementRule) {
            throw new LinglongSQLParsingException(String.format("Unsupported SQL of `%s`", sql));
        }
    }

    public ParserRuleContext getParserRuleContext() {
        return parserRuleContext;
    }

    public String getSql() {
        return sql;
    }

    public StatementRule getStatementRule() {
        return statementRule;
    }
}
