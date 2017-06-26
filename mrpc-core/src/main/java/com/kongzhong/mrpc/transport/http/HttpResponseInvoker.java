package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.server.AbstractResponseInvoker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Http响应回调处理
 */
@Slf4j
public class HttpResponseInvoker extends AbstractResponseInvoker<FullHttpResponse> {

    private FullHttpResponse httpResponse;

    public HttpResponseInvoker(RpcRequest request, FullHttpResponse httpResponse, Map<String, ServiceBean> serviceBeanMap) {
        super(request, null, serviceBeanMap);
        this.httpResponse = httpResponse;
    }

    @Override
    public FullHttpResponse call() throws Exception {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(request.getRequestId());
        try {
            Object result = super.invokeMethod(request);
            rpcResponse.setResult(result);
            if (null != request.getReturnType()) {
                rpcResponse.setReturnType(request.getReturnType().getName());
            }
            rpcResponse.setSuccess(true);
        } catch (Throwable e) {
            e = buildErrorResponse(e, rpcResponse);
            log.error("Rpc method processor error", e);
        } finally {
            RpcContext.remove();
            String body = JacksonSerialize.toJSONString(rpcResponse);
            ByteBuf bbuf = Unpooled.wrappedBuffer(body.getBytes(CharsetUtil.UTF_8));

            httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bbuf.readableBytes());
            httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, httpResponse.content().readableBytes());
            httpResponse.content().clear().writeBytes(bbuf);
            return httpResponse;
        }
    }

}