package com.linglong.sql;

import com.linglong.sql.exception.LinglongSQLParsingException;
import com.linglong.sql.extractor.LinglongSQLSegmentExtractor;
import com.linglong.sql.extractor.select.SelectExtractor;
import com.linglong.sql.rule.StatementRule;
import com.linglong.sql.segment.complex.SelectSegment;
import com.linglong.sql.statement.LinglongSQLStatement;
import com.linglong.sql.statement.SelectStatement;
import com.google.common.base.Optional;


/**
 * @author Stereo on 2019/10/11.
 */
public final class LinglongSQLExtractorEngine {


    public LinglongSQLStatement extract(final LinglongSQLAST ast) {
        StatementRule rule = ast.getStatementRule();
        LinglongSQLSegmentExtractor extractor = ast.getStatementRule().getExtractor();
        switch (rule.getRuleType()) {
            case SELECT:
                Optional<SelectSegment> selectSegmentOptional = ((SelectExtractor) extractor).extract(ast.getParserRuleContext());
                if (selectSegmentOptional.isPresent()) {
                    return new SelectStatement(selectSegmentOptional.get());
                } else {
                    throw new LinglongSQLParsingException("Linglong Sql extract Error");
                }
            default:
                throw new LinglongSQLParsingException(String.format("Linglong Sql Extract Unsupported of Rule <`%s`>", rule.getRuleType().name()));
        }
    }
}
