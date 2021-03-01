
package com.linglong.engine.extend;


import com.linglong.engine.core.frame.Database;
import com.linglong.engine.core.tx.Transaction;

import java.io.IOException;

/**
 * @author Stereo
 */
public interface RecoveryHandler {

    void init(Database db) throws IOException;

    void recover(Transaction txn) throws IOException;
}
