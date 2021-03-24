package com.linglong.server.database.process;

/**
 * Created by liuj-ai on 2021/3/24.
 */
public class _IndexName {

    String idxName;
    String newName;

    public _IndexName indexName(String name) {
        this.idxName = name;
        return this;
    }

    public _IndexName newName(String newName) {
        this.newName = newName;
        return this;
    }
}
