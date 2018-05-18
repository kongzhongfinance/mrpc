package com.kongzhong.mrpc.interceptor;

import com.kongzhong.mrpc.client.invoke.ClientInvocation;

/**
 * RPC服务端请求拦截器
 *
 * @author biezhi
 *         2017/4/20
 */
public interface RpcClientInterceptor extends RpcInterceptor {

    Object execute(ClientInvocation invocation) throws Throwable;

    @Override
    default Object execute(Invocation invocation) throws Throwable {
        return execute((ClientInvocation) invocation);
    }

}
