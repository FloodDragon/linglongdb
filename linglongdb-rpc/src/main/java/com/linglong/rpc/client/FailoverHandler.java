package com.linglong.rpc.client;


/**
 * Created by liuj-ai on 2019/12/12.
 */
public interface FailoverHandler {
    void failover(Client client);
}
