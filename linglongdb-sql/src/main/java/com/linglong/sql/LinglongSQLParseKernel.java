package com.linglong.sql;

import com.linglong.sql.statement.LinglongSQLStatement;

/**
 * @author Stereo on 2019/10/11.
 */
public final class LinglongSQLParseKernel {

    private final LinglongSQLParserEngine linglongSQLParserEngine;
    private final LinglongSQLExtractorEngine linglongSQLExtractorEngine;


    public LinglongSQLParseKernel(final String sql) {
        linglongSQLParserEngine = new LinglongSQLParserEngine(sql);
        linglongSQLExtractorEngine = new LinglongSQLExtractorEngine();
    }

    public LinglongSQLStatement parse() {
        LinglongSQLAST linglongSQLAST = linglongSQLParserEngine.parse();
        LinglongSQLStatement linglongSQLStatement = linglongSQLExtractorEngine.extract(linglongSQLAST);
        return linglongSQLStatement;
    }

    public LinglongSQLExtractorEngine getExtractorEngine() {
        return linglongSQLExtractorEngine;
    }

    public LinglongSQLParserEngine getParserEngine() {
        return linglongSQLParserEngine;
    }
}
