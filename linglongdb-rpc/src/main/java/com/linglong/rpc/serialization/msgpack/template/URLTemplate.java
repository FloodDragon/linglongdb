/**
 *  URLTemplate.java Created on 2015/1/30 10:11
 *
 */
package com.linglong.rpc.serialization.msgpack.template;

import com.linglong.rpc.serialization.msgpack.MessageTypeException;
import com.linglong.rpc.serialization.msgpack.packer.Packer;
import com.linglong.rpc.serialization.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * Created by stereo on 16-8-5.
 */
public class URLTemplate extends AbstractTemplate<URL> {

    private static final Logger logger = LoggerFactory.getLogger(URLTemplate.class);

    @Override
    public void write(Packer pk, URL v, boolean required) throws IOException {
        if (v == null) {
            if (required) {
                logger.error("Attempted to write null");
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
            return;
        }
        pk.write(v.toString());
    }

    @Override
    public URL read(Unpacker u, URL to, boolean required) throws IOException {
        if (!required && u.trySkipNil()) {
            return null;
        }
        String path = u.readString();
        return new URL(path);
    }

    private URLTemplate(){}

    private static URLTemplate instance = new URLTemplate();

    public static URLTemplate getInstance() {
        return instance;
    }

    public static void setInstance(URLTemplate instance) {
        URLTemplate.instance = instance;
    }
}