package com.kongzhong.mrpc.client.invoke;

import com.kongzhong.mrpc.client.RpcCallbackFuture;
import com.kongzhong.mrpc.exception.ConnectException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Rpc执行器
 * <p>
 * Created by biezhi on 04/07/2017.
 */
@AllArgsConstructor
public class RpcInvoker {

    @Getter
    private RpcRequest          request;
    private SimpleClientHandler clientHandler;

    public Object invoke() throws Exception {
        if (!clientHandler.getChannel().isActive()) {
            throw new ConnectException(String.format("Server channel %s unActive.", clientHandler.getChannel()));
        }
        RpcCallbackFuture rpcCallbackFuture = clientHandler.asyncSendRequest(request);
        return rpcCallbackFuture.get(request.getWaitTimeout());
    }

}