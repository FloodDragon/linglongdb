package com.linglong.sql.extractor;

import com.linglong.sql.segment.LinglongSQLSegment;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * @author Stereo
 */
public interface OptionalSQLSegmentExtractor extends LinglongSQLSegmentExtractor {
    
    Optional<? extends LinglongSQLSegment> extract(ParserRuleContext ancestorNode);
}
