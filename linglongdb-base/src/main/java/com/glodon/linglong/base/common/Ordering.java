package com.glodon.linglong.base.common;

/**
 * @author Stereo
 */
public enum Ordering {

    ASCENDING, DESCENDING, UNSPECIFIED;

    public Ordering reverse() {
        Ordering ordering = this;
        if (ordering != UNSPECIFIED) {
            if (ordering == ASCENDING) {
                ordering = DESCENDING;
            } else {
                ordering = ASCENDING;
            }
        }
        return ordering;
    }
}
