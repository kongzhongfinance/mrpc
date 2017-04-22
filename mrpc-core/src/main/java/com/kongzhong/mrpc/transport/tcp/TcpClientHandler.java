package com.kongzhong.mrpc.transport.tcp;

import com.kongzhong.mrpc.client.RpcFuture;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.transport.SimpleRpcClientHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author biezhi
 *         2017/4/19
 */
public class TcpClientHandler extends SimpleRpcClientHandler<RpcResponse> {

    public static final Logger log = LoggerFactory.getLogger(TcpClientHandler.class);

    /**
     * 每次客户端发送一次RPC请求的 时候调用.
     *
     * @param request
     * @return
     */
    public RpcFuture sendRequest(RpcRequest request) {
        RpcFuture rpcFuture = new RpcFuture(request);
        mapCallBack.put(request.getRequestId(), rpcFuture);
        channel.writeAndFlush(request);
        return rpcFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        log.debug("rpc server response: {}", response);
        String messageId = response.getRequestId();
        RpcFuture rpcFuture = mapCallBack.get(messageId);
        if (rpcFuture != null) {
            mapCallBack.remove(messageId);
            rpcFuture.done(response);
        }
    }
}