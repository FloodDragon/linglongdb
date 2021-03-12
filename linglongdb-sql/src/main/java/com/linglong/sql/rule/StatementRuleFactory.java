package com.linglong.sql.rule;

/**
 * @author Stereo
 */
public final class StatementRuleFactory {

    private static final StatementRule[] StatementRules = {
            new SelectStatementRule()
    };

    public static StatementRule getStatementRule(String contextName) {
        for (StatementRule statementRule : StatementRules) {
            if (statementRule.getContextName().equals(contextName)) {
                return statementRule;

            }
        }
        return null;
    }
}
