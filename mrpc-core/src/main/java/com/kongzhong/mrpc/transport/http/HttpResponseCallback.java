package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.enums.MediaType;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.utils.JSONUtils;
import com.kongzhong.mrpc.utils.ReflectUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpResponseCallback implements Callable<HttpResponse> {

    public static final Logger log = LoggerFactory.getLogger(HttpResponseCallback.class);

    private Map<String, Object> handlerMap;
    private RpcRequest request;
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

    private String getBody(Object result) {
        if (ReflectUtils.isBasic(result.getClass())) {
            return result.toString();
        }
        return JSONUtils.toJSONString(result);
    }

    private Object handle(RpcRequest request) throws Throwable {
        String className = request.getClassName();
        Object serviceBean = handlerMap.get(className);

        if (null == serviceBean) {
            throw new RpcException("not found service [" + className + "]");
        }

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        // Cglib reflect
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);

        return serviceFastMethod.invoke(serviceBean, parameters);

    }

}