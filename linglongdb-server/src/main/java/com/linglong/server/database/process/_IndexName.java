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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        _IndexName indexName = (_IndexName) o;

        if (!idxName.equals(indexName.idxName)) return false;
        return newName.equals(indexName.newName);
    }

    @Override
    public int hashCode() {
        int result = idxName.hashCode();
        result = 31 * result + newName.hashCode();
        return result;
    }
}
