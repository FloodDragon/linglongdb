package com.linglong.server.database.controller;

import com.linglong.protocol.IndexProtocol;
import com.linglong.protocol.message.*;
import com.linglong.rpc.server.skeleton.service.Service;
import com.linglong.server.database.exception.ErrorCode;

/**
 * 数据库索引控制器
 *
 * @author Stereo on 2021/3/17.
 */
public class IndexControllerImpl extends Service implements IndexProtocol {

    public IndexControllerImpl() {
        super(IndexProtocol.class);
    }

    @Override
    public WriteResponse write(WriteRequest request) {
        try {
            if (request == null) {
                ResponseBuilder.error(null, ErrorCode.INDEX_WRITE_PARAMETER_ERROR);
            }
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
