package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.transport.SimpleClientHandler;

/**
 * 服务调用
 *
 * @author biezhi
 *         2017/4/24
 */
public class SimpleRpcInvoker<T> implements RpcInvoker {

    private SimpleClientHandler<T> clientHandler;

    public SimpleRpcInvoker(SimpleClientHandler<T> clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public Object invoke(RpcRequest request) throws Exception {
        if (!clientHandler.getChannel().isActive()) {
            throw new RpcException("Client Channel is unactive.");
        }
        RpcCallbackFuture rpcCallbackFuture = clientHandler.sendRequest(request);
        return rpcCallbackFuture.get();
    }

}