package com.linglong.protocol.message;

import com.linglong.rpc.serialization.msgpack.BeanMessage;

/**
 * @author Stereo on 2021/3/17.
 */
public class IndexRenameRequest extends IndexRequest implements BeanMessage {
    private String newName;

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }
}
