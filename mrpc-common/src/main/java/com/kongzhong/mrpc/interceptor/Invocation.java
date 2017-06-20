package com.kongzhong.mrpc.interceptor;

import com.kongzhong.mrpc.model.RpcRequest;

/**
 * 拦截器执行器
 * <p>
 * Created by biezhi on 2016/12/23.
 */
public interface Invocation {

    RpcRequest rpcRequest();

    Object next() throws Exception;

}