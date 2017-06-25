package com.kongzhong.mrpc.transport.netty;

import com.kongzhong.mrpc.model.ServiceBean;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 抽象服务端请求处理器
 *
 * @author biezhi
 *         2017/4/21
 */
public abstract class SimpleServerHandler<T> extends SimpleChannelInboundHandler<T> {

    public static final Logger log = LoggerFactory.getLogger(SimpleServerHandler.class);

    protected Map<String, ServiceBean> serviceBeanMap;

    public SimpleServerHandler(Map<String, ServiceBean> serviceBeanMap) {
        this.serviceBeanMap = serviceBeanMap;
    }

    public abstract void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception;

    public abstract void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
}