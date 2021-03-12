package com.linglong.sql.segment.predicate;

import com.linglong.sql.segment.LinglongSQLSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Stereo on 2019/10/9.
 */
public class OrPredicateSegment implements LinglongSQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final Collection<AndPredicateSegment> andPredicates = new LinkedList<>();

    public OrPredicateSegment() {
        this.startIndex = 0;
        this.stopIndex = 0;
    }

    public OrPredicateSegment(int startIndex, int stopIndex) {
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


    public Collection<AndPredicateSegment> getAndPredicates() {
        return andPredicates;
    }
}
