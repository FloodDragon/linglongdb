package com.linglong.sql.segment.item;

import com.linglong.sql.segment.LinglongSQLSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Stereo on 2019/10/8.
 */
public class SelectItemsSegment implements LinglongSQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final Collection<SelectItemSegment> selectItems = new LinkedList<>();

    public SelectItemsSegment(int startIndex, int stopIndex) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    public Collection<SelectItemSegment> getSelectItems() {
        return selectItems;
    }
}
