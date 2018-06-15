package com.kongzhong.mrpc.transport.http;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.client.Connections;
import com.kongzhong.mrpc.client.RpcCallbackFuture;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.exception.SerializeException;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.model.RequestBody;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.transport.netty.NettyClient;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import static com.kongzhong.mrpc.Const.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.Values.GZIP;

/**
 * @author biezhi
 * 2017/4/19
 */
@Slf4j
public class HttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    protected NettyConfig nettyConfig;

    @Getter
    protected volatile Channel channel;

    @Getter
    @Setter
    protected NettyClient nettyClient;

    protected static boolean isShutdown;

    protected LongAdder hits = new LongAdder();

    public static final Map<String, RpcCallbackFuture> CALLBACK_FUTURE_MAP = Maps.newConcurrentMap();

    HttpClientHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel active: {}", this.channel);
        super.channelActive(ctx);
    }

    /**
     * 每次客户端发送一次RPC请求的 时候调用.
     *
     * @param rpcRequest RpcRequest
     * @return return RpcCallbackFuture
     */
    public RpcCallbackFuture asyncSendRequest(RpcRequest rpcRequest) {
        if (isShutdown) {
            throw new SystemException("Rpc client has been shutdown.");
        }
        RpcCallbackFuture rpcCallbackFuture = new RpcCallbackFuture(rpcRequest);
        CALLBACK_FUTURE_MAP.put(rpcRequest.getRequestId(), rpcCallbackFuture);

        RequestBody requestBody = RequestBody.builder()
                .requestId(rpcRequest.getRequestId())
                .service(rpcRequest.getClassName())
                .method(rpcRequest.getMethodName())
                .context(rpcRequest.getContext())
                .parameters(Arrays.asList(rpcRequest.getParameters()))
                .build();

        try {
            String sendBody = JacksonSerialize.toJSONString(requestBody);

            log.debug("Client send body: {}", JacksonSerialize.toJSONString(requestBody));

            DefaultFullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/rpc", false);
            req.headers().set(CONNECTION, KEEP_ALIVE);
            req.headers().set(ACCEPT_ENCODING, GZIP);
            req.headers().set(CONTENT_TYPE, TEXT_PLAIN);
            req.headers().set(HEADER_REQUEST_ID, rpcRequest.getRequestId());
            req.headers().set(HEADER_SERVICE_CLASS, rpcRequest.getClassName());
            req.headers().set(HEADER_METHOD_NAME, rpcRequest.getMethodName());

            ByteBuf bodyBuf = Unpooled.wrappedBuffer(sendBody != null ? sendBody.getBytes(CharsetUtil.UTF_8) : new byte[0]);
            req.headers().set(CONTENT_LENGTH, bodyBuf.readableBytes());
            req.content().clear().writeBytes(bodyBuf);

            this.setChannelRequestId(rpcRequest.getRequestId());

            channel.writeAndFlush(req).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (future.isSuccess()) {
                        log.debug("Client requestId [{}] send success.", rpcRequest.getRequestId());
                    } else {
                        log.debug("Client requestId [{}] send fail.", rpcRequest.getRequestId());
                        throw new SystemException("Client requestId [" + rpcRequest.getRequestId() + "] send fail.");
                    }
                }
            });

        } catch (Exception e) {
            log.error("Client send request error", e);
        }
        return rpcCallbackFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse httpResponse) throws Exception {
        log.debug("Client channel read: {}", ctx.channel());

        String body = httpResponse.content().toString(CharsetUtil.UTF_8);
        if (StringUtils.isEmpty(body)) {
            return;
        }

        String requestId    = httpResponse.headers().get(HEADER_REQUEST_ID);
        String serviceClass = httpResponse.headers().get(HEADER_SERVICE_CLASS);
        String methodName   = httpResponse.headers().get(HEADER_METHOD_NAME);

        if (StringUtils.isEmpty(requestId) || StringUtils.isEmpty(serviceClass) || StringUtils.isEmpty(methodName)) {
            log.error("{}", body);
        }

        RpcResponse rpcResponse = JacksonSerialize.parseObject(body, RpcResponse.class);

        if (rpcResponse.getSuccess()) {
            log.debug("Client receive body: {}", JacksonSerialize.toJSONString(rpcResponse));
            Object result = rpcResponse.getResult();
            if (null != result && null != rpcResponse.getReturnType()
                    && !rpcResponse.getReturnType().equals(Void.class)) {
                Method method = ReflectUtils.method(ReflectUtils.from(serviceClass), methodName);
                Object object = null;
                if (method != null) {
                    object = JacksonSerialize.parseObject(JacksonSerialize.toJSONString(result), method.getGenericReturnType());
                }
                rpcResponse.setResult(object);
            }
        }

        RpcCallbackFuture rpcCallbackFuture = CALLBACK_FUTURE_MAP.get(requestId);
        if (rpcCallbackFuture != null) {
            CALLBACK_FUTURE_MAP.remove(requestId);
            rpcCallbackFuture.done(rpcResponse);
        } else {
            log.error("Not found request id [{}]", requestId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (IOException.class.isInstance(cause) && cause.getMessage().contains("Connection reset by peer")) {
        } else {
            log.error("Client receive body error", cause);
            sendError(ctx, cause);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel inActive: {}", ctx.channel());
        this.nettyClient.cancelSchedule(ctx.channel());

        // 移除客户端Channel
        Connections.me().inActive(this.nettyClient.getAddress());

        ctx.channel().close().sync();
//        if (nettyClient.isRunning()) {
//            // 断线重连
//            nettyClient.asyncCreateChannel(ctx.channel().eventLoop());
//        }
//        super.channelInactive(ctx);
    }

    /**
     * 添加一次调用
     */
    public void addHit() {
        hits.add(1);
    }

    public Long getHits() {
        return hits.longValue();
    }

    /**
     * 客户端关闭时调用
     */
    public void close() throws InterruptedException {
        nettyClient.shutdown();
        this.nettyClient.cancelSchedule(channel);
        this.channel.close().sync();
    }

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
     * @param cause
     */
    protected void sendError(ChannelHandlerContext ctx, Throwable cause) throws SerializeException {
        Channel           channel           = ctx.channel();
        String            requestId         = channel.attr(AttributeKey.valueOf(Const.HEADER_REQUEST_ID)).get().toString();
        RpcCallbackFuture rpcCallbackFuture = CALLBACK_FUTURE_MAP.get(requestId);
        if (rpcCallbackFuture != null) {
            CALLBACK_FUTURE_MAP.remove(requestId);
            rpcCallbackFuture.done(null);
        }
    }

    public static void shutdown() {
        isShutdown = true;
    }

}