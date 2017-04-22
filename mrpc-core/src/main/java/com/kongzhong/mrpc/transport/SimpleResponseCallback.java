package com.kongzhong.mrpc.transport;

import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 抽象响应回调处理
 *
 * @param <T>
 */
public abstract class SimpleResponseCallback<T> implements Callable<T> {

    public static final Logger log = LoggerFactory.getLogger(SimpleResponseCallback.class);

    protected Map<String, Object> handlerMap;
    protected RpcRequest request;
    protected RpcResponse response;

    public SimpleResponseCallback(RpcRequest request, RpcResponse response, Map<String, Object> handlerMap) {
        this.request = request;
        this.response = response;
        this.handlerMap = handlerMap;
    }

    public SimpleResponseCallback() {

    }

    public abstract T call() throws Exception;

    protected Object handle(RpcRequest request) throws Throwable {
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