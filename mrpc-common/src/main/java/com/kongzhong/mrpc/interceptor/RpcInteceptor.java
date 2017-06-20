package com.kongzhong.mrpc.interceptor;

/**
 * RPC请求拦截器
 *
 * @author biezhi
 *         2017/4/20
 */
@FunctionalInterface
interface RpcInteceptor {

    Object execute(Invocation invocation) throws Exception;

}
