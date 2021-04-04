package com.linglong.base.async;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerDefine<T, V> {
    private String id;
    private T param;
    private IWorker<T, V> worker;
    private ICallback<T, V> callback;

    private List<WorkerDefine<?, ?>> nextDefines;
    private List<WorkerDepend> workerDepends;
    private AtomicInteger state = new AtomicInteger(0);
    private Map<String, WorkerDefine> forParamUseDefines;
    private volatile WorkResult<V> workResult = WorkResult.defaultResult();
    private volatile boolean needCheckNextDefineResult = true;

    private static final int FINISH = 1;
    private static final int ERROR = 2;
    private static final int WORKING = 3;
    private static final int INIT = 0;

    private WorkerDefine(String id, IWorker<T, V> worker, T param, ICallback<T, V> callback) {
        if (worker == null) {
            throw new NullPointerException("async.worker is null");
        }
        this.worker = worker;
        this.param = param;
        this.id = id;
        if (callback == null) {
            callback = new DefaultCallback<>();
        }
        this.callback = callback;
    }

    private void work(ExecutorService executorService, WorkerDefine fromDefine, long remainTime, Map<String, WorkerDefine> forParamUseDefines) {
        this.forParamUseDefines = forParamUseDefines;
        forParamUseDefines.put(id, this);
        long now = SystemClock.now();
        if (remainTime <= 0) {
            System.out.println("WorkerDefine.work 1");
            fastFail(INIT, null);
            beginNext(executorService, now, remainTime);
            return;
        }
        if (getState() == FINISH || getState() == ERROR) {
            System.out.println("WorkerDefine.work 2");
            beginNext(executorService, now, remainTime);
            return;
        }

        if (needCheckNextDefineResult) {
            if (!checkNextDefineResult()) {
                System.out.println("WorkerDefine.work 3");
                fastFail(INIT, new SkippedException());
                beginNext(executorService, now, remainTime);
                return;
            }
        }

        if (workerDepends == null || workerDepends.size() == 0) {
            System.out.println("WorkerDefine.work 4");
            fire();
            beginNext(executorService, now, remainTime);
            return;
        }

        if (workerDepends.size() == 1) {
            System.out.println("WorkerDefine.work 5");
            doDependsOneJob(fromDefine);
            beginNext(executorService, now, remainTime);
        } else {
            System.out.println("WorkerDefine.work 6");
            doDependsJobs(executorService, workerDepends, fromDefine, now, remainTime);
        }
    }

    public void work(ExecutorService executorService, long remainTime, Map<String, WorkerDefine> forParamUseDefines) {
        work(executorService, null, remainTime, forParamUseDefines);
    }

    public void stopNow() {
        if (getState() == INIT || getState() == WORKING) {
            fastFail(getState(), null);
        }
    }

    private boolean checkNextDefineResult() {
        if (nextDefines == null || nextDefines.size() != 1) {
            return getState() == INIT;
        }
        WorkerDefine nextDefine = nextDefines.get(0);
        boolean state = nextDefine.getState() == INIT;
        return state && nextDefine.checkNextDefineResult();
    }

    private void beginNext(ExecutorService executorService, long now, long remainTime) {
        long costTime = SystemClock.now() - now;
        if (nextDefines == null) {
            return;
        }
        if (nextDefines.size() == 1) {
            nextDefines.get(0).work(executorService, WorkerDefine.this, remainTime - costTime, forParamUseDefines);
            return;
        }
        CompletableFuture[] futures = new CompletableFuture[nextDefines.size()];
        for (int i = 0; i < nextDefines.size(); i++) {
            int finalI = i;
            futures[i] = CompletableFuture.runAsync(() -> nextDefines.get(finalI)
                    .work(executorService, WorkerDefine.this, remainTime - costTime, forParamUseDefines), executorService);
        }
        try {
            CompletableFuture.allOf(futures).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void doDependsOneJob(WorkerDefine dependDefine) {
        if (WorkState.TIMEOUT == dependDefine.getWorkResult().getWorkState()) {
            workResult = defaultResult();
            fastFail(INIT, null);
        } else if (WorkState.EXCEPTION == dependDefine.getWorkResult().getWorkState()) {
            workResult = defaultExResult(dependDefine.getWorkResult().getEx());
            fastFail(INIT, null);
        } else {
            fire();
        }
    }

    private synchronized void doDependsJobs(ExecutorService executorService, List<WorkerDepend> workerDepends, WorkerDefine from, long now, long remainTime) {
        boolean nowDependIsMust = false;
        Set<WorkerDepend> mustDefine = new HashSet<>();
        for (WorkerDepend workerDepend : workerDepends) {
            if (workerDepend.isMust()) {
                mustDefine.add(workerDepend);
            }
            if (workerDepend.getDepend().equals(from)) {
                nowDependIsMust = workerDepend.isMust();
            }
        }

        if (mustDefine.size() == 0) {
            if (WorkState.TIMEOUT == from.getWorkResult().getWorkState()) {
                fastFail(INIT, null);
            } else {
                fire();
            }
            beginNext(executorService, now, remainTime);
            return;
        }

        if (!nowDependIsMust) {
            return;
        }

        boolean existNoFinish = false;
        boolean hasError = false;
        for (WorkerDepend workerDepend : mustDefine) {
            WorkerDefine workerDefine = workerDepend.getDepend();
            WorkResult tempWorkResult = workerDefine.getWorkResult();
            if (workerDefine.getState() == INIT || workerDefine.getState() == WORKING) {
                existNoFinish = true;
                break;
            }
            if (WorkState.TIMEOUT == tempWorkResult.getWorkState()) {
                workResult = defaultResult();
                hasError = true;
                break;
            }
            if (WorkState.EXCEPTION == tempWorkResult.getWorkState()) {
                workResult = defaultExResult(workerDefine.getWorkResult().getEx());
                hasError = true;
                break;
            }

        }
        if (hasError) {
            fastFail(INIT, null);
            beginNext(executorService, now, remainTime);
            return;
        }

        if (!existNoFinish) {
            fire();
            beginNext(executorService, now, remainTime);
            return;
        }
    }

    private void fire() {
        workResult = workerDoJob();
    }

    private boolean fastFail(int expect, Exception e) {
        if (!compareAndSetState(expect, ERROR)) {
            return false;
        }

        if (checkIsNullResult()) {
            if (e == null) {
                workResult = defaultResult();
            } else {
                workResult = defaultExResult(e);
            }
        }

        callback.result(false, param, workResult);
        return true;
    }

    private WorkResult<V> workerDoJob() {
        if (!checkIsNullResult()) {
            return workResult;
        }
        try {
            if (!compareAndSetState(INIT, WORKING)) {
                return workResult;
            }

            callback.begin();

            V resultValue = worker.action(param, forParamUseDefines);

            if (!compareAndSetState(WORKING, FINISH)) {
                return workResult;
            }

            workResult.setWorkState(WorkState.SUCCESS);
            workResult.setResult(resultValue);
            callback.result(true, param, workResult);

            return workResult;
        } catch (Exception e) {
            if (!checkIsNullResult()) {
                return workResult;
            }
            fastFail(WORKING, e);
            return workResult;
        }
    }

    public WorkResult<V> getWorkResult() {
        return workResult;
    }

    public List<WorkerDefine<?, ?>> getNextDefines() {
        return nextDefines;
    }

    public void setParam(T param) {
        this.param = param;
    }

    private boolean checkIsNullResult() {
        return WorkState.DEFAULT == workResult.getWorkState();
    }

    private void addDepend(WorkerDefine<?, ?> workerDefine, boolean must) {
        addDepend(new WorkerDepend(workerDefine, must));
    }

    private void addDepend(WorkerDepend workerDepend) {
        if (workerDepends == null) {
            workerDepends = new ArrayList<>();
        }
        for (WorkerDepend define : workerDepends) {
            if (define.equals(workerDepend)) {
                return;
            }
        }
        workerDepends.add(workerDepend);
    }

    private void addNext(WorkerDefine<?, ?> workerDefine) {
        if (nextDefines == null) {
            nextDefines = new ArrayList<>();
        }
        for (WorkerDefine define : nextDefines) {
            if (workerDefine.equals(define)) {
                return;
            }
        }
        nextDefines.add(workerDefine);
    }

    private void addNextDefines(List<WorkerDefine<?, ?>> defines) {
        if (defines == null) {
            return;
        }
        for (WorkerDefine<?, ?> define : defines) {
            addNext(define);
        }
    }

    private void addDependDefines(List<WorkerDepend> workerDepends) {
        if (workerDepends == null) {
            return;
        }
        for (WorkerDepend depend : workerDepends) {
            addDepend(depend);
        }
    }

    private WorkResult<V> defaultResult() {
        workResult.setWorkState(WorkState.TIMEOUT);
        workResult.setResult(worker.defaultValue());
        return workResult;
    }

    private WorkResult<V> defaultExResult(Exception ex) {
        workResult.setWorkState(WorkState.EXCEPTION);
        workResult.setResult(worker.defaultValue());
        workResult.setEx(ex);
        return workResult;
    }

    private int getState() {
        return state.get();
    }

    public String getId() {
        return id;
    }

    private boolean compareAndSetState(int expect, int update) {
        return this.state.compareAndSet(expect, update);
    }

    private void setNeedCheckNextDefineResult(boolean needCheckNextDefineResult) {
        this.needCheckNextDefineResult = needCheckNextDefineResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorkerDefine<?, ?> that = (WorkerDefine<?, ?>) o;
        return needCheckNextDefineResult == that.needCheckNextDefineResult &&
                Objects.equals(param, that.param) &&
                Objects.equals(worker, that.worker) &&
                Objects.equals(callback, that.callback) &&
                Objects.equals(nextDefines, that.nextDefines) &&
                Objects.equals(workerDepends, that.workerDepends) &&
                Objects.equals(state, that.state) &&
                Objects.equals(workResult, that.workResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(param, worker, callback, nextDefines, workerDepends, state, workResult, needCheckNextDefineResult);
    }

    public static class Builder<W, C> {
        private String id = UUID.randomUUID().toString();
        private W param;
        private IWorker<W, C> worker;
        private ICallback<W, C> callback;
        private List<WorkerDefine<?, ?>> nextDefines;
        private List<WorkerDepend> workerDepends;
        private Set<WorkerDefine<?, ?>> selfIsMustSet;
        private boolean needCheckNextDefineResult = true;

        public Builder<W, C> worker(IWorker<W, C> worker) {
            this.worker = worker;
            return this;
        }

        public Builder<W, C> param(W w) {
            this.param = w;
            return this;
        }

        public Builder<W, C> id(String id) {
            if (id != null) {
                this.id = id;
            }
            return this;
        }

        public Builder<W, C> needCheckNextDefineResult(boolean needCheckNextDefineResult) {
            this.needCheckNextDefineResult = needCheckNextDefineResult;
            return this;
        }

        public Builder<W, C> callback(ICallback<W, C> callback) {
            this.callback = callback;
            return this;
        }

        public Builder<W, C> depend(WorkerDefine<?, ?>... defines) {
            if (defines == null) {
                return this;
            }
            for (WorkerDefine<?, ?> define : defines) {
                depend(define);
            }
            return this;
        }

        public Builder<W, C> depend(WorkerDefine<?, ?> define) {
            return depend(define, true);
        }

        public Builder<W, C> depend(WorkerDefine<?, ?> define, boolean isMust) {
            if (define == null) {
                return this;
            }
            WorkerDepend workerDepend = new WorkerDepend(define, isMust);
            if (workerDepends == null) {
                workerDepends = new ArrayList<>();
            }
            workerDepends.add(workerDepend);
            return this;
        }

        public Builder<W, C> next(WorkerDefine<?, ?> define) {
            return next(define, true);
        }

        public Builder<W, C> next(WorkerDefine<?, ?> define, boolean selfIsMust) {
            if (nextDefines == null) {
                nextDefines = new ArrayList<>();
            }
            nextDefines.add(define);

            if (selfIsMust) {
                if (selfIsMustSet == null) {
                    selfIsMustSet = new HashSet<>();
                }
                selfIsMustSet.add(define);
            }
            return this;
        }

        public Builder<W, C> next(WorkerDefine<?, ?>... defines) {
            if (defines == null) {
                return this;
            }
            for (WorkerDefine<?, ?> define : defines) {
                next(define);
            }
            return this;
        }

        public WorkerDefine<W, C> build() {
            WorkerDefine<W, C> define = new WorkerDefine<>(id, worker, param, callback);
            define.setNeedCheckNextDefineResult(needCheckNextDefineResult);
            if (workerDepends != null) {
                for (WorkerDepend depend : workerDepends) {
                    depend.getDepend().addNext(define);
                    define.addDepend(depend);
                }
            }
            if (nextDefines != null) {
                for (WorkerDefine<?, ?> workerDefine : nextDefines) {
                    boolean must = false;
                    if (selfIsMustSet != null && selfIsMustSet.contains(workerDefine)) {
                        must = true;
                    }
                    workerDefine.addDepend(define, must);
                    define.addNext(workerDefine);
                }
            }
            return define;
        }
    }
}
