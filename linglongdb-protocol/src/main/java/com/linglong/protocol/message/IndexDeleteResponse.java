package com.linglong.protocol.message;

/**
 * @author Stereo on 2021/3/16.
 */
public class IndexDeleteResponse extends Response {
    /* 是否销毁成功 */
    private boolean deleted;

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
