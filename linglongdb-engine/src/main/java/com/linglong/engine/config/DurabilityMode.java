package com.linglong.engine.config;

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

    public static DurabilityMode getDurabilityMode(String mode) {
        DurabilityMode[] durabilityModes = DurabilityMode.values();
        for (DurabilityMode durabilityMode : durabilityModes) {
            if (durabilityMode.name().equals(mode)) {
                return durabilityMode;
            }
        }
        return null;
    }
}
