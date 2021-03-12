package com.linglong.sql;

import com.linglong.sql.statement.InfluxSQLStatement;

/**
 * @author Stereo on 2019/10/11.
 */
public final class LinglongSQLParseKernel {

    private final LinglongSQLParserEngine influxSQLParserEngine;
    private final LinglongSQLExtractorEngine influxSQLExtractorEngine;


    public LinglongSQLParseKernel(final String sql) {
        influxSQLParserEngine = new LinglongSQLParserEngine(sql);
        influxSQLExtractorEngine = new LinglongSQLExtractorEngine();
    }

    public InfluxSQLStatement parse() {
        LinglongSQLAST influxSQLAST = influxSQLParserEngine.parse();
        InfluxSQLStatement influxSQLStatement = influxSQLExtractorEngine.extract(influxSQLAST);
        return influxSQLStatement;
    }

    public LinglongSQLExtractorEngine getExtractorEngine() {
        return influxSQLExtractorEngine;
    }

    public LinglongSQLParserEngine getParserEngine() {
        return influxSQLParserEngine;
    }
}
