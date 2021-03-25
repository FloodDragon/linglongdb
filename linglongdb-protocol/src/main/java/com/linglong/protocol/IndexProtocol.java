package com.linglong.protocol;

import com.linglong.protocol.message.*;
import com.linglong.rpc.common.service.IService;

/**
 * @author Stereo on 2021/3/8.
 */
public interface IndexProtocol extends IService {

    CountResponse count(KeyLowHighRequest request);

    IndexStatsResponse stats(KeyLowHighRequest request);

    IndexDeleteResponse delete(IndexRequest request);

    ExistsResponse exists(IndexRequest request);

    CountResponse evict(KeyLowHighRequest request);

    IndexRenameResponse rename(IndexRenameRequest request);
}
