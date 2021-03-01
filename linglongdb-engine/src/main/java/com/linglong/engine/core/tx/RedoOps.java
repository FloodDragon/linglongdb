package com.linglong.engine.core.tx;

/**
 * @author Stereo
 */
public class RedoOps {
    public static final byte
            OP_RESET = 1,

    OP_TIMESTAMP = 2,

    OP_SHUTDOWN = 3,

    OP_CLOSE = 4,

    OP_END_FILE = 5,

    OP_NOP_RANDOM = 6,

    OP_TXN_ID_RESET = 7,

    OP_CONTROL = 8,

    OP_STORE = 16,

    OP_STORE_NO_LOCK = 17,

    OP_DELETE = 18,

    OP_DELETE_NO_LOCK = 19,

    //OP_DROP_INDEX = 20, deprecated

    OP_RENAME_INDEX = 21,

    OP_DELETE_INDEX = 22,

    OP_TXN_PREPARE = 23,

    OP_TXN_ENTER = 24,

    OP_TXN_ROLLBACK = 25,

    OP_TXN_ROLLBACK_FINAL = 26,

    OP_TXN_COMMIT = 27,

    OP_TXN_COMMIT_FINAL = 28,

    OP_TXN_LOCK_SHARED = 29,

    OP_TXN_LOCK_UPGRADABLE = 30,

    OP_TXN_LOCK_EXCLUSIVE = 31,

    OP_TXN_ENTER_STORE = 32,

    OP_TXN_STORE = 33,

    OP_TXN_STORE_COMMIT = 34,

    OP_TXN_STORE_COMMIT_FINAL = 35,

    OP_TXN_ENTER_DELETE = 36,

    OP_TXN_DELETE = 37,

    OP_TXN_DELETE_COMMIT = 38,

    OP_TXN_DELETE_COMMIT_FINAL = 39,

    OP_CURSOR_REGISTER = 40,

    OP_CURSOR_UNREGISTER = 41,

    OP_CURSOR_STORE = 42,

    OP_CURSOR_DELETE = 43,

    OP_CURSOR_FIND = 44,

    OP_CURSOR_VALUE_SET_LENGTH = 45,

    OP_CURSOR_VALUE_WRITE = 46,

    OP_CURSOR_VALUE_CLEAR = 47,

    OP_TXN_CUSTOM = (byte) 128,

    OP_TXN_CUSTOM_LOCK = (byte) 129;
}
