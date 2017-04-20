package com.kongzhong.mrpc.client;

import com.google.common.reflect.AbstractInvocationHandler;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.transfer.RpcClientHandler;

import java.lang.reflect.Method;
import java.util.UUID;

public class ClientProxy<T> extends AbstractInvocationHandler {

    public Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        //根据执行函数 反射 获取request的信息
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setServiceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);

        RpcClientHandler clientHandler = RpcServerLoader.me().getMessageSendHandler();
        RpcFuture rpcFuture = clientHandler.sendRequest(request);
        return rpcFuture.get();
    }
}
