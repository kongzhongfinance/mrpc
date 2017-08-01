package com.kongzhong.mrpc.transport.tcp;

import com.google.common.base.Throwables;
import com.kongzhong.mrpc.exception.SerializeException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.server.RpcSpringServer;
import com.kongzhong.mrpc.transport.netty.SimpleServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Tcp服务端处理器
 *
 * @author biezhi
 *         2017/4/19
 */
@Slf4j
public class TcpServerHandler extends SimpleServerHandler<RpcRequest> {

    TcpServerHandler() {
        super();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {

        super.channelRead0(ctx, request);

        log.debug("Server receive body: {}", JacksonSerialize.toJSONString(request));

        RpcResponse response = new RpcResponse();
        TcpResponseInvoker tcpResponseInvoker = new TcpResponseInvoker(request, response, serviceBeanMap);
        //非阻塞nio线程，复杂的业务逻辑丢给专门的线程池
        RpcSpringServer.submit(tcpResponseInvoker, ctx, request, response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Server receive body error", cause);
        this.sendError(ctx, cause);
    }

    /**
     * 错误处理
     *
     * @param ctx   ChannelHandlerContext
     * @param cause Throwable
     */
    private void sendError(ChannelHandlerContext ctx, Throwable cause) throws SerializeException {
        RpcResponse response = new RpcResponse();
        response.setSuccess(false);
        response.setException(Throwables.getStackTraceAsString(cause));
        ctx.writeAndFlush(response);
    }

}
