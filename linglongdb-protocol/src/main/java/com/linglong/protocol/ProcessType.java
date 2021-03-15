package com.linglong.protocol;

/**
 * Created by liuj-ai on 2021/3/15.
 */
public interface ProcessType {

    byte getType();

    static KeyValueProcessType getKeyValueProcessType(byte type) {
        KeyValueProcessType[] processTypes = KeyValueProcessType.values();
        for (KeyValueProcessType processType : processTypes) {
            if (processType.getType() == type) {
                return processType;
            }
        }
        return null;
    }
}
