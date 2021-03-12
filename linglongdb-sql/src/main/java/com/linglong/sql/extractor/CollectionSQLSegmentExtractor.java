
package com.linglong.sql.extractor;

import com.linglong.sql.segment.LinglongSQLSegment;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;


/**
 * @author Stereo
 */
public interface CollectionSQLSegmentExtractor extends LinglongSQLSegmentExtractor {

    Collection<? extends LinglongSQLSegment> extract(ParserRuleContext ancestorNode);
}
