package com.linglong.protocol.message;

/**
 * @author Stereo on 2021/3/8.
 */
public class IndexRequest extends Request {
    private String indexName;

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
