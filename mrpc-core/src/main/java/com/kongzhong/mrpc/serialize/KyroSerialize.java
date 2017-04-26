package com.kongzhong.mrpc.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author biezhi
 *         2017/4/26
 */
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

    private Kryo get() {
        return kryos.get();
    }

    @Override
    public <T> byte[] serialize(T obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);

        Kryo kryo = get();
        // writeObjectOrNull object.getClass()
        kryo.writeClassAndObject(output, obj);
        byte[] bs = output.toBytes();

        bos.close();
        output.close();

        return bs;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        Input input = new Input(bis);

        Kryo kryo = get();
        Object bean = kryo.readClassAndObject(input);

        bis.close();
        input.close();
        return clazz.cast(bean);
    }

}
