package com.linglong.sql.segment.predicate;

import com.linglong.sql.segment.LinglongSQLSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Stereo on 2019/10/9.
 */
public class WhereSegment implements LinglongSQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final Collection<AndPredicateSegment> andPredicateSegments = new LinkedList<>();


    public WhereSegment(int startIndex, int stopIndex) {
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

    public Collection<AndPredicateSegment> getAndPredicateSegments() {
        return andPredicateSegments;
    }
}
