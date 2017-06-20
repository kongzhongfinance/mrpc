package com.kongzhong.mrpc.interceptor;

/**
 * RPC服务端请求拦截器
 *
 * @author biezhi
 *         2017/4/20
 */
public interface RpcClientInteceptor extends RpcInteceptor {

    Object execute(ClientInvocation invocation) throws Exception;

    @Override
    default Object execute(Invocation invocation) throws Exception {
        return execute((ClientInvocation) invocation);
    }

}
