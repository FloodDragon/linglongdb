package com.linglong.sql.segment.generic;

import com.linglong.sql.segment.LinglongSQLSegment;
import com.linglong.sql.segment.complex.SubquerySegment;
import com.linglong.sql.util.QuoteCharacter;
import com.linglong.sql.util.SQLUtil;

/**
 * @author Stereo on 2019/10/9.
 */
public class TableSegment implements LinglongSQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final String name;

    private String rpName;

    private String dbName;

    private QuoteCharacter quoteCharacter;

    private final boolean isSubquery;

    private final SubquerySegment subquerySegment;

    public TableSegment(int startIndex, int stopIndex, String name) {
        this(startIndex, stopIndex, name, null, false);
        this.quoteCharacter = QuoteCharacter.getQuoteCharacter(name);
    }

    public TableSegment(int startIndex, int stopIndex, SubquerySegment subquerySegment) {
        this(startIndex, stopIndex, null, subquerySegment, true);

    }

    public TableSegment(int startIndex, int stopIndex, String name, SubquerySegment subquerySegment, boolean isSubquery) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.subquerySegment = subquerySegment;
        this.name = SQLUtil.getExactlyValue(name);
        this.isSubquery = isSubquery;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    public QuoteCharacter getQuoteCharacter() {
        return quoteCharacter;
    }

    public String getName() {
        return name;
    }

    public SubquerySegment getSubquerySegment() {
        return subquerySegment;
    }

    public String getDbName() {
        return dbName;
    }

    public String getRpName() {
        return rpName;
    }

    public void setDbName(String dbName) {
        this.dbName = SQLUtil.getExactlyValue(dbName);
    }

    public void setRpName(String rpName) {
        this.rpName = SQLUtil.getExactlyValue(rpName);
    }

    public boolean isSubquery() {
        return isSubquery;
    }
}
