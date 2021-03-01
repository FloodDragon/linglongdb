package com.linglong.rpc.test;

import com.linglong.rpc.server.skeleton.service.Service;
import com.linglong.rpc.test.protocol.Test;
import com.linglong.rpc.test.protocol.TestService;

import java.util.concurrent.atomic.AtomicInteger;

public class TestServiceImpl extends Service implements TestService {

    public TestServiceImpl(Class<?> cls) {
        super(cls);
    }

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public Test test(Test test) {
        System.out.println(" 处理次数 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  " + counter.incrementAndGet());
        test.setCode(1);
        test.setMsg("返回=1");
//        throw new IllegalArgumentException("哈哈");
        return test;
    }
}
