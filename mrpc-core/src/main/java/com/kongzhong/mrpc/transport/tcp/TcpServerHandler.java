package com.kongzhong.mrpc.transport.tcp;

import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.server.RpcServer;
import com.kongzhong.mrpc.transport.SimpleServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Tcp服务端处理器
 *
 * @author biezhi
 *         2017/4/19
 */
@Slf4j
public class TcpServerHandler extends SimpleServerHandler<RpcRequest> {

    public TcpServerHandler(Map<String, Object> handlerMap) {
        super(handlerMap);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        log.debug("tcp server request: {}", request);
        RpcResponse response = new RpcResponse();
        TcpResponseCallback tcpResponseCallback = new TcpResponseCallback(request, response, handlerMap);
        //非阻塞nio线程，复杂的业务逻辑丢给专门的线程池
        RpcServer.submit(tcpResponseCallback, ctx, request, response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Tcp server handler error", cause);
        ctx.close();
    }
}
