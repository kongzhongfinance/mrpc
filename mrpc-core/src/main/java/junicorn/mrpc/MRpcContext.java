package junicorn.mrpc;

import com.google.common.collect.Maps;
import junicorn.mrpc.serialize.ProtostuffSerialize;
import junicorn.mrpc.serialize.RpcSerialize;

import java.util.Map;

/**
 * Created by biezhi on 2016/12/12.
 */
public final class MRpcContext {

    private static RpcSerialize rpcSerialize = new ProtostuffSerialize();

    public static RpcSerialize getRpcSerialize(){
        return rpcSerialize;
    }

    public static void setRpcSerialize(RpcSerialize rpcSerialize){
        rpcSerialize = rpcSerialize;
    }

    public static Map<String, Object> rpcServices = Maps.newConcurrentMap();

    public static <T> T getRpcBean(Class<T> type){
        return type.cast(rpcServices.get(type.getName()));
    }

}
