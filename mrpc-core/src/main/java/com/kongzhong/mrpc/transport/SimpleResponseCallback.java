package com.kongzhong.mrpc.transport;

import com.google.common.base.Throwables;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.interceptor.InterceptorChain;
import com.kongzhong.mrpc.interceptor.Invocation;
import com.kongzhong.mrpc.interceptor.RpcInteceptor;
import com.kongzhong.mrpc.model.ExceptionMeta;
import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.server.RpcMapping;
import com.kongzhong.mrpc.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.kongzhong.mrpc.model.Const.INTERCEPTOR_NAME_PREFIX;

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
                interceptorChain.addLast(INTERCEPTOR_NAME_PREFIX + (pos--), rpcInteceptor);
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
    protected Object handle(RpcRequest request) throws Throwable {
        try {

            RpcContext.set(new RpcContext(request));

            String className = request.getClassName();
            Object serviceBean = handlerMap.get(className);

            if (null == serviceBean) {
                throw new RpcException("not found service [" + className + "]");
            }

            Class<?> serviceClass = serviceBean.getClass();
            String methodName = request.getMethodName();
            Class<?>[] parameterTypes = request.getParameterTypes();
            Object[] parameters = request.getParameters();

            Method method = ReflectUtils.method(serviceClass, methodName, parameterTypes);

            FastClass serviceFastClass = FastClass.create(serviceClass);
            FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);

            if (!hasInterceptors) {
                return serviceFastMethod.invoke(serviceBean, parameters);
            }

            //执行拦截器
            Invocation invocation = new Invocation(serviceFastMethod, serviceBean, parameters, request, interceptors);
            Object result = invocation.next();
            return result;
        } catch (Exception e) {
            throw e;
        } finally {
            RpcContext.remove();
        }
    }

    protected Throwable buildErrorResponse(Throwable t, RpcResponse response) throws IllegalAccessException {
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