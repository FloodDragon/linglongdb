
package com.linglong.rpc.serialization.msgpack.template.builder;

import com.linglong.rpc.serialization.msgpack.MessageTypeException;
import com.linglong.rpc.serialization.msgpack.packer.Packer;
import com.linglong.rpc.serialization.msgpack.template.AbstractTemplate;
import com.linglong.rpc.serialization.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StackTraceElementArrayTemplate extends
        AbstractTemplate<StackTraceElement[]> {

	private static final Logger logger = LoggerFactory.getLogger(StackTraceElementArrayTemplate.class);
	
	private StackTraceElementTemplate steTemplate = StackTraceElementTemplate.getInstance();

	@Override
	public void write(Packer pk, StackTraceElement[] v, boolean required)
			throws IOException {
		if (v == null) {
			logger.error("Attempted to write null");
            throw new MessageTypeException("Attempted to write null");
        }
		
		pk.writeArrayBegin(v.length);
		
		for(StackTraceElement ste : v){
			steTemplate.write(pk, ste);
		}
		
		pk.writeArrayEnd();
	}

	@Override
	public StackTraceElement[] read(Unpacker u, StackTraceElement[] to,
                                    boolean required) throws IOException {
		if(u.trySkipNil()){
			return null;
		}
		
		int len = u.readArrayBegin();
		if(to == null || to.length != len){
			to = new StackTraceElement[len];
		}
		for(int i=0; i < len; i++){
			to[i] = this.steTemplate.read(u, null);
		}
		
		u.readArrayEnd();
		return to;
	}

	private static StackTraceElementArrayTemplate instance = new StackTraceElementArrayTemplate();
	
	private StackTraceElementArrayTemplate(){}
	
	public static StackTraceElementArrayTemplate getInstance(){
		return instance;
	}
}
