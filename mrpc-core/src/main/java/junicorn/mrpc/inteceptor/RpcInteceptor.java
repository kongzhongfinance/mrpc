package junicorn.mrpc.inteceptor;

/**
 * rpc拦截器
 *
 * Created by biezhi on 2016/12/23.
 */
public interface RpcInteceptor {

    Object execute(RpcInvocation invocation) throws Exception;

}
