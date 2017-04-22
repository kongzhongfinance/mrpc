package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.transport.SimpleResponseCallback;
import com.kongzhong.mrpc.utils.JSONUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpResponseCallback extends SimpleResponseCallback<HttpResponse> {

    public static final Logger log = LoggerFactory.getLogger(HttpResponseCallback.class);

    private HttpResponse response;

    public HttpResponseCallback(RpcRequest request, HttpResponse response, Map<String, Object> handlerMap) {
        this.request = request;
        this.response = response;
        this.handlerMap = handlerMap;
    }

    @Override
    public HttpResponse call() throws Exception {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(request.getRequestId());
        response.setRequestId(request.getRequestId());
        try {
            Object result = handle(request);
            rpcResponse.setResult(result);
            if (null != request.getReturnType()) {
                rpcResponse.setReturnType(request.getReturnType().getName());
            }
        } catch (Throwable t) {
            rpcResponse.setException(t);
            log.error("rpc server invoke error", t);
        } finally {
            String body = JSONUtils.toJSONString(rpcResponse);
            ByteBuf bbuf = Unpooled.copiedBuffer(body, StandardCharsets.UTF_8);
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bbuf.readableBytes());
            response.content().clear().writeBytes(bbuf);
            return response;
        }
    }

}