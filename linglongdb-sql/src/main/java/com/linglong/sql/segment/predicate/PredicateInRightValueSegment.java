package com.linglong.sql.segment.predicate;

import com.linglong.sql.segment.LinglongSQLSegment;

import java.util.Collection;

/**
 * @author Stereo on 2019/10/10.
 */
public class PredicateInRightValueSegment implements LinglongSQLSegment, PredicateRightValue {

    private final Collection<ExpressionSegment> sqlExpressions;

    public PredicateInRightValueSegment(Collection<ExpressionSegment> sqlExpressions) {
        this.sqlExpressions = sqlExpressions;
    }

    @Override
    public int getStartIndex() {
        return 0;
    }

    @Override
    public int getStopIndex() {
        return 0;
    }

    public Collection<ExpressionSegment> getSqlExpressions() {
        return sqlExpressions;
    }
}
