package junicorn.mrpc.serialize;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import junicorn.mrpc.common.exception.RpcSerializeException;

/**
 * Created by biezhi on 2016/12/12.
 */
public class JSONSerialize implements RpcSerialize {

    @Override
    public <T> byte[] serialize(T obj) throws RpcSerializeException {
        return JSON.toJSONBytes(obj, SerializerFeature.SortField);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws RpcSerializeException {
        return JSON.parseObject(data, clazz, Feature.SortFeidFastMatch);
    }
}
