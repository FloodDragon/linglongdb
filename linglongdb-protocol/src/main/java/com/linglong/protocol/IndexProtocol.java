package com.linglong.protocol;

import com.linglong.protocol.message.*;

/**
 * @author Stereo on 2021/3/8.
 */
public interface IndexProtocol {

    /**
     * 索引写操作
     *
     * @param request
     * @return
     */
    WriteResponse write(WriteRequest request);

    /**
     * 索引读操作
     *
     * @param request
     * @return
     */
    QueryResponse query(QueryRequest request);

    /**
     * 索引销毁
     *
     * @param request
     * @return
     */
    IndexDropResponse drop(IndexRequest request);
}
