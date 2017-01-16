package junicorn.mrpc.serialize;

/**
 * Created by biezhi on 2016/11/6.
 */
public interface RpcSerialize {

    <T> byte[] serialize(T obj) throws Exception;

    <T> T deserialize(byte[] data, Class<T> clazz) throws Exception;

}
