package com.kongzhong.mrpc.client;

import com.google.common.reflect.AbstractInvocationHandler;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.transfer.RpcClientHandler;

import java.lang.reflect.Method;
import java.util.UUID;

public class ClientProxy<T> extends AbstractInvocationHandler {

    public Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest(UUID.randomUUID().toString(),
                method.getDeclaringClass().getName(), method.getName(),
                method.getParameterTypes(), args);

        RpcClientHandler clientHandler = RpcServerLoader.me().getMessageSendHandler();
        RpcFuture rpcFuture = clientHandler.sendRequest(request);
        return rpcFuture.get();
    }
}
