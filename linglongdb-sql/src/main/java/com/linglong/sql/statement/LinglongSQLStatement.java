
package com.linglong.sql.statement;

import com.linglong.sql.segment.LinglongSQLSegment;
import com.google.common.base.Optional;

import java.util.Collection;

public interface LinglongSQLStatement {
    
    Collection<LinglongSQLSegment> getAllSQLSegments();
    
    <T extends LinglongSQLSegment> Optional<T> findSQLSegment(Class<T> sqlSegmentType);
    
    <T extends LinglongSQLSegment> Collection<T> findSQLSegments(Class<T> sqlSegmentType);
}
