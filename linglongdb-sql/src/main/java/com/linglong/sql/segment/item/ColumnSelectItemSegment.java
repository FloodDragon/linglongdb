package com.linglong.sql.segment.item;

import com.linglong.sql.util.QuoteCharacter;
import com.linglong.sql.util.SQLUtil;
import com.google.common.base.Optional;

/**
 * @author Stereo on 2019/10/8.
 */
public class ColumnSelectItemSegment implements SelectItemSegment {

    private final String text;

    private final int startIndex;

    private final int stopIndex;

    private final String name;

    private final QuoteCharacter quoteCharacter;

    private String alias;

    private ColumnSelectItemSegment columnSelectItemSegment;

    public ColumnSelectItemSegment(int startIndex, int stopIndex, String text, String name) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.text = text;
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

    @Override
    public String getText() {
        return text;
    }

    public void setColumnSelectItemSegment(ColumnSelectItemSegment columnSelectItemSegment) {
        this.columnSelectItemSegment = columnSelectItemSegment;
    }

    public String getName() {
        return name;
    }

    public QuoteCharacter getQuoteCharacter() {
        return quoteCharacter;
    }

    public ColumnSelectItemSegment getColumnSelectItemSegment() {
        return columnSelectItemSegment;
    }

    public Optional<String> getAlias() {
        return Optional.fromNullable(alias);
    }

    public void setAlias(final String alias) {
        this.alias = SQLUtil.getExactlyValue(alias);
    }
}
