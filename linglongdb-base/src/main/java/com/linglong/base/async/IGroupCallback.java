package com.linglong.base.async;

import java.util.List;

public interface IGroupCallback {
    void success(List<WorkerDefine> workerDefines);

    void failure(List<WorkerDefine> workerDefines, Exception e);
}
