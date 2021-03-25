package com.linglong.protocol;

import com.linglong.protocol.message.*;
import com.linglong.rpc.common.service.IService;

/**
 * Created by liuj-ai on 2021/3/25.
 */
public interface TransactionProtocol extends IService {

    Response openTxn();

    TxnCommitResponse commitTxn(TxnRequest request);

    TxnRollbackResponse rollbackTxn(TxnRequest request);
}
