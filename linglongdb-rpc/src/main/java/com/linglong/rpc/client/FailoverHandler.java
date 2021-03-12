package com.linglong.rpc.client;


/**
 * @author Stereo on 2019/12/12.
 */
public interface FailoverHandler {
    void failover(Client client);
}
