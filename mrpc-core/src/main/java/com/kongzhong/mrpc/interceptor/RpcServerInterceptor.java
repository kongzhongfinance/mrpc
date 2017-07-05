package com.kongzhong.mrpc.interceptor;

/**
 * RPC服务端请求拦截器
 *
 * @author biezhi
 *         2017/4/20
 */
public interface RpcServerInterceptor extends RpcInterceptor {

    Object execute(ServerInvocation invocation) throws Exception;

    @Override
    default Object execute(Invocation invocation) throws Exception {
        return execute((ServerInvocation) invocation);
    }

}
