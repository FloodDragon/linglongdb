package com.linglong.engine.command;

import com.linglong.engine.config.DatabaseConfig;
import com.linglong.engine.core.frame.Database;
import com.linglong.engine.event.EventPrinter;
import com.linglong.engine.event.EventType;

/**
 * @author Stereo
 */
public class Compact {

    public static void main(String[] args) throws Exception {
        DatabaseConfig config = new DatabaseConfig()
                .baseFilePath(args[0])
                .eventListener(new EventPrinter().ignore(EventType.Category.CHECKPOINT))
                .checkpointSizeThreshold(0);

        double target = Double.parseDouble(args[1]);

        if (args.length > 2) {
            config.minCacheSize(Long.parseLong(args[2]));
        }

        Database db = Database.open(config);
        System.out.println("Compact Before: " + db.stats());
        db.compactFile(null, target);
        System.out.println("Compact After: " + db.stats());
    }

    private Compact() {
    }
}
