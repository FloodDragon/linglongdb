package com.linglong.engine.command;

import com.linglong.engine.config.DatabaseConfig;
import com.linglong.engine.core.frame.Database;
import com.linglong.engine.core.frame.Index;
import com.linglong.engine.event.EventPrinter;
import com.linglong.engine.observer.VerificationObserver;

/**
 * @author Stereo
 */
public class Verify extends VerificationObserver {

    public static void main(String[] args) throws Exception {
        DatabaseConfig config = new DatabaseConfig()
                .baseFilePath(args[0])
                .eventListener(new EventPrinter());

        if (args.length > 1) {
            config.minCacheSize(Long.parseLong(args[1]));
        }

        Database db = Database.open(config);
        System.out.println(db.stats());
        Verify v = new Verify();
        db.verify(v);
        System.out.println(v);
        System.exit(v.failed);
    }

    private int failed;
    private long totalNodeCount;
    private long totalEntryCount;
    private long totalFreeBytes;
    private long totalLargeValues;

    @Override
    public boolean indexBegin(Index ix, int height) {
        System.out.println("Index: " + ix.getNameString() + ", height: " + height);
        return super.indexBegin(ix, height);
    }

    @Override
    public boolean indexNodePassed(long id,
                                   int level,
                                   int entryCount,
                                   int freeBytes,
                                   int largeValueCount) {
        totalEntryCount += entryCount;
        totalFreeBytes += freeBytes;
        totalLargeValues += largeValueCount;
        if (((++totalNodeCount) % 10000) == 0) {
            System.out.println(this);
        }
        return true;
    }

    @Override
    public boolean indexNodeFailed(long id, int level, String message) {
        failed = 1;
        return super.indexNodeFailed(id, level, message);
    }

    @Override
    public String toString() {
        return "totalNodeCount: " + totalNodeCount +
                ", totalEntryCount: " + totalEntryCount +
                ", totalFreeBytes: " + totalFreeBytes +
                ", totalLargeValues: " + totalLargeValues;
    }
}
