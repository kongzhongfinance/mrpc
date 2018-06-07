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
 * 2017/4/21
 */
@Slf4j
public abstract class SimpleServerHandler<T> extends SimpleChannelInboundHandler<T> {

    protected Map<String, ServiceBean> serviceBeanMap;

    protected static boolean IS_SHUTDOWN = false;

    public SimpleServerHandler() {
        this.serviceBeanMap = RpcMapping.me().getServiceBeanMap();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.debug("Channel Inactive {}", ctx.channel());
    }

    @Override
    public abstract void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;

    public static void shutdown() {
        log.info("Shutdown now.");
        IS_SHUTDOWN = true;
        EventManager.me().fireEvent(EventType.SERVER_OFFLINE, null);
    }

}