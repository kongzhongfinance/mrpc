package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;

/**
 * 服务执行器
 *
 * @author biezhi
 *         2017/4/24
 */
public class SimpleRpcProcessor<T> implements RpcProcessor {

    private SimpleClientHandler<T> clientHandler;

    public SimpleRpcProcessor(SimpleClientHandler<T> clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public Object processor(RpcRequest request) throws Exception {
        if (!clientHandler.getChannel().isActive()) {
            throw new RpcException(String.format("Client Channel %s unactive.", clientHandler.getChannel()));
        }
        RpcCallbackFuture rpcCallbackFuture = clientHandler.sendRequest(request);
        return rpcCallbackFuture.get();
    }

}