package junicorn.mrpc.inteceptor;

import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 拦截器执行器
 *
 * Created by biezhi on 2016/12/23.
 */
public class RpcInvocation {

    private Object target;
    private FastMethod fastMethod;
    private Method method;
    private Class<?>[] parameterTypes;
    private Class<?> returnType;
    private Object[] parameters;

    //拦截器
    private List<RpcInteceptor> interceptors;

    //当前Interceptor索引值，初始值：-1，范围：0-interceptors.size()-1
    private int currentIndex = -1;

    public RpcInvocation(Object target, FastMethod fastMethod, Class<?>[] parameterTypes, Object[] parameters, List<RpcInteceptor> interceptors) {
        this.target = target;
        this.fastMethod = fastMethod;
        this.method = fastMethod.getJavaMethod();
        this.returnType = method.getReturnType();
        this.parameterTypes = parameterTypes;
        this.parameters = parameters;
        this.interceptors = interceptors;
    }

    public Object getTarget() {
        return target;
    }

    public RpcInvocation setTarget(Object target) {
        this.target = target;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public RpcInvocation setMethod(Method method) {
        this.method = method;
        return this;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public RpcInvocation setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
        return this;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public RpcInvocation setParameters(Object[] parameters) {
        this.parameters = parameters;
        return this;
    }

    public Object next() throws Exception {
        if (this.currentIndex == this.interceptors.size() - 1) {
            return fastMethod.invoke(target, parameters);
        } else {
            RpcInteceptor interceptor = this.interceptors.get(++this.currentIndex);
            return interceptor.execute(this);
        }
    }

}
