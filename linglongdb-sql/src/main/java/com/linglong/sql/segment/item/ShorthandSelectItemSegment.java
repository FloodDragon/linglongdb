package com.linglong.sql.segment.item;

/**
 * @author Stereo on 2019/10/8.
 */
public class ShorthandSelectItemSegment implements SelectItemSegment {

    private final int startIndex;

    private final int stopIndex;

    private final String text;

    public ShorthandSelectItemSegment(int startIndex, int stopIndex, String text) {
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
}
