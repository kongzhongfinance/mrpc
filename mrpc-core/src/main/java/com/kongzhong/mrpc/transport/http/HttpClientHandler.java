package com.kongzhong.mrpc.transport.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kongzhong.mrpc.client.RpcFuture;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.transport.SimpleRpcClientHandler;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * @author biezhi
 *         2017/4/19
 */
public class HttpClientHandler extends SimpleRpcClientHandler<Object> {

    public static final Logger log = LoggerFactory.getLogger(HttpClientHandler.class);

    /**
     * 每次客户端发送一次RPC请求的 时候调用.
     *
     * @param request
     * @return
     */
    @Override
    public RpcFuture sendRequest(RpcRequest rpcRequest) {
        RpcFuture rpcFuture = new RpcFuture(rpcRequest);
        mapCallBack.put(rpcRequest.getRequestId(), rpcFuture);

        JSONObject reqJSON = new JSONObject();
        reqJSON.put("service", rpcRequest.getClassName());
        reqJSON.put("method", rpcRequest.getMethodName());

        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] args = rpcRequest.getParameters();

        JSONArray parameterTypesJSON = new JSONArray();
        if (null != parameterTypes) {
            for (Class<?> type : parameterTypes) {
                parameterTypesJSON.add(type.getName());
            }
        }

        reqJSON.put("parameterArray", rpcRequest.getParameters());
        reqJSON.put("parameterTypes", parameterTypesJSON);
        reqJSON.put("requestId", rpcRequest.getRequestId());

        try {
            DefaultFullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/rpc");
            req.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE); // or HttpHeaders.Values.CLOSE
            req.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
            req.headers().add(HttpHeaders.Names.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
            ByteBuf bbuf = Unpooled.copiedBuffer(reqJSON.toJSONString(), StandardCharsets.UTF_8);
            req.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bbuf.readableBytes());
            req.content().clear().writeBytes(bbuf);

            channel.writeAndFlush(req);
        } catch (Exception e) {
            log.error("", e);
        }

        return rpcFuture;
    }

    private boolean isDone;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (isDone) {
            return;
        }
        if (msg instanceof HttpContent) {
            isDone = true;
            HttpContent content = (HttpContent) msg;
            ByteBuf buf = content.content();
            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);
            String body = new String(req, "UTF-8");

            if (StringUtils.isNotEmpty(body)) {
                RpcResponse rpcResponse = JSON.parseObject(body, RpcResponse.class);
                Object r = rpcResponse.getResult();
                if (r instanceof JSONObject) {
                    Class re = ReflectUtils.from(rpcResponse.getReturnType());
                    rpcResponse.setResult(JSON.parseObject(((JSONObject) r).toJSONString(), re));
                }
                log.debug("rpc http server response: {}", body);
                RpcFuture rpcFuture = mapCallBack.get(rpcResponse.getRequestId());
                if (rpcFuture != null) {
                    mapCallBack.remove(rpcResponse.getRequestId());
                    rpcFuture.done(rpcResponse);
                }
            }
        }
    }
}