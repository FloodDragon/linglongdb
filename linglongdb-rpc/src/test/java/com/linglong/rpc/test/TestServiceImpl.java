package com.linglong.rpc.test;

import com.linglong.rpc.server.skeleton.service.DataStreamTransfer;
import com.linglong.rpc.server.skeleton.service.Service;
import com.linglong.rpc.server.skeleton.service.ServiceContext;
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
        DataStreamTransfer dataStreamTransfer = ServiceContext.getDataStreamTransfer();
        if (dataStreamTransfer != null) {
            for (int i = 0; i < 10; i++) {
                try {
                    dataStreamTransfer.transferTo(String.valueOf(i));
                    Thread.sleep(2 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("DataStreamTransfer = " + dataStreamTransfer);
        System.out.println(" 处理次数 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  " + counter.incrementAndGet());
        test.setCode(1);
        test.setMsg("返回=1");
//        throw new IllegalArgumentException("哈哈");
        return test;
    }
}
