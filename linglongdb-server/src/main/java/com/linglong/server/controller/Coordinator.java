package com.linglong.server.controller;

import com.linglong.rpc.server.skeleton.service.Service;

/**
 * Created by liuj-ai on 2021/3/17.
 */
public abstract class Coordinator extends Service {

    public Coordinator(Class<?> cls) {
        super(cls);
    }
}
