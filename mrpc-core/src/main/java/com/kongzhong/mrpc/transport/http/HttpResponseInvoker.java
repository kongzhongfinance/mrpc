package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.model.ServiceStatusTable;
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

import static com.kongzhong.mrpc.Const.HEADER_METHOD_NAME;
import static com.kongzhong.mrpc.Const.HEADER_REQUEST_ID;
import static com.kongzhong.mrpc.Const.HEADER_SERVICE_CLASS;

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
            ServiceStatusTable.me().addSuccessInvoke(request.getClassName());
        } catch (Throwable e) {
            e = buildErrorResponse(e, rpcResponse);
            log.error("Service method invoke error", e);
            ServiceStatusTable.me().addErrorInvoke(request.getClassName());
        } finally {
            RpcContext.remove();
            String body = JacksonSerialize.toJSONString(rpcResponse);
            ByteBuf byteBuf = Unpooled.wrappedBuffer(body.getBytes(CharsetUtil.UTF_8));

            httpResponse.content().clear().writeBytes(byteBuf);
            httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, httpResponse.content().readableBytes());
            return httpResponse;
        }
    }

}