package com.linglong.base.async;

public class WorkResult<V> {
    private V result;
    private WorkState workState;
    private Exception ex;

    public WorkResult(V result, WorkState workState) {
        this(result, workState, null);
    }

    public WorkResult(V result, WorkState workState, Exception ex) {
        this.result = result;
        this.workState = workState;
        this.ex = ex;
    }

    public static <V> WorkResult<V> defaultResult() {
        return new WorkResult<>(null, WorkState.DEFAULT);
    }

    @Override
    public String toString() {
        return "WorkResult{" +
                "result=" + result +
                ", workState=" + workState +
                ", ex=" + ex +
                '}';
    }

    public Exception getEx() {
        return ex;
    }

    public void setEx(Exception ex) {
        this.ex = ex;
    }

    public V getResult() {
        return result;
    }

    public void setResult(V result) {
        this.result = result;
    }

    public WorkState getWorkState() {
        return workState;
    }

    public void setWorkState(WorkState workState) {
        this.workState = workState;
    }
}
