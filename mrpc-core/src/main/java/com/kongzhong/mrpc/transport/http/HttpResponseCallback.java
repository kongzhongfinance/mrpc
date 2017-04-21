package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.utils.JSONUtils;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.Map;
import java.util.concurrent.Callable;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

public class HttpResponseCallback implements Callable<Boolean> {

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
    public Boolean call() throws Exception {
        response.setRequestId(request.getRequestId());
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");

        try {
            Object result = handle(request);
            if (null != result) {
                String jsonstr = JSONUtils.toJSONString(result);
                response.replace(Unpooled.copiedBuffer(jsonstr, CharsetUtil.UTF_8));
            }
            return Boolean.TRUE;
        } catch (Throwable t) {
            response.setException(t);
            log.error("rpc server invoke error", t);
            return Boolean.FALSE;
        }
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