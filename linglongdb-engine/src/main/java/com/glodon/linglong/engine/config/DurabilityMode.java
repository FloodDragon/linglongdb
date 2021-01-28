package com.glodon.linglong.engine.config;

/**
 * @author Stereo
 */
public enum DurabilityMode {

    SYNC,
    NO_SYNC,
    NO_FLUSH,
    NO_REDO;

    public DurabilityMode alwaysRedo() {
        return this == NO_REDO ? NO_FLUSH : this;
    }
}
