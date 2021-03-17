package com.linglong.protocol.message;

import com.linglong.protocol.Message;

/**
 * @author Stereo on 2021/3/16.
 */
public class IndexDropResponse extends Message {
    /* 是否销毁成功 */
    private boolean destroyed;

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
}
