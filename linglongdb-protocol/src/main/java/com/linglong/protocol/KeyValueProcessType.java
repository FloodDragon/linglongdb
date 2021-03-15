package com.linglong.protocol;

/**
 * Created by liuj-ai on 2021/3/15.
 */
public enum KeyValueProcessType implements ProcessType {

    /* 将值与给定的键相关联，已存在相应的值存在等价于: update */
    INSERT((byte) 0x01),
    /* 无条件地将值与给定键关联 */
    STORE((byte) 0x02),
    /* 将值与给定的键相关联，但前提是相应的值已存在 */
    REPLACE((byte) 0x03),
    /* 仅当给定值与现有值不同时，才将值与给定键相关联 */
    UPDATE((byte) 0x04),
    /* 无条件地删除与给定密钥关联的条目 等价于: replace(txn, key, null) */
    DELETE((byte) 0x05),
    /* 删除与给定键关联的条目，但仅当给定值进行匹配 等价于: update(txn, key, value, null) */
    REMOVE((byte) 0x06),
    /* 无条件地将值与给定键相关联，返回上一个值 */
    EXCHANGE((byte) 0x07),
    /* 值是否存在 */
    EXISTS((byte) 0x08),
    /* 加载值 */
    LOAD((byte) 0x09),
    /* 统计总个数 */
    COUNT((byte) 0x10);

    private byte type;

    KeyValueProcessType(byte type) {
        this.type = type;
    }

    @Override
    public byte getType() {
        return type;
    }
}
