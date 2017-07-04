package com.kongzhong.mrpc.transport.netty;

import com.kongzhong.mrpc.enums.EventType;
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
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        // 客户端建立连接
        EventManager.me().fireEvent(EventType.SERVER_CLIENT_CONNECTED, RpcContext.get());
    }

    public void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        // 服务端接收到请求
        EventManager.me().fireEvent(EventType.SERVER_ACCEPT, RpcContext.get());
    }

    public abstract void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
}