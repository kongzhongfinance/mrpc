package com.kongzhong.mrpc.transport.tcp;

import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.Map;
import java.util.concurrent.Callable;

public class ResponseCallback implements Callable<Boolean> {

    public static final Logger log = LoggerFactory.getLogger(ResponseCallback.class);

    private Map<String, Object> handlerMap;
    private RpcRequest request;
    private RpcResponse response;

    public ResponseCallback(RpcRequest request, RpcResponse response, Map<String, Object> handlerMap) {
        this.request = request;
        this.response = response;
        this.handlerMap = handlerMap;
    }

    @Override
    public Boolean call() throws Exception {
        response.setRequestId(request.getRequestId());
        try {
            Object result = handle(request);
            response.setResult(result);
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