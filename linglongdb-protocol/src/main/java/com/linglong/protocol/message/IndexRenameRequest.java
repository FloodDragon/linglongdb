package com.linglong.protocol.message;

/**
 * @author Stereo on 2021/3/17.
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
