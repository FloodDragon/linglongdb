package com.linglong.rpc.client.ds;

import com.linglong.rpc.common.service.IService;

/**
 * Created by liuj-ai on 2021/4/8.
 */
public interface DataStreamExecutor<S extends IService> {

    void execute(S s);
}
