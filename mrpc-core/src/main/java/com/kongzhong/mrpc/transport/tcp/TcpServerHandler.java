package com.kongzhong.mrpc.transport.tcp;

import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.server.RpcServer;
import com.kongzhong.mrpc.transport.SimpleServerHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Tcp服务端处理器
 *
 * @author biezhi
 *         2017/4/19
 */
public class TcpServerHandler extends SimpleServerHandler<RpcRequest> {

    public static final Logger log = LoggerFactory.getLogger(TcpServerHandler.class);

    public TcpServerHandler(Map<String, Object> handlerMap) {
        super(handlerMap);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        log.debug("request: {}", request);
        RpcResponse response = new RpcResponse();
        // new 一个服务器消息处理线程
        TcpResponseCallback tcpResponseCallback = new TcpResponseCallback(request, response, handlerMap);
        // 将服务端的处理任务提交给服务端的 消息处理线程池
        //不要阻塞nio线程，复杂的业务逻辑丢给专门的线程池
        RpcServer.submit(tcpResponseCallback, ctx, request, response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
