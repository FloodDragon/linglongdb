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
public class IndexMap {
    private final RWLock rwLock = new RWLock();
    private final Map<String, IndexEntry> indexMap = new LinkedHashMap<>();

    IndexEntry put(Index index) {
        if (index == null) {
            return null;
        } else {
            rwLock.acquireExclusive();
            try {
                IndexEntry indexEntry;
                if (!indexMap.containsKey(index.getNameString())) {
                    indexMap.put(index.getNameString(), indexEntry = new IndexEntry(index));
                    return indexEntry;
                } else {
                    return indexMap.get(index.getNameString());
                }
            } finally {
                rwLock.releaseExclusive();
            }
        }
    }

    IndexEntry find(String idxName) {
        if (StringUtils.isBlank(idxName)) {
            return null;
        } else {
            rwLock.acquireShared();
            try {
                return indexMap.containsKey(idxName) ? indexMap.get(idxName) : null;
            } finally {
                rwLock.releaseShared();
            }
        }
    }

    void clean(String... names) throws IOException {
        if (indexMap.isEmpty()) {
            return;
        } else {
            try {
                rwLock.acquireExclusive();
                if (names == null || names.length == 0) {
                    for (Map.Entry<String, IndexEntry> entry : indexMap.entrySet()) {
                        entry.getValue().getIndex().close();
                    }
                    indexMap.clear();
                } else {
                    for (String name : names) {
                        IndexEntry indexEntry = indexMap.remove(name);
                        if (indexEntry != null) {
                            indexEntry.getIndex().close();
                        }
                    }
                }
            } finally {
                rwLock.releaseExclusive();
            }
        }
    }
}
