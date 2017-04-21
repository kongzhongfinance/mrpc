package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.enums.MediaType;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.utils.JSONUtils;
import com.kongzhong.mrpc.utils.ReflectUtils;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.Map;
import java.util.concurrent.Callable;

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
        initResponse();
        try {
            Object result = handle(request);
            if (null != result) {
                String body = getBody(result);
                response = new HttpResponse(HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(body, CharsetUtil.UTF_8));
                if (ReflectUtils.isBasic(result.getClass())) {
                    response.headers().set(CONTENT_TYPE, MediaType.TEXT);
                }
                initResponse();
            }
            return response;
        } catch (Throwable t) {
            response.setException(t);
            log.error("rpc server invoke error", t);
            return null;
        }
    }

    private void initResponse() {
        response.setRequestId(request.getRequestId());
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