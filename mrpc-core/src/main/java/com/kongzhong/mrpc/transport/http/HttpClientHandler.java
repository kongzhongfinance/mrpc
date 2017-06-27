package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.client.RpcCallbackFuture;
import com.kongzhong.mrpc.model.RequestBody;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.transport.netty.NettyClient;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;

import static com.kongzhong.mrpc.Const.*;

/**
 * @author biezhi
 *         2017/4/19
 */
@Slf4j
public class HttpClientHandler extends SimpleClientHandler<FullHttpResponse> {

    public HttpClientHandler(NettyClient nettyClient) {
        super(nettyClient);
    }

    /**
     * 每次客户端发送一次RPC请求的 时候调用.
     *
     * @param request
     * @return
     */
    @Override
    public RpcCallbackFuture sendRequest(RpcRequest rpcRequest) {
        RpcCallbackFuture rpcCallbackFuture = new RpcCallbackFuture(rpcRequest);
        callbackFutureMap.put(rpcRequest.getRequestId(), rpcCallbackFuture);

        RequestBody requestBody = RequestBody.builder()
                .requestId(rpcRequest.getRequestId())
                .service(rpcRequest.getClassName())
                .method(rpcRequest.getMethodName())
                .parameters(Arrays.asList(rpcRequest.getParameters()))
                .build();

        try {
            String sendBody = JacksonSerialize.toJSONString(requestBody);

            log.debug("Request body: \n{}", JacksonSerialize.toJSONString(requestBody, true));

            DefaultFullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/rpc");
            req.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            req.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
            req.headers().set(HttpHeaders.Names.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);

            ByteBuf bbuf = Unpooled.wrappedBuffer(sendBody.getBytes(CharsetUtil.UTF_8));
            req.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bbuf.readableBytes());
            req.content().clear().writeBytes(bbuf);

            this.setChannelRequestId(rpcRequest.getRequestId());

            channel.writeAndFlush(req);
        } catch (Exception e) {
            log.error("Client send request error", e);
        }
        return rpcCallbackFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse httpResponse) throws Exception {

        log.debug("Channel read: {}", ctx.channel());

        String body = httpResponse.content().toString(CharsetUtil.UTF_8);
        if (StringUtils.isEmpty(body)) {
            return;
        }

        String requestId = httpResponse.headers().get(HEADER_REQUEST_ID);
        String serviceClass = httpResponse.headers().get(HEADER_SERVICE_CLASS);
        String methodName = httpResponse.headers().get(HEADER_METHOD_NAME);

        if (StringUtils.isEmpty(requestId) || StringUtils.isEmpty(serviceClass) || StringUtils.isEmpty(methodName)) {
            log.error("{}", body);
        }

        RpcResponse rpcResponse = JacksonSerialize.parseObject(body, RpcResponse.class);
        if (rpcResponse.getSuccess()) {
            log.debug("Response body: \n{}", body);
            Object result = rpcResponse.getResult();
            if (null != result && null != rpcResponse.getReturnType() && !rpcResponse.getReturnType().equals(Void.class)) {
                Method method = ReflectUtils.method(ReflectUtils.from(serviceClass), methodName);
                Object object = JacksonSerialize.parseObject(JacksonSerialize.toJSONString(result), method.getGenericReturnType());
                rpcResponse.setResult(object);
            }
        }

        RpcCallbackFuture rpcCallbackFuture = callbackFutureMap.get(requestId);
        if (rpcCallbackFuture != null) {
            callbackFutureMap.remove(requestId);
            rpcCallbackFuture.done(rpcResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Client accept error", cause);
        super.sendError(ctx, cause);
//        ctx.close();
    }

}