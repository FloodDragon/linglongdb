package com.linglong.server.database.controller;

import com.linglong.protocol.TransactionProtocol;
import com.linglong.protocol.message.*;
import com.linglong.server.database.process.DatabaseProcessor;
import com.linglong.server.database.process.TxnOptions;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public class TransactionController extends AbsController<TransactionController> implements TransactionProtocol {

    public TransactionController(DatabaseProcessor databaseProcessor) {
        super(TransactionProtocol.class, databaseProcessor);
    }

    @Override
    public Response openTxn() {
        try {
            TxnOptions txn = databaseProcessor.new OpenTxn().process(null);
            Response response = response(null, Response.class);
            response.setXid(txn.getTxnId());
            return response;
        } catch (Exception ex) {
            LOGGER.error("open txn error", ex);
            return response(null, TxnResponse.class, ex);
        }
    }

    @Override
    public TxnCommitResponse commitTxn(TxnRequest request) {
        try {
            TxnCommitResponse response = response(request, TxnCommitResponse.class);
            response.setCommited(databaseProcessor.new TxnCommitOrRollback().process(new TxnOptions().txnId(request.getTxnId()).commit()));
            return response;
        } catch (Exception ex) {
            LOGGER.error("commit txn error", ex);
            return response(null, TxnCommitResponse.class, ex);
        }
    }

    @Override
    public TxnRollbackResponse rollbackTxn(TxnRequest request) {
        try {
            TxnRollbackResponse response = response(request, TxnRollbackResponse.class);
            response.setRollback(databaseProcessor.new TxnCommitOrRollback().process(new TxnOptions().txnId(request.getTxnId()).rollback()));
            return response;
        } catch (Exception ex) {
            LOGGER.error("rollback txn error", ex);
            return response(request, TxnRollbackResponse.class, ex);
        }
    }

    @Override
    public TransactionController getInstance() {
        return this;
    }
}
