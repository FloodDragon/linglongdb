
package com.linglong.sql.statement;

import com.linglong.sql.segment.LinglongSQLSegment;
import com.google.common.base.Optional;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Stereo
 */
public abstract class AbstractStatement implements LinglongSQLStatement {

    private final Collection<LinglongSQLSegment> sqlSegments = new LinkedList<>();

    @Override
    public final Collection<LinglongSQLSegment> getAllSQLSegments() {
        return sqlSegments;
    }

    @Override
    public final <T extends LinglongSQLSegment> Optional<T> findSQLSegment(final Class<T> sqlSegmentType) {
        for (LinglongSQLSegment each : sqlSegments) {
            if (sqlSegmentType.isAssignableFrom(each.getClass())) {
                return Optional.of((T) each);
            }
        }
        return Optional.absent();
    }

    @Override
    public final <T extends LinglongSQLSegment> Collection<T> findSQLSegments(final Class<T> sqlSegmentType) {
        Collection<T> result = new LinkedList<>();
        for (LinglongSQLSegment each : sqlSegments) {
            if (sqlSegmentType.isAssignableFrom(each.getClass())) {
                result.add((T) each);
            }
        }
        return result;
    }
}
