package com.linglong.protocol;

import com.linglong.rpc.serialization.msgpack.BeanMessage;

/**
 * TODO 设计中...
 *
 * @author Stereo
 */
public abstract class Message implements BeanMessage {
    private String id;
    private long timestamp;
    private String version;

    //txId
    //创建临时库
    //拆卸临时库
    //统计count
    //打开Index/查找Index/exists Index/replace Index
    //open tx/commit tx/rest tx
    //kv store/kv exchange/kv insert/kv update/kv delete/kv load/kv replace
    //sort
}
