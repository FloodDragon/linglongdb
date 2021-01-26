/*
 *  Copyright (C) 2018 Cojen.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.glodon.linglong.base.ext;

import com.glodon.my.Database;
import com.glodon.my.DatabaseConfig;
import com.glodon.my.Transaction;

import java.io.IOException;

/**
 * Handler for recovered transactions which were prepared, but aren't finished yet.
 *
 * @author Stereo
 * @see DatabaseConfig#recoveryHandler DatabaseConfig.recoveryHandler
 */
public interface RecoveryHandler {
    /**
     * Called once when the database is opened, before the recover method is ever called.
     */
    void init(Database db) throws IOException;

    /**
     * Called for each recovered transaction.
     */
    void recover(Transaction txn) throws IOException;
}
