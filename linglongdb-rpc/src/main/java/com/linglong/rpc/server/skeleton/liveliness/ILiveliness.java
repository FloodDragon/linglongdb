package com.linglong.rpc.server.skeleton.liveliness;

import com.linglong.rpc.server.skeleton.liveliness.listener.ClientListenerRegistry;

import java.util.Collection;

/**
 * @author Stereo on 2019/1/21.
 */
public interface ILiveliness<O> {

    Collection<O> living();

    ClientListenerRegistry getClientListenerRegistry();
}
