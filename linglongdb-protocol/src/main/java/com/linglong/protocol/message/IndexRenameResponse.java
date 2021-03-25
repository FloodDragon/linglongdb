package com.linglong.protocol.message;

/**
 * @author Stereo on 2021/3/17.
 */
public class IndexRenameResponse extends Response {
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
