package com.linglong.sql.segment.generic;

import com.linglong.sql.segment.LinglongSQLSegment;

import java.util.Collection;

/**
 * @author Stereo on 2019/10/11.
 */
public class TablesSegment implements LinglongSQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final Collection<TableSegment> tableSegments;

    public TablesSegment(int startIndex, int stopIndex, Collection<TableSegment> tableSegments) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.tableSegments = tableSegments;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    public Collection<TableSegment> getTableSegments() {
        return tableSegments;
    }

    public boolean has() {
        return !tableSegments.isEmpty();
    }
}
