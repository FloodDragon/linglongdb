package com.linglong.protocol;

import com.linglong.protocol.message.TxnCommitResponse;
import com.linglong.protocol.message.TxnRequest;
import com.linglong.protocol.message.TxnResponse;
import com.linglong.protocol.message.TxnRollbackResponse;
import com.linglong.rpc.common.service.IService;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public interface TransactionProtocol extends IService {

    TxnResponse openTxn();

    TxnCommitResponse commitTxn(TxnRequest request);

    TxnRollbackResponse rollbackTxn(TxnRequest request);
}
