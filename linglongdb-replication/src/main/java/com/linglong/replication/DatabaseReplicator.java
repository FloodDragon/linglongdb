
package com.linglong.replication;

import com.linglong.engine.extend.ReplicationManager;
import com.linglong.replication.confg.ReplicatorConfig;

import java.io.IOException;

/**
 * @author Stereo
 */
public interface DatabaseReplicator extends Replicator, ReplicationManager {
    static DatabaseReplicator open(ReplicatorConfig config) throws IOException {
        StreamReplicator streamRepl = StreamReplicator.open(config);
        DatabaseReplicator dbRepl = new DatabaseStreamReplicator(streamRepl);
        return dbRepl;
    }
}
