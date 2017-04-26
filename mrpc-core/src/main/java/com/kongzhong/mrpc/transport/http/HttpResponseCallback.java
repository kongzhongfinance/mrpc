package com.kongzhong.mrpc.transport.http;

import com.google.common.base.Throwables;
import com.kongzhong.mrpc.exception.HttpException;
import com.kongzhong.mrpc.model.RpcContext;
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

/**
 * Http响应回调处理
 */
public class HttpResponseCallback extends SimpleResponseCallback<HttpResponse> {

    public static final Logger log = LoggerFactory.getLogger(HttpResponseCallback.class);

    private HttpResponse httpResponse;

    public HttpResponseCallback(RpcRequest request, HttpResponse httpResponse, Map<String, Object> handlerMap) {
        super(request, null, handlerMap);
        this.httpResponse = httpResponse;
    }

    @Override
    public HttpResponse call() throws Exception {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(request.getRequestId());
        httpResponse.setRequestId(request.getRequestId());
        try {
            Object result = handle(request);
            rpcResponse.setResult(result);
            if (null != request.getReturnType()) {
                rpcResponse.setReturnType(request.getReturnType().getName());
            }
        } catch (Exception e) {
            rpcResponse.setException(Throwables.getStackTraceAsString(e));
            log.error("rpc server invoke error", e);
        } finally {
            RpcContext.remove();

            String body = JSONUtils.toJSONString(rpcResponse);
            ByteBuf bbuf = Unpooled.copiedBuffer(body, StandardCharsets.UTF_8);
            httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bbuf.readableBytes());
            httpResponse.content().clear().writeBytes(bbuf);
            return httpResponse;
        }
    }

}