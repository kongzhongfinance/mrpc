package com.kongzhong.mrpc.transport;

import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.client.RpcCallbackFuture;
import com.kongzhong.mrpc.client.cluster.Connections;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.exception.SerializeException;
import com.kongzhong.mrpc.model.RpcRequest;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象客户端请求处理器
 *
 * @author biezhi
 *         2017/4/19
 */
@Slf4j
public abstract class SimpleClientHandler<T> extends SimpleChannelInboundHandler<T> {

    protected SocketAddress socketAddress;

    protected NettyConfig nettyConfig;

    @Getter
    protected volatile Channel channel;

    @Getter
    @Setter
    protected String serverAddress;

    protected Map<String, RpcCallbackFuture> mapCallBack = new ConcurrentHashMap<>();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.socketAddress = this.channel.remoteAddress();
        log.debug("Channel actived: {}", this.channel);
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
        super.channelInactive(ctx);
        log.debug("Channel inactive: {}", this.channel);
        // 创建异步重连
        final EventLoop eventLoopGroup = this.channel.eventLoop();


        Connections.me().remove(this);

        // 创建异步重连
//        final EventLoop eventLoopGroup = this.channel.eventLoop();
//        Set<String> referNames = Sets.newHashSet();
//        List<Class<?>> referers = ClientConfig.me().getReferers();
//        if (null != referers && !referers.isEmpty()) {
//            referers.forEach(type -> referNames.add(type.getName()));
//        }
//
//        System.out.println("提交重连请求");
//        LISTENING_EXECUTOR_SERVICE.submit(new SimpleRequestCallback(referNames, eventLoopGroup, this.channel.remoteAddress()));
    }

    /**
     * 客户端关闭时调用
     */
    public void close() {
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
        RpcCallbackFuture rpcCallbackFuture = mapCallBack.get(requestId);
        if (rpcCallbackFuture != null) {
            mapCallBack.remove(requestId);
            rpcCallbackFuture.done(null);
        }
    }

}