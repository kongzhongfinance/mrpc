package com.kongzhong.mrpc.transfer;

import com.kongzhong.mrpc.common.JSONUtils;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.server.RpcServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author biezhi
 *         2017/4/19
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    public static final Logger log = LoggerFactory.getLogger(RpcServerHandler.class);

    private Map<String, Object> handlerMap;

    public RpcServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {

        log.debug("request: [{}]", JSONUtils.toJSONString(request));

        RpcResponse response = new RpcResponse();
        // new 一个服务器消息处理线程
        ResponseCallback responseCallback = new ResponseCallback(request, response, handlerMap);
        // 将服务端的处理任务提交给服务端的 消息处理线程池
        //不要阻塞nio线程，复杂的业务逻辑丢给专门的线程池
        RpcServer.submit(responseCallback, ctx, request, response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
