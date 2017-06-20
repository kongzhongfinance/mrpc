package com.kongzhong.mrpc.transport.tcp;

import com.google.common.base.Throwables;
import com.kongzhong.mrpc.exception.SerializeException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.model.RpcRet;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.server.RpcServer;
import com.kongzhong.mrpc.transport.SimpleServerHandler;
import com.kongzhong.mrpc.utils.JSONUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Tcp服务端处理器
 *
 * @author biezhi
 *         2017/4/19
 */
@Slf4j
public class TcpServerHandler extends SimpleServerHandler<RpcRequest> {

    public TcpServerHandler(Map<String, ServiceBean> serviceBeanMap) {
        super(serviceBeanMap);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        log.debug("Tcp server request: {}", request);
        RpcResponse response = new RpcResponse();
        TcpResponseCallback tcpResponseCallback = new TcpResponseCallback(request, response, serviceBeanMap);
        //非阻塞nio线程，复杂的业务逻辑丢给专门的线程池
        RpcServer.submit(tcpResponseCallback, ctx, request, response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Tcp server accept error", cause);
        this.sendError(ctx, cause);
//        ctx.close();
    }

    /**
     * 错误处理
     *
     * @param ctx
     * @param status
     */
    private void sendError(ChannelHandlerContext ctx, Throwable cause) throws SerializeException {
        RpcResponse response = new RpcResponse();
        response.setSuccess(false);
        response.setException(Throwables.getStackTraceAsString(cause));
        ctx.writeAndFlush(response);
    }

}
