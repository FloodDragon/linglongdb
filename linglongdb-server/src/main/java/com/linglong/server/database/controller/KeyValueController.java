package com.linglong.server.database.controller;

import com.linglong.protocol.KeyValueProtocol;
import com.linglong.protocol.message.*;
import com.linglong.rpc.server.skeleton.service.DataStreamTransfer;
import com.linglong.rpc.server.skeleton.service.ServiceContext;
import com.linglong.server.database.process.DatabaseProcessor;
import com.linglong.server.database.process._Options;
import com.linglong.server.utils.MixAll;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public class KeyValueController extends AbsController implements KeyValueProtocol {

    public KeyValueController(DatabaseProcessor databaseProcessor) {
        super(KeyValueProtocol.class, databaseProcessor);
    }

    @Override
    public KeyValueResponse insert(KeyValueRequest request) {
        try {
            _Options options = databaseProcessor.newOptions()
                    .indexName(request.getIndexName())
                    .key(request.getKey())
                    .value(request.getValue())
                    .txn(request.getXid());
            KeyValueResponse response = response(request, KeyValueResponse.class);
            response.setSuccessful(databaseProcessor.new KeyValueInsert().process(options));
            return response;
        } catch (Exception ex) {
            LOGGER.error("key value insert error", ex);
            return response(request, KeyValueResponse.class, ex);
        }
    }

    @Override
    public Response store(KeyValueRequest request) {
        try {
            _Options options = databaseProcessor.newOptions()
                    .indexName(request.getIndexName())
                    .key(request.getKey())
                    .value(request.getValue())
                    .txn(request.getXid());
            databaseProcessor.new KeyValueStore().process(options);
            return response(request, KeyValueResponse.class);
        } catch (Exception ex) {
            LOGGER.error("key value store error", ex);
            return response(request, KeyValueResponse.class, ex);
        }
    }

    @Override
    public KeyValueResponse replace(KeyValueRequest request) {
        try {
            _Options options = databaseProcessor.newOptions()
                    .indexName(request.getIndexName())
                    .key(request.getKey())
                    .value(request.getValue())
                    .txn(request.getXid());
            KeyValueResponse response = response(request, KeyValueResponse.class);
            response.setSuccessful(databaseProcessor.new KeyValueReplace().process(options));
            return response;
        } catch (Exception ex) {
            LOGGER.error("key value replace error", ex);
            return response(request, KeyValueResponse.class, ex);
        }
    }

    @Override
    public KeyValueResponse update(KeyValueRequest request) {
        try {
            _Options options = databaseProcessor.newOptions()
                    .indexName(request.getIndexName())
                    .key(request.getKey())
                    .value(request.getValue())
                    .oldValue(request.getOldValue())
                    .txn(request.getXid());
            KeyValueResponse response = response(request, KeyValueResponse.class);
            response.setSuccessful(databaseProcessor.new KeyValueUpdate().process(options));
            return response;
        } catch (Exception ex) {
            LOGGER.error("key value update error", ex);
            return response(request, KeyValueResponse.class, ex);
        }
    }

    @Override
    public KeyValueResponse delete(KeyValueRequest request) {
        try {
            _Options options = databaseProcessor.newOptions()
                    .indexName(request.getIndexName())
                    .key(request.getKey())
                    .txn(request.getXid());
            KeyValueResponse response = response(request, KeyValueResponse.class);
            response.setSuccessful(databaseProcessor.new KeyValueDelete().process(options));
            return response;
        } catch (Exception ex) {
            LOGGER.error("key value delete error", ex);
            return response(request, KeyValueResponse.class, ex);
        }
    }

    @Override
    public KeyValueResponse remove(KeyValueRequest request) {
        try {
            _Options options = databaseProcessor.newOptions()
                    .indexName(request.getIndexName())
                    .key(request.getKey())
                    .value(request.getValue())
                    .txn(request.getXid());
            KeyValueResponse response = response(request, KeyValueResponse.class);
            response.setSuccessful(databaseProcessor.new KeyValueRemove().process(options));
            return response;
        } catch (Exception ex) {
            LOGGER.error("key value remove error", ex);
            return response(request, KeyValueResponse.class, ex);
        }
    }

    @Override
    public KeyValueResponse exchange(KeyValueRequest request) {
        try {
            _Options options = databaseProcessor.newOptions()
                    .indexName(request.getIndexName())
                    .key(request.getKey())
                    .value(request.getValue())
                    .txn(request.getXid());
            KeyValueResponse response = response(request, KeyValueResponse.class);
            databaseProcessor.new KeyValueExchange().process(options);
            response.setValue(options.getOldValue());
            return response;
        } catch (Exception ex) {
            LOGGER.error("key value exchange error", ex);
            return response(request, KeyValueResponse.class, ex);
        }
    }

    @Override
    public ExistsResponse exists(KeyValueRequest request) {
        try {
            _Options options = databaseProcessor.newOptions()
                    .indexName(request.getIndexName())
                    .key(request.getKey())
                    .txn(request.getXid());
            ExistsResponse response = response(request, ExistsResponse.class);
            response.setExists(databaseProcessor.new KeyValueExists().process(options));
            return response;
        } catch (Exception ex) {
            LOGGER.error("key value exists error", ex);
            return response(request, ExistsResponse.class, ex);
        }
    }

    @Override
    public KeyValueResponse load(KeyValueRequest request) {
        try {
            _Options options = databaseProcessor.newOptions()
                    .indexName(request.getIndexName())
                    .key(request.getKey())
                    .txn(request.getXid());
            KeyValueResponse response = response(request, KeyValueResponse.class);
            databaseProcessor.new KeyValueLoad().process(options);
            response.setValue(options.getValue());
            return response;
        } catch (Exception ex) {
            LOGGER.error("key value load error", ex);
            return response(request, KeyValueResponse.class, ex);
        }
    }

    @Override
    public IndexScanResponse scan(IndexRequest request) {
        try {
            DataStreamTransfer transfer = ServiceContext.getDataStreamTransfer();
            _Options options = databaseProcessor.newOptions()
                    .indexName(request.getIndexName())
                    .txn(request.getXid());
            MixAll.Counter counter = MixAll.getCounter();
            if (transfer != null) {
                options.scanFunc((entry) -> {
                    try {
                        IndexScanItemResponse response = new IndexScanItemResponse();
                        response.setKey(entry.getKey());
                        response.setValue(entry.getValue());
                        transfer.transferTo(response);
                        counter.increment();
                    } catch (Exception ex) {
                        LOGGER.error("key value scan func execute error", ex);
                    }
                });
            }
            databaseProcessor.new IndexKeyValueScan().process(options);
            IndexScanResponse response = response(request, IndexScanResponse.class);
            response.setScanTotal(counter.getCount());
            return response;
        } catch (Exception ex) {
            LOGGER.error("key value scan error", ex);
            return response(request, IndexScanResponse.class, ex);
        }
    }
}
