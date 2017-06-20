package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.client.RpcCallbackFuture;
import com.kongzhong.mrpc.model.RequestBody;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.transport.SimpleClientHandler;
import com.kongzhong.mrpc.utils.JSONUtils;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.kongzhong.mrpc.model.Const.HEADER_REQUEST_ID;

/**
 * @author biezhi
 *         2017/4/19
 */
@Slf4j
public class HttpClientHandler extends SimpleClientHandler<FullHttpResponse> {

    /**
     * 每次客户端发送一次RPC请求的 时候调用.
     *
     * @param request
     * @return
     */
    @Override
    public RpcCallbackFuture sendRequest(RpcRequest rpcRequest) {

        RpcCallbackFuture rpcCallbackFuture = new RpcCallbackFuture(rpcRequest);
        mapCallBack.put(rpcRequest.getRequestId(), rpcCallbackFuture);

        RequestBody requestBody = new RequestBody();
        requestBody.setRequestId(rpcRequest.getRequestId());
        requestBody.setService(rpcRequest.getClassName());
        requestBody.setMethod(rpcRequest.getMethodName());
        requestBody.setParameters(Arrays.asList(rpcRequest.getParameters()));

        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        if (null != parameterTypes) {
            List<String> parameterTypesJSON = new ArrayList<>();
            for (Class<?> type : parameterTypes) {
                parameterTypesJSON.add(type.getName());
            }
            requestBody.setParameterTypes(parameterTypesJSON);
        }

        try {
            String sendBody = JSONUtils.toJSONString(requestBody);

            log.debug("request: \n{}", JSONUtils.toJSONString(requestBody, true));

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
            log.error("client send request error", e);
        }
        return rpcCallbackFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse httpResponse) throws Exception {
        ByteBuf buf = httpResponse.content();
        byte[] resp = new byte[buf.readableBytes()];
        buf.readBytes(resp);
        String body = new String(resp, "UTF-8");

        if (StringUtils.isEmpty(body)) {
            return;
        }

        String requestId = httpResponse.headers().get(HEADER_REQUEST_ID);

        RpcResponse rpcResponse = JSONUtils.parseObject(body, RpcResponse.class);
        if (rpcResponse.getSuccess()) {
            log.debug("response: \n{}", body);
            Object result = rpcResponse.getResult();
            if (null != result && null != rpcResponse.getReturnType() && !rpcResponse.getReturnType().equals(Void.class)) {
                Class<?> re = ReflectUtils.getClassType(rpcResponse.getReturnType());
                rpcResponse.setResult(JSONUtils.parseObject(JSONUtils.toJSONString(result), re));
            }
        }
        RpcCallbackFuture rpcCallbackFuture = mapCallBack.get(requestId);
        if (rpcCallbackFuture != null) {
            mapCallBack.remove(requestId);
            rpcCallbackFuture.done(rpcResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Http client accept error", cause);
        super.sendError(ctx, cause);
//        ctx.close();
    }

}