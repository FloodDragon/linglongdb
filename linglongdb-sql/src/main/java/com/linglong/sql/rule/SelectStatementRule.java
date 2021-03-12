package com.linglong.sql.rule;

import com.linglong.sql.extractor.select.SelectExtractor;
import com.google.common.base.CaseFormat;

/**
 * @author Stereo
 */
public class SelectStatementRule implements StatementRule {

    public String getContextName() {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, "selectClause" + "Context");
    }

    @Override
    public SelectExtractor getExtractor() {
        return new SelectExtractor();
    }

    @Override
    public RuleType getRuleType() {
        return RuleType.SELECT;
    }
}
