package com.kongzhong.mrpc.transport.netty;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.client.RpcCallbackFuture;
import com.kongzhong.mrpc.client.cluster.Connections;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.exception.SerializeException;
import com.kongzhong.mrpc.model.RpcRequest;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 抽象客户端请求处理器
 *
 * @author biezhi
 *         2017/4/19
 */
@Slf4j
public abstract class SimpleClientHandler<T> extends SimpleChannelInboundHandler<T> {

    protected NettyConfig nettyConfig;

    @Getter
    protected volatile Channel channel;

    @Getter
    @Setter
    protected NettyClient nettyClient;

    protected final Map<String, RpcCallbackFuture> callbackFutureMap = Maps.newConcurrentMap();

    public SimpleClientHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel actived: {}", this.channel);
        nettyClient.resetRetryCount();
        super.channelActive(ctx);
    }

    /**
     * handler 中出现异常才会执行这个函数
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public abstract void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (nettyClient.isRunning()) {
            log.debug("Channel inactive: {}", ctx.channel());
            Connections.me().remove(this);

            // 断线重连
            nettyClient.createBootstrap(ctx.channel().eventLoop());
            super.channelInactive(ctx);
        }

    }

    /**
     * 客户端关闭时调用
     */
    public void close() {
        nettyClient.shutdown();
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public abstract RpcCallbackFuture sendRequest(RpcRequest request);

    /**
     * 在channel上保存一个requestId
     *
     * @param requestId
     */
    protected void setChannelRequestId(String requestId) {
        channel.attr(AttributeKey.valueOf(Const.HEADER_REQUEST_ID)).set(requestId);
    }

    /**
     * 错误处理
     *
     * @param ctx
     * @param status
     */
    protected void sendError(ChannelHandlerContext ctx, Throwable cause) throws SerializeException {
        Channel channel = ctx.channel();
        String requestId = channel.attr(AttributeKey.valueOf(Const.HEADER_REQUEST_ID)).get().toString();
        RpcCallbackFuture rpcCallbackFuture = callbackFutureMap.get(requestId);
        if (rpcCallbackFuture != null) {
            callbackFutureMap.remove(requestId);
            rpcCallbackFuture.done(null);
        }
    }

}