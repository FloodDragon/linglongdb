package com.linglong.server.database.coordinator;


/**
 * 集群组协调
 *
 * @author Stereo on 2021/3/17.
 */
public class LeaderCoordinator {


    /**
     *
     *
     * 	coordinator  ->  判断读/写  ->   读 -> 走本地读
     *                           写 ->   判断Leader 否 -> 走RPC 广播到Leader进行处理写
     *                                              是 -> 本地写
                                     -> IndexController
                                                        -> LeaderCoordinator ( rpc client -> server ) -> CURD SERVICE
                                     -> TableController
     *
     *
     *  协调器
     *  1.当前节点是否是Leader
     *  2.提供Leader peer client
     *  3.转发到leader
     *
     *
     */






}
