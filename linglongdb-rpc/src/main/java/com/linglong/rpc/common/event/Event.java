
package com.linglong.rpc.common.event;

/**
 * Created by liuj-ai on 2019/11/1.
 */
public interface Event<TYPE extends Enum<TYPE>> {
	public TYPE getType();
	public long getTimestamp();
	public String toString();
}
