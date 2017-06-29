package com.kongzhong.mrpc.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.kongzhong.mrpc.exception.SerializeException;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kyro序列化实现
 *
 * @author biezhi
 *         2017/4/26
 */
@NoArgsConstructor
public class KyroSerialize implements RpcSerialize {

    private ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            // kryo.setRegistrationRequired(false);
            // kryo.setReferences(true);
            // kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        }
    };

    @Override
    public <T> byte[] serialize(T obj) throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Output output = new Output(bos)) {
            Kryo kryo = kryos.get();
            // writeObjectOrNull object.getClass()
            kryo.writeClassAndObject(output, obj);
            byte[] bs = output.toBytes();
            return bs;
        } catch (Exception e) {
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             Input input = new Input(bis)) {
            Kryo kryo = kryos.get();
            Object bean = kryo.readClassAndObject(input);
            return clazz.cast(bean);
        } catch (Exception e) {
            throw new SerializeException(e);
        }
    }

}
