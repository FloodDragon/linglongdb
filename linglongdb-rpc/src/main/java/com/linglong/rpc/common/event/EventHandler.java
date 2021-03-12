
package com.linglong.rpc.common.event;

/**
 * @author Stereo on 2019/11/1.
 */
public interface EventHandler<T extends Event> {

  void handle(T event);

}
