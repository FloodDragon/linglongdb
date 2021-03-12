package com.linglong.sql.segment.item;

/**
 * @author Stereo on 2019/10/8.
 */
public class FunctionSelectItemSegment implements SelectItemSegment {

    private final int startIndex;

    private final int stopIndex;

    private final String text;

    private String alias;

    public FunctionSelectItemSegment(int startIndex, int stopIndex, String text) {
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

    @Override
    public String getText() {
        return text;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }
}
