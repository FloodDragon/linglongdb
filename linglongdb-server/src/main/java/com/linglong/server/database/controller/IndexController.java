package com.linglong.server.database.controller;

import com.linglong.engine.core.frame.Index;
import com.linglong.protocol.IndexProtocol;
import com.linglong.protocol.message.*;
import com.linglong.server.database.controller.annotation.Leader;
import com.linglong.server.database.process.DatabaseProcessor;
import com.linglong.server.database.process.IndexName;
import com.linglong.server.database.process.KeyValueOptions;

/**
 * 数据库索引控制器
 *
 * @author Stereo on 2021/3/17.
 */
public class IndexController extends AbsController<IndexProtocol> implements IndexProtocol {

    public IndexController(DatabaseProcessor processor) {
        super(IndexProtocol.class, processor);
    }

    @Override
    @Leader
    public CountResponse count(KeyLowHighRequest request) {
        try {
            KeyValueOptions options = databaseProcessor.newOptions()
                    .indexName(request.getIndexName())
                    .lowKey(request.getLowKey())
                    .highKey(request.getHighKey());
            databaseProcessor.new IndexCount().process(options);
            CountResponse response = response(request, CountResponse.class);
            response.setCount(options.getCount());
            return response;
        } catch (Exception ex) {
            LOGGER.error("index count error", ex);
            return response(request, CountResponse.class, ex);
        }
    }

    @Override
    @Leader
    public IndexStatsResponse stats(KeyLowHighRequest request) {
        try {
            KeyValueOptions options = databaseProcessor.newOptions()
                    .indexName(request.getIndexName())
                    .lowKey(request.getLowKey())
                    .highKey(request.getHighKey());
            Index.Stats stats = databaseProcessor.new IndexStats().process(options);
            IndexStatsResponse response = response(request, IndexStatsResponse.class);
            response.setEntryCount(stats.entryCount());
            response.setFreeBytes(stats.freeBytes());
            response.setKeyBytes(stats.keyBytes());
            response.setValueBytes(stats.valueBytes());
            response.setTotalBytes(stats.totalBytes());
            return response;
        } catch (Exception ex) {
            LOGGER.error("index stats error", ex);
            return response(request, IndexStatsResponse.class, ex);
        }
    }

    @Override
    @Leader
    public ExistsResponse exists(IndexRequest request) {
        try {
            ExistsResponse response = response(request, ExistsResponse.class);
            response.setExists(databaseProcessor.existsIndex(request.getIndexName()));
            return response;
        } catch (Exception ex) {
            LOGGER.error("index exists error", ex);
            return response(request, ExistsResponse.class, ex);
        }
    }

    @Override
    @Leader
    public CountResponse evict(KeyLowHighRequest request) {
        try {
            KeyValueOptions options = databaseProcessor.newOptions()
                    .indexName(request.getIndexName())
                    .lowKey(request.getLowKey())
                    .highKey(request.getHighKey())
                    .txn(request.getXid());
            databaseProcessor.new IndexEvict().process(options);
            CountResponse response = response(request, CountResponse.class);
            response.setCount(options.getCount());
            return response;
        } catch (Exception ex) {
            LOGGER.error("index evict error", ex);
            return response(request, CountResponse.class, ex);
        }
    }

    @Override
    @Leader
    public IndexRenameResponse rename(IndexRenameRequest request) {
        try {
            IndexRenameResponse response = response(request, IndexRenameResponse.class);
            response.setNewName(request.getNewName());
            response.setRenamed(databaseProcessor.new IndexRename().process(new IndexName().indexName(request.getIndexName()).newName(request.getNewName())));
            return response;
        } catch (Exception ex) {
            LOGGER.error("index rename error", ex);
            return response(request, IndexRenameResponse.class, ex);
        }
    }

    @Override
    @Leader
    public IndexDeleteResponse delete(IndexRequest request) {
        try {
            boolean deleted = databaseProcessor.new IndexDelete().process(new IndexName().indexName(request.getIndexName()));
            IndexDeleteResponse response = response(request, IndexDeleteResponse.class);
            response.setDeleted(deleted);
            return response;
        } catch (Exception ex) {
            LOGGER.error("index delete error", ex);
            return response(request, IndexDeleteResponse.class, ex);
        }
    }
}
