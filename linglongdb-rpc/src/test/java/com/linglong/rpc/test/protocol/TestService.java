package com.linglong.rpc.test.protocol;

import com.linglong.rpc.common.service.IService;

public interface TestService extends IService {
    Test test(Test test);
}
