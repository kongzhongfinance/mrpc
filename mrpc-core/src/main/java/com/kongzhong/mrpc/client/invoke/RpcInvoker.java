package com.kongzhong.mrpc.client.invoke;

import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.client.RpcCallbackFuture;
import com.kongzhong.mrpc.exception.ConnectException;
import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Rpc执行器
 * <p>
 * Created by biezhi on 04/07/2017.
 */
public class RpcInvoker {

    @Getter
    private RpcRequest          request;
    private SimpleClientHandler clientHandler;

    public RpcInvoker(RpcRequest request, SimpleClientHandler clientHandler) {
        this.request = request;
        this.clientHandler = clientHandler;
        String address = clientHandler.getNettyClient().getAddress();
        this.request.getContext().put(Const.SERVER_HOST, address.split(":")[0]);
        this.request.getContext().put(Const.SERVER_PORT, address.split(":")[1]);

        this.request.getContext().put("remote_address", address);
    }

    public Object invoke() throws Throwable {
        if (!clientHandler.getChannel().isActive()) {
            throw new ConnectException(String.format("Server channel %s unActive.", clientHandler.getChannel()));
        }
        RpcContext.setAttachments(request.getContext());
        RpcCallbackFuture rpcCallbackFuture = clientHandler.asyncSendRequest(request);
        return rpcCallbackFuture.get(request.getWaitTimeout());
    }

}