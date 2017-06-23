package com.kongzhong.mrpc.transport;

import com.google.common.base.Throwables;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.interceptor.InterceptorChain;
import com.kongzhong.mrpc.interceptor.Invocation;
import com.kongzhong.mrpc.interceptor.RpcServerInteceptor;
import com.kongzhong.mrpc.interceptor.ServerInvocation;
import com.kongzhong.mrpc.model.*;
import com.kongzhong.mrpc.server.RpcMapping;
import com.kongzhong.mrpc.utils.CollectionUtils;
import com.kongzhong.mrpc.utils.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
public abstract class SimpleResponseCallback<T> implements Callable<T> {

    protected Map<String, ServiceBean> serviceBeanMap;
    protected List<RpcServerInteceptor> interceptors;
    protected InterceptorChain interceptorChain = new InterceptorChain();
    protected RpcRequest request;
    protected RpcResponse response;
    protected boolean hasInterceptors;

    public SimpleResponseCallback(RpcRequest request, RpcResponse response, Map<String, ServiceBean> serviceBeanMap) {
        this.request = request;
        this.response = response;
        this.serviceBeanMap = serviceBeanMap;
        this.interceptors = RpcMapping.me().getInteceptors();
        if (CollectionUtils.isNotEmpty(interceptors)) {
            hasInterceptors = true;
            int pos = interceptors.size();
            for (RpcServerInteceptor rpcInteceptor : interceptors) {
                interceptorChain.addLast(SERVER_INTERCEPTOR_PREFIX + (pos--), rpcInteceptor);
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
    protected Object invokeMethod(RpcRequest request) throws Throwable {
        try {

            RpcContext.set(new RpcContext(request));
            String serviceName = request.getClassName();

            ServiceBean serviceBean = serviceBeanMap.get(serviceName);
            if (null == serviceBean) {
                throw new RpcException("Not found service bean define [" + serviceName + "]");
            }

            Object bean = serviceBean.getBean();
            if (null == bean) {
                throw new RpcException("Not found service bean [" + serviceName + "]");
            }

            Class<?> serviceClass = bean.getClass();
            String methodName = request.getMethodName();
            Class<?>[] parameterTypes = request.getParameterTypes();
            Object[] parameters = request.getParameters();

            Method method = ReflectUtils.method(serviceClass, methodName, parameterTypes);

            FastClass serviceFastClass = FastClass.create(serviceClass);
            FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);

            if (!hasInterceptors) {
                return serviceFastMethod.invoke(bean, parameters);
            }

            //执行拦截器
            Invocation invocation = new ServerInvocation(serviceFastMethod, bean, parameters, request, interceptors);
            Object result = invocation.next();
            return result;
        } catch (Exception e) {
            throw e;
        } finally {
            RpcContext.remove();
        }
    }

    /**
     * 构建一个异常响应
     *
     * @param t
     * @param response
     * @return
     * @throws IllegalAccessException
     */
    protected Throwable buildErrorResponse(Throwable t, RpcResponse response) throws IllegalAccessException {
        t = t instanceof InvocationTargetException ? ((InvocationTargetException) t).getTargetException() : t;
        Class<?> exceptionType = t.getClass();
        Field[] fields = exceptionType.getDeclaredFields();
        if (null != fields && fields.length > 0) {
            List<ExceptionMeta> ftypes = new ArrayList<>();
            for (Field field : fields) {
                if ("serialVersionUID".equals(field.getName())) {
                    continue;
                }
                Class<?> ftype = field.getType();
                field.setAccessible(true);
                ExceptionMeta exceptionMeta = new ExceptionMeta(ftype.getTypeName(), field.get(t));
                ftypes.add(exceptionMeta);
            }
            if (!ftypes.isEmpty()) {
                response.setResult(ftypes);
            }
        }

        String exceptionName = exceptionType.getName();
        String exception = Throwables.getStackTraceAsString(t).replace(exceptionName + ": ", "");
        exception = exception.replace(exceptionName, "");
        response.setReturnType(exceptionName);
        response.setException(exception);
        response.setMessage(t.getMessage());
        response.setSuccess(false);
        return t;
    }

}