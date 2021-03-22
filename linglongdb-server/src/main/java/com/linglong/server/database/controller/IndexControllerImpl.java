package com.linglong.server.database.controller;

import com.linglong.protocol.IndexProtocol;
import com.linglong.protocol.message.*;
import com.linglong.server.database.DatabaseProcessor;

/**
 * 数据库索引控制器
 *
 * @author Stereo on 2021/3/17.
 */
public class IndexControllerImpl extends AbsController implements IndexProtocol {

    public IndexControllerImpl(DatabaseProcessor processor) {
        super(IndexProtocol.class, processor);
    }

    @Override
    public WriteResponse write(WriteRequest request) {
        try {
            //TODO to invoke leader -> call back
            return null;
        } finally {
        }
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
