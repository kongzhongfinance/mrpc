package com.kongzhong.mrpc.transport;

import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.interceptor.InterceptorChain;
import com.kongzhong.mrpc.interceptor.Invocation;
import com.kongzhong.mrpc.interceptor.RpcInteceptor;
import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.server.RpcMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
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
    protected List<RpcInteceptor> interceptors;
    protected InterceptorChain interceptorChain = new InterceptorChain();
    protected RpcRequest request;
    protected RpcResponse response;
    protected boolean hasInterceptors;

    public SimpleResponseCallback(RpcRequest request, RpcResponse response, Map<String, Object> handlerMap) {
        this.request = request;
        this.response = response;
        this.handlerMap = handlerMap;
        this.interceptors = RpcMapping.me().getInteceptors();
        if (null != interceptors && !interceptors.isEmpty()) {
            hasInterceptors = true;
            int pos = interceptors.size();
            for (RpcInteceptor rpcInteceptor : interceptors) {
                interceptorChain.addLast("mrpc:server:interceptor:" + (pos--), rpcInteceptor);
            }
        }
    }

    public abstract T call() throws Exception;

    /**
     * 执行请求的方法
     *
     * @param request
     * @return
     * @throws Throwable
     */
    protected Object handle(RpcRequest request) throws Exception {
        RpcContext.set();

        String className = request.getClassName();
        Object serviceBean = handlerMap.get(className);

        if (null == serviceBean) {
            throw new RpcException("not found service [" + className + "]");
        }

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        Method method = request.getMethod();


        if (!hasInterceptors) {
            return method.invoke(serviceBean, parameters);
        }

        //执行拦截器
        Invocation invocation = new Invocation(serviceBean, parameters, request, interceptors);
        return invocation.next();
    }

}