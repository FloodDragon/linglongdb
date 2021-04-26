package com.linglong.server.database.process;

import com.linglong.base.concurrent.RWLock;
import com.linglong.engine.core.frame.Index;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by liuj-ai on 2021/4/26.
 */
public class _IndexCache {

    private final RWLock indexLock = new RWLock();
    private final Map<String, Index> indexMap = new LinkedHashMap<>();

    void put(Index index) {
        if (index == null)
            return;
        else {
            indexLock.acquireExclusive();
            try {
                indexMap.put(index.getNameString(), index);
            } finally {
                indexLock.releaseExclusive();
            }
        }
    }

    Index find(String idxName) {
        if (StringUtils.isBlank(idxName))
            return null;
        else {
            indexLock.acquireShared();
            try {
                return indexMap.containsKey(idxName) ? indexMap.get(idxName) : null;
            } finally {
                indexLock.releaseShared();
            }
        }
    }

    void clean(String... names) throws IOException {
        if (indexMap.isEmpty()) {
            return;
        }
        try {
            indexLock.acquireExclusive();
            if (names == null || names.length == 0) {
                for (Map.Entry<String, Index> entry : indexMap.entrySet()) {
                    entry.getValue().close();
                }
                indexMap.clear();
            } else {
                for (String name : names) {
                    Index index = indexMap.remove(name);
                    if (index != null) {
                        index.close();
                    }
                }
            }
        } finally {
            indexLock.releaseExclusive();
        }
    }
}
