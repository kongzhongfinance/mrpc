package com.kongzhong.mrpc.transport.tcp;

import com.kongzhong.mrpc.client.RpcCallbackFuture;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.transport.netty.NettyClient;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author biezhi
 *         2017/4/19
 */
@Slf4j
public class TcpClientHandler extends SimpleClientHandler<RpcResponse> {

    TcpClientHandler(NettyClient nettyClient) {
        super(nettyClient);
    }

    /**
     * 每次客户端发送一次RPC请求的 时候调用.
     *
     * @param request   RpcRequest对象
     * @return 返回一个RpcCallbackFuture
     */
    public RpcCallbackFuture sendRequest(RpcRequest request) {

        RpcCallbackFuture rpcCallbackFuture = new RpcCallbackFuture(request);
        callbackFutureMap.put(request.getRequestId(), rpcCallbackFuture);

        log.debug("Client send body: \n{}", JacksonSerialize.toJSONString(request, true));

        this.setChannelRequestId(request.getRequestId());
        channel.writeAndFlush(request);
        return rpcCallbackFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        if (response.getSuccess()) {
            log.debug("Client receive body: \n{}", JacksonSerialize.toJSONString(response, true));
        }
        String requestId = response.getRequestId();
        RpcCallbackFuture rpcCallbackFuture = callbackFutureMap.get(requestId);
        if (rpcCallbackFuture != null) {
            callbackFutureMap.remove(requestId);
            rpcCallbackFuture.done(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Client receive body error", cause);
        super.sendError(ctx, cause);
//        ctx.close();
    }

}