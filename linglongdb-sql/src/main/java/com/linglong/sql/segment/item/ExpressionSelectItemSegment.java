package com.linglong.sql.segment.item;

import com.linglong.sql.util.SQLUtil;
import com.google.common.base.Optional;

/**
 * @author Stereo on 2019/10/9.
 */
public final class ExpressionSelectItemSegment implements SelectItemSegment {

    private final int startIndex;

    private final int stopIndex;

    private final String text;

    private String alias;

    public ExpressionSelectItemSegment(final int startIndex, final int stopIndex, final String text) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.text = text;
    }

    public Optional<String> getAlias() {
        return Optional.fromNullable(alias);
    }

    public void setAlias(final String alias) {
        this.alias = SQLUtil.getExactlyValue(alias);
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
}
