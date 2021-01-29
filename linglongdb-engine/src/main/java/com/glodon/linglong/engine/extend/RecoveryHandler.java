
package com.glodon.linglong.engine.extend;


import com.glodon.linglong.engine.core.frame.Database;
import com.glodon.linglong.engine.core.tx.Transaction;

import java.io.IOException;

/**
 * @author Stereo
 */
public interface RecoveryHandler {

    void init(Database db) throws IOException;

    void recover(Transaction txn) throws IOException;
}
