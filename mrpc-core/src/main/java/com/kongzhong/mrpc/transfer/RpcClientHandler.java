package com.kongzhong.mrpc.transfer;

import com.kongzhong.mrpc.client.RpcFuture;
import com.kongzhong.mrpc.common.JSONUtils;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author biezhi
 *         2017/4/19
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    public static final Logger log = LoggerFactory.getLogger(RpcClientHandler.class);

    private Map<String, RpcFuture> mapCallBack = new ConcurrentHashMap<>();

    private volatile Channel channel;

    private SocketAddress socketAddress;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
        log.debug("channel registered");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.socketAddress = this.channel.remoteAddress();
        log.debug("channel actived");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        log.debug("rpc server response: {}", response);
        String messageId = response.getRequestId();
        RpcFuture rpcFuture = mapCallBack.get(messageId);
        if (rpcFuture != null) {
            mapCallBack.remove(messageId);
            rpcFuture.done(response);
        }
    }

    /**
     * handler 中出现异常才会执行这个函数
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("", cause);
        ctx.close();
    }

    /**
     * 客户端关闭时调用
     */
    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 每次客户端发送一次RPC请求的 时候调用.
     *
     * @param request
     * @return
     */
    public RpcFuture sendRequest(RpcRequest request) {
        RpcFuture rpcFuture = new RpcFuture(request);
        mapCallBack.put(request.getRequestId(), rpcFuture);
        channel.writeAndFlush(request);
        return rpcFuture;
    }

}