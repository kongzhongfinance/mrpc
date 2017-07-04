package com.kongzhong.mrpc.client.invoke;

import com.kongzhong.mrpc.client.RpcCallbackFuture;
import com.kongzhong.mrpc.exception.ConnectException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import lombok.AllArgsConstructor;

/**
 * Created by biezhi on 04/07/2017.
 */
@AllArgsConstructor
public class RpcInvoker {

    private RpcRequest request;
    private SimpleClientHandler clientHandler;

    public Object invoke() throws Exception {
        if (!clientHandler.getChannel().isActive()) {
            throw new ConnectException(String.format("Client Channel %s unactive.", clientHandler.getChannel()));
        }
        RpcCallbackFuture rpcCallbackFuture = clientHandler.sendRequest(request);
        return rpcCallbackFuture.get();
    }

    public RpcRequest getRequest() {
        return request;
    }
}
