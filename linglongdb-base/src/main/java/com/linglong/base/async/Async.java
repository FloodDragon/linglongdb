package com.linglong.base.async;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public final class Async {

    private static final ThreadPoolExecutor ASYNC_COMMON_POOL = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            128,
            15L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            (ThreadFactory) Thread::new);

    private static ExecutorService executorService;

    public static boolean submitWork(long timeout, ExecutorService executorService, List<WorkerDefine> workerDefines) throws ExecutionException, InterruptedException {
        if (workerDefines == null || workerDefines.size() == 0) {
            return false;
        }
        Async.executorService = executorService;
        Map<String, WorkerDefine> forParamUseDefines = new ConcurrentHashMap<>();
        CompletableFuture[] futures = new CompletableFuture[workerDefines.size()];
        for (int i = 0; i < workerDefines.size(); i++) {
            WorkerDefine define = workerDefines.get(i);
            futures[i] = CompletableFuture.runAsync(() -> define.work(executorService, timeout, forParamUseDefines), executorService);
        }
        try {
            CompletableFuture.allOf(futures).get(timeout, TimeUnit.MILLISECONDS);
            return true;
        } catch (TimeoutException e) {
            Set<WorkerDefine> set = new HashSet<>();
            totalWorkers(workerDefines, set);
            for (WorkerDefine define : set) {
                define.stopNow();
            }
            return false;
        }
    }

    public static boolean submitWork(long timeout, ExecutorService executorService, WorkerDefine... workerDefine) throws ExecutionException, InterruptedException {
        if (workerDefine == null || workerDefine.length == 0) {
            return false;
        }
        List<WorkerDefine> workerDefines = Arrays.stream(workerDefine).collect(Collectors.toList());
        return submitWork(timeout, executorService, workerDefines);
    }

    public static boolean submitWork(long timeout, WorkerDefine... workerDefine) throws ExecutionException, InterruptedException {
        return submitWork(timeout, ASYNC_COMMON_POOL, workerDefine);
    }

    public static void submitWorkAsync(long timeout, IGroupCallback groupCallback, WorkerDefine... workerDefines) {
        if (groupCallback == null) {
            groupCallback = new DefaultGroupCallback();
        }
        IGroupCallback finalGroupCallback = groupCallback;
        if (executorService != null) {
            executorService.submit(() -> {
                try {
                    boolean success = submitWork(timeout, ASYNC_COMMON_POOL, workerDefines);
                    if (success) {
                        finalGroupCallback.success(Arrays.asList(workerDefines));
                    } else {
                        finalGroupCallback.failure(Arrays.asList(workerDefines), new TimeoutException());
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    finalGroupCallback.failure(Arrays.asList(workerDefines), e);
                }
            });
        } else {
            ASYNC_COMMON_POOL.submit(() -> {
                try {
                    boolean success = submitWork(timeout, ASYNC_COMMON_POOL, workerDefines);
                    if (success) {
                        finalGroupCallback.success(Arrays.asList(workerDefines));
                    } else {
                        finalGroupCallback.failure(Arrays.asList(workerDefines), new TimeoutException());
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    finalGroupCallback.failure(Arrays.asList(workerDefines), e);
                }
            });
        }

    }

    @SuppressWarnings("unchecked")
    private static void totalWorkers(List<WorkerDefine> workerDefines, Set<WorkerDefine> set) {
        set.addAll(workerDefines);
        for (WorkerDefine define : workerDefines) {
            if (define.getNextDefines() == null) {
                continue;
            }
            List<WorkerDefine> defines = define.getNextDefines();
            totalWorkers(defines, set);
        }

    }

    public static void shutDown() {
        if (executorService != null) {
            executorService.shutdown();
        } else {
            ASYNC_COMMON_POOL.shutdown();
        }
    }

    public static String getThreadCount() {
        return "activeCount=" + ASYNC_COMMON_POOL.getActiveCount() +
                "  completedCount " + ASYNC_COMMON_POOL.getCompletedTaskCount() +
                "  largestCount " + ASYNC_COMMON_POOL.getLargestPoolSize();
    }
}
