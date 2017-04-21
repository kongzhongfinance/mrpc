package com.kongzhong.mrpc.client;

import com.google.common.reflect.AbstractInvocationHandler;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.transport.RpcClientHandler;
import com.kongzhong.mrpc.utils.StringUtils;

import java.lang.reflect.Method;

/**
 * 客户端代理
 *
 * @param <T>
 */
public class ClientProxy<T> extends AbstractInvocationHandler {

    public Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest(StringUtils.getUUID(),
                method.getDeclaringClass().getName(), method.getName(),
                method.getParameterTypes(), args);

        RpcClientHandler clientHandler = RpcServerLoader.me().getRpcClientHandler();
        RpcFuture rpcFuture = clientHandler.sendRequest(request);
        return rpcFuture.get();
    }

}