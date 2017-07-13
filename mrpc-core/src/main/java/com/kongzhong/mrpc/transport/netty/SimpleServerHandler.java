package com.kongzhong.mrpc.transport.netty;

import com.kongzhong.mrpc.enums.EventType;
import com.kongzhong.mrpc.event.Event;
import com.kongzhong.mrpc.event.EventManager;
import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.server.RpcMapping;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 抽象服务端请求处理器
 *
 * @author biezhi
 *         2017/4/21
 */
@Slf4j
public abstract class SimpleServerHandler<T> extends SimpleChannelInboundHandler<T> {

    protected Map<String, ServiceBean> serviceBeanMap;

    public SimpleServerHandler() {
        this.serviceBeanMap = RpcMapping.me().getServiceBeanMap();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 客户端建立连接
        Event event = Event.builder().rpcContext(RpcContext.get()).build();
        event.setAttribute("clientAddress", ctx.channel().remoteAddress());
        EventManager.me().fireEvent(EventType.SERVER_CLIENT_CONNECTED, event);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // 客户端断开连接
        Event event = Event.builder().rpcContext(RpcContext.get()).build();
        event.setAttribute("clientAddress", ctx.channel().remoteAddress());
        EventManager.me().fireEvent(EventType.SERVER_CLIENT_DISCONNECT, event);
    }

    public void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        // 服务端接收到请求
        Event event = Event.builder().rpcContext(RpcContext.get()).build();
        event.setAttribute("clientAddress", ctx.channel().remoteAddress());
        event.setAttribute("message", msg);
        EventManager.me().fireEvent(EventType.SERVER_ACCEPT, event);
    }

    public abstract void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
}