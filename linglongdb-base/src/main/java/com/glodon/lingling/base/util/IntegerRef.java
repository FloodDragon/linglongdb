package com.glodon.lingling.base.util;

/**
 * @author Stereo
 */
public interface IntegerRef {
    int get();

    void set(int v);

    class Value implements IntegerRef {
        int value;

        public int get() {
            return value;
        }

        public void set(int v) {
            value = v;
        }
    }
}
