package com.linglong.protocol;

/**
 * Created by liuj-ai on 2021/3/15.
 */
public enum ProcessType {

    /* 将值与给定的键相关联，已存在相应的值存在等价于: update */
    KV_INSERT((byte) 0x01),
    /* 无条件地将值与给定键关联 */
    KV_STORE((byte) 0x02),
    /* 将值与给定的键相关联，但前提是相应的值已存在 */
    KV_REPLACE((byte) 0x03),
    /* 仅当给定值与现有值不同时，才将值与给定键相关联 */
    KV_UPDATE((byte) 0x04),
    /* 无条件地删除与给定密钥关联的条目 等价于: replace(txn, key, null) */
    KV_DELETE((byte) 0x05),
    /* 删除与给定键关联的条目，但仅当给定值进行匹配 等价于: update(txn, key, value, null) */
    KV_REMOVE((byte) 0x06),
    /* 无条件地将值与给定键相关联，返回上一个值 */
    KV_EXCHANGE((byte) 0x07),
    /* 值是否存在 */
    KV_EXISTS((byte) 0x08),
    /* 加载值 */
    KV_LOAD((byte) 0x09),
    /* 统计总个数 */
    KV_COUNT((byte) 0x10),
    /* 索引销毁 */
    INDEX_DROP((byte) 0x11),
    /* 索引查找 */
    INDEX_FIND((byte) 0x12),
    /* 索引分析 */
    INDEX_ANALYZE((byte) 0x13),
    /* 索引数据驱逐 */
    INDEX_EVICT((byte) 0x14);

    private byte type;

    ProcessType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public static ProcessType getProcessType(byte type) {
        ProcessType[] processTypes = ProcessType.values();
        for (ProcessType processType : processTypes) {
            if (processType.getType() == type) {
                return processType;
            }
        }
        return null;
    }
}