package com.kongzhong.mrpc.serialize;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.exception.SerializeException;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.NoArgsConstructor;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;

/**
 * Protostuff序列化实现
 * <p>
 * Created by biezhi on 2016/11/6.
 */
@NoArgsConstructor
public class ProtostuffSerialize implements RpcSerialize {

    private static final Map<Class<?>, Schema<?>> cachedSchema = Maps.newConcurrentMap();

    private static final Objenesis objenesis = new ObjenesisStd(true);

    /**
     * 序列化（对象 -> 字节数组）
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> byte[] serialize(T obj) throws Exception {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new SerializeException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化（字节数组 -> 对象）
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) throws Exception {
        try {
            T message = (T) objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new SerializeException(e.getMessage(), e);
        }
    }

    private <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }

}
