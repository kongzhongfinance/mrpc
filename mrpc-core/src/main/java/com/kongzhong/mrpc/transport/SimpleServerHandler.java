package com.kongzhong.mrpc.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * http请求处理器
 *
 * @author biezhi
 *         2017/4/21
 */
public abstract class SimpleServerHandler<T> extends SimpleChannelInboundHandler<T> {

    public static final Logger log = LoggerFactory.getLogger(SimpleServerHandler.class);

    protected Map<String, Object> handlerMap;

    public SimpleServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    public abstract void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception;

    public abstract void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;
}