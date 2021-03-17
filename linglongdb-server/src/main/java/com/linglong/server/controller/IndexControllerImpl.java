package com.linglong.server.controller;

import com.linglong.protocol.message.*;

/**
 * Created by liuj-ai on 2021/3/17.
 */
public class IndexControllerImpl extends Coordinator implements IndexController {

    public IndexControllerImpl() {
        super(IndexController.class);
    }

    @Override
    public WriteResponse write(WriteRequest request) {
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
