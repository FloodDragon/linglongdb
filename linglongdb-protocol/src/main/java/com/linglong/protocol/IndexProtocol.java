package com.linglong.protocol;

import com.linglong.protocol.message.QueryRequest;
import com.linglong.protocol.message.QueryResponse;
import com.linglong.protocol.message.WriteRequest;
import com.linglong.protocol.message.WriteResponse;

/**
 * @author Stereo on 2021/3/8.
 */
public interface IndexProtocol {

    WriteResponse write(WriteRequest request);

    QueryResponse query(QueryRequest request);
}
