package com.linglong.rpc.common.event;

/**
 * Created by liuj-ai on 2019/11/1.
 */
public interface Dispatcher {

    EventHandler getEventHandler();

    void register(Class<? extends Enum> eventType, EventHandler handler);

    void serviceStart() throws Exception;

    void serviceStop() throws Exception;
}
