package com.linglong.protocol;

/**
 * Created by liuj-ai on 2021/3/8.
 */
public class KeyValueRequest extends Message {
    private String index;
    private byte[] key;
    private byte[] value;
}
