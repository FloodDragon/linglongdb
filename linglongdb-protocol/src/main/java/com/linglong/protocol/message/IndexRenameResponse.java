package com.linglong.protocol.message;

import com.linglong.protocol.Message;

/**
 * Created by liuj-ai on 2021/3/17.
 */
public class IndexRenameResponse extends Message {
    private String newName;
    private boolean renamed;

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public boolean isRenamed() {
        return renamed;
    }

    public void setRenamed(boolean renamed) {
        this.renamed = renamed;
    }
}
