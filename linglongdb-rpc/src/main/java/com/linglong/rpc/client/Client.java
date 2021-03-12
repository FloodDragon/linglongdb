
package com.linglong.rpc.client;


import com.linglong.rpc.common.config.Config;
import com.linglong.rpc.common.remoting.Channel;
import com.linglong.rpc.common.remoting.Endpoint;

/**
 * @author Stereo
 */
public interface Client extends Endpoint, Channel {

    Config getConfig();

    Channel getChannel();

    HeartbeatPostMan getHeartbeatPostMan();

    HeartBeatState getHeartBeatState();

    ServerTimestamp serverTimestamp();
}