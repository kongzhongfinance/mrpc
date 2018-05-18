package com.kongzhong.mrpc.server;

import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.interceptor.InterceptorChain;
import com.kongzhong.mrpc.interceptor.Invocation;
import com.kongzhong.mrpc.interceptor.RpcServerInterceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.kongzhong.mrpc.Const.SERVER_INTERCEPTOR_PREFIX;

/**
 * 抽象响应回调处理
 *
 * @param <T>
 */
@Slf4j
public abstract class AbstractResponseInvoker<T> implements Callable<T> {

    private   Map<String, ServiceBean>   serviceBeanMap  = null;
    private   List<RpcServerInterceptor> interceptors    = null;
    private   boolean                    hasInterceptors = false;
    protected RpcRequest                 request         = null;
    protected RpcResponse                response        = null;

    public AbstractResponseInvoker(RpcRequest request, RpcResponse response, Map<String, ServiceBean> serviceBeanMap) {
        this.request = request;
        this.response = response;
        this.serviceBeanMap = serviceBeanMap;
        this.interceptors = RpcMapping.me().getServerInterceptors();
        if (CollectionUtils.isNotEmpty(interceptors)) {
            hasInterceptors = true;
            int pos = interceptors.size();
            for (RpcServerInterceptor interceptor : interceptors) {
                InterceptorChain interceptorChain = new InterceptorChain();
                interceptorChain.addLast(SERVER_INTERCEPTOR_PREFIX + (pos--), interceptor);
            }
        }
    }

    @Override
    public abstract T call() throws Exception;

    /**
     * 执行请求的方法
     *
     * @param request Rpc请求
     * @return 返回执行方法后的返回值
     * @throws Throwable 当执行服务方法出现异常时抛出
     */
    protected Object invokeMethod(RpcRequest request) throws Throwable {
        String serviceName = request.getClassName();
        String methodName  = request.getMethodName();
        long   startTime   = System.currentTimeMillis();

        try {
            ServiceBean serviceBean = serviceBeanMap.get(serviceName);
            if (null == serviceBean) {
                throw new RpcException("Not found service bean define [" + serviceName + "]");
            }

            Object bean = serviceBean.getBean();
            if (null == bean) {
                throw new RpcException("Not found service bean [" + serviceName + "]");
            }

            Class<?>   serviceClass      = bean.getClass();
            Class<?>[] parameterTypes    = request.getParameterTypes();
            Object[]   parameters        = request.getParameters();
            FastClass  serviceFastClass  = FastClass.create(serviceClass);
            FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);

            if (!hasInterceptors) {
                return serviceFastMethod.invoke(bean, parameters);
            }

            //执行拦截器
            Invocation invocation = new ServerInvocation(serviceFastMethod, bean, parameters, request, interceptors);
            return invocation.next();
        } finally {
            log.debug("[{}.{}]", serviceName, methodName);
            log.debug("Request [{}] execute time: {}ms", request.getRequestId(), (System.currentTimeMillis() - startTime));
            RpcContext.remove();
        }
    }

    /**
     * 构建一个异常响应
     *
     * @param t        异常
     * @param response Rpc响应
     * @return 设置Rpc响应异常并返回当前异常
     */
    protected Throwable buildErrorResponse(Throwable t, RpcResponse response) {
        t = t instanceof InvocationTargetException ? ((InvocationTargetException) t).getTargetException() : t;
        String exception;
        String className;
        try {
            t.getClass().getConstructor();
            exception = JacksonSerialize.toJSONString(t);
            className = t.getClass().getName();
        } catch (Exception e) {
            exception = JacksonSerialize.toJSONString(new SystemException(t.getMessage(), e));
            className = SystemException.class.getName();
        }
        response.getContext().put(Const.SERVER_EXCEPTION, exception);
        response.setReturnType(className);
        response.setException(exception);
        response.setSuccess(false);

        return t;
    }

}