package com.linglong.sql.segment.predicate;

/**
 * @author Stereo on 2019/10/9.
 */
public class CommonExpressionSegment implements ExpressionSegment {

    private final int startIndex;

    private final int stopIndex;

    private final String text;

    public CommonExpressionSegment(int startIndex, int stopIndex, String text) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.text = text;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    public String getText() {
        return text;
    }
}
