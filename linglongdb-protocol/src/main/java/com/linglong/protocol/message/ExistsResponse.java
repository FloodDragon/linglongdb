package com.linglong.protocol.message;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public class ExistsResponse extends Response {

    /* 是否存在 */
    private boolean exists;

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }
}
