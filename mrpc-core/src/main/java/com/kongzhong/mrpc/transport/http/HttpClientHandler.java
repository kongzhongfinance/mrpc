package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.client.RpcCallbackFuture;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.model.RequestBody;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.trace.TraceConstants;
import com.kongzhong.mrpc.transport.netty.NettyClient;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.lang.reflect.Method;
import java.util.Arrays;

import static com.kongzhong.mrpc.Const.HEADER_METHOD_NAME;
import static com.kongzhong.mrpc.Const.HEADER_REQUEST_ID;
import static com.kongzhong.mrpc.Const.HEADER_SERVICE_CLASS;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCEPT_ENCODING;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Values.GZIP;

/**
 * @author biezhi
 * 2017/4/19
 */
@Slf4j
public class HttpClientHandler extends SimpleClientHandler<FullHttpResponse> {

    HttpClientHandler(NettyClient nettyClient) {
        super(nettyClient);
    }

    /**
     * 每次客户端发送一次RPC请求的 时候调用.
     *
     * @param rpcRequest RpcRequest
     * @return return RpcCallbackFuture
     */
    @Override
    public RpcCallbackFuture asyncSendRequest(RpcRequest rpcRequest) {
        if(isShutdown){
            throw new SystemException("Rpc client has been shutdown.");
        }
        RpcCallbackFuture rpcCallbackFuture = new RpcCallbackFuture(rpcRequest);
        callbackFutureMap.put(rpcRequest.getRequestId(), rpcCallbackFuture);

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

            DefaultFullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/rpc");
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

            channel.writeAndFlush(req);
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
        // TODO: 兼容期，过后删除
        if(null != rpcResponse.getContext()){
            MDC.put(TraceConstants.TRACE_ID, rpcResponse.getContext().get(TraceConstants.TRACE_ID));
        }

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

        RpcCallbackFuture rpcCallbackFuture = callbackFutureMap.get(requestId);
        if (rpcCallbackFuture != null) {
            callbackFutureMap.remove(requestId);
            rpcCallbackFuture.done(rpcResponse);
        } else {
            log.error("Not found request id [{}]", requestId);
        }
        MDC.remove(TraceConstants.TRACE_ID);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Client receive body error", cause);
        super.sendError(ctx, cause);
//        ctx.close();
    }

}