package junicorn.mrpc.serialize;

import junicorn.mrpc.common.config.Constant;

/**
 * Created by biezhi on 2017/1/14.
 */
public class RpcSerializeBuilder {

    public static RpcSerialize build(String serialize){
        if(null != serialize){
            if(serialize.equalsIgnoreCase(Constant.PROTOSTUFF)){
                return new ProtostuffSerialize();
            } else if(serialize.equalsIgnoreCase(Constant.JSON)){
                return new JSONSerialize();
            } else if(serialize.equalsIgnoreCase(Constant.KRYO)){
                return new KryoSerialize();
            }
        }
        return null;
    }

}
