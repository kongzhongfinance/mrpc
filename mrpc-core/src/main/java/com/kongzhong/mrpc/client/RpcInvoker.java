package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.transport.SimpleClientHandler;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 服务调用
 *
 * @author biezhi
 *         2017/4/24
 */
public class RpcInvoker<T> {

    private SimpleClientHandler<T> clientHandler;

    public RpcInvoker(SimpleClientHandler<T> clientHandler) {
        this.clientHandler = clientHandler;
    }

    public Object invoke(RpcRequest request) throws Exception {
        if (!clientHandler.getChannel().isActive()) {
            throw new RpcException("Client Channel is unactive.");
        }
        RpcFuture rpcFuture = clientHandler.sendRequest(request);
        return rpcFuture.get();
    }

}