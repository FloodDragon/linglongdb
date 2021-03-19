package com.linglong.server.coordinator;


/**
 * 集群组协调
 *
 * @author Stereo on 2021/3/17.
 */
public class GroupCoordinator {


    /**
     *
     *
     * 	coordinator  ->  判断读/写  ->   读 -> 走本地读
     *                           写 ->   判断Leader 否 -> 走RPC 广播到Leader进行处理写
     *                                              是 -> 本地写
     *
                                     -> IndexController -> GroupCoordinator -> ( rpc client -> server )
                                     -> TableController

     *
     *
     *
     */

}
