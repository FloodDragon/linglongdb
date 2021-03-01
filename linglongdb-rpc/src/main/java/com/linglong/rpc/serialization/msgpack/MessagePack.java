//
// MessagePack for Java
//
// Copyright (C) 2009 - 2013 FURUHASHI Sadayuki
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package com.linglong.rpc.serialization.msgpack;

import com.linglong.rpc.serialization.msgpack.packer.*;
import com.linglong.rpc.serialization.msgpack.template.Template;
import com.linglong.rpc.serialization.msgpack.template.TemplateRegistry;
import com.linglong.rpc.serialization.msgpack.type.Value;
import com.linglong.rpc.serialization.msgpack.unpacker.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

/**
 * <p>
 * This is basic class to use MessagePack for Java. It creates serializers and
 * deserializers for objects of classes.
 * </p>
 * <p>
 * <p>
 * See <a
 * href="http://wiki.msgpack.org/display/MSGPACK/QuickStart+for+Java">Quick
 * Start for Java</a> on MessagePack wiki.
 * </p>
 */

/**
 * 基于公版MessagePack 0.6.0 改进
 * (可序列化和反序列化任何Java对象,同时放弃MessagePack跨语言的特性)
 * <p>
 * @author Stereo
 */
public class MessagePack {
    private TemplateRegistry registry;

    /**
     * @since 0.6.0
     */
    public MessagePack() {
        registry = new TemplateRegistry(null);
    }

    /**
     * @param msgpack
     * @since 0.6.0
     */
    public MessagePack(MessagePack msgpack) {
        registry = new TemplateRegistry(msgpack.registry);
    }

    protected MessagePack(TemplateRegistry registry) {
        this.registry = registry;
    }

    /**
     * @param cl
     * @since 0.6.0
     */
    public void setClassLoader(final ClassLoader cl) {
        registry.setClassLoader(cl);
    }

    /**
     * Returns serializer that enables serializing objects into
     * {@link OutputStream} object.
     *
     * @param out output stream
     * @return stream-based serializer
     * @since 0.6.0
     */
    public Packer createPacker(OutputStream out) {
        return new MessagePackPacker(this, out);
    }

    /**
     * Returns serializer that enables serializing objects into buffer.
     *
     * @return buffer-based serializer
     * @since 0.6.0
     */
    public BufferPacker createBufferPacker() {
        return new MessagePackBufferPacker(this);
    }

    /**
     * Returns serializer that enables serializing objects into buffer.
     *
     * @param bufferSize initial size of buffer
     * @return buffer-based serializer
     * @since 0.6.0
     */
    public BufferPacker createBufferPacker(int bufferSize) {
        return new MessagePackBufferPacker(this, bufferSize);
    }

    /**
     * Returns deserializer that enables deserializing
     * {@link InputStream} object.
     *
     * @param in input stream
     * @return stream-based deserializer
     * @since 0.6.0
     */
    public Unpacker createUnpacker(InputStream in) {
        return new MessagePackUnpacker(this, in);
    }

    /**
     * Returns empty deserializer that enables deserializing buffer.
     *
     * @return buffer-based deserializer
     * @since 0.6.0
     */
    public BufferUnpacker createBufferUnpacker() {
        return new MessagePackBufferUnpacker(this);
    }

    /**
     * Returns deserializer that enables deserializing buffer.
     *
     * @param bytes input byte array
     * @return buffer-based deserializer
     * @since 0.6.0
     */
    public BufferUnpacker createBufferUnpacker(byte[] bytes) {
        return createBufferUnpacker().wrap(bytes);
    }

    /**
     * Returns deserializer that enables deserializing buffer.
     *
     * @param bytes
     * @param off
     * @param len
     * @return buffer-based deserializer
     * @since 0.6.0
     */
    public BufferUnpacker createBufferUnpacker(byte[] bytes, int off, int len) {
        return createBufferUnpacker().wrap(bytes, off, len);
    }

    /**
     * Returns deserializer that enables deserializing buffer.
     *
     * @param buffer input {@link ByteBuffer} object
     * @return buffer-based deserializer
     * @since 0.6.0
     */
    public BufferUnpacker createBufferUnpacker(ByteBuffer buffer) {
        return createBufferUnpacker().wrap(buffer);
    }

    /**
     * Serializes specified object.
     *
     * @param v serialized object
     * @return output byte array
     * @throws IOException
     * @since 0.6.0
     */
    public <T> byte[] write(T v) throws IOException {
        BufferPacker pk = createBufferPacker();
        if (v == null) {
            pk.writeNil();
        } else {
            @SuppressWarnings("unchecked")
            Template<T> tmpl = registry.lookup(v.getClass());
            tmpl.write(pk, v);
        }
        return pk.toByteArray();
    }

    /**
     * Serializes specified object. It allows serializing object by specified
     * template.
     *
     * @param v
     * @param template
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> byte[] write(T v, Template<T> template) throws IOException {
        BufferPacker pk = createBufferPacker();
        template.write(pk, v);
        return pk.toByteArray();
    }

    /**
     * Serializes specified object to output stream.
     *
     * @param out output stream
     * @param v   serialized object
     * @throws IOException
     * @since 0.6.0
     */
    public <T> void write(OutputStream out, T v) throws IOException {
        Packer pk = createPacker(out);
        if (v == null) {
            pk.writeNil();
        } else {
            @SuppressWarnings("unchecked")
            Template<T> tmpl = registry.lookup(v.getClass());
            tmpl.write(pk, v);
        }
    }

    /**
     * Serializes object to output stream by specified template.
     *
     * @param out      output stream
     * @param v        serialized object
     * @param template serializer/deserializer for the object
     * @throws IOException
     * @since 0.6.0
     */
    public <T> void write(OutputStream out, T v, Template<T> template)
            throws IOException {
        Packer pk = createPacker(out);
        template.write(pk, v);
    }

    /**
     * Serializes {@link Value} object to byte array.
     *
     * @param v serialized {@link Value} object
     * @return output byte array
     * @throws IOException
     * @since 0.6.0
     */
    public byte[] write(Value v) throws IOException {
        // FIXME ValueTemplate should do this
        BufferPacker pk = createBufferPacker();
        pk.write(v);
        return pk.toByteArray();
    }

    /**
     * Deserializes specified byte array to {@link Value}
     * object.
     *
     * @param bytes input byte array
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public Value read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    /**
     * Deserializes byte array to {@link Value} object.
     *
     * @param bytes
     * @param off
     * @param len
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public Value read(byte[] bytes, int off, int len) throws IOException {
        return createBufferUnpacker(bytes, off, len).readValue();
    }

    /**
     * Deserializes {@link ByteBuffer} object to
     * {@link Value} object.
     *
     * @param buffer input buffer
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public Value read(ByteBuffer buffer) throws IOException {
        return createBufferUnpacker(buffer).readValue();
    }

    /**
     * Deserializes input stream to {@link Value} object.
     *
     * @param in input stream
     * @return deserialized {@link Value} object
     * @throws IOException
     * @since 0.6.0
     */
    public Value read(InputStream in) throws IOException {
        return createUnpacker(in).readValue();
    }

    /**
     * Deserializes byte array to object.
     *
     * @param bytes input byte array
     * @param v
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> T read(byte[] bytes, T v) throws IOException {
        @SuppressWarnings("unchecked")
        Template<T> tmpl = registry.lookup(v.getClass());
        return read(bytes, v, tmpl);
    }

    /**
     * Deserializes byte array to object according to template.
     *
     * @param bytes input byte array
     * @param tmpl  template
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> T read(byte[] bytes, Template<T> tmpl) throws IOException {
        return read(bytes, null, tmpl);
    }

    /**
     * Deserializes byte array to object of specified class.
     *
     * @param bytes input byte array
     * @param c
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> T read(byte[] bytes, Class<T> c) throws IOException {
        @SuppressWarnings("unchecked")
        Template<T> tmpl = registry.lookup(c);
        return read(bytes, null, tmpl);
    }

    /**
     * Deserializes byte array to object according to specified template.
     *
     * @param bytes input byte array
     * @param v
     * @param tmpl  template
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> T read(byte[] bytes, T v, Template<T> tmpl) throws IOException {
        BufferUnpacker u = createBufferUnpacker(bytes);
        return (T) tmpl.read(u, v);
    }

    /**
     * Deserializes byte array to object.
     *
     * @param bytes input byte array
     * @param v
     * @return
     * @throws IOException
     * @since 0.6.8
     */
    public <T> T read(byte[] bytes, int off, int len, Class<T> c) throws IOException {
        @SuppressWarnings("unchecked")
        Template<T> tmpl = registry.lookup(c);
        BufferUnpacker u = createBufferUnpacker(bytes, off, len);
        return (T) tmpl.read(u, null);
    }

    /**
     * Deserializes buffer to object.
     *
     * @param b input {@link ByteBuffer} object
     * @param v
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> T read(ByteBuffer b, T v) throws IOException {
        @SuppressWarnings("unchecked")
        Template<T> tmpl = registry.lookup(v.getClass());
        return read(b, v, tmpl);
    }

    /**
     * Deserializes buffer to object according to template.
     *
     * @param b    input buffer object
     * @param tmpl
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> T read(ByteBuffer b, Template<T> tmpl) throws IOException {
        return read(b, null, tmpl);
    }

    /**
     * Deserializes buffer to object of specified class.
     *
     * @param b
     * @param c
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> T read(ByteBuffer b, Class<T> c) throws IOException {
        @SuppressWarnings("unchecked")
        Template<T> tmpl = registry.lookup(c);
        return read(b, null, tmpl);
    }

    /**
     * Deserializes buffer to object according to template.
     *
     * @param b    input buffer object
     * @param v
     * @param tmpl
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> T read(ByteBuffer b, T v, Template<T> tmpl) throws IOException {
        BufferUnpacker u = createBufferUnpacker(b);
        return tmpl.read(u, v);
    }

    /**
     * Deserializes input stream to object.
     *
     * @param in input stream
     * @param v
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> T read(InputStream in, T v) throws IOException {
        @SuppressWarnings("unchecked")
        Template<T> tmpl = registry.lookup(v.getClass());
        return read(in, v, tmpl);
    }

    /**
     * Deserializes input stream to object according to template.
     *
     * @param in   input stream
     * @param tmpl
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> T read(InputStream in, Template<T> tmpl) throws IOException {
        return read(in, null, tmpl);
    }

    /**
     * Deserializes input stream to object of specified class.
     *
     * @param in
     * @param c
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> T read(InputStream in, Class<T> c) throws IOException {
        @SuppressWarnings("unchecked")
        Template<T> tmpl = registry.lookup(c);
        return read(in, null, tmpl);
    }

    /**
     * Deserializes input stream to object according to template
     *
     * @param in   input stream
     * @param v
     * @param tmpl
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> T read(InputStream in, T v, Template<T> tmpl) throws IOException {
        Unpacker u = createUnpacker(in);
        return tmpl.read(u, v);
    }

    /**
     * Converts specified {@link Value} object to object.
     *
     * @param v
     * @param to
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> T convert(Value v, T to) throws IOException {
        @SuppressWarnings("unchecked")
        Template<T> tmpl = registry.lookup(to.getClass());
        return tmpl.read(new Converter(this, v), to);
    }

    /**
     * Converts {@link Value} object to object specified class.
     *
     * @param v
     * @param c
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> T convert(Value v, Class<T> c) throws IOException {
        @SuppressWarnings("unchecked")
        Template<T> tmpl = registry.lookup(c);
        return tmpl.read(new Converter(this, v), null);
    }

    /**
     * Converts {@link Value} object to object according to template
     *
     * @param v
     * @param tmpl
     * @return
     * @throws IOException
     * @since 0.6.8
     */
    public <T> T convert(Value v, Template<T> tmpl) throws IOException {
        return tmpl.read(new Converter(this, v), null);
    }

    /**
     * Unconverts specified object to {@link Value} object.
     *
     * @param v
     * @return
     * @throws IOException
     * @since 0.6.0
     */
    public <T> Value unconvert(T v) throws IOException {
        Unconverter pk = new Unconverter(this);
        if (v == null) {
            pk.writeNil();
        } else {
            @SuppressWarnings("unchecked")
            Template<T> tmpl = registry.lookup(v.getClass());
            tmpl.write(pk, v);
        }
        return pk.getResult();
    }

    /**
     * Registers {@link Template} object for objects of
     * specified class. <tt>Template</tt> object is a pair of serializer and
     * deserializer for object serialization. It is generated automatically.
     *
     * @param type
     * @since 0.6.0
     */
    public void register(Class<?> type) {
        registry.register(type);
    }

    /**
     * Registers specified {@link Template} object
     * associated by class.
     *
     * @param type
     * @param template
     * @see #register(Class)
     * @since 0.6.0
     */
    public <T> void register(Class<T> type, Template<T> template) {
        registry.register(type, template);
    }

    /**
     * Unregisters {@link Template} object for objects of
     * specified class.
     *
     * @param type
     * @return
     * @since 0.6.0
     */
    public boolean unregister(Class<?> type) {
        return registry.unregister(type);
    }

    /**
     * Unregisters all {@link Template} objects that have
     * been registered in advance.
     *
     * @since 0.6.0
     */
    public void unregister() {
        registry.unregister();
    }

    /**
     * Looks up a {@link Template} object, which is
     * serializer/deserializer associated by specified class.
     *
     * @param type
     * @return
     * @since 0.6.0
     */
    @SuppressWarnings("unchecked")
    public <T> Template<T> lookup(Class<T> type) {
        return registry.lookup(type);
    }

    public Template<?> lookup(Type type) {
        return registry.lookup(type);
    }

    private static final MessagePack globalMessagePack = new MessagePack();

    /**
     * Serializes specified object and returns the byte array.
     *
     * @param v
     * @return
     * @throws IOException
     * @deprecated {@link MessagePack#write(Object)}
     */
    @Deprecated
    public static byte[] pack(Object v) throws IOException {
        return globalMessagePack.write(v);
    }

    /**
     * Serializes specified object to output stream.
     *
     * @param out
     * @param v
     * @throws IOException
     * @deprecated {@link MessagePack#write(OutputStream, Object)}
     */
    @Deprecated
    public static void pack(OutputStream out, Object v) throws IOException {
        globalMessagePack.write(out, v);
    }

    /**
     * Serializes object by specified template and return the byte array.
     *
     * @param v
     * @param template
     * @return
     * @throws IOException
     * @deprecated {@link MessagePack#write(Object, Template)}
     */
    @Deprecated
    public static <T> byte[] pack(T v, Template<T> template) throws IOException {
        return globalMessagePack.write(v, template);
    }

    /**
     * Serializes object to output stream. The object is serialized by specified
     * template.
     *
     * @param out
     * @param v
     * @param template
     * @throws IOException
     * @deprecated {@link MessagePack#write(OutputStream, Object, Template)}
     */
    @Deprecated
    public static <T> void pack(OutputStream out, T v, Template<T> template)
            throws IOException {
        globalMessagePack.write(out, v, template);
    }

    /**
     * Converts byte array to {@link Value} object.
     *
     * @param bytes
     * @return
     * @throws IOException
     * @deprecated {@link MessagePack#read(byte[])}
     */
    @Deprecated
    public static Value unpack(byte[] bytes) throws IOException {
        return globalMessagePack.read(bytes);
    }

    @Deprecated
    public static <T> T unpack(byte[] bytes, Template<T> template) throws IOException {
        BufferUnpacker u = new MessagePackBufferUnpacker(globalMessagePack).wrap(bytes);
        return template.read(u, null);
    }

    @Deprecated
    public static <T> T unpack(byte[] bytes, Template<T> template, T to) throws IOException {
        BufferUnpacker u = new MessagePackBufferUnpacker(globalMessagePack).wrap(bytes);
        return template.read(u, to);
    }

    /**
     * Deserializes byte array to object of specified class.
     *
     * @param bytes
     * @param klass
     * @return
     * @throws IOException
     * @deprecated {@link MessagePack#read(byte[], Class)}
     */
    @Deprecated
    public static <T> T unpack(byte[] bytes, Class<T> klass) throws IOException {
        return globalMessagePack.read(bytes, klass);
    }

    /**
     * Deserializes byte array to object.
     *
     * @param bytes
     * @param to
     * @return
     * @throws IOException
     */
    @Deprecated
    public static <T> T unpack(byte[] bytes, T to) throws IOException {
        return globalMessagePack.read(bytes, to);
    }

    /**
     * Converts input stream to {@link Value} object.
     *
     * @param in
     * @return
     * @throws IOException
     * @deprecated {@link MessagePack#read(InputStream)}
     */
    @Deprecated
    public static Value unpack(InputStream in) throws IOException {
        return globalMessagePack.read(in);
    }

    /**
     * @param in
     * @param tmpl
     * @return
     * @throws IOException
     * @throws MessageTypeException
     * @deprecated
     */
    @Deprecated
    public static <T> T unpack(InputStream in, Template<T> tmpl)
            throws IOException, MessageTypeException {
        return tmpl.read(new MessagePackUnpacker(globalMessagePack, in), null);
    }

    /**
     * @param in
     * @param tmpl
     * @param to
     * @return
     * @throws IOException
     * @throws MessageTypeException
     * @deprecated
     */
    @Deprecated
    public static <T> T unpack(InputStream in, Template<T> tmpl, T to)
            throws IOException, MessageTypeException {
        return (T) tmpl.read(new MessagePackUnpacker(globalMessagePack, in), to);
    }

    /**
     * Deserializes input stream to object of specified class.
     *
     * @param in
     * @param klass
     * @return
     * @throws IOException
     * @deprecated {@link MessagePack#read(InputStream, Class)}
     */
    @Deprecated
    public static <T> T unpack(InputStream in, Class<T> klass)
            throws IOException {
        return globalMessagePack.read(in, klass);
    }

    /**
     * Deserializes input stream to object.
     *
     * @param in
     * @param to
     * @return
     * @throws IOException
     * @deprecated {@link MessagePack#read(InputStream, Object)}
     */
    @Deprecated
    public static <T> T unpack(InputStream in, T to) throws IOException {
        return globalMessagePack.read(in, to);
    }
}
