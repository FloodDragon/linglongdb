package com.linglong.protocol;

/**
 * Created by liuj-ai on 2021/3/8.
 */
public interface KeyValueProtocol {

    /**
     * 插入数据
     *
     * @param request
     * @return
     */
    KeyValueResponse insert(KeyValueRequest request);

    /**
     * 加载数据
     *
     * @param request
     * @return
     */
    KeyValueResponse load(KeyValueRequest request);

    /**
     * 替换value
     *
     * @param request
     * @return
     */
    KeyValueResponse replace(KeyValueRequest request);

    /**
     * 存储数据
     *
     * @param request
     * @return
     */
    KeyValueResponse store(KeyValueRequest request);

    /**
     * 更新数据
     *
     * @param request
     * @return
     */
    KeyValueResponse update(KeyValueRequest request);

    /**
     * 删除数据
     *
     * @param request
     * @return
     */
    KeyValueResponse delete(KeyValueRequest request);

    /**
     * 是否存在
     *
     * @param request
     * @return
     */
    KeyValueResponse exists(KeyValueRequest request);
}
