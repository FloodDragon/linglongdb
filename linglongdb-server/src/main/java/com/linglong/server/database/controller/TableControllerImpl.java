package com.linglong.server.database.controller;

import com.linglong.protocol.TableProtocol;
import com.linglong.server.database.process.DatabaseProcessor;

/**
 * Created by liuj-ai on 2021/3/22.
 */
public class TableControllerImpl extends AbsController implements TableProtocol {

    public TableControllerImpl(DatabaseProcessor processor) {
        super(TableProtocol.class, processor);
    }
}
