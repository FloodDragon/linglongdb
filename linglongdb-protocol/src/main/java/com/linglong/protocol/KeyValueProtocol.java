package com.linglong.protocol;

import com.linglong.protocol.message.*;
import com.linglong.rpc.common.service.IService;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public interface KeyValueProtocol extends IService {

    /**
     * 索引kv写操作
     *
     * @param request
     * @return
     */
    KeyValueResponse insert(KeyValueRequest request);

    /**
     * 索引kv无条件写入
     *
     * @param request
     * @return
     */
    Response store(KeyValueRequest request);

    /**
     * 索引kv替换
     *
     * @param request
     * @return
     */
    KeyValueResponse replace(KeyValueRequest request);

    /**
     * 索引kv更新
     *
     * @param request
     * @return
     */
    KeyValueResponse update(KeyValueRequest request);

    /**
     * 索引kv删除
     *
     * @param request
     * @return
     */
    KeyValueResponse delete(KeyValueRequest request);

    /**
     * 索引kv移除
     *
     * @param request
     * @return
     */
    KeyValueResponse remove(KeyValueRequest request);

    /**
     * 索引kv交换
     *
     * @param request
     * @return
     */
    KeyValueResponse exchange(KeyValueRequest request);

    /**
     * 索引k是否存在
     *
     * @param request
     * @return
     */
    ExistsResponse exists(KeyValueRequest request);

    /**
     * 加载索引kv
     *
     * @param request
     * @return
     */
    KeyValueResponse load(KeyValueRequest request);
}
