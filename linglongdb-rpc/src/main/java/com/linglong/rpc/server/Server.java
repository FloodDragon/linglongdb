package com.linglong.rpc.server;

import com.linglong.rpc.common.remoting.Channel;
import com.linglong.rpc.common.remoting.Endpoint;

import java.net.InetSocketAddress;
import java.util.Collection;


/**
 * @author Stereo
 */
public interface Server extends Endpoint {

    /**
     * get channels.
     *
     * @return channels
     */
    Collection<Channel> getChannels();

    /**
     * get channel.
     *
     * @param remoteAddress
     * @return channel
     */
    Channel getChannel(InetSocketAddress remoteAddress);


    boolean isBound();

}