package com.glodon.linglong.base.io;

/**
 * FilePageArray Open Option
 *
 * @author Stereo
 */
public enum OpenOption {
    /**
     * 以只读模式打开文件。
     */
    READ_ONLY,

    /**
     * 创建文件（如果尚不存在）。
     */
    CREATE,

    /**
     * 将文件映射到主内存。
     */
    MAPPED,

    /**
     * 所有文件I/O应该是持久的。
     */
    SYNC_IO,

    /**
     * 所有文件I/O应该是持久的，并尽可能绕过文件系统缓存。
     */
    DIRECT_IO,

    /**
     * 操作系统崩溃或电源故障后，文件内容不会持续存在。
     */
    NON_DURABLE,

    /**
     * 以随机顺序访问文件。
     */
    RANDOM_ACCESS,

    /**
     * 用于对文件执行预读的可选提示。
     */
    READAHEAD,

    /**
     * 在文件关闭时应用，不访问文件数据。
     */
    CLOSE_DONTNEED,
}
