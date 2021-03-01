
package com.linglong.rpc.common.remoting;


import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.exception.RpcException;

import java.net.InetSocketAddress;

/**
 * @author Stereo
 */
public interface Endpoint {

    /**
     * get config.
     *
     * @return Config
     */
    Config getConfig();

    /**
     * get channel handler.
     *
     * @return channel handler
     */
    ChannelHandler getChannelHandler();

    /**
     * get local address.
     *
     * @return local address.
     */
    InetSocketAddress getLocalAddress();

    /**
     * send message.
     *
     * @param message
     * @throws RpcException
     */
    void send(Object message) throws RpcException;

    /**
     * send message.
     *
     * @param message
     * @param sent    是否已发送完成
     */
    void send(Object message, boolean sent) throws RpcException;

    /**
     * is closed.
     *
     * @return closed
     */
    boolean isClosed();
}