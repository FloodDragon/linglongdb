package com.linglong.sql.segment.predicate;

import com.linglong.sql.segment.LinglongSQLSegment;
import com.linglong.sql.util.QuoteCharacter;
import com.linglong.sql.util.SQLUtil;

/**
 * @author Stereo on 2019/10/9.
 */
public class ColumnWhereSegment implements PredicateRightValue, LinglongSQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final String name;

    private String operator;

    private final QuoteCharacter quoteCharacter;

    public ColumnWhereSegment(final int startIndex, final int stopIndex, final String name) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.name = SQLUtil.getExactlyValue(name);
        this.quoteCharacter = QuoteCharacter.getQuoteCharacter(name);
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    public String getName() {
        return name;
    }

    public ColumnWhereSegment operator(String operator) {
        this.operator = operator;
        return this;
    }

    public String getOperator() {
        return operator;
    }

    public QuoteCharacter getQuoteCharacter() {
        return quoteCharacter;
    }
}
