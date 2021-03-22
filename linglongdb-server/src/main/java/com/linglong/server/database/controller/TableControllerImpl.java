package com.linglong.server.database.controller;

import com.linglong.protocol.TableProtocol;
import com.linglong.rpc.server.skeleton.service.Service;

/**
 * Created by liuj-ai on 2021/3/22.
 */
public class TableControllerImpl extends Service implements TableProtocol {

    public TableControllerImpl() {
        super(TableProtocol.class);
    }
}
