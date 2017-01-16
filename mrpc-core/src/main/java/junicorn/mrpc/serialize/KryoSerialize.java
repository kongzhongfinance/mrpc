package junicorn.mrpc.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by biezhi on 2017/1/14.
 */
public class KryoSerialize implements RpcSerialize {

    private Kryo kryo = new Kryo();

    @Override
    public <T> byte[] serialize(T obj) throws Exception {
        kryo.setReferences(false);
        kryo.register(obj.getClass(), new JavaSerializer());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        kryo.writeClassAndObject(output, obj);
        output.flush();
        output.close();

        byte[] b = baos.toByteArray();
        baos.flush();
        baos.close();
        return b;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        kryo.setReferences(false);
        kryo.register(clazz, new JavaSerializer());
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        Input input = new Input(bais);
        return (T) kryo.readClassAndObject(input);
    }
}
