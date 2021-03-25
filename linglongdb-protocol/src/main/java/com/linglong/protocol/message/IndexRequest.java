package com.linglong.protocol.message;

import com.linglong.rpc.serialization.msgpack.BeanMessage;

/**
 * @author Stereo on 2021/3/8.
 */
public class IndexRequest extends Request implements BeanMessage {
    private String indexName;

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
