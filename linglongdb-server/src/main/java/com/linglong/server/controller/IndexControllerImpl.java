package com.linglong.server.controller;

import com.linglong.protocol.message.*;
import com.linglong.rpc.server.skeleton.service.Service;

/**
 * 数据库索引控制器
 *
 * @author Stereo on 2021/3/17.
 */
public class IndexControllerImpl extends Service implements IndexController {

    public IndexControllerImpl() {
        super(IndexController.class);
    }

    @Override
    public WriteResponse write(WriteRequest request) {
        //TODO
        return null;
    }

    @Override
    public QueryResponse query(QueryRequest request) {
        return null;
    }

    @Override
    public IndexDropResponse drop(IndexRequest request) {
        return null;
    }

    @Override
    public IndexRenameResponse rename(IndexRenameRequest request) {
        return null;
    }

    @Override
    public IndexStatsResponse stats(IndexRequest request) {
        return null;
    }
}
