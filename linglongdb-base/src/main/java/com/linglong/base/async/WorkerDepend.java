package com.linglong.base.async;

public class WorkerDepend {
    private WorkerDefine<?, ?> workerDefine;
    private boolean must = true;

    public WorkerDepend(WorkerDefine<?, ?> workerDefine, boolean must) {
        this.workerDefine = workerDefine;
        this.must = must;
    }

    public WorkerDepend() {
    }

    public WorkerDefine<?, ?> getDepend() {
        return workerDefine;
    }

    public void setDepend(WorkerDefine<?, ?> workerDefine) {
        this.workerDefine = workerDefine;
    }

    public boolean isMust() {
        return must;
    }

    public void setMust(boolean must) {
        this.must = must;
    }

    @Override
    public String toString() {
        return "WorkerDepend{" +
                "depend=" + workerDefine +
                ", must=" + must +
                '}';
    }
}
