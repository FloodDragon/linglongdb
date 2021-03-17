package com.linglong.protocol.message;

/**
 * Created by liuj-ai on 2021/3/17.
 */
public class IndexRenameRequest extends IndexRequest {
    private String newName;

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }
}
