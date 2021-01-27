
package com.glodon.linglong.replication;

import com.glodon.linglong.engine.extend.ReplicationManager;

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
