package com.linglong.sql.rule;

import com.linglong.sql.extractor.LinglongSQLSegmentExtractor;

/**
 * @author Stereo
 */
public interface StatementRule {
    String getContextName();

    LinglongSQLSegmentExtractor getExtractor();

    RuleType getRuleType();
}
