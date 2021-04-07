package com.linglong.base.async;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * 并行测试
 */
@SuppressWarnings("ALL")
public class TestAll {

    public static void main(String[] args) throws Exception {
        //test_0();
        //test_1();
        //test_2();
        //test_3();
        //test_4();
        //test_5();
        //test_6();
        //test_7();
        //test_8();
        //test_9();
        //test_10();
        test_7();
//        testMultiReverse();
//        testMultiError2();
//        testMulti3();
//        testMulti3Reverse();
//        testMulti4();
//        testMulti4Reverse();
//        testMulti5();
//        testMulti5Reverse();
//        testMulti6();
//        testMulti7();
//        testMulti8();
//        testMulti9();
//        testMulti9Reverse();
    }

    private static void test_0() throws InterruptedException, ExecutionException {
        Worker0 w = new Worker0();
        Worker1 w1 = new Worker1();
        Worker2 w2 = new Worker2();

        WorkerDefine<String, String> WorkerDefine2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .build();

        WorkerDefine<String, String> WorkerDefine1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .build();

        WorkerDefine<String, String> WorkerDefine = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("0")
                .build();

        long now = SystemClock.now();
        System.out.println("begin-" + now);

        Async.submitWork(1500, WorkerDefine, WorkerDefine1, WorkerDefine2);
//        Async.submitWork(800, WorkerDefine, WorkerDefine1, WorkerDefine2);
//        Async.submitWork(1000, WorkerDefine, WorkerDefine1, WorkerDefine2);

        System.out.println("end-" + SystemClock.now());
        System.err.println("cost-" + (SystemClock.now() - now));
        System.out.println(Async.getThreadCount());
        System.out.println(WorkerDefine.getWorkResult());
        Async.shutDown();
    }

    private static void test_1() throws ExecutionException, InterruptedException {
        Worker0 w = new Worker0();
        Worker1 w1 = new Worker1();
        Worker2 w2 = new Worker2();

        WorkerDefine<String, String> WorkerDefine2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .build();

        WorkerDefine<String, String> WorkerDefine1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .build();

        WorkerDefine<String, String> WorkerDefine = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(WorkerDefine1) //0,2同时开启,1在0后面  0---1 2
                .build();

        long now = SystemClock.now();
        System.out.println("begin-" + now);

        Async.submitWork(2500, WorkerDefine, WorkerDefine2);

        System.out.println("end-" + SystemClock.now());
        System.err.println("cost-" + (SystemClock.now() - now));

        Async.shutDown();
    }

    private static void test_2() throws ExecutionException, InterruptedException {
        Worker0 w = new Worker0();
        Worker1 w1 = new Worker1();
        Worker2 w2 = new Worker2();

        WorkerDefine<String, String> WorkerDefine = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("0")
                .build();

        WorkerDefine<String, String> WorkerDefine1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .depend(WorkerDefine) //0,2同时开启,1在0后面,0---1,2
                .build();

        WorkerDefine<String, String> WorkerDefine2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .build();


        long now = SystemClock.now();
        System.out.println("begin-" + now);

        Async.submitWork(2500, WorkerDefine, WorkerDefine2);

        System.out.println("end-" + SystemClock.now());
        System.err.println("cost-" + (SystemClock.now() - now));

        Async.shutDown();
    }

    private static void test_3() throws ExecutionException, InterruptedException {
        Worker0 w = new Worker0();
        Worker1 w1 = new Worker1();
        Worker2 w2 = new Worker2();

        WorkerDefine<String, String> WorkerDefine2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .build();

        WorkerDefine<String, String> WorkerDefine1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .build();

        WorkerDefine<String, String> WorkerDefine = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(WorkerDefine1) //0,2同时开启,1在0后面. 组超时,则0和2成功,1失败, 0---1, 2
                .build();

        long now = SystemClock.now();
        System.out.println("begin-" + now);

        Async.submitWork(15000000, WorkerDefine, WorkerDefine2);

        System.out.println("end-" + SystemClock.now());
        System.err.println("cost-" + (SystemClock.now() - now));

        Async.shutDown();
    }

    private static void test_4() throws ExecutionException, InterruptedException {
        Worker0 w = new Worker0();
        Worker1 w1 = new Worker1();
        Worker2 w2 = new Worker2();
        Worker3 w3 = new Worker3();
        //0执行完,同时1和2, 1\2都完成后3
        WorkerDefine<String, String> WorkerDefine3 = new WorkerDefine.Builder<String, String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .build();

        WorkerDefine<String, String> WorkerDefine2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .next(WorkerDefine3)
                .build();

        WorkerDefine<String, String> WorkerDefine1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .next(WorkerDefine3)
                .build();

        WorkerDefine<String, String> WorkerDefine = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(WorkerDefine1, WorkerDefine2)
                .build();
        long now = SystemClock.now();
        System.out.println("begin-" + now);
        Async.submitWork(3100, WorkerDefine);
//        Async.submitWork(2100, WorkerDefine);
        System.out.println("end-" + SystemClock.now());
        System.err.println("cost-" + (SystemClock.now() - now));
        System.out.println(Async.getThreadCount());
        Async.shutDown();
    }

    private static void test_5() throws ExecutionException, InterruptedException {
        Worker0 w = new Worker0();
        Worker1 w1 = new Worker1();
        Worker2 w2 = new Worker2();
        Worker3 w3 = new Worker3();
        //0执行完,同时1和2, 1\2都完成后3
        WorkerDefine<String, String> WorkerDefine = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("0")
                .build();

        WorkerDefine<String, String> WorkerDefine2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .depend(WorkerDefine)
                .build();

        WorkerDefine<String, String> WorkerDefine1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .depend(WorkerDefine)
                .build();

        WorkerDefine<String, String> WorkerDefine3 = new WorkerDefine.Builder<String, String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .depend(WorkerDefine1, WorkerDefine2)
                .build();
        long now = SystemClock.now();
        System.out.println("begin-" + now);

        Async.submitWork(3100000, WorkerDefine);
//        Async.submitWork(2100, WorkerDefine);

        System.out.println("end-" + SystemClock.now());
        System.err.println("cost-" + (SystemClock.now() - now));
        System.out.println(Async.getThreadCount());
        Async.shutDown();
    }

    private static void test_6() throws ExecutionException, InterruptedException {
        Worker0 w = new Worker0();
        Worker1 w1 = new Worker1();

        Worker2 w2 = new Worker2();
        w2.setSleepTime(2000);

        Worker3 w3 = new Worker3();

        //0执行完,同时执行1和2, 1\2都完成后3，2耗时2秒，1耗时1秒。3会等待2完成
        WorkerDefine<String, String> WorkerDefine3 = new WorkerDefine.Builder<String, String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .build();

        WorkerDefine<String, String> WorkerDefine2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .next(WorkerDefine3)
                .build();

        WorkerDefine<String, String> WorkerDefine1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .next(WorkerDefine3)
                .build();

        WorkerDefine<String, String> WorkerDefine = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(WorkerDefine1, WorkerDefine2)
                .build();

        long now = SystemClock.now();
        System.out.println("begin-" + now);

        //正常完毕
        Async.submitWork(4100, WorkerDefine);
        //3会超时
//        Async.submitWork(3100, WorkerDefine);
        //2,3会超时
//        Async.submitWork(2900, WorkerDefine);

        System.out.println("end-" + SystemClock.now());
        System.err.println("cost-" + (SystemClock.now() - now));

        System.out.println(Async.getThreadCount());
        Async.shutDown();
    }

    private static void test_7() throws ExecutionException, InterruptedException {
        Worker0 w = new Worker0();
        Worker1 w1 = new Worker1();

        Worker2 w2 = new Worker2();
        w2.setSleepTime(2000);
        Worker3 w3 = new Worker3();

        //0执行完,同时1和2, 1和2都完成后执行3，2耗时2秒，1耗时1秒。3会等待2完成
        WorkerDefine<String, String> WorkerDefine_11 = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("11")
                .build();

        WorkerDefine<String, String> WorkerDefine = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("0")
                .build();

        WorkerDefine<String, String> WorkerDefine3 = new WorkerDefine.Builder<String, String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .build();

        WorkerDefine<String, String> WorkerDefine2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .depend(WorkerDefine)
                .next(WorkerDefine3)
                .build();

        WorkerDefine<String, String> WorkerDefine1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .depend(WorkerDefine)
                .next(WorkerDefine3)
                .build();

        long now = SystemClock.now();
        System.out.println("begin-" + now);

        //正常完毕
        Async.submitWork(60 * 1000, WorkerDefine);
        //3会超时
//        Async.submitWork(3100, WorkerDefine);
        //2,3会超时
//        Async.submitWork(2900, WorkerDefine);

        System.out.println("end-" + SystemClock.now());
        System.err.println("cost-" + (SystemClock.now() - now));

        System.out.println(Async.getThreadCount());
        Async.shutDown();
    }

    private static void test_8() throws ExecutionException, InterruptedException {
        Worker0 w = new Worker0();
        Worker1 w1 = new Worker1();

        Worker2 w2 = new Worker2();
        w2.setSleepTime(500);

        Worker3 w3 = new Worker3();
        w3.setSleepTime(400);
        //0执行完,同时1和2, 1\2 任何一个执行完后，都执行3
        WorkerDefine<String, String> WorkerDefine3 = new WorkerDefine.Builder<String, String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .build();

        WorkerDefine<String, String> WorkerDefine2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .next(WorkerDefine3, false)
                .build();

        WorkerDefine<String, String> WorkerDefine1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .next(WorkerDefine3, false)
                .build();

        WorkerDefine<String, String> WorkerDefine = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(WorkerDefine1, WorkerDefine2)
                .build();

        long now = SystemClock.now();
        System.out.println("begin-" + now);

        //正常完毕
        Async.submitWork(4100, WorkerDefine);

        System.out.println("end-" + SystemClock.now());
        System.err.println("cost-" + (SystemClock.now() - now));

        System.out.println(Async.getThreadCount());
        Async.shutDown();
    }

    private static void test_9() throws ExecutionException, InterruptedException {
        Worker0 w = new Worker0();
        Worker1 w1 = new Worker1();

        Worker2 w2 = new Worker2();
        w2.setSleepTime(500);

        Worker3 w3 = new Worker3();
        w3.setSleepTime(400);
        //0执行完,同时1和2, 1\2 任何一个执行完后，都执行3
        WorkerDefine<String, String> WorkerDefine = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("0")
                .build();

        WorkerDefine<String, String> WorkerDefine3 = new WorkerDefine.Builder<String, String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .build();

        WorkerDefine<String, String> WorkerDefine2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .depend(WorkerDefine, true)
                .next(WorkerDefine3, false)
                .build();

        WorkerDefine<String, String> WorkerDefine1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .depend(WorkerDefine, true)
                .next(WorkerDefine3, false)
                .build();


        long now = SystemClock.now();
        System.out.println("begin-" + now);

        //正常完毕
        Async.submitWork(4100, WorkerDefine);

        System.out.println("end-" + SystemClock.now());
        System.err.println("cost-" + (SystemClock.now() - now));

        System.out.println(Async.getThreadCount());
        Async.shutDown();
    }

    private static void test_10() throws ExecutionException, InterruptedException {
        Worker0 w = new Worker0();
        Worker1 w1 = new Worker1();

        Worker2 w2 = new Worker2();
        w2.setSleepTime(500);

        Worker3 w3 = new Worker3();
        w3.setSleepTime(400);

        //0执行完,同时1和2, 必须1执行完毕后，才能执行3. 无论2是否领先1完毕，都要等1
        WorkerDefine<String, String> WorkerDefine3 = new WorkerDefine.Builder<String, String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .build();

        //设置2不是必须
        WorkerDefine<String, String> WorkerDefine2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .next(WorkerDefine3, false)
                .build();
        // 设置1是必须的
        WorkerDefine<String, String> WorkerDefine1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .next(WorkerDefine3, true)
                .build();

        WorkerDefine<String, String> WorkerDefine0 = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(WorkerDefine2, WorkerDefine1)
                .build();


        long now = SystemClock.now();
        System.out.println("begin-" + now);

        //正常完毕
        Async.submitWork(4100, WorkerDefine0);

        System.out.println("end-" + SystemClock.now());
        System.err.println("cost-" + (SystemClock.now() - now));

        System.out.println(Async.getThreadCount());
        Async.shutDown();
    }

    private static void test_11() throws ExecutionException, InterruptedException {
        Worker0 w = new Worker0();
        Worker1 w1 = new Worker1();
        Worker2 w2 = new Worker2();
        Worker3 w3 = new Worker3();
        Worker4 w4 = new Worker4();
        // 两个0并行，上面0执行完,同时1和2, 下面0执行完开始1，上面的 必须1、2执行完毕后，才能执行3. 最后必须2、3都完成，才能4
        // callback worker0 success--1577242870969----result = 1577242870968---param = 00 from 0-threadName:Thread-1
        // callback worker0 success--1577242870969----result = 1577242870968---param = 0 from 0-threadName:Thread-0
        // callback worker1 success--1577242871972----result = 1577242871972---param = 11 from 1-threadName:Thread-1
        // callback worker1 success--1577242871972----result = 1577242871972---param = 1 from 1-threadName:Thread-2
        // callback worker2 success--1577242871973----result = 1577242871973---param = 2 from 2-threadName:Thread-3
        // callback worker2 success--1577242872975----result = 1577242872975---param = 22 from 2-threadName:Thread-1
        // callback worker3 success--1577242872977----result = 1577242872977---param = 3 from 3-threadName:Thread-2
        // callback worker4 success--1577242873980----result = 1577242873980---param = 4 from 3-threadName:Thread-2
        WorkerDefine<String, String> WorkerDefine4 = new WorkerDefine.Builder<String, String>()
                .worker(w4)
                .callback(w4)
                .param("4")
                .build();

        WorkerDefine<String, String> WorkerDefine3 = new WorkerDefine.Builder<String, String>()
                .worker(w3)
                .callback(w3)
                .param("3")
                .next(WorkerDefine4)
                .build();

        //下面的2
        WorkerDefine<String, String> WorkerDefine22 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("22")
                .next(WorkerDefine4)
                .build();

        //下面的1
        WorkerDefine<String, String> WorkerDefine11 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("11")
                .next(WorkerDefine22)
                .build();

        //下面的0
        WorkerDefine<String, String> WorkerDefine00 = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("00")
                .next(WorkerDefine11)
                .build();

        //上面的1
        WorkerDefine<String, String> WorkerDefine1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .next(WorkerDefine3)
                .build();

        //上面的2
        WorkerDefine<String, String> WorkerDefine2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .next(WorkerDefine3)
                .build();

        //上面的0
        WorkerDefine<String, String> WorkerDefine0 = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("0")
                .next(WorkerDefine1, WorkerDefine2)
                .build();

        long now = SystemClock.now();
        System.out.println("begin-" + now);

        //正常完毕
        Async.submitWork(4100, WorkerDefine00, WorkerDefine0);

        System.out.println("end-" + SystemClock.now());
        System.err.println("cost-" + (SystemClock.now() - now));

        System.out.println(Async.getThreadCount());
        Async.shutDown();
    }

    /**
     * a1 -> b -> c
     * a2 -> b -> c
     * <p>
     * b、c
     */
    private static void testMulti8() throws ExecutionException, InterruptedException {
        Worker0 w = new Worker0();
        Worker1 w1 = new Worker1();
        w1.setSleepTime(1005);

        Worker2 w2 = new Worker2();
        w2.setSleepTime(3000);
        Worker3 w3 = new Worker3();
        w3.setSleepTime(1000);

        WorkerDefine<String, String> WorkerDefine3 = new WorkerDefine.Builder<String, String>()
                .worker(w3)
                .callback(w3)
                .param("c")
                .build();

        WorkerDefine<String, String> WorkerDefine2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("b")
                .next(WorkerDefine3)
                .build();

        WorkerDefine<String, String> WorkerDefinea1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("a1")
                .next(WorkerDefine2)
                .build();
        WorkerDefine<String, String> WorkerDefinea2 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("a2")
                .next(WorkerDefine2)
                .build();


        Async.submitWork(6000, WorkerDefinea1, WorkerDefinea2);
        Async.shutDown();
    }

    /**
     * w1 -> w2 -> w3
     * ---  last
     * w
     * w1和w并行，w执行完后就执行last，此时b、c还没开始，b、c就不需要执行了
     */
    private static void testMulti9() throws ExecutionException, InterruptedException {
        Worker1 w1 = new Worker1();
        //注意这里，如果w1的执行时间比w长，那么w2和w3肯定不走。 如果w1和w执行时间一样长，多运行几次，会发现w2有时走有时不走
//        w1.setSleepTime(1100);

        Worker0 w = new Worker0();
        Worker2 w2 = new Worker2();
        Worker3 w3 = new Worker3();
        Worker4 w4 = new Worker4();

        WorkerDefine<String, String> last = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("last")
                .build();

        WorkerDefine<String, String> wrapperW = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("w")
                .next(last, false)
                .build();

        WorkerDefine<String, String> wrapperW3 = new WorkerDefine.Builder<String, String>()
                .worker(w3)
                .callback(w3)
                .param("w3")
                .next(last, false)
                .build();

        WorkerDefine<String, String> wrapperW2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("w2")
                .next(wrapperW3)
                .build();

        WorkerDefine<String, String> wrapperW1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("w1")
                .next(wrapperW2)
                .build();

        Async.submitWork(6000, wrapperW, wrapperW1);
        Async.shutDown();
    }

    /**
     * w1 -> w2 -> w3
     * ---  last
     * w
     * w1和w并行，w执行完后就执行last，此时b、c还没开始，b、c就不需要执行了
     */
    private static void testMulti9Reverse() throws ExecutionException, InterruptedException {
        Worker1 w1 = new Worker1();
        //注意这里，如果w1的执行时间比w长，那么w2和w3肯定不走。 如果w1和w执行时间一样长，多运行几次，会发现w2有时走有时不走
//        w1.setSleepTime(1100);

        Worker0 w = new Worker0();
        Worker2 w2 = new Worker2();
        Worker3 w3 = new Worker3();
        Worker4 w4 = new Worker4();

        WorkerDefine<String, String> wrapperW1 = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("w1")
                .build();

        WorkerDefine<String, String> wrapperW = new WorkerDefine.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("w")
                .build();

        WorkerDefine<String, String> last = new WorkerDefine.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("last")
                .depend(wrapperW)
                .build();

        WorkerDefine<String, String> wrapperW2 = new WorkerDefine.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("w2")
                .depend(wrapperW1)
                .build();

        WorkerDefine<String, String> wrapperW3 = new WorkerDefine.Builder<String, String>()
                .worker(w3)
                .callback(w3)
                .param("w3")
                .depend(wrapperW2)
                .next(last, false)
                .build();

        Async.submitWork(6000, Executors.newCachedThreadPool(), wrapperW, wrapperW1);
        Async.shutDown();
    }
}
