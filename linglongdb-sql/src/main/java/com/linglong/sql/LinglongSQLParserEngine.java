package com.linglong.sql;

import com.linglong.sql.antlr4.LinglongSQLStatementLexer;
import com.linglong.sql.antlr4.LinglongSQLStatementParser;
import com.linglong.sql.exception.LinglongSQLParsingException;
import com.linglong.sql.listener.LinglongSQLErrorListener;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * @author Stereo
 */
public final class LinglongSQLParserEngine {

    private final String sql;
    private LinglongSQLStatementLexer lexer;
    private LinglongSQLStatementParser parser;

    public LinglongSQLParserEngine(String sql) {
        this.sql = sql;
    }

    public LinglongSQLAST parse() {
        this.lexer = new LinglongSQLStatementLexer(CharStreams.fromString(sql));
        this.parser = new LinglongSQLStatementParser(new CommonTokenStream(lexer));
        this.parser.addErrorListener(new LinglongSQLErrorListener());
        ParseTree parseTree = parser.execute().getChild(0);
        if (parseTree instanceof ErrorNode) {
            throw new LinglongSQLParsingException(String.format("Unsupported SQL of `%s`", sql));
        }
        return new LinglongSQLAST(sql, (ParserRuleContext) parseTree);
    }


    public LinglongSQLStatementLexer getLexer() {
        return lexer;
    }

    public LinglongSQLStatementParser getParser() {
        return parser;
    }

    public String getSql() {
        return sql;
    }
}
